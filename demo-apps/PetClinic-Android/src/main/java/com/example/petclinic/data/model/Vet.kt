package com.example.petclinic.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Vet(
    @Json(name = "id")
    val id: Int,
    @Json(name = "firstName")
    val firstName: String,
    @Json(name = "lastName")
    val lastName: String,
    @Json(name = "specialties")
    val specialties: List<Specialty> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Specialty(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)
