package ir.kharjyar.core.export

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import ir.kharjyar.core.money.Money
import ir.kharjyar.core.money.MoneyFormatter
import ir.kharjyar.domain.model.CurrencyUnit
import ir.kharjyar.domain.model.FinancialTransaction
import ir.kharjyar.domain.model.TransactionType
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfAccountantReportGenerator @Inject constructor() {

    fun generateFinancialReportPdf(
        workspaceTitle: String,
        periodLabel: String,
        transactions: List<FinancialTransaction>,
        currency: CurrencyUnit,
        outputFile: File
    ): Boolean {
        val pdfDocument = PdfDocument()
        try {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(20, 40, 80)
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 11f
                textAlign = Paint.Align.RIGHT
            }

            val rightMargin = 555f
            var currentY = 55f

            canvas.drawText("گزارش مالی و صورت سود و زیان — $workspaceTitle", rightMargin, currentY, titlePaint)
            currentY += 24f
            canvas.drawText("بازه زمانی گزارش: $periodLabel", rightMargin, currentY, bodyPaint)
            currentY += 28f

            val activeTx = transactions.filter { !it.isDeleted }
            val totalIncome = activeTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinorUnits }
            val totalExpense = activeTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinorUnits + it.taxMinorUnits }
            val netProfit = totalIncome - totalExpense

            canvas.drawText(
                "مجموع درآمدها: ${MoneyFormatter.format(Money(totalIncome), currency)}",
                rightMargin,
                currentY,
                bodyPaint
            )
            currentY += 18f
            canvas.drawText(
                "مجموع هزینه‌ها: ${MoneyFormatter.format(Money(totalExpense), currency)}",
                rightMargin,
                currentY,
                bodyPaint
            )
            currentY += 18f
            canvas.drawText(
                "سود / (زیان) خالص دوره: ${MoneyFormatter.format(Money(netProfit), currency)}",
                rightMargin,
                currentY,
                titlePaint
            )
            currentY += 30f

            activeTx.take(25).forEachIndexed { idx, tx ->
                val amountFormatted = MoneyFormatter.format(Money(tx.amountMinorUnits), currency)
                val rowText = "${idx + 1}. [${tx.type.name}] $amountFormatted — ${tx.note.ifBlank { "بدون توضیح" }}"
                canvas.drawText(rowText, rightMargin, currentY, bodyPaint)
                currentY += 18f
            }

            pdfDocument.finishPage(page)
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            return true
        } catch (e: Exception) {
            return false
        } finally {
            pdfDocument.close()
        }
    }
}
