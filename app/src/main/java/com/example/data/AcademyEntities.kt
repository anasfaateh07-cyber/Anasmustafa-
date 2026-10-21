package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

object AcademyDepartments {
    const val COMMON_ALL = "Common - All Students"
    const val HIFZ = "Hifz Group"
    const val NAZRA = "Nazra & Tajweed"
    const val TUITION_BOYS = "Tuition Boys"
    const val GIRLS_PLAYGROUP = "Girls & Playgroup"

    // Sub-groups for Groups Tab
    const val SUB_QURAN_HIFZ = "Quran Hifz"
    const val SUB_NAZRA = "Nazra"
    const val SUB_TAJWEED = "Tajweed"
    const val SUB_TUITION_BOYS = "Tuition Boys"
    const val SUB_TUITION_GIRLS = "Tuition Girls"
    const val SUB_PLAYGROUP = "Playgroup"

    val ALL = listOf(HIFZ, NAZRA, TUITION_BOYS, GIRLS_PLAYGROUP)

    val ASSIGNABLE_GROUPS = listOf(
        HIFZ,
        NAZRA,
        TUITION_BOYS,
        GIRLS_PLAYGROUP,
        SUB_QURAN_HIFZ,
        SUB_NAZRA,
        SUB_TAJWEED,
        SUB_TUITION_GIRLS,
        SUB_PLAYGROUP
    )

    val GROUP_SUB_TABS = listOf(
        COMMON_ALL,
        SUB_QURAN_HIFZ,
        SUB_NAZRA,
        SUB_TAJWEED,
        SUB_TUITION_BOYS,
        SUB_TUITION_GIRLS,
        SUB_PLAYGROUP
    )

    val DEFAULT_SCHOOL_BOOKS = listOf(
        "Urdu",
        "English",
        "Maths",
        "Science",
        "Islamiat"
    )

    fun shortLabel(dept: String): String = when (dept) {
        HIFZ, SUB_QURAN_HIFZ -> "Hifz"
        NAZRA, SUB_NAZRA -> "Nazra"
        SUB_TAJWEED -> "Tajweed"
        TUITION_BOYS -> "Tuition"
        SUB_TUITION_GIRLS -> "Tuition Girls"
        GIRLS_PLAYGROUP, SUB_PLAYGROUP -> "Playgroup"
        else -> dept
    }

    fun matchesSubGroup(studentDept: String, subGroup: String): Boolean {
        if (subGroup == COMMON_ALL || subGroup == "All") return true
        val normDept = studentDept.lowercase()
        return when (subGroup) {
            HIFZ, SUB_QURAN_HIFZ -> normDept.contains("hifz")
            NAZRA, SUB_NAZRA -> normDept.contains("nazra") || normDept.contains("tajweed")
            SUB_TAJWEED -> normDept.contains("tajweed") || normDept.contains("nazra")
            TUITION_BOYS, SUB_TUITION_BOYS -> normDept.contains("tuition") && !normDept.contains("girl")
            SUB_TUITION_GIRLS -> normDept.contains("girl")
            GIRLS_PLAYGROUP, SUB_PLAYGROUP -> normDept.contains("playgroup") || normDept.contains("girl")
            else -> studentDept.equals(subGroup, ignoreCase = true)
        }
    }

    fun parseAssignedGroups(assignedGroupsCsv: String): List<String> =
        assignedGroupsCsv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

    fun isDepartmentAllowedForTeacher(targetDept: String, assignedGroupsCsv: String): Boolean {
        val groups = parseAssignedGroups(assignedGroupsCsv)
        if (groups.isEmpty()) return false
        if (groups.any { it.equals("All", ignoreCase = true) || it.equals(COMMON_ALL, ignoreCase = true) }) {
            return true
        }
        return groups.any { assigned ->
            assigned.equals(targetDept, ignoreCase = true) ||
                matchesSubGroup(targetDept, assigned) ||
                matchesSubGroup(assigned, targetDept)
        }
    }
}

@Entity(tableName = "users")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val role: String = "Teacher", // "Admin", "Teacher", "Parent"
    val assignedGroups: String = "", // Comma-separated groups assigned by Admin
    val assignedGroupsJson: String = "[]",
    val linkedStudentIds: String = "",
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val fatherName: String = "",
    val className: String,
    val department: String,
    val teacherName: String,
    val monthlyFee: Int = 800,
    val phone: String = "",
    val enrolledAt: Long = System.currentTimeMillis(),
    val parentId: Int = 0,
    val parentName: String = "",
    val quranStatus: String = "MaShaAllah reciting with strong Tajweed & regular Manzil",
    val schoolStatus: String = "Good performance in English, Urdu, Maths & Science"
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val teacherName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sabaq")
data class SabaqEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int = 0,
    val studentName: String,
    val bookName: String = "Quran",
    val sabaqDetail: String,
    val dateString: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val department: String,
    val qualification: String = "Qari & Islamic Scholar",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val assignedGroups: String = ""
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val priority: String = "Important",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fees")
data class FeeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val fatherName: String = "",
    val className: String,
    val department: String,
    val amount: Int,
    val monthCycle: String = "October cycle",
    val isPaid: Boolean = false,
    val paidAt: Long? = null,
    val receiptNumber: String = ""
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val className: String,
    val department: String,
    val dateString: String,
    val status: String // "PRESENT", "ABSENT", "LEAVE"
)

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val authorOrNotes: String = "",
    val totalPages: Int = 100,
    val completedPages: Int = 0,
    val assignedStudentId: Int? = null,
    val assignedStudentName: String = "",
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (totalPages > 0) (completedPages.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

@Entity(tableName = "quran_logs")
data class QuranProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val department: String,
    val paraNumber: Int,
    val surahName: String,
    val sabakDetail: String,
    val sabqiDetail: String,
    val manzilDetail: String,
    val quality: String = "Excellent",
    val teacherName: String = "Qari Sahib",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tests")
data class TestRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val department: String,
    val studentId: Int,
    val studentName: String,
    val obtainedMarks: Int,
    val totalMarks: Int = 100,
    val remarks: String = "",
    val dateString: String,
    val createdAt: Long = System.currentTimeMillis(),
    // Separate 100 + 100 Marks System (Quran Pak / Nazra out of 100 + School out of 100)
    val quranMarks: Int = obtainedMarks,
    val quranTotalMarks: Int = 100,
    val schoolMarks: Int = obtainedMarks,
    val schoolTotalMarks: Int = 100
) {
    val quranPercentage: Int
        get() = if (quranTotalMarks > 0) ((quranMarks * 100f) / quranTotalMarks).toInt().coerceIn(0, 100) else 0

    val schoolPercentage: Int
        get() = if (schoolTotalMarks > 0) ((schoolMarks * 100f) / schoolTotalMarks).toInt().coerceIn(0, 100) else 0

    val percentage: Int
        get() {
            val combinedTotal = quranTotalMarks + schoolTotalMarks
            return if (combinedTotal > 0) {
                (((quranMarks + schoolMarks) * 100f) / combinedTotal).toInt().coerceIn(0, 100)
            } else if (totalMarks > 0) {
                ((obtainedMarks * 100f) / totalMarks).toInt().coerceIn(0, 100)
            } else 0
        }

    val grade: String
        get() = when {
            percentage >= 90 -> "A+"
            percentage >= 80 -> "A"
            percentage >= 70 -> "B"
            percentage >= 60 -> "C"
            else -> "Needs Focus"
        }
}

@Entity(tableName = "school_books")
data class SchoolBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookName: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "school_sabaq")
data class SchoolSabaqEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val className: String = "",
    val dateString: String,
    val bookName: String,
    val sabaqDetail: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "monthly_reports")
data class MonthlyReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val className: String,
    val department: String,
    val quranTeacherRemark: String,
    val principalRemark: String,
    val classTeacherRemark: String,
    val behaviorWithFellows: String, // Excellent / Good / Average / Needs Improvement
    val behaviorWithTeachers: String, // Excellent / Good / Average / Needs Improvement
    val needAttentionIn: String, // School, Quran, Behavior
    val participatedInMilad: Boolean, // Yes / No
    val dateString: String,
    val createdAt: Long = System.currentTimeMillis()
)
