import Foundation
import SwiftData

@Model
final class MaintenanceType {
    var vehicle: Vehicle?
    var name: String
    var intervalMiles: Int
    var intervalMonths: Int?
    var descriptionText: String?
    var isActive: Bool
    var createdAt: Date

    @Relationship(deleteRule: .cascade, inverse: \MaintenanceRecord.maintenanceType)
    var records: [MaintenanceRecord]

    init(vehicle: Vehicle, name: String, intervalMiles: Int, intervalMonths: Int? = nil, descriptionText: String? = nil) {
        self.vehicle = vehicle
        self.name = name
        self.intervalMiles = intervalMiles
        self.intervalMonths = intervalMonths
        self.descriptionText = descriptionText
        self.isActive = true
        self.createdAt = Date()
        self.records = []
    }
}
