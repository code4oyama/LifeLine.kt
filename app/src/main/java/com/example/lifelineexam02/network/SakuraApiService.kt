package com.example.lifelineexam02.network

import android.util.Log
import com.squareup.moshi.JsonAdapter
import okhttp3.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// さくらインターネットのレンタルサーバーのPHP Web API URL
// TODO: 実際のさくらサーバーのURLに変更してください
// 例: "https://yourname.sakura.ne.jp/api/shelters.php"
// または データベース版を使用する場合: "https://yourname.sakura.ne.jp/api/shelters_db.php"
const val sakuraApiUrl = "https://yourname.sakura.ne.jp/shelters.php"

// Moshiライブラリを使用してJSONをパースするためのセットアップ
val sakuraMoshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
val sakuraJsonAdapter: JsonAdapter<ShelterResponse> = sakuraMoshi.adapter(ShelterResponse::class.java)

class SakuraApiService {

    // OkHttpClientを使ってリクエストを送信
    private val client = OkHttpClient()

    companion object {
        private const val TAG = "SakuraApiService"
    }

    // さくらサーバーのPHP APIからデータを取得するメソッド（コルーチン対応）
    suspend fun fetchShelterData(): ShelterResponse? = suspendCancellableCoroutine { continuation ->
        
        val request = Request.Builder()
            .url(sakuraApiUrl)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "リクエストに失敗しました: ${e.message}")
                println("リクエストに失敗しました: ${e.message}")
                continuation.resume(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                val shelterResponse = sakuraJsonAdapter.fromJson(responseBody)
                                Log.d(TAG, "データ取得成功: ${shelterResponse?.shelters?.size} 件")
                                continuation.resume(shelterResponse)
                            } catch (e: Exception) {
                                Log.e(TAG, "JSONパースエラー: ${e.message}")
                                println("JSONパースエラー: ${e.message}")
                                continuation.resume(null)
                            }
                        } else {
                            Log.e(TAG, "レスポンスボディが空です")
                            println("レスポンスボディが空です")
                            continuation.resume(null)
                        }
                    } else {
                        Log.e(TAG, "リクエストに失敗しました: HTTP ${response.code} - ${response.message}")
                        println("リクエストに失敗しました: HTTP ${response.code} - ${response.message}")
                        continuation.resume(null)
                    }
                }
            }
        })
    }
}

// さくらサーバーのPHP APIレスポンスをパースするためのデータクラス
data class ShelterResponse(
    val status: String,
    val shelters: List<Shelter>
)

data class Shelter(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val capacity: Int? = null
)
