package com.lorenzovainigli.foodexpirationdates.model.recipe

import com.lorenzovainigli.foodexpirationdates.model.entity.ExpirationDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartRecipeSuggesterTest {

    @Test
    fun suggest_emptyInventory_returnsEmpty() {
        val result = SmartRecipeSuggester.suggest(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun suggest_hasBreadAndCheese_suggestsToast() {
        val items = listOf(
            ExpirationDate(id = 1, foodName = "Sari Roti Tawar Gandum", expirationDate = 1000L),
            ExpirationDate(id = 2, foodName = "Keju Cheddar Kraft", expirationDate = 2000L)
        )

        val results = SmartRecipeSuggester.suggest(items)
        assertTrue(results.any { it.title.contains("Roti Panggang Keju") })
    }

    @Test
    fun suggest_hasMilkAndEggs_suggestsPancake() {
        val items = listOf(
            ExpirationDate(id = 1, foodName = "Susu UHT Greenfield", expirationDate = 1000L),
            ExpirationDate(id = 2, foodName = "Telur Ayam Omega", expirationDate = 2000L)
        )

        val results = SmartRecipeSuggester.suggest(items)
        assertTrue(results.any { it.title.contains("Pancake Susu") })
    }

    @Test
    fun suggest_hasUnrelatedItems_onlyMatchesAvailableRecipes() {
        val items = listOf(
            ExpirationDate(id = 1, foodName = "Kopi Bubuk Robusta", expirationDate = 1000L),
            ExpirationDate(id = 2, foodName = "Gula Pasir", expirationDate = 2000L)
        )

        val results = SmartRecipeSuggester.suggest(items)
        assertTrue(results.isEmpty())
    }
}
