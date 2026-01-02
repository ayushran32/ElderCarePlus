package com.eldercareplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eldercareplus.elder.ElderApprovedViewModel
import com.eldercareplus.elder.EmergencyViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.eldercareplus.util.LocationHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ElderDashboard(navController: NavController) {
    val context = LocalContext.current
    
    // Request permissions
    val permissions = mutableListOf<String>().apply {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CALL_PHONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }.toTypedArray()
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        launcher.launch(permissions)
    }
    
    // Listen for fall detection events and navigate to confirmation screen
    LaunchedEffect(Unit) {
        com.eldercareplus.services.FallDetectionService.fallEvents.collect { fallDetected ->
            if (fallDetected) {
                Log.d("ElderDashboard", "Fall detected! Navigating to confirmation screen")
                navController.navigate("fall_confirmation")
            }
        }
    }

    val vm: ElderApprovedViewModel = viewModel()
    val emergencyVm: EmergencyViewModel = viewModel()
    val medicineVm: MedicineViewModel = viewModel(factory = MedicineViewModelFactory(context))
    val userName by vm.currentUserName.collectAsState()
    val emergencyContacts by emergencyVm.emergencyContacts.collectAsState()
    val medicines by medicineVm.medicines.collectAsState()
    val caretakers by vm.approvedCaretakers.collectAsState()
    
    // Dialog states
    var showCallCaretakerDialog by remember { mutableStateOf(false) }

    BackHandler {
        (context as? android.app.Activity)?.finish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Profile Photo Placeholder
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { /* TODO: Navigate to edit profile */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        "Welcome,",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        userName.ifEmpty { "Elder" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Settings Icon
            IconButton(onClick = { navController.navigate("elder_settings") }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(28.dp),
                    tint = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // EMERGENCY PANIC BUTTON
        var showPanicConfirmation by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Column(
                Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "⚠️ EMERGENCY",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Text(
                    "Instantly alert all caretakers",
                    fontSize = 14.sp,
                    color = Color(0xFF616161)
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { showPanicConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "SEND ALERT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Panic Confirmation Dialog
        if (showPanicConfirmation) {
            AlertDialog(
                onDismissRequest = { showPanicConfirmation = false },
                title = { Text("Send Emergency Alert?", fontWeight = FontWeight.Bold) },
                text = { Text("This will immediately notify all your caretakers with your location.") },
                confirmButton = {
                    Button(
                        onClick = {
                            val auth = FirebaseAuth.getInstance()
                            val user = auth.currentUser
                            if (user != null) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    val locationHelper = com.eldercareplus.util.LocationHelper(context)
                                    val location = locationHelper.getCurrentLocation()
                                    val alertManager = com.eldercareplus.model.AlertManager()
                                    alertManager.sendAlert(
                                        elderId = user.uid,
                                        elderName = user.displayName ?: "Elder",
                                        location = location,
                                        type = "PANIC_BUTTON"
                                    )
                                    Log.d("ElderDashboard", "Panic alert sent")
                                }
                            }
                            showPanicConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("SEND ALERT", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPanicConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        // EMERGENCY CONTACTS SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📞 Emergency Contacts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate("emergency_contacts") }) {
                        Text("Manage")
                    }
                }
                
                if (emergencyContacts.isEmpty()) {
                    Text(
                        "No emergency contacts yet. Tap 'Manage' to add contacts.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    emergencyContacts.take(3).forEach { contact ->
                        EmergencyContactItem(
                            contact = contact,
                            context = context,
                            canDelete = true, // Elder can delete
                            onDelete = { emergencyVm.deleteEmergencyContact(contact.id, "ELDER") }
                        )
                    }
                    if (emergencyContacts.size > 3) {
                        TextButton(onClick = { navController.navigate("elder_settings") }) {
                            Text("View All (${emergencyContacts.size})")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // QUICK DIAL - Ambulance, Police, Fire
        Text(
            "Quick Dial",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickDialCard(
                title = "Ambulance",
                number = "102",
                icon = Icons.Default.LocalHospital,
                color = Color(0xFFE53935),
                context = context,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            QuickDialCard(
                title = "Police",
                number = "100",
                icon = Icons.Default.Security,
                color = Color(0xFF1E88E5),
                context = context,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            QuickDialCard(
                title = "Fire",
                number = "101",
                icon = Icons.Default.Whatshot,
                color = Color(0xFFFF6F00),
                context = context,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // MAIN FEATURE GRID
        Text(
            "Daily Activities",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Row 1: Games & Doctor Appointments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardCard(
                title = "Brain Games",
                subtitle = "Keep your mind sharp",
                icon = Icons.Default.Extension,
                color = Color(0xFF4CAF50),
                onClick = { navController.navigate("games") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            DashboardCard(
                title = "Doctor Visits",
                subtitle = "Your appointments",
                icon = Icons.Default.MedicalServices,
                color = Color(0xFF2196F3),
                onClick = { navController.navigate("appointment") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 2: Government Schemes & Call Caretaker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardCard(
                title = "Govt Schemes",
                subtitle = "Benefits for seniors",
                icon = Icons.Default.AccountBalance,
                color = Color(0xFFFF9800),
                onClick = { navController.navigate("government_schemes") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            DashboardCard(
                title = "Call Caretaker",
                subtitle = "Quick contact",
                icon = Icons.Default.Phone,
                color = Color(0xFF9C27B0),
                onClick = { showCallCaretakerDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Row 3: Medicine Reminder (full width)
        val nextMedicine = medicines.sortedBy { it.timeInMillis }.firstOrNull()
        val medicineSubtitle = if (nextMedicine != null) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(nextMedicine.timeInMillis))
            "${nextMedicine.name} - $time"
        } else {
            "No medicines scheduled"
        }
        
        DashboardCard(
            title = "Medicine Reminder",
            subtitle = medicineSubtitle,
            icon = Icons.Default.Medication,
            color = Color(0xFFE91E63),
            onClick = { navController.navigate("medicine") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
        
        // Approved Caretakers Section (Simple List)
        if (caretakers.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👥 My Caretakers",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate("caretaker_management") }) {
                    Text("Manage")
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    caretakers.take(3).forEach { caretaker ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                caretaker.caretakerName.ifEmpty { "Caretaker" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (caretaker != caretakers.take(3).last()) {
                            HorizontalDivider()
                        }
                    }
                    
                    if (caretakers.size > 3) {
                        HorizontalDivider()
                        TextButton(
                            onClick = { navController.navigate("caretaker_management") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View All (${caretakers.size})")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
    
    // Call Caretaker Dialog
    if (showCallCaretakerDialog) {
        AlertDialog(
            onDismissRequest = { showCallCaretakerDialog = false },
            title = { Text("Call Caretaker", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (caretakers.isEmpty()) {
                        Text("No caretakers linked yet. Go to Settings to link caretakers.")
                    } else {
                        Text("Select a caretaker to call:", modifier = Modifier.padding(bottom = 12.dp))
                        caretakers.forEach { caretaker ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (caretaker.phoneNumber.isNotEmpty()) {
                                            val intent = Intent(Intent.ACTION_CALL, "tel:${caretaker.phoneNumber}".toUri())
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Log.e("ElderDashboard", "Call failed", e)
                                            }
                                            showCallCaretakerDialog = false
                                        } else {
                                            Log.e("ElderDashboard", "Phone number not available for caretaker: ${caretaker.caretakerName}")
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (caretaker.phoneNumber.isNotEmpty()) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        Color.Gray.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            caretaker.caretakerName.ifEmpty { "Caretaker" }, 
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (caretaker.phoneNumber.isNotEmpty()) {
                                            Text(
                                                caretaker.phoneNumber,
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        } else {
                                            Text(
                                                "Phone number not available",
                                                fontSize = 12.sp,
                                                color = Color.Red
                                            )
                                        }
                                    }
                                    if (caretaker.phoneNumber.isNotEmpty()) {
                                        Icon(
                                            Icons.Default.Call, 
                                            contentDescription = "Call",
                                            tint = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCallCaretakerDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun EmergencyContactItem(
    contact: com.eldercareplus.model.EmergencyContact,
    context: Context,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(contact.phoneNumber, fontSize = 14.sp, color = Color.Gray)
                Text(contact.relationship, fontSize = 12.sp, color = Color.Gray)
            }
        }
        
        Row {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_CALL, "tel:${contact.phoneNumber}".toUri())
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF4CAF50))
            }
            
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun QuickDialCard(
    title: String,
    number: String,
    icon: ImageVector,
    color: Color,
    context: Context,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable {
                val intent = Intent(Intent.ACTION_CALL, "tel:$number".toUri())
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(36.dp),
                tint = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                number,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(28.dp),
                    tint = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
