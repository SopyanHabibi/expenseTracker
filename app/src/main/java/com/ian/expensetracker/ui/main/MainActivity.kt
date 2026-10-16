package com.ian.expensetracker.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ian.expensetracker.R
import com.ian.expensetracker.data.Expense
import com.ian.expensetracker.ui.ExpenseAdapter
import com.ian.expensetracker.utils.SharedPrefsHelper
import com.ian.expensetracker.utils.formatRupiah
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var expenseList: MutableList<Expense>
    private lateinit var expenseAdapter: ExpenseAdapter

    private fun updateTotal(tvTotal: TextView) {
        val total = expenseList.sumOf { it.amount }
        tvTotal.text = formatRupiah(total)
    }

    private fun setupPieChart(pieChart: PieChart) {
        // 1. Kelompokkan total pengeluaran berdasarkan kategori
        val categoryTotals = expenseList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val entries = ArrayList<PieEntry>()
        for ((category, total) in categoryTotals) {
            if (total > 0) {
                entries.add(PieEntry(total.toFloat(), category))
            }
        }

        // Jika data kosong, bersihkan chart
        if (entries.isEmpty()) {
            pieChart.clear()
            return
        }

        // 2. Set data ke PieDataSet
        val dataSet = PieDataSet(entries, "Kategori")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.valueTextSize = 12f

        val pieData = PieData(dataSet)

        // 3. Konfigurasi tampilan chart agar cocok dengan tema gelap
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.legend.textColor = android.graphics.Color.WHITE
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(android.graphics.Color.TRANSPARENT)
        pieChart.setEntryLabelColor(android.graphics.Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.invalidate() // Refresh chart
    }

    private fun checkEmptyState(tvEmpty: TextView, rvExpense: RecyclerView) {
        if (expenseList.isEmpty()) {
            tvEmpty.visibility = android.view.View.VISIBLE
            rvExpense.visibility = android.view.View.GONE
        } else {
            tvEmpty.visibility = android.view.View.GONE
            rvExpense.visibility = android.view.View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBudgetProgress(tvTotal: TextView, progressBar: ProgressBar, tvStatus: TextView) {
        val currentTotal = expenseList.sumOf { it.amount }
        val maxBudget = SharedPrefsHelper.loadBudget(this)

        val percentage = if (maxBudget > 0) {
            ((currentTotal.toFloat() / maxBudget) * 100).toInt().coerceIn(0, 100)
        } else 0

        progressBar.progress = percentage
        tvStatus.text = "Pengeluaran dari ${formatRupiah(maxBudget)}"

        if (currentTotal > maxBudget) {
            tvStatus.text = "⚠️ Anggaran jebol! (Maks: ${formatRupiah(maxBudget)})"
            tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
        } else {
            tvStatus.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val rvExpense = findViewById<RecyclerView>(R.id.rvExpense)
        val pieChart = findViewById<PieChart>(R.id.pieChart)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)

        val progressBarBudget = findViewById<ProgressBar>(R.id.progressBarBudget)
        val tvBudgetStatus = findViewById<TextView>(R.id.tvBudgetStatus)
        val tvEditBudget = findViewById<TextView>(R.id.tvEditBudget)

        expenseList = SharedPrefsHelper.loadExpenses(this)
        updateTotal(tvTotal)
        setupPieChart(pieChart)
        updateBudgetProgress(tvTotal, progressBarBudget, tvBudgetStatus)

        // Setup Adapter
        expenseAdapter = ExpenseAdapter(expenseList) { position ->
            val deletedTitle = expenseList[position].title

            expenseList.removeAt(position)
            SharedPrefsHelper.saveExpenses(this, expenseList)

            expenseAdapter.notifyItemRemoved(position)
            expenseAdapter.notifyItemRangeChanged(position, expenseList.size)

            updateTotal(tvTotal)
            checkEmptyState(tvEmpty, rvExpense)
            updateBudgetProgress(tvTotal, progressBarBudget, tvBudgetStatus)
            setupPieChart(pieChart)

            Toast.makeText(this, "Berhasil hapus: $deletedTitle", Toast.LENGTH_SHORT).show()
        }

        rvExpense.layoutManager = LinearLayoutManager(this)
        rvExpense.adapter = expenseAdapter

        // Setup Swipe-to-Delete
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedExpense = expenseList[position]
                val deletedPosition = position

                expenseList.removeAt(position)
                SharedPrefsHelper.saveExpenses(this@MainActivity, expenseList)

                expenseAdapter.notifyItemRemoved(position)
                updateTotal(tvTotal)
                checkEmptyState(tvEmpty, rvExpense)
                updateBudgetProgress(tvTotal, progressBarBudget, tvBudgetStatus)
                setupPieChart(pieChart)

                Snackbar.make(
                    rvExpense,
                    "Berhasil hapus: ${deletedExpense.title}",
                    Snackbar.LENGTH_LONG
                ).setAction("BATAL") {
                    expenseList.add(deletedPosition, deletedExpense)
                    SharedPrefsHelper.saveExpenses(this@MainActivity, expenseList)

                    expenseAdapter.notifyItemInserted(deletedPosition)
                    rvExpense.scrollToPosition(deletedPosition)
                    updateTotal(tvTotal)
                    checkEmptyState(tvEmpty, rvExpense)
                    updateBudgetProgress(tvTotal, progressBarBudget, tvBudgetStatus)
                    setupPieChart(pieChart)
                }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(rvExpense)

        // ==========================================
        // EVENT KLIK TOMBOL FAB (BOTTOM SHEET MODAL)
        // ==========================================
        fabAdd.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.dialog_add_expense, null)
            bottomSheetDialog.setContentView(view)

            val etTitleModal = view.findViewById<EditText>(R.id.etTitleModal)
            val etAmountModal = view.findViewById<EditText>(R.id.etAmountModal)
            val spinnerCategoryModal = view.findViewById<Spinner>(R.id.spinnerCategoryModal)
            val btnAddModal = view.findViewById<Button>(R.id.btnAddModal)

            // Setup Adapter Spinner Kategori di dalam Modal
            val categories = arrayOf("Makanan", "Transportasi", "Belanja", "Tagihan", "Hiburan", "Lainnya")
            val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
            spinnerCategoryModal.adapter = categoryAdapter

            btnAddModal.setOnClickListener {
                val titleStr = etTitleModal.text.toString().trim()
                val amountStr = etAmountModal.text.toString().trim()
                val selectedCategory = spinnerCategoryModal.selectedItem.toString()

                if (titleStr.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(this, "Mohon isi semua data!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val amountInt = amountStr.toInt()

                if (amountInt <= 0) {
                    Toast.makeText(this, "Jumlah pengeluaran harus lebih dari 0!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newExpense = Expense(titleStr, amountInt, selectedCategory)
                expenseList.add(newExpense)
                SharedPrefsHelper.saveExpenses(this, expenseList)

                expenseAdapter.notifyItemInserted(expenseList.size - 1)

                updateTotal(tvTotal)
                checkEmptyState(tvEmpty, rvExpense)
                updateBudgetProgress(tvTotal, progressBarBudget, tvBudgetStatus)
                setupPieChart(pieChart)

                Toast.makeText(this, "Berhasil tambah: ${newExpense.title}", Toast.LENGTH_SHORT).show()

                // Tutup Bottom Sheet setelah berhasil simpan
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        // Dialog Atur Budget
        tvEditBudget.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Atur Target Anggaran Bulanan")

            val input = EditText(this)
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            input.hint = "Masukkan jumlah (contoh: 1500000)"
            builder.setView(input)

            builder.setPositiveButton("Simpan") { _, _ ->
                val strBudget = input.text.toString().trim()
                if (strBudget.isNotEmpty()) {
                    val newBudget = strBudget.toInt()
                    SharedPrefsHelper.saveBudget(this, newBudget)
                    updateBudgetProgress(tvTotal, progressBarBudget, tvBudgetStatus)
                    Toast.makeText(this, "Anggaran diperbarui!", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        checkEmptyState(tvEmpty, rvExpense)
    }
}