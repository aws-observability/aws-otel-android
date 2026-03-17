package com.example.petclinic.data.dao

import com.example.petclinic.data.model.*
import com.example.petclinic.data.network.ApiResult
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PetClinicDaoTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var dao: PetClinicDao
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        dao = PetClinicDaoImpl(mockWebServer.url("/").toString().removeSuffix("/"))
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `getOwners returns success with valid response`() = runTest {
        // Given
        val mockResponse = """
            [
                {
                    "id": 1,
                    "firstName": "George",
                    "lastName": "Franklin",
                    "address": "110 W. Liberty St.",
                    "city": "Madison",
                    "telephone": "6085551023",
                    "pets": []
                }
            ]
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        // When
        val result = dao.getOwners()
        
        // Then
        assertTrue(result is ApiResult.Success)
        val owners = (result as ApiResult.Success).data
        assertEquals(1, owners.size)
        assertEquals("George", owners[0].firstName)
        assertEquals("Franklin", owners[0].lastName)
    }
    
    @Test
    fun `getOwner returns success with valid response`() = runTest {
        // Given
        val mockResponse = """
            {
                "id": 1,
                "firstName": "George",
                "lastName": "Franklin",
                "address": "110 W. Liberty St.",
                "city": "Madison",
                "telephone": "6085551023",
                "pets": [
                    {
                        "id": 1,
                        "name": "Leo",
                        "birthDate": "2010-09-07",
                        "type": {
                            "id": 1,
                            "name": "cat"
                        },
                        "visits": []
                    }
                ]
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        // When
        val result = dao.getOwner(1)
        
        // Then
        assertTrue(result is ApiResult.Success)
        val owner = (result as ApiResult.Success).data
        assertEquals(1, owner.id)
        assertEquals("George", owner.firstName)
        assertEquals(1, owner.pets.size)
        assertEquals("Leo", owner.pets[0].name)
    }
    
    @Test
    fun `getPetTypes returns success with valid response`() = runTest {
        // Given
        val mockResponse = """
            [
                {
                    "id": 1,
                    "name": "cat"
                },
                {
                    "id": 2,
                    "name": "dog"
                }
            ]
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        // When
        val result = dao.getPetTypes()
        
        // Then
        assertTrue(result is ApiResult.Success)
        val petTypes = (result as ApiResult.Success).data
        assertEquals(2, petTypes.size)
        assertEquals("cat", petTypes[0].name)
        assertEquals("dog", petTypes[1].name)
    }
    
    @Test
    fun `getOwners returns error on HTTP error`() = runTest {
        // Given
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error")
        )
        
        // When
        val result = dao.getOwners()
        
        // Then
        assertTrue(result is ApiResult.Error)
    }
    
    @Test
    fun `addOwner sends correct request`() = runTest {
        // Given
        val ownerRequest = OwnerRequest(
            firstName = "John",
            lastName = "Doe",
            address = "123 Main St",
            city = "Springfield",
            telephone = "555-1234"
        )
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(201)
        )
        
        // When
        val result = dao.addOwner(ownerRequest)
        
        // Then
        assertTrue(result is ApiResult.Success)
        
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/api/customer/owners", request.path)
        assertTrue(request.body.readUtf8().contains("John"))
        assertTrue(request.body.readUtf8().contains("Doe"))
    }
}
