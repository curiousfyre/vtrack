import Foundation
import SwiftData

@Observable
final class DashboardViewModel {
    var vehicles: [Vehicle] = []
    var activeVehicle: Vehicle?
    var currentOdometer: Int = 0
    var lastFuelEntry: FuelEntry?
    var latestMpg: Double?
    var maintenanceStatuses: [MaintenanceStatus] = []
    var totalFuelCost: Double = 0

    private var modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func load() {
        let descriptor = FetchDescriptor<Vehicle>(
            predicate: #Predicate { $0.isActive },
            sortBy: [SortDescriptor(\.name)]
        )
        vehicles = (try? modelContext.fetch(descriptor)) ?? []

        let savedId = UserDefaults.standard.string(forKey: "activeVehicleId")
        if let savedId, let vehicle = vehicles.first(where: { $0.persistentModelID.hashValue.description == savedId }) {
            activeVehicle = vehicle
        } else {
            activeVehicle = vehicles.first
        }

        if let vehicle = activeVehicle {
            loadVehicleData(vehicle)
        }
    }

    func selectVehicle(_ vehicle: Vehicle) {
        activeVehicle = vehicle
        UserDefaults.standard.set(vehicle.persistentModelID.hashValue.description, forKey: "activeVehicleId")
        loadVehicleData(vehicle)
    }

    private func loadVehicleData(_ vehicle: Vehicle) {
        let entries = vehicle.fuelEntries.sorted { $0.odometer > $1.odometer }
        lastFuelEntry = entries.first
        currentOdometer = entries.first?.odometer ?? vehicle.initialOdometer ?? 0
        totalFuelCost = entries.reduce(0) { $0 + $1.totalCost }

        if let latest = entries.first, !latest.isPartialFill {
            let previousFull = entries.dropFirst().first(where: { !$0.isPartialFill })
            if let prev = previousFull {
                let gallonsBetween = entries.filter { $0.odometer > prev.odometer && $0.odometer <= latest.odometer }.reduce(0.0) { $0 + $1.gallons }
                latestMpg = MpgCalculator.calculateMpg(
                    currentOdometer: latest.odometer,
                    previousFullFillOdometer: prev.odometer,
                    totalGallonsSinceLastFullFill: gallonsBetween,
                    isCurrentPartial: false
                )
            } else {
                latestMpg = nil
            }
        } else {
            latestMpg = nil
        }

        let activeTypes = vehicle.maintenanceTypes.filter { $0.isActive }
        maintenanceStatuses = activeTypes.map { type in
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
    }
}
