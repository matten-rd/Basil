package com.example.basil.data

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken

import com.google.gson.Gson




class Converters {

    @TypeConverter
    fun restoreList(listOfString: String): List<String> {
        return Gson().fromJson(listOfString, object : TypeToken<List<String?>?>() {}.type)
    }

    @TypeConverter
    fun saveList(listOfString: List<String>): String {
        return Gson().toJson(listOfString)
    }

}