package com.lorenzovainigli.foodexpirationdates.model.recipe

import com.lorenzovainigli.foodexpirationdates.model.entity.ExpirationDate

data class RecipeSuggestion(
    val title: String,
    val description: String,
    val matchedIngredients: List<String>,
    val difficulty: String, // "Mudah", "Sedang"
    val estimatedMinutes: Int,
    val urgencyScore: Int // Higher means ingredients are expiring sooner
)

/**
 * Intelligent recipe engine that recommends quick home meals based on expiring ingredients
 * to proactively prevent food waste ("Use This Up" feature).
 */
object SmartRecipeSuggester {

    private data class RecipeTemplate(
        val title: String,
        val description: String,
        val requiredKeywords: List<List<String>>, // Each sublist represents synonymous keywords for an ingredient
        val difficulty: String,
        val estimatedMinutes: Int
    )

    private val RECIPES = listOf(
        RecipeTemplate(
            title = "Omelet Keju & Daging",
            description = "Masak telur dengan potongan keju dan daging cincang untuk sarapan atau makan malam praktis.",
            requiredKeywords = listOf(
                listOf("telur", "egg"),
                listOf("keju", "cheese", "susu", "milk", "daging", "meat", "sosis")
            ),
            difficulty = "Mudah",
            estimatedMinutes = 10
        ),
        RecipeTemplate(
            title = "Pancake Susu Lembut",
            description = "Habiskan stok susu dan telur sebelum basi dengan membuat pancake manis lezat.",
            requiredKeywords = listOf(
                listOf("susu", "milk"),
                listOf("telur", "egg", "tepung", "flour", "mentega", "butter")
            ),
            difficulty = "Mudah",
            estimatedMinutes = 15
        ),
        RecipeTemplate(
            title = "Sup Daging Sayuran Hangat",
            description = "Gunakan daging beku dan sayuran segar menjadi sup gurih bergizi keluarga.",
            requiredKeywords = listOf(
                listOf("daging", "meat", "ayam", "chicken", "kornet", "beef"),
                listOf("sayur", "wortel", "kentang", "buncis", "soup")
            ),
            difficulty = "Sedang",
            estimatedMinutes = 25
        ),
        RecipeTemplate(
            title = "Roti Panggang Keju Leleh",
            description = "Pilihan terbaik menghabiskan roti tawar dan keju yang mendekati tanggal kadaluarsa.",
            requiredKeywords = listOf(
                listOf("roti", "bread"),
                listOf("keju", "cheese", "mentega", "butter", "selai")
            ),
            difficulty = "Sangat Mudah",
            estimatedMinutes = 5
        ),
        RecipeTemplate(
            title = "Nasi Goreng Spesial Dapur",
            description = "Kombinasi nasi dengan sisa telur, sosis, ayam, atau bumbu dapur yang ada.",
            requiredKeywords = listOf(
                listOf("beras", "nasi", "rice"),
                listOf("telur", "sosis", "ayam", "kecap", "bawang")
            ),
            difficulty = "Mudah",
            estimatedMinutes = 15
        ),
        RecipeTemplate(
            title = "Mie Kuah Telur & Sayur",
            description = "Olah mie instan dengan tambahan telur dan sawi/sayur agar lebih bernutrisi.",
            requiredKeywords = listOf(
                listOf("mie", "noodle", "indomie"),
                listOf("telur", "egg", "sayur", "cabai")
            ),
            difficulty = "Sangat Mudah",
            estimatedMinutes = 8
        )
    )

    fun suggest(items: List<ExpirationDate>): List<RecipeSuggestion> {
        if (items.isEmpty()) return emptyList()

        val itemNames = items.map { it.foodName.lowercase() }
        val suggestions = mutableListOf<RecipeSuggestion>()

        for (recipe in RECIPES) {
            val matchedNames = mutableListOf<String>()
            var allCriteriaMet = true

            for (synonymGroup in recipe.requiredKeywords) {
                val matched = items.firstOrNull { item ->
                    val lower = item.foodName.lowercase()
                    synonymGroup.any { lower.contains(it) }
                }

                if (matched != null) {
                    if (!matchedNames.contains(matched.foodName)) {
                        matchedNames.add(matched.foodName)
                    }
                } else {
                    allCriteriaMet = false
                    break
                }
            }

            if (allCriteriaMet && matchedNames.isNotEmpty()) {
                suggestions.add(
                    RecipeSuggestion(
                        title = recipe.title,
                        description = recipe.description,
                        matchedIngredients = matchedNames,
                        difficulty = recipe.difficulty,
                        estimatedMinutes = recipe.estimatedMinutes,
                        urgencyScore = matchedNames.size * 10
                    )
                )
            }
        }

        return suggestions.sortedByDescending { it.urgencyScore }
    }
}
