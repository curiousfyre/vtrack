import Foundation
import SwiftData

@Model
final class MaintenanceRecord {
    var maintenanceType: MaintenanceType?
    var vehicle: Vehicle?
    var date: Date
    var odometer: Int
    var cost: Double?
    var notes: String?
    var createdAt: Date

    init(maintenanceType: MaintenanceType, vehicle: Vehicle, date: Date, odometer: Int, cost: Double? = nil, notes: String? = nil) {
        self.maintenanceType = maintenanceType
        self.vehicle = vehicle
        self.date = date
        self.odometer = odometer
        self.cost = cost
        self.notes = notes
        self.createdAt = Date()
    }
}
