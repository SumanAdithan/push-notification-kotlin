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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.androidmate.pushnotification.ui.theme.PushnotificationTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.d("FCM", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationId = intent?.getIntExtra("NOTIFICATION_ID", -1) ?: -1
        val actionType = intent.getStringExtra("ACTION_TYPE")

        // Cancel notification
        Log.d("notificationId--: ","notificationId--: $notificationId")
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationId != -1) {
            notificationManager.cancel(notificationId)
        }

        setContent {
            PushnotificationTheme {
                Greeting("No Ride Available")
            }
        }

        if (intent.getBooleanExtra("FROM_NOTIFICATION", false)) {
            when (actionType) {
                "ACCEPT" -> {
                    Toast.makeText(this, "You accepted the notification!", Toast.LENGTH_LONG).show()
                    Log.d("ACTION", "Accepted")
                    setContent {
                        PushnotificationTheme {
                            Greeting("You Accepted the ride")
                        }
                    }
                }
                "REJECT" -> {
                    Toast.makeText(this, "You rejected the notification!", Toast.LENGTH_LONG).show()
                    Log.d("ACTION", "Rejected")
                    setContent {
                        PushnotificationTheme {
                            Greeting("You Rejected the ride")
                        }
                    }
                }

            }
        }

        // Request permission for notifications (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Fetch FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM", "Device Token: $token")
        }
    }
}

@Composable
fun Greeting(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message,fontSize = 24.sp,fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PushnotificationTheme {
        Greeting("Android")
    }
}
