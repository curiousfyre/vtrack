package com.vtrack.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.TimeZone

class FormatUtilTest {

    @Test
    fun `formatCurrency formats with dollar sign and two decimals`() {
        assertThat(FormatUtil.formatCurrency(1234.56)).isEqualTo("$1,234.56")
    }

    @Test
    fun `formatCurrency formats zero`() {
        assertThat(FormatUtil.formatCurrency(0.0)).isEqualTo("$0.00")
    }

    @Test
    fun `formatCurrency formats small amount`() {
        assertThat(FormatUtil.formatCurrency(3.50)).isEqualTo("$3.50")
    }

    @Test
    fun `formatDate formats epoch millis to readable date`() {
        val savedTz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            // Jan 15, 2025 00:00:00 UTC
            assertThat(FormatUtil.formatDate(1736899200000L)).isEqualTo("Jan 15, 2025")
        } finally {
            TimeZone.setDefault(savedTz)
        }
    }

    @Test
    fun `formatMpg formats with one decimal and MPG suffix`() {
        assertThat(FormatUtil.formatMpg(28.5)).isEqualTo("28.5 MPG")
    }

    @Test
    fun `formatMpg formats whole number`() {
        assertThat(FormatUtil.formatMpg(30.0)).isEqualTo("30 MPG")
    }

    @Test
    fun `formatMpg truncates to one decimal`() {
        assertThat(FormatUtil.formatMpg(28.567)).isEqualTo("28.6 MPG")
    }

    @Test
    fun `formatMiles formats with comma separator and mi suffix`() {
        assertThat(FormatUtil.formatMiles(12345)).isEqualTo("12,345 mi")
    }

    @Test
    fun `formatMiles formats small number without comma`() {
        assertThat(FormatUtil.formatMiles(500)).isEqualTo("500 mi")
    }

    @Test
    fun `formatGallons formats with one decimal and gal suffix`() {
        assertThat(FormatUtil.formatGallons(12.345)).isEqualTo("12.3 gal")
    }

    @Test
    fun `formatGallons formats whole number`() {
        assertThat(FormatUtil.formatGallons(10.0)).isEqualTo("10 gal")
    }
}
