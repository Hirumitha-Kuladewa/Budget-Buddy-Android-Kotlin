package com.expense.tracker.kotlin.ui.summary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.expense.tracker.kotlin.R
import com.expense.tracker.kotlin.ui.transaction.Transaction
import com.expense.tracker.kotlin.database.TransactionDBHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Suppress("DEPRECATION")
class SummaryFragment : Fragment() {

    private lateinit var dbHelper: TransactionDBHelper
    private lateinit var editTextDay: EditText
    private lateinit var editTextMonth: EditText
    private lateinit var editTextYear: EditText
    private lateinit var buttonSearch: TextView
    private lateinit var buttonSaveToPdf: TextView
    private lateinit var barChart: BarChart

    private val createFileRequestCode = 1
    private var transactions: List<Transaction> = ArrayList()
    private var totalIncome: Float = 0f
    private var totalExpenses: Float = 0f
    private var dateSummary: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_summary, container, false)

        dbHelper = TransactionDBHelper(requireContext())

        editTextDay = root.findViewById(R.id.edit_text_day)
        editTextMonth = root.findViewById(R.id.edit_text_month)
        editTextYear = root.findViewById(R.id.edit_text_year)
        buttonSearch = root.findViewById(R.id.search_btn)
        barChart = root.findViewById(R.id.bar_graph)
        buttonSaveToPdf = root.findViewById(R.id.btn_save_pdf)

        buttonSearch.setOnClickListener { performSearch() }
        buttonSaveToPdf.setOnClickListener { saveToPDF() }

        transactions = dbHelper.getAllTransactions()

        return root
    }

    private fun filterTransactions(day: String, month: String, year: String, transactions: List<Transaction>?): List<Transaction> {
        val filteredTransactions = mutableListOf<Transaction>()

        transactions?.let {
            for (transaction in it) {
                val dateParts = transaction.date.split("/")
                val transDay = dateParts.getOrNull(0) ?: ""
                val transMonth = dateParts.getOrNull(1) ?: ""
                val transYear = dateParts.getOrNull(2) ?: ""

                if ((day.isEmpty() || day == transDay) &&
                    (month.isEmpty() || month == transMonth) &&
                    (year.isEmpty() || year == transYear)) {
                    filteredTransactions.add(transaction)
                }
            }
        }
        return filteredTransactions
    }

    @SuppressLint("SetTextI18n")
    private fun performSearch() {
        val day = editTextDay.text.toString().trim()
        val month = editTextMonth.text.toString().trim()
        val year = editTextYear.text.toString().trim()

        if (day.isEmpty() && month.isEmpty() && year.isEmpty()) {
            Toast.makeText(context, "Please enter at least one date field.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Searching for $day/$month/$year", Toast.LENGTH_SHORT).show()

        val filteredTransactions = filterTransactions(day, month, year, transactions)

        dateSummary = buildDateSummary(day, month, year)

        totalIncome = 0f
        totalExpenses = 0f

        for (transaction in filteredTransactions) {
            when (transaction.type) {
                "Income" -> totalIncome += transaction.amount
                "Expense" -> totalExpenses += transaction.amount
            }
        }

        if (filteredTransactions.isEmpty()) {
            Toast.makeText(context, "No transactions found for the given date.", Toast.LENGTH_SHORT).show()
        } else {
            populateBarChart(totalIncome, totalExpenses)
            displaySummary(totalIncome, totalExpenses)
            clearInputFields()
        }
    }

    private fun clearInputFields() {
        editTextDay.setText("")
        editTextMonth.setText("")
        editTextYear.setText("")
    }

    private fun buildDateSummary(day: String, month: String, year: String): String {
        val dateBuilder = StringBuilder()

        if (day.isNotEmpty()) dateBuilder.append(day).append("/")
        if (month.isNotEmpty()) dateBuilder.append(month).append("/")
        if (year.isNotEmpty()) dateBuilder.append(year)

        return if (dateBuilder.isNotEmpty()) dateBuilder.toString() else "All Dates"
    }

    private fun populateBarChart(totalIncome: Float, totalExpenses: Float) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        val colors = ArrayList<Int>()

        entries.add(BarEntry(0f, totalIncome))
        labels.add("Total Income")
        colors.add(Color.parseColor("#00845A"))

        entries.add(BarEntry(1f, totalExpenses))
        labels.add("Total Expenses")
        colors.add(Color.parseColor("#C5141A"))

        val netIncome = totalIncome - totalExpenses
        entries.add(BarEntry(2f, netIncome))
        labels.add("Net Income")
        colors.add(if (netIncome >= 0) Color.parseColor("#25A18E") else Color.parseColor("#C5141A"))

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = colors

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.animateY(500)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        dataSet.valueTextSize = 10f

        barData.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        barChart.data = barData

        val legend = barChart.legend
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        val leftAxis = barChart.axisLeft
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        val rightAxis = barChart.axisRight
        rightAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        barChart.invalidate()
    }

    @SuppressLint("DefaultLocale")
    private fun displaySummary(totalIncome: Float, totalExpenses: Float) {
        val summaryTextView: TextView = requireView().findViewById(R.id.summary_text)

        val netIncome = totalIncome - totalExpenses
        val summaryText = SpannableString(
            "Total Income: $%.2f\nTotal Expenses: $%.2f\nNet Income: $%.2f"
                .format(totalIncome, totalExpenses, netIncome)
        )

        summaryText.setSpan(
            ForegroundColorSpan(Color.parseColor("#00845A")),
            0, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        summaryText.setSpan(
            ForegroundColorSpan(Color.parseColor("#C5141A")),
            27, 41, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        summaryText.setSpan(
            ForegroundColorSpan(if (netIncome >= 0) Color.parseColor("#25A18E") else Color.parseColor("#C5141A")),
            55, summaryText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        summaryText.setSpan(StyleSpan(Typeface.BOLD), 55, summaryText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        summaryTextView.text = summaryText
    }

    private fun saveToPDF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "transaction_summary.pdf")
        }
        startActivityForResult(intent, createFileRequestCode)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createFileRequestCode && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                Toast.makeText(context, "Saving PDF...", Toast.LENGTH_SHORT).show()
                PDFUtils(context).createPDF(
                    uri = uri,
                    dateSummary = dateSummary,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netIncome = totalIncome - totalExpenses,
                    transactions = transactions,
                    barChart = barChart
                )
            }
        }
    }
}
