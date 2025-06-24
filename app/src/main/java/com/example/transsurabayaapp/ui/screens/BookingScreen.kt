package com.example.transsurabayaapp.ui.screens.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.transsurabayaapp.data.BusStop
import com.example.transsurabayaapp.ui.icon.LocalCardGiftcard
import com.example.transsurabayaapp.ui.icon.LocalPayment
import com.example.transsurabayaapp.viewmodel.TransSurabayaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(viewModel: TransSurabayaViewModel, navController: NavController) {
    val selectedRoute by viewModel.selectedRoute
    val selectedFromStop by viewModel.selectedFromStop
    val selectedToStop by viewModel.selectedToStop
    val paymentProcessing by viewModel.paymentProcessing
    val userProfile by viewModel.userProfile
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Beli Tiket", fontWeight = FontWeight.Bold, color = Color.White) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = selectedRoute?.color ?: Color(0xFF1976D2))
        )

        selectedRoute?.let { route ->
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = route.color.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rute: ${route.code} - ${route.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${route.origin} → ${route.destination}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                            }
                        }
                    }

                    if (userProfile.freeRideCount > 0) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(LocalCardGiftcard, contentDescription = "Reward", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("🎉 Tiket Gratis Tersedia!", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                        Text("Anda memiliki ${userProfile.freeRideCount} tiket gratis", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }

                    item { Text("Pilih Halte Asal", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    items(route.stops) { stop ->
                        StopSelectionCard(stop = stop, isSelected = selectedFromStop == stop, onClick = { viewModel.selectFromStop(stop) }, routeColor = route.color, estimatedTime = viewModel.getEstimatedArrival(route, stop.id))
                    }

                    if (selectedFromStop != null) {
                        item { Text("Pilih Halte Tujuan", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        items(route.stops.filter { it.id != selectedFromStop!!.id }) { stop ->
                            StopSelectionCard(stop = stop, isSelected = selectedToStop == stop, onClick = { viewModel.selectToStop(stop) }, routeColor = route.color, estimatedTime = "N/A")
                        }
                    }
                }

                if (selectedFromStop != null && selectedToStop != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Harga:")
                                Text(if (userProfile.freeRideCount > 0) "GRATIS" else "Rp ${viewModel.calculatePrice()}", fontWeight = FontWeight.Bold, color = route.color, fontSize = 18.sp)
                            }
                            Button(
                                onClick = {
                                    if (!paymentProcessing) {
                                        coroutineScope.launch {
                                            viewModel.purchaseTicket(selectedFromStop!!, selectedToStop!!, route)
                                            // Navigate to tickets screen after purchase
                                            navController.navigate("tickets") {
                                                // Clear back stack up to home
                                                popUpTo("home")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = route.color),
                                enabled = !paymentProcessing,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (paymentProcessing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Memproses...")
                                } else {
                                    Icon(LocalPayment, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (userProfile.freeRideCount > 0) "Gunakan Tiket Gratis" else "Bayar Sekarang", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopSelectionCard(stop: BusStop, isSelected: Boolean, onClick: () -> Unit, routeColor: Color, estimatedTime: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) routeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = if (isSelected) BorderStroke(2.dp, routeColor) else BorderStroke(1.dp, Color.Transparent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stop.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                if (estimatedTime != "N/A"){
                    Text("Estimasi: $estimatedTime", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Dipilih", tint = routeColor, modifier = Modifier.size(24.dp))
            }
        }
    }
}