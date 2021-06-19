package com.example.basil

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.example.basil.data.RecipeData
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.create.CreateImageRecipe
import com.example.basil.ui.create.CreateRecipe
import com.example.basil.ui.create.EditScreen
import com.example.basil.ui.detail.DetailScreen
import com.example.basil.ui.home.HomeScreen
import com.example.basil.ui.navigation.*
import com.example.basil.ui.theme.BasilTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val recipeViewModel: RecipeViewModel by viewModels()

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasilTheme {
                MainScreen(recipeViewModel = recipeViewModel)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun MainScreen(recipeViewModel: RecipeViewModel) {
        val transitionState = remember { MutableTransitionState(SplashState.SHOWN) }
        val transition = updateTransition(transitionState = transitionState, label = "splashTransition")
        val splashAlpha by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 200) }, label = "splashAlpha"
        ) {
            if (it == SplashState.SHOWN) 1f else 0f
        }
        val contentAlpha by transition.animateFloat(
            transitionSpec = { tween(durationMillis = 500) }, label = "contentAlpha"
        ) {
            if (it == SplashState.SHOWN) 0f else 1f
        }
        val contentTopPadding by transition.animateDp(
            transitionSpec = { spring(stiffness = StiffnessLow) }, label = "contentTopPadding"
        ) {
            if (it == SplashState.SHOWN) 100.dp else 0.dp
        }

        Box {
            LandingScreen(
                modifier = Modifier.alpha(splashAlpha),
                onTimeOut = { transitionState.targetState = SplashState.COMPLETED }
            )
            BasilApp(
                recipeViewModel = recipeViewModel,
                modifier = Modifier
                    .alpha(contentAlpha)
                    .padding(top = contentTopPadding)
            )
        }
}

private const val SplashWaitTime: Long = 2000

@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    onTimeOut: () -> Unit
) {
    Box(modifier = modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colors.primary), contentAlignment = Alignment.Center) {
        val currentOnTimeout by rememberUpdatedState(newValue = onTimeOut)

        LaunchedEffect(Unit) {
            delay(SplashWaitTime)
            currentOnTimeout()
        }
        Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null)
    }
}


@ExperimentalMaterialApi
@Composable
fun BasilApp(recipeViewModel: RecipeViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val systemUiController = rememberSystemUiController()
    val systemBarsColor = MaterialTheme.colors.background
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = systemBarsColor
        )
    }
    
    BackdropScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        appBar = {
            when(currentRoute) {
                Screen.Home.route -> HomeTopAppBar(scaffoldState = scaffoldState, scope = scope, navController = navController)
                Screen.Detail.route -> {
                    var recipe = navController.previousBackStackEntry?.arguments?.getParcelable<RecipeData>("recipe_detail")
                    DetailTopAppBar(scaffoldState = scaffoldState, scope = scope, navController = navController, recipe = recipe, viewModel = recipeViewModel)
                }
                Screen.CreateUrl.route -> CreateUrlTopAppBar(scaffoldState = scaffoldState, scope = scope, navController = navController, viewModel = recipeViewModel)
                Screen.CreateImage.route -> CreateImageTopAppBar(scaffoldState = scaffoldState, scope = scope, navController = navController, viewModel = recipeViewModel)
                Screen.Edit.route -> EditTopAppBar(scaffoldState = scaffoldState, scope = scope, navController = navController, viewModel = recipeViewModel)

                else -> {  }
            }
         },
        backLayerContent = {
            BasilBackLayer(scaffoldState = scaffoldState, scope = scope, navController = navController)
        },
        frontLayerContent = {
            NavHost(navController = navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) { HomeScreen(navController = navController, viewModel = recipeViewModel) }
                composable(Screen.Detail.route) {
                    var recipe = navController.previousBackStackEntry?.arguments?.getParcelable<RecipeData>("recipe_detail")
                    DetailScreen(navController = navController, recipe = recipe)
                }
                composable(Screen.CreateUrl.route) { CreateRecipe(navController = navController, viewModel = recipeViewModel) }
                composable(Screen.CreateImage.route) { CreateImageRecipe(navController = navController, viewModel = recipeViewModel) }
                composable(Screen.Edit.route) {
                    var recipe = navController.previousBackStackEntry?.arguments?.getParcelable<RecipeData>("recipe_edit")
                    EditScreen(navController = navController, recipe = recipe, viewModel = recipeViewModel)
                }

            }
        },
        backLayerBackgroundColor = MaterialTheme.colors.background,
        frontLayerBackgroundColor = MaterialTheme.colors.background,
        frontLayerShape = RectangleShape,
        frontLayerScrimColor = MaterialTheme.colors.background.copy(0.5f)

    )
}


enum class SplashState { SHOWN, COMPLETED }





