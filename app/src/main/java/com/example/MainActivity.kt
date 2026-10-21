package com.example

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.data.GoogleSignInResult
import com.example.data.UserPreferencesRepository
import com.example.ui.AcademyViewModel
import com.example.ui.BottomNavTab
import com.example.ui.components.AlHadidBottomNavigationBar
import com.example.ui.components.AlHadidTopBar
import com.example.ui.components.IslamicGeometricBackground
import com.example.ui.screens.ApprovalsScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BooksScreen
import com.example.ui.screens.FeesScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MonthlyReportScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SchoolSabaqScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudentProfileDetailScreen
import com.example.ui.screens.StudentsScreen
import com.example.ui.screens.TestsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppStartupScreen {
    SPLASH,
    WELCOME,
    LOGIN,
    HOME
}

class MainActivity : ComponentActivity() {
    private var isColdStart: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isColdStart = (savedInstanceState == null)
        val userPrefs = UserPreferencesRepository(applicationContext)
        if (!userPrefs.isRememberMeEnabled()) {
            userPrefs.clearSessionOnAppCloseSync()
            runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
        }
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AlHadidAcademyApp(isColdStart = isColdStart)
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            val userPrefs = UserPreferencesRepository(applicationContext)
            if (!userPrefs.isRememberMeEnabled()) {
                userPrefs.clearSessionOnAppCloseSync()
                runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
            }
        }
        super.onDestroy()
    }
}

@Composable
fun AlHadidAcademyApp(
    viewModel: AcademyViewModel = viewModel(),
    isColdStart: Boolean = true
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val departmentFilter by viewModel.departmentFilter.collectAsStateWithLifecycle()
    val selectedStudentProfileId by viewModel.selectedStudentForProfileId.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val pendingUsers by viewModel.pendingUsers.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
    val firestoreStudents by viewModel.firestoreStudents.collectAsStateWithLifecycle()
    val isApprovalSaving by viewModel.isApprovalSaving.collectAsStateWithLifecycle()
    val approvalStatusMessage by viewModel.approvalStatusMessage.collectAsStateWithLifecycle()
    val approvalErrorMessage by viewModel.approvalErrorMessage.collectAsStateWithLifecycle()

    var currentScreen by rememberSaveable { mutableStateOf(AppStartupScreen.SPLASH) }
    var isCheckingAuth by remember { mutableStateOf(false) }
    var showAlertsModal by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (isColdStart) {
            currentScreen = AppStartupScreen.SPLASH
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(uiState.userSession.isLoggedIn) {
        if (uiState.userSession.isLoggedIn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPerm) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Step 1: Splash screen (Bismillah, Al Hadid Academy logo, Nasirabad Jatlan, "Developed by anas mustafa")
    // Show it for about 2.5 seconds, then go automatically to step 2.
    if (currentScreen == AppStartupScreen.SPLASH) {
        BackHandler {
            (context as? Activity)?.finish()
        }
        SplashScreen(
            onFinished = {
                currentScreen = AppStartupScreen.WELCOME
            }
        )
        return
    }

    // Step 2: "Ahlian-e-Jatlan ke liye Khushkhabri" info screen.
    // Wait for the user to tap "Agay Barhein (Login / Signup)". Do not skip it automatically.
    if (currentScreen == AppStartupScreen.WELCOME) {
        BackHandler {
            (context as? Activity)?.finish()
        }
        WelcomeScreen(
            isLoading = isCheckingAuth,
            onContinueToAuth = {
                isCheckingAuth = true
                viewModel.checkStartupAuth { result ->
                    isCheckingAuth = false
                    when (result) {
                        is AcademyViewModel.StartupAuthResult.OpenHome -> {
                            currentScreen = AppStartupScreen.HOME
                        }
                        is AcademyViewModel.StartupAuthResult.PendingApproval -> {
                            currentScreen = AppStartupScreen.LOGIN
                        }
                        is AcademyViewModel.StartupAuthResult.OpenLogin -> {
                            currentScreen = AppStartupScreen.LOGIN
                        }
                        is AcademyViewModel.StartupAuthResult.Error -> {
                            currentScreen = AppStartupScreen.LOGIN
                        }
                    }
                }
            }
        )
        return
    }

    // Step 3: Real Working Login / Signup Screen (Google, Phone OTP 123456, Email + Password, Role Selection)
    if (currentScreen == AppStartupScreen.LOGIN) {
        BackHandler {
            currentScreen = AppStartupScreen.WELCOME
        }
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        AuthScreen(
            registeredUsers = uiState.users,
            authErrorMessage = authErrorMessage,
            onClearAuthError = { viewModel.clearAuthError() },
            isFirebaseConfigured = viewModel.firebaseAuthRepo.hasValidFirebaseApiKey(),
            onGoogleSignIn = { activity, role, rememberMe ->
                coroutineScope.launch {
                    when (val result = viewModel.firebaseAuthRepo.launchGoogleSignIn(activity)) {
                        is GoogleSignInResult.Success -> {
                            viewModel.signInWithGoogle(
                                idToken = result.idToken,
                                role = role,
                                rememberMe = rememberMe,
                                onSuccess = {
                                    currentScreen = AppStartupScreen.HOME
                                }
                            )
                        }
                        is GoogleSignInResult.Error -> {
                            viewModel.setAuthErrorMessage(result.message)
                        }
                        is GoogleSignInResult.Cancelled -> {
                            // User cancelled account selection
                        }
                    }
                }
            },
            onAuthenticated = { role, name, email, phone, password, isSignUp, rememberMe ->
                viewModel.signInOrSignUp(
                    role = role,
                    name = name,
                    email = email,
                    phone = phone,
                    password = password,
                    isSignUp = isSignUp,
                    rememberMe = rememberMe,
                    onSuccess = {
                        currentScreen = AppStartupScreen.HOME
                    }
                )
            }
        )
        return
    }

    // Back handling on Home dashboard:
    // 1. If viewing a student's Full Profile Detail Screen, close profile
    // 2. If on a secondary tab, return to HOME
    // 3. If on HOME tab and user presses Back to close app, finish activity (closes app, does NOT return to login)
    if (selectedStudentProfileId != null) {
        BackHandler {
            viewModel.closeStudentProfile()
        }
    } else if (selectedTab != BottomNavTab.HOME) {
        BackHandler {
            viewModel.selectTab(BottomNavTab.HOME)
        }
    } else {
        BackHandler {
            val userPrefs = UserPreferencesRepository(context)
            if (!userPrefs.isRememberMeEnabled()) {
                viewModel.clearSessionOnAppClose()
                runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().signOut() }
            }
            (context as? Activity)?.finish()
        }
    }

    val activeProfileStudent = remember(uiState.students, selectedStudentProfileId) {
        selectedStudentProfileId?.let { id ->
            uiState.students.firstOrNull { it.id == id }
        }
    }

    val visibleStudentsForRole = uiState.roleFilteredStudents

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            AlHadidTopBar(
                alertCount = uiState.alerts.size,
                onAlertClick = { showAlertsModal = true },
                onLogoutClick = {
                    viewModel.logout()
                    currentScreen = AppStartupScreen.LOGIN
                }
            )
        },
        bottomBar = {
            AlHadidBottomNavigationBar(
                selectedTab = selectedTab,
                canShowTab = { tab ->
                    if (tab == BottomNavTab.APPROVALS) {
                        return@AlHadidBottomNavigationBar uiState.isAdmin
                    }
                    if (uiState.isTeacher && !uiState.teacherHasAssignedGroups) {
                        when (tab) {
                            BottomNavTab.FEES,
                            BottomNavTab.ATTENDANCE,
                            BottomNavTab.QURAN,
                            BottomNavTab.SCHOOL_SABAQ,
                            BottomNavTab.GROUPS -> false
                            else -> true
                        }
                    } else {
                        when (tab) {
                            BottomNavTab.FEES -> uiState.isAdmin || (uiState.isTeacher && uiState.teacherHasAssignedGroups) || uiState.isParent
                            else -> true
                        }
                    }
                },
                onSelectTab = { tab ->
                    if (tab == BottomNavTab.STUDENTS && selectedTab != BottomNavTab.STUDENTS) {
                        viewModel.selectDepartmentFilter("All")
                    }
                    viewModel.selectTab(tab)
                }
            )
        }
    ) { innerPadding ->
        IslamicGeometricBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (activeProfileStudent != null) {
                    StudentProfileDetailScreen(
                        student = activeProfileStudent,
                        schoolSabaqList = uiState.schoolSabaqList,
                        quranLogs = uiState.quranLogs,
                        testRecords = uiState.tests,
                        attendanceRecords = uiState.allAttendance,
                        feeRecords = uiState.fees,
                        monthlyReports = uiState.monthlyReports,
                        isParentRole = uiState.isParent,
                        onLinkParentChild = { student ->
                            viewModel.linkParentToStudent(student)
                        },
                        onEditStudent = { updatedStudent ->
                            viewModel.updateStudent(updatedStudent)
                        },
                        onBack = { viewModel.closeStudentProfile() }
                    )
                } else {
                    when (selectedTab) {
                        BottomNavTab.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                onAddTeacher = { name, dept, qual, phone ->
                                    viewModel.addTeacher(name, dept, qual, phone)
                                },
                                onAddStudent = { name, father, cls, dept, teacher, fee, phone, paid ->
                                    viewModel.addStudent(name, father, cls, dept, teacher, fee, phone, paid)
                                },
                                onAddAlert = { title, message, priority ->
                                    viewModel.addAlert(title, message, priority)
                                },
                                onDeleteAlert = { alert ->
                                    viewModel.deleteAlert(alert)
                                },
                                onUpdateRoleAndName = { role, name ->
                                    viewModel.updateProfileRoleAndName(role, name)
                                },
                                onNavigateToTab = { tab ->
                                    viewModel.selectTab(tab)
                                },
                                onDepartmentClick = { dept ->
                                    viewModel.selectDepartmentFilter(dept)
                                    viewModel.selectTab(BottomNavTab.STUDENTS)
                                },
                                showAlertsDialogExternal = showAlertsModal,
                                onDismissAlertsDialogExternal = { showAlertsModal = false },
                                onOpenStudentProfile = { student ->
                                    viewModel.openStudentProfile(student.id)
                                },
                                onLinkParentChild = { student ->
                                    viewModel.linkParentToStudent(student)
                                },
                                onAssignTeacherGroups = { teacher, userAcc, groupsCsv ->
                                    viewModel.assignGroupsToTeacher(teacher, userAcc, groupsCsv)
                                }
                            )
                        }

                        BottomNavTab.GROUPS -> {
                            GroupsScreen(
                                students = visibleStudentsForRole,
                                onStudentClick = { student ->
                                    viewModel.openStudentProfile(student.id)
                                },
                                onAddStudentToGroup = { name, father, cls, dept, teacher, fee, phone, paid ->
                                    viewModel.addStudent(name, father, cls, dept, teacher, fee, phone, paid)
                                }
                            )
                        }

                        BottomNavTab.STUDENTS -> {
                            StudentsScreen(
                                students = visibleStudentsForRole,
                                teachers = uiState.teachers,
                                selectedDepartmentFilter = departmentFilter,
                                onSelectDepartmentFilter = { viewModel.selectDepartmentFilter(it) },
                                onAddStudent = { name, father, cls, dept, teacher, fee, phone, paid ->
                                    viewModel.addStudent(name, father, cls, dept, teacher, fee, phone, paid)
                                },
                                onDeleteStudent = { viewModel.deleteStudent(it) },
                                onAddTeacher = { name, dept, qual, phone ->
                                    viewModel.addTeacher(name, dept, qual, phone)
                                },
                                onDeleteTeacher = { viewModel.deleteTeacher(it) },
                                onStudentClick = { student ->
                                    viewModel.openStudentProfile(student.id)
                                },
                                isParentRole = uiState.isParent,
                                onLinkParentChild = { student ->
                                    viewModel.linkParentToStudent(student)
                                },
                                isTeacherRole = uiState.isTeacher,
                                teacherHasAssignedGroups = uiState.teacherHasAssignedGroups
                            )
                        }

                        BottomNavTab.FEES -> {
                            FeesScreen(
                                fees = uiState.fees,
                                students = visibleStudentsForRole,
                                feeCollected = uiState.feeCollected,
                                pendingFees = uiState.pendingFees,
                                onAddFeeVoucher = { student, amount, cycle, paid ->
                                    viewModel.addFeeVoucher(student, amount, cycle, paid)
                                },
                                onAddStudentAndFee = { name, className, dept, amount, paid ->
                                    viewModel.addStudent(
                                        name = name,
                                        fatherName = "",
                                        className = className,
                                        department = dept,
                                        teacherName = "Qari Sahib",
                                        monthlyFee = amount,
                                        phone = "",
                                        feePaidInitially = paid
                                    )
                                },
                                onToggleFeePaid = { viewModel.toggleFeeStatus(it) },
                                onDeleteFee = { viewModel.deleteFee(it) }
                            )
                        }

                        BottomNavTab.ATTENDANCE -> {
                            AttendanceScreen(
                                students = visibleStudentsForRole,
                                attendanceList = uiState.attendanceForSelectedDate,
                                selectedDate = uiState.selectedDate,
                                presentCount = uiState.presentToday,
                                absentCount = uiState.absentToday,
                                leaveCount = uiState.leaveToday,
                                onMarkAttendance = { student, status ->
                                    viewModel.markAttendance(student, status)
                                },
                                onMarkAllPresent = { viewModel.markAllPresent() },
                                onAddStudentQuick = { viewModel.selectTab(BottomNavTab.STUDENTS) }
                            )
                        }

                        BottomNavTab.QURAN -> {
                            QuranScreen(
                                quranLogs = uiState.quranLogs,
                                students = visibleStudentsForRole,
                                onAddQuranLog = { sId, sName, dept, para, surah, sabak, sabqi, manzil, qual, tName ->
                                    viewModel.addQuranLog(
                                        studentId = sId,
                                        studentName = sName,
                                        department = dept,
                                        paraNumber = para,
                                        surahName = surah,
                                        sabakDetail = sabak,
                                        sabqiDetail = sabqi,
                                        manzilDetail = manzil,
                                        quality = qual,
                                        teacherName = tName
                                    )
                                },
                                onDeleteQuranLog = { viewModel.deleteQuranLog(it) }
                            )
                        }

                        BottomNavTab.SCHOOL_SABAQ -> {
                            SchoolSabaqScreen(
                                students = visibleStudentsForRole,
                                bookNames = uiState.availableSchoolBookNames,
                                sabaqEntries = uiState.schoolSabaqList,
                                defaultDate = uiState.selectedDate,
                                onAddNewBookName = { bookName ->
                                    viewModel.addSchoolBookName(bookName)
                                },
                                onAddSchoolSabaq = { sId, sName, cls, date, bookName, detail ->
                                    viewModel.addSchoolSabaq(
                                        studentId = sId,
                                        studentName = sName,
                                        className = cls,
                                        dateString = date,
                                        bookName = bookName,
                                        sabaqDetail = detail
                                    )
                                },
                                onDeleteSchoolSabaq = { viewModel.deleteSchoolSabaq(it) }
                            )
                        }

                        BottomNavTab.BOOKS -> {
                            BooksScreen(
                                books = uiState.books,
                                students = visibleStudentsForRole,
                                onAddBook = { title, cat, notes, total, comp, stId, stName ->
                                    viewModel.addBook(title, cat, notes, total, comp, stId, stName)
                                },
                                onUpdateBook = { viewModel.updateBook(it) },
                                onAssignBook = { book, stId, stName ->
                                    viewModel.assignBookToStudent(book, stId, stName)
                                },
                                onUpdateBookProgress = { book, completedPages ->
                                    viewModel.updateBookProgress(book, completedPages)
                                },
                                onDeleteBook = { viewModel.deleteBook(it) }
                            )
                        }

                        BottomNavTab.TESTS -> {
                            TestsScreen(
                                tests = uiState.tests,
                                students = visibleStudentsForRole,
                                onAddTest = { title, subj, dept, sId, sName, obt, tot, rem, qMarks, qTot, sMarks, sTot ->
                                    viewModel.addTestRecord(
                                        title = title,
                                        subject = subj,
                                        department = dept,
                                        studentId = sId,
                                        studentName = sName,
                                        obtainedMarks = obt,
                                        totalMarks = tot,
                                        remarks = rem,
                                        quranMarks = qMarks,
                                        quranTotalMarks = qTot,
                                        schoolMarks = sMarks,
                                        schoolTotalMarks = sTot
                                    )
                                },
                                onDeleteTest = { viewModel.deleteTestRecord(it) }
                            )
                        }

                        BottomNavTab.MONTHLY_REPORT -> {
                            MonthlyReportScreen(
                                students = visibleStudentsForRole,
                                reports = uiState.monthlyReports,
                                defaultDate = uiState.selectedDate,
                                onSaveAndGenerateReport = { sId, sName, cls, dept, qRem, pRem, cRem, bFel, bTeach, att, milad, date, onSaved ->
                                    viewModel.saveMonthlyReport(
                                        studentId = sId,
                                        studentName = sName,
                                        className = cls,
                                        department = dept,
                                        quranTeacherRemark = qRem,
                                        principalRemark = pRem,
                                        classTeacherRemark = cRem,
                                        behaviorWithFellows = bFel,
                                        behaviorWithTeachers = bTeach,
                                        needAttentionIn = att,
                                        participatedInMilad = milad,
                                        dateString = date,
                                        onSaved = onSaved
                                    )
                                },
                                onDeleteReport = { viewModel.deleteMonthlyReport(it) }
                            )
                        }

                        BottomNavTab.APPROVALS -> {
                            ApprovalsScreen(
                                pendingUsers = pendingUsers,
                                allUsers = allUsers,
                                students = firestoreStudents,
                                isSaving = isApprovalSaving,
                                statusMessage = approvalStatusMessage,
                                errorMessage = approvalErrorMessage,
                                onSaveUser = { targetUid, newRole, groups, studentIds ->
                                    viewModel.approveOrUpdateUser(targetUid, newRole, groups, studentIds)
                                },
                                onDismissMessages = { viewModel.clearApprovalMessages() },
                                onRefreshStudents = { viewModel.loadFirestoreStudents() }
                            )
                        }
                    }
                }
            }
        }
    }
}
