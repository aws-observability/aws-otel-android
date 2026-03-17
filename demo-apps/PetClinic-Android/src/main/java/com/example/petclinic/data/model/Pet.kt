package com.example.petclinic.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Pet(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "birthDate")
    val birthDate: String,
    @Json(name = "type")
    val type: PetType,
    @Json(name = "visits")
    val visits: List<Visit> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PetType(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)

@JsonClass(generateAdapter = true)
data class PetRequest(
    @Json(name = "name")
    val name: String,
    @Json(name = "birthDate")
    val birthDate: String,
    @Json(name = "typeId")
    val typeId: Int
)
