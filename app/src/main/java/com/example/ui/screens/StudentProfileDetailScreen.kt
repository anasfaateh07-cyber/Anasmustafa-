package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.AttendanceEntity
import com.example.data.FeeRecordEntity
import com.example.data.MonthlyReportEntity
import com.example.data.QuranProgressEntity
import com.example.data.SchoolSabaqEntity
import com.example.data.StudentEntity
import com.example.data.TestRecordEntity
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyDarkGreen
import com.example.ui.theme.AcademyEmeraldTop
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGoldBorder
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyProgressTrack
import com.example.ui.theme.AcademyRedAlert
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

@Composable
fun StudentProfileDetailScreen(
    student: StudentEntity,
    schoolSabaqList: List<SchoolSabaqEntity>,
    quranLogs: List<QuranProgressEntity>,
    testRecords: List<TestRecordEntity>,
    attendanceRecords: List<AttendanceEntity>,
    feeRecords: List<FeeRecordEntity>,
    monthlyReports: List<MonthlyReportEntity>,
    isParentRole: Boolean = false,
    onLinkParentChild: (StudentEntity) -> Unit = {},
    onEditStudent: (StudentEntity) -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    // 4 Profile Tabs:
    // 0 = 1) Sabaq (Kahan se chal raha hai: Quran Sabaq + School Sabaq)
    // 1 = 2) Parhai (Quran me kaisa hai + School me kaisa hai)
    // 2 = 3) Results (2 columns: Quran Pak/Nazra out of 100 AND School out of 100 separately)
    // 3 = 4) Attendance & Fees
    var selectedProfileTab by rememberSaveable { mutableIntStateOf(0) }
    var showEditStudentDialog by remember { mutableStateOf(false) }

    val profileTabs = listOf(
        "1) Sabaq",
        "2) Parhai",
        "3) Results",
        "4) Attendance & Fees"
    )

    val studentSchoolSabaq = schoolSabaqList.filter {
        it.studentId == student.id || it.studentName.equals(student.name, ignoreCase = true)
    }
    val studentQuranLogs = quranLogs.filter {
        it.studentId == student.id || it.studentName.equals(student.name, ignoreCase = true)
    }
    val studentTests = testRecords.filter {
        it.studentId == student.id || it.studentName.equals(student.name, ignoreCase = true)
    }
    val studentAttendance = attendanceRecords.filter {
        it.studentId == student.id || it.studentName.equals(student.name, ignoreCase = true)
    }
    val studentFees = feeRecords.filter {
        it.studentId == student.id || it.studentName.equals(student.name, ignoreCase = true)
    }
    val studentReports = monthlyReports.filter {
        it.studentId == student.id || it.studentName.equals(student.name, ignoreCase = true)
    }

    val latestQuran = studentQuranLogs.firstOrNull()
    val latestSchoolSabaq = studentSchoolSabaq.firstOrNull()
    val last3QuranLogs = studentQuranLogs.take(3)
    val latestReport = studentReports.firstOrNull()

    val presentDays = studentAttendance.count { it.status == "PRESENT" }
    val absentDays = studentAttendance.count { it.status == "ABSENT" }
    val leaveDays = studentAttendance.count { it.status == "LEAVE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_profile_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card: Name, Class 6, Tuition, Qari Sahib, Fee Rs 800 + Button Edit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, AcademyGoldBorder),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(AcademyEmeraldTop, AcademyGreen, AcademyDarkGreen)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x26FFFFFF))
                                    .testTag("btn_back_from_student_profile")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Students",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = student.name,
                                    fontFamily = MerriweatherFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${student.className} • ${AcademyDepartments.shortLabel(student.department)} • ${student.teacherName.ifBlank { "Qari Sahib" }} • Fee Rs ${student.monthlyFee}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademyBrightGold
                                )
                            }
                        }

                        // Button Edit
                        Button(
                            onClick = { showEditStudentDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AcademyBrightGold,
                                contentColor = Color(0xFF1F1905)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_edit_student_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Student",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Student Core Info Badges Row: Name, Class, Group (Tuition/Hifz), Teacher Name (Qari Sahib), Fee Rs 800
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x24FFFFFF),
                        border = BorderStroke(1.dp, Color(0x4DFACC15)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileMetaItem(
                                    label = "Student Name",
                                    value = student.name,
                                    modifier = Modifier.weight(1f)
                                )
                                ProfileMetaItem(
                                    label = "Class",
                                    value = student.className,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = Color(0x33FFFFFF))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileMetaItem(
                                    label = "Group / Department",
                                    value = "${AcademyDepartments.shortLabel(student.department)} (${student.department})",
                                    modifier = Modifier.weight(1f)
                                )
                                ProfileMetaItem(
                                    label = "Teacher Name",
                                    value = student.teacherName.ifBlank { "Qari Sahib" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = Color(0x33FFFFFF))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileMetaItem(
                                    label = "Fee",
                                    value = "Fee Rs ${student.monthlyFee}",
                                    modifier = Modifier.weight(1f)
                                )
                                ProfileMetaItem(
                                    label = "Parent / Linked",
                                    value = student.parentName.ifBlank {
                                        student.fatherName.ifBlank { "Not Linked Yet" }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (isParentRole || student.parentId == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onLinkParentChild(student) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AcademyBrightGold,
                                contentColor = Color(0xFF1F1905)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_profile_link_parent")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FamilyRestroom,
                                contentDescription = "Link Child",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (student.parentId > 0) {
                                    "Linked to Parent (${student.parentName}) ✓"
                                } else {
                                    "Add kar lain - Pakka Pakka (Link Child)"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. 4 Tabs Selector Bar: 1) Sabaq | 2) Parhai | 3) Results | 4) Attendance & Fees
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0A3917),
                border = BorderStroke(1.2.dp, AcademyBrightGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_profile_tabs_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    profileTabs.forEachIndexed { index, tabLabel ->
                        val isSelected = selectedProfileTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) AcademyBrightGold else Color.Transparent)
                                .clickable { selectedProfileTab = index }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("profile_tab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabLabel,
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFF1F1905) else Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: Sabaq - Kahan se chal raha hai (Quran Sabaq + School Sabaq)
        if (selectedProfileTab == 0) {
            item {
                SectionDetailCard(
                    title = "1) Sabaq - Kahan Se Chal Raha Hai",
                    subtitle = "Current Quran Sabaq + Current School Sabaq",
                    icon = Icons.AutoMirrored.Filled.MenuBook
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEAF5EC),
                        border = BorderStroke(1.dp, AcademyGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Quran Sabaq (Kahan se chal raha hai)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = AcademyGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (latestQuran != null) {
                                Text(
                                    text = "Para ${latestQuran.paraNumber} • ${latestQuran.surahName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = AcademyTextDark
                                )
                                Text(
                                    text = "Sabak: ${latestQuran.sabakDetail} | Sabqi: ${latestQuran.sabqiDetail} | Manzil: ${latestQuran.manzilDetail}",
                                    fontSize = 12.5.sp,
                                    color = AcademyTextMuted
                                )
                            } else {
                                Text(
                                    text = "Para 30 (Surah An-Naba) • Daily Sabak, Sabqi & Manzil Active",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = AcademyTextDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAF7EE),
                        border = BorderStroke(1.dp, AcademyGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "School Sabaq (Kahan se chal raha hai)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = AcademyOchre
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (latestSchoolSabaq != null) {
                                Text(
                                    text = "${latestSchoolSabaq.bookName} (${latestSchoolSabaq.dateString})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = AcademyTextDark
                                )
                                Text(
                                    text = latestSchoolSabaq.sabaqDetail,
                                    fontSize = 12.5.sp,
                                    color = AcademyTextMuted
                                )
                            } else {
                                Text(
                                    text = "English, Urdu, Maths & Science • Daily School Tuition Sabaq Active",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = AcademyTextDark
                                )
                            }

                            if (studentSchoolSabaq.size > 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = AcademyCardBorder)
                                Spacer(modifier = Modifier.height(6.dp))
                                studentSchoolSabaq.drop(1).take(4).forEach { entry ->
                                    Text(
                                        text = "• ${entry.bookName} (${entry.dateString}): ${entry.sabaqDetail}",
                                        fontSize = 12.sp,
                                        color = AcademyTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: Parhai - Quran me kaisa hai + School me kaisa hai
        if (selectedProfileTab == 1 || selectedProfileTab == 0) {
            item {
                SectionDetailCard(
                    title = "2) Parhai - Quran Me Kaisa Hai + School Me Kaisa Hai",
                    subtitle = "Last 3 Quran Sabaq + Quran & School Parhai Remarks",
                    icon = Icons.Filled.AutoStories
                ) {
                    val overallQuality = latestQuran?.quality ?: "Excellent"
                    val quranRemarkText = latestReport?.quranTeacherRemark
                        ?: student.quranStatus

                    val schoolRemarkText = latestReport?.classTeacherRemark
                        ?: student.schoolStatus

                    // Quran me kaisa hai
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEAF5EC),
                        border = BorderStroke(1.dp, AcademyGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quran Me Kaisa Hai",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = AcademyGreen
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AcademyGreen
                                ) {
                                    Text(
                                        text = overallQuality,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = quranRemarkText,
                                fontSize = 13.sp,
                                color = AcademyTextDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // School me kaisa hai
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFAF7EE),
                        border = BorderStroke(1.dp, AcademyGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "School Me Kaisa Hai",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = AcademyOchre
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = schoolRemarkText,
                                fontSize = 13.sp,
                                color = AcademyTextDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (last3QuranLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Last 3 Quran Sabaq Logs:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = AcademyGreen
                        )
                        last3QuranLogs.forEachIndexed { index, log ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFAF7EE),
                                border = BorderStroke(1.dp, AcademyCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "#${index + 1} • Para ${log.paraNumber} (${log.surahName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = AcademyGreen
                                        )
                                        Text(
                                            text = log.quality,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = AcademyOchre
                                        )
                                    }
                                    Text(
                                        text = "Sabak: ${log.sabakDetail} • Sabqi: ${log.sabqiDetail} • Manzil: ${log.manzilDetail}",
                                        fontSize = 12.sp,
                                        color = AcademyTextDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: Results - Show Test Results like screenshot 2 with 2 columns:
        // Quran Pak/Nazra out of 100 AND School out of 100 separately
        if (selectedProfileTab == 2 || selectedProfileTab == 0) {
            item {
                val latestTest = studentTests.firstOrNull()
                val quranScore = latestTest?.quranMarks ?: 92
                val quranTotal = latestTest?.quranTotalMarks ?: 100
                val quranPct = if (quranTotal > 0) (quranScore * 100) / quranTotal else 92

                val schoolScore = latestTest?.schoolMarks ?: 88
                val schoolTotal = latestTest?.schoolTotalMarks ?: 100
                val schoolPct = if (schoolTotal > 0) (schoolScore * 100) / schoolTotal else 88

                SectionDetailCard(
                    title = "3) Results - 100 + 100 Separate Marks",
                    subtitle = latestTest?.let { "${it.title} • ${it.dateString}" } ?: "Monthly Assessment (2 Columns: Quran /100 & School /100)",
                    icon = Icons.Filled.AssignmentTurnedIn
                ) {
                    // 2 Columns Side-by-Side: Column 1 = Quran Pak/Nazra out of 100 | Column 2 = School out of 100
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TwoColumnScoreCard(
                            heading = "Quran Pak / Nazra",
                            marks = quranScore,
                            total = quranTotal,
                            percentage = quranPct,
                            accentColor = AcademyGreen,
                            bgColor = Color(0xFFEAF5EC),
                            modifier = Modifier.weight(1f)
                        )
                        TwoColumnScoreCard(
                            heading = "School Tuition",
                            marks = schoolScore,
                            total = schoolTotal,
                            percentage = schoolPct,
                            accentColor = AcademyOchre,
                            bgColor = Color(0xFFFAF7EE),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (studentTests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = AcademyCardBorder)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All Test Results (${studentTests.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        studentTests.forEach { test ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, AcademyCardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${test.title} (${test.subject})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = AcademyTextDark
                                        )
                                        Text(
                                            text = test.dateString,
                                            fontSize = 11.sp,
                                            color = AcademyTextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFEAF5EC),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Quran: ${test.quranMarks}/${test.quranTotalMarks} (${test.quranPercentage}%)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AcademyGreen,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFAF7EE),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "School: ${test.schoolMarks}/${test.schoolTotalMarks} (${test.schoolPercentage}%)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = AcademyOchre,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 4: Attendance & Fees
        if (selectedProfileTab == 3 || selectedProfileTab == 0) {
            item {
                SectionDetailCard(
                    title = "4) Attendance & Fees",
                    subtitle = "Attendance summary, monthly fee & behavior record",
                    icon = Icons.Filled.EventAvailable
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiniStatBox(
                            label = "Present",
                            value = "$presentDays Days",
                            color = AcademyGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatBox(
                            label = "Absent",
                            value = "$absentDays Days",
                            color = AcademyRedAlert,
                            modifier = Modifier.weight(1f)
                        )
                        MiniStatBox(
                            label = "Leave",
                            value = "$leaveDays Days",
                            color = AcademyOchre,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Fees History • Monthly Fee Rs ${student.monthlyFee}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = AcademyGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (studentFees.isEmpty()) {
                        Text(
                            text = "Monthly Fee: Rs ${student.monthlyFee} • No fee vouchers generated yet.",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted
                        )
                    } else {
                        studentFees.forEach { fee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${fee.monthCycle} • Rs ${fee.amount}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AcademyTextDark
                                    )
                                    Text(
                                        text = "Receipt: ${fee.receiptNumber.ifBlank { "AHA-${1000 + fee.id}" }}",
                                        fontSize = 11.5.sp,
                                        color = AcademyTextMuted
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (fee.isPaid) Color(0xFFEAF5EC) else Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = if (fee.isPaid) "PAID" else "PENDING",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = if (fee.isPaid) AcademyGreen else AcademyRedAlert,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditStudentDialog) {
        var editName by remember { mutableStateOf(student.name) }
        var editClass by remember { mutableStateOf(student.className) }
        var editDept by remember { mutableStateOf(student.department) }
        var editTeacher by remember { mutableStateOf(student.teacherName) }
        var editFee by remember { mutableStateOf(student.monthlyFee.toString()) }
        var editQuranStatus by remember { mutableStateOf(student.quranStatus) }
        var editSchoolStatus by remember { mutableStateOf(student.schoolStatus) }

        AlertDialog(
            onDismissRequest = { showEditStudentDialog = false },
            title = {
                Text(
                    text = "Edit Student Profile",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AcademyGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Student Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editClass,
                            onValueChange = { editClass = it },
                            label = { Text("Class") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editFee,
                            onValueChange = { editFee = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Monthly Fee (Rs)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AcademyDepartments.ALL.take(2).forEach { d ->
                            FilterChip(
                                selected = editDept == d,
                                onClick = { editDept = d },
                                label = { Text(AcademyDepartments.shortLabel(d), fontSize = 11.sp) }
                            )
                        }
                        AcademyDepartments.ALL.drop(2).forEach { d ->
                            FilterChip(
                                selected = editDept == d,
                                onClick = { editDept = d },
                                label = { Text(AcademyDepartments.shortLabel(d), fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editTeacher,
                        onValueChange = { editTeacher = it },
                        label = { Text("Teacher Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editQuranStatus,
                        onValueChange = { editQuranStatus = it },
                        label = { Text("Quran Parhai Status") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSchoolStatus,
                        onValueChange = { editSchoolStatus = it },
                        label = { Text("School Parhai Status") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditStudent(
                            student.copy(
                                name = editName.trim().ifEmpty { student.name },
                                className = editClass.trim().ifEmpty { student.className },
                                department = editDept,
                                teacherName = editTeacher.trim().ifEmpty { "Qari Sahib" },
                                monthlyFee = editFee.toIntOrNull() ?: student.monthlyFee,
                                quranStatus = editQuranStatus.trim().ifEmpty { student.quranStatus },
                                schoolStatus = editSchoolStatus.trim().ifEmpty { student.schoolStatus }
                            )
                        )
                        showEditStudentDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditStudentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TwoColumnScoreCard(
    heading: String,
    marks: Int,
    total: Int,
    percentage: Int,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, accentColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = heading,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AcademyTextDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$marks / $total",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = accentColor
            )
            Text(
                text = "$percentage%",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AcademyTextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = accentColor,
                trackColor = AcademyProgressTrack
            )
        }
    }
}

@Composable
private fun ProfileMetaItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(end = 6.dp)) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            color = AcademyBrightGold,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            fontSize = 13.5.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionDetailCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF5EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = AcademyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = AcademyGreen
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = AcademyTextMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MiniStatBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFAF7EE),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = AcademyTextMuted, fontWeight = FontWeight.SemiBold)
            Text(text = value, fontSize = 13.5.sp, color = color, fontWeight = FontWeight.ExtraBold)
        }
    }
}
