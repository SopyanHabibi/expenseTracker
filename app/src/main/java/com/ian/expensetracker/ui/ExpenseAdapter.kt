package com.ian.expensetracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ian.expensetracker.R
import com.ian.expensetracker.data.Expense
import com.ian.expensetracker.utils.formatRupiah

// Tambahkan parameter onItemDeleted: (Int) -> Unit di constructor adapter
class ExpenseAdapter(
    private val expenseList: List<Expense>,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val btnDelete: android.view.View = itemView.findViewById(R.id.btnDelete) // <-- Binding tombol hapus
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.tvTitle.text = expense.title
        holder.tvCategory.text = expense.category
        holder.tvAmount.text = formatRupiah(expense.amount)

        // Berikan event klik pada tombol hapus
        holder.btnDelete.setOnClickListener {
            // Mengirim posisi (index) item yang diklik kembali ke MainActivity
            onItemDeleted(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = expenseList.size
}