package com.porroe.garitawatch.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.porroe.garitawatch.R
import com.porroe.garitawatch.domain.model.BorderWaitTime
import com.porroe.garitawatch.domain.model.LaneDetails
import com.porroe.garitawatch.domain.model.LaneType
import com.porroe.garitawatch.ui.theme.GaritawatchTheme
import com.porroe.garitawatch.ui.theme.WaitTimeGreen
import com.porroe.garitawatch.ui.theme.WaitTimeRed
import com.porroe.garitawatch.ui.theme.WaitTimeYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToSearch: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.manage_ports))
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
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
        shape = RoundedCornerShape(28.dp), // More expressive corners
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = port.portName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = port.crossingName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            if (isPortClosed) {
                Text(
                    text = stringResource(R.string.port_closed),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WaitTimeRed
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    port.passengerLanes.forEach { lane ->
                        LaneRow(lane, stringResource(R.string.vehicles))
                    }
                    port.pedestrianLanes.forEach { lane ->
                        LaneRow(lane, stringResource(R.string.pedestrians))
                    }
                }
            }
        }
    }
}

@Composable
fun LaneRow(lane: LaneDetails, category: String) {
    val isClosed = lane.operationalStatus.equals("Closed", ignoreCase = true)
    val indicatorColor = when {
        isClosed -> Color.Gray
        lane.delayMinutes <= 30 -> WaitTimeGreen
        lane.delayMinutes <= 60 -> WaitTimeYellow
        else -> WaitTimeRed
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$category - ${lane.type.name.replace("_", " ")}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (!isClosed) {
                Text(
                    text = stringResource(R.string.lanes_open_text, lane.lanesOpen),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Text(
            text = if (isClosed) stringResource(R.string.port_closed) else stringResource(R.string.minutes_abbreviation, lane.delayMinutes),
            style = if (isClosed) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = indicatorColor
        )
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
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToSearch,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.explore_ports))
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardPreview() {
    GaritawatchTheme {
        val mockPort = BorderWaitTime(
            portNumber = "1",
            portName = "San Ysidro",
            crossingName = "El Chaparral",
            border = "Mexico",
            portStatus = "Open",
            lastUpdate = "2023-10-27 10:00 AM",
            passengerLanes = listOf(
                LaneDetails(LaneType.STANDARD, "10:00 AM", "Open", 45, 10),
                LaneDetails(LaneType.READY, "10:00 AM", "Open", 20, 5)
            ),
            pedestrianLanes = listOf(
                LaneDetails(LaneType.STANDARD, "10:00 AM", "Open", 15, 2)
            ),
            commercialLanes = emptyList()
        )

        val closedPort = BorderWaitTime(
            portNumber = "2",
            portName = "Otay Mesa",
            crossingName = "Otay Mesa",
            border = "Mexico",
            portStatus = "Closed",
            lastUpdate = "2023-10-27 10:00 AM",
            passengerLanes = emptyList(),
            pedestrianLanes = emptyList(),
            commercialLanes = emptyList()
        )

        val partiallyClosedPort = BorderWaitTime(
            portNumber = "3",
            portName = "Tecate",
            crossingName = "Tecate",
            border = "Mexico",
            portStatus = "Open",
            lastUpdate = "2023-10-27 10:00 AM",
            passengerLanes = listOf(
                LaneDetails(LaneType.STANDARD, "10:00 AM", "Closed", 0, 0),
                LaneDetails(LaneType.READY, "10:00 AM", "Open", 30, 4)
            ),
            pedestrianLanes = listOf(
                LaneDetails(LaneType.STANDARD, "10:00 AM", "Open", 10, 2)
            ),
            commercialLanes = emptyList()
        )



        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PortCard(mockPort)
                PortCard(partiallyClosedPort)
                PortCard(closedPort)
            }
        }
    }
}