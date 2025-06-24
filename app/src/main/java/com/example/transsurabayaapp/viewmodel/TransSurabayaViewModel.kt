package com.example.transsurabayaapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transsurabayaapp.data.Bus
import com.example.transsurabayaapp.data.BusRoute
import com.example.transsurabayaapp.data.BusStop
import com.example.transsurabayaapp.data.Ticket
import com.example.transsurabayaapp.data.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class TransSurabayaViewModel : ViewModel() {
    private val _selectedRoute = mutableStateOf<BusRoute?>(null)
    val selectedRoute: State<BusRoute?> = _selectedRoute

    private val _selectedFromStop = mutableStateOf<BusStop?>(null)
    val selectedFromStop: State<BusStop?> = _selectedFromStop

    private val _selectedToStop = mutableStateOf<BusStop?>(null)
    val selectedToStop: State<BusStop?> = _selectedToStop

    private val _buses = mutableStateOf<List<Bus>>(emptyList())
    val buses: State<List<Bus>> = _buses

    private val _tickets = mutableStateOf<List<Ticket>>(emptyList())
    val tickets: State<List<Ticket>> = _tickets

    private val _userProfile = mutableStateOf(
        UserProfile(
            name = "Arek Suroboyo",
            email = "arek@suroboyo.com",
            totalTrips = 7,
            freeRideCount = 0
        )
    )
    val userProfile: State<UserProfile> = _userProfile

    private val _paymentProcessing = mutableStateOf(false)
    val paymentProcessing: State<Boolean> = _paymentProcessing

    // Dummy Data dengan koordinat yang lebih realistis untuk Surabaya
    val routes = listOf(
        BusRoute(
            code = "R1",
            name = "Purabaya - Rajawali",
            origin = "Purabaya",
            destination = "Rajawali",
            color = Color(0xFFD32F2F),
            stops = listOf(
                BusStop("R1-S1", "Terminal Purabaya", -7.3575, 112.7421),
                BusStop("R1-S2", "Halte UINSA", -7.3320, 112.7415),
                BusStop("R1-S3", "Halte Royal Plaza", -7.3090, 112.7380),
                BusStop("R1-S4", "Halte Joyoboyo", -7.2990, 112.7375),
                BusStop("R1-S5", "Halte Tunjungan Plaza", -7.2610, 112.7400),
                BusStop("R1-S6", "Halte Siola", -7.2550, 112.7390),
                BusStop("R1-S7", "Halte JMP", -7.2380, 112.7350),
                BusStop("R1-S8", "Halte Rajawali", -7.2320, 112.7340)
            )
        ),
        BusRoute(
            code = "R2",
            name = "Kenjeran - Unesa",
            origin = "Kenjeran",
            destination = "Unesa",
            color = Color(0xFF388E3C),
            stops = listOf(
                BusStop("R2-S1", "Halte Kenjeran Park", -7.2500, 112.7900),
                BusStop("R2-S2", "Halte Galaxy Mall", -7.2780, 112.7850),
                BusStop("R2-S3", "Halte Kertajaya", -7.2850, 112.7650),
                BusStop("R2-S4", "Stasiun Gubeng", -7.2653, 112.7520),
                BusStop("R2-S5", "Halte Darmo", -7.2850, 112.7400),
                BusStop("R2-S6", "Halte TVRI", -7.2950, 112.7150),
                BusStop("R2-S7", "Halte Unesa", -7.3100, 112.7050)
            )
        ),
        BusRoute(
            code = "R3",
            name = "Osowilangun - Gunung Anyar",
            origin = "Osowilangun",
            destination = "Gunung Anyar",
            color = Color(0xFFFFA000),
            stops = listOf(
                BusStop("R3-S1", "Terminal Osowilangun", -7.2183, 112.6713),
                BusStop("R3-S2", "Halte Margomulyo", -7.2400, 112.6900),
                BusStop("R3-S3", "Pasar Turi", -7.2433, 112.7307),
                BusStop("R3-S4", "Halte Embong Malang", -7.2600, 112.7350),
                BusStop("R3-S5", "Halte Basuki Rahmat", -7.2680, 112.7420),
                BusStop("R3-S6", "Halte Jemursari", -7.3200, 112.7500),
                BusStop("R3-S7", "Halte Rungkut Industri", -7.3350, 112.7680),
                BusStop("R3-S8", "Halte Gunung Anyar", -7.3380, 112.7950)
            )
        )
    )

    // State untuk tracking pergerakan bus individual
    private val busStates = mutableMapOf<String, BusMovementState>()

    private data class BusMovementState(
        var currentStopIndex: Int = 0,
        var isMoving: Boolean = false,
        var movementProgress: Float = 0f,
        var stayDuration: Long = 0L,
        var lastUpdateTime: Long = System.currentTimeMillis()
    )

    init {
        generateDummyBuses()
        startBusSimulation()
    }

    private fun generateDummyBuses() {
        val busList = mutableListOf<Bus>()
        routes.forEach { route ->
            repeat(3) { i ->
                val busId = "${route.code}-B${i + 1}"
                val randomStopIndex = Random.nextInt(0, route.stops.size)

                busList.add(
                    Bus(
                        id = busId,
                        routeCode = route.code,
                        currentStopIndex = randomStopIndex,
                        isMoving = Random.nextBoolean(),
                        estimatedArrival = System.currentTimeMillis() + Random.nextLong(300000, 1800000)
                    )
                )

                // Initialize movement state
                busStates[busId] = BusMovementState(
                    currentStopIndex = randomStopIndex,
                    isMoving = Random.nextBoolean(),
                    stayDuration = if (Random.nextBoolean()) Random.nextLong(5000, 15000) else 0L
                )
            }
        }
        _buses.value = busList
    }

    private fun startBusSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(3000) // Update setiap 3 detik untuk simulasi yang lebih smooth
                updateBusPositions()
            }
        }
    }

    private fun updateBusPositions() {
        val currentTime = System.currentTimeMillis()
        val updatedBuses = _buses.value.map { bus ->
            val route = routes.find { it.code == bus.routeCode } ?: return@map bus
            val busState = busStates[bus.id] ?: return@map bus

            val timeDelta = currentTime - busState.lastUpdateTime
            busState.lastUpdateTime = currentTime

            when {
                // Bus sedang berhenti di halte
                !busState.isMoving && busState.stayDuration > 0 -> {
                    busState.stayDuration -= timeDelta
                    if (busState.stayDuration <= 0) {
                        // Mulai bergerak setelah berhenti cukup lama
                        busState.isMoving = true
                        busState.movementProgress = 0f
                    }
                    bus.copy(isMoving = false)
                }

                // Bus sedang bergerak
                busState.isMoving -> {
                    busState.movementProgress += (timeDelta / 12000f) // 12 detik untuk satu segmen

                    if (busState.movementProgress >= 1f) {
                        // Sampai di halte berikutnya
                        busState.currentStopIndex = (busState.currentStopIndex + 1) % route.stops.size
                        busState.movementProgress = 0f
                        busState.isMoving = false
                        busState.stayDuration = Random.nextLong(3000, 10000) // Berhenti 3-10 detik
                    }

                    bus.copy(
                        currentStopIndex = busState.currentStopIndex,
                        isMoving = true,
                        estimatedArrival = currentTime + Random.nextLong(300000, 1800000)
                    )
                }

                // Bus mulai bergerak
                else -> {
                    busState.isMoving = true
                    busState.movementProgress = 0f
                    bus.copy(isMoving = true)
                }
            }
        }

        _buses.value = updatedBuses
    }

    fun selectRoute(route: BusRoute) {
        _selectedRoute.value = route
        _selectedFromStop.value = null
        _selectedToStop.value = null
    }

    fun deselectRoute() {
        _selectedRoute.value = null
    }

    fun selectFromStop(stop: BusStop) {
        _selectedFromStop.value = stop
    }

    fun selectToStop(stop: BusStop) {
        _selectedToStop.value = stop
    }

    fun calculatePrice(): Int {
        return 5000 // Fixed price
    }

    suspend fun purchaseTicket(fromStop: BusStop, toStop: BusStop, route: BusRoute) {
        _paymentProcessing.value = true
        delay(2000) // Simulate payment processing

        val currentFreeRides = _userProfile.value.freeRideCount
        val useFreeRide = currentFreeRides > 0

        val newTicket = Ticket(
            id = UUID.randomUUID().toString(),
            routeCode = route.code,
            fromStop = fromStop.name,
            toStop = toStop.name,
            price = if (useFreeRide) 0 else calculatePrice(),
            purchaseTime = System.currentTimeMillis(),
            isFree = useFreeRide
        )

        _tickets.value += newTicket

        var updatedTotalPaidTrips = _userProfile.value.totalTrips
        var updatedFreeRideCount = _userProfile.value.freeRideCount

        if (useFreeRide) {
            updatedFreeRideCount -= 1
        } else {
            updatedTotalPaidTrips += 1
            if (updatedTotalPaidTrips > 0 && updatedTotalPaidTrips % 10 == 0) {
                updatedFreeRideCount += 1
            }
        }

        _userProfile.value = _userProfile.value.copy(
            totalTrips = updatedTotalPaidTrips,
            freeRideCount = updatedFreeRideCount
        )

        _paymentProcessing.value = false
    }

    fun getEstimatedArrival(route: BusRoute, stopId: String): String {
        val busesOnRoute = _buses.value.filter { it.routeCode == route.code }
        if (busesOnRoute.isEmpty()) return "N/A"

        val targetStopIndex = route.stops.indexOfFirst { it.id == stopId }
        if (targetStopIndex == -1) return "N/A"

        val nearestBus = busesOnRoute.minByOrNull { bus ->
            val stopDifference = if (bus.currentStopIndex <= targetStopIndex) {
                targetStopIndex - bus.currentStopIndex
            } else {
                (route.stops.size - bus.currentStopIndex) + targetStopIndex
            }
            stopDifference
        }

        return if (nearestBus != null) {
            val stopDifference = if (nearestBus.currentStopIndex <= targetStopIndex) {
                targetStopIndex - nearestBus.currentStopIndex
            } else {
                (route.stops.size - nearestBus.currentStopIndex) + targetStopIndex
            }

            val estimatedMinutes = stopDifference * 3 + Random.nextInt(0, 3)
            if (estimatedMinutes == 0) "Tiba" else "$estimatedMinutes menit"
        } else {
            "N/A"
        }
    }
}