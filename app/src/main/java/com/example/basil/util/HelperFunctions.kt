package com.example.basil.util

import android.util.Log
import android.util.Patterns
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.net.URL


fun getDomainName(url: String): String {
    if (!isValidUrl(url)) return "Recept"

    val uri = URI(url)
    val domain: String = uri.host
    return if (domain.startsWith("www.")) domain.substring(4) else domain
}

fun isValidUrl(url: String?): Boolean {
    url ?: return false
    return Patterns.WEB_URL.matcher(url).matches()
}

fun extractNumbers(string: String): String {
    return string.filter { it.isDigit() }
}

fun getHoursFromDuration(s: String): Int {
    return try {
        val sNew = s.replace(Regex("Y.*D"), "D")
        (java.time.Duration.parse(sNew).toHours() % 24L).toInt()
    } catch (e: Exception) {
        Log.e("getHoursFromDuration", e.message.toString() + " Failed duration: $s")
        0
    }

}

fun getMinutesFromDuration(s: String): Int {
    return try {
        val sNew = s.replace(Regex("Y.*D"), "D")
        (java.time.Duration.parse(sNew).toMinutes() % 60L).toInt()
    } catch (e: Exception) {
        Log.e("getMinutesFromDuration", e.message.toString() + " Failed duration: $s")
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
        Log.e("humanReadableDuration", e.message.toString() + " Failed duration: $s")
        "0 h 0 min"
    }
}

fun getDurationFromHourAndMinute(hour: Int, minute: Int): String {
    val totalMinutes = hour*60 + minute
    return "PT${totalMinutes}M"
}

fun isUrlImage(stringUrl: String): Boolean {
    val isSvgOrEmpty = stringUrl.endsWith("svg") || stringUrl.isEmpty()
    if (isSvgOrEmpty) return false

    var urlConnection: HttpURLConnection? = null
    System.setProperty("http.keepAlive", "false")
    return try {
        val url = URL(stringUrl)
        urlConnection = url.openConnection() as HttpURLConnection
        val contentType = urlConnection.getHeaderField("Content-Type")
        contentType.startsWith("image/")
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        false
    } catch (e: IOException) {
        e.printStackTrace()
        false
    } finally {
        urlConnection?.disconnect()
    }
}

fun removePartMatchesFromList(list: List<String>): List<String> {
    val returnList = mutableListOf<String>()
    list.forEach { elem ->
        val check = list.any { it.contains(elem) && it != elem }

        if (check && elem !in returnList) {
            returnList.add(elem)
        }
    }

    /*
    list.filter { elem ->
        list.any { it.contains(elem) && it != elem }
    }
     */

    if (returnList.isEmpty())
        return list

    return if (returnList != list)
        removePartMatchesFromList(returnList)
    else
        returnList.toList()
}
