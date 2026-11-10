package com.example.financeapp.data

import com.example.financeapp.Transaction
import com.example.financeapp.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionRepositoryTest {
    @Test
    fun add_convertsRublesToMinorUnitsAndUsesProvidedTime() = runTest {
        val dao = FakeTransactionDao()
        val repository = TransactionRepository(dao, now = { 1_700_000_000_000L })

        repository.add(Transaction(250, TransactionType.EXPENSE, "Кофе"))

        val saved = dao.inserted.single()
        assertEquals(25_000L, saved.amountMinor)
        assertEquals("EXPENSE", saved.type)
        assertEquals("Кофе", saved.description)
        assertEquals(1_700_000_000_000L, saved.occurredAt)
    }

    @Test
    fun transactions_mapMinorUnitsBackToRubles() = runTest {
        val dao = FakeTransactionDao()
        val repository = TransactionRepository(dao, now = { 1L })
        repository.add(Transaction(500, TransactionType.INCOME, "Зарплата"))

        val history = repository.transactions.first()

        assertEquals(listOf(Transaction(500, TransactionType.INCOME, "Зарплата")), history)
    }

    @Test
    fun balance_isIncomeMinusExpenseInMinorUnits() = runTest {
        val dao = FakeTransactionDao()
        val repository = TransactionRepository(dao, now = { 1L })
        repository.add(Transaction(500, TransactionType.INCOME, "Зарплата"))
        repository.add(Transaction(250, TransactionType.EXPENSE, "Кофе"))

        assertEquals(25_000L, repository.balanceMinor.first())
    }

    @Test
    fun remove_delegatesToDao() = runTest {
        val dao = FakeTransactionDao()
        val repository = TransactionRepository(dao, now = { 1L })
        repository.add(Transaction(250, TransactionType.EXPENSE, "Кофе"))
        val entity = dao.inserted.single()

        repository.remove(entity)

        assertEquals(listOf(entity), dao.deleted)
    }
}
