package me.plony.bot.utils

import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

suspend fun <T> suspendedTransaction(
    context: CoroutineDispatcher? = null,
    db: Database? = null,
    transactionIsolation: Int? = null,
    statement: suspend Transaction.() -> T
) = suspendedTransactionAsync(
    context, db, transactionIsolation, statement
).await()