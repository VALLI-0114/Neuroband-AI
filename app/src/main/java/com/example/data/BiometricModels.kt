package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biometric_logs")
data class BiometricLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val heartRate: Int,
    val temperature: Double,
    val stressGsr: Double,
    val bloodPressureSystolic: Int,
    val bloodPressureDiastolic: Int,
    val steps: Int,
    val calories: Int,
    val stairs: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stress_scan_results")
data class StressScanResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stressScore: Int,
    val confidence: Int,
    val status: String,
    val hrvMs: Int,
    val conductanceUs: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/** One AI-coach chat bubble; sender is "user" or "coach". */
@Entity(tableName = "chat_messages")
data class ChatMessage(
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Entity(tableName = "mindfulness_sessions")
data class MindfulnessSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val durationMin: Int,
    val difficulty: String,
    val heartRateReduction: Int,
    val focusIncreasePercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)
