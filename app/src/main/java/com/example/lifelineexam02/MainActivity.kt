package com.example.lifelineexam02

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Address
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.lifelineexam02.network.GooglePlacesApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var latitude: Double? = null
    var longitude: Double? = null

    private lateinit var edtElderly: EditText
    private lateinit var edtBaby: EditText
    private lateinit var edtMale: EditText
    private lateinit var edtFemale: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnClear: Button
    private lateinit var tvLocationInfo: TextView
    private lateinit var tvCoordinates: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ビューの初期化
        edtElderly = findViewById(R.id.edtElderly)
        edtBaby = findViewById(R.id.edtBaby)
        edtMale = findViewById(R.id.edtMale)
        edtFemale = findViewById(R.id.edtFemale)
        btnSearch = findViewById(R.id.btnSearch)
        btnClear = findViewById(R.id.btnClear)
        tvLocationInfo = findViewById(R.id.tvLocationInfo)
        tvCoordinates = findViewById(R.id.tvCoordinates)

        // ActivityResultLauncher の初期化
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            if (isLocationGranted) {
                getCurrentLocation()
            } else {
                println("位置情報の権限が拒否されました")
                tvLocationInfo.text = "位置情報の権限が必要です"
                tvCoordinates.text = ""
            }
        }

        requestPermissions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }

        // ボタンのクリックイベントを設定
        btnSearch.setOnClickListener {
            val elderlyCount = edtElderly.text.toString()
            val babyCount = edtBaby.text.toString()
            val maleCount = edtMale.text.toString()
            val femaleCount = edtFemale.text.toString()

            // どれか一つでも1以上の値が入力されているかチェック
            val elderlyNum = elderlyCount.toIntOrNull() ?: 0
            val babyNum = babyCount.toIntOrNull() ?: 0
            val maleNum = maleCount.toIntOrNull() ?: 0
            val femaleNum = femaleCount.toIntOrNull() ?: 0

            if (elderlyNum >= 1 || babyNum >= 1 || maleNum >= 1 || femaleNum >= 1) {

                Toast.makeText(
                    this, "避難場所検索中", Toast.LENGTH_SHORT
                ).show()

                // MapActivity を起動
                val intent = Intent(this, MapActivity::class.java)

                intent.putExtra("elderlyCount", elderlyNum)
                intent.putExtra("babyCount", babyNum)
                intent.putExtra("maleCount", maleNum)
                intent.putExtra("femaleCount", femaleNum)

                Log.i("JunSato20250123.0506", "latitude is $latitude")
                Log.i("JunSato20250123.0506", "longitude is $longitude")

                intent.putExtra("latitude", latitude)
                intent.putExtra("longitude", longitude)

                startActivity(intent)
            } else {
                Toast.makeText(
                    this, "いずれかの人数に1以上の値を入力してください", Toast.LENGTH_SHORT
                ).show()
            }
        }

        // クリアボタンのクリックイベントを設定
        btnClear.setOnClickListener {
            clearAllInputs()
        }
    }

    private fun clearAllInputs() {
        edtElderly.text.clear()
        edtBaby.text.clear()
        edtMale.text.clear()
        edtFemale.text.clear()
        
        Toast.makeText(
            this, "入力値をクリアしました", Toast.LENGTH_SHORT
        ).show()
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        )
    }

    private fun checkLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 表示を更新
        tvLocationInfo.text = "GPSから現在地を取得中..."
        tvCoordinates.text = "位置情報を取得しています..."

        // CancellationTokenSourceを作成
        val cancellationTokenSource = CancellationTokenSource()

        // getCurrentLocationを使用してリアルタイムでGPSから位置情報を取得
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                this.latitude = location.latitude
                this.longitude = location.longitude
                println("GPS現在地: 緯度 = $latitude, 経度 = $longitude")
                Log.i("GPS", "現在地取得成功: 緯度 = $latitude, 経度 = $longitude")
                
                // UIを更新
                updateLocationDisplay(latitude!!, longitude!!)
                
                                    // 最寄りの店舗を検索
                    searchNearestStore(latitude!!, longitude!!)
                } else {
                    println("GPS位置情報を取得できませんでした")
                    Log.w("GPS", "位置情報がnullです")
                    
                    // フォールバック: lastLocationを試す
                    getLastLocationAsFallback()
                }
            }.addOnFailureListener { exception ->
                println("GPS位置情報の取得中にエラーが発生しました: ${exception.message}")
                Log.e("GPS", "位置情報取得エラー: ${exception.message}")
                
                // フォールバック: lastLocationを試す
                getLastLocationAsFallback()
            }
    }

    private fun searchNearestStore(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Geocoderで詳細な住所情報を取得してから店舗検索
                searchWithDetailedGeocoder(lat, lng)
                
            } catch (e: Exception) {
                Log.e("LocationSearch", "店舗検索エラー: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvLocationInfo.text = "位置情報の取得に失敗しました"
                }
            }
        }
    }

    private suspend fun searchWithDetailedGeocoder(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 15)
            
            if (addresses != null && addresses.isNotEmpty()) {
                var nearestStore: String? = null
                var storeAddress: String? = null
                
                // デバッグ: 取得した住所情報をログ出力
                Log.d("LocationDebug", "取得した住所数: ${addresses.size}")
                addresses.forEachIndexed { index, address ->
                    Log.d("LocationDebug", "Address $index:")
                    Log.d("LocationDebug", "  addressLine: ${address.getAddressLine(0)}")
                    Log.d("LocationDebug", "  featureName: ${address.featureName}")
                    Log.d("LocationDebug", "  premises: ${address.premises}")
                    Log.d("LocationDebug", "  subThoroughfare: ${address.subThoroughfare}")
                    Log.d("LocationDebug", "  thoroughfare: ${address.thoroughfare}")
                    Log.d("LocationDebug", "  locality: ${address.locality}")
                    Log.d("LocationDebug", "  subLocality: ${address.subLocality}")
                    Log.d("LocationDebug", "  adminArea: ${address.adminArea}")
                    Log.d("LocationDebug", "  subAdminArea: ${address.subAdminArea}")
                }
                
                // 住所リストから店舗や建物を探す
                for (address in addresses) {
                    val addressLine = address.getAddressLine(0) ?: ""
                    val featureName = address.featureName ?: ""
                    val premises = address.premises ?: ""
                    val subThoroughfare = address.subThoroughfare ?: ""
                    val thoroughfare = address.thoroughfare ?: ""
                    val locality = address.locality ?: ""
                    val subLocality = address.subLocality ?: ""
                    val adminArea = address.adminArea ?: ""
                    val subAdminArea = address.subAdminArea ?: ""
                    
                    // より幅広い店舗・施設キーワードで検索
                    val storeKeywords = listOf(
                        // コンビニ
                        "セブンイレブン", "セブン-イレブン", "7-Eleven", "7-11",
                        "ファミリーマート", "FamilyMart", "ローソン", "Lawson", 
                        "ミニストップ", "MINISTOP", "デイリーヤマザキ", "ポプラ",
                        
                        // スーパー・小売
                        "スーパー", "イオン", "AEON", "西友", "ライフ", "マルエツ", 
                        "イトーヨーカドー", "ヨーカドー", "マックスバリュ", "ベイシア",
                        "カスミ", "とりせん", "ヤオコー", "コープ", "生協",
                        
                        // 駅・交通
                        "駅", "ステーション", "Station", "バス停", "インターチェンジ",
                        
                        // 商業施設
                        "センター", "モール", "プラザ", "マート", "パーク", "タワー",
                        "ビル", "複合施設", "商業施設", "ショッピング",
                        
                        // サービス
                        "ドラッグストア", "薬局", "病院", "クリニック", "銀行", 
                        "郵便局", "市役所", "町役場", "図書館", "公園", "学校", "大学"
                    )
                    
                    val searchText = "$featureName $premises $addressLine $thoroughfare $subLocality"
                    Log.d("LocationDebug", "検索対象テキスト: $searchText")
                    
                    for (keyword in storeKeywords) {
                        if (searchText.contains(keyword, ignoreCase = true)) {
                            nearestStore = when {
                                featureName.isNotEmpty() && !featureName.equals(locality, true) -> featureName
                                premises.isNotEmpty() && !premises.equals(locality, true) -> premises
                                thoroughfare.isNotEmpty() && thoroughfare.contains(keyword, true) -> thoroughfare
                                else -> keyword
                            }
                            
                            // 詳細な住所情報を構築
                            storeAddress = buildDetailedAddress(address)
                            
                            Log.d("LocationDebug", "マッチした店舗: $nearestStore (キーワード: $keyword)")
                            break
                        }
                    }
                    
                    if (nearestStore != null) break
                }
                
                withContext(Dispatchers.Main) {
                    if (nearestStore != null && storeAddress != null) {
                        val detailedLocation = "${storeAddress}の${nearestStore}近く"
                        tvLocationInfo.text = detailedLocation
                        Log.i("LocationResult", "詳細位置情報: $detailedLocation")
                    } else {
                        // 店舗が見つからない場合はより詳細な地域名を表示
                        val address = addresses[0]
                        val detailedAddress = buildDetailedAddress(address)
                        val locationName = when {
                            address.subLocality != null && address.subLocality != address.locality -> address.subLocality
                            address.thoroughfare != null -> address.thoroughfare  
                            address.locality != null -> address.locality
                            else -> "現在地"
                        }
                        val detailedLocation = "${detailedAddress}の${locationName}近く"
                        tvLocationInfo.text = detailedLocation
                        Log.i("LocationResult", "詳細地域情報: $detailedLocation")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    tvLocationInfo.text = "現在地近く"
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvLocationInfo.text = "現在地近く"
                Log.e("MainActivity", "住所検索エラー: ${e.message}")
            }
        }
    }

    private fun buildDetailedAddress(address: Address): String {
        val parts = mutableListOf<String>()
        
        // 県
        address.adminArea?.let { if (it.isNotEmpty()) parts.add(it) }
        
        // 市・区
        address.subAdminArea?.let { if (it.isNotEmpty()) parts.add(it) }
        
        // 町・村・大字
        if (address.locality != null && address.locality != address.subAdminArea) {
            parts.add(address.locality)
        }
        
        // 小字・丁目
        address.subLocality?.let { 
            if (it.isNotEmpty() && it != address.locality) {
                parts.add(it)
            }
        }
        
        // 番地
        address.thoroughfare?.let { 
            if (it.isNotEmpty() && !it.contains("県") && !it.contains("市") && !it.contains("区")) {
                parts.add(it)
            }
        }
        
        // 建物番号
        address.subThoroughfare?.let { 
            if (it.isNotEmpty()) {
                parts.add("${it}番地")
            }
        }
        
        return if (parts.isNotEmpty()) {
            parts.joinToString("")
        } else {
            "現在地"
        }
    }

    private suspend fun searchWithGeocoder(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 15)
            
            if (addresses != null && addresses.isNotEmpty()) {
                var nearestStore: String? = null
                
                // デバッグ: 取得した住所情報をログ出力
                Log.d("LocationDebug", "取得した住所数: ${addresses.size}")
                addresses.forEachIndexed { index, address ->
                    Log.d("LocationDebug", "Address $index:")
                    Log.d("LocationDebug", "  addressLine: ${address.getAddressLine(0)}")
                    Log.d("LocationDebug", "  featureName: ${address.featureName}")
                    Log.d("LocationDebug", "  premises: ${address.premises}")
                    Log.d("LocationDebug", "  subThoroughfare: ${address.subThoroughfare}")
                    Log.d("LocationDebug", "  thoroughfare: ${address.thoroughfare}")
                    Log.d("LocationDebug", "  locality: ${address.locality}")
                    Log.d("LocationDebug", "  subLocality: ${address.subLocality}")
                    Log.d("LocationDebug", "  adminArea: ${address.adminArea}")
                    Log.d("LocationDebug", "  subAdminArea: ${address.subAdminArea}")
                }
                
                // 住所リストから店舗や建物を探す
                for (address in addresses) {
                    val addressLine = address.getAddressLine(0) ?: ""
                    val featureName = address.featureName ?: ""
                    val premises = address.premises ?: ""
                    val subThoroughfare = address.subThoroughfare ?: ""
                    val thoroughfare = address.thoroughfare ?: ""
                    val locality = address.locality ?: ""
                    val subLocality = address.subLocality ?: ""
                    
                    // より幅広い店舗・施設キーワードで検索
                    val storeKeywords = listOf(
                        // コンビニ
                        "セブンイレブン", "セブン-イレブン", "7-Eleven", "7-11",
                        "ファミリーマート", "FamilyMart", "ローソン", "Lawson", 
                        "ミニストップ", "MINISTOP", "デイリーヤマザキ", "ポプラ",
                        
                        // スーパー・小売
                        "スーパー", "イオン", "AEON", "西友", "ライフ", "マルエツ", 
                        "イトーヨーカドー", "ヨーカドー", "マックスバリュ", "ベイシア",
                        "カスミ", "とりせん", "ヤオコー", "コープ", "生協",
                        
                        // 駅・交通
                        "駅", "ステーション", "Station", "バス停", "インターチェンジ",
                        
                        // 商業施設
                        "センター", "モール", "プラザ", "マート", "パーク", "タワー",
                        "ビル", "複合施設", "商業施設", "ショッピング",
                        
                        // サービス
                        "ドラッグストア", "薬局", "病院", "クリニック", "銀行", 
                        "郵便局", "市役所", "町役場", "図書館", "公園", "学校", "大学"
                    )
                    
                    val searchText = "$featureName $premises $addressLine $thoroughfare $subLocality"
                    Log.d("LocationDebug", "検索対象テキスト: $searchText")
                    
                    for (keyword in storeKeywords) {
                        if (searchText.contains(keyword, ignoreCase = true)) {
                            nearestStore = when {
                                featureName.isNotEmpty() && !featureName.equals(locality, true) -> featureName
                                premises.isNotEmpty() && !premises.equals(locality, true) -> premises
                                thoroughfare.isNotEmpty() && thoroughfare.contains(keyword, true) -> thoroughfare
                                else -> keyword
                            }
                            Log.d("LocationDebug", "マッチした店舗: $nearestStore (キーワード: $keyword)")
                            break
                        }
                    }
                    
                    if (nearestStore != null) break
                }
                
                withContext(Dispatchers.Main) {
                    if (nearestStore != null) {
                        tvLocationInfo.text = "${nearestStore}近く"
                        Log.i("LocationResult", "Geocoder結果: ${nearestStore}近く")
                    } else {
                        // 店舗が見つからない場合はより詳細な地域名を表示
                        val address = addresses[0]
                        val locationName = when {
                            address.subLocality != null && address.subLocality != address.locality -> address.subLocality
                            address.thoroughfare != null -> address.thoroughfare  
                            address.locality != null -> address.locality
                            else -> "現在地"
                        }
                        tvLocationInfo.text = "${locationName}近く"
                        Log.i("LocationResult", "Geocoder結果: ${locationName}近く (店舗なし)")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    tvLocationInfo.text = "現在地近く"
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                tvLocationInfo.text = "現在地近く"
                Log.e("MainActivity", "住所検索エラー: ${e.message}")
            }
        }
    }

    private fun getLastLocationAsFallback() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        Log.i("GPS", "フォールバック: lastLocationを試行中...")
        tvLocationInfo.text = "最後の位置情報を取得中..."

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    this.latitude = location.latitude
                    this.longitude = location.longitude
                    println("フォールバック位置: 緯度 = $latitude, 経度 = $longitude")
                    Log.i("GPS", "フォールバック成功: 緯度 = $latitude, 経度 = $longitude")
                    
                    // UIを更新
                    updateLocationDisplay(latitude!!, longitude!!)
                    
                    // 最寄りの店舗を検索
                    searchNearestStore(latitude!!, longitude!!)
                } else {
                    println("位置情報を取得できませんでした")
                    Log.e("GPS", "位置情報の取得に完全に失敗しました")
                    tvLocationInfo.text = "位置情報を取得できませんでした"
                    tvCoordinates.text = "GPS設定を確認してください"
                }
            }
            .addOnFailureListener { exception ->
                println("位置情報の取得中にエラーが発生しました: ${exception.message}")
                Log.e("GPS", "フォールバックも失敗: ${exception.message}")
                tvLocationInfo.text = "位置情報の取得に失敗しました"
                tvCoordinates.text = "GPS設定を確認してください"
            }
    }

    private fun updateLocationDisplay(lat: Double, lng: Double) {
        tvCoordinates.text = "緯度: %.6f, 経度: %.6f".format(lat, lng)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
