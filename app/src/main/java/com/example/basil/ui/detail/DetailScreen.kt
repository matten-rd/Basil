package com.example.basil.ui.detail

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.Crossfade

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import com.example.basil.data.RecipeData
import com.example.basil.data.RecipeState
import com.example.basil.ui.navigation.Screen
import com.example.basil.ui.theme.Green100
import com.example.basil.util.*
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.launch


private val TabHeight = 50.dp

@ExperimentalMaterialApi
@Composable
fun DetailScreenRow(navController: NavController, recipe: RecipeData?) {
    BoxWithConstraints(
        contentAlignment = Alignment.TopCenter
    ) {
        val dragRange = constraints.maxHeight.toFloat()
        val scope = rememberCoroutineScope()

        var sheetState = rememberSwipeableState(initialValue = SheetState.CLOSED)

        Box(
            Modifier
                .swipeable(
                    state = sheetState,
                    anchors = mapOf(
                        0f to SheetState.CLOSED,
                        -dragRange to SheetState.OPEN
                    ),
                    thresholds = { _, _ -> FractionalThreshold(0.5f) },
                    orientation = Orientation.Vertical
                )
        ) {
            val openFraction = if (sheetState.offset.value.isNaN()) {
                0f
            } else {
                -sheetState.offset.value / dragRange
            }.coerceIn(0f, 1f)

            DetailLanding(recipe = recipe)

            TabSheet(
                recipe = recipe,
                openFraction = openFraction,
                height = this@BoxWithConstraints.constraints.maxHeight.toFloat()
            ) {
                scope.launch {
                    sheetState.animateTo(it)
                }
            }
        }

    }
}
enum class SheetState { OPEN, CLOSED }

@Composable
fun DetailLanding(recipe: RecipeData?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleAndDescription(recipe = recipe)
        Spacer(modifier = Modifier.height(16.dp))
        PortionsAndCategory(recipe = recipe)
        Spacer(
            modifier = Modifier
                .background(color = MaterialTheme.colors.primary)
                .defaultMinSize(300.dp, 1.dp)
        )
    }
}

@Composable
fun TitleAndDescription(recipe: RecipeData?) {
    ConstraintLayout() {
        val (title, line1, emptybox, descbox, desctext) = createRefs()

        // Title
        Text(
            text = recipe?.title ?: "",
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.secondary,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            maxLines = 4,
            modifier = Modifier.constrainAs(title) {
                centerHorizontallyTo(parent)
                top.linkTo(parent.top, margin = 16.dp)
            }
        )

        Box(
            modifier = Modifier
                .background(color = Green100)
                .defaultMinSize(300.dp, 75.dp)
                .zIndex(-1f)
                .constrainAs(emptybox) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(title.bottom, margin = (-8).dp)
                }
        )

        Spacer(
            modifier = Modifier
                .background(color = MaterialTheme.colors.primary)
                .defaultMinSize(300.dp, 1.dp)
                .constrainAs(line1) {
                    centerHorizontallyTo(parent)
                    top.linkTo(emptybox.bottom)
                }
        )
        // Description
        Box(
            modifier = Modifier
                .background(color = Green100)
                .defaultMinSize(300.dp, 225.dp)
                .zIndex(-1f)
                .constrainAs(descbox) {
                    centerHorizontallyTo(parent)
                    top.linkTo(line1.bottom)
                }
        )
        Text(
            text = recipe?.description ?: "",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
            lineHeight = 25.sp,
            maxLines = 9,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .constrainAs(desctext) {
                    centerTo(descbox)
                }
        )
    }
}

@Composable
fun TabSheet(
    recipe: RecipeData?,
    openFraction: Float,
    height: Float,
    updateSheet: (SheetState) -> Unit
) {
    val tabSize = with(LocalDensity.current) { TabHeight.toPx() }
    val offsetY = lerp(height-tabSize, 0f, openFraction)
    // TODO: Remove this color transition
    val surfaceColor = lerpColor5(
        MaterialTheme.colors.secondary,
        MaterialTheme.colors.background,
        0f,
        0.3f,
        openFraction
    )
    Surface(
        color = surfaceColor,
        contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.background),
        modifier = Modifier.graphicsLayer {
            translationX = 0f
            translationY = offsetY
        }
    ) {
        // TODO: change to when statement and add IMAGE tab
        if (recipe?.recipeState == RecipeState.SCRAPED)
            InstructionsAndIngredients(
                recipe = recipe,
                openFraction = openFraction,
                surfaceColor = surfaceColor,
                updateSheet = updateSheet
            )
        else
            WebViewTabLayout(
                recipe = recipe,
                openFraction = openFraction,
                surfaceColor = surfaceColor,
                updateSheet = updateSheet
            )
    }
}


@Composable
fun PortionsAndCategory(
    modifier: Modifier = Modifier,
    recipe: RecipeData?
) {
    Row(
        modifier = modifier
            .width(300.dp)
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.defaultMinSize(100.dp, 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Portioner", style = MaterialTheme.typography.subtitle2)
            Text(text = recipe?.yield.toString(), style = MaterialTheme.typography.subtitle1)
        }
        Spacer(
            modifier = Modifier
                .width((0.75f).dp)
                .height(40.dp)
                .background(color = MaterialTheme.colors.primary)
        )
        Column(
            modifier = Modifier.defaultMinSize(100.dp, 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Tid", style = MaterialTheme.typography.subtitle2)
            Text(text = humanReadableDuration(recipe?.cookTime ?: "PT0M"), style = MaterialTheme.typography.subtitle1)
        }

    }
}

@Composable
fun InstructionsAndIngredients(
    recipe: RecipeData?,
    openFraction: Float,
    surfaceColor: Color = MaterialTheme.colors.background,
    updateSheet: (SheetState) -> Unit
) {

    var state by remember { mutableStateOf(0) }
    val titles = listOf("Ingredienser", "Instruktioner", recipe?.let { getDomainName(it.url) } ?: "Recept")

    Box(modifier = Modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TabRow(
                selectedTabIndex = state,
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.primary,
                modifier = Modifier.height(50.dp)
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = state == index,
                        onClick = {
                            updateSheet(SheetState.OPEN)
                            state = index
                        }
                    ) {
                        Text(text = title)
                    }
                }
            }
            Crossfade(targetState = state) {
                when (it) {
                    0 -> IngredientsTab(recipe = recipe)
                    1 -> InstructionsTab(recipe = recipe)
                    2 -> WebViewTab(recipe = recipe)
                }
            }
        }

    }

}

@Composable
fun IngredientsTab(
    modifier: Modifier = Modifier,
    recipe: RecipeData?
) {
    Column(modifier = modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        recipe?.ingredients?.forEach { ingredient ->
            Text(
                text = "•  $ingredient",
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.padding(vertical = 12.dp))
        }
    }
}

@Composable
fun InstructionsTab(
    modifier: Modifier = Modifier,
    recipe: RecipeData?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = modifier.weight(5f)) {
            recipe?.instructions?.forEach { instruction ->
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.subtitle1
                )
            }
        }
        Column(modifier = modifier.weight(1f), horizontalAlignment = Alignment.End) {
            recipe?.instructions?.forEachIndexed { index, instruction ->
                Text(
                    text = String.format("%02d", index+1),
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }

}

@Composable
fun WebViewTabLayout(
    recipe: RecipeData?,
    openFraction: Float,
    surfaceColor: Color = MaterialTheme.colors.background,
    updateSheet: (SheetState) -> Unit
) {
    var state by remember { mutableStateOf(0) }
    val titles = listOf(recipe?.url?.let { getDomainName(it) } ?: "RECEPT")

    Box(modifier = Modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TabRow(
                selectedTabIndex = state,
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.primary,
                modifier = Modifier.height(50.dp)
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = state == index,
                        onClick = {
                            updateSheet(SheetState.OPEN)
                            state = index
                        }
                    ) {
                        Text(text = title)
                    }
                }
            }
            WebViewTab(recipe = recipe)
        }
    }

}


@Composable
fun WebViewTab(
    recipe: RecipeData?
) {
    Row(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = WebViewClient()
                if (recipe != null) {
                    loadUrl(recipe.url)
                }
            }
        }, update = {
            if (recipe != null) {
                it.loadUrl(recipe.url)
            }
        })
    }

}

@Composable
fun InstructionItem(
    header: String,
    instruction: String
) {

}