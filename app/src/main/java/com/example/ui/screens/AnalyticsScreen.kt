package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BiometricViewModel
import com.example.ui.components.GlassmorphicCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalyticsScreen(
    viewModel: BiometricViewModel,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val latestLog by viewModel.latestLog.collectAsStateWithLifecycle(initialValue = null)

    // Interactive circadian heatmap selection state
    var selectedDayName by remember { mutableStateOf<String?>("Tuesday") }
    var selectedHourName by remember { mutableStateOf<String?>("Noon (12PM)") }
    var selectedCellStressScore by remember { mutableStateOf<Int?>(88) }
    var selectedCellTriggerReason by remember { mutableStateOf<String?>("Midterm Exam Session") }

    // Interactive predictive hour timeline selection index
    var selectedPredictiveHourIndex by remember { mutableStateOf(1) } // Default to 12 PM (node index 1)

    val sections = listOf(
        "Core Dashboard",
        "Analytics",
        "Cause Analysis",
        "Prediction",
        "Student Intel",
        "Sleep Analytics",
        "Burnout Dashboard",
        "PDF Reports"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // App Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = innerPadding.calculateTopPadding() + 16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Wearable Bio-Link",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF06B6D4),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Stress Intelligence",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Stream,
                            contentDescription = "Signal Status",
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Scrollable sub-navigation chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sections.forEach { section ->
                        val isSelected = viewModel.selectedDashboardSection == section
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(
                                    if (isSelected) Color(0xFF06B6D4) else Color(0xFF1E293B).copy(alpha = 0.6f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF06B6D4) else Color(0xFF334155),
                                    RoundedCornerShape(99.dp)
                                )
                                .clickable { viewModel.selectedDashboardSection = section }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = section,
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 92.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (viewModel.selectedDashboardSection) {
                "Core Dashboard" -> {
                    // LARGE STRESS RING CARD (78% stress level, High Stress)
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = Color(0x33EF4444)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.1f)) {
                                    Text(
                                        "STRESS INTENSITY",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Level: High Stress",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFEF4444)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Active heart rhythm stability is decreased. Recommended breathing interval due soon.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 16.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .weight(0.9f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(80.dp)) {
                                        drawCircle(
                                            color = Color(0xFF1E293B),
                                            radius = size.minDimension / 2,
                                            style = Stroke(width = 6.dp.toPx())
                                        )
                                        drawArc(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFFEF4444), Color(0xFFF97316))
                                            ),
                                            startAngle = -90f,
                                            sweepAngle = 280f, // 78% of 360f
                                            useCenter = false,
                                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "78%",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text("ACUTE", fontSize = 8.sp, color = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }

                    // KPI CARDS GRID (HR, HRV, Sleep Quality, Burnout Risk, Focus Score, Mental Fatigue, Recovery, Activity Level)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "REAL-TIME VECTORS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )

                            // Grid Rows
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KpiCard(modifier = Modifier.weight(1f), title = "Heart Rate", value = "72 BPM", desc = "Optimal Baseline", icon = Icons.Rounded.Favorite, tint = Color(0xFF06B6D4))
                                KpiCard(modifier = Modifier.weight(1f), title = "HRV (Cardiac)", value = "58 ms", desc = "Stable Stability", icon = Icons.Rounded.MonitorHeart, tint = Color(0xFF8B5CF6))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KpiCard(modifier = Modifier.weight(1f), title = "Sleep Quality", value = "81%", desc = "8h 12m Duration", icon = Icons.Rounded.Bedtime, tint = Color(0xFF3B82F6))
                                KpiCard(modifier = Modifier.weight(1f), title = "Burnout Risk", value = "43%", desc = "Moderate Threat", icon = Icons.Rounded.Widgets, tint = Color(0xFFEF4444))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KpiCard(modifier = Modifier.weight(1f), title = "Focus Level", value = "68%", desc = "Flow State Link", icon = Icons.Rounded.AutoAwesome, tint = Color(0xFFF59E0B))
                                KpiCard(modifier = Modifier.weight(1f), title = "Mental Fatigue", value = "52%", desc = "Cognitive Load", icon = Icons.Rounded.Psychology, tint = Color(0xFFEC4899))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                KpiCard(modifier = Modifier.weight(1f), title = "Recovery Score", value = "77%", desc = "Highly Receptive", icon = Icons.Rounded.Bolt, tint = Color(0xFF10B981))
                                KpiCard(modifier = Modifier.weight(1f), title = "Activity Level", value = "63%", desc = "8,432 Steps", icon = Icons.Rounded.DirectionsRun, tint = Color(0xFF06B6D4))
                            }
                        }
                    }

                    // REAL-TIME ANALYTICS TREND GRAPHS
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Bio-Sensor Streaming", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("PPG and Skin Conductance Trend", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.Timeline,
                                        contentDescription = null,
                                        tint = Color(0xFF06B6D4)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                ) {
                                    val points = listOf(40f, 65f, 45f, 75f, 90f, 55f, 70f, 30f, 85f)
                                    val widthStep = size.width / (points.size - 1)
                                    val path = Path()

                                    points.forEachIndexed { i, p ->
                                        // Map values to canvas coords
                                        val x = i * widthStep
                                        val y = size.height - (p / 100f * size.height)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }

                                    // Draw line
                                    drawPath(
                                        path = path,
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6))
                                        ),
                                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                                    )

                                    // Draw background area translucent
                                    val areaPath = Path()
                                    areaPath.addPath(path)
                                    areaPath.lineTo(size.width, size.height)
                                    areaPath.lineTo(0f, size.height)
                                    areaPath.close()

                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.15f), Color.Transparent)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                "Analytics" -> {
                    // STRESS ANALYTICS PAGE (Daily, Weekly, Monthly)
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Weekly Stress Density", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Daily averages and active recovery benchmarks", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.BarChart,
                                        contentDescription = null,
                                        tint = Color(0xFF06B6D4)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Weekly bar chart representation
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val weeklyData = listOf(68, 72, 85, 42, 60, 78, 55)
                                    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                                    weeklyData.forEachIndexed { i, value ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.5f)
                                                    .fillMaxHeight(value / 100f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        brush = Brush.verticalGradient(
                                                            colors = if (value > 75) {
                                                                listOf(Color(0xFFEF4444), Color(0xFFF97316))
                                                            } else {
                                                                listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
                                                            }
                                                        )
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(weekdays[i], fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // UNDERSTANDABLE HEATMAP MODULE
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Circadian Stress Matrix", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Click cells to inspect specific stress triggers", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.GridOn,
                                        contentDescription = null,
                                        tint = Color(0xFF06B6D4)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Labeled Matrix headers
                                val columns = listOf("Morning", "Noon", "Evening", "Night")
                                val weekdaysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                val matrixData = listOf(
                                    listOf(25, 62, 75, 40), // Mon
                                    listOf(30, 88, 45, 82), // Tue
                                    listOf(55, 78, 22, 18), // Wed
                                    listOf(15, 42, 60, 88), // Thu
                                    listOf(40, 71, 85, 30), // Fri
                                    listOf(20, 25, 35, 52), // Sat
                                    listOf(12, 18, 28, 24)  // Sun
                                )
                                val triggerReasons = listOf(
                                    listOf("Morning study review", "Laboratory research task", "Assignment rush", "Evening rest"),
                                    listOf("Lecture notes review", "Midterm Exam Session", "Coherent breathing", "Late dinner"),
                                    listOf("Class cognitive fatigue", "Research thesis study", "Tree yoga practice", "General deep sleep"),
                                    listOf("Early morning run", "Class discussion", "Exam prep stressor", "Late research session"),
                                    listOf("Morning prep cycle", "Weekly test slot", "Peak workload crunch", "Weekend transition rest"),
                                    listOf("Morning exercise", "Outdoor walking", "Calm breathwork", "Interactive reading"),
                                    listOf("Late sleeping", "Sabbath resting", "Family interaction", "Restorative preparation")
                                )

                                Row {
                                    // Row label buffer
                                    Spacer(modifier = Modifier.width(36.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        columns.forEach { colName ->
                                            Text(
                                                text = colName,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF64748B),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Grid rows
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    matrixData.forEachIndexed { rowIndex, colList ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Row label
                                            Text(
                                                text = weekdaysList[rowIndex],
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF94A3B8),
                                                modifier = Modifier.width(30.dp)
                                            )

                                            colList.forEachIndexed { colIndex, cellVal ->
                                                val cellColor = when {
                                                    cellVal <= 30 -> Color(0xFF06B6D4).copy(alpha = 0.85f) // resting flow
                                                    cellVal <= 64 -> Color(0xFF3B82F6).copy(alpha = 0.85f) // wave alert
                                                    cellVal <= 80 -> Color(0xFF8B5CF6).copy(alpha = 0.85f) // high strain
                                                    else -> Color(0xFFEF4444).copy(alpha = 0.85f) // critical peak
                                                }

                                                val isCellSelected = selectedDayName == weekdaysList[rowIndex] && columns[colIndex] == selectedHourName?.substringBefore(" ")

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1.6f)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(cellColor)
                                                        .border(
                                                            width = if (isCellSelected) 2.dp else 0.dp,
                                                            color = if (isCellSelected) Color.White else Color.Transparent,
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .clickable {
                                                            selectedDayName = weekdaysList[rowIndex]
                                                            selectedHourName = columns[colIndex] + when (colIndex) {
                                                                0 -> " (8AM)"
                                                                1 -> " (12PM)"
                                                                2 -> " (4PM)"
                                                                else -> " (8PM)"
                                                            }
                                                            selectedCellStressScore = cellVal
                                                            selectedCellTriggerReason = triggerReasons[rowIndex][colIndex]
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Clear Heatmap Legend
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    LegendItem(color = Color(0xFF06B6D4), text = "Flow (0-30%)")
                                    LegendItem(color = Color(0xFF3B82F6), text = "Steady (31-60%)")
                                    LegendItem(color = Color(0xFF8B5CF6), text = "Strain (61-80%)")
                                    LegendItem(color = Color(0xFFEF4444), text = "Peak (81-100%)")
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Selected Cell Detail panel (Makes the Heatmap extremely understandable!)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.7f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "CELL DETAILS & ATTRIBUTION",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF06B6D4),
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "${selectedDayName ?: "Tuesday"} at ${selectedHourName ?: "Noon (12PM)"}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            val badgeColor = when {
                                                (selectedCellStressScore ?: 0) <= 30 -> Color(0xFF06B6D4)
                                                (selectedCellStressScore ?: 0) <= 60 -> Color(0xFF3B82F6)
                                                (selectedCellStressScore ?: 0) <= 80 -> Color(0xFF8B5CF6)
                                                else -> Color(0xFFEF4444)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Stress: ${selectedCellStressScore ?: 45}%",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = badgeColor
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Trigger Factors: ${selectedCellTriggerReason ?: "General activity study review"}.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Suggested Remediation: " + when {
                                                (selectedCellStressScore ?: 0) <= 30 -> "Autonomic state optimal. Maintain reading focus."
                                                (selectedCellStressScore ?: 0) <= 60 -> "Steady load. Integrate 5-min Coherent Breathwork soon."
                                                (selectedCellStressScore ?: 0) <= 80 -> "High strain. Perform Tree Yoga Pose (Vrikshasana) + green tea."
                                                else -> "Peak Spike. Immediately stop desk work, execute 10-min Child's Pose rest!"
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Cause Analysis" -> {
                    // CAUSE ANALYSIS PAGE ("Why Am I Stressed?")
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = Color(0x228B5CF6)
                        ) {
                            Column {
                                Text("Why Am I Stressed?", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("AI-driven multi-source cognitive attribution", fontSize = 12.sp, color = Color(0xFF94A3B8))

                                Spacer(modifier = Modifier.height(24.dp))

                                // Donut Chart drawing (Sleep Deficit 70%, Academic Pressure 20%, Environment 10%)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(120.dp)) {
                                        // Sleep Deficit Arc (70% range: from -90 to sweep 252 degrees)
                                        drawArc(
                                            color = Color(0xFF06B6D4),
                                            startAngle = -90f,
                                            sweepAngle = 252f,
                                            useCenter = false,
                                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        // Academic Pressure Arc (20% range: from 162 to sweep 72 degrees)
                                        drawArc(
                                            color = Color(0xFF8B5CF6),
                                            startAngle = 162f,
                                            sweepAngle = 72f,
                                            useCenter = false,
                                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        // Environmental Arc (10% range: from 234 to sweep 36 degrees)
                                        drawArc(
                                            color = Color(0xFF3B82F6),
                                            startAngle = 234f,
                                            sweepAngle = 36f,
                                            useCenter = false,
                                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("70%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Sleep deficit", fontSize = 8.sp, color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Legends
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    LegendItem(color = Color(0xFF06B6D4), text = "Sleep deficit (70%)")
                                    LegendItem(color = Color(0xFF8B5CF6), text = "Academic load (20%)")
                                    LegendItem(color = Color(0xFF3B82F6), text = "Thermal (10%)")
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // AI Reasoning block
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF06B6D4),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "Your cardiac variability (HRV) baseline decreased by 18% over the last 3 days. Correlation with your active calendar reveals sleep duration averages sub-6 hours. Primary trigger: Acute Sleep Deficit.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "Prediction" -> {
                    // STRESS PREDICTION RANGE (Interactive Glassmorphic 24h Line Chart Timeline)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "FUTURE STRESS RISK TIMELINE (24 HOUR FORECAST)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )

                            // Forecasting points representation
                            val forecastingSlots = listOf(
                                ForecastPoint("08:00 AM", 28, 1.8, 72, "Quiet morning study session", "LOW RISK"),
                                ForecastPoint("12:00 PM", 82, 4.3, 38, "Midterm Exam Cognitive Surge", "HIGH RISK"),
                                ForecastPoint("04:00 PM", 45, 2.2, 59, "Post-Exam Active Recuperation", "MODERATE"),
                                ForecastPoint("08:00 PM", 68, 3.4, 48, "Assignment Submission Deadline", "ALERT"),
                                ForecastPoint("12:00 AM", 31, 1.5, 74, "Deep RestSleep Sleep Entry", "LOW RISK"),
                                ForecastPoint("04:00 AM", 15, 0.9, 81, "Circadian Core Sleep Resync", "LOW RISK")
                            )

                            // Glassmorphic interactive line chart
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("24h Stress Forecast Curve", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text("Tap hour pins below to slide forecasting indicators", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                        }
                                        Icon(imageVector = Icons.Rounded.Timeline, contentDescription = null, tint = Color(0xFF06B6D4))
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Core Canvas drawing modern line and shading area graph
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val paddingLeft = 30f
                                            val paddingRight = 30f
                                            val graphWidth = size.width - paddingLeft - paddingRight
                                            val stepWidth = graphWidth / (forecastingSlots.size - 1)
                                            
                                            val mainPath = Path()
                                            val translucentAreaPath = Path()

                                            // Construct curve
                                            forecastingSlots.forEachIndexed { idx, node ->
                                                val x = paddingLeft + (idx * stepWidth)
                                                // Coords: 0 stress score at bottom of height, 100 at top
                                                val y = size.height - (node.stressScore / 100f * (size.height - 20f)) - 10f

                                                if (idx == 0) {
                                                    mainPath.moveTo(x, y)
                                                    translucentAreaPath.moveTo(x, size.height)
                                                    translucentAreaPath.lineTo(x, y)
                                                } else {
                                                    // Draw cubic bezier connections or simple lines
                                                    mainPath.lineTo(x, y)
                                                    translucentAreaPath.lineTo(x, y)
                                                }
                                            }
                                            translucentAreaPath.lineTo(paddingLeft + ((forecastingSlots.size - 1) * stepWidth), size.height)
                                            translucentAreaPath.close()

                                            // 1. Draw glowing gradient backdrop mesh under the line
                                            drawPath(
                                                path = translucentAreaPath,
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), Color.Transparent)
                                                )
                                            )

                                            // 2. Draw Critical threshold line at high stress boundary
                                            val alertY = size.height - (75 / 100f * (size.height - 20f)) - 10f
                                            drawLine(
                                                color = Color(0xFFEF4444).copy(alpha = 0.35f),
                                                start = Offset(0f, alertY),
                                                end = Offset(size.width, alertY),
                                                strokeWidth = 2f,
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                            )

                                            // 3. Draw main forecasting spline curve line
                                            drawPath(
                                                path = mainPath,
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6))
                                                ),
                                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                                            )

                                            // 4. Draw interactive node pins
                                            forecastingSlots.forEachIndexed { idx, node ->
                                                val x = paddingLeft + (idx * stepWidth)
                                                val y = size.height - (node.stressScore / 100f * (size.height - 20f)) - 10f
                                                val isCurrent = idx == selectedPredictiveHourIndex

                                                // Draw base outer circle
                                                drawCircle(
                                                    color = if (isCurrent) Color.White else Color(0xFF0F172A),
                                                    radius = if (isCurrent) 10f else 6f,
                                                    center = Offset(x, y)
                                                )
                                                drawCircle(
                                                    color = if (node.stressScore > 75) Color(0xFFEF4444) else Color(0xFF06B6D4),
                                                    radius = if (isCurrent) 6f else 4f,
                                                    center = Offset(x, y)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Labeled time slider triggers
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        forecastingSlots.forEachIndexed { index, node ->
                                            val isSelected = selectedPredictiveHourIndex == index
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) Color(0xFF06B6D4).copy(alpha = 0.2f) else Color.Transparent
                                                    )
                                                    .clickable { selectedPredictiveHourIndex = index }
                                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = node.hour.substringBefore(" "),
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                    color = if (isSelected) Color(0xFF06B6D4) else Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    // Interactive Forecasting Metric Panel
                                    val currentPoint = forecastingSlots[selectedPredictiveHourIndex]
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "FORECAST DETAIL: ${currentPoint.hour}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF94A3B8),
                                                    letterSpacing = 0.5.sp
                                                )

                                                val riskColor = when (currentPoint.severity) {
                                                    "HIGH RISK" -> Color(0xFFEF4444)
                                                    "ALERT" -> Color(0xFFF97316)
                                                    "MODERATE" -> Color(0xFF8B5CF6)
                                                    else -> Color(0xFF22C55E)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .background(riskColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = currentPoint.severity,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = riskColor
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Stress Load", fontSize = 10.sp, color = Color(0xFF64748B))
                                                    Text("${currentPoint.stressScore}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("Skin GSR", fontSize = 10.sp, color = Color(0xFF64748B))
                                                    Text("${currentPoint.gsr} µS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06B6D4))
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text("HRV Index", fontSize = 10.sp, color = Color(0xFF64748B))
                                                    Text("${currentPoint.hrv} ms", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Text(
                                                text = "Anticipated Event: ${currentPoint.event}.",
                                                fontSize = 11.sp,
                                                color = Color(0xFFCBD5E1)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "Student Intel" -> {
                    // STUDENT INTELLIGENCE PAGE
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = Color(0x228B5CF6)
                        ) {
                            Column {
                                Text("Student Intelligence Intel", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("Attributions relative to class terms & exams", fontSize = 11.sp, color = Color(0xFF94A3B8))

                                Spacer(modifier = Modifier.height(20.dp))

                                // Radial Gauges Columns
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        MiniRadialGauge(label = "Exam Stress", value = 82f, color = Color(0xFFEF4444))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        MiniRadialGauge(label = "Task Burnout", value = 74f, color = Color(0xFFF97316))
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                        MiniRadialGauge(label = "Academic Energy", value = 77f, color = Color(0xFF22C55E))
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Highlight list
                                Text("ACADEMIC INSIGHTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(8.dp))
                                StudentInsightRow("Exam Strain Threshold", "Elevated study cycles trigger 82% cardiac load over baselines.")
                                StudentInsightRow("Sabbatical Recovery Space", "Plan 1 hr post-test breathing scan to prevent emotional fatigue.")
                            }
                        }
                    }
                }

                "Sleep Analytics" -> {
                    // SLEEP ANALYTICS PAGE (Consistency, quality, trends)
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Sleep Chronotype Analysis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Based on accelerometer sleep latency", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    }
                                    Icon(imageVector = Icons.Rounded.Bedtime, contentDescription = null, tint = Color(0xFF06B6D4))
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SleepMetricColumn("Sleep Quality", "81%", Color(0xFF22C55E))
                                    SleepMetricColumn("Consistency", "94% optimal", Color(0xFF06B6D4))
                                    SleepMetricColumn("Deep Sleep", "2h 45m", Color(0xFF8B5CF6))
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Weekly Sleep Duration chart
                                Text("Sleep Duration History", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(10.dp))
                                Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                                    val points = listOf(6.2f, 7.8f, 5.1f, 8.2f, 6.0f, 7.4f, 8.5f)
                                    val widthStep = size.width / (points.size - 1)
                                    val sleepPath = Path()

                                    points.forEachIndexed { i, duration ->
                                        val x = i * widthStep
                                        val y = size.height - (duration / 10f * size.height)
                                        if (i == 0) sleepPath.moveTo(x, y) else sleepPath.lineTo(x, y)
                                    }

                                    drawPath(path = sleepPath, color = Color(0xFF3B82F6), style = Stroke(width = 4f))
                                }
                            }
                        }
                    }
                }

                "Burnout Dashboard" -> {
                    // BURNOUT DASHBOARD
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowColor = Color(0x22EF4444)
                        ) {
                            Column {
                                Text("Predictive Burnout Registry", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Attributions to autonomic system weardown", fontSize = 11.sp, color = Color(0xFF94A3B8))

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1.1f)) {
                                        Text("Burnout Risk Score", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                        Text("43% Risk", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                                        Text("Status: Moderate Fatigue", fontSize = 12.sp, color = Color(0xFFF97316))
                                    }

                                    // Burnout Gauge
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .weight(0.9f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(
                                                color = Color(0xFF1E293B),
                                                startAngle = 135f,
                                                sweepAngle = 270f,
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            drawArc(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(Color(0xFF22C55E), Color(0xFFF97316))
                                                ),
                                                startAngle = 135f,
                                                sweepAngle = 116f, // 43% of 270 degrees
                                                useCenter = false,
                                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Icon(imageVector = Icons.Rounded.Bolt, contentDescription = null, tint = Color(0xFFF97316))
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text("BURNOUT PREVENTION ALGORITHM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Burnout signals suggest elevated parasympathetic strain. Integrate 2 winddown breaths today.", fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 16.sp)
                            }
                        }
                    }
                }

                "PDF Reports" -> {
                    // REPORTS PAGE (Daily, Weekly, Monthly)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                "GENERATED LOG REGISTRY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )

                            ReportFileRow("NeuroBand Daily PDF Summary", "June 21, 2026", "PDF (1.2 MB)")
                            ReportFileRow("Comprehensive Weekly Stress Audit", "June 14 - June 20, 2026", "CSV (345 KB)")
                            ReportFileRow("Investor AI Telemetry Logs", "Monthly dataset v1.4", "PDF (4.8 MB)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 9.sp,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 10.sp, color = Color(0xFFE2E8F0))
    }
}

@Composable
fun PredictionRiskRow(title: String, risk: String, color: Color, reason: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(title, fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(reason, fontSize = 11.sp, color = Color(0xFF64748B))
            }
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = risk,
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun MiniRadialGauge(label: String, value: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = -225f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -225f,
                    sweepAngle = 270f * (value / 100f),
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                "${value.toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StudentInsightRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.School,
            contentDescription = null,
            tint = Color(0xFF8B5CF6),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(desc, fontSize = 11.sp, color = Color(0xFF94A3B8), lineHeight = 15.sp)
        }
    }
}

@Composable
fun SleepMetricColumn(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ReportFileRow(title: String, date: String, format: String) {
    val context = LocalContext.current
    var isDownloaded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CloudDownload,
                    contentDescription = null,
                    tint = Color(0xFF06B6D4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("$date • $format", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }

            IconButton(
                onClick = {
                    isDownloaded = true
                    // Determine scope (Daily, Weekly, or Monthly)
                    val scope = when {
                        title.contains("Daily", ignoreCase = true) -> "Daily"
                        title.contains("Weekly", ignoreCase = true) -> "Weekly"
                        else -> "Monthly"
                    }
                    val pdfFile = ReportPdfHelper.generateStressReportPdf(context, scope)
                    if (pdfFile != null) {
                        android.widget.Toast.makeText(context, "$scope report PDF generated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        ReportPdfHelper.sharePdfFile(context, pdfFile)
                    } else {
                        android.widget.Toast.makeText(context, "Error generating report PDF", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDownloaded) Color(0xFF10B981) else Color(0xFF1E293B))
            ) {
                Icon(
                    imageVector = if (isDownloaded) Icons.Rounded.Check else Icons.Rounded.Download,
                    contentDescription = null,
                    tint = if (isDownloaded) Color.White else Color(0xFF06B6D4),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

data class ForecastPoint(
    val hour: String,
    val stressScore: Int,
    val gsr: Double,
    val hrv: Int,
    val event: String,
    val severity: String
)
