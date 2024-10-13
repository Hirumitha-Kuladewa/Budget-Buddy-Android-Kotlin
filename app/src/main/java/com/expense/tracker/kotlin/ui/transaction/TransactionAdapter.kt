package com.expense.tracker.kotlin.ui.transaction

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.expense.tracker.kotlin.R
import com.expense.tracker.kotlin.ui.home.HomeFragment

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val homeFragment: HomeFragment,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_HEADER = 0
    private val VIEW_TYPE_ITEM = 1
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(createHeaderView(parent))
        } else {
            TransactionViewHolder(createItemView(parent))
        }
    }

    override fun getItemCount(): Int {
        return transactions.size + 1
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            return
        }
        val transaction = transactions[position - 1]

        (holder as TransactionViewHolder).apply {
            type.text = transaction.type
            category.text = transaction.category
            amount.text = transaction.amount.toString()
            date.text = transaction.date

            itemView.setOnClickListener {
                homeFragment.showEditTransactionDialog(transaction)
            }

            itemView.setOnLongClickListener {
                onDelete(transaction)
                true
            }
        }
    }

    private fun createHeaderView(parent: ViewGroup): LinearLayout {
        val context = parent.context
        val headerLayout = LinearLayout(context)
        headerLayout.orientation = LinearLayout.HORIZONTAL
        headerLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        headerLayout.setPadding(16, 16, 16, 16)

        val headerTexts = listOf(context.getString(R.string.table_column_type),
            context.getString(R.string.table_column_category),
            context.getString(R.string.table_column_amount),
            context.getString(R.string.table_column_date))
        headerTexts.forEach { text ->
            val textView = TextView(context)
            textView.text = text
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            textView.setTypeface(null, Typeface.BOLD)
            textView.setPadding(8, 8, 8, 8)
            textView.gravity = Gravity.CENTER
            textView.layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            headerLayout.addView(textView)
        }

        return headerLayout
    }

    private fun createItemView(parent: ViewGroup): LinearLayout {
        val context = parent.context
        val itemLayout = LinearLayout(context)
        itemLayout.orientation = LinearLayout.HORIZONTAL
        itemLayout.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        itemLayout.setPadding(16, 16, 16, 16)

        val textViews = arrayOfNulls<TextView>(4)
        for (i in textViews.indices) {
            textViews[i] = TextView(context)
            textViews[i]?.setPadding(8, 8, 8, 8)
            textViews[i]?.gravity = Gravity.CENTER
            textViews[i]?.layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            itemLayout.addView(textViews[i])
        }

        return itemLayout
    }

    class HeaderViewHolder(itemView: LinearLayout) : RecyclerView.ViewHolder(itemView)

    class TransactionViewHolder(itemView: LinearLayout) : RecyclerView.ViewHolder(itemView) {
        val type: TextView = itemView.getChildAt(0) as TextView
        val category: TextView = itemView.getChildAt(1) as TextView
        val amount: TextView = itemView.getChildAt(2) as TextView
        val date: TextView = itemView.getChildAt(3) as TextView
    }
}