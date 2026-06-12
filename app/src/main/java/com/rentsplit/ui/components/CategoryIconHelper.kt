package com.rentsplit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    val standardIcons = listOf(
        "Home",
        "ElectricBolt",
        "WaterDrop",
        "Wifi",
        "ShoppingCart",
        "LocalGasStation",
        "Build",
        "Category",
        "Restaurant",
        "DirectionsCar",
        "Flight",
        "Movie",
        "School",
        "FitnessCenter",
        "MedicalServices",
        "Brush"
    )

    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "Home" -> Icons.Default.Home
            "ElectricBolt" -> Icons.Default.ElectricBolt
            "WaterDrop" -> Icons.Default.WaterDrop
            "Wifi" -> Icons.Default.Wifi
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "Build" -> Icons.Default.Build
            "Category" -> Icons.Default.Category
            "Restaurant" -> Icons.Default.Restaurant
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "Flight" -> Icons.Default.Flight
            "Movie" -> Icons.Default.Movie
            "School" -> Icons.Default.School
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "MedicalServices" -> Icons.Default.MedicalServices
            "Brush" -> Icons.Default.Brush
            else -> Icons.Default.Category
        }
    }
}
