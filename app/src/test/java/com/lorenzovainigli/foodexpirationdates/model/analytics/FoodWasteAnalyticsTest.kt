package com.lorenzovainigli.foodexpirationdates.model.analytics

import com.lorenzovainigli.foodexpirationdates.model.entity.ExpirationDate
import com.lorenzovainigli.foodexpirationdates.model.entity.FoodStorageLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class FoodWasteAnalyticsTest {

    private val now = 1770000000000L // Fixed reference timestamp
    private val oneDay = TimeUnit.DAYS.toMillis(1)

    @Test
    fun calculate_emptyList_returnsZeroState() {
        val stats = FoodWasteAnalyticsCalculator.calculate(
            items = emptyList(),
            consumedCount = 0,
            currentTimeMillis = now
        )

        assertEquals(0, stats.totalItems)
        assertEquals(0, stats.activeItems)
        assertEquals(0, stats.expiredCount)
        assertEquals(0, stats.expiringIn3DaysCount)
        assertEquals(UrgencyStatus.SAFE, stats.urgencyStatus)
    }

    @Test
    fun calculate_categorizesExpirationsAccurately() {
        val items = listOf(
            // Expired 2 days ago (Quantity: 2)
            ExpirationDate(id = 1, foodName = "Susu UHT", expirationDate = now - (2 * oneDay), quantity = 2),
            // Expiring in 2 days (Quantity: 1)
            ExpirationDate(id = 2, foodName = "Roti Gandum", expirationDate = now + (2 * oneDay), quantity = 1),
            // Expiring in 5 days (Quantity: 3)
            ExpirationDate(id = 3, foodName = "Keju Cheddar", expirationDate = now + (5 * oneDay), quantity = 3),
            // Expiring in 30 days (Quantity: 4)
            ExpirationDate(id = 4, foodName = "Beras Pandan Wangi", expirationDate = now + (30 * oneDay), quantity = 4)
        )

        val stats = FoodWasteAnalyticsCalculator.calculate(
            items = items,
            consumedCount = 5,
            currentTimeMillis = now
        )

        // 2 + 1 + 3 + 4 = 10 active units, plus 5 consumed = 15 total
        assertEquals(15, stats.totalItems)
        assertEquals(10, stats.activeItems)
        assertEquals(2, stats.expiredCount)
        assertEquals(1, stats.expiringIn3DaysCount)
        assertEquals(3, stats.expiringIn7DaysCount)
        assertEquals(4, stats.freshCount)
        assertEquals(UrgencyStatus.CRITICAL, stats.urgencyStatus) // Because expired > 0
    }

    @Test
    fun calculate_consumptionRateAndSavings_calculatedProperly() {
        val items = listOf(
            // 2 expired items
            ExpirationDate(id = 1, foodName = "Yogurt", expirationDate = now - oneDay, quantity = 2)
        )

        // 8 consumed items, 2 expired items -> 8 / (8 + 2) = 80% consumption rate
        val stats = FoodWasteAnalyticsCalculator.calculate(
            items = items,
            consumedCount = 8,
            currentTimeMillis = now,
            itemCostEstimate = 20000L
        )

        assertEquals(80, stats.consumptionRatePercentage)
        assertEquals(160000L, stats.estimatedMoneySaved) // 8 * 20000
    }

    @Test
    fun deduceStorageLocation_identifiesProperStorage() {
        val milk = ExpirationDate(id = 1, foodName = "Susu Segar Greenfield", expirationDate = now)
        val chicken = ExpirationDate(id = 2, foodName = "Daging Ayam Fillet Frozen", expirationDate = now)
        val oats = ExpirationDate(id = 3, foodName = "Quaker Oats", expirationDate = now)

        assertEquals(FoodStorageLocation.FRIDGE, FoodWasteAnalyticsCalculator.deduceStorageLocation(milk))
        assertEquals(FoodStorageLocation.FREEZER, FoodWasteAnalyticsCalculator.deduceStorageLocation(chicken))
        assertEquals(FoodStorageLocation.PANTRY, FoodWasteAnalyticsCalculator.deduceStorageLocation(oats))
    }
}
