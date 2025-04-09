package com.github.jershell.rjpath

import kotlinx.serialization.json.JsonElement

/**
 * Path-value pair for getAllWithPath result
 */
data class PathValuePair(
    val path: String,
    val value: JsonElement
)

/**
 * JSONPath implementation according to RFC9535
 */
class RJPath private constructor(
    private val selector: Selector,
    private val options: RJPathOptions = RJPathOptions.Default
) {
    companion object {
        /**
         * Creates JSONPath selector according to RFC9535
         */
        fun selector(path: String, options: RJPathOptions = RJPathOptions.Default): RJPath {
            val selector = SelectorParser(FunctionRegistry(options)).parse(path)
            return RJPath(selector, options)
        }
    }

    /**
     * Finds the first element matching the selector
     */
    fun getFirst(element: JsonElement): JsonElement {
        return getAll(element).firstOrNull() 
            ?: throw NoSuchElementException("No element found for path")
    }

    /**
     * Finds the first element matching the selector, or null
     */
    fun getFirstOrNull(element: JsonElement): JsonElement? {
        return getAll(element).firstOrNull()
    }

    /**
     * Finds all elements matching the selector
     */
    fun getAll(element: JsonElement): List<JsonElement> {
        val rootNode = Node(element, Location.Root)
        return selector.select(rootNode).map { it.value }
    }

    /**
     * Finds all elements matching the selector and returns a list of elements with paths
     */
    fun getAllWithPath(element: JsonElement): List<PathValuePair> {
        val rootNode = Node(element, Location.Root)
        return selector.select(rootNode).map { node ->
            PathValuePair(
                path = node.location.toFullPath(),
                value = node.value
            )
        }
    }
}