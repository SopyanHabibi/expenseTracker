package com.ian.expensetracker.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ian.expensetracker.data.Expense

object SharedPrefsHelper {
    private const val PREF_NAME = "expense_prefs"
    private const val KEY_EXPENSES = "key_expenses"

    private const val PREF_BUDGET = "pref_budget"

    // Fungsi untuk Menyimpan Data ke SharedPreferences
    fun saveExpenses(context: Context, list: List<Expense>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Ubah List<Expense> menjadi String JSON pakai Gson
        val jsonString = Gson().toJson(list)

        editor.putString(KEY_EXPENSES, jsonString)
        editor.apply()  // Simpan secara asynchronous
    }

    // Fungsi untuk Membaca Data dari SharedPreferences
    fun loadExpenses(context: Context): MutableList<Expense> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_EXPENSES, null) ?: return mutableListOf()

        // Ubah kembali String JSON menjadi List<Expense> pakai Gson
        val type = object : TypeToken<MutableList<Expense>>() {}.type
        return Gson().fromJson(jsonString, type)
    }

    // Simpan target anggaran
    fun saveBudget(context: Context, budget: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(PREF_BUDGET, budget).apply()
    }

    // Ambil target anggaran (default set misal Rp 1.000.000 jika belum diatur)
    fun loadBudget(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(PREF_BUDGET, 1000000)
    }

}