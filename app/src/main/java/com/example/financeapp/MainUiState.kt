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
    val balance: Int = 0,
    val transactionCount: Int = 0,
    val transactions: List<Transaction> = emptyList(),
    val error: TransactionError? = null,
)

internal sealed interface MainUiEvent {
    data class AmountChanged(val value: String) : MainUiEvent
    data class DescriptionChanged(val value: String) : MainUiEvent
    data class TypeChanged(val type: TransactionType) : MainUiEvent
    data class AddTransaction(val transaction: Transaction) : MainUiEvent
    data object InvalidTransaction : MainUiEvent
    data object AmountTooLarge : MainUiEvent
}

internal fun reduce(state: MainUiState, event: MainUiEvent): MainUiState = when (event) {
    is MainUiEvent.AmountChanged -> changeAmount(state, event.value)
    is MainUiEvent.DescriptionChanged -> state.copy(description = event.value, error = null)
    is MainUiEvent.TypeChanged -> state.copy(type = event.type, error = null)
    is MainUiEvent.AddTransaction -> addTransaction(state, event.transaction)
    MainUiEvent.InvalidTransaction -> state.copy(error = TransactionError.INVALID_INPUT)
    MainUiEvent.AmountTooLarge -> state.copy(error = TransactionError.AMOUNT_TOO_LARGE)
}

private fun changeAmount(state: MainUiState, value: String): MainUiState = if (value.all(Char::isDigit)) {
    state.copy(amount = value, error = null)
} else {
    state
}

private fun addTransaction(state: MainUiState, transaction: Transaction): MainUiState {
    require(transaction.amount > 0) { "Transaction amount must be positive" }
    val change = when (transaction.type) {
        TransactionType.INCOME -> transaction.amount.toLong()
        TransactionType.EXPENSE -> -transaction.amount.toLong()
    }
    val newBalance = state.balance.toLong() + change
    if (!newBalance.fitsInt()) {
        return state.copy(error = TransactionError.AMOUNT_TOO_LARGE)
    }
    return state.copy(
        balance = newBalance.toInt(),
        transactions = state.transactions + transaction,
        transactionCount = state.transactionCount + 1,
        amount = "",
        description = "",
        error = null,
    )
}

private fun Long.fitsInt(): Boolean = this in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
