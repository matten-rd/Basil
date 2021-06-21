package com.example.basil.ui.components


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.navigation.NavController
import com.example.basil.data.RecipeData
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.home.BasilRecipeCard
import com.example.basil.ui.navigation.Screen


/**
 * A custom staggered grid layout. Used to display all recipes.
 */
@Composable
fun HorizontalStaggeredGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        var height = constraints.maxHeight
        var width = 0

        val placeables = measurables.mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints)

            height = placeable.height * 2
            width += when(index % 3) {
                0 -> placeable.width + 200
                1 -> -100
                else -> placeable.width + 200
            }

            placeable
        }

        layout(width, height) {
            var xPos = 100
            placeables.forEachIndexed { index, placeable ->
                val yPos = when(index % 3) {
                    0 -> 0
                    1 -> placeable.height
                    else -> placeable.height / 2
                }

                placeable.place(
                    x = xPos,
                    y = yPos
                )

                xPos += when(index % 3) {
                    0 -> -100
                    1 -> placeable.width + 200
                    else -> placeable.width + 200
                }
            }
        }
    }
}


@ExperimentalFoundationApi
@Composable
fun VerticalGrid(
    recipes: List<RecipeData>,
    viewModel: RecipeViewModel,
    navController: NavController
) {
    LazyVerticalGrid(cells = GridCells.Fixed(count = 2)) {
        items(recipes) { recipe ->
            BasilRecipeCard(
                recipeData = recipe,
                viewModel = viewModel,
                onClick = {
                    navController.currentBackStackEntry?.arguments?.putParcelable("recipe_detail", recipe)
                    navController.navigate(Screen.Detail.route)
                }
            )
        }
    }
}

