package com.example.financeapp.coroutines

import com.example.financeapp.Transaction
import com.example.financeapp.TransactionType
import kotlinx.coroutines.delay
import java.io.IOException

private const val LOAD_DELAY_MS = 1_500L

internal class FakeTransactionsRepository {
    suspend fun load(shouldFail: Boolean): List<Transaction> {
        delay(LOAD_DELAY_MS)
        if (shouldFail) throw IOException("Учебная ошибка загрузки")

        return listOf(Transaction(amount = 250, type = TransactionType.EXPENSE, description = "Кофе"))
    }
}
