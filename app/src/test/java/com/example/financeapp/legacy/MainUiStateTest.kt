package com.example.financeapp.legacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MainUiStateTest {
    @Test
    fun income_increasesBalanceAndCount() {
        val state = reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(Transaction(500, TransactionType.INCOME)),
        )

        assertEquals(25_500, state.balance)
        assertEquals(1, state.transactionCount)
    }

    @Test
    fun expenseAfterIncome_decreasesBalanceAndIncrementsCount() {
        val afterIncome = reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(Transaction(500, TransactionType.INCOME)),
        )
        val state = reduce(
            afterIncome,
            MainUiEvent.AddTransaction(Transaction(250, TransactionType.EXPENSE)),
        )

        assertEquals(25_250, state.balance)
        assertEquals(2, state.transactionCount)
    }

    @Test
    fun zeroAmount_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            reduce(
                MainUiState(),
                MainUiEvent.AddTransaction(Transaction(0, TransactionType.INCOME)),
            )
        }
    }

    @Test
    fun negativeAmount_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            reduce(
                MainUiState(),
                MainUiEvent.AddTransaction(Transaction(-10, TransactionType.EXPENSE)),
            )
        }
    }

    @Test
    fun reduce_doesNotMutateOldState() {
        val initial = MainUiState()

        reduce(initial, MainUiEvent.AddTransaction(Transaction(250, TransactionType.EXPENSE)))

        assertEquals(25_000, initial.balance)
        assertEquals(0, initial.transactionCount)
    }
}
