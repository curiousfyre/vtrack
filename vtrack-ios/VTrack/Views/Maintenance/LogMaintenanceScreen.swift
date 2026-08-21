import SwiftUI
import SwiftData

struct LogMaintenanceScreen: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @State private var viewModel: LogMaintenanceViewModel?

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
                viewModel = LogMaintenanceViewModel(modelContext: modelContext, vehicleId: vehicleId, typeId: typeId)
            }
        }
    }

    @ViewBuilder
    private var formContent: some View {
        @Bindable var vm = viewModel!
        Form {
            Section {
                Text(vm.typeName)
                    .font(.title3)
                    .fontWeight(.semibold)
            }
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
                    Text("$")
                        .foregroundStyle(.secondary)
                    TextField("Cost (optional)", text: $vm.costString)
                        .keyboardType(.decimalPad)
                }
            }
            Section("Notes") {
                TextField("Optional notes", text: $vm.notes, axis: .vertical)
                    .lineLimit(3...5)
            }
        }
        .navigationTitle("Log Service")
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
