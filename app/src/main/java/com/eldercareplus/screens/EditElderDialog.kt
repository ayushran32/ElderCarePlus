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
fun EditElderDialog(
    elder: Elder,
    onDismiss: () -> Unit,
    onSave: (Elder) -> Unit
) {
    var name by remember { mutableStateOf(elder.name) }
    var age by remember { mutableStateOf(elder.age) }
    var phone by remember { mutableStateOf(elder.phone) }
    var condition by remember { mutableStateOf(elder.condition) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Elder") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(age, { age = it }, label = { Text("Age") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(condition, { condition = it }, label = { Text("Condition") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    elder.copy(
                        name = name,
                        age = age,
                        phone = phone,
                        condition = condition
                    )
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
