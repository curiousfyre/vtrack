import SwiftUI
import SwiftData

struct ContentView: View {
    @State private var selectedTab = 0
    @State private var dashboardPath = NavigationPath()
    @State private var fuelPath = NavigationPath()
    @State private var maintenancePath = NavigationPath()

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack(path: $dashboardPath) {
                DashboardScreen(
                    onAddVehicle: { dashboardPath.append(AppRoute.vehicleForm(nil)) },
                    onNavigateToFuelEntry: { vehicleId in dashboardPath.append(AppRoute.fuelEntry(vehicleId, nil)) },
                    onNavigateToMaintenanceTypes: { selectedTab = 2 }
                )
                .navigationTitle("Autometer")
                .overflowMenu(path: $dashboardPath)
                .navigationDestinations(path: $dashboardPath)
            }
            .tabItem { Label("Dashboard", systemImage: "house.fill") }
            .tag(0)

            NavigationStack(path: $fuelPath) {
                FuelListScreen(
                    onAddEntry: { vehicleId in fuelPath.append(AppRoute.fuelEntry(vehicleId, nil)) },
                    onEditEntry: { vehicleId, entryId in fuelPath.append(AppRoute.fuelEntry(vehicleId, entryId)) }
                )
                .navigationTitle("Fuel")
                .overflowMenu(path: $fuelPath)
                .navigationDestinations(path: $fuelPath)
            }
            .tabItem { Label("Fuel", systemImage: "fuelpump.fill") }
            .tag(1)

            NavigationStack(path: $maintenancePath) {
                MaintenanceTypesScreen(
                    onAddType: { vehicleId in maintenancePath.append(AppRoute.maintenanceTypeForm(vehicleId, nil)) },
                    onEditType: { vehicleId, typeId in maintenancePath.append(AppRoute.maintenanceTypeForm(vehicleId, typeId)) },
                    onViewHistory: { typeId in maintenancePath.append(AppRoute.maintenanceHistory(typeId)) }
                )
                .navigationTitle("Maintenance")
                .overflowMenu(path: $maintenancePath)
                .navigationDestinations(path: $maintenancePath)
            }
            .tabItem { Label("Maintenance", systemImage: "wrench.and.screwdriver.fill") }
            .tag(2)
        }
    }
}

enum AppRoute: Hashable {
    case vehicleList
    case vehicleForm(String?)
    case fuelEntry(String, String?)
    case maintenanceTypeForm(String, String?)
    case maintenanceHistory(String)
    case logMaintenance(String, String?)
    case stats
    case settings
}

extension View {
    func overflowMenu(path: Binding<NavigationPath>) -> some View {
        toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button { path.wrappedValue.append(AppRoute.vehicleList) } label: {
                        Label("Vehicles", systemImage: "car.fill")
                    }
                    Button { path.wrappedValue.append(AppRoute.stats) } label: {
                        Label("Statistics", systemImage: "chart.bar.fill")
                    }
                    Button { path.wrappedValue.append(AppRoute.settings) } label: {
                        Label("Settings", systemImage: "gearshape.fill")
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
            }
        }
    }

    func navigationDestinations(path: Binding<NavigationPath>) -> some View {
        navigationDestination(for: AppRoute.self) { route in
            switch route {
            case .vehicleList:
                VehicleListScreen(
                    onAddVehicle: { path.wrappedValue.append(AppRoute.vehicleForm(nil)) },
                    onEditVehicle: { vehicleId in path.wrappedValue.append(AppRoute.vehicleForm(vehicleId)) }
                )
                .navigationTitle("Vehicles")
            case .vehicleForm(let vehicleId):
                VehicleFormScreen(vehicleId: vehicleId)
            case .fuelEntry(let vehicleId, let entryId):
                FuelEntryScreen(vehicleId: vehicleId, entryId: entryId)
            case .maintenanceTypeForm(let vehicleId, let typeId):
                MaintenanceTypeFormScreen(vehicleId: vehicleId, typeId: typeId)
            case .maintenanceHistory(let typeId):
                MaintenanceHistoryScreen(typeId: typeId) { vehicleId, typeId in
                    path.wrappedValue.append(AppRoute.logMaintenance(vehicleId, typeId))
                }
            case .logMaintenance(let vehicleId, let typeId):
                LogMaintenanceScreen(vehicleId: vehicleId, typeId: typeId)
            case .stats:
                StatsScreen()
                    .navigationTitle("Statistics")
            case .settings:
                SettingsScreen()
                    .navigationTitle("Settings")
            }
        }
    }
}
