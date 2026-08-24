import SwiftUI
import SwiftData

struct StatsScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: StatsViewModel?

    var body: some View {
        Group {
            if let vm = viewModel {
                if vm.activeVehicle == nil || vm.fillUpCount == 0 {
                    EmptyStateView(icon: "chart.bar.fill", title: "No stats yet", message: "Add some fill-ups to see statistics")
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            summaryRow(vm: vm)
                            if vm.mpgData.count >= 2 {
                                mpgChartCard(data: vm.mpgData)
                            }
                            if !vm.monthlySpending.isEmpty {
                                monthlySpendingCard(data: vm.monthlySpending)
                            }
                            detailedStatsCard(vm: vm)
                        }
                        .padding()
                    }
                }
            } else {
                ProgressView()
            }
        }
        .onAppear {
            if viewModel == nil {
                viewModel = StatsViewModel(modelContext: modelContext)
            }
            viewModel?.load()
        }
    }

    @ViewBuilder
    private func summaryRow(vm: StatsViewModel) -> some View {
        HStack(spacing: 12) {
            statCard(title: "Avg MPG", value: vm.averageMpg.map { String(format: "%.1f", $0) } ?? "—")
            statCard(title: "Total Spent", value: FormatUtil.formatCurrency(vm.totalSpent))
            statCard(title: "Cost/Mile", value: vm.costPerMile.map { String(format: "$%.2f", $0) } ?? "—")
        }
    }

    @ViewBuilder
    private func statCard(title: String, value: String) -> some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.headline)
                .fontWeight(.bold)
                .minimumScaleFactor(0.7)
                .lineLimit(1)
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func mpgChartCard(data: [MpgDataPoint]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("MPG Over Time")
                .font(.headline)
            MpgChartView(data: data)
                .frame(height: 200)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func monthlySpendingCard(data: [MonthlySpending]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Monthly Spending")
                .font(.headline)
            ForEach(data) { item in
                HStack {
                    Text(item.month)
                        .font(.subheadline)
                    Spacer()
                    Text(FormatUtil.formatCurrency(item.amount))
                        .font(.subheadline)
                        .fontWeight(.medium)
                }
                if item.id != data.last?.id {
                    Divider()
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func detailedStatsCard(vm: StatsViewModel) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Detailed Stats")
                .font(.headline)
            statsRow("Best MPG", vm.bestMpg.map { FormatUtil.formatMpg($0) } ?? "—")
            statsRow("Worst MPG", vm.worstMpg.map { FormatUtil.formatMpg($0) } ?? "—")
            Divider()
            statsRow("Total Gallons", FormatUtil.formatGallons(vm.totalGallons))
            statsRow("Total Miles", FormatUtil.formatMiles(vm.totalMiles))
            statsRow("Fill-up Count", "\(vm.fillUpCount)")
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(.regularMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    @ViewBuilder
    private func statsRow(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundStyle(.secondary)
            Spacer()
            Text(value)
                .font(.subheadline)
                .fontWeight(.medium)
        }
    }
}

struct MpgChartView: View {
    let data: [MpgDataPoint]

    var body: some View {
        GeometryReader { geometry in
            let width = geometry.size.width
            let height = geometry.size.height
            let mpgValues = data.map(\.mpg)
            let minMpg = (mpgValues.min() ?? 0) * 0.9
            let maxMpg = (mpgValues.max() ?? 1) * 1.1
            let range = maxMpg - minMpg

            Canvas { context, size in
                let stepX = width / CGFloat(max(data.count - 1, 1))

                for i in 0..<4 {
                    let y = height - (CGFloat(i) / 3.0) * height
                    var gridPath = Path()
                    gridPath.move(to: CGPoint(x: 0, y: y))
                    gridPath.addLine(to: CGPoint(x: width, y: y))
                    context.stroke(gridPath, with: .color(.secondary.opacity(0.2)), lineWidth: 0.5)
                }

                var linePath = Path()
                for (index, point) in data.enumerated() {
                    let x = CGFloat(index) * stepX
                    let y = height - ((point.mpg - minMpg) / range) * height
                    if index == 0 {
                        linePath.move(to: CGPoint(x: x, y: y))
                    } else {
                        linePath.addLine(to: CGPoint(x: x, y: y))
                    }
                }
                context.stroke(linePath, with: .color(.blue), lineWidth: 3)

                for (index, point) in data.enumerated() {
                    let x = CGFloat(index) * stepX
                    let y = height - ((point.mpg - minMpg) / range) * height
                    let dotRect = CGRect(x: x - 4, y: y - 4, width: 8, height: 8)
                    context.fill(Path(ellipseIn: dotRect), with: .color(.blue))
                }
            }

            HStack {
                Text(String(format: "%.0f", minMpg))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                Spacer()
                Text(String(format: "%.0f", maxMpg))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            .offset(y: height + 4)
        }
    }
}
