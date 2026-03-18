package com.garitawatch.app.ui.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.garitawatch.app.R
import com.garitawatch.app.domain.model.BorderWaitTime
import com.garitawatch.app.ui.theme.GaritawatchTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    GaritawatchTheme(darkTheme = true) {
        Scaffold(
            containerColor = Color(0xFF0F172A),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F172A),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    title = { Text(stringResource(R.string.find_ports), fontWeight = FontWeight.Black) },
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFF1A233A)
                    )
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
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
        placeholder = { Text(stringResource(R.string.search_placeholder), color = Color(0xFF94A3B8)) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8)) },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1A233A),
            unfocusedContainerColor = Color(0xFF1A233A),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFF2D3748),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun SearchPortItem(
    port: BorderWaitTime,
    isMonitored: Boolean,
    onToggleMonitored: () -> Unit
) {
    val starColor by animateColorAsState(
        if (isMonitored) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
        label = "starColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleMonitored() },
        shape = RoundedCornerShape(35.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A233A)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(26.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
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
            IconButton(onClick = onToggleMonitored) {
                Icon(
                    imageVector = if (isMonitored) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Toggle Watchlist",
                    tint = starColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    GaritawatchTheme(darkTheme = true) {
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
        
        Surface(color = Color(0xFF0F172A), modifier = Modifier.fillMaxSize()) {
            Column {
                SearchBar("", {}, Modifier.padding(16.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    SearchPortItem(mockPort, isMonitored = true) { }
                    SearchPortItem(mockPort.copy(portNumber = "2", portName = "Otay Mesa"), isMonitored = false) { }
                }
            }
        }
    }
}
