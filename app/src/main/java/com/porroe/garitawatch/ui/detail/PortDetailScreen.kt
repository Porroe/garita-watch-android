package com.porroe.garitawatch.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porroe.garitawatch.R
import com.porroe.garitawatch.domain.model.BorderWaitTime
import com.porroe.garitawatch.domain.model.LaneDetails
import com.porroe.garitawatch.domain.model.LaneType
import com.porroe.garitawatch.domain.util.PortNavigation
import com.porroe.garitawatch.ui.dashboard.StatusBadge
import com.porroe.garitawatch.ui.dashboard.getWaitTimeColor
import com.porroe.garitawatch.ui.theme.GaritawatchTheme

@Composable
fun PortDetailScreen(
    viewModel: PortDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val port by viewModel.port.collectAsState()
    val isMonitored by viewModel.isMonitored.collectAsState()

    PortDetailScreen(
        port = port,
        isMonitored = isMonitored,
        onNavigateBack = onNavigateBack,
        onToggleFavorite = viewModel::toggleFavorite
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortDetailScreen(
    port: BorderWaitTime?,
    isMonitored: Boolean,
    onNavigateBack: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    val titleText = remember(port) {
        port?.let {
            if (it.crossingName.isNotEmpty() && it.crossingName != it.portName) {
                "${it.portName} - ${it.crossingName}"
            } else {
                it.portName
            }
        } ?: ""
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (port != null) {
                        val mapsIntentUri = PortNavigation.getMapsIntent(port.portNumber)
                        if (mapsIntentUri != null) {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsIntentUri))
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Directions, contentDescription = "Navigate")
                            }
                        }
                        
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (isMonitored) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = if (isMonitored) "Remove from favorites" else "Add to favorites",
                                tint = if (isMonitored) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        port?.let { portData ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    PortHeader(portData)
                }

                // Passenger Lanes
                val passengerLanes = portData.passengerLanes.filter { 
                    it.operationalStatus.isNotBlank() && !it.operationalStatus.equals("N/A", ignoreCase = true) 
                }
                if (passengerLanes.isNotEmpty()) {
                    item {
                        LaneCategorySection(stringResource(R.string.vehicle_label), passengerLanes)
                    }
                }

                // Pedestrian Lanes
                val pedestrianLanes = portData.pedestrianLanes.filter { 
                    it.operationalStatus.isNotBlank() && !it.operationalStatus.equals("N/A", ignoreCase = true) 
                }
                if (pedestrianLanes.isNotEmpty()) {
                    item {
                        LaneCategorySection(stringResource(R.string.pedestrian_label), pedestrianLanes)
                    }
                }

                // Commercial Lanes
                val commercialLanes = portData.commercialLanes.filter { 
                    it.operationalStatus.isNotBlank() && !it.operationalStatus.equals("N/A", ignoreCase = true) 
                }
                if (commercialLanes.isNotEmpty()) {
                    item {
                        LaneCategorySection("Commercial", commercialLanes)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PortHeader(port: BorderWaitTime) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A233A))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = port.portName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (port.crossingName.isNotEmpty() && port.crossingName != port.portName) {
                        Text(
                            text = port.crossingName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                StatusBadge(isOpen = !port.portStatus.equals("Closed", ignoreCase = true))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = port.border,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.last_updated, port.lastUpdate),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Composable
fun LaneCategorySection(title: String, lanes: List<LaneDetails>) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A233A))
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                lanes.forEachIndexed { index, lane ->
                    LaneDetailRow(lane)
                    if (index < lanes.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = Color(0xFF2D3748).copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LaneDetailRow(lane: LaneDetails) {
    val isClosed = lane.operationalStatus.equals("Closed", ignoreCase = true)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            val icon = when (lane.type) {
                LaneType.STANDARD -> Icons.Default.DirectionsCar
                LaneType.SENTRI_NEXUS -> Icons.Default.Verified
                LaneType.READY -> Icons.Default.Bolt
                LaneType.FAST -> Icons.Default.Speed
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = when (lane.type) {
                        LaneType.STANDARD -> "Standard"
                        LaneType.SENTRI_NEXUS -> "SENTRI / NEXUS"
                        LaneType.READY -> "Ready Lane"
                        LaneType.FAST -> "FAST"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lane.operationalStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isClosed) Color(0xFFF44336) else if (lane.operationalStatus.contains("delay", ignoreCase = true)) Color(0xFF4CAF50) else Color(0xFF94A3B8)
                    )
                    if (!isClosed && lane.lanesOpen > 0) {
                        Text(
                            text = " • ${lane.lanesOpen} lanes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
        
        val waitText = when {
            isClosed -> stringResource(R.string.wait_time_na)
            lane.delayMinutes == null -> stringResource(R.string.wait_time_na)
            else -> stringResource(R.string.wait_time_format, lane.delayMinutes)
        }
        
        Text(
            text = waitText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = if (isClosed) Color.White else getWaitTimeColor(lane.delayMinutes)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PortDetailScreenPreview() {
    GaritawatchTheme(darkTheme = true) {
        val samplePort = BorderWaitTime(
            portNumber = "250401",
            portName = "San Ysidro",
            crossingName = "El Chaparral",
            border = "Mexico",
            portStatus = "Open",
            lastUpdate = "10:00 AM",
            passengerLanes = listOf(
                LaneDetails(LaneType.STANDARD, "10:00 AM", "Open", 45, 10),
                LaneDetails(LaneType.SENTRI_NEXUS, "10:00 AM", "Open", 15, 4),
                LaneDetails(LaneType.READY, "10:00 AM", "Open", 30, 6)
            ),
            pedestrianLanes = listOf(
                LaneDetails(LaneType.STANDARD, "10:00 AM", "Open", 10, 2),
                LaneDetails(LaneType.READY, "10:00 AM", "Open", 5, 1)
            ),
            commercialLanes = emptyList()
        )
        
        PortDetailScreen(
            port = samplePort,
            isMonitored = true,
            onNavigateBack = {},
            onToggleFavorite = {}
        )
    }
}
