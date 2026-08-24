import SwiftUI
import SwiftData

struct FuelEntryScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: FuelEntryViewModel?

    let vehicleId: String
    let entryId: String?

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
                viewModel = FuelEntryViewModel(modelContext: modelContext, vehicleId: vehicleId, entryId: entryId)
            }
        }
    }

    @ViewBuilder
    private var formContent: some View {
        @Bindable var vm = viewModel!
        Form {
            Section {
                DatePicker("Date", selection: $vm.date, displayedComponents: .date)
                HStack {
                    TextField("Odometer", text: $vm.odometerString)
                        .keyboardType(.numberPad)
                    Text("mi")
                        .foregroundStyle(.secondary)
                }
            }
            Section {
                HStack {
                    TextField("Gallons", text: $vm.gallonsString)
                        .keyboardType(.decimalPad)
                        .onChange(of: vm.gallonsString) { vm.autoCalculateTotalCost() }
                    Text("gal")
                        .foregroundStyle(.secondary)
                }
                HStack {
                    Text("$")
                        .foregroundStyle(.secondary)
                    TextField("Price per Gallon", text: $vm.pricePerGallonString)
                        .keyboardType(.decimalPad)
                        .onChange(of: vm.pricePerGallonString) { vm.autoCalculateTotalCost() }
                }
                HStack {
                    Text("$")
                        .foregroundStyle(.secondary)
                    TextField("Total Cost", text: $vm.totalCostString)
                        .keyboardType(.decimalPad)
                }
            }
            Section {
                Toggle("Partial Fill", isOn: $vm.isPartialFill)
                if vm.isPartialFill {
                    Text("Tank was not filled completely")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            Section("Notes") {
                TextField("Optional notes", text: $vm.notes, axis: .vertical)
                    .lineLimit(3...5)
            }
        }
        .navigationTitle(vm.isEditing ? "Edit Fill-up" : "Add Fill-up")
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
