package com.example.basil.ui

import androidx.lifecycle.*
import com.example.basil.common.Lce
import com.example.basil.data.RecipeDao
import com.example.basil.data.RecipeData
import com.example.basil.data.RecipeState
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

    private val _url = MutableLiveData("")
    val url: LiveData<String> = _url

    fun onUrlChange(newUrl: String) {
        _url.value = newUrl
    }


    private val _recipe = MutableLiveData<RecipeData>()
    val recipe: LiveData<RecipeData> = _recipe

    fun onRecipeChange(recipeData: RecipeData) {
        _recipe.value = recipeData
    }


    fun createRecipe(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val recipeData = parseURL(url)
            _recipe.postValue(recipeData)
            insertRecipe(recipeData)
        }
        onUrlChange("")
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

    fun onLikeClick(recipeData: RecipeData) = viewModelScope.launch {
        recipeDao.update(recipeData.copy(isLiked = !recipeData.isLiked))
        onRecipeChange(recipeData.copy(isLiked = !recipeData.isLiked))
    }



}