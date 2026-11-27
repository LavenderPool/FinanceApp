package com.example.financeapp.coroutines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.R
import com.example.financeapp.TransactionType

@Composable
@Suppress("FunctionNaming")
internal fun AsyncTransactionsScreen(viewModel: AsyncTransactionsViewModel, onOpenLegacy: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.async_title), style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = onOpenLegacy) { Text(stringResource(R.string.async_open_legacy)) }

        val loadLabel = if (state == AsyncTransactionsState.Error) R.string.async_retry else R.string.async_load
        val canLoad = state != AsyncTransactionsState.Loading
        Button(onClick = { viewModel.load() }, enabled = canLoad) { Text(stringResource(loadLabel)) }
        OutlinedButton(onClick = { viewModel.load(shouldFail = true) }, enabled = canLoad) {
            Text(stringResource(R.string.async_simulate_error))
        }

        when (val current = state) {
            AsyncTransactionsState.Idle -> Unit

            AsyncTransactionsState.Loading -> Text(stringResource(R.string.transaction_loading))

            is AsyncTransactionsState.Content -> current.transactions.forEach { transaction ->
                val label = when (transaction.type) {
                    TransactionType.INCOME -> R.string.transaction_history_income
                    TransactionType.EXPENSE -> R.string.transaction_history_expense
                }
                Text(stringResource(label, transaction.description, transaction.amount))
            }

            AsyncTransactionsState.Error -> {
                Text(stringResource(R.string.async_error), color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
