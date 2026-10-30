package ir.kharjyar.domain

import com.google.common.truth.Truth.assertThat
import ir.kharjyar.domain.model.BudgetStatus
import ir.kharjyar.domain.model.BusinessInvoice
import ir.kharjyar.domain.model.InvoiceLineItem
import ir.kharjyar.domain.usecase.SmartInsightUseCase
import org.junit.Test

class FinancialUseCasesTest {

    private val smartInsightUseCase = SmartInsightUseCase()

    @Test
    fun `business invoice calculates exact VAT and grand total in minor units without float`() {
        val invoice = BusinessInvoice(
            id = "inv_1",
            workspaceId = "ws_biz",
            invoiceNumber = "1405-001",
            customerOrVendorName = "شرکت توسعه فناوری پارس",
            isSalesInvoice = true,
            issueDateUtcMillis = 1760000000000L,
            dueDateUtcMillis = 1761000000000L,
            items = listOf(
                InvoiceLineItem(title = "طراحی سامانه مالی", quantity = 2, unitPriceMinorUnits = 150_000_000L),
                InvoiceLineItem(title = "پشتیبانی سالانه", quantity = 1, unitPriceMinorUnits = 50_000_000L)
            ),
            discountMinorUnits = 10_000_000L,
            vatRateBasisPoints = 1000
        )

        assertThat(invoice.subtotalMinorUnits).isEqualTo(350_000_000L)
        assertThat(invoice.taxableAmountMinorUnits).isEqualTo(340_000_000L)
        assertThat(invoice.vatAmountMinorUnits).isEqualTo(34_000_000L)
        assertThat(invoice.grandTotalMinorUnits).isEqualTo(374_000_000L)
    }

    @Test
    fun `budget status detects 80 percent warning and 100 percent critical thresholds accurately`() {
        val warningBudget = BudgetStatus(
            id = "b1",
            workspaceId = "ws_1",
            categoryId = "cat_food",
            categoryName = "خوراک",
            year = 1405,
            month = 7,
            limitMinorUnits = 100_000_000L,
            spentMinorUnits = 85_000_000L,
            previousMonthSpentMinorUnits = 70_000_000L
        )

        assertThat(warningBudget.isWarning80Percent).isTrue()
        assertThat(warningBudget.isCritical100Percent).isFalse()

        val insights = smartInsightUseCase.generateInsights(listOf(warningBudget))
        assertThat(insights).isNotEmpty()
        assertThat(insights.first()).contains("بیشتر از ماه قبل")
    }
}
