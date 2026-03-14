package com.quran.tathbeet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quran.tathbeet.data.local.dao.AppSettingsDao
import com.quran.tathbeet.data.local.dao.LearnerAccountDao
import com.quran.tathbeet.data.local.dao.ReviewAssignmentDao
import com.quran.tathbeet.data.local.dao.ReviewDayDao
import com.quran.tathbeet.data.local.dao.RevisionScheduleDao
import com.quran.tathbeet.data.local.dao.ScheduleSelectionDao
import com.quran.tathbeet.data.local.entity.AppSettingsEntity
import com.quran.tathbeet.data.local.entity.LearnerAccountEntity
import com.quran.tathbeet.data.local.entity.ReviewAssignmentEntity
import com.quran.tathbeet.data.local.entity.ReviewDayEntity
import com.quran.tathbeet.data.local.entity.RevisionScheduleEntity
import com.quran.tathbeet.data.local.entity.ScheduleSelectionEntity

@Database(
    entities = [
        LearnerAccountEntity::class,
        RevisionScheduleEntity::class,
        ScheduleSelectionEntity::class,
        AppSettingsEntity::class,
        ReviewDayEntity::class,
        ReviewAssignmentEntity::class,
    ],
    version = 6,
    exportSchema = false,
)
abstract class TathbeetDatabase : RoomDatabase() {
    abstract fun learnerAccountDao(): LearnerAccountDao

    abstract fun revisionScheduleDao(): RevisionScheduleDao

    abstract fun scheduleSelectionDao(): ScheduleSelectionDao

    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun reviewDayDao(): ReviewDayDao

    abstract fun reviewAssignmentDao(): ReviewAssignmentDao

    companion object {
        val Migration5To6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        ALTER TABLE app_settings
                        ADD COLUMN forceDarkTheme INTEGER NOT NULL DEFAULT 0
                        """.trimIndent(),
                    )
                }
            }
    }
}
