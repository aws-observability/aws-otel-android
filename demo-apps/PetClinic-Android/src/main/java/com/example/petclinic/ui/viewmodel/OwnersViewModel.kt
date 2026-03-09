package com.example.petclinic.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petclinic.data.model.Owner
import com.example.petclinic.data.model.OwnerRequest
import com.example.petclinic.data.network.ApiResult
import com.example.petclinic.data.repository.PetClinicRepository
import kotlinx.coroutines.launch

class OwnersViewModel(
    private val repository: PetClinicRepository = PetClinicRepository()
) : ViewModel() {
    
    var ownersState by mutableStateOf<ApiResult<List<Owner>>?>(null)
        private set
    
    var searchQuery by mutableStateOf("")
        private set
    
    var filteredOwners by mutableStateOf<List<Owner>>(emptyList())
        private set
    
    fun loadOwners() {
        viewModelScope.launch {
            repository.getOwners().collect { result ->
                ownersState = result
                if (result is ApiResult.Success) {
                    filterOwners(result.data, searchQuery)
                }
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        searchQuery = query
        val currentOwners = (ownersState as? ApiResult.Success)?.data ?: emptyList()
        filterOwners(currentOwners, query)
    }
    
    private fun filterOwners(owners: List<Owner>, query: String) {
        filteredOwners = if (query.isBlank()) {
            owners
        } else {
            owners.filter { owner ->
                "${owner.firstName} ${owner.lastName}".contains(query, ignoreCase = true) ||
                owner.address.contains(query, ignoreCase = true) ||
                owner.city.contains(query, ignoreCase = true) ||
                owner.telephone.contains(query, ignoreCase = true)
            }
        }
    }
    
    fun addOwner(ownerRequest: OwnerRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.addOwner(ownerRequest)) {
                is ApiResult.Success -> {
                    onSuccess()
                    loadOwners() // Refresh the list
                }
                is ApiResult.Error -> {
                    onError(result.exception.message ?: "Unknown error")
                }
                is ApiResult.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
}
