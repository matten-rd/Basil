package com.example.basil.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.basil.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.example.basil.data.RecipeData
import com.example.basil.ui.RecipeViewModel
import com.example.basil.util.isValidUrl
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.TopAppBar


@Composable
fun BaseTopAppBar(
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = MaterialTheme.colors.primary,
    elevation: Dp = 0.dp
) {
    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.statusBars,
            applyBottom = false
        ),
        elevation = elevation
    )
}


@ExperimentalMaterialApi
@Composable
fun CreateBaseTopAppBar(
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    BaseTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_arrow_reply_24_regular), contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onSave) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_document_checkmark_24_regular), contentDescription = null)
            }
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun HomeTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    BaseTopAppBar(
        navigationIcon = {
            if (scaffoldState.isConcealed) {
                IconButton(onClick = { scope.launch { scaffoldState.reveal() } }) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_document_search_24_regular), contentDescription = null)
                }
            } else {
                IconButton(onClick = { scope.launch { scaffoldState.conceal() } }) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_dismiss_24_regular), contentDescription = null)
                }
            }
        },
        actions = {
            val isBasilGrid by viewModel.isBasilGrid.observeAsState()
            IconButton(onClick = { viewModel.onLayoutGridStateChange() }) {
                if (isBasilGrid == true)
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_grid_20_regular), contentDescription = null)
                else
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_board_20_regular), contentDescription = null)
            }
        }
    )
}


@ExperimentalMaterialApi
@Composable
fun DetailTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    recipe: RecipeData?,
    viewModel: RecipeViewModel
) {
    if (recipe != null) {
        viewModel.onRecipeChange(recipe)
        DetailTopAppBarContent(
            scaffoldState = scaffoldState,
            scope = scope,
            navController = navController,
            recipe = recipe,
            viewModel = viewModel
        )
    } else {
        ErrorTopAppBar(navController = navController)
    }
}

@ExperimentalMaterialApi
@Composable
fun DetailTopAppBarContent(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    recipe: RecipeData,
    viewModel: RecipeViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isValidUrl = isValidUrl(recipe.url)

    val intent = remember {
        Intent(Intent.ACTION_VIEW, Uri.parse(recipe.url))
    }

    val onShowSnackBar: (RecipeData) -> Unit = { recipeSnack ->
        scope.launch {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = "Recect raderat.",
                actionLabel = "ÅNGRA",
                duration = SnackbarDuration.Long
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> Log.d("snackBarResult", "Dismissed")
                SnackbarResult.ActionPerformed -> viewModel.insertRecipe(recipeSnack)
            }
        }
    }
    val updatingRecipe by viewModel.recipe.observeAsState(initial = recipe)

    BaseTopAppBar(
        navigationIcon = {
            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_arrow_reply_24_regular), contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { viewModel.onLikeClick(updatingRecipe) }) {
                if (updatingRecipe.isLiked)
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_heart_24_filled), contentDescription = null)
                else
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_heart_24_regular), contentDescription = null)
            }
            IconButton(onClick = {
                if (isValidUrl)
                    context.startActivity(intent)
                else {
                    scope.launch {
                        scaffoldState.snackbarHostState
                            .showSnackbar("Länken går inte att öppna.")
                    }
                }
            }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_open_24_regular), contentDescription = null)
            }

            IconButton(onClick = { expanded = true }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_more_vertical_24_regular), contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.55f)
            ) {
                DropdownMenuItem(
                    onClick = {
                        navController.currentBackStackEntry?.arguments?.putParcelable("recipe_edit", recipe)
                        navController.navigate(Screen.Edit.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_document_edit_24_regular), contentDescription = null, modifier = Modifier.padding(4.dp))
                    Text(text = "Redigera", modifier = Modifier.padding(12.dp, 0.dp), style = MaterialTheme.typography.body1)
                }
                DropdownMenuItem(onClick = {
                    viewModel.deleteRecipe(recipe)
                    onShowSnackBar(recipe)
                    navController.navigate(Screen.Home.route)
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_delete_24_regular), contentDescription = null, modifier = Modifier.padding(4.dp))
                    Text(text = "Radera", modifier = Modifier.padding(12.dp, 0.dp), style = MaterialTheme.typography.body1)
                }
            }
        }
    )
}


@ExperimentalMaterialApi
@Composable
fun CreateImageTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val updatingRecipe by viewModel.recipe.observeAsState()
    CreateBaseTopAppBar(
        onBack = { navController.navigate(Screen.Home.route) },
        onSave = {
            if (updatingRecipe != null) {
                viewModel.insertRecipe(updatingRecipe!!)
                navController.navigate(Screen.Home.route)
            } else {
                scope.launch {
                    scaffoldState.snackbarHostState
                        .showSnackbar("Något gick fel!")
                }
            }
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun CreateUrlTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val url by viewModel.url.observeAsState()
    CreateBaseTopAppBar(
        onBack = { navController.navigate(Screen.Home.route) },
        onSave = {
            if (!url.isNullOrEmpty() && isValidUrl(url)) {
                viewModel.createRecipe(url!!)
                navController.navigate(Screen.Home.route)
            } else {
                scope.launch {
                    scaffoldState.snackbarHostState
                        .showSnackbar("Ange en giltig länk till ett recept!")
                }
            }
        }
    )
}

@ExperimentalMaterialApi
@Composable
fun EditTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val currentRecipe by viewModel.recipe.observeAsState()
    val contentResolver = LocalContext.current.applicationContext.contentResolver
    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    CreateBaseTopAppBar(
        onBack = { navController.navigate(Screen.Home.route) },
        onSave = {
            currentRecipe?.let { viewModel.updateRecipe(it) }
            try {
                contentResolver.takePersistableUriPermission(Uri.parse(currentRecipe?.imageUrl), takeFlags)
            } catch (e: Exception) {
                Log.e("Persistable Uri permission", e.message.toString())
            }
            navController.navigate(Screen.Home.route)
        }
    )
}


@Composable
fun ErrorTopAppBar(navController: NavController) {
    BaseTopAppBar(
        navigationIcon = {
            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_arrow_reply_24_regular), contentDescription = null)
            }
        }
    )
}
