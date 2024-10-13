package com.expense.tracker.kotlin.ui.transaction

data class Transaction(
    val id: Long = 0,
    val type: String,
    val category: String,
    val amount: Float,
    val date: String
) {
    init {
        require(amount >= 0) { "Amount must be non-negative" }
    }
}