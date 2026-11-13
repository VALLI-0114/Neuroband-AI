package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BiometricViewModel
import com.example.ui.components.GlassmorphicCard
import kotlinx.coroutines.delay

@Composable
fun BreathingScreen(
    viewModel: BiometricViewModel,
    innerPadding: PaddingValues
) {
    val scans by viewModel.allStressScans.collectAsStateWithLifecycle(initialValue = emptyList())
    val sessions by viewModel.allMindfulnessSessions.collectAsStateWithLifecycle(initialValue = emptyList())

    val isDark = viewModel.isDarkTheme
    val primaryTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    var activeBreathingTitle by remember { mutableStateOf<String?>(null) }
    var activeBreathingDuration by remember { mutableStateOf(5) }
    var showInferenceResult by remember { mutableStateOf(false) }

    // When a scan completes, automatically trigger visual show of completion card
    LaunchedEffect(viewModel.isScanning) {
        if (!viewModel.isScanning && viewModel.scanProgress >= 0.99f) {
            showInferenceResult = true
        }
    }

    // Sub-tab selection state
    var selectedSubTab by remember { mutableStateOf("Diagnostics") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 92.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Core Section Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NEUROBAND RETREAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Parasympathetic Hub",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryTextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trigger calming vagus nerve stimulation through targeted biometrics, restorative body poses, and adrenal diets.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Sub Tab Switcher Buttons (Glassmorphic Selection Chips)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.95f), RoundedCornerShape(14.dp))
                    .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Triple("Diagnostics", Icons.Rounded.Fingerprint, "SCAN"),
                    Triple("Yoga", Icons.Rounded.SelfImprovement, "YOGA"),
                    Triple("Diet", Icons.Rounded.Restaurant, "DIET")
                ).forEach { (tabId, icon, label) ->
                    val isSelected = selectedSubTab == tabId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedSubTab = tabId }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        when (selectedSubTab) {
            "Diagnostics" -> {
                // Central Fingerprint Scan Widget
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "fingerprint")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = EaseInOutBack),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )

                            // Pulse glow backdrops
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (viewModel.isScanning) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f * pulseScale) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (viewModel.isScanning) Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                    .clickable { viewModel.startScanning() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Fingerprint,
                                    contentDescription = "Scan target",
                                    tint = if (viewModel.isScanning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(64.dp)
                                )
                            }

                            // Rotating tech circle outline
                            Canvas(modifier = Modifier.size(185.dp)) {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6), Color.Transparent, Color(0xFF06B6D4))
                                    ),
                                    radius = size.minDimension / 2,
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scan Actions Label
                        Text(
                            text = viewModel.scanStatusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (viewModel.isScanning) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewModel.isScanning) {
                            LinearProgressIndicator(
                                progress = viewModel.scanProgress,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(99.dp))
                            )
                        } else {
                            Button(
                                onClick = { viewModel.startScanning() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("INITIATE DIAGNOSTICS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // LATEST SCAN COMPLETED CARD
                item {
                    viewModel.latestCompletedScan?.let { latest ->
                        AnimatedVisibility(
                            visible = showInferenceResult,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                glowColor = Color(0x3322C55E)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF22C55E)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "TinyML Analysis Complete",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 16.sp
                                            )
                                        }
                                        IconButton(onClick = { showInferenceResult = false }) {
                                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = latest.stressScore.toString(),
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "STRESS SCORE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(30.dp)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${latest.confidence}%",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF8B5CF6)
                                            )
                                            Text(
                                                text = "CONFIDENCE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = latest.status,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "Yoga" -> {
                // THEME EXPLANATION (Explains "Center Your Soul" clearly)
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = Color(0x228B5CF6)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Spa,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Understanding 'Center Your Soul'",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Center Your Soul is a neuro-biological aligner combining slow physical stretching postures with haptic-paced diaphragmatic breathing. Holding these restorative poses while aligning with the Smart Band's vibrations directly quietens autonomic nerves, reduces central adrenal fatigue, and lowers cortical stress index under 10 minutes.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // DETAILED INTERACTIVE YOGA PRACTICE LIBRARY
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "STRESS-REDUCTION YOGA GALLERY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )

                        val yogaPoses = listOf(
                            YogaPose(
                                title = "Child's Pose Rest (Balasana)",
                                duration = 5,
                                difficulty = "Beginner",
                                cortisolMechanism = "Decompresses low spinal channels and drains excessive adrenaline pooling",
                                step1 = "Kneel on your mat, sit back on your heels, and open knees wide apart",
                                step2 = "Fold forward laying your torso between thighs, stretch hands forward on floor",
                                step3 = "Close your eyes, relax your jaw, and let the band vibration pace your deep inhalations"
                            ),
                            YogaPose(
                                title = "Grounded Tree Pose (Vrikshasana)",
                                duration = 8,
                                difficulty = "Moderate",
                                cortisolMechanism = "Establishes intense physical poise & quietens busy cerebral beta-wave chatter",
                                step1 = "Stand tall, and shift your entire bodyweight onto your right standing leg",
                                step2 = "Place your left inner foot high against your right thigh (avoiding knee joint area)",
                                step3 = "Press palms together in front of heart, focusing eyes on a still point while breathing"
                            ),
                            YogaPose(
                                title = "Cat-Cow Flex (Marjaryasana)",
                                duration = 5,
                                difficulty = "Beginner",
                                cortisolMechanism = "Re-aligns central vertebrae and coordinates motion with vagal nerve pacing",
                                step1 = "Get down on all fours with hands directly under shoulders and knees under hips",
                                step2 = "Inhale: Arch spine downward and lift chest and tailbone high toward ceiling",
                                step3 = "Exhale: Round spine upward toward ceiling, drawing chin closely down toward chest"
                            ),
                            YogaPose(
                                title = "Full Corpse Savasana",
                                duration = 10,
                                difficulty = "Casual",
                                cortisolMechanism = "Total neurological downregulation restoring standard vagus balance",
                                step1 = "Lie completely flat on your back, letting legs and arms drop out relaxed",
                                step2 = "Ensure palms face upwards, close eyes, and consciously soften all body muscles",
                                step3 = "Let go of focused thoughts entirely, turning mental focus to band haptic ripples"
                            )
                        )

                        yogaPoses.forEach { pose ->
                            var expandedPoseDetails by remember { mutableStateOf(false) }

                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.SelfImprovement,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1.1f)) {
                                            Text(
                                                text = pose.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Schedule,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${pose.duration} MIN",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFF8B5CF6).copy(alpha = 0.15f), shape = RoundedCornerShape(99.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = pose.difficulty.uppercase(),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF8B5CF6)
                                                    )
                                                }
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { expandedPoseDetails = !expandedPoseDetails }) {
                                                Icon(
                                                    imageVector = if (expandedPoseDetails) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                                    contentDescription = "Toggle details",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Button(
                                                onClick = {
                                                    activeBreathingTitle = pose.title
                                                    activeBreathingDuration = pose.duration
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                contentPadding = PaddingValues(horizontal = 8.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("PLAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    AnimatedVisibility(visible = expandedPoseDetails) {
                                        Column(
                                            modifier = Modifier
                                                .padding(top = 12.dp)
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = "CORTISOL BIO-MECHANICS:",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF8B5CF6),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = pose.cortisolMechanism,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )

                                            Text(
                                                text = "STEP-BY-STEP FLOW:",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = "1. ${pose.step1}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "2. ${pose.step2}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "3. ${pose.step3}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "Diet" -> {
                // DIET COMPANION SUMMARY CARD
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = Color(0x2210B981)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Eco,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Anti-Cortisol Nutrition Science",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Certain food types are clinically proven to block cortisol spike receptors. Incorporating high metal magnesium trace elements, antioxidant amino complexes, and omega-3 polyunsaturated fatty acids protects key cardiac muscle cells during intense study loads.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // NOURISHING DIET PLANS: MORNING, AFTERNOON, & NIGHT
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "STRESS-REDUCTION MEAL PLAN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )

                        // Morning Plan
                        DietPeriodCard(
                            period = "Morning (Dawn Activation)",
                            menu = "Magnesium Zinc Oatmeal + Calming L-Theanine Green Tea",
                            bioMechanism = "L-Theanine crosses blood-brain pathways to immediately plug over-excited glutamate sensors, soothing racing focus vectors without inducing physical drowsiness.",
                            nutrients = "Magnesium: 140mg • L-Theanine: 220mg",
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Afternoon Plan
                        DietPeriodCard(
                            period = "Afternoon (Noon Focus Stabilization)",
                            menu = "Coastal Baked Salmon Fillet + Avocado & Grilled Baby Spinach Salad",
                            bioMechanism = "DHA Omega-3 oils bolster cardiac vessel elasticities during study pressure. Avocado lipids secure blood sugar levels against erratic energy crashes.",
                            nutrients = "Omega-3: 1.8g • Zinc: 24mg • Potassium: 650mg",
                            color = Color(0xFF8B5CF6)
                        )

                        // Night Plan
                        DietPeriodCard(
                            period = "Night (Deep Restorative Induction)",
                            menu = "Carbohydrate Tryptophan Sweet Potato + Roasted Turkey Breast",
                            bioMechanism = "Tryptophan serves as chemical building blocks for serotonin & deep sleep sleep melatonin. Starchy sweep potatoes stimulate insulin-released melatonin uptake.",
                            nutrients = "Tryptophan: 380mg • Fiber: 6g • Vitamin B6: 1.2mg",
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        // Your real session results (from saved sessions)
        item {
            val measured = sessions.filter { it.heartRateReduction != 0 } // 0 = no HR reading or no change
            val avgDrop = if (measured.isEmpty()) null else measured.map { it.heartRateReduction }.average().toInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                avgDrop == null -> "--"
                                avgDrop >= 0 -> "-$avgDrop bpm"
                                else -> "+${-avgDrop} bpm"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (avgDrop == null) "HR CHANGE (NO DATA YET)" else "AVG HR CHANGE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(
                            imageVector = Icons.Rounded.SelfImprovement,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${sessions.size}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SESSIONS • ${sessions.sumOf { it.durationMin }} MIN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }

    // Guided session: runs for the pose's real duration and measures heart rate before/after.
    if (activeBreathingTitle != null) {
        val totalSeconds = activeBreathingDuration * 60
        var secondsPassed by remember { mutableStateOf(0) }
        var breathingStateText by remember { mutableStateOf("Get comfortable...") }
        var isCompletedDialogue by remember { mutableStateOf(false) }
        var startHr by remember { mutableStateOf<Int?>(null) }
        var endHr by remember { mutableStateOf<Int?>(null) }
        val breatheScale = remember { Animatable(0.45f) }

        fun finish() {
            if (isCompletedDialogue) return
            endHr = viewModel.currentHeartRate()
            isCompletedDialogue = true
        }

        // Session timer
        LaunchedEffect(activeBreathingTitle) {
            startHr = viewModel.currentHeartRate()
            while (secondsPassed < totalSeconds && !isCompletedDialogue) {
                delay(1000)
                secondsPassed++
            }
            finish()
        }

        // Breath pacing: inhale 4s, hold 4s, exhale 6s, with a phone buzz on each inhale/exhale
        LaunchedEffect(isCompletedDialogue) {
            while (!isCompletedDialogue) {
                breathingStateText = "Inhale slowly..."
                viewModel.breathCue()
                breatheScale.animateTo(1f, tween(4000, easing = EaseInOutCubic))
                breathingStateText = "Hold..."
                delay(4000)
                breathingStateText = "Exhale slowly..."
                viewModel.breathCue()
                breatheScale.animateTo(0.45f, tween(6000, easing = EaseInOutCubic))
            }
        }

        AlertDialog(
            // Tapping outside shouldn't throw away a 10-minute session
            onDismissRequest = { if (isCompletedDialogue) activeBreathingTitle = null },
            title = {
                Text(
                    text = activeBreathingTitle ?: "Active Breathwork",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isCompletedDialogue) {
                        Text(
                            text = if (viewModel.vibrationGuidanceEnabled) "PHONE BUZZES ON EACH BREATH" else "FOLLOW THE CIRCLE",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    3.dp, Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, Color(0xFF8B5CF6))
                                    ), CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(breatheScale.value)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            )
                            Icon(
                                imageVector = Icons.Rounded.Air,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = breathingStateText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val left = totalSeconds - secondsPassed
                        Text(
                            text = "%d:%02d left".format(left / 60, left % 60) +
                                (startHr?.let { " • Start HR $it BPM" } ?: " • No HR reading"),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = secondsPassed / totalSeconds.toFloat(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Session Complete",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sessionResultText(startHr, endHr, secondsPassed),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (isCompletedDialogue) {
                    Button(
                        onClick = {
                            val s = startHr
                            val e = endHr
                            viewModel.completeMindfulnessSession(
                                activeBreathingTitle ?: "Relax Drill",
                                ((secondsPassed + 30) / 60).coerceAtLeast(1),
                                if (s != null && e != null) s - e else null
                            )
                            activeBreathingTitle = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save & Close", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = { finish() }) {
                        Text("Finish now", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { activeBreathingTitle = null }) {
                    Text(if (isCompletedDialogue) "Discard" else "Cancel", color = Color(0xFFEF4444))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/** Honest summary of the measured heart-rate change for the session result. */
fun sessionResultText(startHr: Int?, endHr: Int?, seconds: Int): String {
    val time = "You practiced for %d:%02d.".format(seconds / 60, seconds % 60)
    if (startHr == null || endHr == null) {
        return "$time No heart-rate reading was available, so the change couldn't be measured. Keep a finger on the band's sensor next time."
    }
    val drop = startHr - endHr
    return time + " " + when {
        drop > 0 -> "Your heart rate dropped by $drop BPM ($startHr → $endHr)."
        drop == 0 -> "Your heart rate stayed steady at $endHr BPM."
        else -> "Your heart rate rose by ${-drop} BPM ($startHr → $endHr). Try a slower, longer exhale next time."
    }
}

data class YogaPose(
    val title: String,
    val duration: Int,
    val difficulty: String,
    val cortisolMechanism: String,
    val step1: String,
    val step2: String,
    val step3: String
)

@Composable
fun DietPeriodCard(
    period: String,
    menu: String,
    bioMechanism: String,
    nutrients: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = period.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Cortisol Defense Active",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = menu,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = bioMechanism,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = nutrients,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
