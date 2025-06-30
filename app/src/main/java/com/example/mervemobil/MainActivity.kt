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
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    var weatherText by remember { mutableStateOf("Sonuç burada gözükecek") }
    val activity = LocalContext.current as ComponentActivity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                // Konum izni kontrolü
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
                    // İzin yoksa izin iste
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        1001 // requestCode, sabit bir sayı
                    )
                } else {
                    // İzin varsa MainActivity'deki konum fonksiyonunu çağır
                    (activity as MainActivity).getCurrentLocation(
                        onSuccess = { lat, lon ->
                            weatherText = "Konum: $lat, $lon"
                        },
                        onError = { error ->
                            weatherText = error
                        }
                    )
                }
            }
        ) {
            Text("Hava Durumunu Göster")
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = weatherText,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}