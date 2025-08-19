package com.example.lifelineexam02.network

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume

// Google Places API Nearby Search用のデータクラス
data class PlacesResponse(
    val results: List<Place>,
    val status: String
)

data class Place(
    val name: String,
    val place_id: String,
    val types: List<String>,
    val vicinity: String?,
    val geometry: Geometry?
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

class GooglePlacesApiService {
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val jsonAdapter: JsonAdapter<PlacesResponse> = moshi.adapter(PlacesResponse::class.java)

    companion object {
        private const val TAG = "GooglePlacesAPI"
        // Google Places API キー (AndroidManifest.xmlと同じものを使用)
        private const val API_KEY = "AIzaSyBG3aaQUlXmnme0qHW5TSsjU0lc2G7tuBc"
        private const val PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    }

    suspend fun searchNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radius: Int = 500,
        type: String = "store"
    ): PlacesResponse? = suspendCancellableCoroutine { continuation ->
        
        val url = "$PLACES_API_URL?location=$latitude,$longitude&radius=$radius&type=$type&key=$API_KEY&language=ja"
        
        Log.d(TAG, "Places API URL: $url")
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Places API リクエスト失敗: ${e.message}")
                continuation.resume(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                Log.d(TAG, "Places API レスポンス: $responseBody")
                                val placesResponse = jsonAdapter.fromJson(responseBody)
                                continuation.resume(placesResponse)
                            } catch (e: Exception) {
                                Log.e(TAG, "Places API JSONパースエラー: ${e.message}")
                                continuation.resume(null)
                            }
                        } else {
                            Log.e(TAG, "Places API レスポンスボディが空です")
                            continuation.resume(null)
                        }
                    } else {
                        Log.e(TAG, "Places API リクエスト失敗: HTTP ${response.code} - ${response.message}")
                        continuation.resume(null)
                    }
                }
            }
        })
    }

    suspend fun searchNearbyConvenienceStores(
        latitude: Double,
        longitude: Double
    ): String? {
        try {
            // コンビニエンスストアを検索
            val response = searchNearbyPlaces(
                latitude, longitude, 
                radius = 300, 
                type = "convenience_store"
            )
            
            if (response != null && response.results.isNotEmpty()) {
                val nearestStore = response.results[0]
                Log.i(TAG, "最寄りのコンビニ: ${nearestStore.name}")
                return nearestStore.name
            }
            
            // コンビニが見つからない場合は一般的な店舗を検索
            val storeResponse = searchNearbyPlaces(
                latitude, longitude,
                radius = 500,
                type = "store"
            )
            
            if (storeResponse != null && storeResponse.results.isNotEmpty()) {
                val nearestStore = storeResponse.results[0]
                Log.i(TAG, "最寄りの店舗: ${nearestStore.name}")
                return nearestStore.name
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Places API検索エラー: ${e.message}")
        }
        
        return null
    }
}
