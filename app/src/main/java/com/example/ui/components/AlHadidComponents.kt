package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BottomNavTab
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyCreamBg
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyNavInactive
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyPatternLine
import com.example.ui.theme.AcademyRedAlert
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AlHadidLogoEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    outerRingColor: Color = Color(0xFFD4A72C),
    showInnerText: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
            .border(width = (size.value * 0.055f).dp, color = outerRingColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // Inner double thin rings matching the Al Hadid emblem
            drawCircle(
                color = AcademyGreen,
                radius = w * 0.41f,
                center = Offset(cx, cy),
                style = Stroke(width = w * 0.022f)
            )
            drawCircle(
                color = outerRingColor,
                radius = w * 0.37f,
                center = Offset(cx, cy),
                style = Stroke(width = w * 0.015f)
            )

            // Mosque Dome in center-top
            val domeBaseY = h * 0.50f
            val domeRadius = w * 0.16f
            drawArc(
                color = AcademyGreen,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(cx - domeRadius, domeBaseY - domeRadius),
                size = Size(domeRadius * 2f, domeRadius * 2f)
            )

            // Spire & Crescent on top of dome
            drawLine(
                color = outerRingColor,
                start = Offset(cx, domeBaseY - domeRadius - h * 0.07f),
                end = Offset(cx, domeBaseY - domeRadius),
                strokeWidth = w * 0.025f
            )
            drawCircle(
                color = outerRingColor,
                radius = w * 0.025f,
                center = Offset(cx, domeBaseY - domeRadius - h * 0.08f)
            )

            // Left & Right Minarets
            val minWidth = w * 0.036f
            val leftMinX = cx - domeRadius - w * 0.055f
            val rightMinX = cx + domeRadius + w * 0.02f
            drawRect(
                color = AcademyGreen,
                topLeft = Offset(leftMinX, h * 0.28f),
                size = Size(minWidth, h * 0.25f)
            )
            drawRect(
                color = AcademyGreen,
                topLeft = Offset(rightMinX, h * 0.28f),
                size = Size(minWidth, h * 0.25f)
            )

            // Mosque base band
            drawRect(
                color = AcademyGreen,
                topLeft = Offset(cx - domeRadius * 1.15f, domeBaseY),
                size = Size(domeRadius * 2.3f, h * 0.055f)
            )

            // Small open golden Quran book under the dome
            val bookPath = Path().apply {
                moveTo(cx, domeBaseY + h * 0.07f)
                lineTo(cx - w * 0.11f, domeBaseY + h * 0.04f)
                lineTo(cx - w * 0.11f, domeBaseY + h * 0.09f)
                lineTo(cx, domeBaseY + h * 0.11f)
                lineTo(cx + w * 0.11f, domeBaseY + h * 0.09f)
                lineTo(cx + w * 0.11f, domeBaseY + h * 0.04f)
                close()
            }
            drawPath(path = bookPath, color = outerRingColor)
        }

        if (showInnerText) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (size.value * 0.13f).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Al Hadid",
                    fontFamily = MerriweatherFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.135f).sp,
                    lineHeight = (size.value * 0.14f).sp,
                    color = AcademyGreen,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Academy",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (size.value * 0.09f).sp,
                    lineHeight = (size.value * 0.10f).sp,
                    color = AcademyOchre,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun IslamicGeometricBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AcademyCreamBg)
            .drawBehind {
                val patternColor = AcademyPatternLine
                val step = 180.dp.toPx()
                val radius = 72.dp.toPx()
                val cols = (size.width / step).toInt() + 2
                val rows = (size.height / step).toInt() + 2

                for (r in 0..rows) {
                    for (c in 0..cols) {
                        val cx = c * step + if (r % 2 == 1) step / 2f else 0f
                        val cy = r * step
                        val path = Path()
                        val points = 16
                        for (i in 0 until points) {
                            val angle = Math.toRadians((i * 360.0 / points))
                            val rad = if (i % 2 == 0) radius else radius * 0.45f
                            val px = cx + (rad * cos(angle)).toFloat()
                            val py = cy + (rad * sin(angle)).toFloat()
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        drawPath(
                            path = path,
                            color = patternColor,
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                    }
                }
            }
    ) {
        content()
    }
}

@Composable
fun AlHadidTopBar(
    alertCount: Int,
    onAlertClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlHadidLogoEmblem(
                    size = 48.dp,
                    outerRingColor = Color(0xFFC59B27)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Al Hadid Academy",
                        fontFamily = MerriweatherFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = AcademyGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nasirabad Jatlan, Azad Kashmir",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AcademyOchre,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Notification Bell with Red Counter Badge
                IconButton(
                    onClick = onAlertClick,
                    modifier = Modifier.testTag("header_notifications_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Academy Alerts",
                            tint = AcademyGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-3).dp)
                                .size(19.dp)
                                .clip(CircleShape)
                                .background(AcademyRedAlert),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alertCount.toString(),
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Red Logout / Exit Button
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("header_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = AcademyRedAlert,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            HorizontalDivider(thickness = 0.8.dp, color = AcademyCardBorder.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun AlHadidBottomNavigationBar(
    selectedTab: BottomNavTab,
    canShowTab: (BottomNavTab) -> Boolean = { true },
    onSelectTab: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            HorizontalDivider(thickness = 0.8.dp, color = AcademyCardBorder.copy(alpha = 0.7f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                BottomNavTab.entries.filter(canShowTab).forEach { tab ->
                    val isSelected = selectedTab == tab
                    val icon: ImageVector = when (tab) {
                        BottomNavTab.HOME -> Icons.Filled.Mosque
                        BottomNavTab.APPROVALS -> Icons.Filled.VerifiedUser
                        BottomNavTab.GROUPS -> Icons.Filled.Diversity3
                        BottomNavTab.STUDENTS -> Icons.Filled.Groups
                        BottomNavTab.FEES -> Icons.Filled.Receipt
                        BottomNavTab.ATTENDANCE -> Icons.Filled.CheckCircle
                        BottomNavTab.QURAN -> Icons.AutoMirrored.Filled.MenuBook
                        BottomNavTab.SCHOOL_SABAQ -> Icons.Filled.School
                        BottomNavTab.BOOKS -> Icons.Filled.AutoStories
                        BottomNavTab.TESTS -> Icons.Filled.Assignment
                        BottomNavTab.MONTHLY_REPORT -> Icons.Filled.Assessment
                    }

                    Column(
                        modifier = Modifier
                            .width(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 2.dp)
                            .testTag("nav_tab_${tab.route}"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(AcademyGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .height(34.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label,
                                    tint = AcademyNavInactive,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tab.label,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.5.sp,
                            lineHeight = 12.sp,
                            color = if (isSelected) AcademyGreen else AcademyNavInactive,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
