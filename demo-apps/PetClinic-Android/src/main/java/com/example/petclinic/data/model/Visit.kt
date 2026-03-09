package com.example.petclinic.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Visit(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "petId")
    val petId: Int?,
    @Json(name = "date")
    val date: String?,
    @Json(name = "description")
    val description: String?
)

@JsonClass(generateAdapter = true)
data class Visits(
    @Json(name = "items")
    val items: List<Visit> = emptyList()
)
