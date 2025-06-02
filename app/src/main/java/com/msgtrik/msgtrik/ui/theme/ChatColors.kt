package com.msgtrik.msgtrik.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.msgtrik.msgtrik.R

object ChatColors {
    @Composable
    fun messageSentBackground(): Color = colorResource(id = R.color.message_sent_background)

    @Composable
    fun messageReceivedBackground(): Color = colorResource(id = R.color.message_received_background)
} 