package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AcademyDatabase
import com.example.data.AcademyDepartments
import com.example.data.AcademyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AcademyDatabase
    private lateinit var repository: AcademyRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AcademyDatabase::class.java
        )
            .allowMainThreadQueries()
            .addMigrations(
                AcademyDatabase.MIGRATION_1_2,
                AcademyDatabase.MIGRATION_2_3,
                AcademyDatabase.MIGRATION_3_4,
                AcademyDatabase.MIGRATION_4_5,
                AcademyDatabase.MIGRATION_5_6,
                AcademyDatabase.MIGRATION_6_7,
                AcademyDatabase.MIGRATION_1_6,
                AcademyDatabase.MIGRATION_2_6,
                AcademyDatabase.MIGRATION_3_6,
                AcademyDatabase.MIGRATION_4_6,
                AcademyDatabase.MIGRATION_1_7,
                AcademyDatabase.MIGRATION_2_7,
                AcademyDatabase.MIGRATION_3_7,
                AcademyDatabase.MIGRATION_4_7,
                AcademyDatabase.MIGRATION_5_7
            )
            .build()
        repository = AcademyRepository(database.academyDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun v6Features_realAuth_parentLink_teacherAssignedGroups_andSeparate100Marks_workCorrectly() = runBlocking {
        // 1. Verify "anas mustafa" is hidden/excluded from teachers list
        repository.addTeacher(
            name = "anas mustafa",
            department = AcademyDepartments.HIFZ,
            qualification = "Quran Teacher",
            phone = "03700880220"
        )
        repository.addTeacher(
            name = "Qari Sahib",
            department = AcademyDepartments.HIFZ,
            qualification = "Senior Qari",
            phone = "03129484214",
            assignedGroups = "${AcademyDepartments.HIFZ},${AcademyDepartments.TUITION_BOYS}"
        )

        val teachers = repository.allTeachers.first()
        assertEquals(1, teachers.size)
        assertFalse(teachers.any { it.name.contains("anas mustafa", ignoreCase = true) })
        assertEquals("Qari Sahib", teachers.first().name)

        // 2. Verify V6 Real Login System (Users Table + Role & Assigned Groups)
        val parentUser = repository.registerOrAuthenticateUser(
            name = "Haider Father",
            email = "parent.haider@gmail.com",
            phone = "03001234567",
            rawPassword = "123456",
            role = "Parent",
            isSignUp = true
        )
        assertEquals("Parent", parentUser.role)
        assertTrue(parentUser.id > 0)

        // 3. Verify Student enrollment + Parent Link ("Add kar lain - Pakka Pakka")
        val haiderId = repository.addStudentWithInitialFee(
            name = "Haider",
            fatherName = "Haider Father",
            className = "Class 6",
            department = AcademyDepartments.HIFZ,
            teacherName = "Qari Sahib",
            monthlyFee = 1500,
            phone = "03001234567",
            feePaidInitially = true
        ).toInt()
        repository.linkParentToStudent(
            studentId = haiderId,
            parentId = parentUser.id,
            parentName = parentUser.name
        )

        val allStudents = repository.allStudents.first()
        val haider = allStudents.first { it.id == haiderId }
        assertEquals(parentUser.id, haider.parentId)
        assertEquals("Haider Father", haider.parentName)

        // 4. Verify Teacher Assigned Groups permission logic
        assertTrue(
            AcademyDepartments.isDepartmentAllowedForTeacher(
                AcademyDepartments.HIFZ,
                "${AcademyDepartments.HIFZ},${AcademyDepartments.TUITION_BOYS}"
            )
        )
        assertFalse(
            AcademyDepartments.isDepartmentAllowedForTeacher(
                AcademyDepartments.GIRLS_PLAYGROUP,
                "${AcademyDepartments.HIFZ},${AcademyDepartments.TUITION_BOYS}"
            )
        )

        // 5. Verify Separate 100 + 100 Marks System (Quran Assessment out of 100 + School Assessment out of 100)
        repository.addTestRecord(
            title = "Monthly Assessment",
            subject = "Quran & School",
            department = AcademyDepartments.HIFZ,
            studentId = haiderId,
            studentName = "Haider",
            obtainedMarks = 92,
            totalMarks = 100,
            remarks = "Excellent",
            dateString = "2026-10-06",
            quranMarks = 92,
            quranTotalMarks = 100,
            schoolMarks = 88,
            schoolTotalMarks = 100
        )

        val tests = repository.allTests.first()
        assertEquals(1, tests.size)
        assertEquals(92, tests.first().quranMarks)
        assertEquals(100, tests.first().quranTotalMarks)
        assertEquals(92, tests.first().quranPercentage)
        assertEquals(88, tests.first().schoolMarks)
        assertEquals(100, tests.first().schoolTotalMarks)
        assertEquals(88, tests.first().schoolPercentage)

        // 6. Verify running migrations repeatedly on an existing database never triggers duplicate column errors
        val sqliteDb = database.openHelper.writableDatabase
        AcademyDatabase.MIGRATION_5_6.migrate(sqliteDb)
        AcademyDatabase.MIGRATION_6_7.migrate(sqliteDb)

        // 7. V7: Verify unassigned teacher permission check
        assertFalse(
            AcademyDepartments.isDepartmentAllowedForTeacher(
                AcademyDepartments.HIFZ,
                ""
            )
        )
    }
}
