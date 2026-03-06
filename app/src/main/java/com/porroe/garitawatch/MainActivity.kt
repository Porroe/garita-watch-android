package com.porroe.garitawatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.porroe.garitawatch.ui.dashboard.DashboardScreen
import com.porroe.garitawatch.ui.dashboard.DashboardViewModel
import com.porroe.garitawatch.ui.search.SearchScreen
import com.porroe.garitawatch.ui.search.SearchViewModel
import com.porroe.garitawatch.ui.theme.GaritawatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaritawatchTheme {
                GaritaWatchNavHost()
            }
        }
    }
}

@Composable
fun GaritaWatchNavHost() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            val viewModel: DashboardViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSearch = { navController.navigate("search") }
            )
        }
        composable("search") {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
