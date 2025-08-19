package com.example.lifelineexam02.network

import android.content.ContentValues.TAG
import android.util.Log
import com.squareup.moshi.JsonAdapter
import okhttp3.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import kotlin.coroutines.resume


// Kintone APIのURLとAPIトークンを設定
const val apiUrl = "https://your.cybozu.com/k/v1/records.json?app=1"

// Moshiライブラリを使用してJSONをパースするためのセットアップ
val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
val jsonAdapter: JsonAdapter<RecordsResponse> = moshi.adapter(RecordsResponse::class.java)

class KintoneApiService {

    // OkHttpClientを使ってリクエストを送信
    private val client = OkHttpClient()

//    // Kintoneからデータを取得するメソッド
//    fun fetchKintoneData(callback: (RecordsResponse?) -> Unit) {
//
//        // Kintone APIトークンを設定
//        val apiToken = loadApiToken()
//
//        val request = Request.Builder()
//            .url(apiUrl)
//            .addHeader("X-Cybozu-API-Token", apiToken.toString())
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                println("リクエストに失敗しました: ${e.message}")
//                callback(null)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body?.string()
//                    if (responseBody != null) {
//                        val recordsResponse = jsonAdapter.fromJson(responseBody)
//                        callback(recordsResponse)
//                    }
//                } else {
//                    println("リクエストに失敗しました: ${response.code}")
//                    callback(null)
//                }
//            }
//        })
//    }

    companion object {
        private fun loadApiToken(): String? {
            val filePath = "hidden_dir/KintoneApiToken.txt"
            return try {
                File(filePath).readText().trim() // ファイルの内容を取得し、前後の空白を削除
            } catch (e: Exception) {
                Log.e(TAG, "API Token の読み込みに失敗しました: $filePath", e)
                null            }
        }
    }

    // Kintoneからデータを取得するメソッド（コルーチン対応）
    suspend fun fetchKintoneData(): RecordsResponse? = suspendCancellableCoroutine { continuation ->
//        // Kintone APIトークンを安全に読み込む（例: 環境変数や設定ファイルから）
//        val loadApiToken = loadApiToken() ?: run {
//            println("APIトークンの読み込みに失敗しました")
//            continuation.resume(null)
//            return@suspendCancellableCoroutine
//        }

        val apiToken = "FVXREcLTr3x24C7pTQMAZGVbozpqFtoB3B9VMZ38"

        // API URLを動的に構成（例）
//        val appId = "YOUR_APP_ID" // アプリIDを適切に設定
        val apiUrl = apiUrl

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("X-Cybozu-API-Token", apiToken)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("fetchKintoneData", "リクエストに失敗しました: ${e.message}")
                println("リクエストに失敗しました: ${e.message}")
                continuation.resume(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            try {
                                val recordsResponse = jsonAdapter.fromJson(responseBody)
                                continuation.resume(recordsResponse)
                            } catch (e: Exception) {
                                Log.e("fetchKintoneData", "JSONパースエラー: ${e.message}")
                                println("JSONパースエラー: ${e.message}")
                                continuation.resume(null)
                            }
                        } else {
                            Log.e("fetchKintoneData", "レスポンスボディが空です")
                            println("レスポンスボディが空です")
                            continuation.resume(null)
                        }
                    } else {
                        Log.e("fetchKintoneData", "リクエストに失敗しました: HTTP ${response.code} - ${response.message}")
                        println("リクエストに失敗しました: HTTP ${response.code} - ${response.message}")
                        continuation.resume(null)
                    }
                }
            }
        })
    }
}

// Kintone APIレスポンスをパースするためのデータクラス
data class RecordsResponse(val records: List<Map<String, Any>>)



