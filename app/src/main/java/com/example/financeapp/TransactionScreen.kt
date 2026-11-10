package com.example.financeapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeapp.data.MINOR_UNITS_PER_RUBLE

@Composable
@Suppress("FunctionNaming")
internal fun TransactionScreen(viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)) {
    val state = viewModel.uiState
    val balanceMinor by viewModel.balanceMinor.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            BalanceHeader(
                balance = balanceMinor / MINOR_UNITS_PER_RUBLE,
                transactionCount = transactions.size,
            )
            TransactionForm(
                state = state,
                onEvent = viewModel::onEvent,
                onAdd = viewModel::addTransaction,
            )
            TransactionHistory(
                transactions = transactions,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun BalanceHeader(balance: Long, transactionCount: Int) {
    Text(
        text = stringResource(R.string.balance_summary, balance, transactionCount),
        style = MaterialTheme.typography.headlineSmall,
    )
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionForm(state: MainUiState, onEvent: (MainUiEvent) -> Unit, onAdd: () -> Unit) {
    OutlinedTextField(
        value = state.amount,
        onValueChange = { onEvent(MainUiEvent.AmountChanged(it)) },
        label = { Text(stringResource(R.string.transaction_amount)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.description,
        onValueChange = { onEvent(MainUiEvent.DescriptionChanged(it)) },
        label = { Text(stringResource(R.string.transaction_description)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        FilterChip(
            selected = state.type == TransactionType.INCOME,
            onClick = { onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME)) },
            label = { Text(stringResource(R.string.transaction_type_income)) },
        )
        FilterChip(
            selected = state.type == TransactionType.EXPENSE,
            onClick = { onEvent(MainUiEvent.TypeChanged(TransactionType.EXPENSE)) },
            label = { Text(stringResource(R.string.transaction_type_expense)) },
        )
    }
    ErrorText(error = state.error)
    Button(onClick = onAdd) {
        Text(text = stringResource(R.string.transaction_add_button))
    }
}

@Composable
@Suppress("FunctionNaming")
private fun ErrorText(error: TransactionError?) {
    val messageRes = when (error) {
        TransactionError.INVALID_INPUT -> R.string.transaction_invalid_input
        TransactionError.AMOUNT_TOO_LARGE -> R.string.transaction_amount_too_large
        null -> null
    }
    messageRes?.let {
        Text(
            text = stringResource(it),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionHistory(transactions: List<Transaction>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item {
            Text(
                text = stringResource(R.string.transaction_history_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(transactions) { transaction ->
            Text(
                text = stringResource(
                    R.string.transaction_history_item,
                    transaction.description,
                    stringResource(typeLabelRes(transaction.type)),
                    transaction.amount,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun typeLabelRes(type: TransactionType): Int = when (type) {
    TransactionType.INCOME -> R.string.transaction_type_income
    TransactionType.EXPENSE -> R.string.transaction_type_expense
}
