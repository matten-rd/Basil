package com.example.basil.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.basil.R
import com.example.basil.data.RecipeData
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.components.*
import com.example.basil.ui.navigation.Screen
import com.example.basil.ui.theme.Green500


@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun HomeScreen(navController: NavController, viewModel: RecipeViewModel) {
    val recipesFromVM = viewModel.allRecipes.observeAsState()

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "BASiL",
            style = MaterialTheme.typography.h2,
            modifier = Modifier.padding(30.dp),
            letterSpacing = 8.sp
        )

        recipesFromVM.value?.let { recipes ->
            BasilLazyRow(recipes = recipes, navController = navController, viewModel = viewModel)
        }
    }
}



@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BasilLazyRow(
    recipes: List<RecipeData>,
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val isBasilGrid by viewModel.isBasilGrid.observeAsState()
    AnimatedContent(targetState = isBasilGrid) {
        if (it == false) {
            VerticalGrid(recipes = recipes, viewModel = viewModel, navController = navController)

        } else {
            Column(
                modifier = modifier
                    .verticalScroll(rememberScrollState())
            ) {
                HorizontalStaggeredGrid(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    recipes.forEach { recipe ->
                        BasilRecipeCard(
                            recipeData = recipe,
                            viewModel = viewModel,
                            onClick = {
                                navController.currentBackStackEntry?.arguments?.putParcelable(
                                    "recipe_detail",
                                    recipe
                                )
                                navController.navigate(Screen.Detail.route)
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun BasilRecipeCard(
    recipeData: RecipeData,
    viewModel: RecipeViewModel,
    onClick: () -> Unit
) {
    Surface(
        elevation = 0.dp,
        shape = RectangleShape,
        modifier = Modifier
            .padding(8.dp)
            .size(200.dp, 260.dp),
        color = MaterialTheme.colors.background
    ) {
        ConstraintLayout(
            modifier = Modifier
                .clickable(onClick = onClick)
        ) {
            val (image, like, title) = createRefs()

            // The recipe image
            NetWorkImage(
                url = recipeData.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(4f / 3f)
                    .constrainAs(image) {
                        centerHorizontallyTo(parent)
                        top.linkTo(parent.top)
                    },
                placeholderColor = Green500
            )

            // The like button sorta integrated in the image above
            Box(modifier = Modifier
                .background(
                    color = MaterialTheme.colors.background,
                    shape = CircleShape
                )
                .constrainAs(like) {
                    centerHorizontallyTo(parent)
                    centerAround(image.bottom)
                }
            ) {
                IconButton(onClick = { viewModel.onLikeClick(recipeData) }) {
                    if (recipeData.isLiked)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fluent_heart_24_filled),
                            contentDescription = null
                        )
                    else
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fluent_heart_24_regular),
                            contentDescription = null
                        )
                }
            }

            // The recipe title below the image
            Text(
                text = recipeData.title,
                color = MaterialTheme.colors.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .constrainAs(title) {
                        centerHorizontallyTo(parent)
                        top.linkTo(like.bottom)
                    }
            )
        }
    }
}
