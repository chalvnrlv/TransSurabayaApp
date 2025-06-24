package com.example.transsurabayaapp.ui.screens.map

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.transsurabayaapp.data.Bus
import com.example.transsurabayaapp.ui.icon.LocalDirectionsBus
import com.example.transsurabayaapp.viewmodel.TransSurabayaViewModel
import kotlin.math.*

// Data class untuk menyimpan bounds koordinat
private data class MapBounds(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)

@SuppressLint("UseOfNonLambdaOffsetOverload")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: TransSurabayaViewModel) {
    val selectedRoute by viewModel.selectedRoute
    val allRoutes = viewModel.routes
    val buses by viewModel.buses
    var showDialog by remember { mutableStateOf<Bus?>(null) }

    // State untuk menyimpan posisi bus yang diinterpolasi
    val busPositions = remember { mutableStateMapOf<String, Offset>() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    selectedRoute?.name ?: "Peta Semua Rute",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                if (selectedRoute != null) {
                    IconButton(onClick = { viewModel.deselectRoute() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Tutup Rute",
                            tint = Color.White
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = selectedRoute?.color ?: Color(0xFF1976D2)
            )
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9))
        ) {
            // Hitung bounds dari semua koordinat
            val mapBounds = remember {
                val allStops = allRoutes.flatMap { it.stops }
                if (allStops.isNotEmpty()) {
                    MapBounds(
                        minLat = allStops.minOf { it.latitude },
                        maxLat = allStops.maxOf { it.latitude },
                        minLon = allStops.minOf { it.longitude },
                        maxLon = allStops.maxOf { it.longitude }
                    )
                } else {
                    MapBounds(-7.4, -7.2, 112.6, 112.8) // Default bounds untuk Surabaya
                }
            }

            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val padding = 40.dp.value

            // Fungsi untuk mengkonversi koordinat geografis ke pixel
            fun getPixelOffset(lat: Double, lon: Double): Offset {
                val latRange = mapBounds.maxLat - mapBounds.minLat
                val lonRange = mapBounds.maxLon - mapBounds.minLon

                // Tambahkan padding agar tidak terpotong di tepi
                val paddingFactor = 0.1
                val adjustedLatRange = latRange * (1 + paddingFactor)
                val adjustedLonRange = lonRange * (1 + paddingFactor)

                val centerLat = (mapBounds.maxLat + mapBounds.minLat) / 2
                val centerLon = (mapBounds.maxLon + mapBounds.minLon) / 2

                val x = ((lon - centerLon) / adjustedLonRange + 0.5) * (width - 2 * padding) + padding
                val y = ((centerLat - lat) / adjustedLatRange + 0.5) * (height - 2 * padding) + padding

                return Offset(x.toFloat(), y.toFloat())
            }

            // Fungsi untuk interpolasi posisi antara dua titik
            fun interpolatePosition(startLat: Double, startLon: Double,
                                    endLat: Double, endLon: Double,
                                    progress: Float): Offset {
                val lat = startLat + (endLat - startLat) * progress
                val lon = startLon + (endLon - startLon) * progress
                return getPixelOffset(lat, lon)
            }

            // Canvas untuk menggambar rute
            Canvas(modifier = Modifier.fillMaxSize()) {
                val routesToDraw = if (selectedRoute != null) listOf(selectedRoute!!) else allRoutes

                routesToDraw.forEach { route ->
                    val pathEffect = if (selectedRoute != null && route.code == selectedRoute!!.code) {
                        null
                    } else {
                        PathEffect.dashPathEffect(floatArrayOf(10f, 20f), 0f)
                    }

                    // Gambar garis rute
                    for (i in 0 until route.stops.size - 1) {
                        val start = getPixelOffset(route.stops[i].latitude, route.stops[i].longitude)
                        val end = getPixelOffset(route.stops[i + 1].latitude, route.stops[i + 1].longitude)
                        drawLine(
                            color = route.color.copy(alpha = 0.6f),
                            start = start,
                            end = end,
                            strokeWidth = if (selectedRoute?.code == route.code) 6.dp.toPx() else 4.dp.toPx(),
                            pathEffect = pathEffect
                        )
                    }
                }
            }

            // Overlay untuk halte dan bus
            Box(modifier = Modifier.fillMaxSize()) {
                val routesToDisplay = if (selectedRoute != null) listOf(selectedRoute!!) else allRoutes

                // Gambar halte
                routesToDisplay.forEach { route ->
                    route.stops.forEach { stop ->
                        val offset = getPixelOffset(stop.latitude, stop.longitude)
                        Box(
                            modifier = Modifier
                                .offset(offset.x.dp - 8.dp, offset.y.dp - 8.dp)
                                .size(16.dp)
                                .background(Color.White, CircleShape)
                                .border(3.dp, route.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(route.color, CircleShape)
                            )
                        }
                    }
                }

                // Gambar bus dengan animasi yang lebih smooth
                val busesToDisplay = if (selectedRoute != null) {
                    buses.filter { it.routeCode == selectedRoute!!.code }
                } else {
                    buses
                }

                busesToDisplay.forEach { bus ->
                    val route = allRoutes.find { it.code == bus.routeCode }
                    if (route != null && route.stops.isNotEmpty()) {
                        val currentStopIndex = bus.currentStopIndex.coerceIn(0, route.stops.size - 1)
                        val nextStopIndex = if (currentStopIndex < route.stops.size - 1) {
                            currentStopIndex + 1
                        } else {
                            0 // Loop kembali ke awal
                        }

                        val currentStop = route.stops[currentStopIndex]
                        val nextStop = route.stops[nextStopIndex]

                        // Hitung posisi target berdasarkan apakah bus sedang bergerak
                        val targetPosition = if (bus.isMoving) {
                            // Jika bergerak, interpolasi antara halte saat ini dan berikutnya
                            val progress = 0.3f // Bisa disesuaikan atau dibuat dinamis
                            interpolatePosition(
                                currentStop.latitude, currentStop.longitude,
                                nextStop.latitude, nextStop.longitude,
                                progress
                            )
                        } else {
                            // Jika berhenti, posisi di halte saat ini
                            getPixelOffset(currentStop.latitude, currentStop.longitude)
                        }

                        // Inisialisasi posisi bus jika belum ada
                        if (!busPositions.containsKey(bus.id)) {
                            busPositions[bus.id] = getPixelOffset(currentStop.latitude, currentStop.longitude)
                        }

                        // Animasi pergerakan bus
                        val animatedOffset by animateOffsetAsState(
                            targetValue = targetPosition,
                            animationSpec = tween(
                                durationMillis = if (bus.isMoving) 8000 else 2000, // Lebih lambat saat bergerak
                                delayMillis = 0
                            ),
                            label = "bus_movement_${bus.id}"
                        )

                        // Update posisi bus
                        LaunchedEffect(animatedOffset) {
                            busPositions[bus.id] = animatedOffset
                        }

                        // Gambar bus
                        Box(
                            modifier = Modifier
                                .offset(
                                    (animatedOffset.x).dp - 16.dp,
                                    (animatedOffset.y).dp - 16.dp
                                )
                                .size(32.dp)
                                .background(
                                    route.color.copy(alpha = if (bus.isMoving) 1f else 0.7f),
                                    CircleShape
                                )
                                .border(3.dp, Color.White, CircleShape)
                                .clickable { showDialog = bus }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                LocalDirectionsBus,
                                contentDescription = "Bus ${bus.id}",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Indikator status bus (opsional)
                        if (bus.isMoving) {
                            Box(
                                modifier = Modifier
                                    .offset(
                                        (animatedOffset.x).dp - 2.dp,
                                        (animatedOffset.y).dp - 20.dp
                                    )
                                    .size(4.dp)
                                    .background(Color.Green, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        // Dialog informasi bus
        if (showDialog != null) {
            val bus = showDialog!!
            val route = allRoutes.find { it.code == bus.routeCode }
            val currentStop = route?.stops?.getOrNull(bus.currentStopIndex)
            val nextStop = route?.stops?.getOrNull(
                if (bus.currentStopIndex < (route.stops.size - 1)) {
                    bus.currentStopIndex + 1
                } else {
                    0
                }
            )

            Dialog(onDismissRequest = { showDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = route?.color ?: MaterialTheme.colorScheme.surface
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Bus ${bus.id}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Rute: ${route?.name}",
                            color = Color.White.copy(0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (currentStop != null) {
                            Text(
                                "Posisi: ${currentStop.name}",
                                fontSize = 12.sp,
                                color = Color.White.copy(0.8f)
                            )
                        }

                        if (nextStop != null) {
                            Text(
                                if (bus.isMoving) "Menuju: ${nextStop.name}" else "Berhenti di halte",
                                fontSize = 12.sp,
                                color = Color.White.copy(0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text(
                                "Tutup",
                                color = route?.color ?: MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}