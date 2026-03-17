package com.porroe.garitawatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.porroe.garitawatch.ui.alerts.AlertsScreen
import com.porroe.garitawatch.ui.dashboard.DashboardScreen
import com.porroe.garitawatch.ui.dashboard.DashboardViewModel
import com.porroe.garitawatch.ui.detail.PortDetailScreen
import com.porroe.garitawatch.ui.detail.PortDetailViewModel
import com.porroe.garitawatch.ui.premium.PremiumScreen
import com.porroe.garitawatch.ui.search.SearchScreen
import com.porroe.garitawatch.ui.search.SearchViewModel
import com.porroe.garitawatch.ui.theme.GaritawatchTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("dashboard", "Home", Icons.Default.Home)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    object Premium : Screen("premium", "Premium", Icons.Default.Star)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaritawatchTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Home,
        Screen.Alerts,
        Screen.Premium
    )

    // Only show bottom bar on top-level destinations
    val showBottomBar = items.any { it.route == currentDestination?.route }

    Scaffold(
        containerColor = Color(0xFF0F172A),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1A233A),
                    contentColor = Color.White
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = Color(0xFF94A3B8),
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = Color(0xFF0F172A).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(
                bottom = innerPadding.calculateBottomPadding(),
                start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
            )
        ) {
            composable(Screen.Home.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToDetail = { portNumber ->
                        navController.navigate("detail/$portNumber")
                    }
                )
            }
            composable(Screen.Alerts.route) {
                AlertsScreen()
            }
            composable(Screen.Premium.route) {
                PremiumScreen()
            }
            composable("search") {
                val viewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "detail/{portNumber}",
                arguments = listOf(navArgument("portNumber") { type = NavType.StringType })
            ) {
                val viewModel: PortDetailViewModel = hiltViewModel()
                PortDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
