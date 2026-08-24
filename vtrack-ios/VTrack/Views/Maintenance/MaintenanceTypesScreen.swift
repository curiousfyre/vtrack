import SwiftUI
import SwiftData

struct MaintenanceTypesScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: MaintenanceTypesViewModel?

    var onAddType: (String) -> Void
    var onEditType: (String, String) -> Void
    var onViewHistory: (String) -> Void

    var body: some View {
        Group {
            if let vm = viewModel {
                if vm.activeVehicle == nil {
                    EmptyStateView(icon: "car.fill", title: "No Vehicle", message: "Add a vehicle first to track maintenance")
                } else if vm.statuses.isEmpty {
                    EmptyStateView(icon: "wrench.and.screwdriver.fill", title: "No maintenance types", message: "Tap + to add your first maintenance schedule")
                } else {
                    List {
                        ForEach(vm.statuses, id: \.type.persistentModelID) { status in
                            MaintenanceTypeRow(status: status) {
                                let vehicleId = vm.activeVehicle!.persistentModelID.hashValue.description
                                let typeId = status.type.persistentModelID.hashValue.description
                                onEditType(vehicleId, typeId)
                            }
                            .contentShape(Rectangle())
                            .onTapGesture {
                                onViewHistory(status.type.persistentModelID.hashValue.description)
                            }
                        }
                        .onDelete { indexSet in
                            for index in indexSet {
                                vm.deleteType(vm.statuses[index].type)
                            }
                        }
                    }
                }
            } else {
                ProgressView()
            }
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    if let vehicleId = viewModel?.activeVehicle?.persistentModelID.hashValue.description {
                        onAddType(vehicleId)
                    }
                } label: {
                    Image(systemName: "plus")
                }
                .disabled(viewModel?.activeVehicle == nil)
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = MaintenanceTypesViewModel(modelContext: modelContext)
            }
            viewModel?.load()
        }
    }
}

struct MaintenanceTypeRow: View {
    let status: MaintenanceStatus
    let onEdit: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(status.type.name)
                    .font(.headline)
                urgencyBadge
                Spacer()
                Button(action: onEdit) {
                    Image(systemName: "pencil")
                        .foregroundStyle(.secondary)
                }
                .buttonStyle(.plain)
            }
            Text(statusText)
                .font(.subheadline)
                .foregroundStyle(.secondary)
            ProgressView(value: min(max(status.percentUsed, 0), 1))
                .tint(urgencyColor)
            if let date = status.lastServiceDate {
                Text("Last serviced: \(FormatUtil.formatDate(date))")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }

    private var statusText: String {
        switch status.urgency {
        case .overdue:
            return "Overdue by \(FormatUtil.formatMiles(abs(status.milesUntilDue)))"
        case .dueSoon:
            return "\(FormatUtil.formatMiles(status.milesUntilDue)) remaining"
        case .ok:
            return "\(FormatUtil.formatMiles(status.milesUntilDue)) remaining"
        }
    }

    @ViewBuilder
    private var urgencyBadge: some View {
        let (text, bg, fg): (String, Color, Color) = {
            switch status.urgency {
            case .overdue: return ("Overdue", .red, .white)
            case .dueSoon: return ("Due Soon", .orange, .black)
            case .ok: return ("OK", .green, .white)
            }
        }()
        Text(text)
            .font(.caption2)
            .fontWeight(.semibold)
            .padding(.horizontal, 6)
            .padding(.vertical, 2)
            .background(bg)
            .foregroundStyle(fg)
            .clipShape(Capsule())
    }

    private var urgencyColor: Color {
        switch status.urgency {
        case .overdue: return .red
        case .dueSoon: return .orange
        case .ok: return .green
        }
    }
}
