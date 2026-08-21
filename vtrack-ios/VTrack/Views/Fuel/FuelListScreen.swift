import SwiftUI
import SwiftData

struct FuelListScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: FuelListViewModel?

    var onAddEntry: (String) -> Void
    var onEditEntry: (String, String) -> Void

    var body: some View {
        Group {
            if let vm = viewModel {
                if vm.activeVehicle == nil {
                    EmptyStateView(icon: "car.fill", title: "No Vehicle", message: "Add a vehicle first to track fuel")
                } else if vm.entries.isEmpty {
                    EmptyStateView(icon: "fuelpump.fill", title: "No fill-ups yet", message: "Tap + to add your first fill-up")
                } else {
                    List {
                        if let avg = vm.averageMpg {
                            Section {
                                HStack {
                                    Text("Average Fuel Economy")
                                        .font(.subheadline)
                                    Spacer()
                                    Text(FormatUtil.formatMpg(avg))
                                        .font(.headline)
                                        .fontWeight(.bold)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                        Section {
                            ForEach(vm.entries) { item in
                                FuelEntryRow(item: item)
                                    .contentShape(Rectangle())
                                    .onTapGesture {
                                        let vehicleId = vm.activeVehicle!.persistentModelID.hashValue.description
                                        let entryId = item.entry.persistentModelID.hashValue.description
                                        onEditEntry(vehicleId, entryId)
                                    }
                            }
                            .onDelete { indexSet in
                                for index in indexSet {
                                    vm.deleteEntry(vm.entries[index].entry)
                                }
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
                        onAddEntry(vehicleId)
                    }
                } label: {
                    Image(systemName: "plus")
                }
                .disabled(viewModel?.activeVehicle == nil)
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = FuelListViewModel(modelContext: modelContext)
            }
            viewModel?.load()
        }
    }
}

struct FuelEntryRow: View {
    let item: FuelEntryWithMpg

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(FormatUtil.formatDate(item.entry.date))
                    .font(.subheadline)
                    .fontWeight(.medium)
                Spacer()
                if item.entry.isPartialFill {
                    Text("Partial")
                        .font(.caption2)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(.orange.opacity(0.2))
                        .foregroundStyle(.orange)
                        .clipShape(Capsule())
                } else if let mpg = item.mpg {
                    Text(FormatUtil.formatMpg(mpg))
                        .font(.caption)
                        .fontWeight(.semibold)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(.blue.opacity(0.2))
                        .foregroundStyle(.blue)
                        .clipShape(Capsule())
                }
            }
            HStack {
                Text(FormatUtil.formatMiles(item.entry.odometer))
                Text("•")
                Text(FormatUtil.formatGallons(item.entry.gallons))
                Text("•")
                Text(FormatUtil.formatCurrency(item.entry.totalCost))
            }
            .font(.caption)
            .foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }
}
