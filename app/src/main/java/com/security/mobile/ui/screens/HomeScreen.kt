package com.security.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.security.mobile.core.models.ScanMode
import com.security.mobile.core.models.ThreatInfo
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var scanState by remember { mutableStateOf("Ready") }
    var threats by remember { mutableStateOf<List<ThreatInfo>>(emptyList()) }
    var scanMode by remember { mutableStateOf(ScanMode.QUICK_SCAN) }
    var isScanning by remember { mutableStateOf(false) }
    var selectedThreat by remember { mutableStateOf<ThreatInfo?>(null) }

    val scanningAnim by remember { Animatable(0f) }
    val scanProgress by remember { derivedStateOf { scanningAnim.value } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mobile Security Scanner",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ANIMATED HEADER - Rotates and scales
                AnimatedHeader()

                Spacer(Modifier.height(24.dp))

                // SCAN MODE SELECTOR - Animated dropdown
                ScanModeSelector(
                    selectedMode = scanMode,
                    onModeSelected = { scanMode = it }
                )

                Spacer(Modifier.height(24.dp))

                // SCAN BUTTON - With loading animation and progress
                ScanButton(
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    onClick = {
                        isScanning = true
                        scanState = "Scanning... Please wait"

                        scanningAnim.animateTo(1f, animationSpec = tween(3000))
                        delay(100)

                        scanState = "Scan Complete"
                        isScanning = false

                        // Sample threats for demonstration
                        threats = listOf(
                            ThreatInfo(
                                packageName = "com.example.suspicious1",
                                appName = "Photo Editor Pro",
                                severity = ThreatInfo.Severity.HIGH,
                                description = "Has excessive permissions: SMS, Contacts, Location",
                                threatType = ThreatInfo.ThreatType.SPYWARE,
                                action = "uninstall"
                            )
                        )
                    }
                )

                // ANIMATED STATUS TEXT
                AnimatedStatusText(scanState)

                // STATISTICS CARDS
                if (!isScanning) {
                    StatisticsCards(threatCount = threats.size)
                }

                // ANIMATED THREAT LIST
                if (threats.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Detected Threats (${threats.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(threats) { threat ->
                            AnimatedThreatCard(
                                threat = threat,
                                onClick = { selectedThreat = threat },
                                onDelete = { /* Remove threat */ }
                            )
                        }
                    }
                }
            }

            // FLOATING ACTION BUTTON
            if (!isScanning) {
                FloatingActionButton(
                    onClick = {
                        isScanning = true
                        scanState = "Quick Scanning..."
                        scanState = "Scan Complete"
                        isScanning = false
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Quick Scan")
                }
            }
        }
    }

    // THREAT DETAIL DIALOG
    if (selectedThreat != null) {
        ThreatDetailDialog(
            threat = selectedThreat!!,
            onDismiss = { selectedThreat = null },
            onAction = { /* Handle action */ }
        )
    }
}

// ────────────────────────────────────────────────────────────────
// ANIMATION FUNCTIONS
// ────────────────────────────────────────────────────────────────

@Composable
fun AnimatedHeader() {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteTransition.repeatable(
            iterations = AnimationSpec.InfiniteRepeatableSpec,
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = RoundedCornerShape(50)
            )
            .graphicsLayer {
                rotationZ = offset * 360f
                scaleX = 1f + 0.05f * kotlin.math.sin(offset * 360f * 3.14159f / 180f)
                scaleY = scaleX
            },
            contentAlignment = Alignment.Center
        ) {
        Icon(
            Icons.Default.Shield,
            contentDescription = "Security",
            modifier = Modifier.size(50.dp),
            tint = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanModeSelector(
    selectedMode: ScanMode,
    onModeSelected: (ScanMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select Scan Mode",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = when (selectedMode) {
                    ScanMode.QUICK_SCAN -> "Quick Scan"
                    ScanMode.DEEP_SCAN -> "Deep Scan"
                    ScanMode.SECURITY_AUDIT -> "Security Audit"
                },
                onValueChange = {},
                readOnly = true,
                label = { Text("Scan Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .dropdownMenu()
                    .width In(250.dp)
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ScanMode.values().forEach { mode ->
                DropdownMenuItem(
                    onClick = {
                        onModeSelected(mode)
                        expanded = false
                    },
                    text = {
                        Text(
                            when (mode) {
                                ScanMode.QUICK_SCAN -> "Quick Scan (30s)"
                                ScanMode.DEEP_SCAN -> "Deep Scan (2-5 min)"
                                ScanMode.SECURITY_AUDIT -> "Security Audit (5-10 min)"
                            },
                            fontWeight = if (mode == selectedMode) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ScanButton(
    isScanning: Boolean,
    scanProgress: Float,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isScanning,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isScanning) {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "${(scanProgress * 100).toInt()}%",
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Text(
                    "Scan Now",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun AnimatedStatusText(scanState: String) {
    val pastelColor = when {
        scanState.contains("Complete", ignoreCase = true) -> MaterialTheme.colorScheme.tertiary
        scanState.contains("Scanning", ignoreCase = true) -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    AnimatedVisibility(
        visible = scanState.isNotEmpty(),
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Text(
            scanState,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = pastelColor
        )
    }
}

@Composable
fun StatisticsCards(threatCount: Int) {
    val stats = listOf(
        Triple("Apps Scanned", "45", Color(0xFF4CAF50)),
        Triple("Threats Found", threatCount.toString(), Color(0xFFF44336)),
        Triple("Scan Time", "~45s", Color(0xFF2196F3))
    )

    Row(
        modifier = Modifier.scrollable(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { (title, value, color) ->
            StatCard(title = title, value = value, color = color)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AnimatedThreatCard(
    threat: ThreatInfo,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val alpha by animateFloatAsState(targetValue = 1f)

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(),
        exit = shrinkOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .alpha(alpha),
            colors = CardDefaults.cardColors(
                containerColor = when (threat.severity) {
                    ThreatInfo.Severity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                    ThreatInfo.Severity.HIGH -> MaterialTheme.colorScheme.secondaryContainer
                    ThreatInfo.Severity.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                    ThreatInfo.Severity.LOW -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(threat.appName, fontWeight = FontWeight.Bold)
                    Text(threat.description, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Chip(
                        label = { Text(threat.severity.name) },
                        colors = ChipDefaults.chipColors(
                            containerColor = when (threat.severity) {
                                ThreatInfo.Severity.CRITICAL -> MaterialTheme.colorScheme.error;
                                ThreatInfo.Severity.HIGH -> MaterialTheme.colorScheme.secondary;
                                ThreatInfo.Severity.MEDIUM -> MaterialTheme.colorScheme.tertiary;
                                ThreatInfo.Severity.LOW -> MaterialTheme.colorScheme.primary;
                            }
                        )
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatDetailDialog(
    threat: ThreatInfo,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onAction()
                onDismiss()
            }) {
                Text("Take Action")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(threat.appName, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Threat Type: ${threat.threatType.name}", fontWeight = FontWeight.Bold)
                Text("Severity: ${threat.severity.name}")
                Text("Description: ${threat.description}")
                Text("Permissions: ${threat.permissions.size} suspicious permissions detected")
            }
        }
    )
}
