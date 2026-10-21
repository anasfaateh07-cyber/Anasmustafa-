package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyDepartments
import com.example.data.FeeRecordEntity
import com.example.data.StudentEntity
import com.example.ui.theme.AcademyBrightGold
import com.example.ui.theme.AcademyCardBorder
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyGreen
import com.example.ui.theme.AcademyMint
import com.example.ui.theme.AcademyOchre
import com.example.ui.theme.AcademyProgressTrack
import com.example.ui.theme.AcademyTextDark
import com.example.ui.theme.AcademyTextMuted
import com.example.ui.theme.MerriweatherFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import kotlin.math.max

data class FeeChartBucket(
    val shortLabel: String,
    val fullLabel: String,
    val collected: Int,
    val pending: Int,
    val target: Int
) {
    val progressFraction: Float
        get() = if (target > 0) (collected.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = (progressFraction * 100f).toInt()
}

@Composable
fun MonthlyFeeCollectionChartCard(
    fees: List<FeeRecordEntity>,
    students: List<StudentEntity>,
    feeCollected: Int,
    pendingFees: Int,
    onOpenFeesTab: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Chart view mode: "DEPARTMENT" (Grouped Bar vs Target) or "MONTHLY" (Area/Trend vs Target)
    var chartMode by rememberSaveable { mutableStateOf("DEPARTMENT") }
    // Target preset: 0 = Auto (from billed/enrolled Room DB), or custom target in PKR
    var customTargetOverride by rememberSaveable { mutableIntStateOf(0) }
    var selectedBucketIndex by remember { mutableIntStateOf(0) }

    val billedTotal = feeCollected + pendingFees
    val enrolledMonthlyFeeSum = remember(students) { students.sumOf { it.monthlyFee } }
    val autoTarget = remember(billedTotal, enrolledMonthlyFeeSum) {
        val raw = max(billedTotal, enrolledMonthlyFeeSum)
        if (raw > 0) raw else 50000
    }
    val effectiveMonthlyTarget = if (customTargetOverride > 0) customTargetOverride else autoTarget

    val overallFraction = remember(feeCollected, effectiveMonthlyTarget) {
        if (effectiveMonthlyTarget > 0) {
            (feeCollected.toFloat() / effectiveMonthlyTarget.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }
    val animatedOverallFraction by animateFloatAsState(
        targetValue = overallFraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "overallFeeProgress"
    )
    val overallPercent = (overallFraction * 100f).toInt()

    // Build Department Buckets from live Room DB
    val departmentBuckets = remember(fees, students, effectiveMonthlyTarget) {
        val defaultDeptShare = max(effectiveMonthlyTarget / 4, 1500)
        listOf(
            Triple("Hifz", AcademyDepartments.HIFZ, AcademyDepartments.SUB_QURAN_HIFZ),
            Triple("Nazra", AcademyDepartments.NAZRA, AcademyDepartments.SUB_NAZRA),
            Triple("Tuition", AcademyDepartments.TUITION_BOYS, AcademyDepartments.SUB_TUITION_BOYS),
            Triple("Playgroup", AcademyDepartments.GIRLS_PLAYGROUP, AcademyDepartments.SUB_PLAYGROUP)
        ).map { (shortName, deptTitle, subGroupKey) ->
            val deptFees = fees.filter {
                AcademyDepartments.matchesSubGroup(it.department, subGroupKey) ||
                    it.department.equals(deptTitle, ignoreCase = true)
            }
            val deptStudents = students.filter {
                AcademyDepartments.matchesSubGroup(it.department, subGroupKey) ||
                    it.department.equals(deptTitle, ignoreCase = true)
            }
            val collected = deptFees.filter { it.isPaid }.sumOf { it.amount }
            val pending = deptFees.filter { !it.isPaid }.sumOf { it.amount }
            val billedOrEnrolled = max(collected + pending, deptStudents.sumOf { it.monthlyFee })
            val target = if (customTargetOverride > 0) {
                customTargetOverride / 4
            } else if (billedOrEnrolled > 0) {
                billedOrEnrolled
            } else {
                defaultDeptShare
            }
            FeeChartBucket(
                shortLabel = shortName,
                fullLabel = deptTitle,
                collected = collected,
                pending = pending,
                target = target
            )
        }
    }

    // Build Monthly Cycle Buckets from live Room DB
    val monthlyBuckets = remember(fees, effectiveMonthlyTarget) {
        val groupedByCycle = fees.groupBy { it.monthCycle.ifBlank { "October cycle" } }
        val standardCycles = listOf(
            "July cycle" to "Jul",
            "August cycle" to "Aug",
            "September cycle" to "Sep",
            "October cycle" to "Oct"
        )
        val extraCycles = groupedByCycle.keys
            .filterNot { k -> standardCycles.any { it.first.equals(k, ignoreCase = true) } }
            .map { k -> k to k.take(3) }

        (standardCycles + extraCycles).takeLast(5).map { (cycleKey, shortMonth) ->
            val cycleFees = fees.filter { it.monthCycle.equals(cycleKey, ignoreCase = true) }
            val collected = cycleFees.filter { it.isPaid }.sumOf { it.amount }
            val pending = cycleFees.filter { !it.isPaid }.sumOf { it.amount }
            val cycleBilled = collected + pending
            val target = if (cycleBilled > 0 && customTargetOverride == 0) {
                max(cycleBilled, effectiveMonthlyTarget)
            } else {
                effectiveMonthlyTarget
            }
            FeeChartBucket(
                shortLabel = shortMonth,
                fullLabel = cycleKey,
                collected = collected,
                pending = pending,
                target = target
            )
        }
    }

    val activeBuckets = if (chartMode == "DEPARTMENT") departmentBuckets else monthlyBuckets
    val safeSelectedIndex = selectedBucketIndex.coerceIn(0, max(activeBuckets.lastIndex, 0))
    val selectedBucket = activeBuckets.getOrNull(safeSelectedIndex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_fee_collection_chart_card")
            .semantics {
                contentDescription =
                    "Admin Dashboard Monthly Fee Collection Progress Card. Collected Rs $feeCollected of Rs $effectiveMonthlyTarget target ($overallPercent percent)."
            },
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
            // Top Accent Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(AcademyGreen, AcademyBrightGold, AcademyGreen)
                        )
                    )
            )

            // Header Row: Title + Chart Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD8EAE2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Fee Collection Progress",
                            tint = AcademyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Monthly Fee Collection vs Targets",
                            fontFamily = MerriweatherFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp,
                            color = AcademyGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Admin Financial Analytics • Live Room DB",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = AcademyOchre
                        )
                    }
                }
            }

            // KPI Strip + Radial Target Gauge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFAF7EE),
                border = BorderStroke(1.dp, AcademyCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Radial Donut Gauge
                    Box(
                        modifier = Modifier.size(74.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 8.dp.toPx()
                            val diameter = size.minDimension - strokeW
                            val topLeft = Offset(
                                (size.width - diameter) / 2f,
                                (size.height - diameter) / 2f
                            )
                            drawArc(
                                color = AcademyProgressTrack,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(AcademyGreen, AcademyMint, AcademyGold, AcademyGreen)
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedOverallFraction,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallPercent%",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = AcademyGreen
                            )
                            Text(
                                text = "of Goal",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademyTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Collected vs Target Breakdown Numbers
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Collected:",
                                fontSize = 12.sp,
                                color = AcademyTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Rs $feeCollected",
                                fontSize = 13.5.sp,
                                color = AcademyGreen,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("chart_collected_value")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Monthly Target:",
                                fontSize = 12.sp,
                                color = AcademyTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Rs $effectiveMonthlyTarget",
                                fontSize = 13.sp,
                                color = AcademyOchre,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.testTag("chart_target_value")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val remainingGap = max(effectiveMonthlyTarget - feeCollected, pendingFees)
                            Text(
                                text = "Pending / To Target:",
                                fontSize = 12.sp,
                                color = AcademyTextMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Rs $remainingGap",
                                fontSize = 12.5.sp,
                                color = AcademyTextDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Mode Switcher & Target Presets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = chartMode == "DEPARTMENT",
                    onClick = {
                        chartMode = "DEPARTMENT"
                        selectedBucketIndex = 0
                    },
                    label = { Text("By Department", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = "By Department Chart",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("chart_mode_department")
                )
                FilterChip(
                    selected = chartMode == "MONTHLY",
                    onClick = {
                        chartMode = "MONTHLY"
                        selectedBucketIndex = activeBuckets.lastIndex.coerceAtLeast(0)
                    },
                    label = { Text("Monthly Cycles", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Timeline,
                            contentDescription = "Monthly Trend Chart",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("chart_mode_monthly")
                )

                listOf(
                    0 to "Auto Target",
                    25000 to "25k Goal",
                    50000 to "50k Goal",
                    100000 to "100k Goal"
                ).forEach { (presetVal, label) ->
                    FilterChip(
                        selected = customTargetOverride == presetVal,
                        onClick = { customTargetOverride = presetVal },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = AcademyGreen, text = "Fee Collected")
                    LegendDot(color = Color(0xFFD8EAE2), text = "Target Bar")
                    LegendDot(color = AcademyGold, text = "Target Line")
                }
                selectedBucket?.let { bucket ->
                    Text(
                        text = "${bucket.shortLabel}: ${bucket.progressPercent}%",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcademyGreen
                    )
                }
            }

            // Interactive Native Compose Chart Canvas (Bar + Area + Target Threshold Line)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFDFBF6),
                border = BorderStroke(1.dp, AcademyCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fee_collection_canvas_container")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    val maxChartValue = remember(activeBuckets) {
                        val peak = activeBuckets.maxOfOrNull { max(it.target, it.collected + it.pending) } ?: 10000
                        max(peak, 2000)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(156.dp)
                            .pointerInput(activeBuckets) {
                                detectTapGestures { tapOffset ->
                                    if (activeBuckets.isNotEmpty() && size.width > 0) {
                                        val slotWidth = size.width.toFloat() / activeBuckets.size
                                        val idx = (tapOffset.x / slotWidth)
                                            .toInt()
                                            .coerceIn(0, activeBuckets.lastIndex)
                                        selectedBucketIndex = idx
                                    }
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val topPad = 14.dp.toPx()
                            val bottomPad = 10.dp.toPx()
                            val chartH = (h - topPad - bottomPad).coerceAtLeast(1f)

                            // 1. Horizontal reference gridlines (25%, 50%, 75%, 100%)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            for (step in 0..3) {
                                val frac = step / 3f
                                val y = topPad + chartH * (1f - frac)
                                drawLine(
                                    color = Color(0xFFE7E0CE),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = if (step in 1..2) dashEffect else null
                                )
                            }

                            if (activeBuckets.isEmpty()) return@Canvas

                            val count = activeBuckets.size
                            val slotWidth = w / count.toFloat()

                            if (chartMode == "DEPARTMENT") {
                                // Grouped / Layered Target vs Collected Bars per Department
                                activeBuckets.forEachIndexed { index, bucket ->
                                    val centerX = slotWidth * index + slotWidth / 2f
                                    val barWidth = (slotWidth * 0.46f).coerceAtMost(42.dp.toPx())
                                    val targetRatio = (bucket.target.toFloat() / maxChartValue.toFloat()).coerceIn(0.06f, 1f)
                                    val collectedRatio = (bucket.collected.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)

                                    val targetBarHeight = chartH * targetRatio
                                    val collectedBarHeight = chartH * collectedRatio
                                    val bottomY = topPad + chartH

                                    // Selection highlight background column
                                    if (index == safeSelectedIndex) {
                                        drawRoundRect(
                                            color = Color(0x14115E27),
                                            topLeft = Offset(centerX - barWidth * 0.75f, topPad - 6.dp.toPx()),
                                            size = Size(barWidth * 1.5f, chartH + 10.dp.toPx()),
                                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                        )
                                    }

                                    // Target backdrop bar
                                    drawRoundRect(
                                        color = Color(0xFFD8EAE2),
                                        topLeft = Offset(centerX - barWidth / 2f, bottomY - targetBarHeight),
                                        size = Size(barWidth, targetBarHeight),
                                        cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                                    )

                                    // Collected foreground bar
                                    if (collectedBarHeight > 0f) {
                                        val safeHeight = max(collectedBarHeight, 5.dp.toPx())
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(AcademyMint, AcademyGreen)
                                            ),
                                            topLeft = Offset(centerX - barWidth / 2f, bottomY - safeHeight),
                                            size = Size(barWidth, safeHeight),
                                            cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                                        )
                                    }

                                    // Golden Target Cap Indicator on top of target bar
                                    val targetCapY = bottomY - targetBarHeight
                                    drawLine(
                                        color = AcademyGold,
                                        start = Offset(centerX - barWidth * 0.6f, targetCapY),
                                        end = Offset(centerX + barWidth * 0.6f, targetCapY),
                                        strokeWidth = 2.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            } else {
                                // MONTHLY CYCLES: Smooth Area + Line Chart against Target Threshold Line
                                val bottomY = topPad + chartH
                                val avgTarget = activeBuckets.map { it.target }.average().toFloat()
                                val targetLineRatio = (avgTarget / maxChartValue.toFloat()).coerceIn(0.08f, 1f)
                                val targetLineY = bottomY - chartH * targetLineRatio

                                // Dashed Golden Target Threshold Line
                                drawLine(
                                    color = AcademyGold,
                                    start = Offset(0f, targetLineY),
                                    end = Offset(w, targetLineY),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )

                                val points = activeBuckets.mapIndexed { index, bucket ->
                                    val x = slotWidth * index + slotWidth / 2f
                                    val ratio = (bucket.collected.toFloat() / maxChartValue.toFloat()).coerceIn(0f, 1f)
                                    val y = bottomY - chartH * ratio
                                    Offset(x, y)
                                }

                                // Soft target comparison bars behind the line
                                activeBuckets.forEachIndexed { index, bucket ->
                                    val centerX = slotWidth * index + slotWidth / 2f
                                    val barW = (slotWidth * 0.32f).coerceAtMost(28.dp.toPx())
                                    val tRatio = (bucket.target.toFloat() / maxChartValue.toFloat()).coerceIn(0.06f, 1f)
                                    val tHeight = chartH * tRatio
                                    drawRoundRect(
                                        color = Color(0xFFE5EFE9),
                                        topLeft = Offset(centerX - barW / 2f, bottomY - tHeight),
                                        size = Size(barW, tHeight),
                                        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                                    )
                                }

                                if (points.size >= 2) {
                                    val areaPath = Path().apply {
                                        moveTo(points.first().x, bottomY)
                                        points.forEach { pt -> lineTo(pt.x, pt.y) }
                                        lineTo(points.last().x, bottomY)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                AcademyGreen.copy(alpha = 0.28f),
                                                AcademyGreen.copy(alpha = 0.03f)
                                            )
                                        )
                                    )

                                    val linePath = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) {
                                            lineTo(points[i].x, points[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = linePath,
                                        color = AcademyGreen,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                points.forEachIndexed { index, pt ->
                                    val isSel = index == safeSelectedIndex
                                    drawCircle(
                                        color = Color.White,
                                        radius = if (isSel) 6.5.dp.toPx() else 5.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = if (isSel) AcademyGold else AcademyGreen,
                                        radius = if (isSel) 4.5.dp.toPx() else 3.2.dp.toPx(),
                                        center = pt
                                    )
                                }
                            }
                        }
                    }

                    // X-Axis Labels Row (Tappable)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        activeBuckets.forEachIndexed { index, bucket ->
                            val isSelected = index == safeSelectedIndex
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedBucketIndex = index }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = bucket.shortLabel,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) AcademyGreen else AcademyTextMuted,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Rs ${bucket.collected}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AcademyOchre else AcademyTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Selected Bucket Callout Banner
            selectedBucket?.let { bucket ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEAF5EC),
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
                                text = bucket.fullLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = AcademyGreen
                            )
                            Text(
                                text = "Collected Rs ${bucket.collected} / Target Rs ${bucket.target} • Pending Rs ${bucket.pending}",
                                fontSize = 11.sp,
                                color = AcademyTextDark
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AcademyGreen
                        ) {
                            Text(
                                text = "${bucket.progressPercent}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.5.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Department Progress Bars Summary
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                departmentBuckets.forEach { dept ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dept.fullLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AcademyTextDark
                            )
                            Text(
                                text = "Rs ${dept.collected} / Rs ${dept.target} (${dept.progressPercent}%)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademyOchre
                            )
                        }
                        LinearProgressIndicator(
                            progress = { dept.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AcademyGreen,
                            trackColor = AcademyProgressTrack
                        )
                    }
                }
            }

            if (onOpenFeesTab != null) {
                HorizontalDivider(color = AcademyCardBorder.copy(alpha = 0.7f))
                OutlinedButton(
                    onClick = onOpenFeesTab,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AcademyGreen),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AcademyGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chart_open_fees_button")
                ) {
                    Text(
                        text = "Open Fee Management & Generate Receipts",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Fees Tab",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = AcademyTextMuted
        )
    }
}
