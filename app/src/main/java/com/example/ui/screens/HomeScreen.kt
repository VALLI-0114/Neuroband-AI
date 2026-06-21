package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.BiometricViewModel
import com.example.ui.ChatMessage
import com.example.ui.components.GlassmorphicCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BiometricViewModel,
    innerPadding: PaddingValues
) {
    var showDemoDialog by remember { mutableStateOf(false) }
    var isChatOpen by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    // Circular stress ring animation values
    val infiniteTransition = rememberInfiniteTransition(label = "rings")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 80.dp // Leave space for float button
            ),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // HERO SECTION
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Neural stress intelligence badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF06B6D4).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(99.dp)
                            )
                            .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f), RoundedCornerShape(99.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✦ NEURAL STRESS INTELLIGENCE",
                            color = Color(0xFF06B6D4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title
                    Text(
                        text = "NeuroBand AI™",
                        fontSize = 42.sp,
                        lineHeight = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        letterSpacing = (-1.5).sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Detect Stress • Understand Stress\nPredict Stress • Reduce Stress",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6),
                        textAlign = TextAlign.Center,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Description subtitle
                    Text(
                        text = "An AI-Powered Wearable companion that detects stress, explains why it happens, predicts what comes next, and guides users toward better mental wellbeing.",
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Animated Stress Visualization Rings
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .rotate(ringRotation)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(200.dp)) {
                            // Ring 1 - Base outline
                            drawCircle(
                                color = Color(0xFF1E293B),
                                radius = size.minDimension / 2,
                                style = Stroke(width = 8.dp.toPx())
                            )
                            
                            // Ring 2 - Stress levels active circular progress (78%)
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF06B6D4), Color(0xFF8B5CF6))
                                ),
                                startAngle = -90f,
                                sweepAngle = 280f, // ~78%
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Inner Glow Ring 3 - (63% activity level)
                            drawArc(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
                                ),
                                startAngle = 30f,
                                sweepAngle = 226f, // ~63%
                                useCenter = false,
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                                size = size * 0.75f,
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    (size.width - size.width * 0.75f) / 2,
                                    (size.height - size.height * 0.75f) / 2
                                )
                            )
                        }

                        // Static text inside rings
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.rotate(-ringRotation) // negate rotation to keep text static
                        ) {
                            Text(
                                "HEART FLOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF06B6D4)
                            )
                            Text(
                                "78%",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "HIGH STRESS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Buttons CTA Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.selectTab(1) }, // Go to Dashboard analytics
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF06B6D4),
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(99.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Dashboard, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Started", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        OutlinedButton(
                            onClick = { showDemoDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                )
                            ),
                            shape = RoundedCornerShape(99.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Demo icon",
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch Demo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // WEARABLE DEVICE PREVIEW
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = Color(0x228B5CF6)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.DeveloperBoard,
                                    contentDescription = null,
                                    tint = Color(0xFF06B6D4),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Smart ESP32 Hardware Band",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "CONNECTED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF22C55E)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // High fidelity rendering of wearable band using a nice network vector image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0B1120)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://lh3.googleusercontent.com/aida-public/AB6AXuAP8IVs8JjZeFyw0UywYHMChYK0WTFgGcgfnYhE5fxjbimcx1gH7O6gRCVKyuUUo5tZRtBxYeZFhTADP7W7If252yxPDVjYqfN7mLpTE3rCK3Y46xt0INEabK2Ma2AOJz2iSfER796Lys16N4bss9SEqsPfFxQhgkFKLXTl7RGO5a2_xMsCHa0sduvxK7ECytWHHrI1kkILpTZOekwT1ZwIFt9CI2O6Qsc1vAdkI4pCZf3apOFIIkc3DgINSIExZid00TZiBdlAC8c")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Smart Band 3D render",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Packed with MAX30102 PPG pulse oximetry, GSR sweat conductance, MLX90614 contact temperature, MPU6050 accelerometer, and a haptic vibration actuator.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // STATISTICS STRIPE GRID
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "HEALTHCARE GRADE FEEDS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.2.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Active Users", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("12.8K+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📈 +14% this month", fontSize = 9.sp, color = Color(0xFF22C55E))
                            }
                        }

                        // Stat 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Stress Accuracy", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("98.4%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✓ Clinically Verified", fontSize = 9.sp, color = Color(0xFF06B6D4))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat 3
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Burnout Predict", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("94% Rate", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("⚡ TinyML Engines", fontSize = 9.sp, color = Color(0xFF8B5CF6))
                            }
                        }

                        // Stat 4
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF1E293B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text("Daily Insights", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("AI-Driven", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("✦ ChatGPT Core", fontSize = 9.sp, color = Color(0xFF3B82F6))
                            }
                        }
                    }
                }
            }

            // VALUE PROPOSITION CARDS (Apple/WHOOP responsive aesthetic)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "EXPLAINABLE TECH SPECS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 1.2.sp
                    )

                    // Explainable AI Card 1
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = Color(0x118B5CF6)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Insights,
                                        contentDescription = null,
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Explainable Bio-AI",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Correlates PPG, skin temperature, and electrodermal responses to separate fatigue, environmental thermal stress, and mental workload triggers.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Predictive Alerts Card 2
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = Color(0x1106B6D4)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color(0xFF06B6D4).copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        tint = Color(0xFF06B6D4),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Predictive Stress Control",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Forecasts burnout spikes up to 15 minutes before onset by checking micro-instabilities in cardiac pulse patterns.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // FLOATING AI ASSISTANT TRIGGER BUTTON ("Ask NeuroBand AI")
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 92.dp, end = 20.dp)
        ) {
            FloatingActionButton(
                onClick = { isChatOpen = true },
                containerColor = Color(0xFF06B6D4),
                contentColor = Color(0xFF0F172A),
                shape = CircleShape,
                modifier = Modifier.height(54.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ask NeuroBand AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // CHAT ASSISTANT BOTTOM SHEET MODAL (ChatGPT styled conversation)
        if (isChatOpen) {
            ModalBottomSheet(
                onDismissRequest = { isChatOpen = false },
                containerColor = Color(0xFF0F172A),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF475569)) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    // Chat Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "NeuroBand AI Coach",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Text(
                            "LIVE BIO-LINK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF06B6D4)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Messages list
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            state = chatListState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.chatMessages) { msg ->
                                val isCoach = msg.sender == "coach"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .background(
                                                color = if (isCoach) Color(0xFF1E293B) else Color(0xFF06B6D4).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isCoach) 4.dp else 16.dp,
                                                    bottomEnd = if (isCoach) 16.dp else 4.dp
                                                )
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isCoach) Color(0xFF334155) else Color(0xFF06B6D4).copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isCoach) 4.dp else 16.dp,
                                                    bottomEnd = if (isCoach) 16.dp else 4.dp
                                                )
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            color = if (isCoach) Color(0xFFE2E8F0) else Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                            
                            if (viewModel.isThinkingByCoach) {
                                item {
                                    Row(horizontalArrangement = Arrangement.Start) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                "NeuroBand is composing insight...",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat Input box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Ask about your stress factors or sleep quality...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF06B6D4),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF090D16),
                                unfocusedContainerColor = Color(0xFF090D16)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (chatInputText.isNotBlank()) {
                                        viewModel.sendChatMessage(chatInputText)
                                        chatInputText = ""
                                        scope.launch {
                                            delay(100)
                                            chatListState.animateScrollToItem(viewModel.chatMessages.size - 1)
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    viewModel.sendChatMessage(chatInputText)
                                    chatInputText = ""
                                    scope.launch {
                                        delay(100)
                                        chatListState.animateScrollToItem(viewModel.chatMessages.size - 1)
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF06B6D4), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Features Demo Dialog
    if (showDemoDialog) {
        AlertDialog(
            onDismissRequest = { showDemoDialog = false },
            title = {
                Text(
                    "NeuroBand AI™ - Pitch Blueprint",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        "Connected with the smart band prototype, this app demonstrates real-time feedback loops from 5 telemetry streams.",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "• Home Tab: View wearable model, stats, and live Chatbot.\n\n" +
                        "• Dashboard Tab: Click to switch between 10 customized medical and student analytics panels!\n\n" +
                        "• Scan Tab: Press physical scanner simulation for local TinyML stress metrics analysis.\n\n" +
                        "• Insight Tab: Explore action items, BLE connections, and configurations.",
                        fontSize = 13.sp,
                        color = Color(0xFFF1F5F9),
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDemoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06B6D4))
                ) {
                    Text("Enter Ecosystem", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color(0xFF0F172A)
        )
    }
}
