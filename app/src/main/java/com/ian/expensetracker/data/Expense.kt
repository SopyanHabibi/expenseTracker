package com.ian.expensetracker.data

data class Expense(
    val title: String,
    val amount: Int,
    val category: String = "Lainnya"  // Default jika tidak dipilih
)