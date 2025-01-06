/*Joseph Di Lullo
OSU
CS 492
dilulloj@oregonstate.edu
*/

package com.example.treasurehunt1.ui.theme.screens

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.treasurehunt1.R
import android.content.Intent
import android.content.Context
import androidx.activity.viewModels
import com.example.treasurehunt1.ui.theme.TimerViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import kotlin.math.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import android.os.Looper




class CluePage : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val treasureLocation1 = Pair(38.6359, -121.2633)
    private val treasureLocation2 = Pair(38.6413, -121.2606)

    internal val timerViewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
        val currentTimerValue = intent.getIntExtra("timerValue", 0)
        timerViewModel.setInitialTime(currentTimerValue)

        val prefs = getSharedPreferences("CluePrefs", Context.MODE_PRIVATE)
        var visits = prefs.getInt("visitCount", 0)

        val editor = prefs.edit()
        visits += 1

        editor.putInt("visitCount", visits)
        editor.apply()

        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    timerViewModel.resumeTimer()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    timerViewModel.pauseTimer()
                    editor.putInt("elapsedTime", timerViewModel.secondsElapsed.value).apply()
                }
                else -> {}
            }
        })

        setContent {
            val secondsElapsed = timerViewModel.secondsElapsed.collectAsState().value
            CluePageContent(this, timerViewModel, visits, secondsElapsed)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.locations?.lastOrNull()?.let { location ->
                    Log.d("LocationUpdate", "Location update: Lat=${location.latitude}, Lon=${location.longitude}")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun checkLocation(visits: Int, onSuccess: () -> Unit, onFailure: () -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("CheckLocation", "Permissions not granted")
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val (targetLat, targetLon) = if (visits == 1) treasureLocation1 else treasureLocation2
                if (isNearLocation(location.latitude, location.longitude, targetLat, targetLon)) {
                    onSuccess()

                } else {
                    onFailure()
                }
            }
        }
    }

    private fun isNearLocation(userLat: Double, userLon: Double, targetLat: Double, targetLon: Double, thresholdMeters: Double = 100.0): Boolean {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(targetLat - userLat)
        val dLon = Math.toRadians(targetLon - userLon)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(userLat)) * cos(Math.toRadians(targetLat)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c
        return distance <= thresholdMeters
    }

}

@Composable
fun CluePageContent(cluePage: CluePage, timerViewModel: TimerViewModel, visits: Int, secondsElapsed: Int) {
    var showHint by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clueTextResId = if (visits == 1) R.string.clue_text else R.string.clue_Two_text
    val hintTextResId = if (visits == 1) R.string.hint_text else R.string.hint_Two_text

    var showIncorrectLocationPopup by remember { mutableStateOf(false) }

    if (showIncorrectLocationPopup) {
        AlertDialog(
            onDismissRequest = { showIncorrectLocationPopup = false },
            title = { Text(stringResource(id = R.string.incorrect_location_title)) },
            text = { Text(stringResource(id = R.string.incorrect_location_message)) },
            confirmButton = {
                TextButton(onClick = { showIncorrectLocationPopup = false }) {
                    Text(stringResource(id = R.string.ok_button))
                }
            }
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = { (context as? Activity)?.finish() }) {
                Text(text = stringResource(id = R.string.quit_button_text), fontSize = 23.sp)
            }
        }


        Spacer(modifier = Modifier.height(85.dp))
        Text(text = "Time Elapsed: $secondsElapsed", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(90.dp))
        Text(text = stringResource(id = clueTextResId), modifier = Modifier.padding(50.dp), style = TextStyle(fontSize = 20.sp))
        Spacer(modifier = Modifier.height(30.dp))

        if (showHint) {
            Text(text = stringResource(id = hintTextResId), modifier = Modifier.padding(vertical = 16.dp), style = TextStyle(fontSize = 20.sp))
        } else {
            Button(onClick = { showHint = true }) {
                Text(text = stringResource(R.string.show_hint_button_text))
            }
        }
        Spacer(modifier = Modifier.height(150.dp))
        Button(onClick = {
            cluePage.checkLocation(visits, onSuccess = {
            timerViewModel.pauseTimer()
            context.startActivity(Intent(context, ClueSolved::class.java).apply {
                putExtra("elapsedTime", secondsElapsed)
            })
            }, onFailure = {
                showIncorrectLocationPopup = true
            })
    }) {
        Text(text = stringResource(R.string.found_it_button_text))
    }
}
}
