package com.example.financeapp.coroutines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.Transaction
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

internal sealed interface AsyncTransactionsState {
    data object Idle : AsyncTransactionsState
    data object Loading : AsyncTransactionsState
    data class Content(val transactions: List<Transaction>) : AsyncTransactionsState
    data object Error : AsyncTransactionsState
}

internal class AsyncTransactionsViewModel : ViewModel() {
    private val repository = FakeTransactionsRepository()
    private val mutableState = MutableStateFlow<AsyncTransactionsState>(AsyncTransactionsState.Idle)
    val state: StateFlow<AsyncTransactionsState> = mutableState.asStateFlow()

    private var loadJob: Job? = null

    fun load(shouldFail: Boolean = false) {
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            mutableState.value = AsyncTransactionsState.Loading
            mutableState.value = try {
                AsyncTransactionsState.Content(repository.load(shouldFail))
            } catch (expected: IOException) {
                AsyncTransactionsState.Error
            }
        }
    }

    fun cancelLoad() {
        if (loadJob?.isActive != true) return

        loadJob?.cancel()
        mutableState.value = AsyncTransactionsState.Idle
    }
}
