package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.FeeRecordEntity
import com.example.data.StudentEntity
import com.example.ui.components.AlHadidLogoEmblem
import com.example.ui.components.MonthlyFeeCollectionChartCard
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
import java.io.File

@Composable
fun FeesScreen(
    fees: List<FeeRecordEntity>,
    students: List<StudentEntity>,
    feeCollected: Int,
    pendingFees: Int,
    onAddFeeVoucher: (student: StudentEntity, amount: Int, monthCycle: String, isPaid: Boolean) -> Unit,
    onAddStudentAndFee: (name: String, className: String, dept: String, amount: Int, isPaid: Boolean) -> Unit,
    onToggleFeePaid: (FeeRecordEntity) -> Unit,
    onDeleteFee: (FeeRecordEntity) -> Unit
) {
    val context = LocalContext.current
    var filterStatus by remember { mutableStateOf("All") }
    var showCreateFeeDialog by remember { mutableStateOf(false) }
    var receiptPreviewFee by remember { mutableStateOf<FeeRecordEntity?>(null) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    val filteredFees = remember(fees, filterStatus) {
        when (filterStatus) {
            "Paid" -> fees.filter { it.isPaid }
            "Pending" -> fees.filter { !it.isPaid }
            else -> fees
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("fees_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Financial Summary Banner
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
                                text = "Academy Fee Management",
                                fontFamily = MerriweatherFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Generate official PDF receipts & share via WhatsApp",
                                fontSize = 12.5.sp,
                                color = Color(0xFFDFF5E3)
                            )
                        }
                        Button(
                            onClick = { showCreateFeeDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AcademyGold,
                                contentColor = Color(0xFF1F1905)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_add_fee_voucher")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "New Fee",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Fee", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0E4D1F)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Fee Collected", fontSize = 11.5.sp, color = Color(0xFFDFF5E3))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Rs $feeCollected",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = AcademyBrightGold
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0E4D1F)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pending Fees", fontSize = 11.5.sp, color = Color(0xFFDFF5E3))
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Rs $pendingFees",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Fee Collection vs Targets Chart Card
        item {
            MonthlyFeeCollectionChartCard(
                fees = fees,
                students = students,
                feeCollected = feeCollected,
                pendingFees = pendingFees
            )
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Paid", "Pending").forEach { mode ->
                    FilterChip(
                        selected = filterStatus == mode,
                        onClick = { filterStatus = mode },
                        label = { Text(mode, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        if (filteredFees.isEmpty()) {
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
                                imageVector = Icons.Filled.Receipt,
                                contentDescription = "No Fee Records",
                                tint = AcademyGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Fee Vouchers Found",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AcademyGreen
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create a fee voucher to track collected & pending fees, generate an official PDF receipt, and share it directly on WhatsApp.",
                            fontSize = 13.sp,
                            color = AcademyTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showCreateFeeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Create Fee Voucher", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredFees, key = { it.id }) { fee ->
                FeeRecordCard(
                    fee = fee,
                    onTogglePaid = { onToggleFeePaid(fee) },
                    onDelete = { onDeleteFee(fee) },
                    onGenerateAndPreviewPdf = {
                        val file = PdfReceiptGenerator.generateFeeReceiptPdf(context, fee)
                        generatedPdfFile = file
                        receiptPreviewFee = fee
                        Toast.makeText(
                            context,
                            "PDF Receipt saved: ${file.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onShareWhatsApp = {
                        val file = PdfReceiptGenerator.shareReceiptToWhatsApp(context, fee, directWhatsApp = true)
                        generatedPdfFile = file
                    }
                )
            }
        }
    }

    if (showCreateFeeDialog) {
        CreateFeeVoucherDialog(
            students = students,
            onDismiss = { showCreateFeeDialog = false },
            onConfirmExistingStudent = { student, amount, cycle, paid ->
                onAddFeeVoucher(student, amount, cycle, paid)
                showCreateFeeDialog = false
            },
            onConfirmQuickStudent = { name, className, dept, amount, paid ->
                onAddStudentAndFee(name, className, dept, amount, paid)
                showCreateFeeDialog = false
            }
        )
    }

    receiptPreviewFee?.let { fee ->
        PdfReceiptPreviewDialog(
            fee = fee,
            pdfFile = generatedPdfFile,
            onDismiss = { receiptPreviewFee = null },
            onShareWhatsApp = {
                PdfReceiptGenerator.shareReceiptToWhatsApp(context, fee, directWhatsApp = true)
            }
        )
    }
}

@Composable
private fun FeeRecordCard(
    fee: FeeRecordEntity,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit,
    onGenerateAndPreviewPdf: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val receiptNo = fee.receiptNumber.ifEmpty { "AHA-${1000 + fee.id}" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fee_card_${fee.id}"),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (fee.isPaid) Color(0xFFD8EAE2) else Color(0xFFF9E2DC)
                        ) {
                            Text(
                                text = if (fee.isPaid) "PAID" else "PENDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (fee.isPaid) AcademyGreen else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Receipt #$receiptNo",
                            fontSize = 11.5.sp,
                            color = AcademyTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = fee.studentName,
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AcademyTextDark
                    )
                    Text(
                        text = "${fee.className} • ${fee.department} • ${fee.monthCycle}",
                        fontSize = 12.5.sp,
                        color = AcademyTextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Rs ${fee.amount}",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = AcademyGreen
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Fee",
                            tint = AcademyRedAlert,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mark Paid / Unpaid
                OutlinedButton(
                    onClick = onTogglePaid,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AcademyGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_toggle_fee_${fee.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Toggle Paid",
                        tint = AcademyGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (fee.isPaid) "Mark Unpaid" else "Mark Paid",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademyGreen
                    )
                }

                // PDF Receipt Preview & Save
                OutlinedButton(
                    onClick = onGenerateAndPreviewPdf,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AcademyOchre),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_pdf_receipt_${fee.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PictureAsPdf,
                        contentDescription = "PDF Receipt",
                        tint = AcademyOchre,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PDF Receipt",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademyOchre
                    )
                }

                // WhatsApp Share Button
                Button(
                    onClick = onShareWhatsApp,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.15f)
                        .testTag("btn_whatsapp_share_${fee.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share on WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateFeeVoucherDialog(
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onConfirmExistingStudent: (student: StudentEntity, amount: Int, monthCycle: String, isPaid: Boolean) -> Unit,
    onConfirmQuickStudent: (name: String, className: String, dept: String, amount: Int, isPaid: Boolean) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var customStudentName by remember { mutableStateOf("") }
    var customClass by remember { mutableStateOf("Class 6") }
    var customDept by remember { mutableStateOf(AcademyDepartments.HIFZ) }
    var amountText by remember {
        mutableStateOf((students.firstOrNull()?.monthlyFee ?: 1500).toString())
    }
    var monthCycle by remember { mutableStateOf("October cycle") }
    var isPaid by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Fee Voucher",
                fontFamily = MerriweatherFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AcademyGreen
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (students.isNotEmpty()) {
                    Text(
                        text = "Select Enrolled Student:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademyGreen
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        students.take(4).forEach { st ->
                            FilterChip(
                                selected = selectedStudent?.id == st.id,
                                onClick = {
                                    selectedStudent = st
                                    amountText = st.monthlyFee.toString()
                                },
                                label = { Text("${st.name} (${st.className})", fontSize = 11.5.sp) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customStudentName,
                        onValueChange = { customStudentName = it },
                        label = { Text("Student Name *") },
                        placeholder = { Text("e.g. Muhammad Bilal") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_fee_student_name")
                    )
                    OutlinedTextField(
                        value = customClass,
                        onValueChange = { customClass = it },
                        label = { Text("Class") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Fee Amount (Rs) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_fee_amount")
                )

                OutlinedTextField(
                    value = monthCycle,
                    onValueChange = { monthCycle = it },
                    label = { Text("Fee Cycle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPaid,
                        onCheckedChange = { isPaid = it }
                    )
                    Text(
                        text = "Fee Received (Mark as Paid)",
                        fontSize = 13.sp,
                        color = AcademyTextDark
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toIntOrNull() ?: 1500
                    val st = selectedStudent
                    if (st != null) {
                        onConfirmExistingStudent(st, amt, monthCycle.ifBlank { "October cycle" }, isPaid)
                    } else if (customStudentName.isNotBlank()) {
                        onConfirmQuickStudent(customStudentName, customClass, customDept, amt, isPaid)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen),
                modifier = Modifier.testTag("confirm_create_fee")
            ) {
                Text("Save Fee Voucher")
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
private fun PdfReceiptPreviewDialog(
    fee: FeeRecordEntity,
    pdfFile: File?,
    onDismiss: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    val receiptNo = fee.receiptNumber.ifEmpty { "AHA-${1000 + fee.id}" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlHadidLogoEmblem(size = 40.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Al Hadid Academy",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AcademyGreen
                    )
                    Text(
                        text = "Official PDF Fee Receipt #$receiptNo",
                        fontSize = 11.5.sp,
                        color = AcademyOchre,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAF7EE),
                border = BorderStroke(1.2.dp, AcademyGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nasirabad Jatlan, Azad Kashmir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AcademyOchre
                    )
                    HorizontalDivider(color = AcademyCardBorder)
                    Text("Student: ${fee.studentName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Class & Dept: ${fee.className} • ${fee.department}", fontSize = 13.sp)
                    Text("Fee Cycle: ${fee.monthCycle}", fontSize = 13.sp)
                    Text(
                        text = "Amount: Rs ${fee.amount}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = AcademyGreen
                    )
                    Text(
                        text = "Status: ${if (fee.isPaid) "PAID" else "PENDING DUES"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (fee.isPaid) AcademyGreen else AcademyRedAlert
                    )
                    HorizontalDivider(color = AcademyCardBorder)
                    Text(
                        text = "Principal Awais Mustafa • anas mustafa",
                        fontSize = 11.sp,
                        color = AcademyTextMuted
                    )
                    Text(
                        text = "anas mustafa • 03700880220 | 03129484214",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcademyOchre
                    )
                    if (pdfFile != null) {
                        Text(
                            text = "PDF Generated: ${pdfFile.name} (${pdfFile.length()} bytes)",
                            fontSize = 10.5.sp,
                            color = AcademyGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShareWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                modifier = Modifier.testTag("dialog_whatsapp_share_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = "Share via WhatsApp",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share PDF on WhatsApp")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
