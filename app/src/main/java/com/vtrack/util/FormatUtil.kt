package com.vtrack.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtil {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 1
    }

    fun formatCurrency(amount: Double): String = currencyFormat.format(amount)
    fun formatDate(epochMillis: Long): String = dateFormat.format(Date(epochMillis))
    fun formatMpg(mpg: Double): String = "${numberFormat.format(mpg)} MPG"
    fun formatMiles(miles: Int): String = "${NumberFormat.getIntegerInstance(Locale.US).format(miles)} mi"
    fun formatGallons(gallons: Double): String = "${numberFormat.format(gallons)} gal"
}
