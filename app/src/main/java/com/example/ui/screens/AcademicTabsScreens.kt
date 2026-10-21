package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.AttendanceEntity
import com.example.data.QuranProgressEntity
import com.example.data.StudentEntity
import com.example.data.TeacherEntity
import com.example.data.TestRecordEntity
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyRedAlert
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

@Composable
fun StudentsScreen(
    students: List<StudentEntity>,
    teachers: List<TeacherEntity>,
    selectedDepartmentFilter: String,
    onSelectDepartmentFilter: (String) -> Unit,
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
    onDeleteStudent: (StudentEntity) -> Unit,
    onAddTeacher: (name: String, dept: String, qualification: String, phone: String) -> Unit,
    onDeleteTeacher: (TeacherEntity) -> Unit,
    onStudentClick: (StudentEntity) -> Unit = {},
    isParentRole: Boolean = false,
    onLinkParentChild: (StudentEntity) -> Unit = {},
    isTeacherRole: Boolean = false,
    teacherHasAssignedGroups: Boolean = true
) {
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = remember(students, selectedDepartmentFilter, searchQuery) {
        val byDept = if (selectedDepartmentFilter == "All") {
            students
        } else {
            students.filter { it.department == selectedDepartmentFilter }
        }
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            byDept
        } else {
            byDept.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.className.contains(q, ignoreCase = true) ||
                    it.department.contains(q, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("students_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isTeacherRole && !teacherHasAssignedGroups) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_no_group_assigned_center_card"),
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
                            text = "Admin ne abhi tak aapko koi group assign nahi kiya. Admin se rabta karein.",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = AcademyTextDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jab Admin aapko groups assign karega, aapke students aur enroll button yahan unlock ho jayenge.",
                            fontSize = 12.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AcademyGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Students & Teachers Directory",
                                fontFamily = MerriweatherFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.5.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${students.size} Enrolled Students • ${teachers.size} Teachers",
                                fontSize = 12.5.sp,
                                color = AcademyBrightGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (!isTeacherRole || teacherHasAssignedGroups) {
                            Button(
                                onClick = { showAddStudentDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AcademyGold,
                                    contentColor = Color(0xFF1F1905)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("students_tab_add_student_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = "Enroll Student",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Enroll", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar ("Apna bacha ka naam likhein") + Department Filter Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Apna bacha ka naam likhein (Search Student)") },
                placeholder = { Text("e.g. Haider...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("students_search_bar")
            )
        }

        // Department Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (listOf("All") + AcademyDepartments.ALL).forEach { dept ->
                    FilterChip(
                        selected = selectedDepartmentFilter == dept,
                        onClick = { onSelectDepartmentFilter(dept) },
                        label = { Text(dept, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Teachers Summary Strip (Ensure anas mustafa is never displayed as teacher)
        val visibleTeachers = teachers.filterNot {
            it.name.trim().equals("anas mustafa", ignoreCase = true) ||
                it.name.contains("anas mustafa", ignoreCase = true)
        }
        if (visibleTeachers.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Academy Teachers (${visibleTeachers.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = AcademyGreen
                            )
                            TextButton(onClick = { showAddTeacherDialog = true }) {
                                Text("+ Add Teacher", fontSize = 12.sp, color = AcademyOchre)
                            }
                        }
                        visibleTeachers.forEach { teacher ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = teacher.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AcademyTextDark
                                    )
                                    Text(
                                        text = "${teacher.department} • ${teacher.qualification}",
                                        fontSize = 12.sp,
                                        color = AcademyTextMuted
                                    )
                                }
                                IconButton(onClick = { onDeleteTeacher(teacher) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Remove Teacher",
                                        tint = AcademyRedAlert,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD8EAE2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Groups,
                                contentDescription = "No Students",
                                tint = AcademyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Students in $selectedDepartmentFilter",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enroll students into Hifz Group, Nazra & Tajweed, Tuition Boys, or Girls & Playgroup.",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showAddStudentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                        ) {
                            Text("+ Enroll Student")
                        }
                    }
                }
            }
        } else {
            items(filteredStudents, key = { it.id }) { student ->
                Card(
                    onClick = { onStudentClick(student) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_card_${student.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, AcademyCardBorder)
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.5.sp,
                                    color = AcademyTextDark
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${student.className} • ${AcademyDepartments.shortLabel(student.department)} • ${student.teacherName}",
                                    fontSize = 13.sp,
                                    color = AcademyTextMuted
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Monthly Fee: Rs ${student.monthlyFee}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AcademyOchre
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEAF5EC)
                                    ) {
                                        Text(
                                            text = "Tap for Full Profile →",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AcademyGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            if (!isParentRole) {
                                IconButton(onClick = { onDeleteStudent(student) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete Student",
                                        tint = AcademyRedAlert
                                    )
                                }
                            }
                        }

                        if (isParentRole) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onLinkParentChild(student) },
                                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_card_pakka_pakka_${student.id}")
                            ) {
                                Text(
                                    text = if (student.parentId > 0) {
                                        "Linked (${student.parentName}) ✓"
                                    } else {
                                        "Add kar lain - Pakka Pakka"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        val defaultDept = if (selectedDepartmentFilter in AcademyDepartments.ALL) {
            selectedDepartmentFilter
        } else {
            AcademyDepartments.HIFZ
        }
        AddStudentDialog(
            defaultTeacher = teachers.firstOrNull { !it.name.contains("anas mustafa", ignoreCase = true) }?.name ?: "Qari Sahib",
            initialDepartment = defaultDept,
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, father, cls, dept, teacher, fee, phone, paid ->
                onAddStudent(name, father, cls, dept, teacher, fee, phone, paid)
                showAddStudentDialog = false
            }
        )
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
}

@Composable
fun AttendanceScreen(
    students: List<StudentEntity>,
    attendanceList: List<AttendanceEntity>,
    selectedDate: String,
    presentCount: Int,
    absentCount: Int,
    leaveCount: Int,
    onMarkAttendance: (StudentEntity, String) -> Unit,
    onMarkAllPresent: () -> Unit,
    onAddStudentQuick: () -> Unit
) {
    val statusByStudentId = remember(attendanceList) {
        attendanceList.associateBy({ it.studentId }, { it.status })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("attendance_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AcademyGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Attendance Register",
                                fontFamily = MerriweatherFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.5.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Date: $selectedDate • Updates Home 'Present Today' Live",
                                fontSize = 12.sp,
                                color = Color(0xFFDFF5E3)
                            )
                        }
                        if (students.isNotEmpty()) {
                            Button(
                                onClick = onMarkAllPresent,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AcademyGold,
                                    contentColor = Color(0xFF1F1905)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_mark_all_present")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Mark All Present",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("All Present", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AttendanceStatPill("Present", presentCount, AcademyBrightGold, Modifier.weight(1f))
                        AttendanceStatPill("Absent", absentCount, Color(0xFFFCA5A5), Modifier.weight(1f))
                        AttendanceStatPill("Leave", leaveCount, Color(0xFFFDE68A), Modifier.weight(1f))
                    }
                }
            }
        }

        if (students.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "No Attendance",
                            tint = AcademyGreen,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Students Enrolled for Attendance",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enroll students first to mark daily Present, Absent, or Leave status.",
                            fontSize = 13.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddStudentQuick,
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                        ) {
                            Text("Go to Students & Enroll")
                        }
                    }
                }
            }
        } else {
            items(students, key = { it.id }) { student ->
                val currentStatus = statusByStudentId[student.id] ?: "UNMARKED"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.5.sp,
                                    color = AcademyTextDark
                                )
                                Text(
                                    text = "${student.className} • ${student.department}",
                                    fontSize = 12.sp,
                                    color = AcademyTextMuted
                                )
                            }
                            Text(
                                text = currentStatus,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when (currentStatus) {
                                    "PRESENT" -> AcademyGreen
                                    "ABSENT" -> AcademyRedAlert
                                    "LEAVE" -> AcademyOchre
                                    else -> AcademyTextMuted
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("PRESENT", "Present", AcademyGreen),
                                Triple("ABSENT", "Absent", AcademyRedAlert),
                                Triple("LEAVE", "Leave", AcademyOchre)
                            ).forEach { (code, label, activeColor) ->
                                val selected = currentStatus == code
                                if (selected) {
                                    Button(
                                        onClick = { onMarkAttendance(student, code) },
                                        colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("att_${code.lowercase()}_${student.id}")
                                    ) {
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onMarkAttendance(student, code) },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, activeColor),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("att_${code.lowercase()}_${student.id}")
                                    ) {
                                        Text(label, fontSize = 12.sp, color = activeColor, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatPill(
    label: String,
    count: Int,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0E4D1F)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = valueColor
            )
            Text(text = label, fontSize = 11.sp, color = Color.White)
        }
    }
}

@Composable
fun QuranScreen(
    quranLogs: List<QuranProgressEntity>,
    students: List<StudentEntity>,
    onAddQuranLog: (
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
    ) -> Unit,
    onDeleteQuranLog: (QuranProgressEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AcademyGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hifz, Nazra & Tajweed",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Sabak, Sabqi & Manzil tracker",
                            fontSize = 12.5.sp,
                            color = Color(0xFFDFF5E3)
                        )
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcademyGold,
                            contentColor = Color(0xFF1F1905)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Log Lesson")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Sabak", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        if (quranLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Quran Progress",
                            tint = AcademyGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Quran Sabak Logs Yet",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record daily Hifz & Nazra Sabak, Sabqi, and Manzil for students.",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(quranLogs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${log.studentName} • Para ${log.paraNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AcademyGreen
                                )
                                Text(
                                    text = "Surah: ${log.surahName} (${log.quality})",
                                    fontSize = 12.5.sp,
                                    color = AcademyOchre,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = { onDeleteQuranLog(log) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = AcademyRedAlert
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Sabak: ${log.sabakDetail}", fontSize = 13.sp, color = AcademyTextDark)
                        Text("Sabqi: ${log.sabqiDetail} • Manzil: ${log.manzilDetail}", fontSize = 12.5.sp, color = AcademyTextMuted)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var studentName by remember { mutableStateOf(students.firstOrNull()?.name ?: "") }
        var paraText by remember { mutableStateOf("30") }
        var surahName by remember { mutableStateOf("Surah Al-Hadid") }
        var sabak by remember { mutableStateOf("Ruku 1 - 12 Ayat with Tajweed") }
        var sabqi by remember { mutableStateOf("Last 2 Pages") }
        var manzil by remember { mutableStateOf("Para 29 Complete") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Record Quran Sabak",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AcademyGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = paraText,
                            onValueChange = { paraText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Para (1-30)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = surahName,
                            onValueChange = { surahName = it },
                            label = { Text("Surah") },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                    OutlinedTextField(
                        value = sabak,
                        onValueChange = { sabak = it },
                        label = { Text("Sabak (New Lesson)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sabqi,
                        onValueChange = { sabqi = it },
                        label = { Text("Sabqi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manzil,
                        onValueChange = { manzil = it },
                        label = { Text("Manzil") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentName.isNotBlank()) {
                            onAddQuranLog(
                                0,
                                studentName,
                                AcademyDepartments.HIFZ,
                                paraText.toIntOrNull() ?: 30,
                                surahName,
                                sabak,
                                sabqi,
                                manzil,
                                "Excellent",
                                "Qari Sahib"
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                ) {
                    Text("Save Lesson")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TestsScreen(
    tests: List<TestRecordEntity>,
    students: List<StudentEntity>,
    onAddTest: (
        title: String,
        subject: String,
        department: String,
        studentId: Int,
        studentName: String,
        obtainedMarks: Int,
        totalMarks: Int,
        remarks: String,
        quranMarks: Int,
        quranTotalMarks: Int,
        schoolMarks: Int,
        schoolTotalMarks: Int
    ) -> Unit,
    onDeleteTest: (TestRecordEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tests_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AcademyGreen)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Academy Tests (100 + 100 System)",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.5.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Separate Marks: Quran Pak / Nazra (100) & School (100)",
                            fontSize = 12.sp,
                            color = AcademyBrightGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcademyGold,
                            contentColor = Color(0xFF1F1905)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_open_add_test_dialog")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Test")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Test", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                    }
                }
            }
        }

        if (tests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = "Tests",
                            tint = AcademyGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Test Records Added Yet",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Record separate student marks for Quran Pak / Nazra (out of 100) and School (out of 100).",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(tests, key = { it.id }) { test ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_record_card_${test.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = test.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AcademyGreen
                                )
                                Text(
                                    text = "${test.studentName} • ${test.subject} (${test.dateString})",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = AcademyTextDark
                                )
                            }
                            IconButton(onClick = { onDeleteTest(test) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Test",
                                    tint = AcademyRedAlert
                                )
                            }
                        }

                        // Separate Row 1: Quran Assessment (out of 100)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEAF5EC),
                            border = BorderStroke(1.dp, AcademyGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quran Assessment (Quran Pak / Nazra)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = AcademyGreen
                                )
                                Text(
                                    text = "${test.quranMarks} / ${test.quranTotalMarks} (${test.quranPercentage}%)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = AcademyGreen
                                )
                            }
                        }

                        // Separate Row 2: School Assessment (out of 100)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFAF7EE),
                            border = BorderStroke(1.dp, AcademyGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "School Assessment (Subjects)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = AcademyOchre
                                )
                                Text(
                                    text = "${test.schoolMarks} / ${test.schoolTotalMarks} (${test.schoolPercentage}%)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = AcademyOchre
                                )
                            }
                        }

                        if (test.remarks.isNotBlank()) {
                            Text(
                                text = "Remarks: ${test.remarks}",
                                fontSize = 12.sp,
                                color = AcademyTextMuted
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("Monthly Assessment") }
        var subject by remember { mutableStateOf("Quran & School") }
        var studentName by remember { mutableStateOf(students.firstOrNull()?.name ?: "") }
        var quranMarksText by remember { mutableStateOf("92") }
        var schoolMarksText by remember { mutableStateOf("88") }
        var remarksText by remember { mutableStateOf("Excellent performance") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Separate 100 + 100 Assessment",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AcademyGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Assessment Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("Student Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quranMarksText,
                        onValueChange = { quranMarksText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Quran Pak / Nazra Marks (Out of 100)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_quran_marks_100")
                    )
                    OutlinedTextField(
                        value = schoolMarksText,
                        onValueChange = { schoolMarksText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("School Marks (Out of 100)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_school_marks_100")
                    )
                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Teacher Remarks") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentName.isNotBlank()) {
                            val matchedStudent = students.firstOrNull {
                                it.name.equals(studentName.trim(), ignoreCase = true)
                            }
                            val qMarks = (quranMarksText.toIntOrNull() ?: 92).coerceIn(0, 100)
                            val sMarks = (schoolMarksText.toIntOrNull() ?: 88).coerceIn(0, 100)
                            onAddTest(
                                title,
                                subject,
                                matchedStudent?.department ?: AcademyDepartments.HIFZ,
                                matchedStudent?.id ?: 0,
                                studentName.trim(),
                                qMarks,
                                100,
                                remarksText.ifBlank { "Good effort" },
                                qMarks,
                                100,
                                sMarks,
                                100
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                ) {
                    Text("Save 100+100 Result")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
