package com.example.petclinic

import androidx.compose.runtime.Composable
import com.example.petclinic.ui.screen.AddOwnerScreen

/**
 * Activity that displays the add owner form
 */
class AddOwnerActivity : BaseActivity() {
    
    @Composable
    override fun ActivityContent() {
        AddOwnerScreen(
            onNavigateBack = {
                finish() // Close this activity and return to previous one
            }
        )
    }
    
    override fun getSelectedBottomNavRoute(): String? = null // No bottom nav for form screens
}
