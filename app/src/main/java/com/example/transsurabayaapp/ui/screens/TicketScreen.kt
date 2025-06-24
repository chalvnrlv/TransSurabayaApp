package com.example.transsurabayaapp.ui.screens.tickets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.transsurabayaapp.data.Ticket
import com.example.transsurabayaapp.ui.icon.LocalConfirmationNumber
import com.example.transsurabayaapp.ui.icon.LocalFlag
import com.example.transsurabayaapp.viewmodel.TransSurabayaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(viewModel: TransSurabayaViewModel) {
    val tickets by viewModel.tickets
    // Diubah: Menggunakan loggedInUser yang nullable
    val userProfile by viewModel.loggedInUser

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Tiket & Reward", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2))
        )

        // Ditambahkan: Null check untuk memastikan pengguna sudah login
        userProfile?.let { user ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Status Reward", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Perjalanan (berbayar):")
                                // Diubah: Menggunakan data dari user
                                Text("${user.totalTrips}", fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tiket Gratis Tersisa:")
                                Text("${user.freeRideCount}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = if(user.totalTrips > 0) (user.totalTrips % 10) / 10f else 0f
                            Text(
                                "Progress ke reward berikutnya: ${user.totalTrips % 10}/10",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                                color = Color(0xFF4CAF50),
                                trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            )
                        }
                    }
                }

                if (tickets.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(LocalConfirmationNumber, contentDescription = "Tidak ada tiket", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Belum ada riwayat tiket", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Beli tiket pertama Anda untuk melihatnya di sini", fontSize = 14.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                } else {
                    item {
                        Text("Riwayat Tiket", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    items(tickets.sortedByDescending { it.purchaseTime }) { ticket ->
                        TicketCard(ticket = ticket, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
// Composable TicketCard tidak ada perubahan
@Composable
fun TicketCard(ticket: Ticket, viewModel: TransSurabayaViewModel) {
    val route = viewModel.routes.find { it.code == ticket.routeCode }
    val dateFormat = SimpleDateFormat("dd MMMizzi, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, route?.color?.copy(alpha=0.5f) ?: MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().background(route?.color?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    if (route != null) {
                        Box(
                            modifier = Modifier.size(24.dp).background(route.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(route.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(route?.name ?: ticket.routeCode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(if (ticket.isFree) "GRATIS" else "Rp ${ticket.price}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (ticket.isFree) Color(0xFF4CAF50) else route?.color ?: Color(0xFF1976D2))
            }
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dari: ${ticket.fromStop}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(LocalFlag, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ke: ${ticket.toStop}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ID: ${ticket.id.take(8).uppercase()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(dateFormat.format(Date(ticket.purchaseTime)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}