import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment(private val victimCurrentLocation:Pair<Double, Double>, val shelterList: List<Pair<Double, Double>>) : SupportMapFragment(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i("JunSato20250123.0333", "latitude is ${victimCurrentLocation.first}")
        Log.i("JunSato20250123.0333", "longitude is ${victimCurrentLocation.second}")

        googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(victimCurrentLocation.first, victimCurrentLocation.second))
                .title("現在地")
        )

        Log.i("JunSato20250124.0129", "A 避難所の数は ${shelterList.size} 件です。")

        // 避難所の座標と名前のリスト
        val shelters = shelterList.mapIndexed { index, pair ->
            Pair(LatLng(pair.first, pair.second), "避難所${'A' + index}")
        }

        Log.i("JunSato20250124.0129", "B")

        // Google Map にマーカーを追加
        shelters.forEach { shelter ->
            val location = shelter.first
            val title = shelter.second
            googleMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // マーカーの色を変更
            )
        }

        Log.i("JunSato20250124.0129", "C")

        // 2km (2000m) の距離でLatLngBoundsを作成
        val padding = 100 // マップの周囲に余白を追加
        val radius = 2000.0 // 2kmの半径

        val bounds = LatLngBounds(
            LatLng(victimCurrentLocation.first - radius / 111320, victimCurrentLocation.second - radius / 111320),
            LatLng(victimCurrentLocation.first + radius / 111320, victimCurrentLocation.second + radius / 111320)
        )

        Log.i("JunSato20250124.0129", "D")

        // カメラを指定した範囲に移動
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

        Log.i("JunSato20250124.0129", "E")
    }

    companion object {
        private const val ARG_LATITUDE_VALUE: String = "arg_latitude_value"
        private const val ARG_LONGITUDE_VALUE: String = "arg_longitude_value"
    }
}
