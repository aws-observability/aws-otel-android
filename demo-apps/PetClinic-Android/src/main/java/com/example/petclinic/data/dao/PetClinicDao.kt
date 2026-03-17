package com.example.petclinic.data.dao

import com.example.petclinic.data.model.*
import com.example.petclinic.data.network.ApiResult

/**
 * Data Access Object interface for Pet Clinic API operations
 */
interface PetClinicDao {
    
    // Owner operations
    suspend fun getOwners(): ApiResult<List<Owner>>
    suspend fun getOwner(ownerId: Int): ApiResult<Owner>
    suspend fun getOwnerWithVisits(ownerId: Int): ApiResult<Owner>
    suspend fun updateOwner(ownerId: Int, ownerRequest: OwnerRequest): ApiResult<Unit>
    suspend fun addOwner(ownerRequest: OwnerRequest): ApiResult<Unit>
    
    // Pet operations
    suspend fun getPetTypes(): ApiResult<List<PetType>>
    suspend fun getPet(ownerId: Int, petId: Int): ApiResult<Pet>
    suspend fun updatePet(ownerId: Int, petId: Int, petRequest: PetRequest): ApiResult<Unit>
    suspend fun addPet(ownerId: Int, petRequest: PetRequest): ApiResult<Pet>
    suspend fun diagnosePet(ownerId: Int, petId: Int): ApiResult<Unit>
    
    // Visit operations
    suspend fun getVisits(ownerId: Int, petId: Int): ApiResult<Visits>
    suspend fun addVisit(ownerId: Int, petId: Int, visit: Visit): ApiResult<String>
    
    // Vet operations
    suspend fun getVets(): ApiResult<List<Vet>>
    
    // Nutrition operations
    suspend fun getPetNutrition(petType: String): ApiResult<PetNutrition>
}
