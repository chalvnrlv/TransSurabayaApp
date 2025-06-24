package com.example.transsurabayaapp.data

import androidx.compose.ui.graphics.Color

object DummyData {
    val dummyUsers = listOf(
        UserProfile(
            id = 1,
            name = "John Doe",
            email = "john@example.com",
            password = "password123",
            totalTrips = 7,
            freeRideCount = 3
        ),
        UserProfile(
            id = 2,
            name = "Jane Smith",
            email = "jane@example.com",
            password = "password456",
            totalTrips = 4,
            freeRideCount = 1
        )
    )

    val dummyRoutes = listOf(
        BusRoute(
            code = "SDA",
            name = "Sidoarjo - Ahmad Yani",
            origin = "Terminal Sidoarjo",
            destination = "Terminal Ahmad Yani",
            color = Color(0xFF1976D2),
            stops = listOf(
                BusStop("s1", "Terminal Sidoarjo", -7.4575, 112.7183),
                BusStop("s2", "Pasar Porong", -7.5302, 112.6788),
                BusStop("s3", "Surabaya Town Square", -7.2922, 112.7350),
                BusStop("s4", "Terminal Ahmad Yani", -7.2452, 112.7468)
            ),
            farePerKm = 1500
        ),
        BusRoute(
            code = "PCS",
            name = "Purabaya - Cerme - Sukodono",
            origin = "Terminal Purabaya",
            destination = "Terminal Sukodono",
            color = Color(0xFF4CAF50),
            stops = listOf(
                BusStop("s5", "Terminal Purabaya", -7.3198, 112.7303),
                BusStop("s6", "Cerme", -7.3401, 112.6701),
                BusStop("s7", "Gresik Town Square", -7.1535, 112.6561),
                BusStop("s8", "Terminal Sukodono", -7.3837, 112.6789)
            ),
            farePerKm = 1500
        ),
        BusRoute(
            code = "TBG",
            name = "Tanjungsari - Benowo - Galaxi",
            origin = "Terminal Tanjungsari",
            destination = "Terminal Galaxi",
            color = Color(0xFFFF9800),
            stops = listOf(
                BusStop("s9", "Terminal Tanjungsari", -7.3788, 112.6831),
                BusStop("s10", "Benowo", -7.2578, 112.6114),
                BusStop("s11", "Pakal", -7.2938, 112.6738),
                BusStop("s12", "Terminal Galaxi", -7.2641, 112.7511)
            ),
            farePerKm = 1500
        )
    )

    val dummyTickets = listOf(
        Ticket(
            id = "ticket1",
            userId = 1,
            routeCode = "SDA",
            fromStop = "Terminal Sidoarjo",
            toStop = "Surabaya Town Square",
            price = 4500,
            purchaseTime = System.currentTimeMillis() - 86400000, // 1 hari lalu
            isFree = false
        ),
        Ticket(
            id = "ticket2",
            userId = 1,
            routeCode = "PCS",
            fromStop = "Terminal Purabaya",
            toStop = "Gresik Town Square",
            price = 0,
            purchaseTime = System.currentTimeMillis(),
            isFree = true
        )
    )

    val dummyBuses = listOf(
        Bus(
            id = "bus1",
            routeCode = "SDA",
            currentStopIndex = 0,
            isMoving = false,
            estimatedArrival = System.currentTimeMillis() + 300000
        ),
        Bus(
            id = "bus2",
            routeCode = "SDA",
            currentStopIndex = 2,
            isMoving = true,
            estimatedArrival = System.currentTimeMillis() + 600000
        ),
        Bus(
            id = "bus3",
            routeCode = "PCS",
            currentStopIndex = 1,
            isMoving = false,
            estimatedArrival = System.currentTimeMillis() + 900000
        )
    )
}