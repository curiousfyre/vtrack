import Foundation
import SwiftData
import SwiftUI

@Observable
final class VehicleListViewModel {
    var vehicles: [Vehicle] = []
    var vehicleToDelete: Vehicle?
    var showDeleteConfirmation = false

    private var modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        fetchVehicles()
    }

    func fetchVehicles() {
        let descriptor = FetchDescriptor<Vehicle>(
            predicate: #Predicate { $0.isActive },
            sortBy: [SortDescriptor(\.name)]
        )
        vehicles = (try? modelContext.fetch(descriptor)) ?? []
    }

    func confirmDelete(_ vehicle: Vehicle) {
        vehicleToDelete = vehicle
        showDeleteConfirmation = true
    }

    func deleteVehicle() {
        guard let vehicle = vehicleToDelete else { return }
        vehicle.isActive = false
        try? modelContext.save()
        fetchVehicles()
        vehicleToDelete = nil
    }
}
