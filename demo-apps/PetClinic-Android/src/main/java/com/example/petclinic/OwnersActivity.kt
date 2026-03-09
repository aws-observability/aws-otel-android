package com.example.petclinic

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.petclinic.ui.fragment.AddOwnerFragment
import com.example.petclinic.ui.screen.OwnersScreen

/**
 * Activity that displays the owners list
 */
class OwnersActivity : BaseActivity() {
    
    // State to trigger recomposition when activity resumes
    private var refreshTrigger by mutableStateOf(0)
    private var isFirstResume = true
    
    override fun onResume() {
        super.onResume()
        // Skip first resume (initial load) - let ViewModel init handle it
        if (isFirstResume) {
            isFirstResume = false
        } else {
            // Only increment on subsequent resumes (when returning from other screens)
            refreshTrigger++
        }
    }
    
    @Composable
    override fun ActivityContent() {
        // Pass refreshTrigger to force recomposition
        OwnersScreen(
            onOwnerClick = { ownerId ->
                val intent = Intent(this, OwnerDetailActivity::class.java)
                intent.putExtra("ownerId", ownerId)
                startActivity(intent)
            },
            onAddOwnerClick = {
                supportFragmentManager.beginTransaction()
                    .replace(android.R.id.content, AddOwnerFragment())
                    .addToBackStack(null)
                    .commit()
            },
            refreshTrigger = refreshTrigger
        )
    }
    
    override fun getSelectedBottomNavRoute(): String = "owners"
}
