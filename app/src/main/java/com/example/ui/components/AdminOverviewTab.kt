package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.HotspotViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminOverviewTab(
    viewModel: HotspotViewModel,
    routersList: List<RouterEntity>,
    packagesList: List<AccessPackageEntity>,
    sessionsList: List<SessionEntity>,
    transactionsList: List<TransactionLogEntity>,
    vouchersList: List<VoucherEntity>
) {
    var showAddRouterDialog by remember { mutableStateOf(false) }
    var showAddPkgDialog by remember { mutableStateOf(false) }
    var showAddVoucherDialog by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalRevenue = transactionsList.filter { it.status == "SUCCESS" }.sumOf { it.amount }
    val formatter = DecimalFormat("#,##0.00")
    val totalBandwidthUsed = sessionsList.sumOf { it.bytesUploaded + it.bytesDownloaded }
    val formattedBandwidth = formatBytes(totalBandwidthUsed)

    var activeAiSearch by remember { mutableStateOf(false) }
    val aiAnalyticState by viewModel.aiStatus.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Fraud Detection & Network Insights Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "AI Analyzer",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AI Network Intelligence",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automated fraud, bottleneck & promo optimization checks",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI Engine Content Box
                    when (val currentStatus = aiAnalyticState) {
                        is HotspotViewModel.AiAnalysisStatus.Idle -> {
                            Text(
                                text = "Run local analytics or leverage the Gemini AI Suite scanner to evaluate MAC spoofing vulnerabilities, performance limits, and maximize bandwidth income.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                        is HotspotViewModel.AiAnalysisStatus.Analyzing -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Gemini cognitive model analyzing database parameters...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        is HotspotViewModel.AiAnalysisStatus.Success -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = MaterialTheme.colorScheme.success,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentStatus.isMock) "Local Analytical Insights Engine" else "Gemini Flash AI Engine Result",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentStatus.report,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                        is HotspotViewModel.AiAnalysisStatus.Error -> {
                            Text(
                                text = "Error during analysis: ${currentStatus.message}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (aiAnalyticState !is HotspotViewModel.AiAnalysisStatus.Analyzing) {
                        Button(
                            onClick = {
                                viewModel.runAiNetworkAnalysis {
                                    activeAiSearch = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Run Audit Log",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Live Security & Revenue Audit")
                        }
                    }
                }
            }
        }

        // Live statistics Grid (2x2 Column Simulation)
        item {
            Text(
                text = "Operational Metrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Daily Income",
                    value = "$${formatter.format(totalRevenue)}",
                    subtitle = "${transactionsList.count { it.status == "SUCCESS" }} Receipts",
                    icon = Icons.Default.ShoppingCart,
                    accentColor = MaterialTheme.colorScheme.success,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Connected devices",
                    value = "${sessionsList.count { it.isActive }} online",
                    subtitle = "${sessionsList.size} Total Registries",
                    icon = Icons.Default.Person,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    title = "Bandwidth Transit",
                    value = formattedBandwidth,
                    subtitle = "All-Time Flow",
                    icon = Icons.Default.PlayArrow,
                    accentColor = MaterialTheme.colorScheme.warning,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active Routers",
                    value = "${routersList.count { it.status == "ONLINE" }} / ${routersList.size}",
                    subtitle = "All gateways operational",
                    icon = Icons.Default.Settings,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Admin Interactive Management Panels
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Admin Configuration Interfaces",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showAddRouterDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Router", fontSize = 12.sp)
                }

                Button(
                    onClick = { showAddPkgDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Package", fontSize = 12.sp)
                }

                Button(
                    onClick = { showAddVoucherDialog = true },
                    modifier = Modifier.weight(1.1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Vouchers", fontSize = 12.sp)
                }
            }
        }

        // Live Router Nodes
        item {
            Text(
                text = "Active Multi-Vendor Router System",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (routersList.isEmpty()) {
            item {
                Text(
                    text = "No edge routers connected. Touch 'Add Router' to hook a node.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            items(routersList) { router ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (router.vendor) {
                                        "MikroTik" -> Icons.Default.Settings
                                        "UniFi" -> Icons.Default.PlayArrow
                                        "pfSense" -> Icons.Default.Lock
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = router.vendor,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = router.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${router.vendor} • IP: ${router.ipAddress}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (router.status == "ONLINE") MaterialTheme.colorScheme.success else Color.Red,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = router.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (router.status == "ONLINE") MaterialTheme.colorScheme.success else Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.removeRouter(router.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove Router",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogs
    if (showAddRouterDialog) {
        var name by remember { mutableStateOf("") }
        var vendor by remember { mutableStateOf("MikroTik") }
        val vendors = listOf("MikroTik", "UniFi", "Cisco", "TP-Link", "OpenWRT", "pfSense")
        var ip by remember { mutableStateOf("192.168.88.1") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddRouterDialog = false },
            title = { Text("Connect New Edge Gateway Router") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Router Friendly Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = vendor,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Hardware Vendor API") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            vendors.forEach { vend ->
                                DropdownMenuItem(
                                    text = { Text(vend) },
                                    onClick = {
                                        vendor = vend
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("Local Management / VPN IP Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            viewModel.addRouter(name, vendor, ip)
                            showAddRouterDialog = false
                        }
                    }
                ) {
                    Text("Register Cloud Router")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRouterDialog = false }) { Text("Dismiss") }
            }
        )
    }

    if (showAddPkgDialog) {
        var name by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("60") }
        var limit by remember { mutableStateOf("0") }
        var speed by remember { mutableStateOf("10") }
        var price by remember { mutableStateOf("1.50") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPkgDialog = false },
            title = { Text("Create hotspot Internet Package") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Plan Label (e.g. Turbo Week)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Time (Minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = limit,
                            onValueChange = { limit = it },
                            label = { Text("Quota MB (0 unlimited)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = speed,
                            onValueChange = { speed = it },
                            label = { Text("Throttling Mbps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price (USD)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Short Customer Facing Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            val dur = duration.toLongOrNull() ?: 60L
                            val lim = limit.toLongOrNull() ?: 0L
                            val spd = speed.toIntOrNull() ?: 5
                            val prc = price.toDoubleOrNull() ?: 1.00
                            viewModel.addAccessPackage(name, dur, lim, spd, prc, desc = desc)
                            showAddPkgDialog = false
                        }
                    }
                ) {
                    Text("Publish Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPkgDialog = false }) { Text("Dismiss") }
            }
        )
    }

    if (showAddVoucherDialog) {
        var selectedPkgId by remember { mutableIntStateOf(0) }
        var count by remember { mutableStateOf("5") }
        var prefix by remember { mutableStateOf("HOT") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        if (packagesList.isNotEmpty() && selectedPkgId == 0) {
            selectedPkgId = packagesList.first().id
        }

        val activePkg = packagesList.firstOrNull { it.id == selectedPkgId }

        AlertDialog(
            onDismissRequest = { showAddVoucherDialog = false },
            title = { Text("Bulk Generate Voucher Tokens") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = activePkg?.name ?: "Select Target Plan",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Associated Internet Package") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            packagesList.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} ($${p.price})") },
                                    onClick = {
                                        selectedPkgId = p.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = count,
                        onValueChange = { count = it },
                        label = { Text("Vouchers Volume Count (Maximum 1000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prefix,
                        onValueChange = { prefix = it },
                        label = { Text("Optional Code Prefix (e.g. VIP, CAFE)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = count.toIntOrNull() ?: 5
                        viewModel.generateVouchers(selectedPkgId, num, prefix)
                        showAddVoucherDialog = false
                    }
                ) {
                    Text("Generate Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVoucherDialog = false }) { Text("Dismiss") }
            }
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
