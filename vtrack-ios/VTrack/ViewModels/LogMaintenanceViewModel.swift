import Foundation
import SwiftData

@Observable
final class LogMaintenanceViewModel {
    var typeName = ""
    var date = Date()
    var odometerString = ""
    var costString = ""
    var notes = ""
    var error: String?
    var isSaved = false

    private var vehicle: Vehicle?
    private var maintenanceType: MaintenanceType?
    private var modelContext: ModelContext

    init(modelContext: ModelContext, vehicleId: String, typeId: String?) {
        self.modelContext = modelContext

        let vehicleDescriptor = FetchDescriptor<Vehicle>()
        let vehicles = (try? modelContext.fetch(vehicleDescriptor)) ?? []
        self.vehicle = vehicles.first(where: { $0.persistentModelID.hashValue.description == vehicleId })

        if let typeId {
            let typeDescriptor = FetchDescriptor<MaintenanceType>()
            let types = (try? modelContext.fetch(typeDescriptor)) ?? []
            self.maintenanceType = types.first(where: { $0.persistentModelID.hashValue.description == typeId })
            self.typeName = self.maintenanceType?.name ?? ""
        }

        if let vehicle = self.vehicle {
            let currentOdometer = vehicle.fuelEntries.map(\.odometer).max() ?? vehicle.initialOdometer ?? 0
            odometerString = String(currentOdometer)
        }
    }

    func save() -> Bool {
        guard let odometer = Int(odometerString), odometer > 0 else {
            error = "Valid odometer required"
            return false
        }
        guard let vehicle, let maintenanceType else {
            error = "Missing vehicle or maintenance type"
            return false
        }

        let cost = Double(costString)
        let record = MaintenanceRecord(
            maintenanceType: maintenanceType,
            vehicle: vehicle,
            date: date,
            odometer: odometer,
            cost: cost,
            notes: notes.isEmpty ? nil : notes
        )
        modelContext.insert(record)
        try? modelContext.save()
        isSaved = true
        return true
    }
}
