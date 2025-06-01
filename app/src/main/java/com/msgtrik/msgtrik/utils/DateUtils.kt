package com.msgtrik.msgtrik.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatTimestamp(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val localDateTime = LocalDateTime.ofInstant(
            instant,
            ZoneId.systemDefault()
        )
        val now = LocalDateTime.now()

        when {
            localDateTime.toLocalDate() == now.toLocalDate() -> {
                // Today - show time only
                localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            localDateTime.toLocalDate() == now.toLocalDate().minusDays(1) -> {
                // Yesterday
                "Yesterday ${localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }
            localDateTime.year == now.year -> {
                // This year
                localDateTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
            }
            else -> {
                // Different year
                localDateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm"))
            }
        }
    } catch (e: Exception) {
        // Fallback if parsing fails
        timestamp
    }
} 