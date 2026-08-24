import Foundation
import SwiftData

@Observable
final class SettingsViewModel {
    var notificationsEnabled: Bool {
        didSet { UserDefaults.standard.set(notificationsEnabled, forKey: "notificationsEnabled") }
    }
    var exportCSVContent: String?
    var activeVehicle: Vehicle?

    private var modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        self.notificationsEnabled = UserDefaults.standard.object(forKey: "notificationsEnabled") as? Bool ?? true
        loadActiveVehicle()
    }

    private func loadActiveVehicle() {
        let descriptor = FetchDescriptor<Vehicle>(
            predicate: #Predicate { $0.isActive },
            sortBy: [SortDescriptor(\.name)]
        )
        let vehicles = (try? modelContext.fetch(descriptor)) ?? []
        let savedId = UserDefaults.standard.string(forKey: "activeVehicleId")
        if let savedId, let v = vehicles.first(where: { $0.persistentModelID.hashValue.description == savedId }) {
            activeVehicle = v
        } else {
            activeVehicle = vehicles.first
        }
    }

    func exportData() {
        guard let vehicle = activeVehicle else { return }
        let entries = vehicle.fuelEntries.sorted { $0.date < $1.date }

        var csv = "Date,Odometer,Gallons,Price/Gallon,Total Cost,Partial Fill,Notes\n"
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"

        for entry in entries {
            let dateStr = dateFormatter.string(from: entry.date)
            let notes = (entry.notes ?? "")
                .replacingOccurrences(of: ",", with: ";")
                .replacingOccurrences(of: "\n", with: " ")
            csv += "\(dateStr),\(entry.odometer),\(entry.gallons),\(entry.pricePerGallon),\(entry.totalCost),\(entry.isPartialFill),\"\(notes)\"\n"
        }
        exportCSVContent = csv
    }
}
