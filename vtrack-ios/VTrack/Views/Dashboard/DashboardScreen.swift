import SwiftUI
import SwiftData

struct DashboardScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: DashboardViewModel?

    var onAddVehicle: () -> Void
    var onNavigateToFuelEntry: (String) -> Void
    var onNavigateToMaintenanceTypes: () -> Void

    var body: some View {
        Group {
            if let vm = viewModel {
                if vm.vehicles.isEmpty {
                    EmptyStateView(
                        icon: "car.fill",
                        title: "Welcome to VTrack!",
                        message: "Add your first vehicle to get started",
                        actionTitle: "Add Vehicle",
                        action: onAddVehicle
                    )
                } else if vm.activeVehicle != nil {
                    ScrollView {
                        VStack(spacing: 16) {
                            if vm.vehicles.count > 1 {
                                vehicleSelector(vm: vm)
                            }
                            odometerCard(odometer: vm.currentOdometer)
                            lastFillUpCard(entry: vm.lastFuelEntry, mpg: vm.latestMpg)
                            maintenanceCard(statuses: vm.maintenanceStatuses)
                            totalSpendCard(total: vm.totalFuelCost)
                        }
                        .padding()
                    }
                } else {
                    ProgressView()
                }
            } else {
                ProgressView()
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = DashboardViewModel(modelContext: modelContext)
            }
            viewModel?.load()
        }
    }

    @ViewBuilder
    private func vehicleSelector(vm: DashboardViewModel) -> some View {
        Menu {
            ForEach(vm.vehicles, id: \.persistentModelID) { vehicle in
                Button {
                    vm.selectVehicle(vehicle)
                } label: {
                    HStack {
                        Text(vehicle.displayName)
                        if vehicle.persistentModelID == vm.activeVehicle?.persistentModelID {
                            Image(systemName: "checkmark")
                        }
                    }
                }
            }
        } label: {
            HStack {
                Text(vm.activeVehicle?.displayName ?? "")
                    .font(.headline)
                Image(systemName: "chevron.down")
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(.thinMaterial)
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    @ViewBuilder
    private func odometerCard(odometer: Int) -> some View {
        VStack(spacing: 4) {
            Text(FormatUtil.formatMiles(odometer))
                .font(.title)
                .fontWeight(.bold)
            Text("Current Odometer")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func lastFillUpCard(entry: FuelEntry?, mpg: Double?) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Last Fill-up")
                .font(.headline)
            if let entry {
                HStack {
                    Text(FormatUtil.formatDate(entry.date))
                    Spacer()
                    Text(FormatUtil.formatGallons(entry.gallons))
                    Text("•")
                    Text(FormatUtil.formatCurrency(entry.totalCost))
                }
                .font(.subheadline)
                if let mpg {
                    Text(FormatUtil.formatMpg(mpg))
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            } else {
                Text("No fill-ups yet")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func maintenanceCard(statuses: [MaintenanceStatus]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Upcoming Maintenance")
                .font(.headline)
            let upcoming = statuses.filter { $0.urgency != .ok }
            if upcoming.isEmpty {
                HStack {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundStyle(.green)
                    Text("All clear!")
                }
                .font(.subheadline)
            } else {
                ForEach(upcoming, id: \.type.persistentModelID) { status in
                    HStack {
                        Image(systemName: status.urgency == .overdue ? "exclamationmark.triangle.fill" : "exclamationmark.circle.fill")
                            .foregroundStyle(status.urgency == .overdue ? .red : .orange)
                        VStack(alignment: .leading) {
                            Text(status.type.name)
                                .font(.subheadline)
                                .fontWeight(.medium)
                            Text(status.urgency == .overdue
                                 ? "Overdue by \(FormatUtil.formatMiles(abs(status.milesUntilDue)))"
                                 : "\(FormatUtil.formatMiles(status.milesUntilDue)) remaining")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                        }
                        Spacer()
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func totalSpendCard(total: Double) -> some View {
        VStack(spacing: 4) {
            Text(FormatUtil.formatCurrency(total))
                .font(.title2)
                .fontWeight(.bold)
            Text("Total Fuel Spend")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}
