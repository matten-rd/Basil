package com.example.basil.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.basil.data.RecipeData
import com.example.basil.data.RecipeState
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.components.*
import com.example.basil.util.getDurationFromHourAndMinute
import com.example.basil.util.getHoursFromDuration
import com.example.basil.util.getMinutesFromDuration
import com.example.basil.util.humanReadableDuration
import kotlinx.coroutines.launch

@ExperimentalComposeUiApi
@Composable
fun CreateUrlRecipe(
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val url by viewModel.url.observeAsState(initial = "")
    Column(modifier = Modifier.padding(16.dp)) {
        BasilTextField(
            value = url,
            onValueChange = { viewModel.onUrlChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Ange en länk till ett recept"
        )
    }
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun CreateImageRecipe(
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val categoryOptions = listOf("Förrätt", "Huvudrätt", "Efterrätt", "Bakning")
    // TODO: Use placeholders for images instead
    val initialRecipe = RecipeData(
        url = "",
        imageUrl = "https://picsum.photos/600/600",
        recipeImageUrl = "https://picsum.photos/600/600",
        recipeState = RecipeState.IMAGE,
        title = "",
        description = "",
        ingredients = listOf(),
        instructions = listOf(),
        cookTime = "PT0M",
        mealType = categoryOptions[1],
        yield = "4",
        isLiked = false
    )
    viewModel.onRecipeChange(initialRecipe)

    CreateImageRecipeState(
        navController = navController,
        viewModel = viewModel,
        initialRecipe = initialRecipe,
        categoryOptions = categoryOptions
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
fun CreateImageRecipeState(
    navController: NavController,
    viewModel: RecipeViewModel,
    initialRecipe: RecipeData,
    categoryOptions: List<String>
) {
    var selectedBottomSheet by remember { mutableStateOf(BottomSheetScreens.PORTIONS) }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    // Image state
    var thumbnailImage by remember { mutableStateOf(initialRecipe.imageUrl) }
    var recipeImage by remember { mutableStateOf(initialRecipe.recipeImageUrl) }

    val openSheet: (BottomSheetScreens) -> Unit = {
        selectedBottomSheet = it
        scope.launch { sheetState.show() }
    }
    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }
    }

    val updatingRecipe by viewModel.recipe.observeAsState(initialRecipe)

    val launcherThumbnail = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            thumbnailImage = uri.toString()
            viewModel.onRecipeChange(updatingRecipe.copy(imageUrl = thumbnailImage))
        }
    }
    val launcherRecipeImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            recipeImage = uri.toString()
            viewModel.onRecipeChange(updatingRecipe.copy(recipeImageUrl = recipeImage))
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            when (selectedBottomSheet) {
                BottomSheetScreens.CATEGORY -> BottomSheetCategory(
                    options = categoryOptions,
                    selected = updatingRecipe.mealType,
                    setSelected = { viewModel.onRecipeChange(updatingRecipe.copy(mealType = it)) },
                    closeSheet = closeSheet
                )
                BottomSheetScreens.PORTIONS -> BottomSheetPortions(
                    range = 1..20,
                    selected = updatingRecipe.yield.toIntOrNull() ?: 4,
                    setSelected = { viewModel.onRecipeChange(updatingRecipe.copy(yield = it.toString())) },
                    closeSheet = closeSheet
                )
                BottomSheetScreens.TIME -> BottomSheetTime(
                    hourRange = 0..10,
                    selectedHour = getHoursFromDuration(updatingRecipe.cookTime),
                    setSelectedHour = {
                        val min = getMinutesFromDuration(updatingRecipe.cookTime)
                        val newCookTime = getDurationFromHourAndMinute(it, min)
                        viewModel.onRecipeChange(updatingRecipe.copy(cookTime = newCookTime))
                    },
                    minutesRange = 0..59,
                    selectedMinute = getMinutesFromDuration(updatingRecipe.cookTime),
                    setSelectedMinute = {
                        val hour = getHoursFromDuration(updatingRecipe.cookTime)
                        val newCookTime = getDurationFromHourAndMinute(hour, it)
                        viewModel.onRecipeChange(updatingRecipe.copy(cookTime = newCookTime))
                    },
                    closeSheet = closeSheet
                )
                else -> {}
            }
        }
    ) {
        Column(modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .padding(16.dp)
        ) {
            CreateImageRecipeContent(
                openSheet = openSheet,
                title = updatingRecipe.title,
                setTitle = { viewModel.onRecipeChange(updatingRecipe.copy(title = it)) },
                description = updatingRecipe.description,
                setDescription = { viewModel.onRecipeChange(updatingRecipe.copy(description = it)) },
                thumbnailImage = updatingRecipe.imageUrl,
                setThumbnailImage = { launcherThumbnail.launch(arrayOf("image/*")) },
                recipeImage = updatingRecipe.recipeImageUrl,
                setRecipeImage = { launcherRecipeImage.launch(arrayOf("image/*")) },
                portions = updatingRecipe.yield,
                time = humanReadableDuration(updatingRecipe.cookTime),
                category = updatingRecipe.mealType,
                setCategory = { viewModel.onRecipeChange(updatingRecipe.copy(mealType = it)) }
            )
        }
    }

}

@ExperimentalComposeUiApi
@Composable
fun CreateImageRecipeContent(
    openSheet: (BottomSheetScreens) -> Unit,
    title: String,
    setTitle: (String) -> Unit,
    description: String,
    setDescription: (String) -> Unit,
    thumbnailImage: String,
    setThumbnailImage: () -> Unit,
    recipeImage: String,
    setRecipeImage: () -> Unit,
    portions: String,
    time: String,
    category: String,
    setCategory: (String) -> Unit
) {
    TextFieldWithHeader(header = "Titel", value = title, onValueChange = setTitle, placeholder = "Ange titel")
    BasilSpacer()
    SubHeader(subheader = "Miniatyrbild")
    EditImage(url = thumbnailImage, setImage = setThumbnailImage)
    BasilSpacer()
    SubHeader(subheader = "Receptbild")
    EditImage(url = recipeImage, setImage = setRecipeImage)
    BasilSpacer()
    TextFieldWithHeader(header = "Beskrivning", value = description, onValueChange = setDescription, placeholder = "Ange beskrivning")
    BasilSpacer()
    TextAndButton(text = "$portions Portioner", onClick = { openSheet(BottomSheetScreens.PORTIONS) })
    BasilSpacer()
    TextAndButton(text = time, onClick = { openSheet(BottomSheetScreens.TIME) })
    BasilSpacer()
    EditCategory(selected = category, setSelected = setCategory, onClick = { openSheet(BottomSheetScreens.CATEGORY) })
}
