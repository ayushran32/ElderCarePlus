package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api
import com.eldercareplus.model.AdminRepository
import com.eldercareplus.model.PendingUser
import com.eldercareplus.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AdminDashboard(navController: NavController) {
    val repository = remember { AdminRepository() }
    val scope = rememberCoroutineScope()
    var pendingUsers by remember { mutableStateOf<List<PendingUser>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.getPendingCaretakers().collect {
            pendingUsers = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Pending Approvals", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            if (pendingUsers.isEmpty()) {
                Text("No pending approvals.")
            } else {
                LazyColumn {
                    items(pendingUsers) { user ->
                        PendingUserItem(
                            user = user,
                            onApprove = {
                                scope.launch { repository.approveUser(user.uid) }
                            },
                            onReject = {
                                scope.launch { repository.rejectUser(user.uid) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingUserItem(user: PendingUser, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(user.email, style = MaterialTheme.typography.bodyLarge)
            Text("Role: ${user.role}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onReject) {
                    Text("Reject", color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onApprove) {
                    Text("Approve")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: @Composable () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    CenterAlignedTopAppBar(
        title = title,
        actions = actions
    )
}
