import Foundation
import SwiftData

struct MaintenancePreset {
    let name: String
    let intervalMiles: Int
}

let maintenancePresets = [
    MaintenancePreset(name: "Oil Change", intervalMiles: 5000),
    MaintenancePreset(name: "Tire Rotation", intervalMiles: 7500),
    MaintenancePreset(name: "Air Filter", intervalMiles: 15000),
    MaintenancePreset(name: "Brake Inspection", intervalMiles: 20000),
    MaintenancePreset(name: "Transmission Fluid", intervalMiles: 30000),
    MaintenancePreset(name: "Spark Plugs", intervalMiles: 30000),
    MaintenancePreset(name: "Coolant Flush", intervalMiles: 30000),
    MaintenancePreset(name: "Timing Belt", intervalMiles: 60000),
]

@Observable
final class MaintenanceTypeFormViewModel {
    var name = ""
    var intervalMilesString = ""
    var intervalMonthsString = ""
    var descriptionText = ""
    var selectedPresetIndex: Int?
    var error: String?
    var isSaved = false

    var isEditing: Bool { editingType != nil }
    private var editingType: MaintenanceType?
    private var vehicle: Vehicle?
    private var modelContext: ModelContext

    init(modelContext: ModelContext, vehicleId: String?, typeId: String? = nil) {
        self.modelContext = modelContext

        if let vehicleId {
            let descriptor = FetchDescriptor<Vehicle>()
            let vehicles = (try? modelContext.fetch(descriptor)) ?? []
            self.vehicle = vehicles.first(where: { $0.persistentModelID.hashValue.description == vehicleId })
        }

        if let typeId {
            let descriptor = FetchDescriptor<MaintenanceType>()
            let types = (try? modelContext.fetch(descriptor)) ?? []
            if let type = types.first(where: { $0.persistentModelID.hashValue.description == typeId }) {
                self.editingType = type
                self.name = type.name
                self.intervalMilesString = String(type.intervalMiles)
                if let months = type.intervalMonths {
                    self.intervalMonthsString = String(months)
                }
                self.descriptionText = type.descriptionText ?? ""
                self.vehicle = type.vehicle
            }
        }
    }

    func selectPreset(_ index: Int) {
        selectedPresetIndex = index
        let preset = maintenancePresets[index]
        name = preset.name
        intervalMilesString = String(preset.intervalMiles)
    }

    func save() -> Bool {
        guard !name.isEmpty else {
            error = "Name is required"
            return false
        }
        guard let intervalMiles = Int(intervalMilesString), intervalMiles > 0 else {
            error = "Valid interval required"
            return false
        }
        let intervalMonths = Int(intervalMonthsString)

        if let type = editingType {
            type.name = name
            type.intervalMiles = intervalMiles
            type.intervalMonths = intervalMonths
            type.descriptionText = descriptionText.isEmpty ? nil : descriptionText
        } else {
            guard let vehicle else {
                error = "No vehicle selected"
                return false
            }
            let type = MaintenanceType(
                vehicle: vehicle,
                name: name,
                intervalMiles: intervalMiles,
                intervalMonths: intervalMonths,
                descriptionText: descriptionText.isEmpty ? nil : descriptionText
            )
            modelContext.insert(type)
        }
        try? modelContext.save()
        isSaved = true
        return true
    }
}
