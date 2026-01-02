package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eldercareplus.elder.ElderApprovedViewModel

@Composable
fun CaretakerManagementScreen(navController: NavController) {
    val vm: ElderApprovedViewModel = viewModel()
    val linkCode by vm.linkCode.collectAsState()
    val pendingRequests by vm.pendingRequests.collectAsState()
    val caretakers by vm.approvedCaretakers.collectAsState()
    
    var showLinkCode by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                "Caretaker Management",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))
        
        // Link Code Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Link with Caretaker",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Generate a code to share with your caretaker for linking",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Spacer(Modifier.height(16.dp))
                
                if (showLinkCode && linkCode.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Your Link Code",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                linkCode,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Text(
                                "Share this code with your caretaker",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLinkCode = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Hide Code")
                        }
                        Button(
                            onClick = { vm.generateNewLinkCode() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate New")
                        }
                    }
                } else {
                    Button(
                        onClick = { 
                            if (linkCode.isEmpty()) {
                                vm.generateNewLinkCode()
                            }
                            showLinkCode = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Link Code")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Pending Requests
        if (pendingRequests.isNotEmpty()) {
            Text(
                "Pending Requests (${pendingRequests.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            pendingRequests.forEach { request ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                request.caretakerName.ifEmpty { "Unknown Caretaker" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Wants to link with you",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        Row {
                            IconButton(onClick = { vm.approveLinking(request.linkId) }) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Approve",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                            IconButton(onClick = { vm.denyLinking(request.linkId) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Deny",
                                    tint = Color(0xFFE53935)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
        }
        
        // Approved Caretakers
        Text(
            "Approved Caretakers (${caretakers.size})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (caretakers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No caretakers linked yet",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Generate a code to link with a caretaker",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            caretakers.forEach { caretaker ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                caretaker.caretakerName.ifEmpty { "Caretaker" },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Active",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        IconButton(onClick = { vm.unlinkCaretaker(caretaker.linkId) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Unlink",
                                tint = Color(0xFFE53935)
                            )
                        }
                    }
                }
            }
        }
    }
}
