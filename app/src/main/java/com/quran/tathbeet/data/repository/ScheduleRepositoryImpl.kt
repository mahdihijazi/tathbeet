package com.quran.tathbeet.data.repository

import androidx.room.withTransaction
import com.quran.tathbeet.data.local.TathbeetDatabase
import com.quran.tathbeet.data.local.entity.RevisionScheduleEntity
import com.quran.tathbeet.data.local.entity.ScheduleSelectionEntity
import com.quran.tathbeet.domain.model.CycleTarget
import com.quran.tathbeet.domain.model.PaceMethod
import com.quran.tathbeet.domain.model.PaceOption
import com.quran.tathbeet.domain.model.RevisionSchedule
import com.quran.tathbeet.domain.model.ScheduleSelection
import com.quran.tathbeet.domain.model.SelectionCategory
import com.quran.tathbeet.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ScheduleRepositoryImpl(
    private val database: TathbeetDatabase,
) : ScheduleRepository {

    override fun observeActiveSchedule(learnerId: String): Flow<RevisionSchedule?> =
        database.revisionScheduleDao()
            .observeActiveSchedule(learnerId)
            .flatMapLatest { scheduleEntity ->
                if (scheduleEntity == null) {
                    flowOf(null)
                } else {
                    database.scheduleSelectionDao()
                        .observeSelections(scheduleEntity.id)
                        .map { selectionEntities ->
                            scheduleEntity.toDomainModel(selectionEntities)
                        }
                }
            }

    override suspend fun saveSchedule(schedule: RevisionSchedule) {
        val scheduleId = "active-${schedule.learnerId}"
        database.withTransaction {
            database.revisionScheduleDao().clearActiveSchedule(schedule.learnerId)
            database.revisionScheduleDao().upsert(
                RevisionScheduleEntity(
                    id = scheduleId,
                    learnerId = schedule.learnerId,
                    paceMethod = schedule.paceMethod.name,
                    cycleTargetDays = schedule.cycleTarget.days,
                    manualPaceSegments = schedule.manualPace.dailySegments,
                    isActive = true,
                ),
            )
            database.scheduleSelectionDao().deleteForSchedule(scheduleId)
            database.scheduleSelectionDao().insertAll(
                schedule.selections.map { selection ->
                    ScheduleSelectionEntity(
                        scheduleId = scheduleId,
                        category = selection.category.name,
                        itemId = selection.itemId,
                        displayOrder = selection.displayOrder,
                    )
                },
            )
        }
    }

    private fun RevisionScheduleEntity.toDomainModel(
        selections: List<ScheduleSelectionEntity>,
    ): RevisionSchedule =
        RevisionSchedule(
            id = id,
            learnerId = learnerId,
            paceMethod = PaceMethod.valueOf(paceMethod),
            cycleTarget = CycleTarget.entries.first { it.days == cycleTargetDays },
            manualPace = PaceOption.entries.first { it.dailySegments == manualPaceSegments },
            selections = selections.map { selection ->
                ScheduleSelection(
                    category = SelectionCategory.valueOf(selection.category),
                    itemId = selection.itemId,
                    displayOrder = selection.displayOrder,
                )
            },
        )
}
