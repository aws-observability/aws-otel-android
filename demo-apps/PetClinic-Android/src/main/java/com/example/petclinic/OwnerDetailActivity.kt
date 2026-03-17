package com.example.petclinic

import androidx.compose.runtime.Composable
import com.example.petclinic.ui.screen.OwnerDetailScreen

/**
 * Activity that displays owner details
 */
class OwnerDetailActivity : BaseActivity() {
    
    @Composable
    override fun ActivityContent() {
        val ownerId = intent.getIntExtra("ownerId", 0)
        
        OwnerDetailScreen(
            ownerId = ownerId,
            onNavigateBack = {
                finish() // Close this activity and return to previous one
            }
        )
    }
    
    override fun getSelectedBottomNavRoute(): String? = null // No bottom nav for detail screens
}
