/*Joseph Di Lullo
OSU
CS 492
dilulloj@oregonstate.edu
*/

package com.example.treasurehunt1.ui.theme.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.treasurehunt1.R

class ClueSolved : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val elapsed = intent.getIntExtra("elapsedTime", 0)
        val prefs = getSharedPreferences("ClueSolvedPrefs", Context.MODE_PRIVATE)
        val currentVisits = prefs.getInt("visitCountQ", 0)
        val editor = prefs.edit()
        editor.putInt("visitCountQ", currentVisits + 1)
        editor.apply()

        setContent {
            ClueSolvedScreen(elapsed, currentVisits)
        }
    }
}

@Composable
fun ClueSolvedScreen(elapsedTime: Int, visitCount: Int) {
    val context = LocalContext.current
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Time Elapsed: $elapsedTime seconds",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Spacer(modifier = Modifier.height(85.dp))
            Text(
                text = if (visitCount % 2 == 0) stringResource(id = R.string.clue_solved_text_two)
                else stringResource(id = R.string.clue_solved_text),
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 50.dp, end = 25.dp)
            )
            Spacer(modifier = Modifier.height(300.dp))
            Button(
                onClick = {
                    if (visitCount % 2 == 0) {
                        val intent = Intent(context, CompletedPage::class.java).apply {
                            putExtra("elapsedTime", elapsedTime)
                        }
                        context.startActivity(intent)

                    } else {
                        val intent = Intent(context, CluePage::class.java).apply {
                            putExtra("resumeTimer", true)
                            putExtra("timerValue", elapsedTime)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                }
            ) {
                Text(text = "Continue")
            }
        }
    }
}