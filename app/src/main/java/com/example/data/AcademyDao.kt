package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {

    // Users (V6 Real Login System: Admin / Teacher / Parent + Assigned Groups)
    @Query("SELECT * FROM users ORDER BY createdAt DESC, id DESC")
    fun getAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE TRIM(phone) = TRIM(:phone) LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity): Long

    @Update
    suspend fun updateUser(user: UserAccountEntity)

    @Delete
    suspend fun deleteUser(user: UserAccountEntity)

    // Students
    @Query("SELECT * FROM students ORDER BY enrolledAt DESC, id DESC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Int): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("UPDATE students SET parentId = :parentId, parentName = :parentName WHERE id = :studentId")
    suspend fun linkParentToStudent(studentId: Int, parentId: Int, parentName: String)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: Int)

    // Legacy Groups table (preserved)
    @Query("SELECT * FROM groups ORDER BY id ASC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    // Legacy Sabaq table (preserved)
    @Query("SELECT * FROM sabaq ORDER BY createdAt DESC, id DESC")
    fun getAllLegacySabaq(): Flow<List<SabaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLegacySabaq(sabaq: SabaqEntity): Long

    // Teachers (Strictly exclude "anas mustafa" from teachers list as requested)
    @Query("SELECT * FROM teachers WHERE LOWER(TRIM(name)) != 'anas mustafa' AND LOWER(name) NOT LIKE '%anas mustafa%' ORDER BY createdAt DESC, id DESC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("DELETE FROM teachers WHERE LOWER(TRIM(name)) = 'anas mustafa' OR LOWER(name) LIKE '%anas mustafa%'")
    suspend fun removeAnasMustafaFromTeachers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherEntity)

    // Alerts
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC, id DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity): Long

    @Delete
    suspend fun deleteAlert(alert: AlertEntity)

    // Fees
    @Query("SELECT * FROM fees ORDER BY isPaid ASC, id DESC")
    fun getAllFees(): Flow<List<FeeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeeRecordEntity): Long

    @Update
    suspend fun updateFee(fee: FeeRecordEntity)

    @Delete
    suspend fun deleteFee(fee: FeeRecordEntity)

    @Query("DELETE FROM fees WHERE studentId = :studentId")
    suspend fun deleteFeesForStudent(studentId: Int)

    // Attendance
    @Query("SELECT * FROM attendance WHERE dateString = :dateString ORDER BY id ASC")
    fun getAttendanceForDate(dateString: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY dateString DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND dateString = :dateString LIMIT 1")
    suspend fun getStudentAttendanceOnDate(studentId: Int, dateString: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAttendanceForStudent(studentId: Int)

    // Books
    @Query("SELECT * FROM books ORDER BY updatedAt DESC, id DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    // Quran Progress
    @Query("SELECT * FROM quran_logs ORDER BY recordedAt DESC, id DESC")
    fun getAllQuranLogs(): Flow<List<QuranProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuranLog(log: QuranProgressEntity): Long

    @Update
    suspend fun updateQuranLog(log: QuranProgressEntity)

    @Delete
    suspend fun deleteQuranLog(log: QuranProgressEntity)

    // Tests
    @Query("SELECT * FROM tests ORDER BY createdAt DESC, id DESC")
    fun getAllTests(): Flow<List<TestRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestRecordEntity): Long

    @Update
    suspend fun updateTest(test: TestRecordEntity)

    @Delete
    suspend fun deleteTest(test: TestRecordEntity)

    // School Books (Urdu, English, Maths, Science, Islamiat, + custom)
    @Query("SELECT * FROM school_books ORDER BY id ASC")
    fun getAllSchoolBooks(): Flow<List<SchoolBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolBook(book: SchoolBookEntity): Long

    @Delete
    suspend fun deleteSchoolBook(book: SchoolBookEntity)

    // School Sabaq (Date + Book Name + Sabaq Detail per Student)
    @Query("SELECT * FROM school_sabaq ORDER BY createdAt DESC, id DESC")
    fun getAllSchoolSabaq(): Flow<List<SchoolSabaqEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolSabaq(entry: SchoolSabaqEntity): Long

    @Delete
    suspend fun deleteSchoolSabaq(entry: SchoolSabaqEntity)

    // Monthly Reports
    @Query("SELECT * FROM monthly_reports ORDER BY createdAt DESC, id DESC")
    fun getAllMonthlyReports(): Flow<List<MonthlyReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyReport(report: MonthlyReportEntity): Long

    @Delete
    suspend fun deleteMonthlyReport(report: MonthlyReportEntity)
}
