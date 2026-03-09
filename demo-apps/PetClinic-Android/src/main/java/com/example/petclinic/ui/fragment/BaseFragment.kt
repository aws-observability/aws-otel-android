package com.example.petclinic.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.petclinic.ui.theme.PetClinicTheme

/**
 * Base Fragment class that provides common functionality for all fragments
 */
abstract class BaseFragment : Fragment() {
    
    /**
     * Override this to provide the Compose content for the fragment
     */
    @Composable
    abstract fun FragmentContent()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PetClinicTheme {
                    FragmentContent()
                }
            }
        }
    }
}
