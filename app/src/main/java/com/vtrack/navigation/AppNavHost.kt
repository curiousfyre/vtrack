package com.vtrack.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vtrack.feature.dashboard.DashboardScreen
import com.vtrack.feature.fuel.entry.FuelEntryScreen
import com.vtrack.feature.fuel.list.FuelListScreen
import com.vtrack.feature.maintenance.history.LogMaintenanceScreen
import com.vtrack.feature.maintenance.history.MaintenanceHistoryScreen
import com.vtrack.feature.maintenance.types.MaintenanceTypeFormScreen
import com.vtrack.feature.maintenance.types.MaintenanceTypesScreen
import com.vtrack.feature.settings.SettingsScreen
import com.vtrack.feature.stats.StatsScreen
import com.vtrack.feature.vehicle.VehicleFormScreen
import com.vtrack.feature.vehicle.VehicleListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = setOf(
        Route.Dashboard.route,
        Route.FuelList.route,
        Route.MaintenanceTypes.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = { if (showBottomBar) BottomNavBar(navController) },
        topBar = {
            TopAppBar(
                title = { Text("VTrack") },
                actions = {
                    if (currentRoute in bottomBarRoutes) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Vehicles") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Route.VehicleList.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Statistics") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Route.Stats.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        menuExpanded = false
                                        navController.navigate(Route.Settings.route)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Route.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Route.FuelList.route) {
                FuelListScreen(
                    navController = navController,
                    onAddFuelEntry = { vehicleId ->
                        navController.navigate(Route.FuelEntry.create(vehicleId))
                    },
                    onEditFuelEntry = { vehicleId, entryId ->
                        navController.navigate(Route.FuelEntry.create(vehicleId, entryId))
                    }
                )
            }
            composable(
                route = "fuel_entry/{vehicleId}?entryId={entryId}",
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType },
                    navArgument("entryId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                FuelEntryScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.MaintenanceTypes.route) {
                MaintenanceTypesScreen(
                    navController = navController,
                    onAddType = { vehicleId ->
                        navController.navigate(Route.MaintenanceTypeForm.create(vehicleId))
                    },
                    onEditType = { vehicleId, typeId ->
                        navController.navigate(Route.MaintenanceTypeForm.create(vehicleId, typeId))
                    },
                    onViewHistory = { typeId ->
                        navController.navigate(Route.MaintenanceHistory.create(typeId))
                    }
                )
            }
            composable(
                route = "maintenance_type_form/{vehicleId}?typeId={typeId}",
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType },
                    navArgument("typeId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                MaintenanceTypeFormScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = "maintenance_history/{typeId}",
                arguments = listOf(navArgument("typeId") { type = NavType.LongType })
            ) {
                MaintenanceHistoryScreen(
                    navController = navController,
                    onLogMaintenance = { vehicleId, typeId ->
                        navController.navigate(Route.LogMaintenance.create(vehicleId, typeId))
                    }
                )
            }
            composable(
                route = "log_maintenance/{vehicleId}?typeId={typeId}",
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType },
                    navArgument("typeId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                LogMaintenanceScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.VehicleList.route) {
                VehicleListScreen(
                    navController = navController,
                    onAddVehicle = { navController.navigate(Route.VehicleForm.create()) },
                    onEditVehicle = { id -> navController.navigate(Route.VehicleForm.create(id)) }
                )
            }
            composable(
                route = "vehicle_form?vehicleId={vehicleId}",
                arguments = listOf(
                    navArgument("vehicleId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                VehicleFormScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.Stats.route) {
                StatsScreen(navController = navController)
            }
            composable(Route.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}
