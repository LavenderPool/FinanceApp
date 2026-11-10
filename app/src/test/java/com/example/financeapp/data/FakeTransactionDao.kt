package com.example.financeapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeTransactionDao : TransactionDao {
    val inserted = mutableListOf<TransactionEntity>()
    val deleted = mutableListOf<TransactionEntity>()
    private val rows = MutableStateFlow<List<TransactionEntity>>(emptyList())

    override suspend fun insert(item: TransactionEntity): Long {
        inserted += item
        val id = inserted.size.toLong()
        rows.value = rows.value + item.copy(id = id)
        return id
    }

    override suspend fun update(item: TransactionEntity) = Unit

    override suspend fun delete(item: TransactionEntity) {
        deleted += item
        rows.value = rows.value - item
    }

    override suspend fun findById(id: Long): TransactionEntity? = rows.value.firstOrNull { it.id == id }

    override fun observeAll(): Flow<List<TransactionEntity>> = rows.map { it.asReversed() }

    override fun observeByType(type: String): Flow<List<TransactionEntity>> =
        rows.map { list -> list.filter { it.type == type } }

    override fun observeByCategory(categoryId: Long): Flow<List<TransactionEntity>> =
        rows.map { list -> list.filter { it.categoryId == categoryId } }

    override fun observeBalance(): Flow<Long> = rows.map { list ->
        list.sumOf { if (it.type == "INCOME") it.amountMinor else -it.amountMinor }
    }

    override fun observeRows(): Flow<List<TransactionRow>> = rows.map { list ->
        list.map { TransactionRow(it.id, it.description, null) }
    }
}
