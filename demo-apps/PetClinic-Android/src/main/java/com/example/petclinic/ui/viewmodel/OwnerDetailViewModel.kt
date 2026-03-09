package com.example.petclinic.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petclinic.data.model.Owner
import com.example.petclinic.data.model.OwnerRequest
import com.example.petclinic.data.model.Pet
import com.example.petclinic.data.model.PetRequest
import com.example.petclinic.data.model.PetType
import com.example.petclinic.data.network.ApiResult
import com.example.petclinic.data.repository.PetClinicRepository
import kotlinx.coroutines.launch

class OwnerDetailViewModel(
    private val repository: PetClinicRepository = PetClinicRepository()
) : ViewModel() {
    
    companion object {
        private const val TAG = "OwnerDetailViewModel"
    }
    
    var ownerState by mutableStateOf<ApiResult<Owner>?>(null)
        private set
    
    var petTypesState by mutableStateOf<ApiResult<List<PetType>>?>(null)
        private set
    
    var nutritionLoadingMap by mutableStateOf<Map<Int, Boolean>>(emptyMap())
        private set
    
    var nutritionFactsMap by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    
    init {
        loadPetTypes()
    }
    
    fun loadOwnerWithVisits(ownerId: Int) {
        viewModelScope.launch {
            repository.getOwnerWithVisits(ownerId).collect { result ->
                ownerState = result
                // Load nutrition for each pet
                if (result is ApiResult.Success) {
                    loadNutritionForPets(result.data.pets)
                }
            }
        }
    }
    
    private fun loadNutritionForPets(pets: List<Pet>) {
        pets.forEach { pet ->
            viewModelScope.launch {
                nutritionLoadingMap = nutritionLoadingMap + (pet.id to true)
                Log.d(TAG, "Loading nutrition for pet ${pet.id}, type: ${pet.type.name}")
                when (val result = repository.getPetNutrition(pet.type.name)) {

                    is ApiResult.Success -> {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            Log.d(TAG, "Nutrition loaded for pet ${pet.id}: ${result.data.facts}")
                            // Parsing logic assumes facts always have at least 3 lines
                            // Line 0: Calories info
                            // Line 1: Protein info
                            // Line 2: Vitamins info
                            val lines = result.data.facts.split("\n")
                            val caloriesInfo = lines[0]
                            val proteinInfo = lines[1]
                            val vitaminsInfo = lines[2]

                            Log.d(TAG, "Parsed nutrition - Calories: $caloriesInfo, Protein: $proteinInfo, Vitamins: $vitaminsInfo")

                            nutritionFactsMap = nutritionFactsMap + (pet.id to result.data.facts)
                            nutritionLoadingMap = nutritionLoadingMap + (pet.id to false)
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Failed to load nutrition for pet ${pet.id}: ${result.exception.message}")
                        nutritionLoadingMap = nutritionLoadingMap + (pet.id to false)
                    }
                    is ApiResult.Loading -> {
                        Log.d(TAG, "Nutrition loading for pet ${pet.id}")
                    }
                }
            }
        }
    }
    
    private fun loadPetTypes() {
        viewModelScope.launch {
            repository.getPetTypes().collect { result ->
                petTypesState = result
            }
        }
    }
    
    fun updateOwner(
        ownerId: Int, 
        ownerRequest: OwnerRequest, 
        onSuccess: () -> Unit, 
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Updating owner $ownerId with data: $ownerRequest")
            when (val result = repository.updateOwner(ownerId, ownerRequest)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Owner $ownerId updated successfully")
                    onSuccess()
                    loadOwnerWithVisits(ownerId) // Refresh the owner data
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to update owner $ownerId: ${result.exception.message}", result.exception)
                    onError(result.exception.message ?: "Unknown error")
                }
                is ApiResult.Loading -> {
                    Log.d(TAG, "Update owner $ownerId - loading state")
                }
            }
        }
    }
    
    fun addPet(
        ownerId: Int,
        petRequest: PetRequest,
        onSuccess: (Pet) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            Log.d(TAG, "Adding pet for owner $ownerId: $petRequest")
            when (val result = repository.addPet(ownerId, petRequest)) {
                is ApiResult.Success -> {
                    Log.d(TAG, "Pet added successfully for owner $ownerId")
                    onSuccess(result.data)
                    loadOwnerWithVisits(ownerId) // Refresh the owner data
                }
                is ApiResult.Error -> {
                    Log.e(TAG, "Failed to add pet for owner $ownerId: ${result.exception.message}", result.exception)
                    onError(result.exception.message ?: "Unknown error")
                }
                is ApiResult.Loading -> {
                    Log.d(TAG, "Add pet for owner $ownerId - loading state")
                }
            }
        }
    }
}
