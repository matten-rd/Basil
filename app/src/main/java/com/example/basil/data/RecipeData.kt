package com.example.basil.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "recipe_table")
@Parcelize
data class RecipeData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val imageUrl: String,
    val recipeState: RecipeState,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val cookTime: String,
    val yield: String,
    val mealType: String,
    val isLiked: Boolean
) : Parcelable


enum class RecipeState {
    SCRAPED,
    WEBVIEW,
    IMAGE
}
