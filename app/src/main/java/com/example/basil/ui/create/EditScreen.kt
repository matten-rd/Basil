package com.example.basil.ui.create


import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.basil.R
import com.example.basil.data.RecipeData
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.components.*
import com.example.basil.ui.theme.Orange800
import com.example.basil.util.*
import kotlinx.coroutines.launch

enum class BottomSheetScreens { CATEGORY, PORTIONS, TIME, SOURCE }

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun EditScreen(
    navController: NavController,
    recipe: RecipeData?,
    viewModel: RecipeViewModel,
    scaffoldState: BackdropScaffoldState
) {
    if (recipe != null) {
        viewModel.onRecipeChange(recipe)
        EditScreen1(navController = navController, recipe = recipe, viewModel = viewModel, scaffoldState = scaffoldState)
    } else {
        ErrorScreen(errorMessage = "Oops! Något gick fel! Försök igen snart!")
    }
}

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun EditScreen1(
    navController: NavController,
    recipe: RecipeData,
    viewModel: RecipeViewModel,
    scaffoldState: BackdropScaffoldState
) {
    var selectedBottomSheet by remember { mutableStateOf(BottomSheetScreens.PORTIONS) }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    // Category state
    val categoryOptions = listOf("Förrätt", "Huvudrätt", "Efterrätt", "Bakning")
    var category by remember { mutableStateOf(recipe.mealType) }
    // Portion state
    val (numberOfPortions, setNumberOfPortions) = remember { mutableStateOf(recipe.yield.toIntOrNull() ?: 4) }
    // Time state
    val (hour, setHour) = remember { mutableStateOf(getHoursFromDuration(recipe.cookTime)) }
    val (minute, setMinute) = remember { mutableStateOf(getMinutesFromDuration(recipe.cookTime)) }
    // Title state
    var title by remember { mutableStateOf(recipe.title) }
    // Description state
    var description by remember { mutableStateOf(recipe.description) }
    // Source state
    var source by remember { mutableStateOf(recipe.url) }
    // Image state
    var image by remember { mutableStateOf(recipe.imageUrl) }
    // Ingredients state
    var ingredients by remember { mutableStateOf(recipe.ingredients.toMutableStateList()) }
    var newIngredient by remember { mutableStateOf("") }
    // Ingredients state
    var instructions by remember { mutableStateOf(recipe.instructions.toMutableStateList()) }
    var newInstruction by remember { mutableStateOf("") }

    val updatingRecipe1 by viewModel.recipe.observeAsState(
        RecipeData(
            id = recipe.id,
            url = source,
            imageUrl = image,
            recipeImageUrl = recipe.recipeImageUrl,
            recipeState = recipe.recipeState,
            title = title,
            description = description,
            ingredients = ingredients,
            instructions = instructions,
            cookTime = recipe.cookTime,
            yield = numberOfPortions.toString(),
            mealType = category,
            isLiked = recipe.isLiked
        )
    )

    val updatingRecipe by viewModel.recipe.observeAsState(initial = recipe)

    val openSheet: (BottomSheetScreens) -> Unit = {
        selectedBottomSheet = it
        scope.launch { sheetState.show() }
    }
    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }
    }
    val onShowIngredientSnackBar: (Int, String) -> Unit = { idx, s ->
        scope.launch {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = "Ingrediens borttagen.",
                actionLabel = "ÅNGRA",
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> Log.d("snackBarResult", "Dismissed")
                SnackbarResult.ActionPerformed -> {
                    ingredients.add(idx, s)
                    viewModel.onRecipeChange(updatingRecipe.copy(ingredients = ingredients))
                }
            }
        }
    }
    val onShowInstructionSnackBar: (Int, String) -> Unit = { idx, s ->
        scope.launch {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = "Instruktionssteg borttaget.",
                actionLabel = "ÅNGRA",
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> Log.d("snackBarResult", "Dismissed")
                SnackbarResult.ActionPerformed -> {
                    instructions.add(idx, s)
                    viewModel.onRecipeChange(updatingRecipe.copy(instructions = instructions))
                }
            }
        }
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            image = uri.toString()
            viewModel.onRecipeChange(updatingRecipe.copy(imageUrl = image))
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
                //TODO: this should redo the parsing step!
                BottomSheetScreens.SOURCE -> BottomSheetSource(
                    source = updatingRecipe.url,
                    setSource = { viewModel.onRecipeChange(updatingRecipe.copy(url = it)) },
                    closeSheet = closeSheet
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            EditContent(
                openSheet = openSheet,
                title = updatingRecipe.title,
                setTitle = { viewModel.onRecipeChange(updatingRecipe.copy(title = it)) },
                description = updatingRecipe.description,
                setDescription = { viewModel.onRecipeChange(updatingRecipe.copy(description = it)) },
                source = updatingRecipe.url,
                image = updatingRecipe.imageUrl,
                setImage = { launcher.launch(arrayOf("image/*")) },
                ingredients = updatingRecipe.ingredients,
                addIngredient = {
                    ingredients.add(newIngredient)
                    viewModel.onRecipeChange(updatingRecipe.copy(ingredients = ingredients))
                    newIngredient = ""
                                },
                deleteIngredient = {
                    ingredients.removeAt(it)
                    viewModel.onRecipeChange(updatingRecipe.copy(ingredients = ingredients))
                                   },
                newIngredient = newIngredient,
                setNewIngredient = { newIngredient = it },
                instructions = updatingRecipe.instructions,
                addInstruction = {
                    instructions.add(newInstruction)
                    viewModel.onRecipeChange(updatingRecipe.copy(instructions = instructions))
                    newInstruction = ""
                                 },
                deleteInstruction = {
                    instructions.removeAt(it)
                    viewModel.onRecipeChange(updatingRecipe.copy(instructions = instructions))
                                    },
                newInstruction = newInstruction,
                setNewInstruction = { newInstruction = it },
                category = updatingRecipe.mealType,
                setCategory = { viewModel.onRecipeChange(updatingRecipe.copy(mealType = it)) },
                numberOfPortions = updatingRecipe.yield,
                time = humanReadableDuration(updatingRecipe.cookTime),
                onShowIngredientSnackbar = { idx, s ->
                    onShowIngredientSnackBar(idx, s)
                },
                onShowInstructionSnackbar = { idx, s ->
                    onShowInstructionSnackBar(idx, s)
                }

            )
        }
    }
}


@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun EditContent(
    openSheet: (BottomSheetScreens) -> Unit,
    title: String,
    setTitle: (String) -> Unit,
    description: String,
    setDescription: (String) -> Unit,
    source: String,
    image: String,
    setImage: () -> Unit,
    ingredients: List<String>,
    addIngredient: () -> Unit,
    deleteIngredient: (Int) -> Unit,
    newIngredient: String,
    setNewIngredient: (String) -> Unit,
    instructions: List<String>,
    addInstruction: () -> Unit,
    deleteInstruction: (Int) -> Unit,
    newInstruction: String,
    setNewInstruction: (String) -> Unit,
    category: String,
    setCategory: (String) -> Unit,
    numberOfPortions: String,
    time: String,
    onShowIngredientSnackbar: (Int, String) -> Unit,
    onShowInstructionSnackbar: (Int, String) -> Unit
) {
    TextFieldWithHeader(header = "Titel", value = title, onValueChange = setTitle, placeholder = "Ange titel")
    BasilSpacer()
    TextAndButton(text = getDomainName(source), buttonText = "ÄNDRA KÄLLA", onClick = { openSheet(BottomSheetScreens.SOURCE) })
    BasilSpacer()
    EditImage(url = image, setImage = setImage)
    BasilSpacer()
    TextFieldWithHeader(header = "Beskrivning", value = description, onValueChange = setDescription, placeholder = "Ange beskrivning")
    BasilSpacer()
    EditableList(
        subHeader = "Ingredienser",
        placeholder = "Lägg till en ingrediens",
        list = ingredients,
        addToList = addIngredient,
        deleteFromList = deleteIngredient,
        newValue = newIngredient,
        setNewValue = setNewIngredient,
        onShowSnackbar = onShowIngredientSnackbar,
        listItems = { Text(text = it, modifier = Modifier.padding(vertical = 6.dp)) }
    )
    BasilSpacer()
    EditableList(
        subHeader = "Instruktioner",
        placeholder = "Lägg till ett steg",
        list = instructions,
        addToList = addInstruction,
        deleteFromList = deleteInstruction,
        newValue = newInstruction,
        setNewValue = setNewInstruction,
        onShowSnackbar = onShowInstructionSnackbar,
        listHeaders = { Text(text = "Steg ${it+1}", modifier = Modifier.padding(top = 4.dp)) },
        listItems = { BasilTextField(value = it, onValueChange = { /*TODO: Edit current instructions, something with the index maybe*/ }, modifier = Modifier.fillMaxWidth(), imeAction = ImeAction.Next) }
    )
    BasilSpacer()
    TextAndButton(text = "$numberOfPortions Portioner", onClick = { openSheet(BottomSheetScreens.PORTIONS) })
    BasilSpacer()
    TextAndButton(text = time, onClick = { openSheet(BottomSheetScreens.TIME) })
    BasilSpacer()
    EditCategory(selected = category, setSelected = setCategory, onClick = { openSheet(BottomSheetScreens.CATEGORY) })
}

@Composable
fun EditImage(
    url: String,
    setImage: () -> Unit
) {
    NetWorkImage(
        url = url,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = setImage)
    )
}

@ExperimentalComposeUiApi
@Composable
fun ContentEdit(
    list: List<String>,
    delete: (Int) -> Unit,
    onShowSnackbar: (Int, String) -> Unit,
    header: @Composable (Int) -> Unit = {}
) {
    Column {
        list.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    delete(index)
                    onShowSnackbar(index, item)
                }) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_dismiss_circle_20_regular), contentDescription = null, tint = Orange800)
                }
                Column(Modifier.weight(1f)) {
                    header(index)
                    BasilTextField(value = item, onValueChange = { /*TODO: Edit current item, something with the index maybe*/ }, modifier = Modifier.fillMaxWidth(), imeAction = ImeAction.Next)
                }
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun EditCategory(
    selected: String,
    setSelected: (String) -> Unit,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SubHeader(subheader = "Receptkategori")
        ReadonlyTextField(
            value = selected,
            onValueChange = { setSelected(it) },
            onClick = onClick,
            trailingIcon = { Icon(painter = painterResource(id = R.drawable.ic_fluent_chevron_up_down_24_regular), contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun BottomSheetSource(
    source: String,
    setSource: (String) -> Unit,
    closeSheet: () -> Unit
) {
    BottomSheetBase(
        title = "Receptkälla",
        subTitle = "Ändra receptets källa",
        buttonText = "Spara",
        onClick = closeSheet
    ) {
        BasilTextField(
            value = source,
            onValueChange = setSource,
            placeholder = "Ange ny länk",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
fun BottomSheetPortions(
    range: IntRange,
    selected: Int,
    setSelected: (Int) -> Unit,
    closeSheet: () -> Unit
) {
    BottomSheetBase(
        title = "Portioner",
        subTitle = "Välj antal portioner",
        buttonText = "Spara",
        onClick = closeSheet
    ) {
        NumberPicker(
            state = remember { mutableStateOf(selected) },
            range = range,
            modifier = Modifier.fillMaxWidth(),
            onStateChanged = setSelected
        )
    }
}

@Composable
fun BottomSheetTime(
    hourRange: IntRange,
    selectedHour: Int,
    setSelectedHour: (Int) -> Unit,
    minutesRange: IntRange,
    selectedMinute: Int,
    setSelectedMinute: (Int) -> Unit,
    closeSheet: () -> Unit
) {
    BottomSheetBase(
        title = "Tid",
        subTitle = "Hur lång tid tar det att laga?",
        buttonText = "Spara",
        onClick = closeSheet
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Timmar")
                NumberPicker(
                    state = remember { mutableStateOf(selectedHour) },
                    range = hourRange,
                    modifier = Modifier.fillMaxWidth(),
                    onStateChanged = setSelectedHour
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Minuter")
                NumberPicker(
                    state = remember { mutableStateOf(selectedMinute) },
                    range = minutesRange,
                    modifier = Modifier.fillMaxWidth(),
                    onStateChanged = setSelectedMinute
                )
            }
        }
    }
}


@Composable
fun BottomSheetCategory(
    options: List<String>,
    selected: String,
    setSelected: (String) -> Unit,
    closeSheet: () -> Unit
) {
    BottomSheetBase(
        title = "Kategori",
        subTitle = "Välj en kategori för det här receptet",
        buttonText = "Spara",
        onClick = closeSheet
    ) {
        Column {
            options.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (text == selected),
                            onClick = { setSelected(text) }
                        )
                        .padding(vertical = 12.dp)
                ) {
                    RadioButton(
                        selected = (text == selected),
                        onClick = { setSelected(text) }
                    )
                    Text(
                        text = text,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}




