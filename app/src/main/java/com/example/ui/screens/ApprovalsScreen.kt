package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreStudentItem
import com.example.data.FirestoreUserRecord
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyCreamBg
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.PlusJakartaFontFamily

private val ACADEMY_GROUPS = listOf(
    "Hifz Group",
    "Nazra & Tajweed",
    "Tuition Boys",
    "Girls & Playgroup"
)

private val ROLE_OPTIONS = listOf(
    "teacher",
    "parent",
    "student",
    "principal",
    "admin"
)

@Composable
fun ApprovalsScreen(
    pendingUsers: List<FirestoreUserRecord>,
    allUsers: List<FirestoreUserRecord>,
    students: List<FirestoreStudentItem>,
    isSaving: Boolean,
    statusMessage: String?,
    errorMessage: String?,
    onSaveUser: (targetUid: String, newRole: String, groups: List<String>, studentIds: List<String>) -> Unit,
    onDismissMessages: () -> Unit,
    onRefreshStudents: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedUserForDetail by remember { mutableStateOf<FirestoreUserRecord?>(null) }

    LaunchedEffect(Unit) {
        onRefreshStudents()
    }

    // If a user was just approved/saved, close the detail modal
    LaunchedEffect(statusMessage) {
        if (!statusMessage.isNullOrBlank()) {
            selectedUserForDetail = null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card
        item {
            ApprovalsHeaderCard(pendingCount = pendingUsers.size, totalUsersCount = allUsers.size)
        }

        // 2. Feedback Message Banners (Success & Error with Firebase error code)
        if (!statusMessage.isNullOrBlank()) {
            item {
                ApprovalMessageBanner(
                    message = statusMessage,
                    isError = false,
                    onDismiss = onDismissMessages
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                ApprovalMessageBanner(
                    message = errorMessage,
                    isError = true,
                    onDismiss = onDismissMessages
                )
            }
        }

        // 3. Section 1: Pending Approvals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (pendingUsers.isNotEmpty()) AcademyOchre else AcademyGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pending Approvals (${pendingUsers.size})",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AcademyTextDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AcademyGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "role: pending",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AcademyGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (pendingUsers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AcademyCardBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AcademyGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = AcademyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Pending Approvals",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AcademyTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All registered user accounts are approved.",
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted
                        )
                    }
                }
            }
        } else {
            items(pendingUsers, key = { it.uid }) { user ->
                PendingUserCard(
                    user = user,
                    onClick = { selectedUserForDetail = user }
                )
            }
        }

        // 4. Section 2: All Users
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "All Users (${allUsers.size})",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AcademyTextDark
                )
                Text(
                    text = "Tap user to edit role",
                    fontSize = 11.5.sp,
                    color = AcademyTextMuted
                )
            }
        }

        if (allUsers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AcademyCardBorder))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No users found in collection.",
                            fontSize = 13.sp,
                            color = AcademyTextMuted
                        )
                    }
                }
            }
        } else {
            items(allUsers, key = { "all_${it.uid}" }) { user ->
                AllUserItemCard(
                    user = user,
                    onClick = { selectedUserForDetail = user }
                )
            }
        }
    }

    // User Detail & Approval Dialog / Modal Card
    if (selectedUserForDetail != null) {
        UserApprovalDetailDialog(
            user = selectedUserForDetail!!,
            allStudents = students,
            isSaving = isSaving,
            onDismiss = {
                if (!isSaving) selectedUserForDetail = null
            },
            onSave = { newRole, groups, studentIds ->
                onSaveUser(selectedUserForDetail!!.uid, newRole, groups, studentIds)
            }
        )
    }
}

@Composable
private fun ApprovalsHeaderCard(pendingCount: Int, totalUsersCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AcademyCreamBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AcademyCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AcademyGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VerifiedUser,
                            contentDescription = "Approvals",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Approvals & Role Management",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = AcademyGreen
                        )
                        Text(
                            text = "Admin Access Control",
                            fontSize = 11.5.sp,
                            color = AcademyTextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (pendingCount > 0) AcademyOchre else AcademyGreen,
                    contentColor = Color.White
                ) {
                    Text(
                        text = if (pendingCount > 0) "$pendingCount Pending" else "Up to date",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = AcademyCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Manage registered users, review pending account requests, and assign verified permissions for Teachers, Parents, Students and Admins.",
                fontSize = 12.sp,
                color = AcademyTextDark,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ApprovalMessageBanner(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    val bgColor = if (isError) Color(0xFFFFECEC) else Color(0xFFE8F5E9)
    val borderColor = if (isError) Color(0xFFE57373) else Color(0xFF81C784)
    val contentColor = if (isError) Color(0xFFB71C1C) else Color(0xFF1B5E20)
    val icon = if (isError) Icons.Filled.Warning else Icons.Filled.CheckCircle

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PendingUserCard(
    user: FirestoreUserRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("pending_user_${user.uid}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AcademyCardBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AcademyOchre.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.HowToReg,
                        contentDescription = null,
                        tint = AcademyOchre,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "User ${user.uid.take(6)}" },
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AcademyTextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.email.ifBlank { "No email address" },
                        fontSize = 12.sp,
                        color = AcademyTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = "Pending",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = AcademyCardBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Requested: ",
                        fontSize = 12.sp,
                        color = AcademyTextMuted
                    )
                    Text(
                        text = user.requestedRole.ifBlank { "Teacher" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AcademyGreen
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademyGreen,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Review",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Review & Approve",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AllUserItemCard(
    user: FirestoreUserRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("all_user_${user.uid}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AcademyCardBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val roleLower = user.role.lowercase()
                val roleColor = when {
                    roleLower == "admin" || roleLower == "principal" -> AcademyGreen
                    roleLower == "teacher" -> Color(0xFF15803D)
                    roleLower == "parent" -> Color(0xFF0369A1)
                    roleLower == "student" -> Color(0xFF4338CA)
                    else -> AcademyOchre
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(roleColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when {
                        roleLower == "admin" || roleLower == "principal" -> Icons.Filled.Shield
                        roleLower == "teacher" -> Icons.Filled.School
                        roleLower == "parent" -> Icons.Filled.FamilyRestroom
                        roleLower == "student" -> Icons.Filled.Person
                        else -> Icons.Filled.HowToReg
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = roleColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "User" },
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = AcademyTextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.email.ifBlank { "No email" },
                        fontSize = 11.5.sp,
                        color = AcademyTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = roleColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = user.role.ifBlank { "pending" }.replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit User",
                        tint = AcademyGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // If teacher: display groups
            if (user.role.equals("teacher", ignoreCase = true) && user.groups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    user.groups.forEach { grp ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AcademyGreen.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = grp,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcademyGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // If parent or student: display linked count
            if ((user.role.equals("parent", ignoreCase = true) || user.role.equals("student", ignoreCase = true)) &&
                user.studentIds.isNotEmpty()
            ) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${user.studentIds.size} student(s) linked",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0369A1)
                )
            }
        }
    }
}

/**
 * Detail Card / Dialog for Approving and Editing user permissions.
 *
 * Requirements:
 * - Role dropdown: teacher, parent, student, principal, admin
 * - If teacher: multi-select of academy groups (Hifz Group, Nazra & Tajweed, Tuition Boys, Girls & Playgroup)
 * - If parent or student: search box to find students by name in students collection, checkbox to link them, saved as studentIds
 * - Save button: saving writes role, groups or studentIds to users/{uid}. Do not change any other field.
 */
@Composable
private fun UserApprovalDetailDialog(
    user: FirestoreUserRecord,
    allStudents: List<FirestoreStudentItem>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (newRole: String, groups: List<String>, studentIds: List<String>) -> Unit
) {
    val initialRole = remember(user) {
        val r = user.role.lowercase().trim()
        if (r != "pending" && r.isNotBlank()) r
        else user.requestedRole.lowercase().trim().ifBlank { "teacher" }
    }

    var selectedRole by remember { mutableStateOf(initialRole) }
    var selectedGroups by remember { mutableStateOf(user.groups.toSet()) }
    var selectedStudentIds by remember { mutableStateOf(user.studentIds.toSet()) }
    var studentSearchQuery by remember { mutableStateOf("") }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (user.role.equals("pending", ignoreCase = true)) "Approve User Registration" else "Edit User Permissions",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AcademyTextDark
                    )
                    Text(
                        text = user.name.ifBlank { "User" },
                        fontSize = 13.sp,
                        color = AcademyTextMuted
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // User info box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AcademyCreamBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Email: ${user.email.ifBlank { "N/A" }}",
                            fontSize = 12.sp,
                            color = AcademyTextDark,
                            fontWeight = FontWeight.Medium
                        )
                        if (user.requestedRole.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Requested: ${user.requestedRole}",
                                fontSize = 11.5.sp,
                                color = AcademyOchre,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Dropdown
                Text(
                    text = "Assign Role:",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AcademyTextDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { roleDropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("role_dropdown_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = AcademyTextDark
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedRole.replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Select Role"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        ROLE_OPTIONS.forEach { roleOpt ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = roleOpt.replaceFirstChar { it.uppercase() },
                                        fontWeight = if (selectedRole == roleOpt) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedRole == roleOpt) AcademyGreen else AcademyTextDark
                                    )
                                },
                                onClick = {
                                    selectedRole = roleOpt
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Conditional Sections based on Selected Role
                when (selectedRole) {
                    "teacher" -> {
                        Text(
                            text = "Academy Groups (Multi-select):",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AcademyTextDark
                        )
                        Text(
                            text = "Select which group(s) this teacher is authorized to teach:",
                            fontSize = 11.5.sp,
                            color = AcademyTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        ACADEMY_GROUPS.forEach { grp ->
                            val isChecked = selectedGroups.contains(grp)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) AcademyGreen.copy(alpha = 0.08f) else Color.White,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isChecked) AcademyGreen else AcademyCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedGroups = if (isChecked) {
                                            selectedGroups - grp
                                        } else {
                                            selectedGroups + grp
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedGroups = if (checked) selectedGroups + grp else selectedGroups - grp
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = AcademyGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = grp,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = AcademyTextDark
                                    )
                                }
                            }
                        }
                    }

                    "parent", "student" -> {
                        Text(
                            text = if (selectedRole == "parent") "Link Children (Students):" else "Link Student Record:",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AcademyTextDark
                        )
                        Text(
                            text = "Search students in collection and check to link (Saved as studentIds):",
                            fontSize = 11.5.sp,
                            color = AcademyTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = studentSearchQuery,
                            onValueChange = { studentSearchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_search_input"),
                            placeholder = { Text("Search student by name...", fontSize = 12.5.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = AcademyTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (studentSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { studentSearchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AcademyGreen,
                                unfocusedBorderColor = AcademyCardBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredStudents = remember(allStudents, studentSearchQuery) {
                            if (studentSearchQuery.isBlank()) allStudents
                            else allStudents.filter {
                                it.name.contains(studentSearchQuery, ignoreCase = true) ||
                                    it.fatherName.contains(studentSearchQuery, ignoreCase = true) ||
                                    it.department.contains(studentSearchQuery, ignoreCase = true)
                            }
                        }

                        Text(
                            text = "${selectedStudentIds.size} student(s) selected",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademyGreen,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        if (filteredStudents.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AcademyCreamBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (allStudents.isEmpty()) "No students found in academy." else "No matching students found.",
                                        fontSize = 12.sp,
                                        color = AcademyTextMuted
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                filteredStudents.forEach { st ->
                                    val isChecked = selectedStudentIds.contains(st.id)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isChecked) Color(0xFFF0FDF4) else Color.White,
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (isChecked) AcademyGreen else AcademyCardBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedStudentIds = if (isChecked) {
                                                    selectedStudentIds - st.id
                                                } else {
                                                    selectedStudentIds + st.id
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedStudentIds = if (checked) selectedStudentIds + st.id else selectedStudentIds - st.id
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = AcademyGreen)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = st.name,
                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = AcademyTextDark
                                                )
                                                if (st.department.isNotBlank() || st.className.isNotBlank()) {
                                                    Text(
                                                        text = "${st.department} • ${st.className}",
                                                        fontSize = 11.sp,
                                                        color = AcademyTextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "admin", "principal" -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AcademyOchre.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = AcademyOchre,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "This will grant complete Administrative and Financial permissions across Al Hadid Academy.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(selectedRole, selectedGroups.toList(), selectedStudentIds.toList())
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcademyGreen,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("save_approval_btn")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Saving...", fontSize = 13.sp)
                } else {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (user.role.equals("pending", ignoreCase = true)) "Approve & Save" else "Save Changes",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", fontSize = 13.sp)
            }
        }
    )
}
