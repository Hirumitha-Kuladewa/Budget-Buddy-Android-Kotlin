package com.expense.tracker.kotlin.ui.transaction

import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.expense.tracker.kotlin.MainActivity
import com.expense.tracker.kotlin.R
import com.expense.tracker.kotlin.database.TransactionDBHelper

class TransactionFragment : Fragment() {

    private lateinit var dbHelper: TransactionDBHelper
    private lateinit var transactionType: String
    private lateinit var category: AutoCompleteTextView
    private lateinit var amount: EditText
    private lateinit var date: TextView
    private lateinit var btnAddTransaction: CardView
    private var selectedDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction, container, false)

        dbHelper = TransactionDBHelper(requireContext())

        val rgTransactionType = view.findViewById<RadioGroup>(R.id.transaction_type)
        category = view.findViewById(R.id.edit_category)
        amount = view.findViewById(R.id.edit_amount)
        date = view.findViewById(R.id.btn_select_date)
        btnAddTransaction = view.findViewById(R.id.btn_add_transaction)

        rgTransactionType.setOnCheckedChangeListener { _, checkedId ->
            transactionType = if (checkedId == R.id.income) "Income" else "Expense"
        }

        date.setOnClickListener {
            showDatePicker()
        }

        btnAddTransaction.setOnClickListener {
            addTransaction()
        }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                date.text = selectedDate
            }, year, month, day)
        datePickerDialog.show()
    }

    private fun addTransaction() {
        val categoryText = category.text.toString().trim()
        val amountValue = amount.text.toString().trim().toFloatOrNull()

        if (categoryText.isEmpty() || amountValue == null || selectedDate.isEmpty() || !::transactionType.isInitialized) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            val transaction = Transaction(
                type = transactionType,
                category = categoryText,
                amount = amountValue,
                date = selectedDate
            )
            dbHelper.insertTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction Added", Toast.LENGTH_SHORT).show()

            (activity as MainActivity).updateHomeFragment()

            clearFields()
        }
    }

    private fun clearFields() {
        category.text.clear()
        amount.text.clear()
        date.text = getString(R.string.select_date_reset_text)
        selectedDate = ""
        transactionType = ""
    }
}