package com.vtrack.util

object MpgCalculator {
    /**
     * Calculate MPG for a full fill-up, accounting for partial fills in between.
     * Returns null if current fill is partial.
     */
    fun calculateMpg(
        currentOdometer: Int,
        previousFullFillOdometer: Int,
        totalGallonsSinceLastFullFill: Double,
        isCurrentPartial: Boolean
    ): Double? {
        if (isCurrentPartial) return null
        if (totalGallonsSinceLastFullFill <= 0.0) return null
        val miles = currentOdometer - previousFullFillOdometer
        if (miles <= 0) return null
        return miles.toDouble() / totalGallonsSinceLastFullFill
    }

    fun calculateAverageMpg(mpgValues: List<Double>): Double? {
        if (mpgValues.isEmpty()) return null
        return mpgValues.average()
    }

    fun calculateCostPerMile(totalCost: Double, totalMiles: Int): Double? {
        if (totalMiles <= 0) return null
        return totalCost / totalMiles
    }
}
