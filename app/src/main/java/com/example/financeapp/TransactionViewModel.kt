package com.example.financeapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

internal class TransactionViewModel : ViewModel() {
    var uiState by mutableStateOf(MainUiState())
        private set

    fun onEvent(event: MainUiEvent) {
        uiState = reduce(uiState, event)
    }

    fun addTransaction() {
        onEvent(validate(uiState))
    }

    private fun validate(state: MainUiState): MainUiEvent {
        val description = state.description.trim()
        val amount = state.amount.toIntOrNull()
        return when {
            state.amount.isEmpty() || description.isEmpty() -> MainUiEvent.InvalidTransaction
            amount == null -> MainUiEvent.AmountTooLarge
            amount <= 0 -> MainUiEvent.InvalidTransaction
            else -> MainUiEvent.AddTransaction(Transaction(amount, state.type, description))
        }
    }
}
