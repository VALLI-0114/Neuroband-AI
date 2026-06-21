package com.example.data

import kotlinx.coroutines.flow.Flow

class BiometricRepository(private val biometricDao: BiometricDao) {
    val recentLogs: Flow<List<BiometricLog>> = biometricDao.getRecentBiometricLogs()
    val latestLog: Flow<BiometricLog?> = biometricDao.getLatestBiometricLog()
    val allStressScans: Flow<List<StressScanResult>> = biometricDao.getAllStressScans()
    val allMindfulnessSessions: Flow<List<MindfulnessSession>> = biometricDao.getAllMindfulnessSessions()

    suspend fun insertLog(log: BiometricLog) {
        biometricDao.insertBiometricLog(log)
    }

    suspend fun insertScan(result: StressScanResult) {
        biometricDao.insertStressScanResult(result)
    }

    suspend fun insertSession(session: MindfulnessSession) {
        biometricDao.insertMindfulnessSession(session)
    }

    suspend fun clearScans() {
        biometricDao.clearAllScans()
    }
}
