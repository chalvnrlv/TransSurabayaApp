package com.example.transsurabayaapp.ui.screens.routedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.transsurabayaapp.data.BusStop
import com.example.transsurabayaapp.ui.icon.LocalConfirmationNumber
import com.example.transsurabayaapp.ui.icon.LocalDirectionsBus
import com.example.transsurabayaapp.ui.icon.LocalMap
import com.example.transsurabayaapp.viewmodel.TransSurabayaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(viewModel: TransSurabayaViewModel, navController: NavController) {
    val selectedRoute by viewModel.selectedRoute
    val buses by viewModel.buses

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    selectedRoute?.name ?: "Detail Rute",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = selectedRoute?.color ?: Color(0xFF1976D2)
            )
        )

        selectedRoute?.let { route ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = route.color.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Informasi Rute", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kode: ${route.code}")
                            Text("Asal: ${route.origin}")
                            Text("Tujuan: ${route.destination}")
                            Text("Jumlah Halte: ${route.stops.size}")
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("booking") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = route.color)
                        ) {
                            Icon(LocalConfirmationNumber, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Beli Tiket")
                        }
                        OutlinedButton(
                            onClick = {
                                // Pastikan rute sudah terpilih sebelum pindah ke map
                                viewModel.selectRoute(route)
                                navController.navigate("map")
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, route.color)
                        ) {
                            Icon(LocalMap, contentDescription = null, tint = route.color)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lihat Peta", color = route.color)
                        }
                    }
                }

                item {
                    Text("Daftar Halte", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                items(route.stops.size) { index ->
                    val stop = route.stops[index]
                    val routeBuses = buses.filter { it.routeCode == route.code }
                    val busAtStop = routeBuses.find { it.currentStopIndex == index && !it.isMoving }

                    BusStopItem(
                        stop = stop,
                        isFirst = index == 0,
                        isLast = index == route.stops.size - 1,
                        hasBus = busAtStop != null,
                        estimatedTime = viewModel.getEstimatedArrival(route, stop.id),
                        routeColor = route.color
                    )
                }
            }
        }
    }
}

@Composable
fun BusStopItem(
    stop: BusStop,
    isFirst: Boolean,
    isLast: Boolean,
    hasBus: Boolean,
    estimatedTime: String,
    routeColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(if(isFirst) Color.Transparent else routeColor.copy(alpha = 0.3f))
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(width = 3.dp, color = if (hasBus) routeColor else routeColor.copy(alpha = 0.3f), shape = CircleShape)
                    .padding(4.dp)
                    .background(if (hasBus) routeColor else Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (hasBus) {
                    Icon(LocalDirectionsBus, contentDescription = "Bus di halte", modifier = Modifier.size(12.dp), tint = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else routeColor.copy(alpha = 0.3f))
            )
        }

        Column(modifier = Modifier.padding(start = 8.dp).fillMaxHeight()) {
            Text(stop.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            if (hasBus) {
                Text("Bus telah tiba", color = routeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else if (estimatedTime != "N/A"){
                Text("Estimasi kedatangan: $estimatedTime", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}