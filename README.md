# RJPath

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

RJPath is JSONPath implementation for Kotlin Multiplatform that allows you to easily extract data from JSON structures using JSONPath expressions. The implementation follows the [RFC9535](https://datatracker.ietf.org/doc/rfc9535/) specification.

## Features

- 🚀 JSONPath RFC9535 specification support
- 💻 Kotlin Multiplatform (JVM, JS, Native, Wasm, IOS)
- 🧪 Test coverage

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.github.jershell:rjpath:1.1.1")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.github.jershell:rjpath:1.1.1'
}
```

## Quick Start

```kotlin
import com.github.jershell.rjpath.RJPath

fun main() {
    val jsonString = """
        {
            "store": {
                "books": [
                    {
                        "title": "The Great Gatsby",
                        "price": 9.99
                    },
                    {
                        "title": "1984",
                        "price": 15.99
                    }
                ]
            }
        }
    """.trimIndent()

    val rjpath = RJPath.selector("$.store.books[*].title")
    
    // Get all book titles
    val titles = rjpath.getAll(json.parseToJsonElement(jsonString))
    println(titles) // ["The Great Gatsby", "1984"]
    
    // Get first book's price
    val firstBookPrice = RJPath.selector("$.store.books[0].price")
        .getFirst(json.parseToJsonElement(jsonString))
    println(firstBookPrice) // 9.99

    // Get all book titles with their paths
    val titlesWithPaths = rjpath.getAllWithPath(json.parseToJsonElement(jsonString))
    titlesWithPaths.forEach { (path, value) ->
        println("$path: $value")
    }
    // Output:
    // $.store.books[0].title: "The Great Gatsby"
    // $.store.books[1].title: "1984"
}
```

## Supported JSONPath Operators

| Operator                  | Description                                   | Example                                |
|--------------------------|-----------------------------------------------|---------------------------------------|
| `$`                      | Root element                                  | `$`                                   |
| `.`                      | Child operator                                | `$.store.book`                        |
| `..`                     | Recursive descent                             | `$..author`                           |
| `*`                      | Wildcard                                      | `$.store.book[*]`                     |
| `[start:end:step]`       | Array slice                                  | `$[0:2]`                             |
| `[,]`                    | Union                                         | `$[0,3]`                             |
| `?()`                    | Filter (script)                               | `$.store.book[?(@.price < 100)]`      |

## Advanced Examples

For comprehensive examples and usage patterns, please refer to our [test suite](rjpath/src/commonTest/kotlin/com/github/jershell/rjpath/JsonPathTest.kt). The tests demonstrate various use cases including:

### Filtering

```kotlin
// Find all books over $10
val expensiveBooks = RJPath.selector("$.store.book[?(@.price > 10)]")
    .getAll(jsonElement)

// Find books by specific author
val authorBooks = RJPath.selector("$.store.book[?(@.author == 'Herman Melville')]")
    .getAll(jsonElement)
```

### Recursive Search

```kotlin
// Find all prices in the document
val allPrices = RJPath.selector("$..price")
    .getAll(jsonElement)

// Find all leaf values
val leaves = RJPath.selector("$..*")
    .getAll(jsonElement)
```

### Error Handling

```kotlin
// Safe access with null handling
val maybeValue = RJPath.selector("$.nonexistent.path")
    .getFirstOrNull(jsonElement)

// Get all matches or empty list
val allMatches = RJPath.selector("$.nonexistent.path")
    .getAll(jsonElement) // returns empty list if no matches
```

## Contributing

We welcome contributions! If you'd like to help:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to your fork (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Testing

The library includes comprehensive test coverage. You can find test cases in:
- [JsonPathTest.kt](rjpath/src/commonTest/kotlin/com/github/jershell/rjpath/JsonPathTest.kt) - Core functionality tests
- [JsonPathFunctionsTest.kt](rjpath/src/commonTest/kotlin/com/github/jershell/rjpath/JsonPathFunctionsTest.kt) - Function tests

These tests serve as both documentation and validation of the library's functionality.

## License

Distributed under the MIT License. See `LICENSE` file for more information.

## Contact

- GitHub: [jershell](https://github.com/jershell)
- Email: tukhvatullin01@gmail.com

## Acknowledgments

Special thanks to all contributors and community members.
