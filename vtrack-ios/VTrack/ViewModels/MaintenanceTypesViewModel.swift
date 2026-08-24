import Foundation
import SwiftData

@Observable
final class MaintenanceTypesViewModel {
    var statuses: [MaintenanceStatus] = []
    var activeVehicle: Vehicle?
    var isLoading = true

    private var modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func load() {
        isLoading = true
        guard let vehicle = loadActiveVehicle() else {
            activeVehicle = nil
            statuses = []
            isLoading = false
            return
        }
        activeVehicle = vehicle

        let currentOdometer = vehicle.fuelEntries.map(\.odometer).max() ?? vehicle.initialOdometer ?? 0
        let activeTypes = vehicle.maintenanceTypes.filter { $0.isActive }

        statuses = activeTypes.map { type in
            let latestRecord = type.records.sorted(by: { $0.odometer > $1.odometer }).first
            return MaintenanceDueCalculator.calculate(
                type: type,
                lastRecord: latestRecord,
                currentOdometer: currentOdometer,
                vehicleInitialOdometer: vehicle.initialOdometer ?? 0
            )
        }.sorted { s1, s2 in
            let order: (MaintenanceUrgency) -> Int = { switch $0 { case .overdue: return 0; case .dueSoon: return 1; case .ok: return 2 } }
            return order(s1.urgency) < order(s2.urgency)
        }
        isLoading = false
    }

    func deleteType(_ type: MaintenanceType) {
        type.isActive = false
        try? modelContext.save()
        load()
    }

    private func loadActiveVehicle() -> Vehicle? {
        let descriptor = FetchDescriptor<Vehicle>(
            predicate: #Predicate { $0.isActive },
            sortBy: [SortDescriptor(\.name)]
        )
        let vehicles = (try? modelContext.fetch(descriptor)) ?? []
        let savedId = UserDefaults.standard.string(forKey: "activeVehicleId")
        if let savedId, let v = vehicles.first(where: { $0.persistentModelID.hashValue.description == savedId }) {
            return v
        }
        return vehicles.first
    }
}
