package com.example.petclinic

import androidx.compose.runtime.Composable
import com.example.petclinic.ui.screen.VetsScreen

/**
 * Activity that displays the veterinarians list
 */
class VetsActivity : BaseActivity() {
    
    @Composable
    override fun ActivityContent() {
        VetsScreen()
    }
    
    override fun getSelectedBottomNavRoute(): String = "vets"
}
