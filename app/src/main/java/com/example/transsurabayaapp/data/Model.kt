package com.example.transsurabayaapp.data

import androidx.compose.ui.graphics.Color

data class BusRoute(
    val code: String,
    val name: String,
    val origin: String,
    val destination: String,
    val color: Color,
    val stops: List<BusStop>
)

data class BusStop(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

data class Bus(
    val id: String,
    val routeCode: String,
    var currentStopIndex: Int,
    var isMoving: Boolean,
    var estimatedArrival: Long
)

data class Ticket(
    val id: String,
    val routeCode: String,
    val fromStop: String,
    val toStop: String,
    val price: Int,
    val purchaseTime: Long,
    val isUsed: Boolean = false,
    val isFree: Boolean = false
)

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val totalTrips: Int,
    val freeRideCount: Int
)