package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** An existing v1 install must keep its data when upgrading to the v2 schema (chat table added). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DatabaseMigrationTest {
    @Test
    fun upgradeFromV1KeepsSessionsAndAddsChat() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = context.getDatabasePath("neuroband_database").apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `biometric_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `heartRate` INTEGER NOT NULL, `temperature` REAL NOT NULL, `stressGsr` REAL NOT NULL, `bloodPressureSystolic` INTEGER NOT NULL, `bloodPressureDiastolic` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `calories` INTEGER NOT NULL, `stairs` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `stress_scan_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `stressScore` INTEGER NOT NULL, `confidence` INTEGER NOT NULL, `status` TEXT NOT NULL, `hrvMs` INTEGER NOT NULL, `conductanceUs` REAL NOT NULL, `timestamp` INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `mindfulness_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `durationMin` INTEGER NOT NULL, `difficulty` TEXT NOT NULL, `heartRateReduction` INTEGER NOT NULL, `focusIncreasePercent` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
            db.execSQL("INSERT INTO mindfulness_sessions (title, durationMin, difficulty, heartRateReduction, focusIncreasePercent, timestamp) VALUES ('Old session', 5, 'Beginner', 3, 0, 1)")
            db.version = 1
        }

        val dao = AppDatabase.getDatabase(context).biometricDao()
        assertEquals("Old session", dao.getAllMindfulnessSessions().first().single().title)

        dao.insertChatMessage(ChatMessage("user", "hello"))
        dao.insertChatMessage(ChatMessage("coach", "hi there"))
        assertEquals(listOf("hello", "hi there"), dao.getChatMessages().map { it.text })
    }
}
