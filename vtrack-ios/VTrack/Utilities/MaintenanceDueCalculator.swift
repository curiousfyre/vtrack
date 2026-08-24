import Foundation

enum MaintenanceDueCalculator {
    static func calculate(type: MaintenanceType, lastRecord: MaintenanceRecord?, currentOdometer: Int, vehicleInitialOdometer: Int = 0) -> MaintenanceStatus {
        let baseOdometer = lastRecord?.odometer ?? vehicleInitialOdometer
        let milesSince = currentOdometer - baseOdometer
        let milesUntilDue = type.intervalMiles - milesSince
        let percentUsed = Double(milesSince) / Double(type.intervalMiles)

        let urgency: MaintenanceUrgency
        if percentUsed >= 1.0 {
            urgency = .overdue
        } else if percentUsed >= 0.9 {
            urgency = .dueSoon
        } else {
            urgency = .ok
        }

        return MaintenanceStatus(
            type: type,
            lastServiceOdometer: lastRecord?.odometer,
            lastServiceDate: lastRecord?.date,
            currentOdometer: currentOdometer,
            milesSinceService: milesSince,
            milesUntilDue: milesUntilDue,
            percentUsed: percentUsed,
            urgency: urgency
        )
    }
}
