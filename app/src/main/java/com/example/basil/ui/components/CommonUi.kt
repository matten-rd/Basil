package com.example.basil.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.basil.R
import com.example.basil.ui.create.ContentEdit
import com.google.accompanist.insets.navigationBarsWithImePadding

/**
 * Standard spacing that is used for most of the Basil app.
 */
@Composable
fun BasilSpacer() {
    Spacer(modifier = Modifier.height(12.dp))
}

/**
 * Error screen to be displayed when something goes wrong.
 */
@Composable
fun ErrorScreen(
    errorMessage: String,
    errorIcon: @Composable () -> Unit = {
        Icon(painter = painterResource(id = R.drawable.ic_fluent_error_circle_20_regular), contentDescription = null, modifier = Modifier.size(96.dp))
    }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        errorIcon()
        Text(text = errorMessage, style = MaterialTheme.typography.h5, textAlign = TextAlign.Center)
    }
}

/**
 * A text field without text input and only reacts to onClicks.
 * Used to display and select date.
 */
@ExperimentalComposeUiApi
@Composable
fun ReadonlyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    trailingIcon: @Composable () -> Unit = {}
) {
    Box {
        BasilTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            trailingIcon = trailingIcon
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .clickable(onClick = onClick),
        )
    }
}

/**
 * The TextField used for the Basil app.
 */
@ExperimentalComposeUiApi
@Composable
fun BasilTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    textStyle: TextStyle = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary),
    cursorColor: Brush = SolidColor(MaterialTheme.colors.primary),
    color: Color = MaterialTheme.colors.primary,
    imeAction: ImeAction = ImeAction.Done,
    trailingIcon: @Composable () -> Unit = {}
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(minHeight = 42.dp).navigationBarsWithImePadding(),
        cursorBrush = cursorColor,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() },
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    ) { innerTextField ->
        Row(
            modifier = Modifier
                .border(1.dp, color, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle.copy(color = color.copy(0.7f)))
                }
                SelectionContainer {
                    innerTextField()
                }
            }

            trailingIcon()
        }
    }
}

/**
 * A custom toggle button with text content.
 */
@Composable
fun TextToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        text = text,
        modifier = modifier
            .toggleable(value = checked, onValueChange = onCheckedChange)
            .padding(ButtonDefaults.TextButtonContentPadding),
        style = MaterialTheme.typography.button
    )
}

/**
 * Displaying a list with your choice of composable.
 */
@Composable
fun DisplayListOfString(list: List<String>, content: @Composable (Int, String) -> Unit) {
    Column {
        list.forEachIndexed { idx, string ->
            content(idx, string)
        }
    }
}

/**
 * A list with your choice of composable that is fully editable.
 * Supports adding and deleting (with snackbar undo). 
 * (Possible to support editing current items).
 */
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun EditableList(
    subHeader: String,
    placeholder: String,
    list: List<String>,
    onValueChange: (String, String) -> Unit,
    addToList: () -> Unit,
    deleteFromList: (Int) -> Unit,
    newValue: String,
    setNewValue: (String) -> Unit,
    onShowSnackbar: (Int, String) -> Unit,
    listHeaders: @Composable (Int) -> Unit = {},
    listItems: @Composable (String) -> Unit
) {
    var checked by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            SubHeader(subheader = subHeader)
            TextToggleButton(
                checked = checked,
                onCheckedChange = { checked = it },
                text = if (checked) "KLAR" else "REDIGERA"
            )
        }
        AnimatedContent(targetState = checked) { isEdit ->
            if (isEdit)
                ContentEdit(
                    list = list,
                    delete = deleteFromList,
                    onValueChange = onValueChange,
                    onShowSnackbar = onShowSnackbar,
                    header = { listHeaders(it) }
                )
            else
                DisplayListOfString(list = list) { idx, item ->
                    listHeaders(idx)
                    listItems(item)
                }
        }

        BasilSpacer()
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                BasilTextField(value = newValue, onValueChange = setNewValue, placeholder = placeholder, modifier = Modifier.weight(1f))
                IconButton(onClick = addToList) {
                    Icon(painter = painterResource(id = R.drawable.ic_fluent_add_24_regular), contentDescription = null)
                }
            }
        }
    }
}

/**
 * Just a Text component styled like a subheader.
 */
@Composable
fun SubHeader(subheader: String, modifier: Modifier = Modifier) {
    Text(text = subheader, style = MaterialTheme.typography.h5, modifier = modifier)
}

/**
 * Simple component displaying some Text and a TextButton in a row filling max width with space between.
 */
@Composable
fun TextAndButton(
    text: String,
    buttonText: String = "ÄNDRA",
    onClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = text)
        TextButton(onClick = onClick) {
            Text(text = buttonText)
        }
    }
}

/**
 * A fully function TextField with a header placed above it.
 */
@ExperimentalComposeUiApi
@Composable
fun TextFieldWithHeader(
    modifier: Modifier = Modifier,
    header: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Column {
        SubHeader(subheader = header)
        BasilTextField(value = value, onValueChange = onValueChange, placeholder = placeholder, modifier = modifier.fillMaxWidth())
    }
}



