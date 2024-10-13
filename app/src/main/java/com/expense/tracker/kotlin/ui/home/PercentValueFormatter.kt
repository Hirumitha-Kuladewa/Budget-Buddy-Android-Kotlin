package com.expense.tracker.kotlin.ui.home

import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class PercentValueFormatter : ValueFormatter() {
    override fun getPieLabel(value: Float, data: PieEntry?): String {
        return "${value.toInt()}%"
    }
}