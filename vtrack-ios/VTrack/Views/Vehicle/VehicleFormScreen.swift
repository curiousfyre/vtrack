import SwiftUI
import SwiftData

struct VehicleFormScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: VehicleFormViewModel?

    let vehicleId: String?

    var body: some View {
        Group {
            if viewModel != nil {
                formContent
            } else {
                ProgressView()
            }
        }
        .onAppear {
            if viewModel == nil {
                var vehicle: Vehicle?
                if let vehicleId {
                    let descriptor = FetchDescriptor<Vehicle>()
                    let vehicles = (try? modelContext.fetch(descriptor)) ?? []
                    vehicle = vehicles.first(where: { $0.persistentModelID.hashValue.description == vehicleId })
                }
                viewModel = VehicleFormViewModel(modelContext: modelContext, vehicle: vehicle)
            }
        }
    }

    @ViewBuilder
    private var formContent: some View {
        @Bindable var vm = viewModel!
        Form {
            Section {
                TextField("e.g., My Daily Driver", text: $vm.name)
                    .textInputAutocapitalization(.words)
                TextField("e.g., Toyota", text: $vm.make)
                    .textInputAutocapitalization(.words)
                TextField("e.g., Camry", text: $vm.model)
                    .textInputAutocapitalization(.words)
                TextField("Year", text: $vm.yearString)
                    .keyboardType(.numberPad)
            }
            Section {
                TextField("Initial Odometer", text: $vm.initialOdometerString)
                    .keyboardType(.numberPad)
            } header: {
                Text("Optional")
            } footer: {
                Text("Leave blank if unknown")
            }
        }
        .navigationTitle(vm.isEditing ? "Edit Vehicle" : "Add Vehicle")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button {
                    if vm.save() { dismiss() }
                } label: {
                    Image(systemName: "checkmark")
                }
                .disabled(!vm.isValid)
            }
        }
    }
}
