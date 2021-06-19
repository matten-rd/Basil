package com.example.basil.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipe_table")
    fun getRecipes(): Flow<List<RecipeData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipeData: RecipeData)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(recipeData: RecipeData)

    @Delete
    suspend fun delete(recipeData: RecipeData)
}