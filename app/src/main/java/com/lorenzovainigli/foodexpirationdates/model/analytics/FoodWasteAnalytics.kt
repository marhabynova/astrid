package com.lorenzovainigli.foodexpirationdates.model.analytics

import com.lorenzovainigli.foodexpirationdates.model.entity.ExpirationDate
import com.lorenzovainigli.foodexpirationdates.model.entity.FoodStorageLocation
import com.lorenzovainigli.foodexpirationdates.model.entity.computeExpirationDate
import java.util.concurrent.TimeUnit

enum class UrgencyStatus {
    CRITICAL, // Has expired items
    WARNING,  // Has items expiring within 3 days
    SAFE      // All items fresh
}

data class FoodWasteStats(
    val totalItems: Int,
    val activeItems: Int,
    val expiredCount: Int,
    val expiringIn3DaysCount: Int,
    val expiringIn7DaysCount: Int,
    val freshCount: Int,
    val consumptionRatePercentage: Int,
    val estimatedMoneySaved: Long,
    val storageBreakdown: Map<FoodStorageLocation, Int>,
    val urgencyStatus: UrgencyStatus
)

object FoodWasteAnalyticsCalculator {

    // Default average cost per food item in IDR (Rp 25.000)
    const val DEFAULT_AVERAGE_ITEM_COST = 25000L

    fun calculate(
        items: List<ExpirationDate>,
        consumedCount: Int = 0,
        currentTimeMillis: Long = System.currentTimeMillis(),
        itemCostEstimate: Long = DEFAULT_AVERAGE_ITEM_COST
    ): FoodWasteStats {
        var expired = 0
        var expiring3Days = 0
        var expiring7Days = 0
        var fresh = 0
        val storageMap = mutableMapOf<FoodStorageLocation, Int>()

        for (item in items) {
            val expiry = computeExpirationDate(item)
            val diffMillis = expiry - currentTimeMillis
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            when {
                diffMillis < 0 -> expired += item.quantity
                diffDays <= 3 -> expiring3Days += item.quantity
                diffDays <= 7 -> expiring7Days += item.quantity
                else -> fresh += item.quantity
            }

            val loc = deduceStorageLocation(item)
            storageMap[loc] = (storageMap[loc] ?: 0) + item.quantity
        }

        val totalEvaluated = consumedCount + expired
        val consumptionRate = if (totalEvaluated > 0) {
            ((consumedCount.toDouble() / totalEvaluated) * 100).toInt()
        } else if (items.isNotEmpty()) {
            // If all items are active and none expired, efficiency is 100%
            100
        } else {
            0
        }

        val estimatedSavings = consumedCount * itemCostEstimate

        val urgency = when {
            expired > 0 -> UrgencyStatus.CRITICAL
            expiring3Days > 0 -> UrgencyStatus.WARNING
            else -> UrgencyStatus.SAFE
        }

        return FoodWasteStats(
            totalItems = items.sumOf { it.quantity } + consumedCount,
            activeItems = items.sumOf { it.quantity },
            expiredCount = expired,
            expiringIn3DaysCount = expiring3Days,
            expiringIn7DaysCount = expiring7Days,
            freshCount = fresh,
            consumptionRatePercentage = consumptionRate,
            estimatedMoneySaved = estimatedSavings,
            storageBreakdown = storageMap,
            urgencyStatus = urgency
        )
    }

    fun deduceStorageLocation(item: ExpirationDate): FoodStorageLocation {
        val lower = item.foodName.lowercase()
        return when {
            lower.contains("susu") || lower.contains("milk") || lower.contains("keju") ||
            lower.contains("cheese") || lower.contains("yogurt") || lower.contains("butter") ||
            lower.contains("telur") || lower.contains("egg") || lower.contains("sayur") -> FoodStorageLocation.FRIDGE

            lower.contains("daging") || lower.contains("meat") || lower.contains("ayam") ||
            lower.contains("chicken") || lower.contains("ikan") || lower.contains("fish") ||
            lower.contains("nugget") || lower.contains("sosis") || lower.contains("ice cream") ||
            lower.contains("frozen") -> FoodStorageLocation.FREEZER

            else -> FoodStorageLocation.PANTRY
        }
    }
}
