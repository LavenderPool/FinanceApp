package com.example.financeapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.DatabaseProvider
import com.example.financeapp.data.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MILLIS = 5_000L

internal class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    var uiState by mutableStateOf(MainUiState())
        private set

    val transactions: StateFlow<List<Transaction>> = repository.transactions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        emptyList(),
    )

    val balanceMinor: StateFlow<Long> = repository.balanceMinor.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        0L,
    )

    fun onEvent(event: MainUiEvent) {
        uiState = reduce(uiState, event)
    }

    fun addTransaction() {
        val draft = uiState
        val description = draft.description.trim()
        val amount = draft.amount.toIntOrNull()
        when {
            draft.amount.isEmpty() || description.isEmpty() -> onEvent(MainUiEvent.InvalidTransaction)
            amount == null -> onEvent(MainUiEvent.AmountTooLarge)
            amount <= 0 -> onEvent(MainUiEvent.InvalidTransaction)
            else -> save(Transaction(amount, draft.type, description))
        }
    }

    private fun save(transaction: Transaction) {
        viewModelScope.launch {
            repository.add(transaction)
            onEvent(MainUiEvent.TransactionSaved)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val dao = DatabaseProvider.get(application).transactionDao()
                TransactionViewModel(TransactionRepository(dao))
            }
        }
    }
}
