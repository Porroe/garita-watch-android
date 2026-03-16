package com.porroe.garitawatch.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.porroe.garitawatch.R
import com.porroe.garitawatch.domain.model.BorderWaitTime
import com.porroe.garitawatch.domain.model.LaneDetails
import com.porroe.garitawatch.domain.model.LaneType
import com.porroe.garitawatch.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSearch: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        containerColor = Color(0xFF0F172A),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.find_ports))
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.monitoredPorts.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToSearch,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text(stringResource(R.string.add_ports)) }
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (uiState.monitoredPorts.isEmpty()) {
                    EmptyDashboard(onNavigateToSearch)
                } else {
                    Text(
                        text = stringResource(R.string.last_updated, uiState.lastUpdated),
                        style = MaterialTheme.typography.bodySmall,
                        // Increased font size from 12sp by 15% (~13.8sp)
                        fontSize = 13.8.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color(0xFF94A3B8)
                    )
                    
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.monitoredPorts, key = { it.portNumber }) { port ->
                            PortCard(port)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortCard(port: BorderWaitTime) {
    val isPortClosed = port.portStatus.equals("Closed", ignoreCase = true)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A233A)
        )
    ) {
        Column(modifier = Modifier.padding(26.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = port.portName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (port.crossingName.isNotEmpty() && port.crossingName != port.portName) {
                        Text(
                            text = port.crossingName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                
                StatusBadge(isOpen = !isPortClosed)
            }
            
            if (isPortClosed) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.port_closed),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            } else {
                // Find representative wait times
                val vehicleWait = port.passengerLanes.firstOrNull { it.type == LaneType.STANDARD }?.delayMinutes
                val sentriWait = port.passengerLanes.firstOrNull { it.type == LaneType.SENTRI_NEXUS }?.delayMinutes
                val readyWait = port.passengerLanes.firstOrNull { it.type == LaneType.READY }?.delayMinutes
                val pedestrianWait = port.pedestrianLanes.firstOrNull { it.type == LaneType.STANDARD }?.delayMinutes

                Spacer(modifier = Modifier.height(35.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    val isVehicleClosed = port.passengerLanes.all { it.type == LaneType.STANDARD && it.operationalStatus.equals("Closed", ignoreCase = true) }
                    val isPedestrianClosed = port.pedestrianLanes.all { it.operationalStatus.equals("Closed", ignoreCase = true) }
                    val isSentriClosed = port.passengerLanes.none { it.type == LaneType.SENTRI_NEXUS && !it.operationalStatus.equals("Closed", ignoreCase = true) }
                    val isReadyClosed = port.passengerLanes.none { it.type == LaneType.READY && !it.operationalStatus.equals("Closed", ignoreCase = true) }

                    LaneSummaryRow(
                        icon = Icons.Default.DirectionsCar,
                        label = stringResource(R.string.vehicle_label),
                        waitTime = if (isVehicleClosed) null else vehicleWait,
                        isClosed = isVehicleClosed
                    )
                    LaneSummaryRow(
                        icon = Icons.Default.Verified,
                        label = "SENTRI",
                        waitTime = if (isSentriClosed) null else sentriWait,
                        isClosed = isSentriClosed
                    )
                    LaneSummaryRow(
                        icon = Icons.Default.Bolt,
                        label = "READY",
                        waitTime = if (isReadyClosed) null else readyWait,
                        isClosed = isReadyClosed
                    )
                    LaneSummaryRow(
                        icon = Icons.Default.DirectionsWalk,
                        label = stringResource(R.string.pedestrian_label),
                        waitTime = if (isPedestrianClosed) null else pedestrianWait,
                        isClosed = isPedestrianClosed
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(isOpen: Boolean) {
    val color = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgColor = if (isOpen) Color(0xFF1B2E2A) else Color(0xFF2E1B1B)
    // Changed resource names from open/closed to status_open/status_closed to avoid potential conflicts with Kotlin keywords in R class generation
    val text = if (isOpen) stringResource(R.string.status_open) else stringResource(R.string.status_closed)
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(13.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(13.dp))
            .padding(horizontal = 13.dp, vertical = 4.5.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun LaneSummaryRow(
    icon: ImageVector,
    label: String,
    waitTime: Int?,
    isClosed: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(13.dp))
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        val waitText = when {
            isClosed -> stringResource(R.string.wait_time_na)
            waitTime == null -> stringResource(R.string.wait_time_na)
            else -> stringResource(R.string.wait_time_format, waitTime)
        }
        
        Text(
            text = waitText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isClosed) Color.White else getWaitTimeColor(waitTime)
        )
    }
}

fun getWaitTimeColor(minutes: Int?): Color {
    if (minutes == null) return Color.White
    return when {
        minutes <= 30 -> WaitTimeGreen
        minutes <= 60 -> WaitTimeYellow
        else -> WaitTimeRed
    }
}

@Composable
fun EmptyDashboard(onNavigateToSearch: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.no_ports_monitored),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onNavigateToSearch,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(stringResource(R.string.explore_ports))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewUiPreview() {
    GaritawatchTheme(darkTheme = true) {
        val andrade = BorderWaitTime(
            portNumber = "1",
            portName = "Andrade",
            crossingName = "",
            border = "Mexico",
            portStatus = "Open",
            lastUpdate = "10:00 AM",
            passengerLanes = listOf(
                LaneDetails(LaneType.STANDARD, "", "Open", 10, 5),
                LaneDetails(LaneType.SENTRI_NEXUS, "", "Open", 45, 2),
                LaneDetails(LaneType.READY, "", "Open", 65, 3)
            ),
            pedestrianLanes = listOf(LaneDetails(LaneType.STANDARD, "", "Open", 0, 2)),
            commercialLanes = emptyList()
        )

        val tecate = BorderWaitTime(
            portNumber = "4",
            portName = "Tecate",
            crossingName = "",
            border = "Mexico",
            portStatus = "Closed",
            lastUpdate = "10:00 AM",
            passengerLanes = listOf(
                LaneDetails(LaneType.STANDARD, "", "Closed", 0, 0),
                LaneDetails(LaneType.SENTRI_NEXUS, "", "Closed", 0, 0),
                LaneDetails(LaneType.READY, "", "Closed", 0, 0)
            ),
            pedestrianLanes = listOf(LaneDetails(LaneType.STANDARD, "", "Closed", 0, 0)),
            commercialLanes = emptyList()
        )

        Surface(color = Color(0xFF0F172A)) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PortCard(andrade)
                PortCard(tecate)
            }
        }
    }
}
