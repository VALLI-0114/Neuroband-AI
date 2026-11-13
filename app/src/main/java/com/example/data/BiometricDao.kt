package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BiometricDao {
    // Biometric Logs
    @Query("SELECT * FROM biometric_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentBiometricLogs(): Flow<List<BiometricLog>>

    @Query("SELECT * FROM biometric_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestBiometricLog(): Flow<BiometricLog?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometricLog(log: BiometricLog)

    // Stress Scans
    @Query("SELECT * FROM stress_scan_results ORDER BY timestamp DESC")
    fun getAllStressScans(): Flow<List<StressScanResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStressScanResult(result: StressScanResult)

    // Mindfulness Sessions
    @Query("SELECT * FROM mindfulness_sessions ORDER BY timestamp DESC")
    fun getAllMindfulnessSessions(): Flow<List<MindfulnessSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMindfulnessSession(session: MindfulnessSession)

    // AI coach chat (last 200, oldest first)
    @Query("SELECT * FROM (SELECT * FROM chat_messages ORDER BY id DESC LIMIT 200) ORDER BY id ASC")
    suspend fun getChatMessages(): List<ChatMessage>

    @Insert
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM stress_scan_results")
    suspend fun clearAllScans()
}
