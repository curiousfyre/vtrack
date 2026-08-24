import Foundation
import SwiftData

struct MpgDataPoint: Identifiable {
    let id = UUID()
    let date: Date
    let mpg: Double
}

struct MonthlySpending: Identifiable {
    let id = UUID()
    let month: String
    let amount: Double
}

@Observable
final class StatsViewModel {
    var mpgData: [MpgDataPoint] = []
    var monthlySpending: [MonthlySpending] = []
    var averageMpg: Double?
    var bestMpg: Double?
    var worstMpg: Double?
    var totalGallons: Double = 0
    var totalSpent: Double = 0
    var totalMiles: Int = 0
    var costPerMile: Double?
    var fillUpCount: Int = 0
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
            isLoading = false
            return
        }
        activeVehicle = vehicle
        buildStats(vehicle: vehicle)
        isLoading = false
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

    private func buildStats(vehicle: Vehicle) {
        let entries = vehicle.fuelEntries.sorted { $0.odometer < $1.odometer }
        fillUpCount = entries.count
        totalGallons = entries.reduce(0) { $0 + $1.gallons }
        totalSpent = entries.reduce(0) { $0 + $1.totalCost }

        if let first = entries.first, let last = entries.last {
            totalMiles = last.odometer - first.odometer
        }
        costPerMile = MpgCalculator.calculateCostPerMile(totalCost: totalSpent, totalMiles: totalMiles)

        var mpgValues: [Double] = []
        var mpgPoints: [MpgDataPoint] = []

        for (index, entry) in entries.enumerated() {
            if index > 0 && !entry.isPartialFill {
                let previousFull = entries[0..<index].last(where: { !$0.isPartialFill })
                if let prev = previousFull {
                    let gallonsBetween = entries.filter { $0.odometer > prev.odometer && $0.odometer <= entry.odometer }
                        .reduce(0.0) { $0 + $1.gallons }
                    if let mpg = MpgCalculator.calculateMpg(
                        currentOdometer: entry.odometer,
                        previousFullFillOdometer: prev.odometer,
                        totalGallonsSinceLastFullFill: gallonsBetween,
                        isCurrentPartial: false
                    ) {
                        mpgValues.append(mpg)
                        mpgPoints.append(MpgDataPoint(date: entry.date, mpg: mpg))
                    }
                }
            }
        }

        mpgData = mpgPoints.sorted { $0.date < $1.date }
        averageMpg = MpgCalculator.calculateAverageMpg(mpgValues)
        bestMpg = mpgValues.max()
        worstMpg = mpgValues.min()

        let monthFormatter = DateFormatter()
        monthFormatter.dateFormat = "MMM yyyy"
        var monthMap: [String: Double] = [:]
        var monthOrder: [String] = []
        for entry in entries {
            let key = monthFormatter.string(from: entry.date)
            if monthMap[key] == nil { monthOrder.append(key) }
            monthMap[key, default: 0] += entry.totalCost
        }
        monthlySpending = monthOrder.map { MonthlySpending(month: $0, amount: monthMap[$0]!) }
    }
}
