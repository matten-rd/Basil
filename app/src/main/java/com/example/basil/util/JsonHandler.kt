package com.example.basil.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

fun jsonArray2List(arr: JsonArray): List<String> {
    val gson = Gson()
    val list: List<String> = gson.fromJson(arr, List::class.java).map { it.toString() }
    list.forEach {
        removeQuotes(it)
    }
    return list
}

fun removeQuotes(str: String): String =
    str.removeSurrounding('"'.toString(), '"'.toString())


fun checkJsonType(data: String, jsonElem: JsonElement): String =
     when (JSONTokener(data).nextValue()) {
        is JSONObject -> jsonElem.asJsonObject.get("@type").toString()
        is JSONArray -> jsonElem.asJsonArray.last().asJsonObject.get("@type").toString() //TODO: Don't just get the last one
        else -> "Error"
    }
