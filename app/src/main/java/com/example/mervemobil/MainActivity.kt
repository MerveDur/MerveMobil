package com.example.mervemobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mervemobil.ui.theme.MerveMobilTheme
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.mervemobil.BuildConfig
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BUILD_CONFIG_KEY", "BuildConfig.OPENWEATHER_API_KEY: ${BuildConfig.OPENWEATHER_API_KEY}")

        setContent {
            MerveMobilTheme {
                // UI burada başlıyor
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        onSuccess: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
        ).addOnSuccessListener { location ->
            if (location != null) {
                onSuccess(location.latitude, location.longitude)
            } else {
                onError("Konum bulunamadı (getCurrentLocation da null)")
            }
        }.addOnFailureListener {
            onError("Konum hatası: ${it.localizedMessage}")
        }
    }


}

@SuppressLint("ContextCastToActivity")
@Composable
fun WeatherScreen() {
    var weatherText by remember { mutableStateOf("") }
    var tomorrowText by remember { mutableStateOf("") }
    var citySearch by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var cityResult by remember { mutableStateOf<Pair<String, String>?>(null) }
    val activity = LocalContext.current as ComponentActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            "MerveMobil'e Hoş Geldiniz! 👋\n",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 24.dp, bottom = 14.dp)
        )

        // Hava durumu butonu (göz ikonu yanında emoji olarak 👀)
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF9800), // Turuncu
                contentColor = Color.White),
            onClick = {
                // Havanı Merak Et butonuna tıklayınca konumdan al
                cityResult = null // Önceki şehir sonucunu gizle
                citySearch = ""
                loading = true
                if (
                    ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        1001
                    )
                    loading = false
                } else {
                    (activity as MainActivity).getCurrentLocation(
                        onSuccess = { lat, lon ->
                            val apiKey = activity.getString(R.string.openweather_api_key)
                            getWeatherInfo(lat, lon, apiKey) { result ->
                                activity.runOnUiThread {
                                    weatherText = result
                                    loading = false
                                }
                            }
                            getTomorrowWeatherInfo(lat, lon, apiKey) { result ->
                                activity.runOnUiThread { tomorrowText = result }
                            }
                        },
                        onError = { error ->
                            weatherText = error
                            tomorrowText = ""
                            loading = false
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(32.dp), // Daha oval görünüm için
        ) {
            Text("Hava Durumunu Göster", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.width(8.dp))
            Text("👀", fontSize = MaterialTheme.typography.titleLarge.fontSize)
        }

        // Sonuç kartı (butonun hemen altında)
        if (weatherText.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0) // Açık turuncu, istersen morla da oynayabilirsin!
                ),
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .padding(vertical = 10.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(weatherText, style = MaterialTheme.typography.titleMedium)
                    if (tomorrowText.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(tomorrowText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Ara boşluk
        Spacer(Modifier.height(16.dp))

        // Alt başlık ve arama kutusu
        Text(
            "Farklı bir şehri mi merak ettin?",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = citySearch,
            onValueChange = { citySearch = it },
            label = { Text("Şehir ara") },
            placeholder = { Text("Mesela: Ankara...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (citySearch.isNotBlank()) {
                        loading = true
                        val apiKey = activity.getString(R.string.openweather_api_key)
                        getWeatherByCity(citySearch, apiKey) { today, tomorrow ->
                            activity.runOnUiThread {
                                cityResult = today to tomorrow
                                loading = false
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Ara")

                }
            }
        )

        // Şehir arama sonucu kutusu
        if (cityResult != null && !loading) {
            Spacer(Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0) // Açık turuncu, istersen morla da oynayabilirsin!
                ),
                modifier = Modifier
                    .fillMaxWidth(0.93f)
                    .padding(bottom = 10.dp),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(cityResult!!.first, style = MaterialTheme.typography.titleMedium)
                    if (cityResult!!.second.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(cityResult!!.second, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Loading ve hoş geldin kısmı
        if (loading) {
            Spacer(Modifier.height(14.dp))
            CircularProgressIndicator()
            Spacer(Modifier.height(8.dp))
            Text("Bir saniye, bulutlar arasında hava raporu aranıyor ☁️⏳")
        } else if (weatherText.isEmpty() && cityResult == null) {
            Spacer(Modifier.height(18.dp))
            Text(
                "Bulunduğunuz konumdaki hava durumunu öğrenmek için konumunuzu kullanabilir ya da yukarıdan şehir arayabilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


fun getWeatherInfo(
    lat: Double,
    lon: Double,
    apiKey: String,
    onResult: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&lang=tr&appid=$apiKey"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                try {
                    val json = JSONObject(body)
                    val weatherDesc = json.getJSONArray("weather").getJSONObject(0).getString("description")
                    val temp = json.getJSONObject("main").getDouble("temp")
                    val city = json.getString("name")
                    val result = """
                        $city
                        Hava: $weatherDesc, ${temp.toInt()}°C
                    """.trimIndent()
                    onResult(result)
                } catch (e: Exception) {
                    onResult("JSON Hatası: ${e.localizedMessage}\nJSON: $body")
                }
            } else {
                onResult("API'den veri alınamadı.")
            }
        } catch (e: Exception) {
            onResult("Hata oluştu: ${e.localizedMessage}")
        }
    }
}

fun getTomorrowWeatherInfo(
    lat: Double,
    lon: Double,
    apiKey: String,
    onResult: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&units=metric&lang=tr&appid=$apiKey"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val json = JSONObject(body)
                val list = json.getJSONArray("list")
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val tomorrowDate = sdf.format(calendar.time)

                var found = false
                for (i in 0 until list.length()) {
                    val item = list.getJSONObject(i)
                    val dtTxt = item.getString("dt_txt")
                    if (dtTxt.startsWith(tomorrowDate) && dtTxt.contains("12:00:00")) {
                        val temp = item.getJSONObject("main").getDouble("temp")
                        val desc = item.getJSONArray("weather").getJSONObject(0).getString("description")
                        onResult("Yarın: $desc, ${temp.toInt()}°C")
                        found = true
                        break
                    }
                }
                if (!found) onResult("Yarınki hava tahmini bulunamadı.")
            } else {
                onResult("Tahmin API'den veri alınamadı.")
            }
        } catch (e: Exception) {
            onResult("Tahmin Hatası: ${e.localizedMessage}")
        }
    }
}


fun getWeatherByCity(
    city: String,
    apiKey: String,
    onResult: (String, String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Bugünkü hava
            val urlToday = "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&lang=tr&appid=$apiKey"
            val client = OkHttpClient()
            val requestToday = Request.Builder().url(urlToday).build()
            val responseToday = client.newCall(requestToday).execute()
            val bodyToday = responseToday.body?.string()
            var weatherDescToday = "Veri alınamadı."
            var cityName = city

            if (bodyToday != null) {
                val json = JSONObject(bodyToday)
                cityName = json.optString("name", city)
                val weatherArr = json.optJSONArray("weather")
                val mainObj = json.optJSONObject("main")
                if (weatherArr != null && mainObj != null) {
                    val desc = weatherArr.getJSONObject(0).getString("description")
                    val temp = mainObj.getDouble("temp")
                    weatherDescToday = "$cityName.\nHava: $desc, ${temp.toInt()}°C"
                } else if (json.has("message")) {
                    weatherDescToday = "Bulunamadı: ${json.getString("message")}"
                }
            }

            // Yarınki hava
            val urlTomorrow = "https://api.openweathermap.org/data/2.5/forecast?q=$city&units=metric&lang=tr&appid=$apiKey"
            val requestTomorrow = Request.Builder().url(urlTomorrow).build()
            val responseTomorrow = client.newCall(requestTomorrow).execute()
            val bodyTomorrow = responseTomorrow.body?.string()
            var weatherDescTomorrow = ""
            if (bodyTomorrow != null) {
                val json = JSONObject(bodyTomorrow)
                val list = json.optJSONArray("list")
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val tomorrowDate = sdf.format(calendar.time)
                var found = false
                if (list != null) {
                    for (i in 0 until list.length()) {
                        val item = list.getJSONObject(i)
                        val dtTxt = item.getString("dt_txt")
                        if (dtTxt.startsWith(tomorrowDate) && dtTxt.contains("12:00:00")) {
                            val temp = item.getJSONObject("main").getDouble("temp")
                            val desc = item.getJSONArray("weather").getJSONObject(0).getString("description")
                            weatherDescTomorrow = "Yarın: $desc, ${temp.toInt()}°C"
                            found = true
                            break
                        }
                    }
                }
                if (!found) weatherDescTomorrow = "Yarınki hava tahmini bulunamadı."
            }
            onResult(weatherDescToday, weatherDescTomorrow)
        } catch (e: Exception) {
            onResult("Şehir arama hatası: ${e.localizedMessage}", "")
        }
    }
}
