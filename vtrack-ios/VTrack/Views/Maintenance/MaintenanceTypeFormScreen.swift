import SwiftUI
import SwiftData

struct MaintenanceTypeFormScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: MaintenanceTypeFormViewModel?

    let vehicleId: String
    let typeId: String?

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
                viewModel = MaintenanceTypeFormViewModel(modelContext: modelContext, vehicleId: vehicleId, typeId: typeId)
            }
        }
    }

    @ViewBuilder
    private var formContent: some View {
        @Bindable var vm = viewModel!
        Form {
            if !vm.isEditing {
                Section("Quick Presets") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(maintenancePresets.indices, id: \.self) { index in
                                Button {
                                    vm.selectPreset(index)
                                } label: {
                                    Text(maintenancePresets[index].name)
                                        .font(.caption)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(vm.selectedPresetIndex == index ? Color.accentColor : Color.secondary.opacity(0.2))
                                        .foregroundStyle(vm.selectedPresetIndex == index ? .white : .primary)
                                        .clipShape(Capsule())
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                }
            }
            Section {
                TextField("Name", text: $vm.name)
                HStack {
                    TextField("Interval (miles)", text: $vm.intervalMilesString)
                        .keyboardType(.numberPad)
                    Text("mi")
                        .foregroundStyle(.secondary)
                }
                HStack {
                    TextField("Interval (months, optional)", text: $vm.intervalMonthsString)
                        .keyboardType(.numberPad)
                    Text("mo")
                        .foregroundStyle(.secondary)
                }
            }
            Section {
                HStack {
                    TextField("Next due at odometer", text: $vm.nextDueOdometerString)
                        .keyboardType(.numberPad)
                    Text("mi")
                        .foregroundStyle(.secondary)
                }
            } header: {
                Text("Next Due Override")
            } footer: {
                Text("Override: set the exact odometer for the next service")
            }
            Section("Description") {
                TextField("Optional description", text: $vm.descriptionText, axis: .vertical)
                    .lineLimit(3...5)
            }
        }
        .navigationTitle(vm.isEditing ? "Edit Maintenance" : "Add Maintenance")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .confirmationAction) {
                Button {
                    if vm.save() { dismiss() }
                } label: {
                    Image(systemName: "checkmark")
                }
            }
        }
        .alert("Error", isPresented: .init(get: { vm.error != nil }, set: { if !$0 { vm.error = nil } })) {
            Button("OK") {}
        } message: {
            Text(vm.error ?? "")
        }
    }
}
