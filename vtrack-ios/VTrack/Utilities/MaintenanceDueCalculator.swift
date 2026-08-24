import Foundation

enum MaintenanceDueCalculator {
    static func calculate(type: MaintenanceType, lastRecord: MaintenanceRecord?, currentOdometer: Int, vehicleInitialOdometer: Int = 0) -> MaintenanceStatus {
        let baseOdometer = lastRecord?.odometer ?? vehicleInitialOdometer

        let nextDueMiles: Int
        let effectiveInterval: Int

        if let override = type.nextDueOdometer,
           lastRecord == nil || (lastRecord!.odometer < override) {
            nextDueMiles = override
            effectiveInterval = override - baseOdometer
        } else {
            nextDueMiles = baseOdometer + type.intervalMiles
            effectiveInterval = type.intervalMiles
        }

        let milesSince = currentOdometer - baseOdometer
        let milesUntilDue = nextDueMiles - currentOdometer
        let percentUsed = effectiveInterval > 0 ? Double(milesSince) / Double(effectiveInterval) : 0.0

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
