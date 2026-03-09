package com.example.petclinic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import com.example.petclinic.ui.components.BottomNavigationBar
import com.example.petclinic.ui.fragment.BottomNavigationFragment
import com.example.petclinic.ui.theme.PetClinicTheme

/**
 * Base activity that provides common functionality for all activities
 */
abstract class BaseActivity : FragmentActivity() {
    
    /**
     * Override this to provide the content for the activity
     */
    @Composable
    abstract fun ActivityContent()
    
    /**
     * Override this to specify which bottom nav item should be selected
     * Return null if this activity shouldn't show bottom navigation
     */
    abstract fun getSelectedBottomNavRoute(): String?
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            PetClinicTheme {
                val selectedRoute = getSelectedBottomNavRoute()
                
                Scaffold(
                    bottomBar = {
                        selectedRoute?.let { route ->
                            BottomNavigationBar(
                                selectedRoute = route,
                                onNavigate = { destination ->
                                    navigateToActivity(destination)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ActivityContent()
                    }
                }
            }
        }
    }
    
    private fun navigateToActivity(route: String) {
        val intent = when (route) {
            "home" -> Intent(this, MainActivity::class.java)
            "owners" -> Intent(this, OwnersActivity::class.java)
            "vets" -> Intent(this, VetsActivity::class.java)
            else -> return
        }
        
        if (intent.component?.className != this::class.java.name) {
            startActivity(intent)
        }
    }
}
