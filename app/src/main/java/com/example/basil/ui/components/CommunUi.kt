package com.example.basil.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.basil.R

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
@Composable
fun BasilTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    textStyle: TextStyle = LocalTextStyle.current.copy(color = MaterialTheme.colors.primary),
    cursorColor: Brush = SolidColor(MaterialTheme.colors.primary),
    color: Color = MaterialTheme.colors.primary,
    trailingIcon: @Composable () -> Unit = {}
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(minHeight = 42.dp),
        cursorBrush = cursorColor,
        textStyle = textStyle
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

