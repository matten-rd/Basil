package com.example.basil.data.remote.parsing

import android.graphics.BitmapFactory
import android.util.Log
import com.example.basil.data.RecipeData
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Node
import org.jsoup.select.Elements
import org.jsoup.select.NodeVisitor
import java.lang.IllegalStateException
import java.util.stream.Collectors
import android.graphics.Bitmap
import androidx.compose.runtime.produceState
import androidx.core.text.HtmlCompat
import com.example.basil.data.RecipeState
import com.example.basil.util.*
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import java.net.URL
import kotlin.streams.toList
import kotlin.time.ExperimentalTime



fun parseURL(url: String): RecipeData {
    val document = Jsoup.connect(url).get()
    val components = mutableListOf<Component>()

    lateinit var recipeData: RecipeData

    val images = getImages(document)

    /*
    val webClient = WebClient()
    webClient.options.isJavaScriptEnabled = true
    webClient.options.isThrowExceptionOnScriptError = false
    webClient.options.isThrowExceptionOnFailingStatusCode = false
    webClient.waitForBackgroundJavaScript(10000)
    val myPage: HtmlPage = webClient.getPage(url)
    val hel = myPage.titleText
    println(hel)
    val docu = Jsoup.parse(myPage.asXml())
    println(docu.outerHtml())
    webClient.closeAllWindows()
     */

    val title = traverseTitle(document = document)
    val description = traverseDescription(document = document)

    try {
        val jsonLd = extractJsonLdParts(document)
        recipeData = getRecipeFromJsonld(jsonLd = jsonLd, img = images[0], url = url)

    } catch (e: Exception) {
        Log.d("json-ld", e.message.toString())
        try {
            recipeData = getRecipeFromTraversing(document = document, img = images[0])

        } catch (e: Exception) {
            Log.d("scraping", e.message.toString())
            try {
                recipeData = RecipeData(
                    url = url,
                    imageUrl = images[0],
                    recipeImageUrl = "",
                    recipeState = RecipeState.WEBVIEW,
                    title = title,
                    description = description,
                    ingredients = listOf(),
                    instructions = listOf(),
                    cookTime = "PT0M",
                    yield = "4",
                    mealType = "Huvudrätt",
                    isLiked = false
                )

            } catch (e: Exception) {
                Log.d("webview", e.message.toString())
                // Final way out
                recipeData = RecipeData(
                    url = url,
                    imageUrl = "https://picsum.photos/600/600",
                    recipeImageUrl = "",
                    recipeState = RecipeState.WEBVIEW,
                    title = "Ingen titel",
                    description = "",
                    ingredients = listOf(),
                    instructions = listOf(),
                    cookTime = "PT0M",
                    yield = "4",
                    mealType = "Huvudrätt",
                    isLiked = false
                )
            }
        }
    }

    // tasteline has some weird format on their json-ld - need some way to check if the data in json-ld is pretty
    // for ingredients is one idea to check if some of the contains numbers (if not it's ugly)
    // Should implement a way for the user to review the scraped data and if they are not satisfied then rerun it using another method.

    components.add(parseTitle(recipeData.title))
    components.add(parseDescription(recipeData.description))
    components.add(parseIngredients(recipeData.ingredients))

    return recipeData
}



private fun parseTitle(title: String): Title = Title(text = title)

private fun parseDescription(description: String): Description = Description(text = description)

private fun parseIngredients(ingredients: List<String>): Ingredients = Ingredients(list = ingredients)

/**
 * Get the recipe using the JSON-LD blob.
 */

private fun getRecipeFromJsonld(
    jsonLd: JsonObject,
    img: String,
    url: String
): RecipeData {
    val title = getTitle(jsonLd)
    val description = getDescription(jsonLd)
    val ingredients = getIngredients(jsonLd)
    val instructions = getInstructions(jsonLd)
    val recipeYield = extractNumbers( getYield(jsonLd) )
    val time = getTime(jsonLd)

    return  RecipeData(
        url = url,
        imageUrl = img,
        recipeImageUrl = "",
        recipeState = RecipeState.SCRAPED,
        title = title,
        description = description,
        ingredients = ingredients,
        instructions = instructions,
        cookTime = time,
        yield = recipeYield,
        mealType = "Huvudrätt",
        isLiked = false
    )

}

fun extractJsonLdParts(document: Document): JsonObject {
    val elements: Elements = document.select("script[type=\"application/ld+json\"]")
    lateinit var obj: JsonObject

    elements.forEach {
        val jsonElem = JsonParser.parseString(it.data())

        // Get the type of script tag (looking for @type: Recipe)
        val type = when (JSONTokener(it.data()).nextValue()) {
            is JSONObject -> jsonElem.asJsonObject.get("@type").toString()
            is JSONArray -> jsonElem.asJsonArray.last().asJsonObject.get("@type").toString() //TODO: Don't just get the last one
            else -> "Error"
        }

        // if type is Recipe then get the JsonObject it contains
        if (type.contains("Recipe")) {
            obj = when(JSONTokener(it.data()).nextValue()) {
                is JSONObject -> jsonElem.asJsonObject
                is JSONArray -> jsonElem.asJsonArray.last().asJsonObject //TODO: Don't just get the last one
                else -> jsonElem.asJsonObject
            }
        }
    }

    return obj
}

private fun getImages(document: Document): List<String> {
    val media = document.select("[src]").distinctBy { it.attr("abs:src").toString() } // remove duplicates
    val mediaLinks = mutableListOf<String>()
    for (src in media) {
        val isValidImage = src.normalName().equals("img") && isUrlImage(src.attr("abs:src").toString())
        if (isValidImage) {
            println(" * ${src.tagName()}: <${src.attr("abs:src")}>")
            mediaLinks.add(src.attr("abs:src").toString())
        }
    }
    if (mediaLinks.isNotEmpty()) {
        val largeMedia = mediaLinks.filter {
            val url = URL(it)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            bmp.width > 100 && bmp.height > 100
        }
        if (largeMedia.isNotEmpty())
            return largeMedia // #1 return large images
        else
            return mediaLinks.toList() // #2 return smaller images
    } else {
        return listOf("https://picsum.photos/600/600") // #3 return random image
    }
}

private fun getTitle(obj: JsonObject): String {
    return removeQuotes(obj.get("name").toString())
}

private fun getDescription(obj: JsonObject): String {
    return removeQuotes(obj.get("description").toString())
}

private fun getIngredients(obj: JsonObject): List<String> {
    return obj.get("recipeIngredient").asJsonArray.map {
        removeQuotes(HtmlCompat
            .fromHtml(it.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
        )
    }.distinct()
}

private fun getYield(obj: JsonObject): String {
    return removeQuotes(obj.get("recipeYield").toString())
}

private fun getTime(obj: JsonObject): String {
    return removeQuotes(obj.get("totalTime").toString())
}

private fun getInstructions(obj: JsonObject): List<String> {
    val ins: MutableList<String> = mutableListOf()

    val instructionsNode = obj.get("recipeInstructions")
    if (instructionsNode.isJsonArray) {
        val instructions = instructionsNode.asJsonArray

        if (instructions.first().isJsonObject) {
            val instructionsObj = instructions.first().asJsonObject
            if (instructionsObj.get("@type").toString().contains("HowToStep", ignoreCase = true)) {
                instructions.forEach {
                    ins.add(it.asJsonObject.get("text").toString())
                }
            } else if (instructionsObj.get("@type").toString()
                    .contains("HowToSection", ignoreCase = true)
            ) {
                instructionsObj.get("itemListElement").asJsonArray.forEach {
                    ins.add(it.asJsonObject.get("text").toString())
                }
            }
        } else if (instructions.first().isJsonPrimitive) {
            instructions.forEach {
                ins.add(it.toString())
            }
        }

    } else if (instructionsNode.isJsonObject) {
        val instructionsObj = instructionsNode.asJsonObject
        instructionsObj.get("itemListElement").asJsonArray.forEach {
            ins.add(it.toString())
        }
    } else {
        ins.add("Något gick snett :(")
    }


    /**
     * This will be difficult since all websites seem to have different ways of displaying this.
     *
     * - In most cases search for the format:
     * {
     *   "@type":"HowToStep",
     *   "text":""Instruction"",
     *   "some extra properties": ...
     * }
     * - Sometimes it will just be a list of instruction strings in which case it can be parsed the same way as the ingredients.
     * - One idea is to iterate over "recipeInstructions" and check the length/number of words and if it is > someAmount then it most likely is an instruction.
     */

    // clean the instructions from possible html tags and quotes before returning
    return ins.map {
        removeQuotes(
            HtmlCompat
                .fromHtml(Jsoup.parse(it).text().replace("""\n""", "\n"), HtmlCompat.FROM_HTML_MODE_LEGACY)
                .toString()
        )
    }.distinct()
}


/**
 * Get the recipe by traversing the HTML.
 */

private fun getRecipeFromTraversing(
    document: Document,
    img: String
): RecipeData {
    val title = traverseTitle(document)
    val description = traverseDescription(document)
    val (ingredients, instructions) = traverseIngredientsAndInstructions(document)

    return  RecipeData(
        url = "url",
        imageUrl = img,
        recipeImageUrl = "",
        recipeState = RecipeState.SCRAPED,
        title = title,
        description = description,
        ingredients = ingredients,
        instructions = instructions,
        cookTime = "PTOM",
        yield = "4",
        mealType = "Huvudrätt",
        isLiked = false
    )
}

private fun traverseTitle(document: Document): String {
    val title = document.select("meta[property='og:title'], meta[name='og:title']").attr("content").toString()
    return if (title.isNotEmpty()) title else document.title().toString()
}

private fun traverseDescription(document: Document): String {
    return document.select("meta[property='og:description'], meta[name='og:description']").attr("content").toString()
}

private fun traverseIngredientsAndInstructions(document: Document): Pair<List<String>, List<String>> {
    /**
     * Idea:
     * If I find one node that is an ingredient then I will assume all of its siblings are as well.
     * How to:
     * Find the two nodes with the highest ingredient scores and calculate their LCA and then this
     * nodes (LCA node) children will be all ingredients.
     * Then do it in the same way with instructions.
     */
    val ingredientMap = mutableMapOf<Node, Double>()
    val instructionMap = mutableMapOf<Node, Double>()

    document.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {

            val nodeIngredientScore: Pair<Boolean, Double> = ScoreIngredient().isIngredient(node)
            if (nodeIngredientScore.first) {
                ingredientMap.put(node, nodeIngredientScore.second)
            }

            val nodeInstructionScore: Pair<Boolean, Double> = ScoreInstruction().isInstruction(node)
            if (nodeInstructionScore.first) {
                instructionMap.put(node, nodeInstructionScore.second)
            }
        }
        override fun tail(node: Node?, depth: Int) {
            // hel
        }
    })

    val (ingredientNode1, ingredientNode2) = findTwoUniqueEntries(ingredientMap)
    val ingredientLcaNode = lowestCommonAncestor(ingredientNode1, ingredientNode2)
    val ingredients = listFromNode(ingredientLcaNode)

    val (instructionNode1, instructionNode2) = findTwoUniqueEntries(instructionMap)
    val instructionLcaNode = lowestCommonAncestor(instructionNode1, instructionNode2)
    val instructions = listFromNode(instructionLcaNode)

    return Pair(ingredients, instructions)
}

private fun listFromNode(node: Node): List<String> {
    /**
     * Return a Nodes childnodes content as a list.
     */
    return node.childNodes()
        .map { Jsoup.parse(it.outerHtml()).body().text() }
        .filter { it.isNotEmpty() }
}

private fun findTwoUniqueEntries(map: MutableMap<Node, Double>): Pair<Node, Node> {
    /**
     * Returns the two highest scoring and unique nodes (based on their content).
     */
    val uniqueKeys = map.keys.distinctBy { Jsoup.parse(it.outerHtml()).body().text() }
    val mapWithUniqueKeys = map.filterKeys { it in uniqueKeys }
    val (_, secondHighestScore) = findTwoMaxNumbers(mapWithUniqueKeys.map { it.value })
    val filteredMap = mapWithUniqueKeys
        .filterValues { it >= secondHighestScore }.entries.stream().toList().take(2)

    return Pair(filteredMap[0].key, filteredMap[1].key)
}

private fun findTwoMaxNumbers(listOfDouble: List<Double>): Pair<Double, Double> {
    /**
     * Returns the two largest Doubles from a list of Doubles.
     */
    var maxOne: Double = 0.0
    var maxTwo: Double = 0.0
    for (n in listOfDouble) {
        if (maxOne < n) {
            maxTwo = maxOne
            maxOne = n
        } else if (maxTwo < n) {
            maxTwo = n
        }
    }
    return Pair(maxOne, maxTwo)
}

private fun lowestCommonAncestor(node1: Node?, node2: Node?): Node {
    /**
     * Returns the lowest common ancestor of node1 and node2
     */
    var ancestor: Node? = node1
    while (ancestor != null) {
        if (isAncestor(ancestor, node2)) {
            return ancestor
        }
        ancestor = ancestor.parent()
    }
    throw IllegalStateException("node1 and node2 do not have common ancestor")
}

private fun isAncestor(node1: Node?, node2: Node?): Boolean {
    /**
     * Returns true if node1 is ancestor of node2 or node1 == node2
     */
    if (node1 === node2) {
        return true
    }
    var ancestor: Node? = node2

    while (ancestor != null) {
        if (ancestor === node1) {
            return true
        }
        ancestor = ancestor.parent()
    }

    return false
}




