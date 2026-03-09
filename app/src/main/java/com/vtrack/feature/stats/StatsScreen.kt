package com.vtrack.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vtrack.util.FormatUtil

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.fillUpCount == 0 -> {
            EmptyStatsState()
        }

        else -> {
            StatsContent(uiState = uiState)
        }
    }
}

@Composable
private fun EmptyStatsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No stats yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Log some fill-ups to see your fuel statistics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatsContent(uiState: StatsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "summary_row") {
            SummaryRow(uiState = uiState)
        }

        if (uiState.mpgData.size >= 2) {
            item(key = "mpg_chart") {
                MpgChartCard(mpgData = uiState.mpgData)
            }
        }

        if (uiState.costPerMonthData.isNotEmpty()) {
            item(key = "monthly_header") {
                Text(
                    text = "Monthly Spending",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item(key = "monthly_spending") {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        uiState.costPerMonthData.forEachIndexed { index, (month, cost) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = month,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = FormatUtil.formatCurrency(cost),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (index < uiState.costPerMonthData.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }

        item(key = "details_header") {
            Text(
                text = "Detailed Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item(key = "detailed_stats") {
            DetailedStatsCard(uiState = uiState)
        }
    }
}

@Composable
private fun SummaryRow(uiState: StatsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryStatCard(
            label = "Avg MPG",
            value = uiState.averageMpg?.let { FormatUtil.formatMpg(it) } ?: "\u2014",
            modifier = Modifier.weight(1f)
        )
        SummaryStatCard(
            label = "Total Spent",
            value = FormatUtil.formatCurrency(uiState.totalSpent),
            modifier = Modifier.weight(1f)
        )
        SummaryStatCard(
            label = "Cost/Mile",
            value = uiState.costPerMile?.let { FormatUtil.formatCurrency(it) } ?: "\u2014",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MpgChartCard(mpgData: List<Pair<Long, Double>>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "MPG Over Time",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (mpgData.size < 2) return@Canvas

                val padding = 8.dp.toPx()
                val chartWidth = size.width - padding * 2
                val chartHeight = size.height - padding * 2

                val mpgValues = mpgData.map { it.second }
                val minMpg = (mpgValues.min() - 2.0).coerceAtLeast(0.0)
                val maxMpg = mpgValues.max() + 2.0
                val mpgRange = maxMpg - minMpg

                val minTime = mpgData.first().first.toFloat()
                val maxTime = mpgData.last().first.toFloat()
                val timeRange = maxTime - minTime

                // Draw horizontal grid lines
                val gridLineCount = 4
                for (i in 0..gridLineCount) {
                    val y = padding + chartHeight * (1f - i.toFloat() / gridLineCount)
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 1f
                    )
                }

                // Build path
                val path = Path()
                mpgData.forEachIndexed { index, (time, mpg) ->
                    val x = padding + if (timeRange > 0) {
                        chartWidth * ((time - minTime) / timeRange)
                    } else {
                        chartWidth / 2
                    }
                    val y = padding + chartHeight * (1f - ((mpg - minMpg) / mpgRange).toFloat())
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw dots
                mpgData.forEach { (time, mpg) ->
                    val x = padding + if (timeRange > 0) {
                        chartWidth * ((time - minTime) / timeRange)
                    } else {
                        chartWidth / 2
                    }
                    val y = padding + chartHeight * (1f - ((mpg - minMpg) / mpgRange).toFloat())
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // Min/Max labels
            val mpgValues = mpgData.map { it.second }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Low: ${FormatUtil.formatMpg(mpgValues.min())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "High: ${FormatUtil.formatMpg(mpgValues.max())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailedStatsCard(uiState: StatsUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            DetailStatRow(
                label = "Best MPG",
                value = uiState.bestMpg?.let { FormatUtil.formatMpg(it) } ?: "\u2014"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DetailStatRow(
                label = "Worst MPG",
                value = uiState.worstMpg?.let { FormatUtil.formatMpg(it) } ?: "\u2014"
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DetailStatRow(
                label = "Total Gallons",
                value = FormatUtil.formatGallons(uiState.totalGallons)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DetailStatRow(
                label = "Total Miles",
                value = FormatUtil.formatMiles(uiState.totalMiles)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DetailStatRow(
                label = "Fill-up Count",
                value = uiState.fillUpCount.toString()
            )
        }
    }
}

@Composable
private fun DetailStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
