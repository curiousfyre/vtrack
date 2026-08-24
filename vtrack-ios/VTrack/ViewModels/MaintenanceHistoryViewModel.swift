import Foundation
import SwiftData

@Observable
final class MaintenanceHistoryViewModel {
    var typeName = ""
    var vehicleId: String?
    var typeId: String?
    var status: MaintenanceStatus?
    var records: [MaintenanceRecord] = []
    var isLoading = true

    private var maintenanceType: MaintenanceType?
    private var modelContext: ModelContext

    init(modelContext: ModelContext, typeId: String) {
        self.modelContext = modelContext
        self.typeId = typeId
        load()
    }

    func load() {
        isLoading = true
        let descriptor = FetchDescriptor<MaintenanceType>()
        let types = (try? modelContext.fetch(descriptor)) ?? []
        guard let type = types.first(where: { $0.persistentModelID.hashValue.description == typeId }) else {
            isLoading = false
            return
        }
        self.maintenanceType = type
        self.typeName = type.name
        self.vehicleId = type.vehicle?.persistentModelID.hashValue.description

        records = type.records.sorted { $0.date > $1.date }

        let vehicle = type.vehicle
        let currentOdometer = vehicle?.fuelEntries.map(\.odometer).max() ?? vehicle?.initialOdometer ?? 0
        let latestRecord = type.records.sorted(by: { $0.odometer > $1.odometer }).first

        status = MaintenanceDueCalculator.calculate(
            type: type,
            lastRecord: latestRecord,
            currentOdometer: currentOdometer,
            vehicleInitialOdometer: vehicle?.initialOdometer ?? 0
        )
        isLoading = false
    }
}
