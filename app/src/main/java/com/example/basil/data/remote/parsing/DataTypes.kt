package com.example.basil.data.remote.parsing


sealed class Component

data class Title(val text: String) : Component()

data class Description(val text: String) : Component()

data class Ingredients(val list: List<String>) : Component()

data class Recipe(
    val url: String,
    val components: List<Component>
)