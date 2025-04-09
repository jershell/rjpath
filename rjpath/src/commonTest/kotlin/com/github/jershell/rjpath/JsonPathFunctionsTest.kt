package com.github.jershell.rjpath

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonPathFunctionsTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val testJson = """
        {
            "store": {
                "book": [
                    {
                        "category": "reference",
                        "author": "Nigel Rees",
                        "title": "Sayings of the Century",
                        "price": 8.95
                    },
                    {
                        "category": "fiction",
                        "author": "Evelyn Waugh",
                        "title": "Sword of Honour",
                        "price": 12.99
                    },
                    {
                        "category": "fiction",
                        "author": "Herman Melville",
                        "title": "Moby Dick",
                        "isbn": "0-553-21311-3",
                        "price": 8.99
                    },
                    {
                        "category": "fiction",
                        "author": "J. R. R. Tolkien",
                        "title": "The Lord of the Rings",
                        "isbn": "0-395-19395-8",
                        "price": 22.99
                    }
                ],
                "bicycle": {
                    "color": "red",
                    "price": 19.95
                }
            }
        }
    """.trimIndent()

    private val element: JsonElement = json.parseToJsonElement(testJson)

    @Test
    fun testLengthFunction() {
        val path = RJPath.Companion.selector("$.store.book[?(@.price > length(@.title))]")
        val books = path.getAll(element)
        assertEquals(1, books.size)
    }

    @Test
    fun testCountFunction() {
        val path = RJPath.Companion.selector("$.store.book[?(@.price > count(@.title))]")
        val books = path.getAll(element)
        assertEquals(4, books.size)
    }

    @Test
    fun testMatchFunction() {
        val path = RJPath.Companion.selector("$.store.book[?match(@.title, '^[A-Z].*')]")
        val books = path.getAll(element)
        assertEquals(4, books.size)
    }

    @Test
    fun testSearchFunction() {
        val path = RJPath.Companion.selector("$.store.book[?search(@.title,'Dick')]")
        val books = path.getAll(element)
        assertEquals(1, books.size)
    }

    @Test
    fun testValueFunction() {
        val path = RJPath.Companion.selector("$.store.book[?(@.price == value(@.price))]")
        val books = path.getAll(element)
        assertEquals(4, books.size)
    }

    @Test
    fun testComplexFilter() {
        val path = RJPath.Companion.selector(
            "$.store.book[?(@.price > 10 && (search(@.title, 'Sword') || search(@.title,'Rings')))]"
        )
        val books = path.getAll(element)
        assertEquals(2, books.size)
    }
} 