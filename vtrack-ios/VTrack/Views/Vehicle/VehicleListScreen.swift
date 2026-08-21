import SwiftUI
import SwiftData

struct VehicleListScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: VehicleListViewModel?

    var onAddVehicle: () -> Void
    var onEditVehicle: (String) -> Void

    var body: some View {
        Group {
            if viewModel != nil {
                listContent
            } else {
                ProgressView()
            }
        }
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: onAddVehicle) {
                    Image(systemName: "plus")
                }
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = VehicleListViewModel(modelContext: modelContext)
            }
            viewModel?.fetchVehicles()
        }
    }

    @ViewBuilder
    private var listContent: some View {
        @Bindable var vm = viewModel!
        if vm.vehicles.isEmpty {
            EmptyStateView(
                icon: "car.fill",
                title: "No vehicles yet",
                message: "Add your first vehicle to get started",
                actionTitle: "Add Vehicle",
                action: onAddVehicle
            )
        } else {
            List {
                ForEach(vm.vehicles, id: \.persistentModelID) { vehicle in
                    VehicleRow(vehicle: vehicle, isActive: isActiveVehicle(vehicle))
                        .contentShape(Rectangle())
                        .onTapGesture {
                            onEditVehicle(vehicle.persistentModelID.hashValue.description)
                        }
                        .swipeActions(edge: .trailing) {
                            Button(role: .destructive) {
                                vm.confirmDelete(vehicle)
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                }
            }
            .confirmDelete(itemName: vm.vehicleToDelete?.name ?? "Vehicle", isPresented: $vm.showDeleteConfirmation) {
                vm.deleteVehicle()
            }
        }
    }

    private func isActiveVehicle(_ vehicle: Vehicle) -> Bool {
        let savedId = UserDefaults.standard.string(forKey: "activeVehicleId")
        return vehicle.persistentModelID.hashValue.description == savedId
    }
}

struct VehicleRow: View {
    let vehicle: Vehicle
    let isActive: Bool

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(vehicle.name)
                        .font(.headline)
                    if isActive {
                        Text("Active")
                            .font(.caption2)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(.blue.opacity(0.2))
                            .foregroundStyle(.blue)
                            .clipShape(Capsule())
                    }
                }
                Text(vehicle.displayName)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Button {
                UserDefaults.standard.set(vehicle.persistentModelID.hashValue.description, forKey: "activeVehicleId")
            } label: {
                Image(systemName: isActive ? "star.fill" : "star")
                    .foregroundStyle(isActive ? .yellow : .secondary)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
    }
}
