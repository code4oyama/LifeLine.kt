package com.example.lifelineexam02

import MapFragment
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.lifelineexam02.network.SakuraApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapActivity : FragmentActivity() {

    // onCreateの実装
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        Log.i("JunSato20250123.0515", "intent is $intent")

        // MainActivity から渡された値を取得
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val victimCurrentLocation = Pair(latitude, longitude)

        Log.i("JunSato20250123.0507", "latitude is $latitude")
        Log.i("JunSato20250123.0507", "longitude is $longitude")

        // コルーチンで非同期処理を実行
        lifecycleScope.launch {
            // getShelterListが1件以上のデータを返すまで待つ
            val shelterList = fetchShelterListWithRetry()

            // Fragmentを動的に追加
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.map_container, MapFragment(victimCurrentLocation, shelterList))
                    .commit()
            }
        }

        // 戻るボタンの設定
        setupBackButton()
    }

    private fun setupBackButton() {
        val backButton = findViewById<Button>(R.id.btn_back_to_search)
        backButton.setOnClickListener {
            // 前の画面（MainActivity）に戻る
            finish()
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_map)
//
//        Log.i("JunSato20250123.0515", "intent is $intent")
//
//        // MainActivity から渡された値を取得
//        val victimCurrentLocation: Pair<Double, Double>
//
//        val latitude = intent.getDoubleExtra("latitude", 0.0)
//        val longitude = intent.getDoubleExtra("longitude", 0.0)
//
//        victimCurrentLocation = Pair(latitude, longitude)
//
//        Log.i("JunSato20250123.0507", "latitude is $latitude")
//        Log.i("JunSato20250123.0507", "longitude is $longitude")
//
//        val shelterList = getShelterList()
//
//        // Fragment を動的に追加
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.map_container, MapFragment(victimCurrentLocation, shelterList))
//                .commit()
//        }
//    }

//    private fun getShelterList(): MutableList<Pair<Double, Double>> {
//        val apiService = KintoneApiService()
//
//        println("Kintone APIからデータを取得中...")
//
//        // 緯度と経度を蓄積するリスト
//        val coordinates = mutableListOf<Pair<Double, Double>>()
//
//        apiService.fetchKintoneData { response: RecordsResponse? ->
//            if (response != null) {
//                println("データ取得に成功しました:")
//                response.records.forEachIndexed { index, record ->
//                    Log.i("JunSato20250124.0137", "Record is $record")
//
//                    // JSON文字列をJSONObjectに変換
//                    val jsonObject = JSONObject(record)
//
//                    // 緯度と経度を取得してリストに追加
//                    val latitude = jsonObject.getJSONObject("数値_0").getDouble("value")
//                    val longitude = jsonObject.getJSONObject("数値_1").getDouble("value")
//
//                    coordinates.add(Pair(latitude, longitude))
//                    println("Record $index: 緯度 = $latitude, 経度 = $longitude")
//                    Log.i("JunSato20250124.0108", "Record $index: 緯度 = $latitude, 経度 = $longitude")
//                }
//
//                // 全ての緯度経度を表示
//                println("蓄積した緯度経度のリスト: $coordinates")
//            } else {
//                println("データ取得に失敗しました。")
//            }
//        }
//
//        // 非同期処理が完了するまで待つ（簡易的な方法）
//        Thread.sleep(3000) // 実運用ではコルーチンや他の非同期手法を使用してください。
//
//        return coordinates
//    }
}

//// 避難所リストを取得するメソッド
//private suspend fun getShelterList(): List<Pair<Double, Double>> {
//    val apiService = KintoneApiService()
//    val coordinates = mutableListOf<Pair<Double, Double>>()
//
//    println("Kintone APIからデータを取得中...")
//    try {
//        val response = apiService.fetchKintoneData()
//        if (response != null && response.records.isNotEmpty()) {
//            println("データ取得に成功しました:")
//            response.records.forEachIndexed { index, record ->
//                try {
//                    val jsonObject = JSONObject(record)
//                    // フィールドの存在と型を安全に確認
//                    if (jsonObject.has(LATITUDE_FIELD) && jsonObject.has(LONGITUDE_FIELD)) {
//                        val latitude = jsonObject.getJSONObject(LATITUDE_FIELD).optDouble("value", Double.NaN)
//                        val longitude = jsonObject.getJSONObject(LONGITUDE_FIELD).optDouble("value", Double.NaN)
//
//                        if (!latitude.isNaN() && !longitude.isNaN()) {
//                            coordinates.add(Pair(latitude, longitude))
//                            println("Record $index: 緯度 = $latitude, 経度 = $longitude")
//                            Log.i("JunSato20250124.0108", "Record $index: 緯度 = $latitude, 経度 = $longitude")
//                        } else {
//                            println("Record $index: 無効なデータ（緯度または経度が無効）")
//                        }
//                    } else {
//                        println("Record $index: 必要なフィールドが存在しません")
//                    }
//                } catch (e: Exception) {
//                    println("Record $index のパースに失敗: ${e.message}")
//                    Log.e("JunSato20250124.0137", "Record parse error: ${e.message}")
//                }
//            }
//        } else {
//            println("データ取得に失敗しました: レスポンスが空またはnull")
//        }
//    } catch (e: Exception) {
//        println("データ取得に失敗しました: ${e.message}")
//        Log.e("JunSato20250124.0137", "Fetch error: ${e.message}")
//    }
//
//    println("蓄積した緯度経度のリスト: $coordinates")
//    return coordinates
//}
//
//// 呼び出し例（例: AndroidのViewModelやActivityから）
//fun main() = runBlocking {
//    val coordinates = getShelterList()
//    println("最終結果: $coordinates")
//}

// getShelterListをリトライ付きで実行
private suspend fun fetchShelterListWithRetry(
    maxRetries: Int = 3,
    retryDelayMs: Long = 2000L
): List<Pair<Double, Double>> {
    var attempt = 0
    while (attempt < maxRetries) {
        val shelterList = getShelterList()
        if (shelterList.isNotEmpty()) {
            Log.i("ShelterList", "避難所リスト取得成功: $shelterList")
            return shelterList
        }
        Log.w("ShelterList", "試行 $attempt: 避難所リストが空です。リトライします...")
        attempt++
        delay(retryDelayMs) // 2秒待機
    }
    Log.e("ShelterList", "最大リトライ回数($maxRetries)に達しました。空のリストを返します。")
    return emptyList() // 最大リトライ回数を超えた場合は空のリストを返す
}

// 避難所リストを取得（コルーチン版）
private suspend fun getShelterList(): List<Pair<Double, Double>> {
    val apiService = SakuraApiService()
    val coordinates = mutableListOf<Pair<Double, Double>>()

    println("さくらサーバーのPHP APIからデータを取得中...")
    try {
        val response = apiService.fetchShelterData()
        if (response != null && response.shelters.isNotEmpty()) {
            println("データ取得に成功しました:")
            response.shelters.forEachIndexed { index, shelter ->
                try {
                    val latitude = shelter.latitude
                    val longitude = shelter.longitude

                    if (!latitude.isNaN() && !longitude.isNaN()) {
                        coordinates.add(Pair(latitude, longitude))
                        println("Shelter $index: ${shelter.name} - 緯度 = $latitude, 経度 = $longitude")
                        Log.i("ShelterData", "Shelter $index: ${shelter.name} - 緯度 = $latitude, 経度 = $longitude")
                    } else {
                        println("Shelter $index: 無効なデータ（緯度または経度が無効）")
                    }
                } catch (e: Exception) {
                    println("Shelter $index のパースに失敗: ${e.message}")
                    Log.e("ShelterData", "Shelter parse error: ${e.message}")
                }
            }
        } else {
            println("データ取得に失敗しました: レスポンスが空またはnull")
        }
    } catch (e: Exception) {
        println("データ取得に失敗しました: ${e.message}")
        Log.e("ShelterData", "Fetch error: ${e.message}")
    }

    println("蓄積した緯度経度のリスト: $coordinates")
    return coordinates
}

