import Foundation
import SwiftData

@Observable
final class VehicleFormViewModel {
    var name = ""
    var make = ""
    var model = ""
    var yearString = ""
    var initialOdometerString = ""

    var isEditing: Bool { editingVehicle != nil }
    private var editingVehicle: Vehicle?
    private var modelContext: ModelContext

    init(modelContext: ModelContext, vehicle: Vehicle? = nil) {
        self.modelContext = modelContext
        if let vehicle {
            self.editingVehicle = vehicle
            self.name = vehicle.name
            self.make = vehicle.make
            self.model = vehicle.model
            self.yearString = String(vehicle.year)
            if let odo = vehicle.initialOdometer {
                self.initialOdometerString = String(odo)
            }
        }
    }

    var isValid: Bool {
        !name.isEmpty && !make.isEmpty && !model.isEmpty && Int(yearString) != nil
    }

    func save() -> Bool {
        guard let year = Int(yearString) else { return false }
        let initialOdometer = Int(initialOdometerString)

        if let vehicle = editingVehicle {
            vehicle.name = name
            vehicle.make = make
            vehicle.model = model
            vehicle.year = year
            vehicle.initialOdometer = initialOdometer
        } else {
            let vehicle = Vehicle(name: name, make: make, model: model, year: year, initialOdometer: initialOdometer)
            modelContext.insert(vehicle)
        }
        try? modelContext.save()
        return true
    }
}
