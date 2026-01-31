package com.example.traverse2.data.api

import android.content.Context
import android.content.SharedPreferences
import com.example.traverse2.BuildConfig
import com.example.traverse2.data.SessionManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    private var sessionManager: SessionManager? = null
    
    // SharedPreferences-backed cookie storage for persistence across app restarts
    private const val COOKIE_PREFS = "cookies"
    private var cookiePrefs: SharedPreferences? = null
    
    // In-memory cache of cookies (loaded from SharedPreferences on init)
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
    
    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val host = url.host
            if (cookieStore[host] == null) {
                cookieStore[host] = mutableListOf()
            }
            cookies.forEach { newCookie ->
                cookieStore[host]?.removeAll { it.name == newCookie.name }
                cookieStore[host]?.add(newCookie)
                
                // Persist auth token to SessionManager
                if (newCookie.name == "auth_token" && sessionManager != null) {
                    runBlocking { sessionManager?.saveAuthToken(newCookie.value) }
                }
            }
            persistCookies(host)
        }
        
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }
    
    private fun persistCookies(host: String) {
        val cookies = cookieStore[host] ?: return
        val prefs = cookiePrefs ?: return
        val cookieString = cookies
            .filter { it.expiresAt > System.currentTimeMillis() }
            .joinToString("|") { "${it.name}=${it.value}:${it.expiresAt}:${it.domain}:${it.path}:${it.secure}:${it.httpOnly}" }
        prefs.edit().putString(host, cookieString).apply()
    }
    
    private fun loadCookiesFromPrefs() {
        val prefs = cookiePrefs ?: return
        val baseUrl = BuildConfig.API_BASE_URL.toHttpUrlOrNull() ?: return
        val host = baseUrl.host
        val cookieString = prefs.getString(host, null) ?: return
        if (cookieString.isEmpty()) return
        
        val cookies = mutableListOf<Cookie>()
        cookieString.split("|").forEach { cookieData ->
            try {
                val parts = cookieData.split(":")
                if (parts.size >= 6) {
                    val nameValue = parts[0].split("=", limit = 2)
                    if (nameValue.size == 2) {
                        val cookie = Cookie.Builder()
                            .name(nameValue[0])
                            .value(nameValue[1])
                            .expiresAt(parts[1].toLongOrNull() ?: 0L)
                            .domain(parts[2])
                            .path(parts[3])
                            .apply {
                                if (parts[4] == "true") secure()
                                if (parts[5] == "true") httpOnly()
                            }
                            .build()
                        if (cookie.expiresAt > System.currentTimeMillis()) {
                            cookies.add(cookie)
                        }
                    }
                }
            } catch (e: Exception) { /* Skip malformed */ }
        }
        if (cookies.isNotEmpty()) {
            cookieStore[host] = cookies.toMutableList()
        }
    }
    
    // Auth interceptor - adds token from SessionManager if cookies are missing
    private val authInterceptor = Interceptor { chain ->
        var request = chain.request()
        val hasCookie = cookieStore.values.flatten().any { it.name == "auth_token" }
        
        if (!hasCookie && sessionManager != null) {
            val token = runBlocking { sessionManager?.getAuthTokenSync() }
            if (!token.isNullOrEmpty()) {
                request = request.newBuilder()
                    .addHeader("Cookie", "auth_token=$token")
                    .build()
            }
        }
        chain.proceed(request)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
    }
    
    private var cache: Cache? = null
    
    // Cache responses - use cache for 10 days, revalidate after 1 minute
    private val cacheInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        val path = request.url.encodedPath
        
        // Cache all GET requests except auth endpoints
        val shouldCache = request.method == "GET" && !path.contains("/auth/")
        
        if (shouldCache) {
            response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "public, max-age=60, max-stale=864000") // Fresh 1 min, stale 10 days
                .build()
        } else {
            response
        }
    }
    
    // Force cache when offline
    private val offlineCacheInterceptor = Interceptor { chain ->
        var request = chain.request()
        if (!isNetworkAvailable) {
            request = request.newBuilder()
                .cacheControl(CacheControl.Builder().maxStale(10, TimeUnit.DAYS).build())
                .build()
        }
        chain.proceed(request)
    }
    
    @Volatile
    private var isNetworkAvailable = true
    
    fun setNetworkAvailable(available: Boolean) {
        isNetworkAvailable = available
    }
    
    private var okHttpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var _api: TraverseApi? = null
    
    fun init(context: Context) {
        if (okHttpClient != null) return
        
        sessionManager = SessionManager.getInstance(context)
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE)
        loadCookiesFromPrefs()
        
        cache = Cache(File(context.cacheDir, "http_cache"), 10L * 1024L * 1024L)
        
        okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .addInterceptor(offlineCacheInterceptor)
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient!!)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        
        _api = retrofit!!.create(TraverseApi::class.java)
    }
    
    val api: TraverseApi
        get() = _api ?: throw IllegalStateException("RetrofitClient not initialized. Call init() first.")
    
    fun clearCookies() {
        cookieStore.clear()
        cookiePrefs?.edit()?.clear()?.apply()
    }
    
    fun clearCache() {
        try { cache?.evictAll() } catch (e: Exception) { }
    }
    
    fun hasAuthCookie(): Boolean {
        return cookieStore.values.flatten().any { it.name == "auth_token" }
    }
}
