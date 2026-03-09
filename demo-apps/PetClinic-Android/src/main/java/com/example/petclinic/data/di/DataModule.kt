package com.example.petclinic.data.di

import com.example.petclinic.data.dao.PetClinicDao
import com.example.petclinic.data.dao.PetClinicDaoImpl
import com.example.petclinic.data.network.ApiConfig

/**
 * Simple dependency injection for data layer
 */
object DataModule {
    
    private var _petClinicDao: PetClinicDao? = null
    
    /**
     * Get singleton instance of PetClinicDao
     */
    fun getPetClinicDao(baseUrl: String = ApiConfig.getBaseUrl()): PetClinicDao {
        return _petClinicDao ?: synchronized(this) {
            _petClinicDao ?: PetClinicDaoImpl(baseUrl).also { _petClinicDao = it }
        }
    }
    
    /**
     * Reset the DAO instance (useful for testing)
     */
    fun resetDao() {
        _petClinicDao = null
    }
}
