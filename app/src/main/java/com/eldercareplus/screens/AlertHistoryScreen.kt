package com.eldercareplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eldercareplus.model.Alert
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.net.Uri

@Composable
fun AlertHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    
    var alerts by remember { mutableStateOf<List<Pair<Alert, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Load alerts from all linked elders
    LaunchedEffect(Unit) {
        val caretakerId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        
        // First get all linked elders
        db.collection("links")
            .whereEqualTo("caretakerId", caretakerId)
            .whereEqualTo("status", "approved")
            .get()
            .addOnSuccessListener { linksSnapshot ->
                val elderIds = linksSnapshot.documents.mapNotNull { it.getString("elderId") }
                
                if (elderIds.isEmpty()) {
                    android.util.Log.d("AlertHistory", "No linked elders found")
                    isLoading = false
                    return@addOnSuccessListener
                }
                
                android.util.Log.d("AlertHistory", "Querying alerts for ${elderIds.size} elders: $elderIds")
                
                // Get all alerts from these elders
                db.collection("alerts")
                    .whereIn("elderId", elderIds)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .addOnSuccessListener { alertsSnapshot ->
                        android.util.Log.d("AlertHistory", "Query successful! Found ${alertsSnapshot.size()} alert documents")
                        alerts = alertsSnapshot.documents.mapNotNull { doc ->
                            val alert = doc.toObject(Alert::class.java)
                            android.util.Log.d("AlertHistory", "Alert: id=${doc.id}, elderId=${alert?.elderId}, type=${alert?.type}, timestamp=${alert?.timestamp}")
                            alert?.let { Pair(it, doc.id) }
                        }
                        android.util.Log.d("AlertHistory", "Loaded ${alerts.size} alerts into UI")
                        isLoading = false
                    }
                    .addOnFailureListener { error ->
                        android.util.Log.e("AlertHistory", "⚠️ QUERY FAILED!", error)
                        android.util.Log.e("AlertHistory", "Error message: ${error.message}")
                        if (error.message?.contains("index", ignoreCase = true) == true) {
                            android.util.Log.e("AlertHistory", "🔥 FIRESTORE INDEX REQUIRED! Check logcat for index creation link")
                        }
                        isLoading = false
                    }
            }
            .addOnFailureListener { error ->
                android.util.Log.e("AlertHistory", "Failed to query links", error)
                isLoading = false
            }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Alert History",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${alerts.size} total alerts",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "All",
                        onClick = { selectedFilter = "All" },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "FALL",
                        onClick = { selectedFilter = "FALL" },
                        label = { Text("Falls") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "SOS",
                        onClick = { selectedFilter = "SOS" },
                        label = { Text("SOS") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "SOUND",
                        onClick = { selectedFilter = "SOUND" },
                        label = { Text("Sound") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color.White,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
        
        // Alert List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No alerts yet",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Alert history will appear here",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            val filteredAlerts = if (selectedFilter == "All") {
                alerts
            } else {
                alerts.filter { it.first.type.contains(selectedFilter, ignoreCase = true) }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAlerts) { (alert, alertId) ->
                    AlertHistoryCard(
                        alert = alert,
                        onViewLocation = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(alert.mapsUrl))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertHistoryCard(
    alert: Alert,
    onViewLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                alert.type.contains("FALL") -> Color(0xFFFFEBEE)
                alert.type.contains("SOS") -> Color(0xFFFFF3E0)
                else -> Color.White
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon based on alert type
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                alert.type.contains("FALL") -> Color(0xFFD32F2F)
                                alert.type.contains("SOS") -> Color(0xFFFF6F00)
                                alert.type.contains("SOUND") -> Color(0xFFFBC02D)
                                else -> MaterialTheme.colorScheme.primary
                            }.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when {
                            alert.type.contains("FALL") -> Icons.Default.PersonOff
                            alert.type.contains("SOS") -> Icons.Default.Emergency
                            alert.type.contains("SOUND") -> Icons.Default.VolumeUp
                            else -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when {
                            alert.type.contains("FALL") -> Color(0xFFD32F2F)
                            alert.type.contains("SOS") -> Color(0xFFFF6F00)
                            alert.type.contains("SOUND") -> Color(0xFFFBC02D)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        alert.elderName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Text(
                        getAlertTypeDisplay(alert.type),
                        fontSize = 14.sp,
                        color = when {
                            alert.type.contains("FALL") -> Color(0xFFD32F2F)
                            alert.type.contains("SOS") -> Color(0xFFFF6F00)
                            else -> Color.Gray
                        },
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(4.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatTimestamp(alert.timestamp),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            HorizontalDivider()
            
            Spacer(Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewLocation,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("View Location")
                }
                
                // Status badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.status == "RESOLVED") 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFFFBC02D)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        alert.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getAlertTypeDisplay(type: String): String {
    return when {
        type.contains("FALL") -> "🚨 Fall Detected"
        type.contains("SOS") -> "🆘 Emergency SOS"
        type.contains("LOUD_SOUND") -> "🔊 Distress Sound"
        type.contains("MANUAL") -> "📱 Manual Alert"
        else -> "⚠️ Alert"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} min ago"
        diff < 86400000 -> "${diff / 3600000} hr ago"
        diff < 604800000 -> "${diff / 86400000} days ago"
        else -> {
            val formatter = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}
