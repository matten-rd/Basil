package com.example.basil.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RecipeData::class], version = 2)
@TypeConverters(Converters::class)
abstract class BasilDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

}