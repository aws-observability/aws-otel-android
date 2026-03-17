package com.example.petclinic.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Default.Home),
    BottomNavItem("owners", "Owners", Icons.Default.People),
    BottomNavItem("vets", "Veterinarians", Icons.Default.Person)
)

@Composable
fun BottomNavigationBar(
    selectedRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
