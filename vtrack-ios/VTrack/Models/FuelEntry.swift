import Foundation
import SwiftData

@Model
final class FuelEntry {
    var vehicle: Vehicle?
    var date: Date
    var odometer: Int
    var gallons: Double
    var pricePerGallon: Double
    var totalCost: Double
    var isPartialFill: Bool
    var notes: String?
    var createdAt: Date

    init(vehicle: Vehicle, date: Date, odometer: Int, gallons: Double, pricePerGallon: Double, totalCost: Double, isPartialFill: Bool = false, notes: String? = nil) {
        self.vehicle = vehicle
        self.date = date
        self.odometer = odometer
        self.gallons = gallons
        self.pricePerGallon = pricePerGallon
        self.totalCost = totalCost
        self.isPartialFill = isPartialFill
        self.notes = notes
        self.createdAt = Date()
    }
}
