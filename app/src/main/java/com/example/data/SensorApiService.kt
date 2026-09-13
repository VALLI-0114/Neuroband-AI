package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SensorPayload(
    @Json(name = "heartRate") val heartRate: Int = 72,
    @Json(name = "temperature") val temperature: Double = 36.6,
    @Json(name = "stressGsr") val stressGsr: Double = 2.1,
    @Json(name = "bloodPressureSystolic") val bloodPressureSystolic: Int = 118,
    @Json(name = "bloodPressureDiastolic") val bloodPressureDiastolic: Int = 76,
    @Json(name = "steps") val steps: Int = 0,
    @Json(name = "calories") val calories: Int = 0,
    @Json(name = "stairs") val stairs: Int = 0,
    @Json(name = "acceleration") val acceleration: Double = 1.0,
    @Json(name = "gyro") val gyro: Double = 0.0,
    @Json(name = "ax") val ax: Double = 0.0,
    @Json(name = "ay") val ay: Double = 0.0,
    @Json(name = "az") val az: Double = 1.0,
    @Json(name = "gx") val gx: Double = 0.0,
    @Json(name = "gy") val gy: Double = 0.0,
    @Json(name = "gz") val gz: Double = 0.0,
    @Json(name = "rawRed") val rawRed: Long = 0,
    @Json(name = "rawIr") val rawIr: Long = 0,
    @Json(name = "fingerDetected") val fingerDetected: Boolean = false,
    @Json(name = "connected") val connected: Boolean = true,
    @Json(name = "sensorStatus") val sensorStatus: String = "ACTIVE",
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class SensorStatusResponse(
    @Json(name = "status") val status: String = "ONLINE",
    @Json(name = "device") val device: String = "Raspberry Pi NeuroBand Node",
    @Json(name = "sensors") val sensors: List<String> = emptyList()
)

class SensorApiClient {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(2, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val payloadAdapter = moshi.adapter(SensorPayload::class.java)
    private val statusAdapter = moshi.adapter(SensorStatusResponse::class.java)

    /**
     * Normalizes the host URL to ensure valid http:// formatting and port 5000 default.
     */
    fun formatBaseUrl(inputIpOrHost: String): String {
        var host = inputIpOrHost.trim()
        if (host.isEmpty()) host = "192.168.1.50"
        
        if (!host.startsWith("http://") && !host.startsWith("https://")) {
            host = "http://$host"
        }
        
        // If no port is specified, default to 5000
        val uriWithoutScheme = host.removePrefix("http://").removePrefix("https://")
        if (!uriWithoutScheme.contains(":")) {
            host = "$host:5000"
        }
        
        return host.removeSuffix("/")
    }

    suspend fun fetchBiometrics(baseUrl: String): Result<SensorPayload> = withContext(Dispatchers.IO) {
        try {
            val url = "${formatBaseUrl(baseUrl)}/api/biometrics"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP Error ${response.code}"))
                }
                val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                val payload = payloadAdapter.fromJson(bodyString) ?: throw Exception("JSON parse failed")
                Result.success(payload)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(baseUrl: String): Result<SensorStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val url = "${formatBaseUrl(baseUrl)}/api/status"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP Error ${response.code}"))
                }
                val bodyString = response.body?.string() ?: throw Exception("Empty response body")
                val status = statusAdapter.fromJson(bodyString) ?: throw Exception("JSON parse failed")
                Result.success(status)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
