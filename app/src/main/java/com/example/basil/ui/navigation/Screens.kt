package com.example.basil.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail")
    object Create : Screen("create")
    object CreateImage : Screen("createImage")
    object Edit : Screen("edit")
}
