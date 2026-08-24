import SwiftUI
import SwiftData

struct MaintenanceHistoryScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: MaintenanceHistoryViewModel?

    let typeId: String
    var onLogMaintenance: (String, String) -> Void

    var body: some View {
        Group {
            if let vm = viewModel {
                if vm.records.isEmpty && vm.status == nil {
                    EmptyStateView(
                        icon: "wrench.fill",
                        title: "No service records yet",
                        message: "Tap + to log your first service"
                    )
                } else {
                    List {
                        if let status = vm.status {
                            Section("Current Status") {
                                VStack(alignment: .leading, spacing: 8) {
                                    HStack {
                                        Text("Status")
                                            .font(.subheadline)
                                        Spacer()
                                        urgencyBadge(status.urgency)
                                    }
                                    Text(status.urgency == .overdue
                                         ? "Overdue by \(FormatUtil.formatMiles(abs(status.milesUntilDue)))"
                                         : "\(FormatUtil.formatMiles(status.milesUntilDue)) until due")
                                    .font(.subheadline)
                                    ProgressView(value: min(max(status.percentUsed, 0), 1))
                                        .tint(colorForUrgency(status.urgency))
                                    HStack {
                                        Text("Every \(FormatUtil.formatMiles(status.type.intervalMiles))")
                                        Spacer()
                                        Text("Current: \(FormatUtil.formatMiles(status.currentOdometer))")
                                    }
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                }
                            }
                        }
                        if !vm.records.isEmpty {
                            Section("Service History") {
                                ForEach(vm.records, id: \.persistentModelID) { record in
                                    VStack(alignment: .leading, spacing: 4) {
                                        HStack {
                                            Text(FormatUtil.formatDate(record.date))
                                                .font(.subheadline)
                                                .fontWeight(.medium)
                                            Spacer()
                                            Text(FormatUtil.formatMiles(record.odometer))
                                                .font(.subheadline)
                                                .foregroundStyle(.secondary)
                                        }
                                        if let cost = record.cost {
                                            Text(FormatUtil.formatCurrency(cost))
                                                .font(.caption)
                                                .foregroundStyle(.secondary)
                                        }
                                        if let notes = record.notes, !notes.isEmpty {
                                            Text(notes)
                                                .font(.caption)
                                                .foregroundStyle(.secondary)
                                        }
                                    }
                                    .padding(.vertical, 2)
                                }
                            }
                        }
                    }
                }
            } else {
                ProgressView()
            }
        }
        .navigationTitle(viewModel?.typeName ?? "History")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    if let vm = viewModel, let vehicleId = vm.vehicleId, let typeId = vm.typeId {
                        onLogMaintenance(vehicleId, typeId)
                    }
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = MaintenanceHistoryViewModel(modelContext: modelContext, typeId: typeId)
            }
            viewModel?.load()
        }
    }

    @ViewBuilder
    private func urgencyBadge(_ urgency: MaintenanceUrgency) -> some View {
        let (text, bg, fg): (String, Color, Color) = {
            switch urgency {
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

    private func colorForUrgency(_ urgency: MaintenanceUrgency) -> Color {
        switch urgency {
        case .overdue: return .red
        case .dueSoon: return .orange
        case .ok: return .green
        }
    }
}
