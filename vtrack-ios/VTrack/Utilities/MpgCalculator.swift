import Foundation

enum MpgCalculator {
    static func calculateMpg(currentOdometer: Int, previousFullFillOdometer: Int, totalGallonsSinceLastFullFill: Double, isCurrentPartial: Bool) -> Double? {
        if isCurrentPartial { return nil }
        if totalGallonsSinceLastFullFill <= 0 { return nil }
        let miles = currentOdometer - previousFullFillOdometer
        if miles <= 0 { return nil }
        return Double(miles) / totalGallonsSinceLastFullFill
    }

    static func calculateAverageMpg(_ values: [Double]) -> Double? {
        guard !values.isEmpty else { return nil }
        return values.reduce(0, +) / Double(values.count)
    }

    static func calculateCostPerMile(totalCost: Double, totalMiles: Int) -> Double? {
        guard totalMiles > 0 else { return nil }
        return totalCost / Double(totalMiles)
    }
}
