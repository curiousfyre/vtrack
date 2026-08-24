import Foundation
import SwiftData

@Model
final class Vehicle {
    var name: String
    var make: String
    var model: String
    var year: Int
    var initialOdometer: Int?
    var isActive: Bool
    var createdAt: Date

    @Relationship(deleteRule: .cascade, inverse: \FuelEntry.vehicle)
    var fuelEntries: [FuelEntry]

    @Relationship(deleteRule: .cascade, inverse: \MaintenanceType.vehicle)
    var maintenanceTypes: [MaintenanceType]

    @Relationship(deleteRule: .cascade, inverse: \MaintenanceRecord.vehicle)
    var maintenanceRecords: [MaintenanceRecord]

    init(name: String, make: String, model: String, year: Int, initialOdometer: Int? = nil) {
        self.name = name
        self.make = make
        self.model = model
        self.year = year
        self.initialOdometer = initialOdometer
        self.isActive = true
        self.createdAt = Date()
        self.fuelEntries = []
        self.maintenanceTypes = []
        self.maintenanceRecords = []
    }

    var displayName: String {
        "\(year) \(make) \(model)"
    }
}
