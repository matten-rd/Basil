package com.example.basil.ui

import androidx.lifecycle.*
import com.example.basil.data.RecipeDao
import com.example.basil.data.RecipeData
import com.example.basil.data.remote.parsing.parseURL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeDao: RecipeDao
) : ViewModel() {

    private val _isBasilGrid = MutableLiveData(true)
    val isBasilGrid: LiveData<Boolean> = _isBasilGrid

    fun onLayoutGridStateChange() {
        _isBasilGrid.value = !_isBasilGrid.value!!
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