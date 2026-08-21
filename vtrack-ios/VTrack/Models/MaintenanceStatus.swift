import Foundation
import SwiftData

enum MaintenanceUrgency {
    case ok
    case dueSoon
    case overdue
}

struct MaintenanceStatus {
    let type: MaintenanceType
    let lastServiceOdometer: Int?
    let lastServiceDate: Date?
    let currentOdometer: Int
    let milesSinceService: Int
    let milesUntilDue: Int
    let percentUsed: Double
    let urgency: MaintenanceUrgency
}

struct FuelEntryWithMpg: Identifiable {
    let entry: FuelEntry
    let mpg: Double?

    var id: PersistentIdentifier { entry.persistentModelID }
}
