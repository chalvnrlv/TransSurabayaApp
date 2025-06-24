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

    private val _paymentProcessing = mutableStateOf(false)
    val paymentProcessing: State<Boolean> = _paymentProcessing

    // --- MANAJEMEN PENGGUNA DAN AUTENTIKASI ---

    // Daftar pengguna dummy yang disimpan secara lokal.
    // Di aplikasi nyata, ini akan berasal dari database atau API.
    // UserProfile sekarang harus memiliki `id` dan `password`.
    private val _registeredUsers = mutableStateOf(listOf(
        UserProfile(
            id = "user-1",
            name = "Arek Suroboyo",
            email = "arek@suroboyo.com",
            password = "password123", // Password untuk login dummy
            totalTrips = 7,
            freeRideCount = 0
        )
    ))

    // State untuk menyimpan pengguna yang sedang login (bisa null jika tidak ada)
    private val _loggedInUser = mutableStateOf<UserProfile?>(null)
    val loggedInUser: State<UserProfile?> = _loggedInUser

    // State untuk menampilkan pesan error saat login/register
    private val _authError = mutableStateOf<String?>(null)
    val authError: State<String?> = _authError


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

    // --- FUNGSI BARU UNTUK AUTENTIKASI ---
    fun login(email: String, password: String) {
        val user = _registeredUsers.value.find { it.email.equals(email, ignoreCase = true) && it.password == password }
        if (user != null) {
            _loggedInUser.value = user
            _authError.value = null
        } else {
            _authError.value = "Email atau password salah."
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authError.value = "Semua kolom harus diisi."
            return
        }
        if (_registeredUsers.value.any { it.email.equals(email, ignoreCase = true) }) {
            _authError.value = "Email sudah terdaftar."
            return
        }
        val newUser = UserProfile(
            id = "user-${UUID.randomUUID()}",
            name = name,
            email = email,
            password = password,
            totalTrips = 0,
            freeRideCount = 0
        )
        _registeredUsers.value += newUser
        _loggedInUser.value = newUser // Otomatis login setelah registrasi
        _authError.value = null
    }

    fun logout() {
        _loggedInUser.value = null
    }

    fun clearAuthError() {
        _authError.value = null
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
        return 5000 // Harga tetap
    }

    suspend fun purchaseTicket(fromStop: BusStop, toStop: BusStop, route: BusRoute) {
        val currentUser = _loggedInUser.value ?: return // Keluar jika tidak ada pengguna yang login
        _paymentProcessing.value = true
        delay(2000) // Simulasi proses pembayaran

        val useFreeRide = currentUser.freeRideCount > 0

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

        var updatedTotalTrips = currentUser.totalTrips
        var updatedFreeRideCount = currentUser.freeRideCount

        if (useFreeRide) {
            updatedFreeRideCount--
        } else {
            updatedTotalTrips++
            // Dapatkan 1 tiket gratis setiap 10 perjalanan berbayar
            if (updatedTotalTrips > 0 && updatedTotalTrips % 10 == 0) {
                updatedFreeRideCount++
            }
        }

        // Perbarui objek pengguna
        val updatedUser = currentUser.copy(
            totalTrips = updatedTotalTrips,
            freeRideCount = updatedFreeRideCount
        )
        _loggedInUser.value = updatedUser

        // Perbarui juga pengguna di dalam daftar _registeredUsers
        val userIndex = _registeredUsers.value.indexOfFirst { it.id == currentUser.id }
        if (userIndex != -1) {
            val updatedList = _registeredUsers.value.toMutableList()
            updatedList[userIndex] = updatedUser
            _registeredUsers.value = updatedList
        }

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