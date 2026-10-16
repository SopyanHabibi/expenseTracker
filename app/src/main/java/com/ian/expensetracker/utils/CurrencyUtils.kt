package com.ian.expensetracker.utils

import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Int): String {
    val localeID = Locale("in", "ID")
    val formatter = NumberFormat.getCurrencyInstance(localeID)
    // Mengubah angka jadi format mata uang IDR (otomatis ada 'Rp' dan titiknya)
    return formatter.format(amount.toLong()).replace(",00", "")
    // .replace(",00" ...) dipakai biar desimal belakang koma (.00) hilang biar lebih bersih
}