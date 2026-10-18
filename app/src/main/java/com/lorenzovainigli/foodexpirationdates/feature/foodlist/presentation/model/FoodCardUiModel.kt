package com.lorenzovainigli.foodexpirationdates.feature.foodlist.presentation.model

import com.lorenzovainigli.foodexpirationdates.model.entity.FoodStorageLocation

data class FoodCardUiModel(
    val id: Int,
    val foodName: String,
    val quantity: Int,
    val expirationText: String,
    val daysUntilExpiration: Int,
    val isOpened: Boolean,
    val expirationStatus: ExpirationStatus,
    val storageLocation: FoodStorageLocation = FoodStorageLocation.PANTRY
)
