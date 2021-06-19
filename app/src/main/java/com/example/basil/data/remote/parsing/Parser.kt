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
import com.example.basil.data.RecipeState
import com.example.basil.util.*
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import java.net.URL
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
        // Final way out - should later be moved to next try statement
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
    }

    try {

    } catch (e: Exception) {
        Log.d("scraping", e.message.toString())
        // Here place the final way out in the form of a webView recipe
    }

    //traverseDocument(document)
    //recTraverse(document)

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
    val media = document.select("[src]")
    val mediaLinks = mutableListOf<String>()
    for (src in media) {
        val isValidImage = src.normalName().equals("img") && !src.attr("abs:src").toString().endsWith("svg") && src.attr("abs:src").toString().isNotEmpty()
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
    return obj.get("recipeIngredient").asJsonArray.map { removeQuotes(it.toString()) }
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
            Jsoup.parse(it).text()
                .replace("""\n""", "")
                .replace("&auml;", "ä")
                .replace("&ouml;", "ö")
                .replace("&aring;", "å")
        )
    }
}


/**
 * Get the recipe by traversing the HTML.
 */

private fun traverseTitle(document: Document): String {
    val title = document.select("meta[property='og:title'], meta[name='og:title']").attr("content").toString()
    return if (title.isNotEmpty()) title else document.title().toString()
}

private fun traverseDescription(document: Document): String {
    return document.select("meta[property='og:description'], meta[name='og:description']").attr("content").toString()
}

private fun recTraverse(document: Document) {
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
    // FIXME: One issue is that if one node is identified as an ingredient then its sibling will
    //  most likely identify as an ingredient as well so I end up with "duplicate" lists of ingredients.
    document.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {


            val nodeIngredientScore: Pair<Boolean, Double> = ScoreIngredient().isIngredient(node)
            if (nodeIngredientScore.first) {
                ingredientMap.put(node, nodeIngredientScore.second)

                val siblings = node.parent().childNodes() // just node.siblingNodes() does NOT contain current node
                val ingredients = siblings.stream()
                    .map { Jsoup.parse(it.outerHtml()).body().text() }
                    .filter { it.isNotEmpty() }
                    .collect(Collectors.toList())
                if (ingredients.isNotEmpty()) {
                    //println(siblings)
                    println(ingredients)
                }
            }

            val nodeInstructionScore: Pair<Boolean, Double> = ScoreInstruction().isInstruction(node)
            if (nodeInstructionScore.first) {
                instructionMap.put(node, nodeInstructionScore.second)
                val parent = node.parent()
                val siblings = if (parent != null) parent.childNodes() else node.siblingNodes()

                val instructions = siblings.stream()
                    .map { Jsoup.parse(it.outerHtml()).body().text() }
                    .filter { it.isNotEmpty() }
                    .collect(Collectors.toList())
                if (instructions.isNotEmpty()) {
                    println(instructions)
                }
            }
        }
        override fun tail(node: Node?, depth: Int) {
            // hel
        }
    })

    // FIXME: Finding the LCA is definitely unnecessary!
    val bestIngredientNode = ingredientMap.maxByOrNull { it.value }?.key
    val bestInstructionNode = instructionMap.maxByOrNull { it.value }?.key
    println("HELLO")
    val lcaNode = lowestCommonAncestor(bestIngredientNode, bestInstructionNode)
    val res = checkFor(lcaNode)
    println("RESULTS")
    println(res.first)
    println(res.second)
    //traverseLca(lcaNode)


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


private fun checkFor(nodeToCheck: Node): Pair<List<String>, List<String>> {
    var ingredientCheck = false
    var noMatchYet = true

    val ingredients = mutableListOf<String>()
    val potentialIngredients = mutableListOf<String>()

    val instructions = mutableListOf<String>()
    val potentialInstructions = mutableListOf<String>()
    // TODO: Only check the lowest nodes. They should not have any children.

    nodeToCheck.traverse(object : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            val nodeIngredientScore: Pair<Boolean, Double> = ScoreIngredient().isIngredient(node)
            val nodeInstructionScore: Pair<Boolean, Double> = ScoreInstruction().isInstruction(node)
            if (nodeIngredientScore.first && noMatchYet) {
                ingredientCheck = true
                noMatchYet = false
            }

            if (nodeIngredientScore.first && ingredientCheck) {
                val ingredient = Jsoup.parse(node.outerHtml()).body().text().trim()
                ingredients.addAll(potentialIngredients)
                if (ingredient.isNotEmpty()) ingredients.add(ingredient)
                potentialIngredients.clear()

            } else if (nodeInstructionScore.first && !noMatchYet) {
                ingredientCheck = false
                val instruction = Jsoup.parse(node.outerHtml()).body().text().trim()
                instructions.addAll(potentialInstructions)
                if (instruction.isNotEmpty()) instructions.add(instruction)
                potentialInstructions.clear()

            } else if (!noMatchYet) {
                if (ingredientCheck) {
                    val potentialIngredient = Jsoup.parse(node.outerHtml()).body().text().trim()
                    if (potentialIngredient.isNotEmpty()) potentialIngredients.add(
                        potentialIngredient
                    )
                } else {
                    val potentialInstruction =
                        Jsoup.parse(node.outerHtml()).body().text().trim()
                    if (potentialInstruction.isNotEmpty()) potentialInstructions.add(
                        potentialInstruction
                    )
                }
            }
        }

        override fun tail(node: Node, depth: Int) {

        }
    })
    return Pair(ingredients, instructions)
}

// TODO: Check for duplicates in the ingredients and instructions
// TODO: It might be fundamentally flawed to use nextSibling() as the children do not get checked (?)
private fun ingredientCheck(node: Node): Pair<List<String>, List<String>> {
    val ingredients = mutableListOf<String>()
    val potentialIngredients = mutableListOf<String>()

    val nodeIngredientScore: Pair<Boolean, Double> = ScoreIngredient().isIngredient(node)
    val nodeInstructionScore: Pair<Boolean, Double> = ScoreInstruction().isInstruction(node)

    if (nodeIngredientScore.first) {
        val ingredient = Jsoup.parse(node.outerHtml()).body().text().trim()
        ingredients.addAll(potentialIngredients)
        if (ingredient.isNotEmpty()) ingredients.add(ingredient)
        potentialIngredients.clear()

        ingredientCheck(node.nextSibling())
    } else if (nodeInstructionScore.first) {
        // move on and pass the ingredients to instructionCheck()
        return instructionCheck(node, ingredients)
    } else {
        val potentialIngredient = Jsoup.parse(node.outerHtml()).body().text().trim()
        if (potentialIngredient.isNotEmpty()) potentialIngredients.add(potentialIngredient)

        ingredientCheck(node.nextSibling())
    }
    // Should never be reached
    return Pair(ingredients, emptyList())
}

private fun instructionCheck(node: Node, ingredients: List<String>): Pair<List<String>, List<String>> {
    val instructions = mutableListOf<String>()
    val potentialInstructions = mutableListOf<String>()

    val nodeInstructionScore: Pair<Boolean, Double> = ScoreInstruction().isInstruction(node)

    if (nodeInstructionScore.first) {
        val instruction = Jsoup.parse(node.outerHtml()).body().text().trim()
        instructions.addAll(potentialInstructions)
        if (instruction.isNotEmpty()) instructions.add(instruction)
        potentialInstructions.clear()

        if (node.nextSibling() == null) {
            return Pair(ingredients, instructions)
        }
        instructionCheck(node.nextSibling(), ingredients)
    } else {
        val potentialInstruction = Jsoup.parse(node.outerHtml()).body().text().trim()
        if (potentialInstruction.isNotEmpty()) potentialInstructions.add(potentialInstruction)
        if (node.nextSibling() == null) {
            return Pair(ingredients, instructions)
        }
        instructionCheck(node.nextSibling(), ingredients)
    }
    return Pair(ingredients, instructions)
}





