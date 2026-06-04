package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.viewmodel.HotspotViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerPortalTab(
    viewModel: HotspotViewModel,
    packagesList: List<AccessPackageEntity>,
    activeSession: SessionEntity?,
    connectedMac: String,
    connectedDevice: String,
    connectedIp: String,
    notifyLogs: List<HotspotViewModel.NotificationLog>
) {
    var isEditingProfile by remember { mutableStateOf(false) }
    var enteredMac by remember { mutableStateOf(connectedMac) }
    var enteredDevice by remember { mutableStateOf(connectedDevice) }
    var enteredIp by remember { mutableStateOf(connectedIp) }

    var portalSelectionTab by remember { mutableStateOf("PORTAL") } // "PORTAL" or "NOTIFICATIONS"

    Column(modifier = Modifier.fillMaxSize()) {
        // Connected profile indicator banner
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.Yellow.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFD48D00),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Hotspot Portal Gateway",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (activeSession != null) MaterialTheme.colorScheme.success.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (activeSession != null) "AUTHORIZED • ONLINE" else "INTERNET BLOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (activeSession != null) MaterialTheme.colorScheme.success else Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))

                if (isEditingProfile) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = enteredDevice,
                                onValueChange = { enteredDevice = it },
                                label = { Text("Mocked Client Device") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = enteredMac,
                                onValueChange = { enteredMac = it },
                                label = { Text("Client MAC") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = enteredIp,
                            onValueChange = { enteredIp = it },
                            label = { Text("Assigned IP") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateClientDemoProfile(enteredMac, enteredDevice, enteredIp)
                                    isEditingProfile = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply Device Identity")
                            }
                            TextButton(
                                onClick = { isEditingProfile = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Device: $connectedDevice • IP: $connectedIp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Hardware MAC ID: $connectedMac",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Button(
                            onClick = { isEditingProfile = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mock Client Device ID", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Sub tab navigation inside client portal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { portalSelectionTab = "PORTAL" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (portalSelectionTab == "PORTAL") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (portalSelectionTab == "PORTAL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Text("Captive Portal Forms")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { portalSelectionTab = "NOTIFICATIONS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (portalSelectionTab == "NOTIFICATIONS") MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (portalSelectionTab == "NOTIFICATIONS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.wrapContentSize()
            ) {
                Text("Notification Simulator (${notifyLogs.size})")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (portalSelectionTab == "NOTIFICATIONS") {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (notifyLogs.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        ) {
                            Text(
                                text = "No notifications generated. Pay in the Portal below to receive automatic billing updates.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(notifyLogs) { log ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = when(log.type) {
                                                "SMS" -> Color(0xFFE0F2FE)
                                                "WhatsApp" -> Color(0xFFDCFCE7)
                                                else -> Color(0xFFF3E8FF)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(log.type) {
                                            "SMS" -> Icons.Default.Email
                                            "WhatsApp" -> Icons.Default.Phone
                                            else -> Icons.Default.Email
                                        },
                                        contentDescription = null,
                                        tint = when(log.type) {
                                            "SMS" -> Color(0xFF0284C7)
                                            "WhatsApp" -> Color(0xFF16A34A)
                                            else -> Color(0xFF9333EA)
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${log.type} alerts to ${log.recipient}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = log.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PORTAL VIEW
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // If user is connected successfully
                if (activeSession != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Your Connection is Active! 🎉",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val maxDur = activeSession.endTime
                                val remainingMs = maxDur - System.currentTimeMillis()
                                val minRem = (remainingMs / 1000 / 60).coerceAtLeast(0L)

                                val dataQuotaText = if (activeSession.packageName.contains("Lite")) "1024 MB Limit" else "Unlimited"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    val percentTime = if (remainingMs > 0) 0.65f else 0f
                                    UsageGauge(
                                        percentage = percentTime,
                                        label = "Time Timer",
                                        valueString = "$minRem min",
                                        accentColor = MaterialTheme.colorScheme.primary
                                    )

                                    UsageGauge(
                                        percentage = 0.12f,
                                        label = "Data Quota",
                                        valueString = dataQuotaText,
                                        accentColor = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Package Speed Capability:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = "${activeSession.speedLimitMbps} Mbps throttling limit",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.terminateSession(activeSession.id, activeSession.macAddress) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Disconnect")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Portal Login Forms
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Ready to Activate Internet?",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Log in using a purchased prepaid physical card or checkout directly via Cashless Mobile Money integrations to unlock absolute coverage.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                )

                                var enteredVoucherCode by remember { mutableStateOf("") }
                                var operationFeed by remember { mutableStateOf<String?>(null) }
                                var isSucceedVoucher by remember { mutableStateOf(false) }

                                OutlinedTextField(
                                    value = enteredVoucherCode,
                                    onValueChange = { enteredVoucherCode = it },
                                    label = { Text("Prepaid Voucher Pass-Code") },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("E.g. DEMO-ABCDEF") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.Star, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (enteredVoucherCode.isNotEmpty()) {
                                                    viewModel.redeemVoucherInPortal(enteredVoucherCode) { ok, feed ->
                                                        isSucceedVoucher = ok
                                                        operationFeed = feed
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Smt")
                                        }
                                    }
                                )

                                AnimatedVisibility(visible = operationFeed != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .background(
                                                color = if (isSucceedVoucher) MaterialTheme.colorScheme.success.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = operationFeed ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSucceedVoucher) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Mobile Money Direct buying plans
                    item {
                        Text(
                            text = "Buy Access Passports",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (packagesList.isEmpty()) {
                        item {
                            Text(
                                "No plans compiled in active system. Use Admin Console to create a hotspot package.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        items(packagesList) { pkg ->
                            var activeCheckoutScreen by remember { mutableStateOf(false) }

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeCheckoutScreen = !activeCheckoutScreen }
                                    .border(
                                        width = 1.dp,
                                        color = if (activeCheckoutScreen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = pkg.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = pkg.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Text(
                                            text = "$${DecimalFormat("#,##0.00").format(pkg.price)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Speed: Up to ${pkg.speedLimitMbps} Mbps",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Validity: ${pkg.durationMinutes / 60} hrs",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = activeCheckoutScreen) {
                                        var billingPhone by remember { mutableStateOf("") }
                                        var selectedMethod by remember { mutableStateOf("M-Pesa") }
                                        val methods = listOf("M-Pesa", "MTN MoMo", "Stripe Checkout", "PayPal", "Card Payment")
                                        var checkoutStatus by remember { mutableStateOf<String?>(null) }
                                        var isPendingCheckout by remember { mutableStateOf(false) }

                                        var expandedByMethod by remember { mutableStateOf(false) }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                            Text(
                                                text = "Secure Direct Gateway Payment Integration",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = selectedMethod,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Choose Billing Provider") },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    trailingIcon = {
                                                        IconButton(onClick = { expandedByMethod = true }) {
                                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                                        }
                                                    }
                                                )
                                                DropdownMenu(
                                                    expanded = expandedByMethod,
                                                    onDismissRequest = { expandedByMethod = false }
                                                ) {
                                                    methods.forEach { met ->
                                                        DropdownMenuItem(
                                                            text = { Text(met) },
                                                            onClick = {
                                                                selectedMethod = met
                                                                expandedByMethod = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = billingPhone,
                                                onValueChange = { billingPhone = it },
                                                label = { Text(if (selectedMethod.contains("Stripe") || selectedMethod.contains("PayPal")) "Your Email Address" else "Mobile Money Account Phone Number") },
                                                placeholder = { Text(if (selectedMethod.contains("Stripe") || selectedMethod.contains("PayPal")) "name@example.com" else "+255 7XX XXX XXX / +233 ...") },
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Button(
                                                onClick = {
                                                    if (billingPhone.isNotEmpty()) {
                                                        isPendingCheckout = true
                                                        viewModel.purchasePackageInPortal(pkg, billingPhone, selectedMethod) { success, msg ->
                                                            isPendingCheckout = false
                                                            checkoutStatus = msg
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(12.dp),
                                                enabled = !isPendingCheckout
                                            ) {
                                                if (isPendingCheckout) {
                                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                                } else {
                                                    Text("Authorize $${DecimalFormat("#,##0.00").format(pkg.price)} Now")
                                                }
                                            }

                                            AnimatedVisibility(visible = checkoutStatus != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                                        .padding(10.dp)
                                                ) {
                                                    Text(
                                                        text = checkoutStatus ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
