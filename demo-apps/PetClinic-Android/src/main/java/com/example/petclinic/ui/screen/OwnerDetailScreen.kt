package com.example.petclinic.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petclinic.data.model.Owner
import com.example.petclinic.data.model.Pet
import com.example.petclinic.data.network.ApiResult
import com.example.petclinic.ui.components.AddPetDialog
import com.example.petclinic.ui.components.EditOwnerDialog
import com.example.petclinic.ui.viewmodel.OwnerDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailScreen(
    ownerId: Int,
    onNavigateBack: () -> Unit,
    viewModel: OwnerDetailViewModel = viewModel()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    
    LaunchedEffect(ownerId) {
        viewModel.loadOwnerWithVisits(ownerId)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Owner Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Owner")
                }
            }
        )
        
        // Content
        when (val state = viewModel.ownerState) {
            is ApiResult.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading owner details...")
                    }
                }
            }
            is ApiResult.Success -> {
                val owner = state.data
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OwnerInfoCard(owner = owner)
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pets (${owner.pets.size})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            FloatingActionButton(
                                onClick = { showAddPetDialog = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Pet")
                            }
                        }
                    }
                    
                    if (owner.pets.isEmpty()) {
                        item {
                            Text(
                                text = "No pets registered for this owner",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(owner.pets) { pet ->
                            PetCard(
                                pet = pet,
                                nutritionFacts = viewModel.nutritionFactsMap[pet.id] ?: "",
                                isLoadingNutrition = viewModel.nutritionLoadingMap[pet.id] ?: false
                            )
                        }
                    }
                }
            }
            is ApiResult.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error Loading Owner",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.exception.message ?: "Unknown error",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadOwnerWithVisits(ownerId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            null -> {
                // Initial state
            }
        }
    }
    
    // Edit Owner Dialog
    if (showEditDialog && viewModel.ownerState is ApiResult.Success) {
        EditOwnerDialog(
            owner = (viewModel.ownerState as ApiResult.Success).data,
            onDismiss = { 
                if (!isUpdating) {
                    showEditDialog = false 
                }
            },
            onSave = { ownerRequest ->
                isUpdating = true
                viewModel.updateOwner(
                    ownerId = ownerId,
                    ownerRequest = ownerRequest,
                    onSuccess = { 
                        isUpdating = false
                        showEditDialog = false
                    },
                    onError = { error ->
                        isUpdating = false
                        errorMessage = error
                        showErrorDialog = true
                    }
                )
            },
            isLoading = isUpdating
        )
    }
    
    // Add Pet Dialog
    if (showAddPetDialog) {
        AddPetDialog(
            petTypes = (viewModel.petTypesState as? ApiResult.Success)?.data ?: emptyList(),
            onDismiss = { showAddPetDialog = false },
            onSave = { petRequest ->
                viewModel.addPet(
                    ownerId = ownerId,
                    petRequest = petRequest,
                    onSuccess = { showAddPetDialog = false },
                    onError = { /* Handle error */ }
                )
            }
        )
    }
    
    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Update Failed") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun OwnerInfoCard(owner: Owner) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${owner.firstName} ${owner.lastName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Address: ${owner.address}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "City: ${owner.city}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Phone: ${owner.telephone}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    nutritionFacts: String = "",
    isLoadingNutrition: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = pet.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Type: ${pet.type.name.replaceFirstChar { it.uppercase() }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Birth Date: ${pet.birthDate}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (pet.visits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Visits: ${pet.visits.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Nutrition Facts Section
            if (isLoadingNutrition) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loading nutrition...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (nutritionFacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nutrition Facts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = nutritionFacts,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
