package com.eldercareplus.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
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
import com.eldercareplus.caretaker.CaretakerViewModel
import com.eldercareplus.caretaker.CaretakerLinkViewModel
import com.eldercareplus.model.LinkedElder
import com.eldercareplus.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CaretakerDashboard(navController: NavController) {
    val context = LocalContext.current
    val vm: CaretakerViewModel = viewModel()
    val caretakerName by vm.caretakerName.collectAsState()

    // LINK VIEWMODEL (ENTER INVITE CODE)
    val linkVm: CaretakerLinkViewModel = viewModel()
    val linkStatus by linkVm.status.collectAsState()
    val linkedElders by linkVm.linkedElders.collectAsState()

    var inviteCodeInput by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // Urgent Alert State
    var showUrgentAlert by remember { mutableStateOf(false) }
    var urgentAlertData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    // Broadcast receiver for urgent alerts
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.eldercareplus.URGENT_ALERT") {
                    urgentAlertData = mapOf(
                        "elderName" to (intent.getStringExtra("ELDER_NAME") ?: "Elder"),
                        "alertType" to (intent.getStringExtra("ALERT_TYPE") ?: "Emergency"),
                        "lat" to intent.getDoubleExtra("LATITUDE", 0.0).toString(),
                        "lng" to intent.getDoubleExtra("LONGITUDE", 0.0).toString(),
                        "mapsUrl" to (intent.getStringExtra("MAPS_URL") ?: "")
                    )
                    showUrgentAlert = true
                }
            }
        }
        
        val filter = IntentFilter("com.eldercareplus.URGENT_ALERT")
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    BackHandler {
        (context as? android.app.Activity)?.finish()
    }
    
    // Auto-start alert service
    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, com.eldercareplus.services.CaretakerAlertService::class.java)
        context.startForegroundService(serviceIntent)
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
                        .background(MaterialTheme.colorScheme.primary),
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
                        caretakerName.ifEmpty { "Caretaker" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Alert History & Settings Icons
            Row {
                IconButton(onClick = { navController.navigate("alert_history") }) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "Alert History",
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // LINK ELDER SECTION
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Link New Elder",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = inviteCodeInput,
                    onValueChange = { inviteCodeInput = it },
                    label = { Text("Enter Invite Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null)
                    }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { linkVm.submitInviteCode(inviteCodeInput) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send Link Request")
                }

                linkStatus?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        it,
                        color = if (it.contains("success", ignoreCase = true)) 
                            Color(0xFF4CAF50) 
                        else 
                            MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // LINKED ELDERS SECTION
        Text(
            "My Linked Elders",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (linkedElders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No elders linked yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                    Text(
                        "Enter an invite code above to link an elder",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            linkedElders.forEach { elder ->
                ElderCard(
                    elder = elder,
                    onClick = {
                        navController.navigate("caretaker_control/${elder.elderId}")
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(40.dp))
    }

    // ALERTS (FALL DETECTION ETC)
    val alertVm: AlertViewModel = viewModel()
    val activeAlerts by alertVm.activeAlerts.collectAsState()

    // Pass linked elder IDs to AlertVM so it knows who to listen for
    LaunchedEffect(linkedElders) {
        val ids = linkedElders.map { it.elderId }
        alertVm.setLinkedElders(ids)
    }

    // EMERGENCY ALERT DIALOG
    if (activeAlerts.isNotEmpty()) {
        val alert = activeAlerts.first() // Show one at a time

        AlertDialog(
            onDismissRequest = { /* Prevent dismissal without acknowledgement */ },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "EMERGENCY ALERT",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        "Elder: ${alert.elderName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Type: ${alert.type}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Time: ${java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(alert.timestamp)}"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(alert.mapsUrl)
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("VIEW ON MAP")
                }
            },
            dismissButton = {
                Button(onClick = { alertVm.acknowledgeAlert(alert.id) }) {
                    Text("ACKNOWLEDGE")
                }
            }
        )
    }

    // SETTINGS DIALOG
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Settings", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingMenuItem(
                        icon = Icons.Default.PersonAdd,
                        title = "Add Elder (Local)",
                        subtitle = "Add elder data locally",
                        onClick = {
                            showSettingsDialog = false
                            showAddDialog = true
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingMenuItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your information",
                        onClick = {
                            showSettingsDialog = false
                            navController.navigate("profile_setup")
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingMenuItem(
                        icon = Icons.Default.Logout,
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0)
                            }
                        },
                        titleColor = Color(0xFFD32F2F)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // ADD ELDER DIALOG
    if (showAddDialog) {
        AddElderDialog(
            onDismiss = { showAddDialog = false },
            onAdd = {
                vm.addElder(it)
                showAddDialog = false
            }
        )
    }
    
    // URGENT ALERT POPUP DIALOG
    if (showUrgentAlert) {
        Dialog(
            onDismissRequest = { showUrgentAlert = false },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Alert Icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "🚨",
                            fontSize = 60.sp
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // Alert Title
                    Text(
                        "URGENT ALERT",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Elder Name
                    Text(
                        urgentAlertData["elderName"] ?: "Elder",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Alert Type
                    Text(
                        urgentAlertData["alertType"] ?: "Emergency",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // View Location Button
                        Button(
                            onClick = {
                                val mapsUrl = urgentAlertData["mapsUrl"] ?: ""
                                if (mapsUrl.isNotEmpty()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                                    context.startActivity(intent)
                                }
                                showUrgentAlert = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Location", fontWeight = FontWeight.Medium)
                        }
                        
                        // Call Emergency Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                                context.startActivity(intent)
                                showUrgentAlert = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Call 112", fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Dismiss Button
                    OutlinedButton(
                        onClick = { showUrgentAlert = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ElderCard(elder: LinkedElder, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (elder.elderName.isNotEmpty() && elder.elderName != "Elder")
                        elder.elderName
                    else
                        "Elder",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ID: ${elder.elderId.take(8)}...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Tap to Manage",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Go",
            tint = Color.Gray
        )
    }
}
