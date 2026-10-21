package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AcademyDatabase
import com.example.data.AcademyDepartments
import com.example.data.AcademyRepository
import com.example.data.AlertEntity
import com.example.data.AttendanceEntity
import com.example.data.BookEntity
import com.example.data.FeeRecordEntity
import com.example.data.FirebaseAuthRepository
import com.example.data.FirestoreStudentItem
import com.example.data.FirestoreUserRecord
import com.example.data.MonthlyReportEntity
import com.example.data.QuranProgressEntity
import com.example.data.SchoolBookEntity
import com.example.data.SchoolSabaqEntity
import com.example.data.StudentEntity
import com.example.data.TeacherEntity
import com.example.data.TestRecordEntity
import com.example.data.UserAccountEntity
import com.example.data.UserPreferencesRepository
import com.example.data.UserSession
import com.example.util.ParentNotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BottomNavTab(val label: String, val route: String) {
    HOME("Home", "home"),
    APPROVALS("Approvals", "approvals"),
    GROUPS("Groups", "groups"),
    STUDENTS("Students", "students"),
    FEES("Fees", "fees"),
    ATTENDANCE("Attendance", "attendance"),
    QURAN("Quran", "quran"),
    SCHOOL_SABAQ("School Sabaq", "school_sabaq"),
    BOOKS("Books", "books"),
    TESTS("Tests", "tests"),
    MONTHLY_REPORT("Monthly Report", "monthly_report")
}

data class AcademyUiState(
    val userSession: UserSession = UserSession(),
    val users: List<UserAccountEntity> = emptyList(),
    val students: List<StudentEntity> = emptyList(),
    val teachers: List<TeacherEntity> = emptyList(),
    val alerts: List<AlertEntity> = emptyList(),
    val fees: List<FeeRecordEntity> = emptyList(),
    val attendanceForSelectedDate: List<AttendanceEntity> = emptyList(),
    val allAttendance: List<AttendanceEntity> = emptyList(),
    val selectedDate: String = AcademyRepository.todayDateString(),
    val books: List<BookEntity> = emptyList(),
    val quranLogs: List<QuranProgressEntity> = emptyList(),
    val tests: List<TestRecordEntity> = emptyList(),
    val schoolBooks: List<SchoolBookEntity> = emptyList(),
    val schoolSabaqList: List<SchoolSabaqEntity> = emptyList(),
    val monthlyReports: List<MonthlyReportEntity> = emptyList()
) {
    val isAdmin: Boolean
        get() = userSession.role.trim().equals("Admin", ignoreCase = true) ||
            userSession.role.trim().equals("Principal", ignoreCase = true)

    val isTeacher: Boolean
        get() = userSession.role.equals("Teacher", ignoreCase = true)

    val isParent: Boolean
        get() = userSession.role.equals("Parent", ignoreCase = true)

    val isStudent: Boolean
        get() = userSession.role.equals("Student", ignoreCase = true)

    val teacherHasAssignedGroups: Boolean
        get() = if (isTeacher) {
            val groups = effectiveTeacherAssignedGroups.trim()
            groups.isNotBlank() && groups != "[]" && groups != ""
        } else true

    // Effective groups assigned by Admin to the logged-in Teacher
    val effectiveTeacherAssignedGroups: String
        get() {
            if (isAdmin) return "All"
            val matchedUser = users.firstOrNull {
                (userSession.userId > 0 && it.id == userSession.userId) ||
                    (userSession.email.isNotBlank() && it.email.equals(userSession.email, ignoreCase = true)) ||
                    it.name.equals(userSession.name, ignoreCase = true)
            }
            val matchedTeacher = teachers.firstOrNull {
                it.name.equals(userSession.name, ignoreCase = true)
            }
            val fromDb = matchedUser?.assignedGroups?.ifBlank { matchedTeacher?.assignedGroups ?: "" }
                ?: matchedTeacher?.assignedGroups
                ?: ""
            return fromDb.ifBlank {
                userSession.assignedGroups
            }
        }

    val linkedStudentRecord: StudentEntity?
        get() = if (isStudent) {
            students.firstOrNull {
                (userSession.userId > 0 && it.id == userSession.userId) ||
                    (userSession.phone.isNotBlank() && it.phone == userSession.phone) ||
                    (userSession.name.isNotBlank() && it.name.equals(userSession.name, ignoreCase = true))
            }
        } else null

    // Teacher sees ONLY assigned groups. Parent sees ONLY verified linked children. Student sees ONLY own record.
    // Unknown or unassigned roles receive NO student data.
    val roleFilteredStudents: List<StudentEntity>
        get() = when {
            isAdmin -> students
            isTeacher -> {
                if (!teacherHasAssignedGroups) {
                    emptyList()
                } else {
                    students.filter {
                        AcademyDepartments.isDepartmentAllowedForTeacher(it.department, effectiveTeacherAssignedGroups)
                    }
                }
            }
            isParent -> {
                linkedChildrenForParent // STRICT: returns ONLY linked children; if empty, returns emptyList()! Never falls back to all students.
            }
            isStudent -> {
                linkedStudentRecord?.let { listOf(it) } ?: emptyList()
            }
            else -> emptyList() // Unknown or unauthenticated roles receive ZERO student data
        }

    val linkedChildrenForParent: List<StudentEntity>
        get() = students.filter {
            (userSession.userId > 0 && it.parentId == userSession.userId) ||
                (userSession.name.isNotBlank() && it.parentName.isNotBlank() &&
                    it.parentName.equals(userSession.name, ignoreCase = true) &&
                    !it.parentName.equals("anas mustafa", ignoreCase = true) &&
                    !it.parentName.equals("Respected Parent", ignoreCase = true))
        }

    fun canCurrentUserEditGroup(department: String): Boolean = when {
        isAdmin -> true
        isTeacher -> AcademyDepartments.isDepartmentAllowedForTeacher(department, effectiveTeacherAssignedGroups)
        else -> false // Parents & Students can never edit groups
    }

    val totalStudents: Int get() = students.size
    val capacityTarget: Int get() = 100
    val feeCollected: Int get() = fees.filter { it.isPaid }.sumOf { it.amount }
    val pendingFees: Int get() = fees.filter { !it.isPaid }.sumOf { it.amount }
    val presentToday: Int get() = attendanceForSelectedDate.count { it.status == "PRESENT" }
    val absentToday: Int get() = attendanceForSelectedDate.count { it.status == "ABSENT" }
    val leaveToday: Int get() = attendanceForSelectedDate.count { it.status == "LEAVE" }

    val hifzCount: Int
        get() = students.count {
            AcademyDepartments.matchesSubGroup(it.department, AcademyDepartments.SUB_QURAN_HIFZ)
        }
    val nazraCount: Int
        get() = students.count {
            AcademyDepartments.matchesSubGroup(it.department, AcademyDepartments.SUB_NAZRA)
        }
    val tuitionBoysCount: Int
        get() = students.count {
            AcademyDepartments.matchesSubGroup(it.department, AcademyDepartments.SUB_TUITION_BOYS)
        }
    val girlsPlaygroupCount: Int
        get() = students.count {
            AcademyDepartments.matchesSubGroup(it.department, AcademyDepartments.SUB_PLAYGROUP)
        }

    val availableSchoolBookNames: List<String>
        get() = (AcademyDepartments.DEFAULT_SCHOOL_BOOKS + schoolBooks.map { it.bookName }).distinct()

    fun departmentSubtitle(dept: String, fallback: String): String {
        val deptTeachers = teachers
            .filter { it.department == dept }
            .map { it.name }
            .filterNot { it.equals("anas mustafa", ignoreCase = true) || it.contains("anas mustafa", ignoreCase = true) }
        if (deptTeachers.isNotEmpty()) {
            return deptTeachers.joinToString(" & ")
        }
        return fallback
    }

    val recentStudents: List<StudentEntity> get() = roleFilteredStudents.take(8)
}

@OptIn(ExperimentalCoroutinesApi::class)
class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AcademyDatabase.getDatabase(application)
    val repository = AcademyRepository(database.academyDao())
    val userPrefs = UserPreferencesRepository(application)
    val firebaseAuthRepo = FirebaseAuthRepository(application)

    private val _selectedTab = MutableStateFlow(BottomNavTab.HOME)
    val selectedTab: StateFlow<BottomNavTab> = _selectedTab.asStateFlow()

    private val _selectedDate = MutableStateFlow(AcademyRepository.todayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _departmentFilter = MutableStateFlow("All")
    val departmentFilter: StateFlow<String> = _departmentFilter.asStateFlow()

    // Selected student for Full Detail Screen (when tapping on a student card)
    private val _selectedStudentForProfileId = MutableStateFlow<Int?>(null)
    val selectedStudentForProfileId: StateFlow<Int?> = _selectedStudentForProfileId.asStateFlow()

    val pendingUsers: StateFlow<List<FirestoreUserRecord>> = userPrefs.userSessionFlow
        .flatMapLatest { session ->
            val role = session.role.trim()
            val isAdmin = session.isLoggedIn && (role.equals("Admin", ignoreCase = true) || role.equals("Principal", ignoreCase = true))
            if (isAdmin && firebaseAuthRepo.currentUser != null) {
                firebaseAuthRepo.getPendingUsersFlow()
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<FirestoreUserRecord>> = userPrefs.userSessionFlow
        .flatMapLatest { session ->
            val role = session.role.trim()
            val isAdmin = session.isLoggedIn && (role.equals("Admin", ignoreCase = true) || role.equals("Principal", ignoreCase = true))
            if (isAdmin && firebaseAuthRepo.currentUser != null) {
                firebaseAuthRepo.getAllUsersFlow()
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _firestoreStudents = MutableStateFlow<List<FirestoreStudentItem>>(emptyList())
    val firestoreStudents: StateFlow<List<FirestoreStudentItem>> = _firestoreStudents.asStateFlow()

    private val _isApprovalSaving = MutableStateFlow(false)
    val isApprovalSaving: StateFlow<Boolean> = _isApprovalSaving.asStateFlow()

    private val _approvalStatusMessage = MutableStateFlow<String?>(null)
    val approvalStatusMessage: StateFlow<String?> = _approvalStatusMessage.asStateFlow()

    private val _approvalErrorMessage = MutableStateFlow<String?>(null)
    val approvalErrorMessage: StateFlow<String?> = _approvalErrorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cleanupAnasMustafaTeacher()
            // If user is a logged-in teacher and firebase has a user, listen to assigned groups
            val currentFbUser = firebaseAuthRepo.currentUser
            if (currentFbUser != null) {
                firebaseAuthRepo.listenToAssignedGroups(currentFbUser.uid) { groupsList ->
                    if (groupsList.isNotEmpty()) {
                        val csv = groupsList.joinToString(",")
                        viewModelScope.launch {
                            userPrefs.updateAssignedGroupsInSession(csv)
                        }
                    }
                }
            }
        }
    }

    private val attendanceFlow = _selectedDate.flatMapLatest { date ->
        repository.getAttendanceForDate(date)
    }

    private val coreDataFlow = combine(
        userPrefs.userSessionFlow,
        repository.allStudents,
        repository.allTeachers,
        repository.allAlerts,
        repository.allFees
    ) { session, students, teachers, alerts, fees ->
        CoreDataBundle(session, students, teachers, alerts, fees)
    }

    private val academicDataFlow = combine(
        attendanceFlow,
        _selectedDate,
        repository.allBooks,
        repository.allQuranLogs,
        repository.allTests
    ) { attendance, date, books, quranLogs, tests ->
        AcademicDataBundle(attendance, date, books, quranLogs, tests)
    }

    private val v6DataFlow = combine(
        repository.allSchoolBooks,
        repository.allSchoolSabaq,
        repository.allMonthlyReports,
        repository.allUsers,
        repository.allAttendance
    ) { schoolBooks, schoolSabaq, monthlyReports, users, allAttendance ->
        V6DataBundle(schoolBooks, schoolSabaq, monthlyReports, users, allAttendance)
    }

    val uiState: StateFlow<AcademyUiState> = combine(
        coreDataFlow,
        academicDataFlow,
        v6DataFlow
    ) { core, academic, v6 ->
        AcademyUiState(
            userSession = core.session,
            users = v6.users,
            students = core.students,
            teachers = core.teachers,
            alerts = core.alerts,
            fees = core.fees,
            attendanceForSelectedDate = academic.attendance,
            allAttendance = v6.allAttendance,
            selectedDate = academic.date,
            books = academic.books,
            quranLogs = academic.quranLogs,
            tests = academic.tests,
            schoolBooks = v6.schoolBooks,
            schoolSabaqList = v6.schoolSabaq,
            monthlyReports = v6.monthlyReports
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AcademyUiState()
    )

    fun selectTab(tab: BottomNavTab) {
        _selectedStudentForProfileId.value = null
        _selectedTab.value = tab
    }

    fun openStudentProfile(studentId: Int) {
        _selectedStudentForProfileId.value = studentId
    }

    fun closeStudentProfile() {
        _selectedStudentForProfileId.value = null
    }

    fun selectDepartmentFilter(dept: String) {
        _departmentFilter.value = dept
    }

    fun setAttendanceDate(dateString: String) {
        _selectedDate.value = dateString
    }

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    fun setAuthErrorMessage(message: String) {
        _authErrorMessage.value = message
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    private fun mapFirebaseError(throwable: Throwable): String {
        val e = if (throwable is java.util.concurrent.ExecutionException && throwable.cause != null) {
            throwable.cause!!
        } else {
            throwable
        }

        if (e is SecurityException || e is IllegalArgumentException || e is IllegalStateException) {
            val msg = e.localizedMessage ?: e.message ?: ""
            if (msg.isNotBlank()) return msg
        }

        if (e is com.google.firebase.auth.FirebaseAuthException) {
            val rawCode = e.errorCode
            val code = rawCode.removePrefix("ERROR_").lowercase().replace('_', '-')
            val msg = e.localizedMessage ?: e.message ?: ""
            return if (code.isNotBlank() && msg.isNotBlank()) {
                "[$code] $msg"
            } else if (code.isNotBlank()) {
                code
            } else {
                msg.ifBlank { "Authentication failed" }
            }
        }

        if (e is com.google.firebase.firestore.FirebaseFirestoreException) {
            val code = e.code.name.lowercase().replace('_', '-')
            val msg = e.localizedMessage ?: e.message ?: ""
            return if (msg.isNotBlank()) "[$code] $msg" else code
        }

        if (e is com.google.firebase.FirebaseNetworkException) {
            val msg = e.localizedMessage ?: e.message ?: "Unable to connect to Firebase backend."
            return "[network-request-failed] $msg"
        }

        if (e is com.google.firebase.FirebaseTooManyRequestsException) {
            val msg = e.localizedMessage ?: e.message ?: "Too many requests."
            return "[too-many-requests] $msg"
        }

        if (e is com.google.firebase.FirebaseException) {
            return e.localizedMessage ?: e.message ?: "Firebase error"
        }

        return e.localizedMessage ?: e.message ?: "Authentication error"
    }

    // Online Google Sign-In with CredentialManager + Room DB sync (NO silent fallbacks)
    fun signInWithGoogle(
        idToken: String,
        role: String,
        rememberMe: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (idToken.isBlank()) {
            val err = "Google Sign-In was cancelled or failed."
            _authErrorMessage.value = err
            onError(err)
            return
        }

        viewModelScope.launch {
            try {
                val fbUser = firebaseAuthRepo.signInWithGoogleCredential(idToken, role)

                // Sync with local Room database preserving existing entities
                val savedUser = repository.registerOrAuthenticateUser(
                    name = fbUser.name.ifBlank { fbUser.email.substringBefore("@") },
                    email = fbUser.email,
                    phone = "",
                    rawPassword = "google_authenticated",
                    role = fbUser.role,
                    isSignUp = false
                )

                userPrefs.saveUserSession(
                    userId = savedUser.id,
                    role = fbUser.role,
                    name = savedUser.name,
                    email = savedUser.email,
                    phone = savedUser.phone,
                    assignedGroups = fbUser.assignedGroups.joinToString(","),
                    rememberMe = rememberMe
                )
                _authErrorMessage.value = null
                onSuccess()
            } catch (e: Exception) {
                val userMsg = mapFirebaseError(e)
                _authErrorMessage.value = userMsg
                onError(userMsg)
            }
        }
    }

    // Real Online Firebase Auth + Room sync (NO mock accounts, NO silent fallbacks)
    fun signInOrSignUp(
        role: String,
        name: String,
        email: String = "",
        phone: String = "",
        password: String = "",
        isSignUp: Boolean = false,
        rememberMe: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            val err = "Please enter your Email Address"
            _authErrorMessage.value = err
            onError(err)
            return
        }
        if (password.isBlank()) {
            val err = "Please enter your Password"
            _authErrorMessage.value = err
            onError(err)
            return
        }

        viewModelScope.launch {
            try {
                // All email authentication strictly uses Firebase Auth
                val fbUser = if (isSignUp) {
                    firebaseAuthRepo.signUpWithEmail(cleanEmail, password, role, name)
                } else {
                    firebaseAuthRepo.signInWithEmail(cleanEmail, password, role)
                }

                val verifiedRole = fbUser.role

                // Sync with local Room DB using firebase_authenticated marker to prevent local password mismatch
                val savedUser = repository.registerOrAuthenticateUser(
                    name = fbUser.name.ifBlank { name.ifBlank { cleanEmail.substringBefore("@") } },
                    email = cleanEmail,
                    phone = phone.trim(),
                    rawPassword = "firebase_authenticated",
                    role = verifiedRole,
                    isSignUp = false
                )

                userPrefs.saveUserSession(
                    userId = savedUser.id,
                    role = verifiedRole,
                    name = savedUser.name,
                    email = savedUser.email,
                    phone = savedUser.phone,
                    assignedGroups = fbUser.assignedGroups.joinToString(","),
                    rememberMe = rememberMe
                )
                _authErrorMessage.value = null
                onSuccess()
            } catch (e: Exception) {
                val userMsg = mapFirebaseError(e)
                _authErrorMessage.value = userMsg
                onError(userMsg)
            }
        }
    }

    fun updateProfileRoleAndName(role: String, name: String) {
        viewModelScope.launch {
            userPrefs.updateRoleAndName(role = role, name = name)
        }
    }

    fun logout() {
        viewModelScope.launch {
            firebaseAuthRepo.signOut()
            userPrefs.logout()
        }
    }

    fun clearSessionOnAppClose() {
        userPrefs.clearSessionOnAppCloseSync()
        if (!userPrefs.isRememberMeEnabled()) {
            firebaseAuthRepo.signOut()
            viewModelScope.launch {
                userPrefs.logout()
            }
        }
    }

    sealed class StartupAuthResult {
        object OpenHome : StartupAuthResult()
        object OpenLogin : StartupAuthResult()
        data class PendingApproval(val message: String) : StartupAuthResult()
        data class Error(val message: String) : StartupAuthResult()
    }

    fun checkStartupAuth(onResult: (StartupAuthResult) -> Unit) {
        viewModelScope.launch {
            try {
                val fbUser = firebaseAuthRepo.checkCurrentUserSession()
                if (fbUser == null) {
                    userPrefs.logout()
                    onResult(StartupAuthResult.OpenLogin)
                } else {
                    val savedUser = repository.registerOrAuthenticateUser(
                        name = fbUser.name.ifBlank { fbUser.email.substringBefore("@") },
                        email = fbUser.email,
                        phone = "",
                        rawPassword = "firebase_authenticated",
                        role = fbUser.role,
                        isSignUp = false
                    )
                    userPrefs.saveUserSession(
                        userId = savedUser.id,
                        role = fbUser.role,
                        name = savedUser.name,
                        email = savedUser.email,
                        phone = savedUser.phone,
                        assignedGroups = fbUser.assignedGroups.joinToString(","),
                        rememberMe = userPrefs.isRememberMeEnabled()
                    )
                    _authErrorMessage.value = null
                    onResult(StartupAuthResult.OpenHome)
                }
            } catch (e: SecurityException) {
                userPrefs.logout()
                val msg = e.message ?: "Your account is waiting for admin approval"
                _authErrorMessage.value = msg
                onResult(StartupAuthResult.PendingApproval(msg))
            } catch (e: Exception) {
                userPrefs.logout()
                val userMsg = mapFirebaseError(e)
                _authErrorMessage.value = userMsg
                onResult(StartupAuthResult.Error(userMsg))
            }
        }
    }

    fun loadFirestoreStudents() {
        viewModelScope.launch {
            val localStudents = uiState.value.students
            val isAdmin = uiState.value.isAdmin
            val hasFbUser = firebaseAuthRepo.currentUser != null

            if (isAdmin && hasFbUser) {
                if (localStudents.isNotEmpty()) {
                    firebaseAuthRepo.syncLocalStudentsToFirestore(localStudents)
                }
                val remoteStudents = firebaseAuthRepo.fetchStudentsFromFirestore()
                if (remoteStudents.isNotEmpty()) {
                    _firestoreStudents.value = remoteStudents
                    return@launch
                }
            }

            if (localStudents.isNotEmpty()) {
                _firestoreStudents.value = localStudents.map { st ->
                    FirestoreStudentItem(
                        id = st.id.toString(),
                        name = st.name,
                        className = st.className,
                        department = st.department,
                        fatherName = st.fatherName
                    )
                }
            }
        }
    }

    fun approveOrUpdateUser(
        targetUid: String,
        newRole: String,
        groups: List<String>,
        studentIds: List<String>,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isApprovalSaving.value = true
            _approvalErrorMessage.value = null
            _approvalStatusMessage.value = null

            val result = firebaseAuthRepo.updateUserApproval(
                targetUid = targetUid,
                newRole = newRole,
                groups = groups,
                studentIds = studentIds
            )

            _isApprovalSaving.value = false
            result.onSuccess {
                _approvalStatusMessage.value = "Approved successfully"
                onComplete?.invoke(true)
            }.onFailure { error ->
                _approvalErrorMessage.value = error.message ?: "Failed to update user."
                onComplete?.invoke(false)
            }
        }
    }

    fun clearApprovalMessages() {
        _approvalStatusMessage.value = null
        _approvalErrorMessage.value = null
    }

    // Admin assigns groups to Teacher (updates Room DB + Firestore)
    fun assignGroupsToTeacher(
        teacher: TeacherEntity?,
        userAccount: UserAccountEntity?,
        assignedGroupsCsv: String
    ) {
        viewModelScope.launch {
            repository.updateTeacherAssignedGroups(teacher, userAccount, assignedGroupsCsv)
            val groupsList = AcademyDepartments.parseAssignedGroups(assignedGroupsCsv)
            if (userAccount != null && userAccount.email.isNotBlank()) {
                firebaseAuthRepo.updateTeacherAssignedGroupsInFirestore(userAccount.email, groupsList)
            }
            val currentSession = uiState.value.userSession
            if (currentSession.role.equals("Teacher", ignoreCase = true) &&
                ((teacher != null && teacher.name.equals(currentSession.name, ignoreCase = true)) ||
                    (userAccount != null && userAccount.id == currentSession.userId))
            ) {
                userPrefs.updateAssignedGroupsInSession(assignedGroupsCsv)
            }
        }
    }

    // Parent links child ("Add kar lain - Pakka Pakka")
    fun linkParentToStudent(student: StudentEntity) {
        val session = uiState.value.userSession
        val parentId = if (session.userId > 0) session.userId else 1
        val parentName = session.name.ifBlank { "Respected Parent" }
        viewModelScope.launch {
            repository.linkParentToStudent(
                studentId = student.id,
                parentId = parentId,
                parentName = parentName
            )
            repository.addAlert(
                title = "${student.name} Linked to Parent ($parentName)",
                message = "Pakka Pakka! You will now receive instant notifications whenever a teacher adds Sabaq or Marks for ${student.name}.",
                priority = "Important"
            )
        }
    }

    private fun notifyParentIfStudentUpdated(studentId: Int, studentName: String, detailTitle: String, detailBody: String) {
        val appContext = getApplication<Application>()
        ParentNotificationHelper.sendParentChildUpdateNotification(
            context = appContext,
            studentName = studentName,
            title = detailTitle,
            message = detailBody
        )
        viewModelScope.launch {
            repository.addAlert(
                title = detailTitle,
                message = detailBody,
                priority = "Parent Update"
            )
        }
    }

    // Teachers
    fun addTeacher(
        name: String,
        department: String,
        qualification: String,
        phone: String,
        assignedGroups: String = department
    ) {
        if (name.isBlank()) return
        if (name.trim().equals("anas mustafa", ignoreCase = true) || name.contains("anas mustafa", ignoreCase = true)) {
            return
        }
        viewModelScope.launch {
            repository.addTeacher(name, department, qualification, phone, assignedGroups)
        }
    }

    fun deleteTeacher(teacher: TeacherEntity) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
        }
    }

    // Students
    fun addStudent(
        name: String,
        fatherName: String,
        className: String,
        department: String,
        teacherName: String,
        monthlyFee: Int,
        phone: String,
        feePaidInitially: Boolean = false
    ) {
        if (name.isBlank()) return
        if (!uiState.value.canCurrentUserEditGroup(department)) return
        viewModelScope.launch {
            repository.addStudentWithInitialFee(
                name = name,
                fatherName = fatherName,
                className = className.ifBlank { "Class 5" },
                department = department,
                teacherName = teacherName.ifBlank { "Qari Sahib" },
                monthlyFee = monthlyFee,
                phone = phone,
                feePaidInitially = feePaidInitially
            )
        }
    }

    fun updateStudent(student: StudentEntity) {
        if (!uiState.value.canCurrentUserEditGroup(student.department)) return
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: StudentEntity) {
        if (!uiState.value.canCurrentUserEditGroup(student.department)) return
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    // Alerts
    fun addAlert(title: String, message: String, priority: String = "Important") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addAlert(title, message, priority)
        }
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            repository.deleteAlert(alert)
        }
    }

    // Fees
    fun addFeeVoucher(student: StudentEntity, amount: Int, monthCycle: String, isPaid: Boolean) {
        viewModelScope.launch {
            repository.addFeeRecord(student, amount, monthCycle, isPaid)
        }
    }

    fun toggleFeeStatus(fee: FeeRecordEntity) {
        viewModelScope.launch {
            repository.toggleFeePaidStatus(fee)
        }
    }

    fun deleteFee(fee: FeeRecordEntity) {
        viewModelScope.launch {
            repository.deleteFee(fee)
        }
    }

    // Attendance
    fun markAttendance(student: StudentEntity, status: String) {
        if (!uiState.value.canCurrentUserEditGroup(student.department)) return
        viewModelScope.launch {
            repository.markAttendance(student, _selectedDate.value, status)
        }
    }

    fun markAllPresent() {
        val currentStudents = uiState.value.roleFilteredStudents
        if (currentStudents.isEmpty()) return
        viewModelScope.launch {
            repository.markAllPresent(currentStudents, _selectedDate.value)
        }
    }

    // Books
    fun addBook(
        title: String,
        category: String,
        authorOrNotes: String,
        totalPages: Int,
        completedPages: Int = 0,
        assignedStudentId: Int? = null,
        assignedStudentName: String = ""
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addBook(
                title = title,
                category = category,
                authorOrNotes = authorOrNotes,
                totalPages = totalPages,
                completedPages = completedPages,
                assignedStudentId = assignedStudentId,
                assignedStudentName = assignedStudentName
            )
        }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch {
            repository.updateBook(book)
        }
    }

    fun assignBookToStudent(book: BookEntity, studentId: Int?, studentName: String) {
        viewModelScope.launch {
            repository.assignBookToStudent(book, studentId, studentName)
        }
    }

    fun updateBookProgress(book: BookEntity, completedPages: Int) {
        viewModelScope.launch {
            repository.updateBookProgress(book, completedPages)
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    // Quran Sabaq (Triggers Parent Notification: "<Child> ka naya Sabaq add hua hai")
    fun addQuranLog(
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
        if (studentName.isBlank()) return
        viewModelScope.launch {
            repository.addQuranLog(
                studentId = studentId,
                studentName = studentName,
                department = department,
                paraNumber = paraNumber,
                surahName = surahName,
                sabakDetail = sabakDetail,
                sabqiDetail = sabqiDetail,
                manzilDetail = manzilDetail,
                quality = quality,
                teacherName = teacherName
            )
            notifyParentIfStudentUpdated(
                studentId = studentId,
                studentName = studentName.trim(),
                detailTitle = "Walidain ke liye ittila: ${studentName.trim()} ka naya result add hua",
                detailBody = "Quran Sabaq: Para $paraNumber ($surahName) • Sabak: $sabakDetail • Quality: $quality"
            )
        }
    }

    fun deleteQuranLog(log: QuranProgressEntity) {
        viewModelScope.launch {
            repository.deleteQuranLog(log)
        }
    }

    // Tests (Separate 100 + 100 Marks: Quran Pak / Nazra = 100, School = 100 + Parent Notification)
    fun addTestRecord(
        title: String,
        subject: String,
        department: String,
        studentId: Int,
        studentName: String,
        obtainedMarks: Int,
        totalMarks: Int,
        remarks: String,
        quranMarks: Int = obtainedMarks,
        quranTotalMarks: Int = 100,
        schoolMarks: Int = obtainedMarks,
        schoolTotalMarks: Int = 100
    ) {
        if (title.isBlank() || studentName.isBlank()) return
        viewModelScope.launch {
            repository.addTestRecord(
                title = title,
                subject = subject,
                department = department,
                studentId = studentId,
                studentName = studentName,
                obtainedMarks = quranMarks,
                totalMarks = quranTotalMarks,
                remarks = remarks,
                dateString = _selectedDate.value,
                quranMarks = quranMarks,
                quranTotalMarks = quranTotalMarks,
                schoolMarks = schoolMarks,
                schoolTotalMarks = schoolTotalMarks
            )
            notifyParentIfStudentUpdated(
                studentId = studentId,
                studentName = studentName.trim(),
                detailTitle = "Walidain ke liye ittila: ${studentName.trim()} ka naya result add hua",
                detailBody = "$title: Quran Pak / Nazra = $quranMarks/$quranTotalMarks • School = $schoolMarks/$schoolTotalMarks"
            )
        }
    }

    fun deleteTestRecord(test: TestRecordEntity) {
        viewModelScope.launch {
            repository.deleteTestRecord(test)
        }
    }

    // School Sabaq & School Books (Triggers Parent Notification)
    fun addSchoolBookName(bookName: String) {
        if (bookName.isBlank()) return
        viewModelScope.launch {
            repository.addSchoolBookName(bookName)
        }
    }

    fun addSchoolSabaq(
        studentId: Int,
        studentName: String,
        className: String,
        dateString: String,
        bookName: String,
        sabaqDetail: String
    ) {
        if (studentName.isBlank() || bookName.isBlank() || sabaqDetail.isBlank()) return
        viewModelScope.launch {
            repository.addSchoolSabaq(
                studentId = studentId,
                studentName = studentName,
                className = className,
                dateString = dateString,
                bookName = bookName,
                sabaqDetail = sabaqDetail
            )
            notifyParentIfStudentUpdated(
                studentId = studentId,
                studentName = studentName.trim(),
                detailTitle = "Walidain ke liye ittila: ${studentName.trim()} ka naya result add hua",
                detailBody = "School Sabaq ($bookName - $dateString): $sabaqDetail"
            )
        }
    }

    fun deleteSchoolSabaq(entry: SchoolSabaqEntity) {
        viewModelScope.launch {
            repository.deleteSchoolSabaq(entry)
        }
    }

    // Monthly Reports
    fun saveMonthlyReport(
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
        dateString: String,
        onSaved: (MonthlyReportEntity) -> Unit = {}
    ) {
        if (studentName.isBlank()) return
        viewModelScope.launch {
            val saved = repository.addMonthlyReport(
                studentId = studentId,
                studentName = studentName,
                className = className,
                department = department,
                quranTeacherRemark = quranTeacherRemark,
                principalRemark = principalRemark,
                classTeacherRemark = classTeacherRemark,
                behaviorWithFellows = behaviorWithFellows,
                behaviorWithTeachers = behaviorWithTeachers,
                needAttentionIn = needAttentionIn,
                participatedInMilad = participatedInMilad,
                dateString = dateString
            )
            onSaved(saved)
        }
    }

    fun deleteMonthlyReport(report: MonthlyReportEntity) {
        viewModelScope.launch {
            repository.deleteMonthlyReport(report)
        }
    }
}

private data class CoreDataBundle(
    val session: UserSession,
    val students: List<StudentEntity>,
    val teachers: List<TeacherEntity>,
    val alerts: List<AlertEntity>,
    val fees: List<FeeRecordEntity>
)

private data class AcademicDataBundle(
    val attendance: List<AttendanceEntity>,
    val date: String,
    val books: List<BookEntity>,
    val quranLogs: List<QuranProgressEntity>,
    val tests: List<TestRecordEntity>
)

private data class V6DataBundle(
    val schoolBooks: List<SchoolBookEntity>,
    val schoolSabaq: List<SchoolSabaqEntity>,
    val monthlyReports: List<MonthlyReportEntity>,
    val users: List<UserAccountEntity>,
    val allAttendance: List<AttendanceEntity>
)
