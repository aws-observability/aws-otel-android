package com.example.petclinic.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PetNutrition(
    @Json(name = "facts") val facts: String
)
