package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.MonthlyReportEntity
import com.example.data.SchoolSabaqEntity
import com.example.data.StudentEntity
import com.example.ui.components.AlHadidLogoEmblem
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
import com.example.util.PdfReceiptGenerator

// =========================================================================
// B) GROUPS TAB - Common Group ("Common - All Students") + 6 Sub Groups
// =========================================================================
@Composable
fun GroupsScreen(
    students: List<StudentEntity>,
    onStudentClick: (StudentEntity) -> Unit = {},
    onAddStudentToGroup: (
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
    var selectedGroup by remember { mutableStateOf(AcademyDepartments.COMMON_ALL) }
    var showEnrollDialog by remember { mutableStateOf(false) }

    val subGroups = listOf(
        AcademyDepartments.SUB_QURAN_HIFZ,
        AcademyDepartments.SUB_NAZRA,
        AcademyDepartments.SUB_TAJWEED,
        AcademyDepartments.SUB_TUITION_BOYS,
        AcademyDepartments.SUB_TUITION_GIRLS,
        AcademyDepartments.SUB_PLAYGROUP
    )

    // Common - All Students automatically shows ALL students from all groups combined (no DB duplication)
    val displayedStudents = remember(students, selectedGroup) {
        if (selectedGroup == AcademyDepartments.COMMON_ALL) {
            students
        } else {
            students.filter { AcademyDepartments.matchesSubGroup(it.department, selectedGroup) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("groups_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Common Group Master Card: "Common - All Students"
        item {
            val isCommonSelected = selectedGroup == AcademyDepartments.COMMON_ALL
            Card(
                onClick = { selectedGroup = AcademyDepartments.COMMON_ALL },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_common_all_students"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCommonSelected) AcademyGreen else Color(0xFF0E4D1F)
                ),
                border = BorderStroke(
                    width = if (isCommonSelected) 2.dp else 1.dp,
                    color = AcademyBrightGold
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AcademyGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Diversity3,
                                    contentDescription = "Common Group",
                                    tint = Color(0xFF1F1905),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Common - All Students",
                                    fontFamily = MerriweatherFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Combined live view of all Academy groups (No duplicate rows)",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDFF5E3)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AcademyBrightGold
                        ) {
                            Text(
                                text = "${students.size} Students",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF1F1905),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sub Groups Grid: Quran Hifz, Nazra, Tajweed, Tuition Boys, Tuition Girls, Playgroup
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Academy Sub Groups",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.5.sp,
                        color = AcademyGreen
                    )
                    OutlinedButton(
                        onClick = { showEnrollDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AcademyGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Add Student",
                            tint = AcademyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("+ Add Student", fontSize = 12.sp, color = AcademyGreen, fontWeight = FontWeight.Bold)
                    }
                }

                subGroups.chunked(2).forEach { rowGroups ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowGroups.forEach { groupName ->
                            val count = students.count {
                                AcademyDepartments.matchesSubGroup(it.department, groupName)
                            }
                            val isSelected = selectedGroup == groupName
                            Card(
                                onClick = { selectedGroup = groupName },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("subgroup_card_$groupName"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFEAF5EC) else Color.White
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AcademyGreen else AcademyCardBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = groupName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = AcademyGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$count Students",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = AcademyOchre
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Combined / Filtered Students List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$selectedGroup (${displayedStudents.size})",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    color = AcademyGreen
                )
                if (selectedGroup != AcademyDepartments.COMMON_ALL) {
                    TextButton(onClick = { selectedGroup = AcademyDepartments.COMMON_ALL }) {
                        Text("Show Common - All Students", fontSize = 12.sp, color = AcademyOchre)
                    }
                }
            }
        }

        if (displayedStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No students in '$selectedGroup' yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AcademyTextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Students added to any group automatically appear in 'Common - All Students'.",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(displayedStudents, key = { it.id }) { student ->
                Card(
                    onClick = { onStudentClick(student) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_student_card_${student.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                text = "${student.className} • Group: ${student.department}",
                                fontSize = 12.5.sp,
                                color = AcademyTextMuted
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEAF5EC)
                        ) {
                            Text(
                                text = student.department,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademyGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showEnrollDialog) {
        val targetDept = if (selectedGroup == AcademyDepartments.COMMON_ALL) {
            AcademyDepartments.SUB_QURAN_HIFZ
        } else {
            selectedGroup
        }
        AddStudentDialog(
            defaultTeacher = "Qari Sahib",
            initialDepartment = targetDept,
            onDismiss = { showEnrollDialog = false },
            onConfirm = { name, father, cls, dept, teacher, fee, phone, paid ->
                onAddStudentToGroup(name, father, cls, dept, teacher, fee, phone, paid)
                showEnrollDialog = false
            }
        )
    }
}

// =========================================================================
// C) SCHOOL SABAQ SYSTEM TAB (Urdu, English, Maths, Science, Islamiat + Custom)
// =========================================================================
@Composable
fun SchoolSabaqScreen(
    students: List<StudentEntity>,
    bookNames: List<String>,
    sabaqEntries: List<SchoolSabaqEntity>,
    defaultDate: String,
    onAddNewBookName: (String) -> Unit,
    onAddSchoolSabaq: (
        studentId: Int,
        studentName: String,
        className: String,
        dateString: String,
        bookName: String,
        sabaqDetail: String
    ) -> Unit,
    onDeleteSchoolSabaq: (SchoolSabaqEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedStudent by remember(students) { mutableStateOf(students.firstOrNull()) }
    var customStudentName by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf(bookNames.firstOrNull() ?: "Urdu") }
    var dateString by remember { mutableStateOf(defaultDate) }
    var sabaqDetail by remember { mutableStateOf("") }
    var showAddBookNameDialog by remember { mutableStateOf(false) }
    var newBookNameInput by remember { mutableStateOf("") }
    var filterBook by remember { mutableStateOf("All") }

    val filteredSabaq = remember(sabaqEntries, filterBook) {
        if (filterBook == "All") sabaqEntries else sabaqEntries.filter { it.bookName.equals(filterBook, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("school_sabaq_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner
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
                            text = "School Sabaq System",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Urdu, English, Maths, Science, Islamiat & custom books",
                            fontSize = 12.5.sp,
                            color = Color(0xFFDFF5E3)
                        )
                    }
                    Button(
                        onClick = { showAddBookNameDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AcademyGold,
                            contentColor = Color(0xFF1F1905)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_add_school_book_name")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Book Name",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Book Name", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Record Student School Sabaq Card (Student + Date + Book Name + Sabaq Detail)
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Assign Daily School Sabaq",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        color = AcademyGreen
                    )

                    // Student Selector
                    if (students.isNotEmpty()) {
                        Text(
                            text = "Select Student:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademyOchre
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            students.forEach { st ->
                                FilterChip(
                                    selected = selectedStudent?.id == st.id,
                                    onClick = {
                                        selectedStudent = st
                                        customStudentName = st.name
                                    },
                                    label = { Text("${st.name} (${st.className})", fontSize = 11.5.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customStudentName.ifBlank { selectedStudent?.name ?: "" },
                        onValueChange = {
                            customStudentName = it
                            selectedStudent = students.firstOrNull { s -> s.name.equals(it.trim(), ignoreCase = true) }
                        },
                        label = { Text("Student Name *") },
                        placeholder = { Text("Select or enter student name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_school_sabaq_student")
                    )

                    // Book Name Chips (Urdu, English, Maths, Science, Islamiat + Custom)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Book Name:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademyGreen
                        )
                        TextButton(onClick = { showAddBookNameDialog = true }) {
                            Text("+ Add New Book Name", fontSize = 11.5.sp, color = AcademyOchre)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bookNames.forEach { bName ->
                            FilterChip(
                                selected = selectedBook == bName,
                                onClick = { selectedBook = bName },
                                label = { Text(bName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dateString,
                            onValueChange = { dateString = it },
                            label = { Text("Date") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_school_sabaq_date")
                        )
                        OutlinedTextField(
                            value = selectedBook,
                            onValueChange = { selectedBook = it },
                            label = { Text("Book Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_school_sabaq_book")
                        )
                    }

                    OutlinedTextField(
                        value = sabaqDetail,
                        onValueChange = { sabaqDetail = it },
                        label = { Text("Sabaq Detail (Lesson / Homework) *") },
                        placeholder = { Text("e.g. Chapter 2 Reading + Exercise Q1-Q5") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_school_sabaq_detail")
                    )

                    Button(
                        onClick = {
                            val finalStudentName = customStudentName.ifBlank { selectedStudent?.name ?: "" }.trim()
                            if (finalStudentName.isBlank() || sabaqDetail.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter Student Name and Sabaq Detail",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onAddSchoolSabaq(
                                    selectedStudent?.id ?: 0,
                                    finalStudentName,
                                    selectedStudent?.className ?: "Class 5",
                                    dateString.ifBlank { defaultDate },
                                    selectedBook.ifBlank { "Urdu" },
                                    sabaqDetail
                                )
                                sabaqDetail = ""
                                Toast.makeText(context, "School Sabaq saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_school_sabaq")
                    ) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Save Sabaq", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save School Sabaq", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Filter by Book Name
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (listOf("All") + bookNames).forEach { b ->
                    FilterChip(
                        selected = filterBook == b,
                        onClick = { filterBook = b },
                        label = { Text(b, fontSize = 11.5.sp) }
                    )
                }
            }
        }

        if (filteredSabaq.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = "No School Sabaq",
                            tint = AcademyGreen,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No School Sabaq Recorded Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = AcademyGreen
                        )
                        Text(
                            text = "Select a student, choose Book Name (Urdu, English, Maths, Science, Islamiat), and save daily Sabaq above.",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredSabaq, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEAF5EC)
                                ) {
                                    Text(
                                        text = item.bookName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AcademyGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.dateString,
                                    fontSize = 11.5.sp,
                                    color = AcademyOchre,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = item.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.5.sp,
                                color = AcademyTextDark
                            )
                            Text(
                                text = "Sabaq: ${item.sabaqDetail}",
                                fontSize = 13.sp,
                                color = AcademyTextMuted
                            )
                        }
                        IconButton(onClick = { onDeleteSchoolSabaq(item) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Sabaq",
                                tint = AcademyRedAlert
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddBookNameDialog) {
        AlertDialog(
            onDismissRequest = { showAddBookNameDialog = false },
            title = {
                Text(
                    text = "Add New School Book Name",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = AcademyGreen
                )
            },
            text = {
                OutlinedTextField(
                    value = newBookNameInput,
                    onValueChange = { newBookNameInput = it },
                    label = { Text("Book / Subject Name *") },
                    placeholder = { Text("e.g. Computer, Social Studies, Arabic") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_school_book_name")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBookNameInput.isNotBlank()) {
                            onAddNewBookName(newBookNameInput.trim())
                            selectedBook = newBookNameInput.trim()
                            newBookNameInput = ""
                            showAddBookNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                    modifier = Modifier.testTag("confirm_new_school_book_name")
                ) {
                    Text("Add Book Name")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// =========================================================================
// D) MONTHLY REPORT TAB (Select Student -> 8 Fields -> 3 Signatures -> PDF)
// =========================================================================
@Composable
fun MonthlyReportScreen(
    students: List<StudentEntity>,
    reports: List<MonthlyReportEntity>,
    defaultDate: String,
    onSaveAndGenerateReport: (
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
        onSaved: (MonthlyReportEntity) -> Unit
    ) -> Unit,
    onDeleteReport: (MonthlyReportEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedStudent by remember(students) { mutableStateOf(students.firstOrNull()) }
    var studentNameInput by remember { mutableStateOf(students.firstOrNull()?.name ?: "") }
    var classNameInput by remember { mutableStateOf(students.firstOrNull()?.className ?: "Class 6") }

    // 8 Required Report Fields
    var quranTeacherRemark by remember { mutableStateOf("MashAllah, consistent Tajweed and daily Sabak progress.") }
    var principalRemark by remember { mutableStateOf("Shows good discipline and dedication to Deeni & Dunyavi studies.") }
    var classTeacherRemark by remember { mutableStateOf("Active in class participation and homework completion.") }

    val behaviorOptions = listOf("Excellent", "Good", "Average", "Needs Improvement")
    var behaviorWithFellows by remember { mutableStateOf("Excellent") }
    var fellowsDropdownExpanded by remember { mutableStateOf(false) }

    var behaviorWithTeachers by remember { mutableStateOf("Excellent") }
    var teachersDropdownExpanded by remember { mutableStateOf(false) }

    val attentionAreas = listOf("School", "Quran", "Behavior")
    val selectedAttention = remember { mutableStateListOf<String>() }

    var participatedInMilad by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("monthly_report_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with Logo
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlHadidLogoEmblem(size = 54.dp, outerRingColor = AcademyBrightGold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monthly Student Report",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Select Student -> Fill 8 Assessment Fields -> Generate PDF & Share",
                            fontSize = 12.sp,
                            color = Color(0xFFDFF5E3)
                        )
                    }
                }
            }
        }

        // Main 8-Field Report Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.2.dp, AcademyCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Student",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AcademyGreen
                    )

                    if (students.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            students.forEach { st ->
                                FilterChip(
                                    selected = selectedStudent?.id == st.id,
                                    onClick = {
                                        selectedStudent = st
                                        studentNameInput = st.name
                                        classNameInput = st.className
                                    },
                                    label = { Text("${st.name} (${st.className})", fontSize = 11.5.sp) }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = studentNameInput,
                            onValueChange = { studentNameInput = it },
                            label = { Text("Student Name *") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("report_student_name_input")
                        )
                        OutlinedTextField(
                            value = classNameInput,
                            onValueChange = { classNameInput = it },
                            label = { Text("Class") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = AcademyCardBorder)

                    // Field 1: Remark of Quran Teacher (multiline text)
                    OutlinedTextField(
                        value = quranTeacherRemark,
                        onValueChange = { quranTeacherRemark = it },
                        label = { Text("1. Remark of Quran Teacher") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_field_quran_remark")
                    )

                    // Field 2: Remarks of Principal (multiline text)
                    OutlinedTextField(
                        value = principalRemark,
                        onValueChange = { principalRemark = it },
                        label = { Text("2. Remarks of Principal") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_field_principal_remark")
                    )

                    // Field 3: Remarks of Class Teacher (multiline text)
                    OutlinedTextField(
                        value = classTeacherRemark,
                        onValueChange = { classTeacherRemark = it },
                        label = { Text("3. Remarks of Class Teacher") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_field_class_teacher_remark")
                    )

                    // Field 4: Behavior with class fellows (Dropdown)
                    BehaviorDropdownField(
                        label = "4. Behavior with class fellows",
                        selectedValue = behaviorWithFellows,
                        options = behaviorOptions,
                        expanded = fellowsDropdownExpanded,
                        onExpandChange = { fellowsDropdownExpanded = it },
                        onSelect = {
                            behaviorWithFellows = it
                            fellowsDropdownExpanded = false
                        }
                    )

                    // Field 5: Behavior with teachers (Dropdown)
                    BehaviorDropdownField(
                        label = "5. Behavior with teachers",
                        selectedValue = behaviorWithTeachers,
                        options = behaviorOptions,
                        expanded = teachersDropdownExpanded,
                        onExpandChange = { teachersDropdownExpanded = it },
                        onSelect = {
                            behaviorWithTeachers = it
                            teachersDropdownExpanded = false
                        }
                    )

                    // Field 6: Need attention in (Checkboxes: School / Quran / Behavior - multiple)
                    Column {
                        Text(
                            text = "6. Need attention in (Select multiple if needed):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = AcademyGreen
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            attentionAreas.forEach { area ->
                                val isChecked = area in selectedAttention
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        if (isChecked) selectedAttention.remove(area) else selectedAttention.add(area)
                                    }
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (area !in selectedAttention) selectedAttention.add(area)
                                            } else {
                                                selectedAttention.remove(area)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AcademyGreen)
                                    )
                                    Text(text = area, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // Field 7: Did participate in Mehfil Milad Pak? (Yes/No Radio)
                    Column {
                        Text(
                            text = "7. Did participate in Mehfil Milad Pak?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = AcademyGreen
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { participatedInMilad = true }
                            ) {
                                RadioButton(
                                    selected = participatedInMilad,
                                    onClick = { participatedInMilad = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = AcademyGreen)
                                )
                                Text("Yes", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { participatedInMilad = false }
                            ) {
                                RadioButton(
                                    selected = !participatedInMilad,
                                    onClick = { participatedInMilad = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = AcademyGreen)
                                )
                                Text("No", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = AcademyCardBorder)

                    // Field 8: Signatures Row (3 signatures with label below)
                    Text(
                        text = "8. Official Signatures",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = AcademyGreen
                    )

                    SignaturesRowComponent()

                    Spacer(modifier = Modifier.height(4.dp))

                    // Generate PDF with logo and share Button
                    Button(
                        onClick = {
                            val finalName = studentNameInput.trim().ifEmpty {
                                selectedStudent?.name ?: "Student"
                            }
                            val attentionJoined = if (selectedAttention.isEmpty()) {
                                "None"
                            } else {
                                selectedAttention.joinToString(", ")
                            }
                            onSaveAndGenerateReport(
                                selectedStudent?.id ?: 0,
                                finalName,
                                classNameInput.ifBlank { "Class 6" },
                                selectedStudent?.department ?: AcademyDepartments.HIFZ,
                                quranTeacherRemark,
                                principalRemark,
                                classTeacherRemark,
                                behaviorWithFellows,
                                behaviorWithTeachers,
                                attentionJoined,
                                participatedInMilad,
                                defaultDate
                            ) { savedReport ->
                                PdfReceiptGenerator.shareMonthlyReportPdf(context, savedReport)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_generate_report_pdf")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PictureAsPdf,
                            contentDescription = "Generate PDF",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate PDF with logo and share",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Previous Saved Monthly Reports
        if (reports.isNotEmpty()) {
            item {
                Text(
                    text = "Generated Monthly Reports (${reports.size})",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    color = AcademyGreen
                )
            }
            items(reports, key = { it.id }) { rep ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, AcademyCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${rep.studentName} (${rep.className})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AcademyTextDark
                            )
                            Text(
                                text = "Fellows: ${rep.behaviorWithFellows} • Teachers: ${rep.behaviorWithTeachers} • Milad: ${if (rep.participatedInMilad) "Yes" else "No"}",
                                fontSize = 12.sp,
                                color = AcademyTextMuted
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { PdfReceiptGenerator.shareMonthlyReportPdf(context, rep) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "Share PDF",
                                    tint = AcademyGreen
                                )
                            }
                            IconButton(onClick = { onDeleteReport(rep) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Report",
                                    tint = AcademyRedAlert
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BehaviorDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = AcademyGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = { onExpandChange(true) },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAF7EE),
                border = BorderStroke(1.dp, AcademyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedValue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AcademyTextDark
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select Behavior",
                        tint = AcademyGreen
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontWeight = FontWeight.Medium) },
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SignaturesRowComponent() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("report_signatures_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left: Class Teacher Sign (stylish female signature) + text "Female Teacher"
        SignatureBox(
            signatureText = "F. Teacher",
            roleHeader = "Class Teacher Sign",
            bottomLabel = "Female Teacher",
            isFemaleFlourish = true,
            modifier = Modifier.weight(1f)
        )
        // Center: Quran Teacher Sign + text "anas mustafa" (Quran Teacher)
        SignatureBox(
            signatureText = "anas mustafa",
            roleHeader = "Quran Teacher Sign",
            bottomLabel = "anas mustafa",
            isFemaleFlourish = false,
            modifier = Modifier.weight(1f)
        )
        // Right: Principal Sign + text "Awais" (Principal)
        SignatureBox(
            signatureText = "Awais",
            roleHeader = "Principal Sign",
            bottomLabel = "Awais",
            isFemaleFlourish = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SignatureBox(
    signatureText: String,
    roleHeader: String,
    bottomLabel: String,
    isFemaleFlourish: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFAF7EE),
        border = BorderStroke(1.dp, AcademyGold)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = roleHeader,
                fontSize = 9.5.sp,
                color = AcademyTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Stylish Calligraphic Signature + Flourish Curve
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        if (isFemaleFlourish) {
                            moveTo(w * 0.15f, h * 0.78f)
                            cubicTo(w * 0.35f, h * 0.45f, w * 0.65f, h * 0.95f, w * 0.88f, h * 0.62f)
                        } else {
                            moveTo(w * 0.18f, h * 0.80f)
                            quadraticTo(w * 0.50f, h * 0.60f, w * 0.85f, h * 0.75f)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF115E27),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                Text(
                    text = signatureText,
                    fontFamily = MerriweatherFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AcademyGreen,
                    textAlign = TextAlign.Center
                )
            }

            HorizontalDivider(
                thickness = 0.8.dp,
                color = AcademyCardBorder,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )

            Text(
                text = bottomLabel,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.5.sp,
                color = AcademyTextDark,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
