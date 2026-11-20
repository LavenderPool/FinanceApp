package com.example.financeapp

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionViewModelTest {
    @Test
    fun amountWithLetters_isIgnored() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.AmountChanged("12a"))
        assertEquals("", viewModel.uiState.amount)

        viewModel.onEvent(MainUiEvent.AmountChanged("12"))
        assertEquals("12", viewModel.uiState.amount)
    }

    @Test
    fun emptyDescription_setsInvalidInputError() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.AmountChanged("100"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("   "))

        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.transactionCount)
    }

    @Test
    fun emptyAmount_setsInvalidInputError() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))

        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
    }

    @Test
    fun hugeAmount_setsAmountTooLargeError() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.AmountChanged("99999999999"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Техника"))

        viewModel.addTransaction()

        assertEquals(TransactionError.AMOUNT_TOO_LARGE, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.balance)
    }

    @Test
    fun expense_thenIncome_updatesBalanceHistoryAndCount() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))
        viewModel.addTransaction()

        assertEquals(-250, viewModel.uiState.balance)
        assertEquals(1, viewModel.uiState.transactions.size)
        assertEquals(1, viewModel.uiState.transactionCount)
        assertEquals("", viewModel.uiState.amount)
        assertEquals("", viewModel.uiState.description)

        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.onEvent(MainUiEvent.AmountChanged("500"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Зарплата"))
        viewModel.addTransaction()

        assertEquals(250, viewModel.uiState.balance)
        assertEquals(2, viewModel.uiState.transactionCount)
    }

    @Test
    fun overflowingBalance_keepsPreviousBalance() {
        val state = MainUiState(balance = Int.MAX_VALUE)

        val result = reduce(
            state,
            MainUiEvent.AddTransaction(Transaction(1, TransactionType.INCOME, "Лотерея")),
        )

        assertEquals(Int.MAX_VALUE, result.balance)
        assertEquals(0, result.transactionCount)
        assertEquals(TransactionError.AMOUNT_TOO_LARGE, result.error)
    }
}
