package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserAccountEntity::class,
        StudentEntity::class,
        GroupEntity::class,
        SabaqEntity::class,
        TeacherEntity::class,
        AlertEntity::class,
        FeeRecordEntity::class,
        AttendanceEntity::class,
        BookEntity::class,
        QuranProgressEntity::class,
        TestRecordEntity::class,
        SchoolBookEntity::class,
        SchoolSabaqEntity::class,
        MonthlyReportEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AcademyDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao

    companion object {
        @Volatile
        private var INSTANCE: AcademyDatabase? = null

        private fun hasColumn(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String
        ): Boolean {
            val foundViaSelect = runCatching {
                db.query("SELECT * FROM `$tableName` LIMIT 0").use { cursor ->
                    cursor.columnNames.any { it.equals(columnName, ignoreCase = true) }
                }
            }.getOrDefault(false)
            if (foundViaSelect) return true

            return runCatching {
                db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        while (cursor.moveToNext()) {
                            if (cursor.getString(nameIndex).equals(columnName, ignoreCase = true)) {
                                return@use true
                            }
                        }
                    }
                    false
                }
            }.getOrDefault(false)
        }

        private fun addColumnIfMissing(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDefinition: String
        ) {
            if (!hasColumn(db, tableName, columnName)) {
                db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
            }
        }

        private fun upgradeAndPreserveAllTablesToV6(db: SupportSQLiteDatabase) {
            // 1. Users table for V6 Real Login System (Admin / Teacher / Parent + Assigned Groups + Linked Students)
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `users` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT NOT NULL DEFAULT '',
                    `phone` TEXT NOT NULL DEFAULT '',
                    `passwordHash` TEXT NOT NULL DEFAULT '',
                    `role` TEXT NOT NULL DEFAULT 'Teacher',
                    `assignedGroups` TEXT NOT NULL DEFAULT '',
                    `assignedGroupsJson` TEXT NOT NULL DEFAULT '[]',
                    `linkedStudentIds` TEXT NOT NULL DEFAULT '',
                    `isApproved` INTEGER NOT NULL DEFAULT 1,
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            addColumnIfMissing(db, "users", "assignedGroupsJson", "TEXT NOT NULL DEFAULT '[]'")
            addColumnIfMissing(db, "users", "linkedStudentIds", "TEXT NOT NULL DEFAULT ''")

            // 2. Preserve old tables if upgrading from any earlier schema
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `students` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `fatherName` TEXT NOT NULL DEFAULT '',
                    `className` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `teacherName` TEXT NOT NULL,
                    `monthlyFee` INTEGER NOT NULL DEFAULT 800,
                    `phone` TEXT NOT NULL DEFAULT '',
                    `enrolledAt` INTEGER NOT NULL DEFAULT 0,
                    `parentId` INTEGER NOT NULL DEFAULT 0,
                    `parentName` TEXT NOT NULL DEFAULT '',
                    `quranStatus` TEXT NOT NULL DEFAULT 'MaShaAllah reciting with strong Tajweed & regular Manzil',
                    `schoolStatus` TEXT NOT NULL DEFAULT 'Good performance in English, Urdu, Maths & Science'
                )
                """.trimIndent()
            )
            addColumnIfMissing(db, "students", "parentId", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(db, "students", "parentName", "TEXT NOT NULL DEFAULT ''")
            addColumnIfMissing(
                db,
                "students",
                "quranStatus",
                "TEXT NOT NULL DEFAULT 'MaShaAllah reciting with strong Tajweed & regular Manzil'"
            )
            addColumnIfMissing(
                db,
                "students",
                "schoolStatus",
                "TEXT NOT NULL DEFAULT 'Good performance in English, Urdu, Maths & Science'"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `groups` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL DEFAULT '',
                    `teacherName` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `sabaq` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `studentId` INTEGER NOT NULL DEFAULT 0,
                    `studentName` TEXT NOT NULL,
                    `bookName` TEXT NOT NULL DEFAULT 'Quran',
                    `sabaqDetail` TEXT NOT NULL,
                    `dateString` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `teachers` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `qualification` TEXT NOT NULL DEFAULT 'Qari & Islamic Scholar',
                    `phone` TEXT NOT NULL DEFAULT '',
                    `createdAt` INTEGER NOT NULL DEFAULT 0,
                    `assignedGroups` TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )
            addColumnIfMissing(db, "teachers", "assignedGroups", "TEXT NOT NULL DEFAULT ''")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `alerts` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `message` TEXT NOT NULL,
                    `priority` TEXT NOT NULL DEFAULT 'Important',
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `fees` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `studentId` INTEGER NOT NULL,
                    `studentName` TEXT NOT NULL,
                    `fatherName` TEXT NOT NULL DEFAULT '',
                    `className` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `amount` INTEGER NOT NULL,
                    `monthCycle` TEXT NOT NULL DEFAULT 'October cycle',
                    `isPaid` INTEGER NOT NULL DEFAULT 0,
                    `paidAt` INTEGER,
                    `receiptNumber` TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `attendance` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `studentId` INTEGER NOT NULL,
                    `studentName` TEXT NOT NULL,
                    `className` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `dateString` TEXT NOT NULL,
                    `status` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `books` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `authorOrNotes` TEXT NOT NULL DEFAULT '',
                    `totalPages` INTEGER NOT NULL DEFAULT 100,
                    `completedPages` INTEGER NOT NULL DEFAULT 0,
                    `assignedStudentId` INTEGER,
                    `assignedStudentName` TEXT NOT NULL DEFAULT '',
                    `updatedAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `quran_logs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `studentId` INTEGER NOT NULL,
                    `studentName` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `paraNumber` INTEGER NOT NULL,
                    `surahName` TEXT NOT NULL,
                    `sabakDetail` TEXT NOT NULL,
                    `sabqiDetail` TEXT NOT NULL,
                    `manzilDetail` TEXT NOT NULL,
                    `quality` TEXT NOT NULL DEFAULT 'Excellent',
                    `teacherName` TEXT NOT NULL DEFAULT 'Qari Sahib',
                    `recordedAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `tests` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `title` TEXT NOT NULL,
                    `subject` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `studentId` INTEGER NOT NULL,
                    `studentName` TEXT NOT NULL,
                    `obtainedMarks` INTEGER NOT NULL,
                    `totalMarks` INTEGER NOT NULL,
                    `remarks` TEXT NOT NULL DEFAULT '',
                    `dateString` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL DEFAULT 0,
                    `quranMarks` INTEGER NOT NULL DEFAULT 92,
                    `quranTotalMarks` INTEGER NOT NULL DEFAULT 100,
                    `schoolMarks` INTEGER NOT NULL DEFAULT 90,
                    `schoolTotalMarks` INTEGER NOT NULL DEFAULT 100
                )
                """.trimIndent()
            )
            addColumnIfMissing(db, "tests", "quranMarks", "INTEGER NOT NULL DEFAULT 92")
            addColumnIfMissing(db, "tests", "quranTotalMarks", "INTEGER NOT NULL DEFAULT 100")
            addColumnIfMissing(db, "tests", "schoolMarks", "INTEGER NOT NULL DEFAULT 90")
            addColumnIfMissing(db, "tests", "schoolTotalMarks", "INTEGER NOT NULL DEFAULT 100")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `school_books` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `bookName` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `school_sabaq` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `studentId` INTEGER NOT NULL,
                    `studentName` TEXT NOT NULL,
                    `className` TEXT NOT NULL DEFAULT '',
                    `dateString` TEXT NOT NULL,
                    `bookName` TEXT NOT NULL,
                    `sabaqDetail` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `monthly_reports` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `studentId` INTEGER NOT NULL,
                    `studentName` TEXT NOT NULL,
                    `className` TEXT NOT NULL,
                    `department` TEXT NOT NULL,
                    `quranTeacherRemark` TEXT NOT NULL,
                    `principalRemark` TEXT NOT NULL,
                    `classTeacherRemark` TEXT NOT NULL,
                    `behaviorWithFellows` TEXT NOT NULL,
                    `behaviorWithTeachers` TEXT NOT NULL,
                    `needAttentionIn` TEXT NOT NULL,
                    `participatedInMilad` INTEGER NOT NULL,
                    `dateString` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            // Remove anas mustafa from teachers table if present
            db.execSQL("DELETE FROM `teachers` WHERE LOWER(TRIM(`name`)) = 'anas mustafa' OR LOWER(`name`) LIKE '%anas mustafa%'")
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_1_5 = object : Migration(1, 5) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_2_5 = object : Migration(2, 5) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_3_5 = object : Migration(3, 5) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_1_6 = object : Migration(1, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_2_6 = object : Migration(2, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_3_6 = object : Migration(3, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_1_7 = object : Migration(1, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_2_7 = object : Migration(2, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_3_7 = object : Migration(3, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_4_7 = object : Migration(4, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }
        val MIGRATION_5_7 = object : Migration(5, 7) {
            override fun migrate(db: SupportSQLiteDatabase) = upgradeAndPreserveAllTablesToV6(db)
        }

        fun getDatabase(context: Context): AcademyDatabase {
            return INSTANCE ?: synchronized(this) {
                // DO NOT use fallbackToDestructiveMigration() — preserve all existing data using .addMigrations()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcademyDatabase::class.java,
                    "al_hadid_academy_db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_1_5,
                        MIGRATION_2_5,
                        MIGRATION_3_5,
                        MIGRATION_1_6,
                        MIGRATION_2_6,
                        MIGRATION_3_6,
                        MIGRATION_4_6,
                        MIGRATION_1_7,
                        MIGRATION_2_7,
                        MIGRATION_3_7,
                        MIGRATION_4_7,
                        MIGRATION_5_7
                    )
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            runCatching {
                                db.execSQL("DELETE FROM `teachers` WHERE LOWER(TRIM(`name`)) = 'anas mustafa' OR LOWER(`name`) LIKE '%anas mustafa%'")
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
