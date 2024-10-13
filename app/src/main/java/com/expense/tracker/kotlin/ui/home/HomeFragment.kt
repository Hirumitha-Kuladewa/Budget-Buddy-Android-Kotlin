package com.expense.tracker.kotlin.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.expense.tracker.kotlin.R
import com.expense.tracker.kotlin.database.TransactionDBHelper
import com.expense.tracker.kotlin.ui.transaction.Transaction
import com.expense.tracker.kotlin.ui.transaction.TransactionAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var greetingChanger: TextView
    private lateinit var pieChart: PieChart
    private lateinit var rvTransactions: RecyclerView
    private lateinit var dbHelper: TransactionDBHelper
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        greetingChanger = view.findViewById(R.id.greeting_changer)
        pieChart = view.findViewById(R.id.pie_chart)
        rvTransactions = view.findViewById(R.id.transaction_table_view)
        barChart = view.findViewById(R.id.bar_chart)

        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        dbHelper = TransactionDBHelper(requireContext())

        updateGreeting()
        loadTransactions()
        setupPieChart()
        setupBarChart()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshData()
        updateGreeting()
    }

    private fun updateGreeting() {
        val currentTime = Calendar.getInstance().time
        val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
        val currentHour = hourFormat.format(currentTime).toInt()

        when (currentHour) {
            in 0..11 -> greetingChanger.text = getString(R.string.good_morning_text)
            in 12..15 -> greetingChanger.text = getString(R.string.good_afternoon_text)
            in 16..18 -> greetingChanger.text = getString(R.string.good_evening_text)
            in 19..23 -> greetingChanger.text = getString(R.string.good_night_text)
        }
    }

    private fun loadTransactions() {
        val transactionList = dbHelper.getAllTransactions()
        transactionAdapter = TransactionAdapter(transactionList, this) { transaction ->
            showDeleteConfirmationDialog(transaction)
        }
        rvTransactions.adapter = transactionAdapter
    }

    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(ContextCompat.getColor(requireContext(), R.color.card_color))
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 80f
        pieChart.transparentCircleRadius = 61f
        pieChart.setDrawCenterText(true)
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true
        pieChart.animateY(1000)

        val totalIncome = dbHelper.getTotalIncome()
        val totalExpense = dbHelper.getTotalExpense()

        if (totalIncome == 0.0 && totalExpense == 0.0) {
            pieChart.centerText = getString(R.string.pie_chart_no_data_text)
            pieChart.invalidate()
            return
        }

        val entries = mutableListOf<PieEntry>()

        if (totalIncome > 0) {
            entries.add(PieEntry(totalIncome.toFloat()))
        }
        if (totalExpense > 0) {
            entries.add(PieEntry(totalExpense.toFloat()))
        }

        val dataSet = PieDataSet(entries, getString(R.string.pie_chart_label))
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.pie_chart_positive),
            ContextCompat.getColor(requireContext(), R.color.pie_chart_negative)
        )
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.setDrawValues(true)

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.pie_chart_precentage_color))
        data.setValueFormatter(PercentValueFormatter())

        pieChart.data = data

        val legend = pieChart.legend
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER

        pieChart.invalidate()
    }

    private fun setupBarChart() {
        val totalIncome = dbHelper.getTotalIncome()
        val totalExpense = dbHelper.getTotalExpense()
        val netIncome = totalIncome - totalExpense

        val entries = mutableListOf<BarEntry>()

        entries.add(BarEntry(0f, totalIncome.toFloat(), "Total Income"))
        entries.add(BarEntry(1f, totalExpense.toFloat(), "Total Expense"))

        val netIncomeColor = if (netIncome > 0) {
            ContextCompat.getColor(requireContext(), R.color.bar_chart_positive)
        } else {
            ContextCompat.getColor(requireContext(), R.color.bar_chart_negative)
        }
        entries.add(BarEntry(2f, netIncome.toFloat(), "Net Income"))

        val dataSet = BarDataSet(entries, "Financial Data")
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.pie_chart_positive),
            ContextCompat.getColor(requireContext(), R.color.pie_chart_negative),
            netIncomeColor
        )

        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        dataSet.valueTextSize = 10f

        val barData = BarData(dataSet)
        barData.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        barChart.data = barData

        barChart.invalidate()

        barChart.description.isEnabled = false

        barChart.legend.isEnabled = true
        val legend = barChart.legend
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        val xAxis = barChart.xAxis
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        val leftAxis = barChart.axisLeft
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        val rightAxis = barChart.axisRight
        rightAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)
    }

    @SuppressLint("SetTextI18n")
    fun showEditTransactionDialog(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_transaction, null)

        val radioIncome: RadioButton = dialogView.findViewById(R.id.income)
        val radioExpense: RadioButton = dialogView.findViewById(R.id.expense)
        val etCategory: EditText = dialogView.findViewById(R.id.edit_category)
        val etAmount: EditText = dialogView.findViewById(R.id.edit_amount)
        val btnSelectDate: TextView = dialogView.findViewById(R.id.btn_select_date)
        val btnSave: CardView = dialogView.findViewById(R.id.btn_add_transaction)

        if (transaction.type == "Income") {
            radioIncome.isChecked = true
        } else {
            radioExpense.isChecked = true
        }
        etCategory.setText(transaction.category)
        etAmount.setText(transaction.amount.toString())

        var selectedDate: String = transaction.date
        btnSelectDate.text = selectedDate

        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    btnSelectDate.text = selectedDate
                }, year, month, day)
            datePickerDialog.show()
        }

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)

        val alertDialog = builder.create()

        btnSave.setOnClickListener {
            val type = if (radioIncome.isChecked) "Income" else "Expense"
            val category = etCategory.text.toString()
            val amountText = etAmount.text.toString()

            if (category.isEmpty() || amountText.isEmpty() || selectedDate.isEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Input Error")
                    .setMessage("Please enter a valid category.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            val amount = amountText.toFloatOrNull()
            if (amount == null) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Input Error")
                    .setMessage("Please enter a valid amount.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            dbHelper.updateTransaction(transaction.id, type, category, amount, selectedDate)
            alertDialog.dismiss()
            refreshData()
        }

        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Transaction")
        builder.setMessage("Are you sure you want to delete this transaction?")
        builder.setPositiveButton("Yes") { _, _ ->
            dbHelper.deleteTransaction(transaction.id)
            refreshData()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun refreshData() {
        loadTransactions()
        setupPieChart()
        setupBarChart()
    }
}