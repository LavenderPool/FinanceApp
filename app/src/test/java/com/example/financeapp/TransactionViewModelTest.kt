package com.example.financeapp

import com.example.financeapp.data.FakeTransactionDao
import com.example.financeapp.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionViewModelTest {
    private lateinit var dao: FakeTransactionDao
    private lateinit var viewModel: TransactionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        dao = FakeTransactionDao()
        viewModel = TransactionViewModel(TransactionRepository(dao))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun amountWithLetters_isIgnored() {
        viewModel.onEvent(MainUiEvent.AmountChanged("12a"))
        assertEquals("", viewModel.uiState.amount)

        viewModel.onEvent(MainUiEvent.AmountChanged("12"))
        assertEquals("12", viewModel.uiState.amount)
    }

    @Test
    fun emptyDescription_setsInvalidInputError_andSavesNothing() {
        viewModel.onEvent(MainUiEvent.AmountChanged("100"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("   "))

        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
        assertTrue(dao.inserted.isEmpty())
    }

    @Test
    fun emptyAmount_setsInvalidInputError() {
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))

        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
    }

    @Test
    fun hugeAmount_setsAmountTooLargeError() {
        viewModel.onEvent(MainUiEvent.AmountChanged("99999999999"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Техника"))

        viewModel.addTransaction()

        assertEquals(TransactionError.AMOUNT_TOO_LARGE, viewModel.uiState.error)
        assertTrue(dao.inserted.isEmpty())
    }

    @Test
    fun expense_isSaved_andDraftIsCleared() {
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))

        viewModel.addTransaction()

        assertEquals(1, dao.inserted.size)
        assertEquals(25_000L, dao.inserted.single().amountMinor)
        assertEquals("EXPENSE", dao.inserted.single().type)
        assertEquals("", viewModel.uiState.amount)
        assertEquals("", viewModel.uiState.description)
        assertEquals(null, viewModel.uiState.error)
    }

    @Test
    fun income_isSaved_withSelectedType() {
        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.onEvent(MainUiEvent.AmountChanged("500"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Зарплата"))

        viewModel.addTransaction()

        assertEquals("INCOME", dao.inserted.single().type)
        assertEquals(50_000L, dao.inserted.single().amountMinor)
    }
}
