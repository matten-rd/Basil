package com.example.basil.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.basil.R

private val LektonFontFamily = FontFamily(
    Font(R.font.lekton_bold, FontWeight.Bold)
)
private val MonterratFontFamily = FontFamily(
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_regular)
)

// Set of Material typography styles to start with
val Typography = Typography(
    defaultFontFamily = MonterratFontFamily,
    h1 = TextStyle(
        fontSize = 96.sp,
        fontWeight = FontWeight.SemiBold
    ),
    h2 = TextStyle(
        fontSize = 60.sp,
        fontWeight = FontWeight.SemiBold
    ),
    h3 = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.SemiBold
    ),
    h4 = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.SemiBold
    ),
    h5 = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Light
    ),
    h6 = TextStyle(
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = LektonFontFamily
    ),
    subtitle1 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = LektonFontFamily
    ),
    subtitle2 = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = LektonFontFamily
    ),
    body1 = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Green800
    ),
    caption = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    overline = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)