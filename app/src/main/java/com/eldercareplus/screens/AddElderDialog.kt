package com.eldercareplus.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eldercareplus.model.Elder

@Composable
fun AddElderDialog(
    onDismiss: () -> Unit,
    onAdd: (Elder) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Elder") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(
                    Elder(
                        name = name,
                        age = age,
                        phone = phone,
                        condition = condition
                    )
                )
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
