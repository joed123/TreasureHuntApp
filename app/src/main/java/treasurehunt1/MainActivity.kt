/*Joseph Di Lullo
OSU
CS 492
dilulloj@oregonstate.edu
*/

package com.example.treasurehunt1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import com.example.treasurehunt1.ui.theme.TreasureHunt1Theme
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.example.treasurehunt1.ui.theme.screens.CluePage
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.Manifest
import android.content.Context
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TreasureHunt1Theme {
                val navController = rememberNavController()
                NavigationComponent(navController = navController)
            }
        }
    }

    @Composable
    fun NavigationComponent(navController: NavHostController) {
        NavHost(navController = navController, startDestination = "permission_screen") {
            composable("permission_screen") { PermissionScreen(navController) }
            composable("start_screen") {
                StartScreen(onStartClick = { navigateToCluePage() })
            }
        }
    }

    @Composable
    fun PermissionScreen(navController: NavHostController) {
        val permissionState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION

            )
        )
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.permissions_required_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.permissions_required_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                scope.launch {
                    permissionState.launchMultiplePermissionRequest()
                }
            }) {
                Text(text = stringResource(id = R.string.grant_permissions_button))
            }
            Spacer(Modifier.height(10.dp))
            when {
                permissionState.allPermissionsGranted -> {
                    Text(text = stringResource(id = R.string.permissions_granted))
                    LaunchedEffect(Unit) {
                        navController.navigate("start_screen") {
                            popUpTo("permission_screen") { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    private fun navigateToCluePage() {
        val prefs = getSharedPreferences("CluePrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("elapsedTime", 0)
            .putInt("visitCount", 0)
            .apply()
        val intent = Intent(this, CluePage::class.java)
        startActivity(intent)
    }
}

    @Composable
    fun StartScreen(onStartClick: () -> Unit) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(25.dp)
        ) {
            Text(
                text = stringResource(id = R.string.welcome_message),
                style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(50.dp))

            val scrollState = rememberScrollState()
            Text(
                text = stringResource(id = R.string.game_rules),

                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxWidth()
                    .padding(end = 25.dp),
                style = TextStyle(fontSize = 20.sp)
            )

            Spacer(Modifier.height(50.dp))

            Button(
                onClick = { onStartClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
            ) {
                Text(stringResource(id = R.string.start_game_button))
            }
        }
    }


