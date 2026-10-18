package com.lorenzovainigli.foodexpirationdates.model.entity

/**
 * Storage locations for food items to help users organize their inventory.
 */
enum class FoodStorageLocation {
    FRIDGE,
    FREEZER,
    PANTRY,
    OTHER;

    companion object {
        fun fromString(value: String?): FoodStorageLocation {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PANTRY
        }
    }
}
