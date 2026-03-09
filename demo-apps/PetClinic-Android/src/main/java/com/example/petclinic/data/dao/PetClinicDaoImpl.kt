package com.example.petclinic.data.dao

import com.example.petclinic.data.model.*
import com.example.petclinic.data.network.*
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.lang.reflect.Type

/**
 * Implementation of PetClinicDao using OkHttp client
 */
class PetClinicDaoImpl(
    private val baseUrl: String = "http://10.0.2.2:8080" // Android emulator localhost
) : PetClinicDao {
    
    private val client = HttpClient.okHttpClient
    private val moshi = HttpClient.moshi
    
    // JSON adapters with explicit types
    private val ownerAdapter = moshi.adapter(Owner::class.java)
    private val ownerListType: Type = Types.newParameterizedType(List::class.java, Owner::class.java)
    private val ownerListAdapter = moshi.adapter<List<Owner>>(ownerListType)
    private val ownerRequestAdapter = moshi.adapter(OwnerRequest::class.java)
    private val petAdapter = moshi.adapter(Pet::class.java)
    private val petTypeListType: Type = Types.newParameterizedType(List::class.java, PetType::class.java)
    private val petTypeListAdapter = moshi.adapter<List<PetType>>(petTypeListType)
    private val petRequestAdapter = moshi.adapter(PetRequest::class.java)
    private val visitsAdapter = moshi.adapter(Visits::class.java)
    private val visitAdapter = moshi.adapter(Visit::class.java)
    private val vetListType: Type = Types.newParameterizedType(List::class.java, Vet::class.java)
    private val vetListAdapter = moshi.adapter<List<Vet>>(vetListType)
    private val petNutritionAdapter = moshi.adapter(PetNutrition::class.java)
    
    override suspend fun getOwners(): ApiResult<List<Owner>> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            ownerListAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse owners list")
        }
    }
    
    override suspend fun getOwner(ownerId: Int): ApiResult<Owner> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners/$ownerId")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            ownerAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse owner")
        }
    }
    
    override suspend fun getOwnerWithVisits(ownerId: Int): ApiResult<Owner> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/gateway/owners/$ownerId")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            ownerAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse owner with visits")
        }
    }
    
    override suspend fun updateOwner(ownerId: Int, ownerRequest: OwnerRequest): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            val json = ownerRequestAdapter.toJson(ownerRequest)
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners/$ownerId")
                .putJsonBody(json)
                .build()
            
            client.newCall(request).execute()
            Unit
        }
    }
    
    override suspend fun addOwner(ownerRequest: OwnerRequest): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            val json = ownerRequestAdapter.toJson(ownerRequest)
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners")
                .jsonBody(json)
                .build()
            
            client.newCall(request).execute()
            Unit
        }
    }
    
    override suspend fun getPetTypes(): ApiResult<List<PetType>> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/customer/petTypes")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            petTypeListAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse pet types")
        }
    }
    
    override suspend fun getPet(ownerId: Int, petId: Int): ApiResult<Pet> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners/$ownerId/pets/$petId")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            petAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse pet")
        }
    }
    
    override suspend fun updatePet(ownerId: Int, petId: Int, petRequest: PetRequest): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            val json = petRequestAdapter.toJson(petRequest)
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners/$ownerId/pets/$petId")
                .putJsonBody(json)
                .build()
            
            client.newCall(request).execute()
            Unit
        }
    }
    
    override suspend fun addPet(ownerId: Int, petRequest: PetRequest): ApiResult<Pet> = withContext(Dispatchers.IO) {
        safeApiCall {
            val json = petRequestAdapter.toJson(petRequest)
            val request = Request.Builder()
                .url("$baseUrl/api/customer/owners/$ownerId/pets")
                .jsonBody(json)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            petAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse new pet")
        }
    }
    
    override suspend fun diagnosePet(ownerId: Int, petId: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/customer/diagnose/owners/$ownerId/pets/$petId")
                .get()
                .build()
            
            client.newCall(request).execute()
            Unit
        }
    }
    
    override suspend fun getVisits(ownerId: Int, petId: Int): ApiResult<Visits> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/visit/owners/$ownerId/pets/$petId/visits")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            visitsAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse visits")
        }
    }
    
    override suspend fun addVisit(ownerId: Int, petId: Int, visit: Visit): ApiResult<String> = withContext(Dispatchers.IO) {
        safeApiCall {
            val json = visitAdapter.toJson(visit)
            val request = Request.Builder()
                .url("$baseUrl/api/visit/owners/$ownerId/pets/$petId/visits")
                .jsonBody(json)
                .build()
            
            val response = client.newCall(request).execute()
            response.getBodyString()
        }
    }
    
    override suspend fun getVets(): ApiResult<List<Vet>> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/vet/vets")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            vetListAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse vets")
        }
    }
    
    override suspend fun getPetNutrition(petType: String): ApiResult<PetNutrition> = withContext(Dispatchers.IO) {
        safeApiCall {
            val request = Request.Builder()
                .url("$baseUrl/api/nutrition/facts/$petType")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.getBodyString()
            
            petNutritionAdapter.fromJson(responseBody) 
                ?: throw ApiException.ParseException("Failed to parse pet nutrition")
        }
    }
}
