import SwiftUI
import SwiftData

struct SettingsScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: SettingsViewModel?
    @State private var showShareSheet = false

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
                viewModel = SettingsViewModel(modelContext: modelContext)
            }
        }
    }

    @ViewBuilder
    private var formContent: some View {
        @Bindable var vm = viewModel!
        Form {
            Section {
                Toggle("Maintenance Notifications", isOn: $vm.notificationsEnabled)
                Text("Get notified when maintenance is due")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Export Data")
                        .font(.headline)
                    Text("Export your fuel data as a CSV file")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Button("Export Fuel Data (CSV)") {
                        vm.exportData()
                        if vm.exportCSVContent != nil {
                            showShareSheet = true
                        }
                    }
                    .disabled(vm.activeVehicle == nil)
                }
            }
            Section {
                HStack {
                    Spacer()
                    Text("Autometer v1.0.0")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Spacer()
                }
            }
        }
        .sheet(isPresented: $showShareSheet) {
            if let csv = vm.exportCSVContent {
                ShareSheet(items: [csv])
            }
        }
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
