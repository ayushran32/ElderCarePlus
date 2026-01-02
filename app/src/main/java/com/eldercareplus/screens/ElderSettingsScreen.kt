package com.eldercareplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.eldercareplus.services.FallDetectionService
import com.eldercareplus.services.AudioSafetyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.eldercareplus.util.LocationHelper
import com.eldercareplus.model.AlertManager
import android.app.ActivityManager

@Composable
fun ElderSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    
    // SharedPreferences for persisting service states
    val prefs = context.getSharedPreferences("elder_settings_prefs", Context.MODE_PRIVATE)
    
    var fallDetectionEnabled by remember { 
        mutableStateOf(prefs.getBoolean("fall_detection_enabled", false)) 
    }
    var soundDetectionEnabled by remember { 
        mutableStateOf(prefs.getBoolean("sound_detection_enabled", false)) 
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Safety Monitoring Section
        SettingsSection(title = "Safety Monitoring") {
            SwitchSettingItem(
                title = "Fall Detection",
                subtitle = "Automatically detect falls",
                icon = Icons.Default.FitnessCenter,
                checked = fallDetectionEnabled,
                onCheckedChange = { enabled ->
                    fallDetectionEnabled = enabled
                    // Save state to SharedPreferences
                    prefs.edit().putBoolean("fall_detection_enabled", enabled).apply()
                    
                    try {
                        val intent = android.content.Intent(context, FallDetectionService::class.java)
                        if (enabled) {
                            context.startForegroundService(intent)
                        } else {
                            // Check if service is running before stopping
                            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                            val isRunning = activityManager.getRunningServices(Integer.MAX_VALUE)
                                .any { it.service.className == FallDetectionService::class.java.name }
                            if (isRunning) {
                                context.stopService(intent)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Settings", "Fall detection toggle error", e)
                    }
                }
            )
            
            HorizontalDivider()
            
            SwitchSettingItem(
                title = "Sound Detection",
                subtitle = "Monitor for loud sounds/screams",
                icon = Icons.Default.GraphicEq,
                checked = soundDetectionEnabled,
                onCheckedChange = { enabled ->
                    soundDetectionEnabled = enabled
                    // Save state to SharedPreferences
                    prefs.edit().putBoolean("sound_detection_enabled", enabled).apply()
                    
                    try {
                        val intent = android.content.Intent(context, AudioSafetyService::class.java)
                        if (enabled) {
                            context.startForegroundService(intent)
                        } else {
                            // Check if service is running before stopping
                            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                            val isRunning = activityManager.getRunningServices(Integer.MAX_VALUE)
                                .any { it.service.className == AudioSafetyService::class.java.name }
                            if (isRunning) {
                                context.stopService(intent)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Settings", "Sound detection toggle error", e)
                    }
                }
            )
            
            HorizontalDivider()
            
            SettingItem(
                title = "Test Emergency Alert",
                subtitle = "Send test alert to caretakers",
                icon = Icons.Default.NotificationImportant,
                onClick = {
                    // Send test alert
                    val auth = FirebaseAuth.getInstance()
                    val user = auth.currentUser
                    if (user != null) {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            val locationHelper = com.eldercareplus.util.LocationHelper(context)
                            val location = locationHelper.getCurrentLocation()
                            val alertManager = com.eldercareplus.model.AlertManager()
                            alertManager.sendAlert(
                                elderId = user.uid,
                                elderName = user.displayName ?: "Elder",
                                location = location,
                                type = "TEST_ALERT"
                            )
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        // Health Tracking Section
        SettingsSection(title = "Health Tracking") {
            SettingItem(
                title = "Medicine Reminders",
                subtitle = "Manage your medications",
                icon = Icons.Default.Medication,
                onClick = { navController.navigate("medicine") }
            )
            
            HorizontalDivider()
            
            SettingItem(
                title = "Doctor Appointments",
                subtitle = "Manage appointments",
                icon = Icons.Default.MedicalServices,
                onClick = { navController.navigate("appointment") }
            )
            
            HorizontalDivider()
            
            SettingItem(
                title = "Sleep Tracking",
                subtitle = "View sleep history",
                icon = Icons.Default.Bedtime,
                onClick = { navController.navigate("sleep") }
            )
        }

        Spacer(Modifier.height(20.dp))

        // Account Section
        SettingsSection(title = "Account") {
            SettingItem(
                title = "Edit Profile",
                subtitle = "Update your information",
                icon = Icons.Default.Person,
                onClick = { navController.navigate("profile_setup") }
            )
            
            HorizontalDivider()
            
            SettingItem(
                title = "Caretaker Management",
                subtitle = "Link & manage caretakers",
                icon = Icons.Default.Group,
                onClick = { navController.navigate("caretaker_management") }
            )
            
            HorizontalDivider()
            
            SettingItem(
                title = "Emergency Contacts",
                subtitle = "Manage emergency contacts",
                icon = Icons.Default.ContactPhone,
                onClick = { navController.navigate("emergency_contacts") }
            )
            
            HorizontalDivider()
            
            SettingItem(
                title = "Logout",
                subtitle = "Sign out of your account",
                icon = Icons.Default.Logout,
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0)
                    }
                },
                textColor = Color(0xFFD32F2F)
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
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

@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
