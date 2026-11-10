package com.example.financeapp.data

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var transactions: TransactionDao
    private lateinit var categories: CategoryDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder<AppDatabase>(context)
            .setDriver(AndroidSQLiteDriver())
            .build()
        transactions = database.transactionDao()
        categories = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_returnsId_andRowCanBeReadBack() = runBlocking {
        val id = transactions.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))

        val found = transactions.findById(id)

        assertEquals("Кофе", found?.description)
    }

    @Test
    fun emptyDatabase_hasZeroBalance() = runBlocking {
        assertEquals(0L, transactions.observeBalance().first())
    }

    @Test
    fun income_increasesBalance_andExpenseDecreasesIt() = runBlocking {
        transactions.insert(transaction("Зарплата", amountMinor = 50_000L, type = "INCOME"))
        transactions.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))

        assertEquals(25_000L, transactions.observeBalance().first())
    }

    @Test
    fun delete_isReflectedInObservedList() = runBlocking {
        val id = transactions.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))
        assertEquals(1, transactions.observeAll().first().size)

        transactions.delete(transactions.findById(id)!!)

        assertTrue(transactions.observeAll().first().isEmpty())
    }

    @Test
    fun transactionWithoutCategory_staysInRowsWithNullCategoryName() = runBlocking {
        transactions.insert(transaction("Кофе", amountMinor = 25_000L, type = "EXPENSE"))

        val rows = transactions.observeRows().first()

        assertEquals(1, rows.size)
        assertNull(rows.single().categoryName)
    }

    @Test
    fun deletingCategory_setsTransactionCategoryToNull() = runBlocking {
        val categoryId = categories.insert(CategoryEntity(name = "Еда", colorArgb = 0xFFAA00L))
        val transactionId = transactions.insert(
            transaction("Обед", amountMinor = 85_000L, type = "EXPENSE", categoryId = categoryId),
        )

        categories.delete(CategoryEntity(id = categoryId, name = "Еда", colorArgb = 0xFFAA00L))

        assertNull(transactions.findById(transactionId)?.categoryId)
    }

    private fun transaction(
        description: String,
        amountMinor: Long,
        type: String,
        categoryId: Long? = null,
    ) = TransactionEntity(
        description = description,
        amountMinor = amountMinor,
        type = type,
        occurredAt = OCCURRED_AT,
        categoryId = categoryId,
    )

    private companion object {
        const val OCCURRED_AT = 1_000L
    }
}
