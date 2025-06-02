package com.msgtrik.msgtrik.ui.activities

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
                        finish()
                    }
                )
            }
        }
    }
}
