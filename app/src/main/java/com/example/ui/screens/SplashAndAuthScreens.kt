package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserAccountEntity
import com.example.ui.components.AlHadidLogoEmblem
import com.example.ui.components.IslamicGeometricBackground
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyDarkGreen
import com.example.ui.theme.AcademyDeepSplashGreen
import com.example.ui.theme.AcademyEmeraldTop
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGoldBorder
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyRedAlert
import com.example.ui.theme.AcademySoftGreenText
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

internal fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

// =========================================================================
// 1. NEW V6 SPLASH SCREEN (3 Seconds + Al Hadid Logo + "anas mustafa")
// =========================================================================
@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AcademyDeepSplashGreen,
                        AcademyGreen,
                        AcademyDarkGreen
                    )
                )
            )
            .clickable { onFinished() }
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val goldLine = Color(0x33FACC15)
            val cx = size.width / 2f
            val cy = size.height * 0.42f
            for (ring in 1..3) {
                val radius = ring * 115.dp.toPx()
                val path = Path()
                val points = 16
                for (i in 0 until points) {
                    val angle = Math.toRadians((i * 360.0 / points))
                    val r = if (i % 2 == 0) radius else radius * 0.62f
                    val px = cx + (r * cos(angle)).toFloat()
                    val py = cy + (r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                drawPath(path = path, color = goldLine, style = Stroke(width = 1.5.dp.toPx()))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(18.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(AcademyBrightGold, AcademyGold, AcademyBrightGold)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    color = AcademyBrightGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(126.dp)
                            .clip(CircleShape)
                            .background(Color(0x26FACC15))
                            .padding(9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AlHadidLogoEmblem(
                            size = 108.dp,
                            outerRingColor = AcademyBrightGold
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Al Hadid Academy",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Nasirabad Jatlan, Azad Kashmir • v7.0 Main",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AcademyBrightGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Islamic & Modern Education System\nHifz • Nazra • Tajweed • Tuition (Class 5-8)",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = AcademySoftGreenText,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0A3917),
                    border = BorderStroke(1.2.dp, AcademyGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DEVELOPED BY",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = AcademySoftGreenText
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "anas mustafa",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AcademyBrightGold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "03700880220 | 03129484214",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. NEW V6 WELCOME SCREEN (Urdu + English Toggle + Jamia Announcement)
// =========================================================================
@Composable
fun WelcomeScreen(
    onContinueToAuth: () -> Unit,
    isLoading: Boolean = false
) {
    var isUrduSelected by rememberSaveable { mutableStateOf(true) }

    val urduTitle = "Ahlian-e-Jatlan ke liye Khushkhabri!"
    val urduFullText =
        "Ahlian-e-Jatlan ke liye Khushkhabri! Aane wale naye taleemi saal se Al-Hadid Academy ek Jamia ki shakal ikhtiyar kar legi. Agar aap apne bachon ko deeni aur dunyavi taleem behtareen asatiza aur qualified staff ke zer-e-asar dilwana chahte hain to apne bachon ko Al-Hadid Academy me dakhil karwain. Yahan Quran Hifz, Nazra, Tajweed ke saath School Tuition, English aur Computer ki taleem di jati hai. Saaf mahol, Islami tarbiyat aur jadeed taleem ka behtareen imtizaj hai."
    val urduParagraphs = listOf(
        urduFullText,
        "Aane wale naye taleemi saal se Al-Hadid Academy ek Jamia ki shakal ikhtiyar kar legi.",
        "Agar aap apne bachon ko deeni aur dunyavi taleem behtareen asatiza aur qualified staff ke zer-e-asar dilwana chahte hain to apne bachon ko Al-Hadid Academy me dakhil karwain.",
        "Yahan Quran Hifz, Nazra, Tajweed ke saath School Tuition, English aur Computer ki taleem di jati hai. Saaf mahol, Islami tarbiyat aur jadeed taleem ka behtareen imtizaj hai."
    )

    val englishTitle = "Good News for People of Jatlaan!"
    val englishFullText =
        "Good News for People of Jatlaan! From upcoming new academic year, Al-Hadid Academy will become a Jamia. If you want your children to get both Islamic and modern education under best teachers and qualified staff, enroll in Al-Hadid Academy. Hifz, Nazra, Tajweed along with School Tuition, English and Computer education. Clean environment, Islamic training and modern education blend."
    val englishParagraphs = listOf(
        englishFullText,
        "From upcoming new academic year, Al-Hadid Academy will become a Jamia.",
        "If you want your children to get both Islamic and modern education under best teachers and qualified staff, enroll in Al-Hadid Academy.",
        "Hifz, Nazra, Tajweed along with School Tuition, English and Computer education. Clean environment, Islamic training and modern education blend."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AcademyDeepSplashGreen,
                        AcademyGreen,
                        AcademyDarkGreen
                    )
                )
            )
            .testTag("welcome_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Language Toggle (Urdu / English)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF0A3917),
                    border = BorderStroke(1.2.dp, AcademyBrightGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome_lang_toggle_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isUrduSelected) AcademyBrightGold else Color.Transparent)
                                .clickable { isUrduSelected = true }
                                .padding(vertical = 10.dp)
                                .testTag("btn_welcome_lang_urdu"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = "Urdu Language",
                                    tint = if (isUrduSelected) Color(0xFF1F1905) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Urdu (اردو)",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = if (isUrduSelected) Color(0xFF1F1905) else Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (!isUrduSelected) AcademyBrightGold else Color.Transparent)
                                .clickable { isUrduSelected = false }
                                .padding(vertical = 10.dp)
                                .testTag("btn_welcome_lang_english"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Language,
                                    contentDescription = "English Language",
                                    tint = if (!isUrduSelected) Color(0xFF1F1905) else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "English",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = if (!isUrduSelected) Color(0xFF1F1905) else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Academy Emblem & Header
                AlHadidLogoEmblem(
                    size = 84.dp,
                    outerRingColor = AcademyBrightGold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Al Hadid Academy",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Nasirabad Jatlan, Azad Kashmir",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = AcademyBrightGold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Main Welcome Message Card (Urdu / English)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("welcome_message_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, AcademyBrightGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEAF5EC)
                        ) {
                            Text(
                                text = if (isUrduSelected) urduTitle else englishTitle,
                                fontFamily = MerriweatherFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.5.sp,
                                color = AcademyGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }

                        val activeParagraphs = if (isUrduSelected) urduParagraphs else englishParagraphs
                        activeParagraphs.forEach { para ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AcademyGold)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = para,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    color = AcademyTextDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Continue Button + Small Golden Credit Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onContinueToAuth,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcademyBrightGold,
                        contentColor = Color(0xFF1F1905)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_welcome_continue_login")
                ) {
                    if (isLoading) {
                        Text(
                            text = if (isUrduSelected) "Tasdeeq ki ja rahi hai..." else "Checking session...",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp
                        )
                    } else {
                        Text(
                            text = if (isUrduSelected) {
                                "Agay Barhein (Login / Signup)"
                            } else {
                                "Continue to Login / Signup"
                            },
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Continue"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Thanks for downloading our app - Made by anas mustafa",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AcademyBrightGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("welcome_footer_credit")
                )
            }
        }
    }
}

private data class RoleOption(
    val cardTitle: String,
    val roleValue: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String
)

// =========================================================================
// 3. REAL ONLINE LOGIN / SIGNUP SCREEN (Firebase Auth + Room)
// =========================================================================
@Composable
fun AuthScreen(
    registeredUsers: List<UserAccountEntity> = emptyList(),
    authErrorMessage: String? = null,
    onClearAuthError: () -> Unit = {},
    isFirebaseConfigured: Boolean = false,
    onGoogleSignIn: (activity: Activity, role: String, rememberMe: Boolean) -> Unit = { _, _, _ -> },
    onAuthenticated: (
        role: String,
        name: String,
        email: String,
        phone: String,
        password: String,
        isSignUp: Boolean,
        rememberMe: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val currentActivity = remember(context) { context.findActivity() ?: (context as? Activity) }
    var isLoginMode by rememberSaveable { mutableStateOf(true) }
    var selectedRole by rememberSaveable { mutableStateOf("Teacher") }
    var nameInput by rememberSaveable { mutableStateOf("") }
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    var authError by remember { mutableStateOf("") }

    // Role options (Admin is restricted from public signup)
    val roleCards = listOf(
        RoleOption(
            cardTitle = "As a Teacher",
            roleValue = "Teacher",
            subtitle = "Manage Assigned Groups, Sabaq & Marks",
            icon = Icons.Filled.School,
            tag = "role_card_teacher"
        ),
        RoleOption(
            cardTitle = "As a Parent",
            roleValue = "Parent",
            subtitle = "Search Child ('Pakka Pakka') & Alerts",
            icon = Icons.Filled.FamilyRestroom,
            tag = "role_card_parent"
        ),
        RoleOption(
            cardTitle = "As a Admin",
            roleValue = "Admin",
            subtitle = "Assign Teacher Groups & Full Control",
            icon = Icons.Filled.AdminPanelSettings,
            tag = "role_card_admin"
        ),
        RoleOption(
            cardTitle = "As a Student",
            roleValue = "Student",
            subtitle = "View Quran, Books & 100+100 Marks",
            icon = Icons.Filled.Person,
            tag = "role_card_student"
        )
    )

    IslamicGeometricBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Academy Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, AcademyGoldBorder),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(AcademyEmeraldTop, AcademyDarkGreen)
                            )
                        )
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AlHadidLogoEmblem(
                        size = 70.dp,
                        outerRingColor = AcademyBrightGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Al Hadid Academy",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nasirabad Jatlan, Azad Kashmir • v7.0 Main",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AcademyBrightGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isFirebaseConfigured) Color(0xFFEAF5EC) else Color(0xFFFFF9E6),
                border = BorderStroke(1.dp, if (isFirebaseConfigured) AcademyGreen else AcademyGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFirebaseConfigured) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        contentDescription = "Status",
                        tint = if (isFirebaseConfigured) AcademyGreen else AcademyOchre,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFirebaseConfigured) {
                            "Online Sync Active • Connected to Firebase Backend"
                        } else {
                            "Local Database Active • Authenticating via secure local storage"
                        },
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = if (isFirebaseConfigured) AcademyGreen else AcademyTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Login / Sign Up Segmented Switcher
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, AcademyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLoginMode) AcademyGreen else Color.Transparent)
                            .clickable {
                                isLoginMode = true
                                isPasswordVisible = false
                                authError = ""
                            }
                            .padding(vertical = 10.dp)
                            .testTag("auth_tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isLoginMode) Color.White else AcademyTextDark
                        )
                    }

                    val signUpSelected = !isLoginMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (signUpSelected) AcademyGreen else Color.Transparent)
                            .clickable {
                                isLoginMode = false
                                isPasswordVisible = false
                                authError = ""
                                onClearAuthError()
                                if (selectedRole.equals("Admin", ignoreCase = true) || selectedRole.equals("Principal", ignoreCase = true)) {
                                    selectedRole = "Teacher"
                                }
                            }
                            .padding(vertical = 10.dp)
                            .testTag("auth_tab_signup"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (signUpSelected) Color.White else AcademyTextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Login with Google (Online Google Sign-In with CredentialManager)
            Surface(
                onClick = {
                    val act = currentActivity ?: context.findActivity()
                    if (act != null) {
                        onGoogleSignIn(act, selectedRole, rememberMe)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, AcademyGold),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_auth_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 13.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, AcademyGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AcademyGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isLoginMode) "Login with Google Account" else "Sign up with Google Account",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AcademyTextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Role Selection Header (Admin is strictly login-only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLoginMode) "Select Role to Login" else "Select Role (Teacher / Parent / Student)",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = AcademyGreen
                )

                if (isLoginMode) {
                    Surface(
                        onClick = { selectedRole = "Principal" },
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedRole == "Principal") AcademyGold else Color.White,
                        border = BorderStroke(1.dp, AcademyGold),
                        modifier = Modifier.testTag("role_card_principal")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = "Principal Role",
                                tint = if (selectedRole == "Principal") AcademyTextDark else AcademyOchre,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Principal",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademyTextDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role Cards Grid (Admin excluded from public signup)
            val visibleRoleCards = remember(isLoginMode) {
                if (isLoginMode) {
                    roleCards
                } else {
                    roleCards.filter { it.roleValue != "Admin" }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                visibleRoleCards.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { option ->
                            val isSelected = selectedRole.equals(option.roleValue, ignoreCase = true)
                            Card(
                                onClick = { selectedRole = option.roleValue },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(option.tag),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFEAF5EC) else Color.White
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) AcademyGreen else AcademyCardBorder
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
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
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) AcademyGreen else Color(0xFFD8EAE2)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = option.icon,
                                                contentDescription = option.cardTitle,
                                                tint = if (isSelected) Color.White else AcademyGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Selected Role",
                                                tint = AcademyGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = option.cardTitle,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AcademyGreen
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = option.subtitle,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp,
                                        color = AcademyTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Login / Signup with Email + Password Card (Room DB Users Table)
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email & Password Auth",
                            tint = AcademyGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLoginMode) {
                                "Login with Email + Password"
                            } else {
                                "Sign Up with Email + Password"
                            },
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = AcademyGreen
                        )
                    }

                    if (selectedRole.equals("Teacher", ignoreCase = true) && !isLoginMode) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEAF5EC)
                        ) {
                            Text(
                                text = "Note: After Teacher signup, Admin can approve & assign your specific Academy Groups (e.g. Hifz Group, Tuition Boys).",
                                fontSize = 11.5.sp,
                                color = AcademyGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            authError = ""
                        },
                        label = { Text("Email Address *") },
                        placeholder = { Text("email@gmail.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcademyGreen,
                            unfocusedBorderColor = AcademyCardBorder
                        )
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            authError = ""
                        },
                        label = { Text(if (isLoginMode) "Password *" else "Create Password *") },
                        placeholder = { Text("Enter password") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = AcademyGreen
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field"),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AcademyGreen,
                            unfocusedBorderColor = AcademyCardBorder
                        )
                    )

                    // Remember Me Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberMe = !rememberMe }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AcademyGreen,
                                uncheckedColor = AcademyCardBorder
                            ),
                            modifier = Modifier.testTag("cbRememberMe")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Remember Me (Keep session on app restart)",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = AcademyGreen
                        )
                    }

                    val effectiveError = (authErrorMessage ?: "").ifBlank { authError }
                    if (effectiveError.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, AcademyRedAlert),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Error",
                                    tint = AcademyRedAlert,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = effectiveError,
                                    color = AcademyRedAlert,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val trimmedEmail = emailInput.trim()
                            if (trimmedEmail.isBlank()) {
                                authError = "Please enter your Email Address"
                            } else if (passwordInput.isBlank()) {
                                authError = "Please enter your Password"
                            } else {
                                authError = ""
                                onClearAuthError()
                                onAuthenticated(
                                    selectedRole,
                                    trimmedEmail.substringBefore("@"),
                                    trimmedEmail,
                                    "",
                                    passwordInput,
                                    !isLoginMode,
                                    rememberMe
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AcademyGreen)
                    ) {
                        Text(
                            text = if (isLoginMode) {
                                "Login As $selectedRole"
                            } else {
                                "Sign Up As $selectedRole"
                            },
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Made by anas mustafa • 03700880220 | 03129484214",
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = AcademyOchre,
                textAlign = TextAlign.Center
            )
        }
    }
}
