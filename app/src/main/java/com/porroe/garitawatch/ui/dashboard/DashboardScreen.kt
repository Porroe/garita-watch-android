package com.porroe.garitawatch.ui.dashboard

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.movePort(from.index, to.index)
    }

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
                        fontSize = 13.8.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color(0xFF94A3B8)
                    )
                    
                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.monitoredPorts, key = { it.portNumber }) { port ->
                            ReorderableItem(reorderableState, key = port.portNumber) { isDragging ->
                                val elevation by animateDpAsState(if (isDragging) 12.dp else 0.dp)
                                val scale by animateFloatAsState(if (isDragging) 1.02f else 1f)
                                val alpha by animateFloatAsState(if (isDragging) 0.9f else 1f)
                                
                                PortCard(
                                    port = port,
                                    modifier = Modifier
                                        .shadow(elevation, shape = RoundedCornerShape(35.dp))
                                        .scale(scale)
                                        .alpha(alpha)
                                        .longPressDraggableHandle(),
                                    isDragging = isDragging,
                                    onClick = { onNavigateToDetail(port.portNumber) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PortCard(
    port: BorderWaitTime,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    onClick: () -> Unit = {}
) {
    val isPortClosed = port.portStatus.equals("Closed", ignoreCase = true)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) Color(0xFF242F4D) else Color(0xFF1A233A)
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
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(isOpen = !isPortClosed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = null,
                        tint = if (isDragging) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8).copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
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
                val vehicleLane = port.passengerLanes.find { it.type == LaneType.STANDARD }
                val sentriLane = port.passengerLanes.find { it.type == LaneType.SENTRI_NEXUS }
                val readyLane = port.passengerLanes.find { it.type == LaneType.READY }
                val pedestrianLane = port.pedestrianLanes.find { it.type == LaneType.STANDARD }

                val displayedLanes = mutableListOf<Pair<LaneDetails, @Composable () -> Unit>>()
                
                vehicleLane?.delayMinutes?.let { waitTime ->
                    displayedLanes.add(vehicleLane to {
                        LaneSummaryRow(
                            icon = Icons.Default.DirectionsCar,
                            label = stringResource(R.string.vehicle_label),
                            waitTime = waitTime,
                            isClosed = vehicleLane.operationalStatus.equals("Closed", ignoreCase = true)
                        )
                    })
                }
                
                sentriLane?.delayMinutes?.let { waitTime ->
                    displayedLanes.add(sentriLane to {
                        LaneSummaryRow(
                            icon = Icons.Default.Verified,
                            label = "SENTRI",
                            waitTime = waitTime,
                            isClosed = sentriLane.operationalStatus.equals("Closed", ignoreCase = true)
                        )
                    })
                }
                
                readyLane?.delayMinutes?.let { waitTime ->
                    displayedLanes.add(readyLane to {
                        LaneSummaryRow(
                            icon = Icons.Default.Bolt,
                            label = "READY",
                            waitTime = waitTime,
                            isClosed = readyLane.operationalStatus.equals("Closed", ignoreCase = true)
                        )
                    })
                }
                
                pedestrianLane?.delayMinutes?.let { waitTime ->
                    displayedLanes.add(pedestrianLane to {
                        LaneSummaryRow(
                            icon = Icons.Default.DirectionsWalk,
                            label = stringResource(R.string.pedestrian_label),
                            waitTime = waitTime,
                            isClosed = pedestrianLane.operationalStatus.equals("Closed", ignoreCase = true)
                        )
                    })
                }

                Spacer(modifier = Modifier.height(35.dp))
                
                if (displayedLanes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_lane_data),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        displayedLanes.forEach { (_, composable) ->
                            composable()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(isOpen: Boolean) {
    val color = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)
    val bgColor = if (isOpen) Color(0xFF1B2E2A) else Color(0xFF2E1B1B)
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
