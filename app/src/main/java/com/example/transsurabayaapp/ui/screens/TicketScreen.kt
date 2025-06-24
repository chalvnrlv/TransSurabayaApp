package com.example.transsurabayaapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.transsurabayaapp.data.Ticket
import com.example.transsurabayaapp.ui.icon.LocalConfirmationNumber
import com.example.transsurabayaapp.ui.icon.LocalFlag
import com.example.transsurabayaapp.util.QRCodeGenerator
import com.example.transsurabayaapp.viewmodel.TransSurabayaViewModel
import androidx.compose.foundation.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketScreen(viewModel: TransSurabayaViewModel) {
    val tickets by viewModel.tickets
    val userProfile by viewModel.loggedInUser

    // State untuk boarding pass dialog
    var showBoardingPass by remember { mutableStateOf(false) }
    var selectedTicket by remember { mutableStateOf<Ticket?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Tiket & Reward", fontWeight = FontWeight.Bold, color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2))
        )

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
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
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
                        TicketCard(
                            ticket = ticket,
                            viewModel = viewModel,
                            onClick = {
                                selectedTicket = ticket
                                showBoardingPass = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Boarding Pass Dialog
    selectedTicket?.let { ticket ->
        if (showBoardingPass) {
            BoardingPassDialog(
                ticket = ticket,
                onDismiss = { showBoardingPass = false },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun TicketCard(
    ticket: Ticket,
    viewModel: TransSurabayaViewModel,
    onClick: () -> Unit
) {
    val route = viewModel.routes.find { it.code == ticket.routeCode }
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

@Composable
fun BoardingPassDialog(
    ticket: Ticket,
    onDismiss: () -> Unit,
    viewModel: TransSurabayaViewModel
) {
    val context = LocalContext.current
    val route = viewModel.routes.find { it.code == ticket.routeCode }

    // Generate QR code
    val qrBitmap = remember(ticket.id) {
        QRCodeGenerator.generateQRCode(ticket.id, 512)
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "BOARDING PASS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = route?.color ?: Color(0xFF1976D2)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Route Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("RUTE", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            route?.name ?: ticket.routeCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("TANGGAL", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            dateFormat.format(Date(ticket.purchaseTime)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stops
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(route?.color?.copy(alpha = 0.1f) ?: Color(0xFF1976D2).copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(route?.color ?: Color(0xFF1976D2), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(ticket.fromStop, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .padding(start = 3.dp, top = 4.dp, bottom = 4.dp)
                                .width(2.dp)
                                .height(24.dp)
                                .background(route?.color ?: Color(0xFF1976D2).copy(alpha = 0.5f))
                        ) {
                            // Vertical line
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(route?.color ?: Color(0xFF1976D2), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(ticket.toStop, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // QR Code
                Text("SCAN UNTUK BOARDING", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Boarding QR Code",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ticket ID
                Text(
                    "ID: ${ticket.id.take(8).uppercase()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Text(
                    "Tunjukkan QR code ini kepada petugas saat boarding",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}