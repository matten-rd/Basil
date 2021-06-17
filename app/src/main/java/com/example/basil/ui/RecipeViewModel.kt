package com.example.basil.ui

import androidx.lifecycle.*
import com.example.basil.common.Lce
import com.example.basil.data.RecipeDao
import com.example.basil.data.RecipeData
import com.example.basil.data.remote.parsing.Recipe
import com.example.basil.data.remote.parsing.parseURL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime


@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeDao: RecipeDao
) : ViewModel() {

    companion object {
        private const val URL1 = "https://www.koket.se/torskrygg-med-potatiskaka-och-tartarsas"
        private const val URL2 = "https://www.ica.se/recept/marinerad-fetaost-727845/"
        private const val URL3 = "https://recept.se/recept/citronlax-med-ortdressing"
        private const val URL4 = "https://www.arla.se/recept/pasta-carbonara/"
        private const val URL5 = "https://www.tasteline.com/recept/helstekt-flaskytterfile-med-gremolata-och-italiensk-potatissallad/"
        private const val URL6 = "https://www.recepten.se/recept/pasta_carbonara.html"
        private const val URL7 = "https://www.coop.se/recept/grillade-kycklingklubbor"
    }


    val recipe = MutableLiveData<RecipeData>()
    val _recipe: LiveData<RecipeData> = recipe

    fun onRecipeChange(recipeData: RecipeData) {
        recipe.value = recipeData
    }

    fun loadRecipe() {
        viewModelScope.launch(Dispatchers.IO) {
            val recipeData = parseURL(url = URL2)
            recipe.postValue(recipeData)
        }
    }

    fun createRecipe(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipeD = parseURL(url)
            recipe.postValue(recipeD)
        }
    }

    val allRecipes = recipeDao.getRecipes().asLiveData()

    fun insertRecipe(recipeData: RecipeData) = viewModelScope.launch {
        recipeDao.insert(recipeData)
    }

    fun updateRecipe(recipeData: RecipeData) = viewModelScope.launch {
        recipeDao.update(recipeData)
    }

    fun deleteRecipe(recipeData: RecipeData) = viewModelScope.launch {
        recipeDao.delete(recipeData)
    }



    val mockRecipes = mutableListOf<RecipeData>(
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 0",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = true
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 1",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = true
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 2",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = false
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 3",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = false
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 4",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = true
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 5",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = false
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 6",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = false
        ),
        RecipeData(
            url = "https://picsum.photos/600/600",
            imageUrl = "https://picsum.photos/600/600",
            isScraped = true,
            title = "Recipe title 7",
            description = "Recipe description",
            ingredients = listOf("ing 1", "ing 2", "ing 3"),
            instructions = listOf("ins 1", "ins 2", "ins 3"),
            cookTime = "50",
            yield = "4",
            mealType = "Dessert",
            isLiked = false
        ),


    )

}