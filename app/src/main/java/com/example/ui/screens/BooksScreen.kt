package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.BookEntity
import com.example.data.StudentEntity
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyProgressTrack
import com.example.ui.theme.AcademyRedAlert
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily

@Composable
fun BooksScreen(
    books: List<BookEntity>,
    students: List<StudentEntity>,
    onAddBook: (
        title: String,
        category: String,
        authorOrNotes: String,
        totalPages: Int,
        completedPages: Int,
        assignedStudentId: Int?,
        assignedStudentName: String
    ) -> Unit,
    onUpdateBook: (BookEntity) -> Unit,
    onAssignBook: (BookEntity, Int?, String) -> Unit,
    onUpdateBookProgress: (BookEntity, Int) -> Unit,
    onDeleteBook: (BookEntity) -> Unit
) {
    var filterMode by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<BookEntity?>(null) }
    var bookToAssign by remember { mutableStateOf<BookEntity?>(null) }
    var bookForProgress by remember { mutableStateOf<BookEntity?>(null) }

    val filteredBooks = remember(books, filterMode) {
        when (filterMode) {
            "Assigned" -> books.filter { it.assignedStudentName.isNotBlank() }
            "Unassigned" -> books.filter { it.assignedStudentName.isBlank() }
            "Completed" -> books.filter { it.completedPages >= it.totalPages && it.totalPages > 0 }
            else -> books
        }
    }

    val assignedCount = books.count { it.assignedStudentName.isNotBlank() }
    val avgProgress = if (books.isNotEmpty()) {
        books.map { it.progressPercent }.average().toInt()
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("books_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AcademyGreen),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                text = "Academy Books & Syllabus",
                                fontFamily = MerriweatherFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Assign Islamic & Tuition books to students & track page progress",
                                fontFamily = PlusJakartaFontFamily,
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
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_add_book")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Book",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add Book",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BookMiniStatChip(
                            label = "Total Books",
                            value = "${books.size}",
                            modifier = Modifier.weight(1f)
                        )
                        BookMiniStatChip(
                            label = "Assigned",
                            value = "$assignedCount",
                            modifier = Modifier.weight(1f)
                        )
                        BookMiniStatChip(
                            label = "Avg Progress",
                            value = "$avgProgress%",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Assigned", "Unassigned", "Completed").forEach { mode ->
                    FilterChip(
                        selected = filterMode == mode,
                        onClick = { filterMode = mode },
                        label = { Text(mode, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        if (filteredBooks.isEmpty()) {
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
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD8EAE2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoStories,
                                contentDescription = "No Books",
                                tint = AcademyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Books Added Yet",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add Noorani Qaida, Tajweed-ul-Quran, Hifz Para, or Class 5-8 Tuition books, assign them to students, and track reading progress.",
                            fontSize = 13.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Add First Book", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredBooks, key = { it.id }) { book ->
                BookItemCard(
                    book = book,
                    onEdit = { bookToEdit = book },
                    onDelete = { onDeleteBook(book) },
                    onAssignStudent = { bookToAssign = book },
                    onOpenProgressSlider = { bookForProgress = book },
                    onQuickStepProgress = { delta ->
                        val updatedPages = (book.completedPages + delta).coerceIn(0, book.totalPages)
                        onUpdateBookProgress(book, updatedPages)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddOrEditBookDialog(
            initialBook = null,
            students = students,
            onDismiss = { showAddDialog = false },
            onSave = { title, category, notes, totalPages, completedPages, studentId, studentName ->
                onAddBook(title, category, notes, totalPages, completedPages, studentId, studentName)
                showAddDialog = false
            }
        )
    }

    bookToEdit?.let { editing ->
        AddOrEditBookDialog(
            initialBook = editing,
            students = students,
            onDismiss = { bookToEdit = null },
            onSave = { title, category, notes, totalPages, completedPages, studentId, studentName ->
                onUpdateBook(
                    editing.copy(
                        title = title,
                        category = category,
                        authorOrNotes = notes,
                        totalPages = totalPages.coerceAtLeast(1),
                        completedPages = completedPages.coerceIn(0, totalPages.coerceAtLeast(1)),
                        assignedStudentId = studentId,
                        assignedStudentName = studentName
                    )
                )
                bookToEdit = null
            }
        )
    }

    bookToAssign?.let { targetBook ->
        AssignBookToStudentDialog(
            book = targetBook,
            students = students,
            onDismiss = { bookToAssign = null },
            onAssign = { studentId, studentName ->
                onAssignBook(targetBook, studentId, studentName)
                bookToAssign = null
            }
        )
    }

    bookForProgress?.let { targetBook ->
        UpdateBookProgressDialog(
            book = targetBook,
            onDismiss = { bookForProgress = null },
            onSaveProgress = { newCompletedPages ->
                onUpdateBookProgress(targetBook, newCompletedPages)
                bookForProgress = null
            }
        )
    }
}

@Composable
private fun BookMiniStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0E4D1F)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFFFACC15)
            )
            Text(
                text = label,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BookItemCard(
    book: BookEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAssignStudent: () -> Unit,
    onOpenProgressSlider: () -> Unit,
    onQuickStepProgress: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("book_card_${book.id}"),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD8EAE2)
                    ) {
                        Text(
                            text = book.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademyGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = book.title,
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AcademyTextDark
                    )
                    if (book.authorOrNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = book.authorOrNotes,
                            fontSize = 12.5.sp,
                            color = AcademyTextMuted
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("btn_edit_book_${book.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Book",
                            tint = AcademyGreen
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("btn_delete_book_${book.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Book",
                            tint = AcademyRedAlert
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Student Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFAF7EE),
                border = BorderStroke(1.dp, AcademyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Assigned Student",
                            fontSize = 11.sp,
                            color = AcademyTextMuted
                        )
                        Text(
                            text = book.assignedStudentName.ifBlank { "Not assigned yet" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = if (book.assignedStudentName.isNotBlank()) AcademyGreen else AcademyOchre,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onAssignStudent,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AcademyGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_assign_student_${book.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Assign to Student",
                            tint = AcademyGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (book.assignedStudentName.isBlank()) "Assign Student" else "Change Student",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcademyGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reading / Syllabus Progress",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AcademyTextDark
                )
                Text(
                    text = "${book.completedPages} / ${book.totalPages} pages (${book.progressPercent}%)",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcademyGreen,
                    modifier = Modifier.testTag("book_progress_text_${book.id}")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { book.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { onOpenProgressSlider() }
                    .testTag("book_progress_bar_${book.id}"),
                color = AcademyGreen,
                trackColor = AcademyProgressTrack
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onQuickStepProgress(-10) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-10 Pages", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcademyTextDark)
                }

                OutlinedButton(
                    onClick = { onQuickStepProgress(10) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_book_plus10_${book.id}")
                ) {
                    Text("+10 Pages", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = AcademyGreen)
                }

                Button(
                    onClick = onOpenProgressSlider,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("btn_update_progress_${book.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Set Progress",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set Progress", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddOrEditBookDialog(
    initialBook: BookEntity?,
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: String,
        authorOrNotes: String,
        totalPages: Int,
        completedPages: Int,
        assignedStudentId: Int?,
        assignedStudentName: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialBook?.title ?: "") }
    var category by remember { mutableStateOf(initialBook?.category ?: AcademyDepartments.HIFZ) }
    var authorOrNotes by remember { mutableStateOf(initialBook?.authorOrNotes ?: "") }
    var totalPagesText by remember { mutableStateOf((initialBook?.totalPages ?: 100).toString()) }
    var completedPagesText by remember { mutableStateOf((initialBook?.completedPages ?: 0).toString()) }
    var assignedStudentId by remember { mutableStateOf(initialBook?.assignedStudentId) }
    var assignedStudentName by remember { mutableStateOf(initialBook?.assignedStudentName ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBook == null) "Add Academy Book" else "Edit Book",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title *") },
                    placeholder = { Text("e.g. Tajweed-ul-Quran / Para 30 / Class 6 Math") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_book_title")
                )

                Text(
                    text = "Department / Category",
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
                                    selected = category == dept,
                                    onClick = { category = dept },
                                    label = { Text(dept, fontSize = 10.5.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = authorOrNotes,
                    onValueChange = { authorOrNotes = it },
                    label = { Text("Author / Subject Notes") },
                    placeholder = { Text("e.g. Daily Sabak & Exercises") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalPagesText,
                        onValueChange = { totalPagesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Total Pages") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_book_total_pages")
                    )
                    OutlinedTextField(
                        value = completedPagesText,
                        onValueChange = { completedPagesText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Completed Pages") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_book_completed_pages")
                    )
                }

                OutlinedTextField(
                    value = assignedStudentName,
                    onValueChange = {
                        assignedStudentName = it
                        assignedStudentId = students.firstOrNull { s ->
                            s.name.equals(it.trim(), ignoreCase = true)
                        }?.id
                    },
                    label = { Text("Assign to Student (Optional)") },
                    placeholder = { Text("Type or select student below") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_book_student_name")
                )

                if (students.isNotEmpty()) {
                    Text(
                        text = "Quick Select Enrolled Student:",
                        fontSize = 11.5.sp,
                        color = AcademyTextMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        students.take(3).forEach { st ->
                            FilterChip(
                                selected = assignedStudentId == st.id || assignedStudentName == st.name,
                                onClick = {
                                    assignedStudentId = st.id
                                    assignedStudentName = st.name
                                },
                                label = { Text(st.name, fontSize = 11.sp, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val total = (totalPagesText.toIntOrNull() ?: 100).coerceAtLeast(1)
                        val completed = (completedPagesText.toIntOrNull() ?: 0).coerceIn(0, total)
                        onSave(
                            title,
                            category,
                            authorOrNotes,
                            total,
                            completed,
                            assignedStudentId,
                            assignedStudentName
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                modifier = Modifier.testTag("confirm_save_book")
            ) {
                Text(if (initialBook == null) "Add Book" else "Save Changes")
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
private fun AssignBookToStudentDialog(
    book: BookEntity,
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onAssign: (studentId: Int?, studentName: String) -> Unit
) {
    var customStudentName by remember { mutableStateOf(book.assignedStudentName) }
    var selectedStudentId by remember { mutableStateOf(book.assignedStudentId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Assign '${book.title}'",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customStudentName,
                    onValueChange = {
                        customStudentName = it
                        selectedStudentId = students.firstOrNull { s ->
                            s.name.equals(it.trim(), ignoreCase = true)
                        }?.id
                    },
                    label = { Text("Student Name") },
                    placeholder = { Text("Enter or pick student below") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assign_dialog_student_input")
                )

                if (students.isNotEmpty()) {
                    Text(
                        text = "Enrolled Students in Room DB:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = AcademyGreen
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        students.take(5).forEach { student ->
                            val isSelected = customStudentName.equals(student.name, ignoreCase = true)
                            Surface(
                                onClick = {
                                    selectedStudentId = student.id
                                    customStudentName = student.name
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFFEAF5EC) else Color(0xFFFAF7EE),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AcademyGreen else AcademyCardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = AcademyTextDark
                                        )
                                        Text(
                                            text = "${student.className} • ${student.department}",
                                            fontSize = 11.5.sp,
                                            color = AcademyTextMuted
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = AcademyGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedStudentId, customStudentName.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                modifier = Modifier.testTag("confirm_assign_student")
            ) {
                Text("Assign Book")
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
private fun UpdateBookProgressDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onSaveProgress: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(book.completedPages.toFloat()) }
    val total = book.totalPages.coerceAtLeast(1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Update Book Progress",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = book.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AcademyTextDark
                )
                Text(
                    text = "${sliderValue.toInt()} / $total pages (${((sliderValue / total) * 100).toInt()}%)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = AcademyGreen
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..total.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = AcademyGreen,
                        activeTrackColor = AcademyGreen
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { sliderValue = (total * 0.5f) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("50%", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { sliderValue = total.toFloat() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("100% Done", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveProgress(sliderValue.toInt()) },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                modifier = Modifier.testTag("confirm_book_progress")
            ) {
                Text("Save Progress")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
