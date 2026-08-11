package com.vtrack.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MpgCalculatorTest {

    @Test
    fun `calculateMpg returns correct MPG for normal full fill`() {
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 10300,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = 10.0,
            isCurrentPartial = false
        )
        assertThat(mpg).isEqualTo(30.0)
    }

    @Test
    fun `calculateMpg returns null for partial fill`() {
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 10300,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = 10.0,
            isCurrentPartial = true
        )
        assertThat(mpg).isNull()
    }

    @Test
    fun `calculateMpg returns null for zero gallons`() {
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 10300,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = 0.0,
            isCurrentPartial = false
        )
        assertThat(mpg).isNull()
    }

    @Test
    fun `calculateMpg returns null for negative gallons`() {
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 10300,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = -5.0,
            isCurrentPartial = false
        )
        assertThat(mpg).isNull()
    }

    @Test
    fun `calculateMpg returns null when odometer did not advance`() {
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 10000,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = 10.0,
            isCurrentPartial = false
        )
        assertThat(mpg).isNull()
    }

    @Test
    fun `calculateMpg returns null when odometer went backwards`() {
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 9500,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = 10.0,
            isCurrentPartial = false
        )
        assertThat(mpg).isNull()
    }

    @Test
    fun `calculateMpg accumulates gallons across partial fills`() {
        // 400 miles, 8 gallons partial + 7 gallons full = 15 total
        val mpg = MpgCalculator.calculateMpg(
            currentOdometer = 10400,
            previousFullFillOdometer = 10000,
            totalGallonsSinceLastFullFill = 15.0,
            isCurrentPartial = false
        )
        assertThat(mpg).isWithin(0.01).of(26.67)
    }

    @Test
    fun `calculateAverageMpg returns correct average`() {
        val avg = MpgCalculator.calculateAverageMpg(listOf(25.0, 30.0, 35.0))
        assertThat(avg).isEqualTo(30.0)
    }

    @Test
    fun `calculateAverageMpg returns null for empty list`() {
        val avg = MpgCalculator.calculateAverageMpg(emptyList())
        assertThat(avg).isNull()
    }

    @Test
    fun `calculateAverageMpg handles single value`() {
        val avg = MpgCalculator.calculateAverageMpg(listOf(28.5))
        assertThat(avg).isEqualTo(28.5)
    }

    @Test
    fun `calculateCostPerMile returns correct value`() {
        val cpm = MpgCalculator.calculateCostPerMile(totalCost = 150.0, totalMiles = 1000)
        assertThat(cpm).isEqualTo(0.15)
    }

    @Test
    fun `calculateCostPerMile returns null for zero miles`() {
        val cpm = MpgCalculator.calculateCostPerMile(totalCost = 150.0, totalMiles = 0)
        assertThat(cpm).isNull()
    }

    @Test
    fun `calculateCostPerMile returns null for negative miles`() {
        val cpm = MpgCalculator.calculateCostPerMile(totalCost = 150.0, totalMiles = -100)
        assertThat(cpm).isNull()
    }
}
