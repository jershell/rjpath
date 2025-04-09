package com.github.jershell.rjpath

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JsonPathTest {
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
    private val test2Json = """
        {
          "store": {
            "book": [
              {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95,
                "metadata": {
                  "tags": ["classic", "quote", "bestseller"],
                  "ratings": {
                    "goodreads": 4.5,
                    "amazon": {
                      "stars": 4,
                      "reviews": ["Excellent!", "Very good", "Informative"]
                    }
                  }
                }
              },
              {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99,
                "metadata": {
                  "tags": [],
                  "ratings": {}
                }
              }
            ],
            "bicycle": {
              "color": "red",
              "price": 19.95,
              "accessories": [
                {
                  "type": "bell",
                  "available": true
                },
                {
                  "type": "basket",
                  "available": false
                }
              ]
            }
          },
          "users": [
            {
              "id": 1,
              "profile": {
                "name": "Alice",
                "contact": {
                  "email": "alice@example.com",
                  "phones": ["123-4567", "987-6543"]
                },
                "preferences": {
                  "notifications": {
                    "email": true,
                    "sms": false,
                    "push": {
                      "enabled": true,
                      "sound": "ping",
                      "vibrationPattern": [100, 200, 100]
                    }
                  }
                }
              }
            },
            {
              "id": 2,
              "profile": {
                "name": "Bob",
                "contact": null,
                "preferences": {}
              }
            }
          ],
          "metrics": {
            "2023": {
              "Q1": [100, 150, 200],
              "Q2": [130, 170, 190],
              "Q3": [null, 160, 210],
              "Q4": {
                "months": {
                  "October": 170,
                  "November": {
                    "week1": 60,
                    "week2": [70, 80, 90],
                    "week3": {
                      "day1": 100,
                      "day2": 110
                    }
                  }
                }
              }
            }
          }
        }

    """.trimIndent()
    private val element: JsonElement = json.parseToJsonElement(testJson)
    private val element2: JsonElement = json.parseToJsonElement(test2Json)

    @Test
    fun testFindTopLevelComparisonOperator() {
        val expression = "(@.price < 10)"
        val result = SelectorParser().findTopLevelComparisonOperator(expression)
        assertEquals(Pair("<", 9), result, "Operator and index must be found")
    }

    @Test
    fun testSimpleProperty() {
        val path = RJPath.selector("$.store.bicycle.color")
        assertEquals("red", path.getFirst(element).jsonPrimitive.content)
    }

    @Test
    fun testArrayIndex() {
        val path = RJPath.selector("$.store.book[0].title")
        assertEquals("Sayings of the Century", path.getFirst(element).jsonPrimitive.content)
    }

    @Test
    fun testArraySlice() {
        val path = RJPath.selector("$.store.book[1:3].title")
        val titles = path.getAll(element).map { it.jsonPrimitive.content }
        assertEquals(
            listOf("Sword of Honour", "Moby Dick"),
            titles
        )
    }

    @Test
    fun testArraySliceWithStep() {
        val path = RJPath.selector("$.store.book[0:4:2].title")
        val titles = path.getAll(element).map { it.jsonPrimitive.content }
        assertEquals(
            listOf("Sayings of the Century", "Moby Dick"),
            titles
        )
    }

    @Test
    fun testWildcard() {
        val path = RJPath.selector("$.store.book[*].author")
        val authors = path.getAll(element).map { it.jsonPrimitive.content }
        assertEquals(
            listOf("Nigel Rees", "Evelyn Waugh", "Herman Melville", "J. R. R. Tolkien"),
            authors
        )
    }

    @Test
    fun testRecursive() {
        val path = RJPath.selector("$..price")
        val prices = path.getAll(element).map { it.jsonPrimitive.content.toDouble() }
        assertEquals(
            listOf(8.95, 12.99, 8.99, 22.99, 19.95),
            prices
        )
    }

    @Test
    fun testFilter() {
        val path = RJPath.selector("$.store.book[?(@.price < 10)]")
        val books = path.getAll(element)
        assertEquals(2, books.size)
    }

    @Test
    fun testFilterWithFunction() {
        val path = RJPath.selector("$.store.book[?(@.price > length(@.title))]")
        val books = path.getAll(element)
        assertEquals(1, books.size)
    }

    @Test
    fun testFilterWithLogicalOperators() {
        val path = RJPath.selector("$.store.book[?(@.price < 10 && @.category == 'fiction')]")
        val books = path.getAll(element)
        assertEquals(1, books.size)
    }

    @Test
    fun testGetFirstOrNull() {
        val path = RJPath.selector("$.store.nonexistent")
        assertNull(path.getFirstOrNull(element))
    }

    @Test
    fun testGetAllOrNull() {
        val path = RJPath.selector("$.store.nonexistent")
        assertTrue(path.getAll(element).isEmpty())
    }

    @Test
    fun testGetFirstThrows() {
        val path = RJPath.selector("$.store.nonexistent")
        assertFailsWith<NoSuchElementException> {
            path.getFirst(element)
        }
    }

    @Test
    fun test2NestedQuery() {
        val path = RJPath.selector("$.store.book[?(@.metadata)].metadata.ratings.amazon.reviews[0]")
        val result = path.getAll(element2)
        val el = result.firstOrNull() as? JsonPrimitive

        assertEquals("Excellent!", el?.content)
    }

    @Test
    fun test2MatchQuery() {
        val path = RJPath.Companion.selector("$.store.book[?match(@.title,'(o){1,3}')]")
        val books = path.getAll(element)
        assertEquals(4, books.size)
    }

    @Test
    fun test2NestedQueryWithCondition() {
        val path = RJPath.selector("$.store.book[?(@.metadata.ratings.goodreads > 3)].metadata.ratings.amazon.reviews[0]")
        val result = path.getAll(element2)
        val el = result.firstOrNull() as? JsonPrimitive

        assertEquals("Excellent!", el?.content)
    }

    @Test
    fun test2NestedQueryWithComplicatedCondition() {
        val path = RJPath.selector("$.store.book[?(@.metadata.ratings.goodreads > 3)][?length(@.tags) > 1 && 1 == count(@.tags[?(@ == 'classic')])].ratings.amazon.reviews[0]")
        val result = path.getAll(element2)
        val el = result.firstOrNull() as? JsonPrimitive

        assertEquals("Excellent!", el?.content)
    }

    @Test
    fun testGetAllWithPath() {
        val path = RJPath.selector("$.store.book[*].title")
        val results = path.getAllWithPath(element)
        
        assertEquals(4, results.size)
        assertEquals("$.store.book[0].title", results[0].path)
        assertEquals("Sayings of the Century", (results[0].value as JsonPrimitive).content)
        
        assertEquals("$.store.book[1].title", results[1].path)
        assertEquals("Sword of Honour", (results[1].value as JsonPrimitive).content)
        
        assertEquals("$.store.book[2].title", results[2].path)
        assertEquals("Moby Dick", (results[2].value as JsonPrimitive).content)
        
        assertEquals("$.store.book[3].title", results[3].path)
        assertEquals("The Lord of the Rings", (results[3].value as JsonPrimitive).content)
    }

    @Test
    fun testGetAllWithPathRecursive() {
        val path = RJPath.selector("$..price")
        val results = path.getAllWithPath(element)
        
        assertEquals(5, results.size)
        assertTrue(results.any { it.path == "$.store.book[0].price" && (it.value as JsonPrimitive).content == "8.95" })
        assertTrue(results.any { it.path == "$.store.book[1].price" && (it.value as JsonPrimitive).content == "12.99" })
        assertTrue(results.any { it.path == "$.store.book[2].price" && (it.value as JsonPrimitive).content == "8.99" })
        assertTrue(results.any { it.path == "$.store.book[3].price" && (it.value as JsonPrimitive).content == "22.99" })
        assertTrue(results.any { it.path == "$.store.bicycle.price" && (it.value as JsonPrimitive).content == "19.95" })
    }

    @Test
    fun testGetAllWithPathNestedObjects() {
        val path = RJPath.selector("$.store.book[0].metadata.tags")
        val results = path.getAllWithPath(element2)
        
        assertEquals(1, results.size)
        assertEquals("$.store.book[0].metadata.tags", results[0].path)
        assertTrue(results[0].value is JsonArray)
    }

    @Test
    fun testGetAllWithPathMultipleIndices() {
        val path = RJPath.selector("$.store.book[0,2].title")
        val results = path.getAllWithPath(element)
        
        assertEquals(2, results.size)
        assertTrue(results.any { it.path == "$.store.book[0].title" && (it.value as JsonPrimitive).content == "Sayings of the Century" })
        assertTrue(results.any { it.path == "$.store.book[2].title" && (it.value as JsonPrimitive).content == "Moby Dick" })
    }

    @Test
    fun testGetAllWithPathSlice() {
        val path = RJPath.selector("$.store.book[0:2].title")
        val results = path.getAllWithPath(element)
        
        assertEquals(2, results.size)
        assertTrue(results.any { it.path == "$.store.book[0].title" && (it.value as JsonPrimitive).content == "Sayings of the Century" })
        assertTrue(results.any { it.path == "$.store.book[1].title" && (it.value as JsonPrimitive).content == "Sword of Honour" })
    }

    @Test
    fun testGetAllWithPathNegativeIndices() {
        val path = RJPath.selector("$.store.book[-1].title")
        val results = path.getAllWithPath(element)
        
        assertEquals(1, results.size)
        assertEquals("$.store.book[3].title", results[0].path)
        assertEquals("The Lord of the Rings", (results[0].value as JsonPrimitive).content)
    }

    @Test
    fun testGetAllWithPathWildcard() {
        val path = RJPath.selector("$.store.book[*].metadata.ratings.amazon.reviews[*]")
        val results = path.getAllWithPath(element2)
        
        assertEquals(3, results.size)
        assertTrue(results.any { it.path == "$.store.book[0].metadata.ratings.amazon.reviews[0]" && (it.value as JsonPrimitive).content == "Excellent!" })
        assertTrue(results.any { it.path == "$.store.book[0].metadata.ratings.amazon.reviews[1]" && (it.value as JsonPrimitive).content == "Very good" })
        assertTrue(results.any { it.path == "$.store.book[0].metadata.ratings.amazon.reviews[2]" && (it.value as JsonPrimitive).content == "Informative" })
    }

    @Test
    fun testGetAllWithPathFilter() {
        val path = RJPath.selector("$.store.book[?(@.price < 10)].title")
        val results = path.getAllWithPath(element)
        
        assertEquals(2, results.size)
        assertTrue(results.any { it.path == "$.store.book[0].title" && (it.value as JsonPrimitive).content == "Sayings of the Century" })
        assertTrue(results.any { it.path == "$.store.book[2].title" && (it.value as JsonPrimitive).content == "Moby Dick" })
    }

    @Test
    fun testGetAllWithPathRecursiveWithFilter() {
        val path = RJPath.selector("$..book[?(@.price > 20)].title")
        val results = path.getAllWithPath(element)
        
        assertEquals(1, results.size)
        assertEquals("$.store.book[3].title", results[0].path)
        assertEquals("The Lord of the Rings", (results[0].value as JsonPrimitive).content)
    }

    @Test
    fun testGetAllWithPathComplexPath() {
        val path = RJPath.selector("$.store.book[0].metadata.ratings.amazon.reviews[0]")
        val results = path.getAllWithPath(element2)
        
        assertEquals(1, results.size)
        assertEquals("$.store.book[0].metadata.ratings.amazon.reviews[0]", results[0].path)
        assertEquals("Excellent!", (results[0].value as JsonPrimitive).content)
    }

    @Test
    fun testGetAllWithPathEmptyResult() {
        val path = RJPath.selector("$.nonexistent.path")
        val results = path.getAllWithPath(element)
        
        assertTrue(results.isEmpty())
    }

    @Test
    fun testGetAllWithPathRoot() {
        val path = RJPath.selector("$")
        val results = path.getAllWithPath(element)
        
        assertEquals(1, results.size)
        assertEquals("$", results[0].path)
        assertTrue(results[0].value is JsonObject)
    }
} 