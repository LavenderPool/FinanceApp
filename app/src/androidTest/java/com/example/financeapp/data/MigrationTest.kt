package com.example.financeapp.data

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        databaseClass = AppDatabase::class,
        driver = AndroidSQLiteDriver(),
        file = instrumentation.targetContext.getDatabasePath(TEST_DB),
    )

    @Test
    fun migrate1To2_keepsExistingRows() = runBlocking {
        val connection = helper.createDatabase(1)
        connection.execSQL(
            "INSERT INTO transactions (description, amount_minor, type, occurred_at) " +
                "VALUES ('Кофе', 25000, 'EXPENSE', 1000)",
        )
        connection.close()

        val migrated = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))
        val hasRow = migrated.prepare("SELECT COUNT(*) FROM transactions").use {
            it.step()
            it.getLong(0) > 0
        }
        migrated.close()

        assertTrue("Existing transaction was not kept after migration 1 to 2", hasRow)
    }

    @Test
    fun migrate2To3_appliesAutoMigration() = runBlocking {
        helper.createDatabase(2).close()

        helper.runMigrationsAndValidate(3, emptyList()).close()
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
