import SwiftUI
import SwiftData

@main
struct VTrackApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .modelContainer(for: [Vehicle.self, FuelEntry.self, MaintenanceType.self, MaintenanceRecord.self])
    }
}
