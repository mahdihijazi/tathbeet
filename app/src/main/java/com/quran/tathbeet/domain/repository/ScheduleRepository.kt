package com.quran.tathbeet.domain.repository

import com.quran.tathbeet.domain.model.RevisionSchedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun observeActiveSchedule(learnerId: String): Flow<RevisionSchedule?>

    suspend fun saveSchedule(schedule: RevisionSchedule)
}
