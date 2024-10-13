package com.expense.tracker.kotlin.ui.summary

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import com.expense.tracker.kotlin.ui.transaction.Transaction
import com.github.mikephil.charting.charts.BarChart
import java.io.FileOutputStream
import java.io.IOException

@Suppress("DEPRECATION")
class PDFUtils(private val context: Context?) {

    @SuppressLint("DefaultLocale")
    fun createPDF(
        uri: Uri,
        dateSummary: String,
        totalIncome: Float,
        totalExpenses: Float,
        netIncome: Float,
        transactions: List<Transaction>,
        barChart: BarChart
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(700, 1000, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            paint.textSize = 20f
            canvas.drawText("Summary of $dateSummary", 50f, 50f, paint)

            paint.textSize = 16f
            canvas.drawText(String.format("Total Income: %.2f", totalIncome), 50f, 100f, paint)
            canvas.drawText(String.format("Total Expenses: %.2f", totalExpenses), 50f, 130f, paint)
            canvas.drawText(String.format("Net Income: %.2f", netIncome), 50f, 160f, paint)

            paint.textSize = 14f
            canvas.drawText("Type", 50f, 200f, paint)
            canvas.drawText("Category", 150f, 200f, paint)
            canvas.drawText("Amount", 250f, 200f, paint)
            canvas.drawText("Date", 350f, 200f, paint)

            var yPosition = 230f
            transactions.forEach { transaction ->
                canvas.drawText(transaction.type, 50f, yPosition, paint)
                canvas.drawText(transaction.category, 150f, yPosition, paint)
                canvas.drawText(String.format("%.2f", transaction.amount), 250f, yPosition, paint)
                canvas.drawText(transaction.date, 350f, yPosition, paint)
                yPosition += 30f
            }

            barChart.isDrawingCacheEnabled = true
            val barChartBitmap: Bitmap = Bitmap.createBitmap(barChart.drawingCache)
            barChart.isDrawingCacheEnabled = false

            val scaledBarChartBitmap = Bitmap.createScaledBitmap(barChartBitmap, 400, 400, true)

            canvas.drawBitmap(scaledBarChartBitmap, 50f, yPosition, paint)

            pdfDocument.finishPage(page)

            val pfd: ParcelFileDescriptor? = context?.contentResolver?.openFileDescriptor(uri, "w")
            val outputStream = FileOutputStream(pfd?.fileDescriptor)
            pdfDocument.writeTo(outputStream)

            pdfDocument.close()
            outputStream.close()

            Toast.makeText(context, "PDF saved to ${uri.path}", Toast.LENGTH_SHORT).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}