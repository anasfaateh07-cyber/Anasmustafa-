package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcademyRepository(private val dao: AcademyDao) {

    val allUsers: Flow<List<UserAccountEntity>> = dao.getAllUsers()
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    val allGroups: Flow<List<GroupEntity>> = dao.getAllGroups()
    val allLegacySabaq: Flow<List<SabaqEntity>> = dao.getAllLegacySabaq()

    // Ensure "anas mustafa" is never shown in the teachers list
    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers().map { list ->
        list.filterNot {
            it.name.trim().equals("anas mustafa", ignoreCase = true) ||
                it.name.contains("anas mustafa", ignoreCase = true)
        }
    }

    val allAlerts: Flow<List<AlertEntity>> = dao.getAllAlerts()
    val allFees: Flow<List<FeeRecordEntity>> = dao.getAllFees()
    val allAttendance: Flow<List<AttendanceEntity>> = dao.getAllAttendance()
    val allBooks: Flow<List<BookEntity>> = dao.getAllBooks()
    val allQuranLogs: Flow<List<QuranProgressEntity>> = dao.getAllQuranLogs()
    val allTests: Flow<List<TestRecordEntity>> = dao.getAllTests()
    val allSchoolBooks: Flow<List<SchoolBookEntity>> = dao.getAllSchoolBooks()
    val allSchoolSabaq: Flow<List<SchoolSabaqEntity>> = dao.getAllSchoolSabaq()
    val allMonthlyReports: Flow<List<MonthlyReportEntity>> = dao.getAllMonthlyReports()

    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForDate(dateString)

    suspend fun cleanupAnasMustafaTeacher() {
        dao.removeAnasMustafaFromTeachers()
    }

    // Real Room + SharedPreferences Auth System
    fun hashPassword(rawPassword: String): String {
        if (rawPassword.isBlank()) return ""
        return runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.digest(rawPassword.trim().toByteArray()).joinToString("") { "%02x".format(it) }
        }.getOrDefault("")
    }

    suspend fun registerOrAuthenticateUser(
        name: String,
        email: String,
        phone: String,
        rawPassword: String,
        role: String,
        isSignUp: Boolean
    ): UserAccountEntity {
        val cleanRole = role.removePrefix("As a ").removePrefix("As an ").trim().ifEmpty { "Teacher" }
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()
        val cleanName = name.trim().ifEmpty {
            when (cleanRole) {
                "Admin" -> "Administrator"
                "Parent" -> "Respected Parent"
                "Student" -> "Academy Student"
                else -> "Qari Sahib"
            }
        }

        if (isSignUp) {
            // Prevent unauthorized privilege escalation
            if (cleanRole.equals("Admin", ignoreCase = true) || cleanRole.equals("Principal", ignoreCase = true)) {
                throw SecurityException("Admin accounts cannot be registered publicly. They must be designated by an administrator.")
            }
        }

        val passHash = hashPassword(rawPassword)

        val existing = when {
            cleanEmail.isNotBlank() -> dao.getUserByEmail(cleanEmail)
            cleanPhone.isNotBlank() -> dao.getUserByPhone(cleanPhone)
            else -> null
        }

        if (existing != null && !isSignUp) {
            // Real password verification (unless authenticated upstream via Firebase or Google)
            if (rawPassword.isNotBlank() && rawPassword != "google_authenticated" && rawPassword != "firebase_authenticated") {
                if (existing.passwordHash.isNotBlank() && existing.passwordHash != passHash) {
                    throw IllegalArgumentException("Incorrect password. Please verify and try again.")
                }
            }
            val updated = existing.copy(
                name = if (name.isNotBlank()) cleanName else existing.name,
                role = if (cleanRole.isNotBlank()) cleanRole else existing.role
            )
            dao.updateUser(updated)
            return updated
        }

        val defaultAssignedGroups = when (cleanRole) {
            "Admin" -> "All"
            "Teacher" -> existing?.assignedGroups ?: "" // Never default to Hifz/Tuition; empty until assigned!
            else -> ""
        }

        val groupsJson = AcademyDepartments.parseAssignedGroups(defaultAssignedGroups)
            .joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }

        val userToSave = UserAccountEntity(
            id = existing?.id ?: 0,
            name = cleanName,
            email = cleanEmail,
            phone = cleanPhone,
            passwordHash = passHash,
            role = cleanRole,
            assignedGroups = defaultAssignedGroups,
            assignedGroupsJson = groupsJson,
            linkedStudentIds = existing?.linkedStudentIds ?: "",
            isApproved = true
        )
        val newId = dao.insertUser(userToSave).toInt()
        val savedUser = userToSave.copy(id = if (existing != null && existing.id > 0) existing.id else newId)

        // If a Teacher registers and isn't "anas mustafa", ensure they also appear in teachers table so Admin can assign groups
        if (cleanRole.equals("Teacher", ignoreCase = true) &&
            !cleanName.contains("anas mustafa", ignoreCase = true)
        ) {
            dao.insertTeacher(
                TeacherEntity(
                    name = cleanName,
                    department = AcademyDepartments.HIFZ,
                    qualification = "Academy Teacher",
                    phone = cleanPhone,
                    assignedGroups = savedUser.assignedGroups
                )
            )
        }
        return savedUser
    }

    suspend fun updateTeacherAssignedGroups(
        teacher: TeacherEntity?,
        userAccount: UserAccountEntity?,
        assignedGroupsCsv: String
    ) {
        val cleanGroups = assignedGroupsCsv.trim()
        val primaryDept = AcademyDepartments.parseAssignedGroups(cleanGroups).firstOrNull()
            ?: teacher?.department
            ?: AcademyDepartments.HIFZ

        if (teacher != null) {
            dao.updateTeacher(
                teacher.copy(
                    department = primaryDept,
                    assignedGroups = cleanGroups
                )
            )
        }
        if (userAccount != null) {
            val groupsJson = AcademyDepartments.parseAssignedGroups(cleanGroups)
                .joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }
            dao.updateUser(
                userAccount.copy(
                    assignedGroups = cleanGroups,
                    assignedGroupsJson = groupsJson,
                    isApproved = true
                )
            )
        }
    }

    suspend fun linkParentToStudent(studentId: Int, parentId: Int, parentName: String) {
        dao.linkParentToStudent(
            studentId = studentId,
            parentId = parentId,
            parentName = parentName.trim().ifEmpty { "Parent" }
        )
        if (parentId > 0) {
            val parentUser = dao.getUserById(parentId)
            if (parentUser != null) {
                val existingIds = parentUser.linkedStudentIds
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toMutableSet()
                existingIds.add(studentId)
                dao.updateUser(
                    parentUser.copy(
                        linkedStudentIds = existingIds.joinToString(",")
                    )
                )
            }
        }
    }

    suspend fun addStudentWithInitialFee(
        name: String,
        fatherName: String,
        className: String,
        department: String,
        teacherName: String,
        monthlyFee: Int,
        phone: String,
        feePaidInitially: Boolean = false
    ): Long {
        val cleanTeacher = if (teacherName.trim().equals("anas mustafa", ignoreCase = true)) {
            "Qari Sahib"
        } else {
            teacherName.trim().ifEmpty { "Qari Sahib" }
        }
        val student = StudentEntity(
            name = name.trim(),
            fatherName = fatherName.trim(),
            className = className.trim(),
            department = department,
            teacherName = cleanTeacher,
            monthlyFee = monthlyFee,
            phone = phone.trim()
        )
        val studentId = dao.insertStudent(student).toInt()
        if (monthlyFee > 0) {
            val receiptNo = "AHA-${1000 + studentId}-${System.currentTimeMillis() % 1000}"
            dao.insertFee(
                FeeRecordEntity(
                    studentId = studentId,
                    studentName = student.name,
                    fatherName = student.fatherName,
                    className = student.className,
                    department = student.department,
                    amount = monthlyFee,
                    monthCycle = "October cycle",
                    isPaid = feePaidInitially,
                    paidAt = if (feePaidInitially) System.currentTimeMillis() else null,
                    receiptNumber = receiptNo
                )
            )
        }
        return studentId.toLong()
    }

    suspend fun updateStudent(student: StudentEntity) = dao.updateStudent(student)

    suspend fun deleteStudent(student: StudentEntity) {
        dao.deleteFeesForStudent(student.id)
        dao.deleteAttendanceForStudent(student.id)
        dao.deleteStudent(student)
    }

    suspend fun addTeacher(
        name: String,
        department: String,
        qualification: String,
        phone: String,
        assignedGroups: String = department
    ) {
        val trimmed = name.trim()
        // Do not allow adding "anas mustafa" as teacher
        if (trimmed.equals("anas mustafa", ignoreCase = true) || trimmed.contains("anas mustafa", ignoreCase = true)) {
            return
        }
        dao.insertTeacher(
            TeacherEntity(
                name = trimmed,
                department = department,
                qualification = qualification.trim(),
                phone = phone.trim(),
                assignedGroups = assignedGroups.ifBlank { department }
            )
        )
    }

    suspend fun deleteTeacher(teacher: TeacherEntity) = dao.deleteTeacher(teacher)

    suspend fun addAlert(title: String, message: String, priority: String) {
        dao.insertAlert(
            AlertEntity(
                title = title.trim(),
                message = message.trim(),
                priority = priority
            )
        )
    }

    suspend fun deleteAlert(alert: AlertEntity) = dao.deleteAlert(alert)

    suspend fun addFeeRecord(
        student: StudentEntity,
        amount: Int,
        monthCycle: String,
        isPaid: Boolean
    ) {
        val receiptNo = "AHA-${1000 + student.id}-${(100..999).random()}"
        dao.insertFee(
            FeeRecordEntity(
                studentId = student.id,
                studentName = student.name,
                fatherName = student.fatherName,
                className = student.className,
                department = student.department,
                amount = amount,
                monthCycle = monthCycle,
                isPaid = isPaid,
                paidAt = if (isPaid) System.currentTimeMillis() else null,
                receiptNumber = receiptNo
            )
        )
    }

    suspend fun toggleFeePaidStatus(fee: FeeRecordEntity) {
        val nowPaid = !fee.isPaid
        dao.updateFee(
            fee.copy(
                isPaid = nowPaid,
                paidAt = if (nowPaid) System.currentTimeMillis() else null,
                receiptNumber = fee.receiptNumber.ifEmpty { "AHA-${1000 + fee.id}" }
            )
        )
    }

    suspend fun deleteFee(fee: FeeRecordEntity) = dao.deleteFee(fee)

    suspend fun markAttendance(
        student: StudentEntity,
        dateString: String,
        status: String
    ) {
        val existing = dao.getStudentAttendanceOnDate(student.id, dateString)
        if (existing != null) {
            dao.updateAttendance(existing.copy(status = status))
        } else {
            dao.insertAttendance(
                AttendanceEntity(
                    studentId = student.id,
                    studentName = student.name,
                    className = student.className,
                    department = student.department,
                    dateString = dateString,
                    status = status
                )
            )
        }
    }

    suspend fun markAllPresent(students: List<StudentEntity>, dateString: String) {
        students.forEach { student ->
            markAttendance(student, dateString, "PRESENT")
        }
    }

    // Books
    suspend fun addBook(
        title: String,
        category: String,
        authorOrNotes: String,
        totalPages: Int,
        completedPages: Int = 0,
        assignedStudentId: Int? = null,
        assignedStudentName: String = ""
    ) {
        dao.insertBook(
            BookEntity(
                title = title.trim(),
                category = category.trim(),
                authorOrNotes = authorOrNotes.trim(),
                totalPages = totalPages.coerceAtLeast(1),
                completedPages = completedPages.coerceIn(0, totalPages.coerceAtLeast(1)),
                assignedStudentId = assignedStudentId,
                assignedStudentName = assignedStudentName.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateBook(book: BookEntity) {
        dao.updateBook(
            book.copy(
                completedPages = book.completedPages.coerceIn(0, book.totalPages.coerceAtLeast(1)),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun assignBookToStudent(book: BookEntity, studentId: Int?, studentName: String) {
        dao.updateBook(
            book.copy(
                assignedStudentId = studentId,
                assignedStudentName = studentName.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateBookProgress(book: BookEntity, completedPages: Int) {
        dao.updateBook(
            book.copy(
                completedPages = completedPages.coerceIn(0, book.totalPages.coerceAtLeast(1)),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteBook(book: BookEntity) = dao.deleteBook(book)

    // Quran
    suspend fun addQuranLog(
        studentId: Int,
        studentName: String,
        department: String,
        paraNumber: Int,
        surahName: String,
        sabakDetail: String,
        sabqiDetail: String,
        manzilDetail: String,
        quality: String,
        teacherName: String
    ) {
        dao.insertQuranLog(
            QuranProgressEntity(
                studentId = studentId,
                studentName = studentName.trim(),
                department = department,
                paraNumber = paraNumber.coerceIn(1, 30),
                surahName = surahName.trim(),
                sabakDetail = sabakDetail.trim(),
                sabqiDetail = sabqiDetail.trim(),
                manzilDetail = manzilDetail.trim(),
                quality = quality,
                teacherName = teacherName.trim()
            )
        )
    }

    suspend fun deleteQuranLog(log: QuranProgressEntity) = dao.deleteQuranLog(log)

    // Tests (Separate 100 + 100 Marks System: Quran Pak / Nazra = 100, School = 100)
    suspend fun addTestRecord(
        title: String,
        subject: String,
        department: String,
        studentId: Int,
        studentName: String,
        obtainedMarks: Int,
        totalMarks: Int,
        remarks: String,
        dateString: String,
        quranMarks: Int = obtainedMarks.coerceIn(0, 100),
        quranTotalMarks: Int = 100,
        schoolMarks: Int = obtainedMarks.coerceIn(0, 100),
        schoolTotalMarks: Int = 100
    ) {
        val safeQuran = quranMarks.coerceIn(0, quranTotalMarks.coerceAtLeast(1))
        val safeSchool = schoolMarks.coerceIn(0, schoolTotalMarks.coerceAtLeast(1))
        dao.insertTest(
            TestRecordEntity(
                title = title.trim(),
                subject = subject.trim(),
                department = department,
                studentId = studentId,
                studentName = studentName.trim(),
                obtainedMarks = safeQuran,
                totalMarks = quranTotalMarks.coerceAtLeast(1),
                remarks = remarks.trim(),
                dateString = dateString,
                quranMarks = safeQuran,
                quranTotalMarks = quranTotalMarks.coerceAtLeast(1),
                schoolMarks = safeSchool,
                schoolTotalMarks = schoolTotalMarks.coerceAtLeast(1)
            )
        )
    }

    suspend fun deleteTestRecord(test: TestRecordEntity) = dao.deleteTest(test)

    // School Books & School Sabaq
    suspend fun addSchoolBookName(bookName: String) {
        val clean = bookName.trim()
        if (clean.isBlank()) return
        dao.insertSchoolBook(SchoolBookEntity(bookName = clean))
    }

    suspend fun deleteSchoolBook(book: SchoolBookEntity) = dao.deleteSchoolBook(book)

    suspend fun addSchoolSabaq(
        studentId: Int,
        studentName: String,
        className: String,
        dateString: String,
        bookName: String,
        sabaqDetail: String
    ) {
        if (studentName.isBlank() || bookName.isBlank() || sabaqDetail.isBlank()) return
        dao.insertSchoolSabaq(
            SchoolSabaqEntity(
                studentId = studentId,
                studentName = studentName.trim(),
                className = className.trim(),
                dateString = dateString.trim(),
                bookName = bookName.trim(),
                sabaqDetail = sabaqDetail.trim()
            )
        )
        // Also mirror into legacy sabaq table so old & new queries stay synced
        dao.insertLegacySabaq(
            SabaqEntity(
                studentId = studentId,
                studentName = studentName.trim(),
                bookName = bookName.trim(),
                sabaqDetail = sabaqDetail.trim(),
                dateString = dateString.trim()
            )
        )
    }

    suspend fun deleteSchoolSabaq(entry: SchoolSabaqEntity) = dao.deleteSchoolSabaq(entry)

    // Monthly Reports
    suspend fun addMonthlyReport(
        studentId: Int,
        studentName: String,
        className: String,
        department: String,
        quranTeacherRemark: String,
        principalRemark: String,
        classTeacherRemark: String,
        behaviorWithFellows: String,
        behaviorWithTeachers: String,
        needAttentionIn: String,
        participatedInMilad: Boolean,
        dateString: String
    ): MonthlyReportEntity {
        val entity = MonthlyReportEntity(
            studentId = studentId,
            studentName = studentName.trim(),
            className = className.trim(),
            department = department,
            quranTeacherRemark = quranTeacherRemark.trim(),
            principalRemark = principalRemark.trim(),
            classTeacherRemark = classTeacherRemark.trim(),
            behaviorWithFellows = behaviorWithFellows,
            behaviorWithTeachers = behaviorWithTeachers,
            needAttentionIn = needAttentionIn,
            participatedInMilad = participatedInMilad,
            dateString = dateString
        )
        val newId = dao.insertMonthlyReport(entity).toInt()
        return entity.copy(id = newId)
    }

    suspend fun deleteMonthlyReport(report: MonthlyReportEntity) = dao.deleteMonthlyReport(report)

    companion object {
        fun todayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }
    }
}
