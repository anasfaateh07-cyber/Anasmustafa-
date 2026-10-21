package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AcademyDepartments
import com.example.data.StudentEntity
import com.example.ui.AcademyViewModel
import com.example.ui.components.IslamicGeometricBackground
import com.example.ui.screens.StudentProfileDetailScreen
import com.example.ui.theme.MyApplicationTheme

class StudentDetailActivity : ComponentActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val passedStudentId = intent.getIntExtra(EXTRA_STUDENT_ID, -1)

        setContent {
            MyApplicationTheme {
                val viewModel: AcademyViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                val student = uiState.students.firstOrNull { it.id == passedStudentId }
                    ?: uiState.students.firstOrNull()
                    ?: StudentEntity(
                        id = passedStudentId.coerceAtLeast(1),
                        name = "Haider",
                        className = "Class 6",
                        department = AcademyDepartments.TUITION_BOYS,
                        teacherName = "Qari Sahib",
                        monthlyFee = 800
                    )

                IslamicGeometricBackground(modifier = Modifier.fillMaxSize()) {
                    StudentProfileDetailScreen(
                        student = student,
                        schoolSabaqList = uiState.schoolSabaqList,
                        quranLogs = uiState.quranLogs,
                        testRecords = uiState.tests,
                        attendanceRecords = uiState.allAttendance,
                        feeRecords = uiState.fees,
                        monthlyReports = uiState.monthlyReports,
                        isParentRole = uiState.isParent,
                        onLinkParentChild = { viewModel.linkParentToStudent(it) },
                        onEditStudent = { viewModel.updateStudent(it) },
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}
