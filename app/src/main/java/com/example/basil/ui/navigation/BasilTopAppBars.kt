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
import androidx.core.content.ContextCompat.startActivity

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.basil.data.RecipeData


@ExperimentalMaterialApi
@Composable
fun HomeTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController
) {
    TopAppBar(
        title = {  },
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
            var clickCount by remember { mutableStateOf(0) }
            IconButton(
                onClick = {
                    scope.launch {
                        scaffoldState.snackbarHostState
                            .showSnackbar("Snackbar #${++clickCount}")
                    }
                }
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_filter_24_regular), contentDescription = null)
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.primary,
        elevation = 0.dp
    )
}


@ExperimentalMaterialApi
@Composable
fun DetailTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController,
    recipe: RecipeData?
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val intent = remember {
        Intent(Intent.ACTION_VIEW, Uri.parse(recipe?.url))
    }
    TopAppBar(
        title = {  },
        navigationIcon = {
            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_arrow_reply_24_regular), contentDescription = null)
            }
        },
        actions = {
            // FIXME: This liked state should be taken from recipeData and updated in the DB
            var liked by remember { mutableStateOf(false) }
            IconButton(onClick = { liked = !liked }) {
                if (liked)
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_heart_24_filled), contentDescription = null)
                else
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_heart_24_regular), contentDescription = null)
            }
            IconButton(onClick = { context.startActivity(intent) }) {
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
                        navController.currentBackStackEntry?.arguments?.putParcelable("recipe", recipe)
                        navController.navigate(Screen.Edit.route)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_document_edit_24_regular), contentDescription = null, modifier = Modifier.padding(4.dp))
                    Text(text = "Redigera", modifier = Modifier.padding(12.dp, 0.dp), style = MaterialTheme.typography.body1)
                }
                DropdownMenuItem(onClick = { /*TODO: Delete this recipe */ }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_delete_24_regular), contentDescription = null, modifier = Modifier.padding(4.dp))
                    Text(text = "Radera", modifier = Modifier.padding(12.dp, 0.dp), style = MaterialTheme.typography.body1)
                }
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.primary,
        elevation = 0.dp
    )
}

@ExperimentalMaterialApi
@Composable
fun CreateTopAppBar(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController
) {
    TopAppBar(
        title = {  },
        navigationIcon = {
            IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_arrow_reply_24_regular), contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { /* TODO: Save the recipe and navigate back (and maybe display a snackbar) */ }) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_document_checkmark_24_regular), contentDescription = null)
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.primary,
        elevation = 0.dp
    )
}