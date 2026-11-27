package com.example.financeapp.practice

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import kotlin.math.abs
import kotlin.time.measureTime

fun main(args: Array<String>) {
    runBlocking {
        when (args.singleOrNull()) {
            "demo" -> delayDemo()
            "1" -> exercise1()
            "2" -> exercise2()
            "3" -> exercise3()
            "4" -> exercise4()
            "5" -> exercise5()
            "6" -> exercise6()
            "7" -> exercise7()
            "8" -> exercise8()
            else -> println("Передайте demo или номер консольного задания от 1 до 8.")
        }
    }
}

private suspend fun delayDemo(): Unit = coroutineScope {
    launch {
        println("Запрос начался")
        delay(600)
        println("Запрос завершился")
    }
    launch {
        delay(200)
        println("Другая корутина выполнилась во время ожидания")
    }
}

private suspend fun loadBalance(): Int = loadIncome() - loadExpenses()

private suspend fun exercise1() {
    println("Начало")
    println("Баланс: ${loadBalance()} ₽")
    println("Готово")
}

private suspend fun exercise2(): Unit = coroutineScope {
    val job = launch { println("Доход: ${loadIncome()} ₽") }
    println("Запрос запущен")
    job.join()
    println("Все задачи завершены")
}

private suspend fun exercise3() {
    val sequentialTime = measureTime {
        println("Баланс: ${loadBalance()} ₽")
    }
    println("Последовательно: $sequentialTime")
    // Concurrent delays overlap; the longer request determines the total time.
    val concurrentTime = measureTime {
        val balance = coroutineScope {
            val income = async { loadIncome() }
            val expenses = async { loadExpenses() }
            income.await() - expenses.await()
        }
        println("Баланс: $balance ₽")
    }
    println("Конкурентно: $concurrentTime")
}

private suspend fun exercise4() {
    println("До чтения: ${Thread.currentThread().name}")
    val balance = withContext(Dispatchers.IO) { readCachedBalance() }
    println("После чтения: ${Thread.currentThread().name}")
    println("Баланс из кеша: $balance ₽")
}

private suspend fun exercise5(): Unit = coroutineScope {
    val job = launch {
        try {
            repeat(5) { step ->
                delay(200)
                println("Шаг ${step + 1}")
            }
        } finally {
            println("Ресурс освобождён")
        }
    }
    delay(450)
    job.cancelAndJoin()
    println("Отмена завершена")
}

private suspend fun exercise6() {
    for (timeout in listOf(300L, 1_200L)) {
        val items = withTimeoutOrNull(timeout) { loadOperations(shouldFail = false) }
        if (items == null) println("Время ожидания истекло")
        else println("Загружено операций: ${items.size}")
    }
    try {
        loadOperations(shouldFail = true)
    } catch (error: IOException) {
        println("Ошибка: ${error.message}")
        // CancellationException is not caught, so cancellation propagates.
        val items = loadOperations(shouldFail = false)
        println("Загружено операций: ${items.size}")
    }
}

private suspend fun exercise7() {
    val updates = operationUpdates()
    updates.collect { println(it) }
    updates.filter { it.amount < 0 }
        .map { "${it.title} — ${abs(it.amount)} ₽" }
        .collect { println(it) }
}

private suspend fun exercise8(): Unit = coroutineScope {
    val mutableState = MutableStateFlow("Ожидание")
    val state = mutableState.asStateFlow()
    launch(start = CoroutineStart.UNDISPATCHED) {
        state.take(3).collect { println(it) }
    }
    mutableState.value = "Загрузка"
    val items = loadOperations(shouldFail = false)
    mutableState.value = "Загружено: ${items.size}"
}

private suspend fun loadIncome(): Int {
    delay(600)
    return 4_000
}

private suspend fun loadExpenses(): Int {
    delay(900)
    return 900
}

private fun readCachedBalance(): Int {
    println("Поток чтения кеша: ${Thread.currentThread().name}")
    // Имитируем блокирующий API чтения с диска.
    Thread.sleep(400)
    return 3_100
}

private data class Operation(val title: String, val amount: Int)

private val operations = listOf(
    Operation("Подработка", 4_000),
    Operation("Продукты", -650),
    Operation("Кофе", -250),
)

private suspend fun loadOperations(shouldFail: Boolean): List<Operation> {
    delay(600)
    if (shouldFail) throw IOException("Учебная ошибка загрузки")
    return operations
}

private fun operationUpdates() = flow {
    println("Источник запущен")
    for (operation in operations) {
        delay(200)
        emit(operation)
    }
}
