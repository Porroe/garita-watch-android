package com.porroe.garitawatch.ui.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.porroe.garitawatch.R
import com.porroe.garitawatch.domain.model.BorderWaitTime
import com.porroe.garitawatch.ui.theme.GaritawatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.find_ports), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.padding(16.dp)
            )

            FilterChips(
                showVehicle = uiState.showVehicle,
                onToggleVehicle = viewModel::toggleVehicleFilter,
                showPedestrian = uiState.showPedestrian,
                onTogglePedestrian = viewModel::togglePedestrianFilter,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.filteredPorts, key = { it.portNumber }) { port ->
                    SearchPortItem(
                        port = port,
                        isMonitored = uiState.monitoredPortNumbers.contains(port.portNumber),
                        onToggleMonitored = { viewModel.toggleMonitored(port) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    showVehicle: Boolean,
    onToggleVehicle: () -> Unit,
    showPedestrian: Boolean,
    onTogglePedestrian: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = showVehicle,
            onClick = onToggleVehicle,
            label = { Text(stringResource(R.string.vehicles)) },
            leadingIcon = { Icon(Icons.Default.DirectionsBus, null, Modifier.size(18.dp)) }
        )
        FilterChip(
            selected = showPedestrian,
            onClick = onTogglePedestrian,
            label = { Text(stringResource(R.string.pedestrians)) },
            leadingIcon = { Icon(Icons.Default.DirectionsWalk, null, Modifier.size(18.dp)) }
        )
    }
}

@Composable
fun SearchPortItem(
    port: BorderWaitTime,
    isMonitored: Boolean,
    onToggleMonitored: () -> Unit
) {
    val starColor by animateColorAsState(
        if (isMonitored) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "starColor"
    )

    Surface(
        onClick = onToggleMonitored,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = port.portName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = port.crossingName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onToggleMonitored) {
                Icon(
                    imageVector = if (isMonitored) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Toggle Watchlist",
                    tint = starColor
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SearchScreenPreview() {
    GaritawatchTheme {
        val mockPort = BorderWaitTime(
            portNumber = "1",
            portName = "San Ysidro",
            crossingName = "El Chaparral",
            border = "Mexico",
            portStatus = "Open",
            lastUpdate = "2023-10-27 10:00 AM",
            passengerLanes = emptyList(),
            pedestrianLanes = emptyList(),
            commercialLanes = emptyList()
        )
        
        Scaffold { padding ->
            Column(modifier = Modifier.padding(padding)) {
                SearchBar("", {}, Modifier.padding(16.dp))
                FilterChips(true, {}, true, {}, Modifier.padding(horizontal = 16.dp))
                SearchPortItem(mockPort, true, {})
                SearchPortItem(mockPort.copy(portNumber = "2", portName = "Otay Mesa"), false, {})
            }
        }
    }
}
