package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HotspotDatabase
import com.example.data.repository.HotspotRepository
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HotspotViewModel
import com.example.ui.viewmodel.HotspotViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create repository instance manually adhering to simple constructor injections without bulky Hilt
        val db = HotspotDatabase.getDatabase(applicationContext)
        val repo = HotspotRepository(db.hotspotDao())
        val factory = HotspotViewModelFactory(repo)

        setContent {
            MyApplicationTheme {
                // Fetch the main ViewModel
                val viewModel: HotspotViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
                HotspotMainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotMainScreen(viewModel: HotspotViewModel) {
    // Current Navigation Section
    var activeTab by remember { mutableStateOf("PORTAL") } // "PORTAL" (Client view) or "DASHBOARD" or "AUDIT"

    // Live observable data states
    val routersList by viewModel.routers.collectAsState()
    val packagesList by viewModel.packages.collectAsState()
    val sessionsList by viewModel.sessions.collectAsState()
    val transactionsList by viewModel.transactions.collectAsState()
    val vouchersList by viewModel.vouchers.collectAsState()
    val systemLogsList by viewModel.systemLogs.collectAsState()
    val notificationLogsList by viewModel.notificationLogs.collectAsState()

    val connectedMac by viewModel.customerMacAddress.collectAsState()
    val connectedDevice by viewModel.customerDeviceName.collectAsState()
    val connectedIp by viewModel.customerIpAddress.collectAsState()
    val activeSession by viewModel.currentConnectedCustomerSession.collectAsState()

    val simulateTrafficActive by viewModel.isSimulatingTraffic.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NetPortal™ Suite",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "AISP Hotspot, Billing & Captive Portal Core Engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    // Simulated active traffic loop trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Auto simulation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = simulateTrafficActive.let { if (it) 1f else 0.5f })
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = simulateTrafficActive,
                            onCheckedChange = { viewModel.toggleTrafficSimulation(it) },
                            modifier = Modifier.scale(0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                // Captive Portal (Client Gateway Simulation)
                NavigationBarItem(
                    selected = (activeTab == "PORTAL"),
                    onClick = { activeTab = "PORTAL" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Portal Screen") },
                    label = { Text("Captive Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                // Admin Dashboard Overview
                NavigationBarItem(
                    selected = (activeTab == "DASHBOARD"),
                    onClick = { activeTab = "DASHBOARD" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Dashboard Statistics") },
                    label = { Text("Admin Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                // Audit Tracking Logs
                NavigationBarItem(
                    selected = (activeTab == "AUDIT"),
                    onClick = { activeTab = "AUDIT" },
                    icon = { Icon(Icons.Default.List, contentDescription = "Auditing Ledger") },
                    label = { Text("Audit Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "PORTAL" -> {
                    CustomerPortalTab(
                        viewModel = viewModel,
                        packagesList = packagesList,
                        activeSession = activeSession,
                        connectedMac = connectedMac,
                        connectedDevice = connectedDevice,
                        connectedIp = connectedIp,
                        notifyLogs = notificationLogsList
                    )
                }

                "DASHBOARD" -> {
                    AdminOverviewTab(
                        viewModel = viewModel,
                        routersList = routersList,
                        packagesList = packagesList,
                        sessionsList = sessionsList,
                        transactionsList = transactionsList,
                        vouchersList = vouchersList
                    )
                }

                "AUDIT" -> {
                    AdminAuditingTab(
                        viewModel = viewModel,
                        sessionsList = sessionsList,
                        transactionsList = transactionsList,
                        vouchersList = vouchersList,
                        logsList = systemLogsList
                    )
                }
            }
        }
    }
}
