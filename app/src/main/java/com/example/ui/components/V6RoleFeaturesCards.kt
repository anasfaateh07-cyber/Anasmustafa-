package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.StudentEntity
import com.example.data.TeacherEntity
import com.example.data.UserAccountEntity
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

@Composable
fun ParentChildSearchAndLinkCard(
    allStudents: List<StudentEntity>,
    linkedChildren: List<StudentEntity>,
    onLinkChild: (StudentEntity) -> Unit,
    onOpenStudentProfile: (StudentEntity) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val matchingStudents = remember(allStudents, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            allStudents.take(5)
        } else {
            allStudents.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.fatherName.contains(q, ignoreCase = true) ||
                    it.className.contains(q, ignoreCase = true)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("parent_search_link_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, AcademyGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF5EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FamilyRestroom,
                        contentDescription = "Parent Portal",
                        tint = AcademyGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Parent Child Link & Sabaq Alerts",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AcademyGreen
                    )
                    Text(
                        text = "Search child & link for instant Sabaq / Marks notifications",
                        fontSize = 11.5.sp,
                        color = AcademyTextMuted
                    )
                }
            }

            if (linkedChildren.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEAF5EC),
                    border = BorderStroke(1.dp, AcademyGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "Active Notifications",
                                tint = AcademyGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Linked Children (${linkedChildren.size}) • Notifications Active",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                color = AcademyGreen
                            )
                        }
                        linkedChildren.forEach { child ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenStudentProfile(child) }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${child.name} (${child.className} - ${AcademyDepartments.shortLabel(child.department)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AcademyTextDark
                                )
                                Text(
                                    text = "View Full Profile →",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademyGreen
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Apna bacha ka naam likhein") },
                placeholder = { Text("e.g. Haider / Ahmed...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Child",
                        tint = AcademyGreen
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AcademyGreen,
                    unfocusedBorderColor = AcademyCardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_child_search_input")
            )

            if (matchingStudents.isEmpty()) {
                Text(
                    text = "No matching student found. Try searching another name or ask Admin/Teacher to enroll the student first.",
                    fontSize = 12.sp,
                    color = AcademyTextMuted
                )
            } else {
                matchingStudents.forEach { student ->
                    val alreadyLinked = linkedChildren.any { it.id == student.id }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAF7EE),
                        border = BorderStroke(1.dp, AcademyCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenStudentProfile(student) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = AcademyTextDark
                                    )
                                    Text(
                                        text = "${student.className} • ${student.department} • ${student.teacherName}",
                                        fontSize = 12.sp,
                                        color = AcademyTextMuted
                                    )
                                }
                                TextButton(onClick = { onOpenStudentProfile(student) }) {
                                    Text("Full Detail", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AcademyGreen)
                                }
                            }

                            Button(
                                onClick = {
                                    onLinkChild(student)
                                    Toast.makeText(
                                        context,
                                        "Walidain ke liye ittila: ${student.name} ka naya result add hua (Alerts Active)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (alreadyLinked) AcademyGold else AcademyGreen,
                                    contentColor = if (alreadyLinked) Color(0xFF1F1905) else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_add_kar_lain_pakka_pakka_${student.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Add kar lain - Pakka Pakka",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (alreadyLinked) {
                                        "Linked Pakka Pakka ✓ (Tap to Re-sync)"
                                    } else {
                                        "Add kar lain - Pakka Pakka"
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
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
fun AdminAssignTeacherGroupsCard(
    teachers: List<TeacherEntity>,
    users: List<UserAccountEntity>,
    onAssignGroups: (teacher: TeacherEntity?, userAccount: UserAccountEntity?, assignedGroupsCsv: String) -> Unit
) {
    val context = LocalContext.current
    var editingTeacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var editingUser by remember { mutableStateOf<UserAccountEntity?>(null) }
    val selectedGroups = remember { mutableStateListOf<String>() }

    val teacherUsers = remember(users, teachers) {
        users.filter {
            it.role.equals("Teacher", ignoreCase = true) &&
                !it.name.contains("anas mustafa", ignoreCase = true)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_assign_teacher_groups_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, AcademyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Admin Group Assignment",
                        tint = AcademyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Admin: Assign Groups to Teachers",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.5.sp,
                        color = AcademyGreen
                    )
                    Text(
                        text = "Teachers can only view & edit students in their assigned groups",
                        fontSize = 11.5.sp,
                        color = AcademyTextMuted
                    )
                }
            }

            if (teachers.isEmpty() && teacherUsers.isEmpty()) {
                Text(
                    text = "No teachers added yet. Tap '+ Add Teacher' above to add a teacher and assign groups (e.g. Hifz Group, Tuition Boys).",
                    fontSize = 12.5.sp,
                    color = AcademyTextMuted
                )
            } else {
                teachers.forEach { teacher ->
                    val matchedUser = teacherUsers.firstOrNull {
                        it.name.equals(teacher.name, ignoreCase = true)
                    }
                    val currentGroups = teacher.assignedGroups.ifBlank {
                        matchedUser?.assignedGroups?.ifBlank { teacher.department } ?: teacher.department
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAF7EE),
                        border = BorderStroke(1.dp, AcademyCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AcademyTextDark
                                )
                                Text(
                                    text = "Assigned Groups: $currentGroups",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AcademyGreen
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    editingTeacher = teacher
                                    editingUser = matchedUser
                                    selectedGroups.clear()
                                    selectedGroups.addAll(AcademyDepartments.parseAssignedGroups(currentGroups))
                                    if (selectedGroups.isEmpty()) {
                                        selectedGroups.add(teacher.department)
                                    }
                                },
                                border = BorderStroke(1.dp, AcademyGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Assign Groups", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcademyGreen)
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingTeacher != null || editingUser != null) {
        val targetName = editingTeacher?.name ?: editingUser?.name ?: "Teacher"
        AlertDialog(
            onDismissRequest = {
                editingTeacher = null
                editingUser = null
            },
            title = {
                Text(
                    text = "Assign Groups to $targetName",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AcademyGreen
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select which Academy groups $targetName is authorized to view and edit:",
                        fontSize = 12.5.sp,
                        color = AcademyTextMuted
                    )
                    AcademyDepartments.ALL.forEach { dept ->
                        val isChecked = selectedGroups.any { it.equals(dept, ignoreCase = true) }
                        FilterChip(
                            selected = isChecked,
                            onClick = {
                                if (isChecked) {
                                    selectedGroups.removeAll { it.equals(dept, ignoreCase = true) }
                                } else {
                                    selectedGroups.add(dept)
                                }
                            },
                            label = {
                                Text(
                                    text = dept,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalCsv = if (selectedGroups.isEmpty()) {
                            AcademyDepartments.HIFZ
                        } else {
                            selectedGroups.joinToString(",")
                        }
                        onAssignGroups(editingTeacher, editingUser, finalCsv)
                        Toast.makeText(
                            context,
                            "Assigned groups updated for $targetName: $finalCsv",
                            Toast.LENGTH_SHORT
                        ).show()
                        editingTeacher = null
                        editingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                ) {
                    Text("Approve & Save Groups")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingTeacher = null
                        editingUser = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TeacherAssignedGroupsBanner(
    teacherName: String,
    assignedGroupsCsv: String
) {
    val groups = AcademyDepartments.parseAssignedGroups(assignedGroupsCsv)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEAF5EC),
        border = BorderStroke(1.2.dp, AcademyGreen),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_assigned_groups_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.VerifiedUser,
                    contentDescription = "Assigned Groups",
                    tint = AcademyGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Teacher Access ($teacherName) • Admin-Assigned Groups",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = AcademyGreen
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                groups.forEach { groupName ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AcademyGreen
                    ) {
                        Text(
                            text = groupName,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun V6CreditsFooterCard() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFFAF7EE),
        border = BorderStroke(1.dp, AcademyGold),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("v6_credits_footer_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Al Hadid Academy • Nasirabad Jatlan, Azad Kashmir • v7.0 Main",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AcademyGreen,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Thanks for downloading our app - Made by anas mustafa",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AcademyOchre,
                textAlign = TextAlign.Center
            )
            Text(
                text = "anas mustafa • 03700880220 | 03129484214",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = AcademyTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
