package com.example.basil.util

import android.util.Log
import java.net.URI
import java.net.URISyntaxException
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

@Throws(URISyntaxException::class)
fun getDomainName(url: String): String {
    val uri = URI(url)
    val domain: String = uri.host
    return if (domain.startsWith("www.")) domain.substring(4) else domain
}

fun extractNumbers(string: String): String {
    return string.filter { it.isDigit() }
}

fun getHoursFromDuration(s: String): Int {
    return try {
        val sNew = s.replace(Regex("Y.*D"), "D")
        (java.time.Duration.parse(sNew).toHours() % 24L).toInt()
    } catch (e: Exception) {
        Log.e("getHoursFromDuration", e.message.toString())
        0
    }

}

fun getMinutesFromDuration(s: String): Int {
    return try {
        val sNew = s.replace(Regex("Y.*D"), "D")
        (java.time.Duration.parse(sNew).toMinutes() % 60L).toInt()
    } catch (e: Exception) {
        Log.e("getMinutesFromDuration", e.message.toString())
        0
    }

}

fun humanReadableDuration(s: String): String {
    return try {
        val sNew = s.replace(Regex("Y.*D"), "D")
        val time = java.time.Duration.parse(sNew)
        val hours = time.toHours() % 24L
        val minutes = time.toMinutes() % 60L
        "$hours h $minutes min"
    } catch (e: Exception) {
        Log.e("humanReadableDuration", e.message.toString())
        "0 h 0 min"
    }
}
