package com.example.petclinic.data.repository

import com.example.petclinic.data.dao.PetClinicDao
import com.example.petclinic.data.di.DataModule
import com.example.petclinic.data.model.*
import com.example.petclinic.data.network.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository class that provides a clean API for UI layer to interact with Pet Clinic data
 */
class PetClinicRepository(
    private val dao: PetClinicDao = DataModule.getPetClinicDao()
) {
    
    /**
     * Get all owners as a Flow
     */
    fun getOwners(): Flow<ApiResult<List<Owner>>> = flow {
        emit(ApiResult.Loading)
        emit(dao.getOwners())
    }
    
    /**
     * Get owner by ID as a Flow
     */
    fun getOwner(ownerId: Int): Flow<ApiResult<Owner>> = flow {
        emit(ApiResult.Loading)
        emit(dao.getOwner(ownerId))
    }
    
    /**
     * Get owner with visits as a Flow
     */
    fun getOwnerWithVisits(ownerId: Int): Flow<ApiResult<Owner>> = flow {
        emit(ApiResult.Loading)
        emit(dao.getOwnerWithVisits(ownerId))
    }
    
    /**
     * Get all veterinarians as a Flow
     */
    fun getVets(): Flow<ApiResult<List<Vet>>> = flow {
        emit(ApiResult.Loading)
        emit(dao.getVets())
    }
    
    /**
     * Get pet types as a Flow
     */
    fun getPetTypes(): Flow<ApiResult<List<PetType>>> = flow {
        emit(ApiResult.Loading)
        emit(dao.getPetTypes())
    }
    
    /**
     * Add a new owner
     */
    suspend fun addOwner(ownerRequest: OwnerRequest): ApiResult<Unit> {
        return dao.addOwner(ownerRequest)
    }
    
    /**
     * Update an existing owner
     */
    suspend fun updateOwner(ownerId: Int, ownerRequest: OwnerRequest): ApiResult<Unit> {
        return dao.updateOwner(ownerId, ownerRequest)
    }
    
    /**
     * Add a new pet to an owner
     */
    suspend fun addPet(ownerId: Int, petRequest: PetRequest): ApiResult<Pet> {
        return dao.addPet(ownerId, petRequest)
    }
    
    /**
     * Add a visit for a pet
     */
    suspend fun addVisit(ownerId: Int, petId: Int, visit: Visit): ApiResult<String> {
        return dao.addVisit(ownerId, petId, visit)
    }
    
    /**
     * Get nutrition facts for a pet type
     */
    suspend fun getPetNutrition(petType: String): ApiResult<PetNutrition> {
        return dao.getPetNutrition(petType)
    }
}
