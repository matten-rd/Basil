package com.example.basil.util

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
    val sNew = s.replace(Regex("Y.*D"), "D")
    return (java.time.Duration.parse(sNew).toHours() % 24L).toInt()
}

fun getMinutesFromDuration(s: String): Int {
    val sNew = s.replace(Regex("Y.*D"), "D")
    return (java.time.Duration.parse(sNew).toMinutes() % 60L).toInt()
}

fun humanReadableDuration(s: String): String {
    val sNew = s.replace(Regex("Y.*D"), "D")
    val time = java.time.Duration.parse(sNew)
    val hours = time.toHours() % 24L
    val minutes = time.toMinutes() % 60L
    val formattedString = "$hours h $minutes min"
    return formattedString
}
