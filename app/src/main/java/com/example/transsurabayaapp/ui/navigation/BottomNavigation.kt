package com.example.transsurabayaapp.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.transsurabayaapp.ui.icon.LocalConfirmationNumber
import com.example.transsurabayaapp.ui.icon.LocalHome
import com.example.transsurabayaapp.ui.icon.LocalMap

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem("Beranda", "home", LocalHome),
        BottomNavItem("Peta", "map", LocalMap),
        BottomNavItem("Tiket", "tickets", LocalConfirmationNumber)
    )
}