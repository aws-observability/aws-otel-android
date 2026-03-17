package com.example.petclinic.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petclinic.data.model.Vet
import com.example.petclinic.data.network.ApiResult
import com.example.petclinic.data.repository.PetClinicRepository
import kotlinx.coroutines.launch

class VetsViewModel(
    private val repository: PetClinicRepository = PetClinicRepository()
) : ViewModel() {
    
    var vetsState by mutableStateOf<ApiResult<List<Vet>>?>(null)
        private set
    
    init {
        loadVets()
    }
    
    fun loadVets() {
        viewModelScope.launch {
            repository.getVets().collect { result ->
                vetsState = result
            }
        }
    }
}
