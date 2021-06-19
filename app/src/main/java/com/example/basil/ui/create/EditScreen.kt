package com.example.basil.ui.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.navigation.NavController
import com.example.basil.R
import com.example.basil.data.RecipeData
import com.example.basil.data.RecipeState
import com.example.basil.ui.RecipeViewModel
import com.example.basil.ui.components.*
import com.example.basil.util.getDomainName
import com.example.basil.util.getHoursFromDuration
import com.example.basil.util.getMinutesFromDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class BottomSheetScreens { CATEGORY, PORTIONS, TIME, SOURCE }

@ExperimentalMaterialApi
@Composable
fun EditScreen(
    navController: NavController,
    recipe: RecipeData?,
    viewModel: RecipeViewModel
) {
    if (recipe != null) {
        EditScreen1(navController = navController, recipe = recipe, viewModel = viewModel)
    } else {
        ErrorScreen(errorMessage = "Oops! Något gick fel! Försök igen snart!")
    }
}

@ExperimentalMaterialApi
@Composable
fun EditScreen1(
    navController: NavController,
    recipe: RecipeData,
    viewModel: RecipeViewModel
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

    val updatingRecipe by viewModel.recipe.observeAsState(
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
            cookTime = recipe.cookTime, // write function to get this
            yield = numberOfPortions.toString(),
            mealType = category,
            isLiked = recipe.isLiked
        )
    )

    val updatingRecipe1 by viewModel.recipe.observeAsState(initial = recipe)


    val openSheet: (BottomSheetScreens) -> Unit = {
        selectedBottomSheet = it
        scope.launch { sheetState.show() }
    }
    val closeSheet: () -> Unit = {
        scope.launch { sheetState.hide() }
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null)
            image = uri.toString()
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
                //TODO: this should redo the parsing step!
                BottomSheetScreens.SOURCE -> BottomSheetSource(source = updatingRecipe.url, setSource = { viewModel.onRecipeChange(updatingRecipe.copy(url = it)) }, closeSheet = closeSheet)
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
                navController = navController,
                viewModel = viewModel,
                openSheet = openSheet,
                title = updatingRecipe.title,
                setTitle = { viewModel.onRecipeChange(updatingRecipe.copy(title = it))  },
                description = updatingRecipe.description,
                setDescription = { viewModel.onRecipeChange(updatingRecipe.copy(description = it)) },
                source = updatingRecipe.url,
                image = updatingRecipe.imageUrl,
                setImage = {
                    launcher.launch(arrayOf("image/*"))
                    viewModel.onRecipeChange(updatingRecipe.copy(imageUrl = image)) },
                ingredients = ingredients,
                addIngredient = { ingredients.add(newIngredient) ; newIngredient = "" },
                newIngredient = newIngredient,
                setNewIngredient = { newIngredient = it },
                instructions = instructions,
                addInstruction = { instructions.add(newInstruction) ; newInstruction = "" },
                newInstruction = newInstruction,
                setNewInstruction = { newInstruction = it },
                category = updatingRecipe.mealType,
                setCategory = { viewModel.onRecipeChange(updatingRecipe.copy(mealType = it)) },
                numberOfPortions = numberOfPortions.toString(),
                time = "$hour h $minute min"
            )
        }
    }
}


@ExperimentalMaterialApi
@Composable
fun EditContent(
    navController: NavController,
    viewModel: RecipeViewModel,
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
    newIngredient: String,
    setNewIngredient: (String) -> Unit,
    instructions: List<String>,
    addInstruction: () -> Unit,
    newInstruction: String,
    setNewInstruction: (String) -> Unit,
    category: String,
    setCategory: (String) -> Unit,
    numberOfPortions: String,
    time: String
) {
    EditTitle(title = title, setTitle = setTitle)
    BasilSpacer()
    EditSource(source = source, onClick = { openSheet(BottomSheetScreens.SOURCE) })
    BasilSpacer()
    EditImage(url = image, setImage = setImage)
    BasilSpacer()
    EditDescription(description = description, setDescription = setDescription)
    BasilSpacer()
    EditIngredients(ingredients = ingredients, addIngredient = addIngredient, newIngredient = newIngredient, setNewIngredient = setNewIngredient)
    BasilSpacer()
    EditInstructions(instructions = instructions, addInstruction = addInstruction, newInstruction = newInstruction, setNewInstruction = setNewInstruction)
    BasilSpacer()
    EditPortions(portions = numberOfPortions, onClick = { openSheet(BottomSheetScreens.PORTIONS) })
    BasilSpacer()
    EditTime(time = time, onClick = { openSheet(BottomSheetScreens.TIME) })
    BasilSpacer()
    EditCategory(selected = category, setSelected = setCategory, onClick = { openSheet(BottomSheetScreens.CATEGORY) })
}



@Composable
fun SubHeader(subheader: String) {
    Text(text = subheader, style = MaterialTheme.typography.h5)
}

@Composable
fun EditButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(text = text)
    }
}

@Composable
fun EditTitle(
    title: String,
    setTitle: (String) -> Unit
) {
    Column {
        SubHeader(subheader = "Titel")
        BasilTextField(value = title, onValueChange = setTitle, placeholder = "Ange titel", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun EditSource(
    source: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = getDomainName(source))
        EditButton(
            text = "ÄNDRA KÄLLA",
            onClick = onClick
        )
    }
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

@Composable
fun EditDescription(
    description: String,
    setDescription: (String) -> Unit
) {
    Column {
        SubHeader(subheader = "Beskrivning")
        BasilTextField(value = description, onValueChange = setDescription, placeholder = "Ange beskriving", modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun EditIngredients(
    ingredients: List<String>,
    addIngredient: () -> Unit,
    newIngredient: String,
    setNewIngredient: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SubHeader(subheader = "Ingredienser")
            EditButton(
                text = "REDIGERA",
                onClick = { /*TODO: Start editing*/ }
            )
        }
        ingredients.forEach { ingredient ->
            Text(text = ingredient, modifier = Modifier.padding(vertical = 6.dp))
        }
        BasilSpacer()
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                BasilTextField(value = newIngredient, onValueChange = setNewIngredient, placeholder = "Lägg till en ingrediens", modifier = Modifier.weight(1f))
                IconButton(onClick = addIngredient) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_add_24_regular), contentDescription = null)
                }
            }

            EditButton(
                text = "+ Rubrik",
                onClick = { /*TODO: Add new header*/ }
            )
        }
    }
}

@Composable
fun EditInstructions(
    instructions: List<String>,
    addInstruction: () -> Unit,
    newInstruction: String,
    setNewInstruction: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SubHeader(subheader = "Instruktioner")
            EditButton(
                text = "REDIGERA",
                onClick = { /*TODO: Start editing*/ }
            )
        }
        instructions.forEachIndexed { index, instruction ->
            Text(text = "Steg ${index+1}", modifier = Modifier.padding(top = 4.dp))
            BasilTextField(value = instruction, onValueChange = { /*TODO: Edit current instructions, something with the index maybe*/ }, modifier = Modifier.fillMaxWidth())
        }
        BasilSpacer()
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                BasilTextField(value = newInstruction, onValueChange = setNewInstruction, placeholder = "Lägg till ett steg", modifier = Modifier.weight(1f))
                IconButton(onClick = addInstruction) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_add_24_regular), contentDescription = null)
                }
            }
            EditButton(
                text = "+ Rubrik",
                onClick = { /*TODO: Add new header*/ }
            )
        }
    }
}

@Composable
fun EditPortions(
    portions: String,
    onClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$portions Portioner")
        EditButton(
            text = "ÄNDRA",
            onClick = onClick
        )
    }
}

@Composable
fun EditTime(
    time: String,
    onClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = time)
        EditButton(
            text = "ÄNDRA",
            onClick = onClick
        )
    }
}

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

@Composable
fun BottomSheetBase(
    title: String,
    subTitle: String,
    buttonText: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        SubHeader(subheader = title)
        BasilSpacer()
        Text(text = subTitle, style = MaterialTheme.typography.body2)
        BasilSpacer()
        content()
        BasilSpacer()
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = buttonText)
        }
    }
}

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




