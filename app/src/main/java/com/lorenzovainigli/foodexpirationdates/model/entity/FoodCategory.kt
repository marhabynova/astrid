package com.lorenzovainigli.foodexpirationdates.model.entity

/**
 * Standard food categories to classify items for better organization and reminders.
 */
enum class FoodCategory {
    DAIRY,      // Susu, Keju, Yogurt
    MEAT,       // Daging, Ayam, Ikan
    PRODUCE,    // Buah & Sayuran segar
    BAKERY,     // Roti, Pastry
    BEVERAGE,   // Jus, Teh, Kopi, Sirup
    PANTRY,     // Bumbu, Makanan Kaleng, Mie, Beras
    SNACKS,     // Camilan, Biskuit, Keripik
    OTHER;      // Lain-lain

    companion object {
        fun fromString(value: String?): FoodCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
