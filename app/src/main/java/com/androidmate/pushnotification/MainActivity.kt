package com.androidmate.pushnotification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.androidmate.pushnotification.ui.theme.PushnotificationTheme
import com.google.firebase.messaging.FirebaseMessaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val defaultMessage = sharedPrefs.getString("greeting", "No Ride Available") ?: "No Ride Available"

        // Cancel notification if present
        val notificationId = intent?.getIntExtra("NOTIFICATION_ID", -1) ?: -1
        val actionType = intent?.getStringExtra("ACTION_TYPE")

        if (notificationId != -1) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
        }

        // Update shared preferences if coming from notification
        if (intent.getBooleanExtra("FROM_NOTIFICATION", false)) {
            when (actionType) {
                "ACCEPT" -> {
                    sharedPrefs.edit().putString("greeting", "You Accepted the Ride").apply()
                    Toast.makeText(this, "You accepted the ride", Toast.LENGTH_SHORT).show()
                }
                "REJECT" -> {
                    sharedPrefs.edit().putString("greeting", "You Rejected the Ride").apply()
                    Toast.makeText(this, "You rejected the ride", Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContent {
            PushnotificationTheme {
                AppContent(defaultMessage)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
    }
}

@Composable
fun MainScreen(greetingMessage: String, token: String) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = greetingMessage,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tap token to copy:",
            modifier = Modifier.padding(top = 24.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = token,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable {
                    clipboardManager.setText(AnnotatedString(token))
                    copied = true
                },
            fontSize = 16.sp
        )

        if (copied) {
            Text(
                text = "Token copied!",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Composable
fun AppContent(initialGreeting: String) {
    val context = LocalContext.current
    var greetingMessage by remember { mutableStateOf(initialGreeting) }
    var token by remember { mutableStateOf("") }

    // Fetch token
    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result
                Log.d("FCM", "Token: $token")
            }
        }
    }

    // Load latest greeting from shared preferences
    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        greetingMessage = sharedPrefs.getString("greeting", "No Ride Available") ?: "No Ride Available"
    }

    MainScreen(greetingMessage, token)
}
