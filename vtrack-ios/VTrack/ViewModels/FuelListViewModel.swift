import Foundation
import SwiftData

@Observable
final class FuelListViewModel {
    var entries: [FuelEntryWithMpg] = []
    var averageMpg: Double?
    var activeVehicle: Vehicle?
    var isLoading = true

    private var modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func load() {
        isLoading = true
        guard let vehicle = loadActiveVehicle() else {
            activeVehicle = nil
            entries = []
            averageMpg = nil
            isLoading = false
            return
        }
        activeVehicle = vehicle
        entries = calculateMpgForEntries(vehicle: vehicle)
        averageMpg = MpgCalculator.calculateAverageMpg(entries.compactMap(\.mpg))
        isLoading = false
    }

    func deleteEntry(_ entry: FuelEntry) {
        modelContext.delete(entry)
        try? modelContext.save()
        load()
    }

    private func loadActiveVehicle() -> Vehicle? {
        let descriptor = FetchDescriptor<Vehicle>(
            predicate: #Predicate { $0.isActive },
            sortBy: [SortDescriptor(\.name)]
        )
        let vehicles = (try? modelContext.fetch(descriptor)) ?? []
        let savedId = UserDefaults.standard.string(forKey: "activeVehicleId")
        if let savedId, let v = vehicles.first(where: { $0.persistentModelID.hashValue.description == savedId }) {
            return v
        }
        return vehicles.first
    }

    private func calculateMpgForEntries(vehicle: Vehicle) -> [FuelEntryWithMpg] {
        let sorted = vehicle.fuelEntries.sorted { $0.odometer < $1.odometer }
        var results: [FuelEntryWithMpg] = []

        for (index, entry) in sorted.enumerated() {
            var mpg: Double? = nil
            if index > 0 && !entry.isPartialFill {
                let previousFull = sorted[0..<index].last(where: { !$0.isPartialFill })
                if let prev = previousFull {
                    let gallonsBetween = sorted.filter { $0.odometer > prev.odometer && $0.odometer <= entry.odometer }
                        .reduce(0.0) { $0 + $1.gallons }
                    mpg = MpgCalculator.calculateMpg(
                        currentOdometer: entry.odometer,
                        previousFullFillOdometer: prev.odometer,
                        totalGallonsSinceLastFullFill: gallonsBetween,
                        isCurrentPartial: false
                    )
                }
            }
            results.append(FuelEntryWithMpg(entry: entry, mpg: mpg))
        }

        return results.reversed()
    }
}
