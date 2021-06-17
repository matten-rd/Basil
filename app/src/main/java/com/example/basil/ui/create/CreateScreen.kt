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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.basil.R
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.components.BasilSpacer
import com.example.basil.ui.components.BasilTextField
import com.example.basil.ui.navigation.Screen
import com.example.basil.util.getHoursFromDuration
import com.example.basil.util.getMinutesFromDuration
import kotlinx.coroutines.launch

@Composable
fun CreateRecipe(
    navController: NavController,
    viewModel: RecipeViewModel
) {
    var url by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(16.dp)) {
        BasilTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "Ange en länk till ett recept"
        )
        Button(
            onClick = {
                if (url.isNotEmpty()) {
                    saveAndNavigateBack(url, viewModel, navController)
                }
            }
        ) {
            Text(text = "Spara")
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun CreateImageRecipe(
    navController: NavController,
    viewModel: RecipeViewModel
) {
    var selectedBottomSheet by remember { mutableStateOf(BottomSheetScreens.PORTIONS) }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    // Category state
    val categoryOptions = listOf("Förrätt", "Huvudrätt", "Efterrätt", "Bakning")
    val (category, setCategory) = remember { mutableStateOf(categoryOptions[1]) }
    // Portion state
    val (numberOfPortions, setNumberOfPortions) = remember { mutableStateOf(4) }
    // Time state
    val (hour, setHour) = remember { mutableStateOf(getHoursFromDuration("PT0M")) }
    val (minute, setMinute) = remember { mutableStateOf(getMinutesFromDuration("PT0M")) }
    // Title state
    var title by remember { mutableStateOf("") }
    // Description state
    var description by remember { mutableStateOf("") }
    // Image state
    var thumbnailImage by remember { mutableStateOf("https://picsum.photos/600/600") }
    var recipeImage by remember { mutableStateOf("https://picsum.photos/600/600") }

    val openSheet: (BottomSheetScreens) -> Unit = {
        selectedBottomSheet = it
        scope.launch { sheetState.show() }
    }
    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }
    }
    val launcherThumbnail = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null)
            thumbnailImage = uri.toString()
    }
    val launcherRecipeImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null)
            recipeImage = uri.toString()
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            when (selectedBottomSheet) {
                BottomSheetScreens.CATEGORY -> BottomSheetCategory(options = categoryOptions, selected = category, setSelected = setCategory, closeSheet = closeSheet)
                BottomSheetScreens.PORTIONS -> BottomSheetPortions(range = 1..20, selected = numberOfPortions, setSelected = setNumberOfPortions, closeSheet = closeSheet)
                BottomSheetScreens.TIME -> BottomSheetTime(
                    hourRange = 0..10,
                    selectedHour = hour,
                    setSelectedHour = setHour,
                    minutesRange = 0..59,
                    selectedMinute = minute,
                    setSelectedMinute = setMinute,
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
                title = title,
                setTitle = { title = it },
                description = description,
                setDescription = { description = it },
                thumbnailImage = thumbnailImage,
                setThumbnailImage = { launcherThumbnail.launch(arrayOf("image/*")) },
                recipeImage = recipeImage,
                setRecipeImage = { launcherRecipeImage.launch(arrayOf("image/*")) },
                portions = numberOfPortions.toString(),
                time = "$hour h $minute min",
                category = category,
                setCategory = setCategory
            )
        }
    }

}

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
    EditTitle(title = title, setTitle = setTitle)
    BasilSpacer()
    EditImage(url = thumbnailImage, setImage = setThumbnailImage)
    BasilSpacer()
    EditImage(url = recipeImage, setImage = setRecipeImage)
    BasilSpacer()
    EditDescription(description = description, setDescription = setDescription)
    BasilSpacer()
    EditPortions(portions = portions, onClick = { openSheet(BottomSheetScreens.PORTIONS) })
    BasilSpacer()
    EditTime(time = time, onClick = { openSheet(BottomSheetScreens.TIME) })
    BasilSpacer()
    EditCategory(selected = category, setSelected = setCategory, onClick = { openSheet(BottomSheetScreens.CATEGORY) })
}


private fun saveAndNavigateBack(
    url: String,
    viewModel: RecipeViewModel,
    navController: NavController
) {
    viewModel.createRecipe(url)
    navController.navigate(Screen.Home.route)
}