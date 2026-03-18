package com.garitawatch.app.ui.detail

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.garitawatch.app.data.local.entity.AlertEntity
import com.garitawatch.app.domain.model.BorderWaitTime
import com.garitawatch.app.domain.model.LaneType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertConfigurationDrawer(
    port: BorderWaitTime,
    existingAlert: AlertEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (crossingType: String, laneTypes: List<LaneType>, threshold: Int, duration: Int) -> Unit
) {
    var selectedCrossingType by remember { mutableStateOf(existingAlert?.crossingType ?: "Passenger") }
    val availableLaneTypes = when (selectedCrossingType) {
        "Passenger" -> port.passengerLanes.map { it.type }
        "Pedestrian" -> port.pedestrianLanes.map { it.type }
        "Commercial" -> port.commercialLanes.map { it.type }
        else -> emptyList()
    }
    
    var selectedLaneTypes by remember { 
        mutableStateOf(existingAlert?.laneTypes ?: emptyList()) 
    }
    
    // Sync selected lanes if crossing type changes and they are not available
    LaunchedEffect(selectedCrossingType) {
        if (existingAlert == null || selectedCrossingType != existingAlert.crossingType) {
            selectedLaneTypes = emptyList()
        }
    }

    var threshold by remember { mutableIntStateOf(existingAlert?.thresholdMinutes ?: 30) }
    var duration by remember { mutableIntStateOf(existingAlert?.durationDays ?: 1) }
    
    val context = LocalContext.current
    var permissionError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onConfirm(selectedCrossingType, selectedLaneTypes, threshold, duration)
            onDismiss()
        } else {
            permissionError = "Notifications are required to receive alerts. Please enable notification permission."
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E293B),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = if (existingAlert == null) "Create Alert" else "Update Alert",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Crossing Type", style = MaterialTheme.typography.labelLarge, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Passenger", "Pedestrian", "Commercial").forEach { type ->
                    FilterChip(
                        selected = selectedCrossingType == type,
                        onClick = { selectedCrossingType = type },
                        label = { Text(type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF334155),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Lane Types", style = MaterialTheme.typography.labelLarge, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableLaneTypes) { laneType ->
                    val isSelected = selectedLaneTypes.contains(laneType)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedLaneTypes = if (isSelected) {
                                selectedLaneTypes - laneType
                            } else {
                                selectedLaneTypes + laneType
                            }
                        },
                        label = { Text(laneType.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF334155),
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Wait Time Threshold: $threshold min", style = MaterialTheme.typography.labelLarge, color = Color(0xFF94A3B8))
            Slider(
                value = threshold.toFloat(),
                onValueChange = { threshold = it.toInt() },
                valueRange = 5f..180f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color(0xFF334155)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Alert Duration (Days)", style = MaterialTheme.typography.labelLarge, color = Color(0xFF94A3B8))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = duration.toString(),
                    onValueChange = { duration = it.toIntOrNull() ?: 1 },
                    modifier = Modifier.width(100.dp),
                    textStyle = LocalTextStyle.current.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )
                Text("days", color = Color(0xFF94A3B8))
            }

            if (permissionError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(permissionError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Enable Settings", 
                            color = Color.Red, 
                            style = MaterialTheme.typography.bodySmall, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    if (checkNotificationPermission(context)) {
                        onConfirm(selectedCrossingType, selectedLaneTypes, threshold, duration)
                        onDismiss()
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        permissionError = "Notifications are required to receive alerts. Please enable notification permission."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedLaneTypes.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (existingAlert == null) "Create Alert" else "Update Alert")
            }
        }
    }
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
