package com.example.basil.ui.create

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavController
import com.example.basil.data.RecipeData
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.components.ErrorScreen

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun EditImageScreen(
    navController: NavController,
    recipe: RecipeData?,
    viewModel: RecipeViewModel,
) {
    if (recipe != null) {
        val categoryOptions = listOf("Förrätt", "Huvudrätt", "Efterrätt", "Bakning")
        CreateImageRecipeState(
            navController = navController,
            initialRecipe = recipe,
            viewModel = viewModel,
            categoryOptions = categoryOptions
        )
    } else {
        ErrorScreen(errorMessage = "Oops! Något gick fel! Försök igen snart!")
    }
}
