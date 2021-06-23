package com.example.basil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * The base for how a Basil bottom sheet looks.
 */
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