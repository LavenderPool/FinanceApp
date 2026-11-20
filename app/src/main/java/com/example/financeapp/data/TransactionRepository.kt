package com.example.financeapp.data

import com.example.financeapp.Transaction
import com.example.financeapp.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal const val MINOR_UNITS_PER_RUBLE = 100L

internal class TransactionRepository(
    private val dao: TransactionDao,
    private val now: () -> Long = System::currentTimeMillis,
) {
    val transactions: Flow<List<Transaction>> =
        dao.observeAll().map { rows -> rows.map(TransactionEntity::toDomain) }

    val balanceMinor: Flow<Long> = dao.observeBalance()

    suspend fun add(transaction: Transaction) {
        dao.insert(transaction.toEntity(occurredAt = now()))
    }

    suspend fun remove(item: TransactionEntity) {
        dao.delete(item)
    }
}

private fun TransactionEntity.toDomain(): Transaction = Transaction(
    amount = (amountMinor / MINOR_UNITS_PER_RUBLE).toInt(),
    type = TransactionType.valueOf(type),
    description = description,
)

private fun Transaction.toEntity(occurredAt: Long): TransactionEntity = TransactionEntity(
    description = description,
    amountMinor = amount.toLong() * MINOR_UNITS_PER_RUBLE,
    type = type.name,
    occurredAt = occurredAt,
    categoryId = null,
)
