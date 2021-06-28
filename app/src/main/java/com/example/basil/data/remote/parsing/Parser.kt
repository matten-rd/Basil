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
import androidx.core.text.HtmlCompat
import com.example.basil.data.RecipeState
import com.example.basil.util.*
import java.net.URL
import kotlin.streams.toList


fun parseURL(url: String): RecipeData {
    val document = Jsoup.connect(url).get()

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
    val jsonBlob = try {
        extractJsonLdParts(document)
    } catch (e: Exception) {
        JsonObject()
    }

    return getRecipe(url, jsonBlob, document, images[0])


    // tasteline has some weird format on their json-ld - need some way to check if the data in json-ld is pretty
    // for ingredients is one idea to check if some of the contains numbers (if not it's ugly)
    // Should implement a way for the user to review the scraped data and if they are not satisfied then rerun it using another method.

}

private fun getRecipe(
    url: String,
    jsonLd: JsonObject,
    document: Document,
    img: String
): RecipeData {
    val title = getTitle(jsonLd, document)
    val description = getDescription(jsonLd, document)
    val ingredients = getIngredients(jsonLd, document)
    val instructions = getInstructions(jsonLd, document, ingredients)
    val time = getTime(jsonLd)
    val recipeYield = getYield(jsonLd)

    val recipeState = if (ingredients.isEmpty() || instructions.isEmpty()) {
        RecipeState.WEBVIEW
    } else {
        RecipeState.SCRAPED
    }

    return RecipeData(
        url = url,
        imageUrl = img,
        recipeImageUrl = "",
        recipeState = recipeState,
        title = title,
        description = description,
        ingredients = ingredients.minus(instructions),
        instructions = instructions.minus(ingredients),
        cookTime = time,
        yield = recipeYield,
        mealType = "Huvudrätt",
        isLiked = false
    )
}

private fun getTitle(
    jsonLd: JsonObject,
    document: Document
): String {
    return try {
        jsonTitle(jsonLd)
    } catch (e: Exception) {
        Log.d("Title: jsonld", e.message.toString())

        try {
            traverseTitle(document)
        } catch (e: Exception) {
            Log.d("Title: traverse", e.message.toString())
            ""
        }
    }
}

private fun getDescription(
    jsonLd: JsonObject,
    document: Document
): String {
    return try {
        jsonDescription(jsonLd)
    } catch (e: Exception) {
        Log.d("Desctiption: jsonld", e.message.toString())

        try {
            traverseDescription(document)
        } catch (e: Exception) {
            Log.d("Description: traverse", e.message.toString())
            ""
        }
    }
}

private fun getIngredients(
    jsonLd: JsonObject,
    document: Document
): List<String> {
    return try {
        jsonIngredients(jsonLd)
    } catch (e: Exception) {
        Log.d("Ingredients: jsonld", e.message.toString())

        try {
            traverseIngredients(document)
        } catch (e: Exception) {
            Log.d("Ingredients: traverse", e.message.toString())
            listOf<String>()
        }
    }
}

private fun getInstructions(
    jsonLd: JsonObject,
    document: Document,
    ingredients: List<String>
): List<String> {
    return try {
        jsonInstructions(jsonLd)
    } catch (e: Exception) {
        Log.d("Instructions: jsonld", e.message.toString())

        try {
            traverseInstructions(document, ingredients)
        } catch (e: Exception) {
            Log.d("Instructions: traverse", e.message.toString())
            listOf<String>()
        }
    }
}

private fun getTime(jsonLd: JsonObject): String {
    return try {
        jsonTime(jsonLd)
    } catch (e: Exception) {
        Log.d("Time", e.message.toString())
        "PT0M"
    }
}

private fun getYield(jsonLd: JsonObject): String {
    return try {
        extractNumbers( jsonYield(jsonLd) )
    } catch (e: Exception) {
        Log.d("Yield", e.message.toString())
        "4"
    }
}

fun extractJsonLdParts(document: Document): JsonObject {
    val elements: Elements = document.select("script[type=\"application/ld+json\"]")
    lateinit var obj: JsonObject

    elements.forEach {
        val jsonElem = JsonParser.parseString(it.data())

        // Get the type of script tag (looking for @type: Recipe)
        val type = try {
            when (JSONTokener(it.data()).nextValue()) {
                is JSONObject -> jsonElem.asJsonObject.get("@type").toString()
                is JSONArray -> jsonElem.asJsonArray.last().asJsonObject.get("@type").toString() //TODO: Don't just get the last one
                else -> "Something weird"
            }
        } catch (e: Exception) {
            "Error"
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
    // First try to get the correct image from the metadata.
    val ogImage = document.select("meta[property='og:image'], meta[name='og:image']")
        .attr("content").toString()
    if (ogImage.isNotEmpty()) return listOf(ogImage)
    // Then just get the largest image. // TODO: Implement image selector.
    // TODO: Make this part more efficient.
    val media = document.select("img[src~=(?i)\\.(png|jpe?g)]") // get .png .jpg .jpeg
        .distinctBy { it.attr("abs:src").toString() } // remove duplicates
    val mediaLinks = mutableListOf<String>()
    for (src in media) {
        val isValidImage = isUrlImage(src.attr("abs:src").toString())
        if (isValidImage) {
            println(" * ${src.tagName()}: <${src.attr("abs:src")}>")
            mediaLinks.add(src.attr("abs:src").toString())
        }
    }
    if (mediaLinks.isNotEmpty()) {
        val largeMedia = mediaLinks.filter {
            val url = URL(it)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            println("${bmp.width} X ${bmp.height} <${url.path.replaceAfterLast("/", "")}>")
            bmp.width > 200 && bmp.height > 200
        }.sortedBy {
            val url = URL(it)
            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            bmp.width
        }
        if (largeMedia.isNotEmpty())
            return largeMedia // #1 return large images
        else
            return mediaLinks.toList() // #2 return smaller images
    } else {
        return listOf("https://picsum.photos/600/600") // #3 return random image
    }
}

private fun jsonTitle(obj: JsonObject): String {
    return removeQuotes(obj.get("name").toString())
}

private fun jsonDescription(obj: JsonObject): String {
    return removeQuotes(obj.get("description").toString())
}

private fun jsonIngredients(obj: JsonObject): List<String> {
    return obj.get("recipeIngredient").asJsonArray.map {
        removeQuotes(HtmlCompat
            .fromHtml(it.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
        )
    }.distinct()
}

private fun jsonYield(obj: JsonObject): String {
    return removeQuotes(obj.get("recipeYield").toString())
}

private fun jsonTime(obj: JsonObject): String {
    return removeQuotes(obj.get("totalTime").toString())
}

private fun jsonInstructions(obj: JsonObject): List<String> {
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
            if (it.isJsonObject) {
                val instructionObj = it.asJsonObject
                val instruction = instructionObj.get("itemListElement").asJsonObject.get("text").toString()
                ins.add(instruction)
            } else {
                ins.add(it.toString())
            }
        }

    } else {
        ins.add("Något gick snett :(")
    }

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
 * Get the recipe from Microdata (itemprop).
 */
private fun getRecipeFromMicrodata(
    document: Document,
    img: String,
    url: String
): RecipeData {
    val title = traverseTitle(document)
    val description = traverseDescription(document)
    val (ingredients, instructions) = microdataIngredientsAndInstructions(document)

    return  RecipeData(
        url = url,
        imageUrl = img,
        recipeImageUrl = "",
        recipeState = RecipeState.SCRAPED,
        title = title,
        description = description,
        ingredients = ingredients,
        instructions = instructions,
        cookTime = "PT0M",
        yield = "4",
        mealType = "Huvudrätt",
        isLiked = false
    )
}

private fun microdataIngredientsAndInstructions(document: Document): Pair<List<String>, List<String>> {
    val ingredientElements = document.select("[itemprop*='recipeIngredient'")
    val instructionElements = document.select("[itemprop*='recipeInstruction'")

    val ingredients = ingredientElements.map {
        Jsoup.parse(it.outerHtml()).body().text()
    }.filter { it.isNotEmpty() }.distinct()

    val instructions = instructionElements.map {
        Jsoup.parse(it.outerHtml()).body().text()
    }.filter { it.isNotEmpty() }.distinct()

    return Pair(ingredients, instructions)
}


private fun traverseTitle(document: Document): String {
    val title = document.select("meta[property='og:title'], meta[name='og:title']").attr("content").toString()
    return if (title.isNotEmpty()) title else document.title().toString()
}

private fun traverseDescription(document: Document): String {
    return document.select("meta[property='og:description'], meta[name='og:description']").attr("content").toString()
}

private fun traverseIngredients(document: Document): List<String> {
    val ingredientMap = mutableMapOf<Node, Double>()

    document.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            val nodeScore: Pair<Boolean, Double> = ScoreIngredient().isIngredient(node)
            if (nodeScore.first) {
                ingredientMap.put(node, nodeScore.second)
            }
        }

        override fun tail(node: Node?, depth: Int) {
            // Leave empty
        }
    })
    val (ingredientNode1, ingredientNode2) = findTwoUniqueEntries(ingredientMap)
    val ingredientLcaNode = lowestCommonAncestor(ingredientNode1, ingredientNode2)

    return listFromNode(ingredientLcaNode)
}

private fun traverseInstructions(document: Document, ingredients: List<String> = listOf()): List<String> {
    val instructionMap = mutableMapOf<Node, Double>()

    document.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            val nodeScore: Pair<Boolean, Double> = ScoreInstruction().isInstruction(node)
            if (nodeScore.first) {
                instructionMap.put(node, nodeScore.second)
            }
        }

        override fun tail(node: Node?, depth: Int) {
            // Leave empty
        }
    })
    val (instructionNode1, instructionNode2) = findTwoUniqueEntries(instructionMap)
    val instructionLcaNode = lowestCommonAncestor(instructionNode1, instructionNode2)

    return lisFromNode(instructionLcaNode, ingredients)
}

private fun checkNotListElement(node: Node): Node {
    /**
     * Checks that the Lca node is not part of a list - because then we want the whole list.
     * If it is just part of a list then we recurse call this function with the parent node.
     */
    val tag = node.nodeName()
    val illegalNodeNames = listOf<String>("li", "tr", "td", "p", "span")
    return if (tag in illegalNodeNames) {
        checkNotListElement(node.parent())
    } else {
        node
    }
}

private fun listFromNode(node: Node): List<String> {
    /**
     * Return a Nodes childnodes content as a list.
     * Different method compared to [lisFromNode].
     * This one is suitable for ingredients.
     */
    return node.childNodes()
        .map { Jsoup.parse(it.outerHtml()).body().text() }
        .filter { it.isNotEmpty() }
}


private fun lisFromNode(inputNode: Node, ingredientList: List<String> = listOf()): List<String> {
    /**
     * Returns a Nodes childnodes content as a list.
     * Different method compared to [listFromNode].
     * This one is suitable for instructions.
     */
    val list = mutableListOf<String>()
    inputNode.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            val doc = Jsoup.parse(node.outerHtml())
            val text = doc.body().text().trim()
            if (
                text.isNotEmpty() &&
                text.split(" ").size > 2 &&
                text !in list &&
                !isPartOfIngredients(text, ingredientList)
            )
                list.add(text)
        }

        override fun tail(node: Node, depth: Int) {
            // leave empty
        }
    })
    println("List from node: ${list.distinct()}")
    return list.distinct()
}

private fun isPartOfIngredients(instruction: String, ingredients: List<String>): Boolean {
    /**
     * Returns if an instruction contains a part match from the ingredient list.
     */
    ingredients.forEach { ingredient ->
        if (instruction in ingredient || ingredient in instruction) {
            return true
        }
    }
    return false
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
            return checkNotListElement(ancestor)
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




