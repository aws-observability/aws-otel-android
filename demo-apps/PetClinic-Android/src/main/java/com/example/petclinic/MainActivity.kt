package com.example.petclinic

import androidx.compose.runtime.Composable
import com.example.petclinic.ui.screen.HomeScreen

/**
 * Main activity that displays the home screen
 */
class MainActivity : BaseActivity() {
    @Composable
    override fun ActivityContent() {
        HomeScreen()
    }
    
    override fun getSelectedBottomNavRoute(): String = "home"
}
