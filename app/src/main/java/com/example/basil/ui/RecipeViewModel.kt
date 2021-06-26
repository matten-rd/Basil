package com.example.basil.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.basil.common.Lce
import com.example.basil.data.RecipeDao
import com.example.basil.data.RecipeData
import com.example.basil.data.remote.parsing.parseURL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeDao: RecipeDao
) : ViewModel() {

    private val _isBasilGrid = MutableLiveData(false)
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

    val loading = mutableStateOf(false)

    fun createRecipe(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            loading.value = true
            val recipeData = parseURL(url)
            _recipe.postValue(recipeData)
            insertRecipe(recipeData)
            loading.value = false
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