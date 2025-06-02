package com.msgtrik.msgtrik.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.msgtrik.msgtrik.ui.screens.RegisterScreen
import com.msgtrik.msgtrik.ui.theme.MsgtrikTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MsgtrikTheme {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Start MainActivity and clear the back stack
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
