package com.example.basil.ui.components


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout



@Composable
fun HorizontalStaggeredGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        var height = constraints.maxHeight
        var width = 0

        val placeables = measurables.mapIndexed { index, measurable ->
            val placeable = measurable.measure(constraints)

            height = placeable.height * 2
            width += when(index % 3) {
                0 -> placeable.width + 200
                1 -> -100
                else -> placeable.width + 200
            }

            placeable
        }

        layout(width, height) {
            var xPos = 100
            placeables.forEachIndexed { index, placeable ->
                val yPos = when(index % 3) {
                    0 -> 0
                    1 -> placeable.height
                    else -> placeable.height / 2
                }

                placeable.place(
                    x = xPos,
                    y = yPos
                )

                xPos += when(index % 3) {
                    0 -> -100
                    1 -> placeable.width + 200
                    else -> placeable.width + 200
                }
            }
        }
    }
}

