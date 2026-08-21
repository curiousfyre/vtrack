import Foundation
import SwiftData

@Observable
final class FuelEntryViewModel {
    var date = Date()
    var odometerString = ""
    var gallonsString = ""
    var pricePerGallonString = ""
    var totalCostString = ""
    var isPartialFill = false
    var notes = ""
    var error: String?
    var isSaved = false

    var isEditing: Bool { editingEntry != nil }
    private var editingEntry: FuelEntry?
    private var vehicle: Vehicle?
    private var modelContext: ModelContext

    init(modelContext: ModelContext, vehicleId: String?, entryId: String? = nil) {
        self.modelContext = modelContext

        if let vehicleId {
            let descriptor = FetchDescriptor<Vehicle>()
            let vehicles = (try? modelContext.fetch(descriptor)) ?? []
            self.vehicle = vehicles.first(where: { $0.persistentModelID.hashValue.description == vehicleId })
        }

        if let entryId {
            let descriptor = FetchDescriptor<FuelEntry>()
            let entries = (try? modelContext.fetch(descriptor)) ?? []
            if let entry = entries.first(where: { $0.persistentModelID.hashValue.description == entryId }) {
                self.editingEntry = entry
                self.date = entry.date
                self.odometerString = String(entry.odometer)
                self.gallonsString = String(entry.gallons)
                self.pricePerGallonString = String(entry.pricePerGallon)
                self.totalCostString = String(entry.totalCost)
                self.isPartialFill = entry.isPartialFill
                self.notes = entry.notes ?? ""
                self.vehicle = entry.vehicle
            }
        }
    }

    func autoCalculateTotalCost() {
        guard let gallons = Double(gallonsString),
              let price = Double(pricePerGallonString) else { return }
        let total = (gallons * price * 100).rounded() / 100
        totalCostString = String(format: "%.2f", total)
    }

    func save() -> Bool {
        guard let odometer = Int(odometerString), odometer > 0 else {
            error = "Valid odometer required"
            return false
        }
        guard let gallons = Double(gallonsString), gallons > 0 else {
            error = "Valid gallons required"
            return false
        }
        guard let pricePerGallon = Double(pricePerGallonString), pricePerGallon > 0 else {
            error = "Valid price required"
            return false
        }
        let totalCost = Double(totalCostString) ?? (gallons * pricePerGallon)

        if let entry = editingEntry {
            entry.date = date
            entry.odometer = odometer
            entry.gallons = gallons
            entry.pricePerGallon = pricePerGallon
            entry.totalCost = totalCost
            entry.isPartialFill = isPartialFill
            entry.notes = notes.isEmpty ? nil : notes
        } else {
            guard let vehicle else {
                error = "No vehicle selected"
                return false
            }
            let entry = FuelEntry(
                vehicle: vehicle,
                date: date,
                odometer: odometer,
                gallons: gallons,
                pricePerGallon: pricePerGallon,
                totalCost: totalCost,
                isPartialFill: isPartialFill,
                notes: notes.isEmpty ? nil : notes
            )
            modelContext.insert(entry)
        }
        try? modelContext.save()
        isSaved = true
        return true
    }
}
