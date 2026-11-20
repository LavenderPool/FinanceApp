package com.example.financeapp

internal enum class TransactionType { INCOME, EXPENSE }

internal enum class TransactionError { INVALID_INPUT, AMOUNT_TOO_LARGE }

internal data class Transaction(
    val amount: Int,
    val type: TransactionType,
    val description: String,
)

internal data class MainUiState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val error: TransactionError? = null,
)

internal sealed interface MainUiEvent {
    data class AmountChanged(val value: String) : MainUiEvent
    data class DescriptionChanged(val value: String) : MainUiEvent
    data class TypeChanged(val type: TransactionType) : MainUiEvent
    data object InvalidTransaction : MainUiEvent
    data object AmountTooLarge : MainUiEvent
    data object TransactionSaved : MainUiEvent
}

internal fun reduce(state: MainUiState, event: MainUiEvent): MainUiState = when (event) {
    is MainUiEvent.AmountChanged -> changeAmount(state, event.value)
    is MainUiEvent.DescriptionChanged -> state.copy(description = event.value, error = null)
    is MainUiEvent.TypeChanged -> state.copy(type = event.type, error = null)
    MainUiEvent.InvalidTransaction -> state.copy(error = TransactionError.INVALID_INPUT)
    MainUiEvent.AmountTooLarge -> state.copy(error = TransactionError.AMOUNT_TOO_LARGE)
    MainUiEvent.TransactionSaved -> state.copy(amount = "", description = "", error = null)
}

private fun changeAmount(state: MainUiState, value: String): MainUiState = if (value.all(Char::isDigit)) {
    state.copy(amount = value, error = null)
} else {
    state
}
