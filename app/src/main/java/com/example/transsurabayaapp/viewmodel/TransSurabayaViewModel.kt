package com.example.transsurabayaapp.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transsurabayaapp.data.*
import com.example.transsurabayaapp.data.local.TicketEntity
import com.example.transsurabayaapp.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class TransSurabayaViewModel(private val repository: TransSurabayaRepository) : ViewModel() {
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

    private val _paymentProcessing = mutableStateOf(false)
    val paymentProcessing: State<Boolean> = _paymentProcessing

    private val _authError = mutableStateOf<String?>(null)
    val authError: State<String?> = _authError

    private val _loggedInUser = mutableStateOf<UserProfile?>(null)
    val loggedInUser: State<UserProfile?> = _loggedInUser

    private val _showPaymentSuccess = mutableStateOf(false)
    val showPaymentSuccess: State<Boolean> = _showPaymentSuccess

    private fun UserEntity.toUserProfile() = UserProfile(
        id = this.id,
        name = this.name,
        email = this.email,
        password = this.password,
        totalTrips = this.totalTrips,
        freeRideCount = this.freeRideCount
    )

    fun hidePaymentSuccess() {
        _showPaymentSuccess.value = false

        _selectedFromStop.value = null
        _selectedToStop.value = null
        _selectedRoute.value = null
        _paymentProcessing.value = false
    }

    // Dummy data untuk rute dan bus
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
                delay(3000)
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
                !busState.isMoving && busState.stayDuration > 0 -> {
                    busState.stayDuration -= timeDelta
                    if (busState.stayDuration <= 0) {
                        busState.isMoving = true
                        busState.movementProgress = 0f
                    }
                    bus.copy(isMoving = false)
                }
                busState.isMoving -> {
                    busState.movementProgress += (timeDelta / 12000f)
                    if (busState.movementProgress >= 1f) {
                        busState.currentStopIndex = (busState.currentStopIndex + 1) % route.stops.size
                        busState.movementProgress = 0f
                        busState.isMoving = false
                        busState.stayDuration = Random.nextLong(3000, 10000)
                    }
                    bus.copy(
                        currentStopIndex = busState.currentStopIndex,
                        isMoving = true,
                        estimatedArrival = currentTime + Random.nextLong(300000, 1800000)
                    )
                }
                else -> {
                    busState.isMoving = true
                    busState.movementProgress = 0f
                    bus.copy(isMoving = true)
                }
            }
        }
        _buses.value = updatedBuses
    }

    // Fungsi untuk login
    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val userEntity = repository.login(email, password)
            if (userEntity != null) {
                _loggedInUser.value = userEntity.toUserProfile()
                loadTicketsForUser(userEntity.id)
                _authError.value = null
            } else {
                _authError.value = "Email atau password salah."
            }
        }
    }

    // Fungsi untuk register
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newUserEntity = repository.registerUser(name, email, password)
                _loggedInUser.value = newUserEntity.toUserProfile()
                _authError.value = null
            } catch (e: Exception) {
                _authError.value = "Registrasi gagal: ${e.message}"
            }
        }
    }

    fun logout() {
        _loggedInUser.value = null
        _tickets.value = emptyList()
    }

    fun clearAuthError() {
        _authError.value = null
    }

    // Fungsi untuk memuat tiket dari database
    private fun loadTicketsForUser(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTicketsForUser(userId).collect { tickets ->
                _tickets.value = tickets
            }
        }
    }

    // Fungsi untuk membeli tiket
    suspend fun purchaseTicket(fromStop: BusStop, toStop: BusStop, route: BusRoute) {
        Log.d("ViewModel_Purchase", "Attempting to purchase ticket...")
        val currentUser = _loggedInUser.value ?: return
        _paymentProcessing.value = true

        // Simulasi proses pembayaran
        delay(2000)

        val useFreeRide = currentUser.freeRideCount > 0
        val price = if (useFreeRide) 0 else calculatePrice()

        val newTicket = Ticket(
            id = UUID.randomUUID().toString(),
            routeCode = route.code,
            fromStop = fromStop.name,
            toStop = toStop.name,
            price = price,
            purchaseTime = System.currentTimeMillis(),
            isFree = useFreeRide
        )

        // Simpan tiket ke database
        val ticketEntity = TicketEntity(
            id = newTicket.id,
            userId = currentUser.id,
            routeCode = newTicket.routeCode,
            fromStop = newTicket.fromStop,
            toStop = newTicket.toStop,
            price = newTicket.price,
            purchaseTime = newTicket.purchaseTime,
            isFree = newTicket.isFree
        )
        repository.purchaseTicket(ticketEntity)

        // Update user: totalTrips dan freeRideCount
        var updatedTotalTrips = currentUser.totalTrips
        var updatedFreeRideCount = currentUser.freeRideCount

        if (useFreeRide) {
            updatedFreeRideCount--
        } else {
            updatedTotalTrips++
            if (updatedTotalTrips % 10 == 0) {
                updatedFreeRideCount++
            }
        }

        val updatedUser = currentUser.copy(
            totalTrips = updatedTotalTrips,
            freeRideCount = updatedFreeRideCount
        )

        // Update user di database
        val updatedUserEntity = UserEntity(
            id = updatedUser.id,
            name = updatedUser.name,
            email = updatedUser.email,
            password = updatedUser.password,
            totalTrips = updatedUser.totalTrips,
            freeRideCount = updatedUser.freeRideCount
        )
        repository.updateUser(updatedUserEntity)

        // Update state
        _loggedInUser.value = updatedUser
        loadTicketsForUser(updatedUser.id)

        // Tampilkan animasi sukses
        _showPaymentSuccess.value = true
        _paymentProcessing.value = false

    }

    fun calculatePrice(): Int = 5000

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
}