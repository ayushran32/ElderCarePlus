package com.eldercareplus.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eldercareplus.model.Elder

@Composable
fun ElderItem(
    elder: Elder,
    onDelete: () -> Unit,
    onEdit: (Elder) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(elder.name, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(4.dp))

            Text("Age: ${elder.age}")
            Text("Phone: ${elder.phone}")
            Text("Condition: ${elder.condition}")

            Spacer(Modifier.height(8.dp))

            Row {
                Button(onClick = { onEdit(elder) }) {
                    Text("Edit")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
