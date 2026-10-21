package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.AlertEntity
import com.example.data.StudentEntity
import com.example.data.TeacherEntity
import com.example.data.UserAccountEntity
import com.example.ui.AcademyUiState
import com.example.ui.BottomNavTab
import com.example.ui.components.AdminAssignTeacherGroupsCard
import com.example.ui.components.AlHadidLogoEmblem
import com.example.ui.components.MonthlyFeeCollectionChartCard
import com.example.ui.components.ParentChildSearchAndLinkCard
import com.example.ui.components.TeacherAssignedGroupsBanner
import com.example.ui.components.V6CreditsFooterCard
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyDarkGreen
import com.example.ui.theme.AcademyEmeraldTop
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGoldBorder
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyGreetingGreen
import com.example.ui.theme.AcademyIconBlue
import com.example.ui.theme.AcademyIconCircleBlueBg
import com.example.ui.theme.AcademyIconCircleCreamBg
import com.example.ui.theme.AcademyIconCircleGreenBg
import com.example.ui.theme.AcademyIconCirclePeachBg
import com.example.ui.theme.AcademyMint
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyProgressTrack
import com.example.ui.theme.AcademyRedAlert
import com.example.ui.theme.AcademySoftGreenText
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.AcademyWarmGoldText
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

@Composable
fun HomeScreen(
    uiState: AcademyUiState,
    onAddTeacher: (name: String, dept: String, qualification: String, phone: String) -> Unit,
    onAddStudent: (
        name: String,
        fatherName: String,
        className: String,
        dept: String,
        teacherName: String,
        monthlyFee: Int,
        phone: String,
        feePaid: Boolean
    ) -> Unit,
    onAddAlert: (title: String, message: String, priority: String) -> Unit,
    onDeleteAlert: (AlertEntity) -> Unit,
    onUpdateRoleAndName: (role: String, name: String) -> Unit,
    onNavigateToTab: (BottomNavTab) -> Unit,
    onDepartmentClick: (String) -> Unit,
    showAlertsDialogExternal: Boolean,
    onDismissAlertsDialogExternal: () -> Unit,
    onOpenStudentProfile: (StudentEntity) -> Unit = {},
    onLinkParentChild: (StudentEntity) -> Unit = {},
    onAssignTeacherGroups: (TeacherEntity?, UserAccountEntity?, String) -> Unit = { _, _, _ -> }
) {
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddAlertDialog by remember { mutableStateOf(false) }
    var showEditGreetingDialog by remember { mutableStateOf(false) }
    var selectedStudentDetail by remember { mutableStateOf<StudentEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome to Al Hadid Academy Card (Matches Screenshot 1)
        item {
            WelcomeAcademyCard()
        }

        // 2. Dynamic Greeting Card (Matches Screenshot 1 + DataStore Role & Name)
        item {
            DynamicGreetingCard(
                role = uiState.userSession.role,
                name = uiState.userSession.name,
                onClickEditRole = { showEditGreetingDialog = true }
            )
        }

        // 2B. V6 Role-Specific Portals (Parent Child Search & Link / Admin Teacher Group Assignment / Teacher Assigned Groups)
        if (uiState.isParent) {
            item {
                ParentChildSearchAndLinkCard(
                    allStudents = uiState.students,
                    linkedChildren = uiState.linkedChildrenForParent,
                    onLinkChild = onLinkParentChild,
                    onOpenStudentProfile = onOpenStudentProfile
                )
            }
        }

        if (uiState.isTeacher) {
            if (!uiState.teacherHasAssignedGroups) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_no_group_assigned_banner"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.5.dp, AcademyGold)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "No Groups Assigned",
                                    tint = AcademyOchre,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Qari Sahab! Aapko abhi tak koi Jamat (Group) assign nahi hui. Admin se rabta karein.",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = AcademyTextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Jab Admin aapko group assign karega, aapke enrolled students aur management tools yahan show honge.",
                                fontSize = 12.sp,
                                color = AcademyTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                item {
                    TeacherAssignedGroupsBanner(
                        teacherName = uiState.userSession.name,
                        assignedGroupsCsv = uiState.effectiveTeacherAssignedGroups
                    )
                }
            }
        }

        if (uiState.isAdmin) {
            item {
                AdminAssignTeacherGroupsCard(
                    teachers = uiState.teachers,
                    users = uiState.users,
                    onAssignGroups = onAssignTeacherGroups
                )
            }
        }

        // 3. Action Buttons Row: + Add Teacher, + Add Student, Approvals (Admin), + Alert
        if (!uiState.isTeacher || uiState.teacherHasAssignedGroups) {
            item {
                ActionButtonsRow(
                    onAddTeacherClick = { showAddTeacherDialog = true },
                    onAddStudentClick = { showAddStudentDialog = true },
                    onAddAlertClick = { showAddAlertDialog = true },
                    onApprovalsClick = if (uiState.isAdmin) {
                        { onNavigateToTab(BottomNavTab.APPROVALS) }
                    } else null
                )
            }
        }

        // Admin Quick Approvals Button
        if (uiState.isAdmin) {
            item {
                Button(
                    onClick = { onNavigateToTab(BottomNavTab.APPROVALS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("admin_home_btn_approvals"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademyGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = "User Approvals",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "User Approvals (Pending & Roles)",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }
            }
        }

        // 3B. V5.0 Quick Access Row: Groups, School Sabaq, Monthly Report (+ Approvals for Admin)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isAdmin) {
                    Button(
                        onClick = { onNavigateToTab(BottomNavTab.APPROVALS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("home_btn_approvals_quick"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcademyBrightGold,
                            contentColor = Color(0xFF1F1905)
                        )
                    ) {
                        Text("Approvals", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, maxLines = 1)
                    }
                }
                if (!uiState.isTeacher || uiState.teacherHasAssignedGroups) {
                    Button(
                        onClick = { onNavigateToTab(BottomNavTab.GROUPS) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("home_btn_groups"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcademyGold,
                            contentColor = Color(0xFF1F1905)
                        )
                    ) {
                        Text("Groups", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, maxLines = 1)
                    }
                }
                Button(
                    onClick = { onNavigateToTab(BottomNavTab.SCHOOL_SABAQ) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("home_btn_school_sabaq"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademyGold,
                        contentColor = Color(0xFF1F1905)
                    )
                ) {
                    Text("School Sabaq", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, maxLines = 1)
                }
                Button(
                    onClick = { onNavigateToTab(BottomNavTab.MONTHLY_REPORT) },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("home_btn_monthly_report"),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademyGold,
                        contentColor = Color(0xFF1F1905)
                    )
                ) {
                    Text("Monthly Report", fontWeight = FontWeight.ExtraBold, fontSize = 11.5.sp, maxLines = 1)
                }
            }
        }

        // 4. Stats Row 1: Total Students & Fee Collected (100% Room DB)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TotalStudentsStatCard(
                    totalStudents = uiState.totalStudents,
                    capacityTarget = uiState.capacityTarget,
                    onClick = { onNavigateToTab(BottomNavTab.STUDENTS) },
                    modifier = Modifier.weight(1f)
                )
                FeeCollectedStatCard(
                    feeCollected = uiState.feeCollected,
                    onClick = { onNavigateToTab(BottomNavTab.FEES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Stats Row 2: Pending Fees & Present Today (Empty state / Room DB)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PendingFeesStatCard(
                    pendingFees = uiState.pendingFees,
                    onClick = { onNavigateToTab(BottomNavTab.FEES) },
                    modifier = Modifier.weight(1f)
                )
                PresentTodayStatCard(
                    presentToday = uiState.presentToday,
                    onClick = { onNavigateToTab(BottomNavTab.ATTENDANCE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5B. Admin Dashboard Monthly Fee Collection Progress vs Targets Chart Card
        item {
            MonthlyFeeCollectionChartCard(
                fees = uiState.fees,
                students = uiState.students,
                feeCollected = uiState.feeCollected,
                pendingFees = uiState.pendingFees,
                onOpenFeesTab = { onNavigateToTab(BottomNavTab.FEES) }
            )
        }

        // 6. Academy Department Enrolment Section (Matches Screenshot 2)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Academy Department Enrolment",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.5.sp,
                    color = AcademyGreen,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DepartmentEnrolmentCard(
                        title = AcademyDepartments.HIFZ,
                        studentCount = uiState.hifzCount,
                        subtitle = uiState.departmentSubtitle(
                            AcademyDepartments.HIFZ,
                            "Qari Sahib"
                        ),
                        onClick = { onDepartmentClick(AcademyDepartments.HIFZ) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dept_card_hifz")
                    )
                    DepartmentEnrolmentCard(
                        title = AcademyDepartments.NAZRA,
                        studentCount = uiState.nazraCount,
                        subtitle = uiState.departmentSubtitle(
                            AcademyDepartments.NAZRA,
                            "Awais & anas mustafa"
                        ),
                        onClick = { onDepartmentClick(AcademyDepartments.NAZRA) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dept_card_nazra")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DepartmentEnrolmentCard(
                        title = AcademyDepartments.TUITION_BOYS,
                        studentCount = uiState.tuitionBoysCount,
                        subtitle = uiState.departmentSubtitle(
                            AcademyDepartments.TUITION_BOYS,
                            "Classes 3,7,8"
                        ),
                        onClick = { onDepartmentClick(AcademyDepartments.TUITION_BOYS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dept_card_tuition_boys")
                    )
                    DepartmentEnrolmentCard(
                        title = AcademyDepartments.GIRLS_PLAYGROUP,
                        studentCount = uiState.girlsPlaygroupCount,
                        subtitle = uiState.departmentSubtitle(
                            AcademyDepartments.GIRLS_PLAYGROUP,
                            "Teacher Isra"
                        ),
                        onClick = { onDepartmentClick(AcademyDepartments.GIRLS_PLAYGROUP) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dept_card_girls_playgroup")
                    )
                }
            }
        }

        // 7. Recent Students Enrolled Card (Matches Screenshot 2)
        item {
            RecentStudentsEnrolledCard(
                recentStudents = uiState.recentStudents,
                onStudentClick = { student ->
                    onOpenStudentProfile(student)
                },
                onAddFirstStudent = { showAddStudentDialog = true }
            )
        }

        // 8. V6 Credits & About Footer ("anas mustafa")
        item {
            V6CreditsFooterCard()
        }
    }

    if (showAddTeacherDialog) {
        AddTeacherDialog(
            onDismiss = { showAddTeacherDialog = false },
            onConfirm = { name, dept, qual, phone ->
                onAddTeacher(name, dept, qual, phone)
                showAddTeacherDialog = false
            }
        )
    }

    if (showAddStudentDialog) {
        AddStudentDialog(
            defaultTeacher = uiState.teachers.firstOrNull()?.name ?: "Qari Sahib",
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, father, cls, dept, teacher, fee, phone, paid ->
                onAddStudent(name, father, cls, dept, teacher, fee, phone, paid)
                showAddStudentDialog = false
            }
        )
    }

    if (showAddAlertDialog) {
        AddAlertDialog(
            onDismiss = { showAddAlertDialog = false },
            onConfirm = { title, message, priority ->
                onAddAlert(title, message, priority)
                showAddAlertDialog = false
            }
        )
    }

    if (showAlertsDialogExternal) {
        AlertsListDialog(
            alerts = uiState.alerts,
            onDismiss = onDismissAlertsDialogExternal,
            onDeleteAlert = onDeleteAlert,
            onAddNewAlert = {
                onDismissAlertsDialogExternal()
                showAddAlertDialog = true
            }
        )
    }

    if (showEditGreetingDialog) {
        EditGreetingRoleDialog(
            currentRole = uiState.userSession.role,
            currentName = uiState.userSession.name,
            onDismiss = { showEditGreetingDialog = false },
            onSave = { newRole, newName ->
                onUpdateRoleAndName(newRole, newName)
                showEditGreetingDialog = false
            }
        )
    }

    selectedStudentDetail?.let { student ->
        AlertDialog(
            onDismissRequest = { selectedStudentDetail = null },
            title = {
                Text(
                    text = student.name,
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AcademyGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Class: ${student.className}", fontWeight = FontWeight.SemiBold)
                    Text("Department: ${student.department}")
                    Text("Teacher: ${student.teacherName}")
                    if (student.fatherName.isNotBlank()) {
                        Text("Father/Guardian: ${student.fatherName}")
                    }
                    Text("Monthly Fee: Rs ${student.monthlyFee}", color = AcademyOchre, fontWeight = FontWeight.Bold)
                    if (student.phone.isNotBlank()) {
                        Text("Contact: ${student.phone}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStudentDetail = null
                        onNavigateToTab(BottomNavTab.STUDENTS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                ) {
                    Text("Open Students Tab")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStudentDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun WelcomeAcademyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_academy_card"),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.2.dp, AcademyGoldBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AcademyEmeraldTop, AcademyDarkGreen)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AlHadidLogoEmblem(
                size = 76.dp,
                outerRingColor = Color(0xFFFFD600)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Welcome to Al Hadid Academy",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.5.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Nasirabad Jatlam, Azad Kashmir",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFF3C64F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Al Hadid Academy is a complete Islamic & Modern Education System. We provide Hifz-ul-Quran, Nazra, Tajweed, Tuition (Class 5-8), Computer Courses and full Fee & Attendance Management. Our mission is to provide quality Deeni & Dunyavi taleem under the supervision of Principal Awais Mustafa and anas mustafa.",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 20.5.sp,
                color = Color(0xFFEAF5EC),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DynamicGreetingCard(
    role: String,
    name: String,
    onClickEditRole: () -> Unit
) {
    val cleanRole = role.trim().ifEmpty { "Teacher" }
    val cleanName = name.trim().ifEmpty { "anas mustafa" }
    val nameParts = cleanName.split(" ").filter { it.isNotBlank() }
    val firstName = nameParts.firstOrNull() ?: cleanName
    val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""
    val fullGreetingForSemantics = "Assalam-o-Alaikum, $cleanRole $cleanName"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickEditRole() }
            .semantics { contentDescription = fullGreetingForSemantics }
            .testTag("dynamic_greeting_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AcademyGreetingGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assalam-o-Alaikum,",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.5.sp,
                        color = AcademyWarmGoldText
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = AcademyBrightGold,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(cleanRole)
                            }
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append(firstName)
                            }
                            if (lastName.isNotEmpty()) {
                                append(" ")
                                withStyle(
                                    SpanStyle(
                                        color = AcademyMint,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) {
                                    append(lastName)
                                }
                            }
                        },
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 23.sp,
                        lineHeight = 28.sp,
                        modifier = Modifier.testTag("greeting_role_name_text")
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AcademyGold
                ) {
                    Text(
                        text = "DUAL ADMIN",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp,
                        color = Color(0xFF1F1905),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Al Hadid Academy • Nasirabad Jatlan, Azad Kashmir",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AcademyBrightGold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Full administrative control over Hifz, Nazra, Tajweed, Tuition & Financials.",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = AcademySoftGreenText
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onAddTeacherClick: () -> Unit,
    onAddStudentClick: () -> Unit,
    onAddAlertClick: () -> Unit,
    onApprovalsClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // + Add Teacher
        Button(
            onClick = onAddTeacherClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_add_teacher"),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = "Add Teacher",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+ Teacher",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // + Add Student
        Button(
            onClick = onAddStudentClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_add_student"),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
        ) {
            Icon(
                imageVector = Icons.Filled.GroupAdd,
                contentDescription = "Add Student",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+ Student",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Approvals (Admin Quick Button)
        if (onApprovalsClick != null) {
            Button(
                onClick = onApprovalsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_action_approvals"),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcademyBrightGold,
                    contentColor = Color(0xFF1F1905)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = "Approvals",
                    tint = Color(0xFF1F1905),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Approvals",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFF1F1905),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // + Alert
        Button(
            onClick = onAddAlertClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("btn_add_alert"),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AcademyOchre)
        ) {
            Icon(
                imageVector = Icons.Filled.Campaign,
                contentDescription = "Add Alert",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+ Alert",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TotalStudentsStatCard(
    totalStudents: Int,
    capacityTarget: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (totalStudents.toFloat() / capacityTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    Card(
        onClick = onClick,
        modifier = modifier.testTag("card_total_students"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Students",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = AcademyTextMuted
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AcademyIconCircleBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "Total Students",
                        tint = AcademyIconBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$totalStudents / $capacityTarget",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = AcademyGreen,
                modifier = Modifier.testTag("val_total_students")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Capacity Track with right-end dot matching Screenshot 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AcademyProgressTrack)
            ) {
                if (progress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceAtLeast(0.08f))
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AcademyGreen)
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AcademyGreen)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Capacity target",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = AcademyOchre
            )
        }
    }
}

@Composable
private fun FeeCollectedStatCard(
    feeCollected: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("card_fee_collected"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fee Collected",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = AcademyTextMuted
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AcademyIconCircleGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    // Green Banknote Badge matching Screenshot 1
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Rs $feeCollected",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = AcademyGreen,
                modifier = Modifier.testTag("val_fee_collected")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "October cycle",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = AcademyOchre
            )
        }
    }
}

@Composable
private fun PendingFeesStatCard(
    pendingFees: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("card_pending_fees"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pending Fees",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = AcademyTextMuted
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AcademyIconCirclePeachBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassBottom,
                        contentDescription = "Pending Fees",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Rs $pendingFees",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = AcademyGreen,
                modifier = Modifier.testTag("val_pending_fees")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Unpaid dues",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = AcademyOchre
            )
        }
    }
}

@Composable
private fun PresentTodayStatCard(
    presentToday: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag("card_present_today"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Present Today",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = AcademyTextMuted
                )
                // Empty state cream circle with subtle checkmark matching Screenshot 2
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(AcademyIconCircleCreamBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Present Today",
                        tint = if (presentToday > 0) AcademyGreen else Color(0xFFD8D4C5),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$presentToday",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = AcademyGreen,
                modifier = Modifier.testTag("val_present_today")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Marked attendance",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = AcademyOchre
            )
        }
    }
}

@Composable
private fun DepartmentEnrolmentCard(
    title: String,
    studentCount: Int,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                color = AcademyGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$studentCount Students",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = AcademyOchre
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = AcademyTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RecentStudentsEnrolledCard(
    recentStudents: List<StudentEntity>,
    onStudentClick: (StudentEntity) -> Unit,
    onAddFirstStudent: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_recent_students"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, Color(0xFFDCD3B8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Green-Gold-Green Accent Bar matching Screenshot 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(AcademyGreen, AcademyGold, AcademyGreen)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "Recent Students Enrolled",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.5.sp,
                    color = AcademyGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (recentStudents.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No students enrolled yet",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = AcademyTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Student' to enroll students into Hifz, Nazra, Tuition or Playgroup.",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddFirstStudent,
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Enroll First Student", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    recentStudents.forEachIndexed { index, student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStudentClick(student) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp,
                                    color = AcademyTextDark
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${student.className} • ${AcademyDepartments.shortLabel(student.department)} • ${student.teacherName}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = AcademyTextMuted
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Student",
                                tint = AcademyGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (index < recentStudents.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.8.dp,
                                color = Color(0xFFF1ECE1)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTeacherDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, dept: String, qualification: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf(AcademyDepartments.HIFZ) }
    var qualification by remember { mutableStateOf("Qari & Islamic Scholar") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Academy Teacher",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Teacher Full Name *") },
                    placeholder = { Text("e.g. Qari Sahib / Teacher Isra") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_teacher_name")
                )

                Text(
                    text = "Department",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademyGreen
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AcademyDepartments.ALL.chunked(2).forEach { rowDepts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowDepts.forEach { dept ->
                                FilterChip(
                                    selected = department == dept,
                                    onClick = { department = dept },
                                    label = { Text(dept, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("Subject / Qualification") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("03700880220") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, department, qualification, phone)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                modifier = Modifier.testTag("confirm_add_teacher")
            ) {
                Text("Save Teacher")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddStudentDialog(
    defaultTeacher: String = "Qari Sahib",
    initialDepartment: String = AcademyDepartments.HIFZ,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        fatherName: String,
        className: String,
        dept: String,
        teacherName: String,
        monthlyFee: Int,
        phone: String,
        feePaid: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("Class 6") }
    var department by remember { mutableStateOf(initialDepartment) }
    var teacherName by remember { mutableStateOf(defaultTeacher) }
    var monthlyFeeText by remember { mutableStateOf("1500") }
    var phone by remember { mutableStateOf("") }
    var feePaidInitially by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enroll New Student",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Student Name *") },
                    placeholder = { Text("e.g. Muhammad Bilal") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_name")
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("Father / Guardian Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class") },
                        placeholder = { Text("Class 6") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = monthlyFeeText,
                        onValueChange = { monthlyFeeText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Fee (Rs)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_student_fee")
                    )
                }

                Text(
                    text = "Academy Department",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademyGreen
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AcademyDepartments.ALL.chunked(2).forEach { rowDepts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowDepts.forEach { dept ->
                                FilterChip(
                                    selected = department == dept,
                                    onClick = {
                                        department = dept
                                        teacherName = when (dept) {
                                            AcademyDepartments.HIFZ -> "Qari Sahib"
                                            AcademyDepartments.NAZRA -> "Principal Awais"
                                            AcademyDepartments.GIRLS_PLAYGROUP -> "Teacher Isra"
                                            else -> "Qari Sahib"
                                        }
                                    },
                                    label = { Text(dept, fontSize = 10.5.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Assigned Teacher") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { feePaidInitially = !feePaidInitially }
                ) {
                    Checkbox(
                        checked = feePaidInitially,
                        onCheckedChange = { feePaidInitially = it }
                    )
                    Text(
                        text = "Mark October fee as already Paid",
                        fontSize = 13.sp,
                        color = AcademyTextDark
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            fatherName,
                            className.ifBlank { "Class 6" },
                            department,
                            teacherName.ifBlank { "Qari Sahib" },
                            monthlyFeeText.toIntOrNull() ?: 1500,
                            phone,
                            feePaidInitially
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                modifier = Modifier.testTag("confirm_add_student")
            ) {
                Text("Enroll Student")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, message: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Important") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Broadcast Academy Alert",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AcademyOchre
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Alert Title *") },
                    placeholder = { Text("e.g. Monthly Tajweed Test Tomorrow") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_alert_title")
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Details") },
                    placeholder = { Text("Enter notice for teachers, parents & students...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_alert_message")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Important", "Fee Reminder", "Exam").forEach { item ->
                        FilterChip(
                            selected = priority == item,
                            onClick = { priority = item },
                            label = { Text(item, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            title,
                            message.ifBlank { "Announcement from Principal Awais Mustafa & anas mustafa." },
                            priority
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyOchre),
                modifier = Modifier.testTag("confirm_add_alert")
            ) {
                Text("Post Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AlertsListDialog(
    alerts: List<AlertEntity>,
    onDismiss: () -> Unit,
    onDeleteAlert: (AlertEntity) -> Unit,
    onAddNewAlert: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Academy Alerts (${alerts.size})",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AcademyGreen
            )
        },
        text = {
            if (alerts.isEmpty()) {
                Text(
                    text = "No active alerts in Room DB. Tap '+ New Alert' to broadcast an announcement.",
                    fontSize = 13.5.sp,
                    color = AcademyTextMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    alerts.take(6).forEach { alert ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFAF7EE),
                            border = BorderStroke(1.dp, AcademyCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${alert.priority}: ${alert.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = AcademyGreen
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = alert.message,
                                        fontSize = 12.sp,
                                        color = AcademyTextDark
                                    )
                                }
                                IconButton(onClick = { onDeleteAlert(alert) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Alert",
                                        tint = AcademyRedAlert
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddNewAlert,
                colors = ButtonDefaults.buttonColors(containerColor = AcademyOchre)
            ) {
                Text("+ New Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun EditGreetingRoleDialog(
    currentRole: String,
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (role: String, name: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(currentRole) }
    var name by remember { mutableStateOf(currentName) }
    val roles = listOf("Teacher", "Student", "Parent", "Admin", "Principal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Greeting Role & Name",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select Role (Saved in DataStore):",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcademyOchre
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.chunked(3).forEach { chunk ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            chunk.forEach { roleItem ->
                                FilterChip(
                                    selected = selectedRole.equals(roleItem, ignoreCase = true),
                                    onClick = { selectedRole = roleItem },
                                    label = { Text(roleItem, fontSize = 11.5.sp) }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedRole, name) },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
            ) {
                Text("Save to DataStore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
