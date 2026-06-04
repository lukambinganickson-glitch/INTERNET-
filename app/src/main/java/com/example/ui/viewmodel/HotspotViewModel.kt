package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.model.*
import com.example.data.repository.HotspotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class HotspotViewModel(private val repository: HotspotRepository) : ViewModel() {

    // Database flows
    val routers: StateFlow<List<RouterEntity>> = repository.allRouters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packages: StateFlow<List<AccessPackageEntity>> = repository.allPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vouchers: StateFlow<List<VoucherEntity>> = repository.allVouchers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<SessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSessions: StateFlow<List<SessionEntity>> = repository.activeSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionLogEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Telemetry and UI management
    private val _systemLogs = MutableStateFlow<List<String>>(emptyList())
    val systemLogs: StateFlow<List<String>> = _systemLogs.asStateFlow()

    private val _notificationLogs = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notificationLogs: StateFlow<List<NotificationLog>> = _notificationLogs.asStateFlow()

    private val _isSimulatingTraffic = MutableStateFlow(true)
    val isSimulatingTraffic: StateFlow<Boolean> = _isSimulatingTraffic.asStateFlow()

    private val _aiStatus = MutableStateFlow<AiAnalysisStatus>(AiAnalysisStatus.Idle)
    val aiStatus: StateFlow<AiAnalysisStatus> = _aiStatus.asStateFlow()

    // Active customer demo state
    private val _customerMacAddress = MutableStateFlow("AA:BB:CC:11:22:33")
    val customerMacAddress: StateFlow<String> = _customerMacAddress.asStateFlow()

    private val _customerDeviceName = MutableStateFlow("Pixel 8 Pro")
    val customerDeviceName: StateFlow<String> = _customerDeviceName.asStateFlow()

    private val _customerIpAddress = MutableStateFlow("192.168.88.243")
    val customerIpAddress: StateFlow<String> = _customerIpAddress.asStateFlow()

    // Observe active customer session inside the simulated captive portal
    val currentConnectedCustomerSession: StateFlow<SessionEntity?> = combine(
        _customerMacAddress, activeSessions
    ) { mac, activeList ->
        activeList.firstOrNull { it.macAddress.lowercase() == mac.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Data structures
    data class NotificationLog(
        val id: String = UUID.randomUUID().toString(),
        val type: String, // "SMS", "WhatsApp", "Email"
        val recipient: String,
        val content: String,
        val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    )

    sealed interface AiAnalysisStatus {
        object Idle : AiAnalysisStatus
        object Analyzing : AiAnalysisStatus
        data class Success(val report: String, val isMock: Boolean) : AiAnalysisStatus
        data class Error(val message: String) : AiAnalysisStatus
    }

    init {
        // Hydrate default data if database is empty
        viewModelScope.launch {
            repository.allPackages.first().let { pkgs ->
                if (pkgs.isEmpty()) {
                    loadDefaultPackages()
                }
            }
            repository.allRouters.first().let { rtrs ->
                if (rtrs.isEmpty()) {
                    loadDefaultRouters()
                }
            }
            repository.allVouchers.first().let { vchs ->
                if (vchs.isEmpty()) {
                    // Pre-seed some default vouchers for demo
                    generateDemoVouchers()
                }
            }
            logSystemEvent("System initialized. Local database loaded and running.")

            // Start live loop for session traffic simulation and automatic expiries
            simulatedTrafficLoop()
        }
    }

    private suspend fun loadDefaultPackages() {
        val list = listOf(
            AccessPackageEntity(
                name = "Turbo Hour (1 hr)",
                durationMinutes = 60,
                dataLimitMB = 0,
                speedLimitMbps = 10,
                price = 0.50,
                currency = "USD",
                description = "1 Hour ultra-fast unlimited data package."
            ),
            AccessPackageEntity(
                name = "Daily Lite (24 hr)",
                durationMinutes = 1440,
                dataLimitMB = 1024,
                speedLimitMbps = 3,
                price = 1.00,
                currency = "USD",
                description = "Budget package. 1 GB valid for 24 Hours."
            ),
            AccessPackageEntity(
                name = "Daily Extreme (24 hr)",
                durationMinutes = 1440,
                dataLimitMB = 0,
                speedLimitMbps = 8,
                price = 1.99,
                currency = "USD",
                description = "Unlimited commercial extreme tier valid 24 Hours."
            ),
            AccessPackageEntity(
                name = "Weekly Bronze (7 Days)",
                durationMinutes = 10080,
                dataLimitMB = 5120,
                speedLimitMbps = 5,
                price = 5.00,
                currency = "USD",
                description = "Economic Weekly pass. 5 GB valid for 7 Days."
            ),
            AccessPackageEntity(
                name = "Monthly Pro (30 Days)",
                durationMinutes = 43200,
                dataLimitMB = 30720,
                speedLimitMbps = 10,
                price = 18.00,
                currency = "USD",
                description = "Heavy worker package. 30 GB valid for 30 Days."
            ),
            AccessPackageEntity(
                name = "Monthly Unlimited (30 Days)",
                durationMinutes = 43200,
                dataLimitMB = 0,
                speedLimitMbps = 12,
                price = 29.99,
                currency = "USD",
                description = "True unlimited master tier, no limits, auto renewal priority."
            )
        )
        list.forEach { repository.insertPackage(it) }
        logSystemEvent("Loaded 6 high-demand default internet packages.")
    }

    private suspend fun loadDefaultRouters() {
        val list = listOf(
            RouterEntity(
                name = "Primary MikroTik RB750Gr3",
                vendor = "MikroTik",
                ipAddress = "192.168.88.1",
                status = "ONLINE",
                uptime = "6d 14h",
                activeClients = 18
            ),
            RouterEntity(
                name = "Lobby Access Point (UniFi)",
                vendor = "UniFi",
                ipAddress = "192.168.88.10",
                status = "ONLINE",
                uptime = "14d 2h",
                activeClients = 7
            ),
            RouterEntity(
                name = "Edge Gateway pfSense",
                vendor = "pfSense",
                ipAddress = "10.0.0.1",
                status = "ONLINE",
                uptime = "32d 11h",
                activeClients = 25
            )
        )
        list.forEach { repository.insertRouter(it) }
        logSystemEvent("Auto-configured 3 active cloud routers on localized networks.")
    }

    private suspend fun generateDemoVouchers() {
        val pkgs = repository.allPackages.first()
        if (pkgs.isNotEmpty()) {
            // Generate some vouchers for Turbo Hour (index 0) and Weekly pass (index 3)
            val turbo = pkgs.firstOrNull { it.name.contains("Turbo") } ?: pkgs[0]
            val weekly = pkgs.firstOrNull { it.name.contains("Weekly") } ?: pkgs[0]

            repository.generateVouchers(turbo.id, 3, "DEMO")
            repository.generateVouchers(weekly.id, 2, "WEEK")
            logSystemEvent("Generated 5 quick-redeem demo vouchers starting with DEMO-* and WEEK-*.")
        }
    }

    fun toggleTrafficSimulation(enable: Boolean) {
        _isSimulatingTraffic.value = enable
        logSystemEvent("Interactive data traffic simulation ${if (enable) "ENABLED" else "DISABLED"}.")
    }

    private fun simulatedTrafficLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(3000) // Sleep 3 seconds
                if (_isSimulatingTraffic.value) {
                    repository.tickSessions() // Automatic Session clean up (Auto-Disconnect)

                    // Retrieve active sessions and simulate byte transfers
                    val activeList = repository.activeSessions.first()
                    activeList.forEach { valSession ->
                        // Generate random simulated bandwidth usage (up to 500KB up, 2MB down)
                        val up = Random.nextLong(20 * 1024, 500 * 1024)
                        val down = Random.nextLong(100 * 1024, 2 * 1024 * 1024)
                        repository.updateSessionUsage(valSession, up, down)

                        // Randomly emit fraud behavior alert simulation in logs (1% chance per tick)
                        if (Random.nextInt(100) == 77) {
                            val fraudTypes = listOf(
                                "MAC Spoof suspected on dev ${valSession.deviceName}.",
                                "High data speed threshold exceeded on user ${valSession.macAddress}.",
                                "Device session sharing alert: Multiple portals active on ${valSession.ipAddress}"
                            )
                            logSystemEvent("⚠️ SECURITY WARNING: ${fraudTypes.random()}")
                        }
                    }
                }
            }
        }
    }

    // Dynamic helpers
    fun logSystemEvent(msg: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val formatted = "[$time] $msg"
        _systemLogs.update { (listOf(formatted) + it).take(100) }
    }

    fun dispatchNotification(type: String, recipient: String, content: String) {
        val log = NotificationLog(type = type, recipient = recipient, content = content)
        _notificationLogs.update { listOf(log) + it }
        logSystemEvent("✉️ Notification Dispatched -> [$type] to $recipient: \"$content\"")
    }

    // Form APIs
    fun addRouter(name: String, vendor: String, ipAddress: String) {
        viewModelScope.launch {
            val router = RouterEntity(
                name = name,
                vendor = vendor,
                ipAddress = ipAddress,
                status = "ONLINE",
                uptime = "0h",
                activeClients = 0
            )
            repository.insertRouter(router)
            logSystemEvent("Admin added router '$name' ($vendor) with management IP $ipAddress.")
        }
    }

    fun removeRouter(id: Int) {
        viewModelScope.launch {
            repository.deleteRouter(id)
            logSystemEvent("Admin removed router reference [ID: $id].")
        }
    }

    fun addAccessPackage(
        name: String,
        durationMin: Long,
        dataLimitMB: Long,
        speedMbps: Int,
        price: Double,
        currency: String = "USD",
        desc: String
    ) {
        viewModelScope.launch {
            val pkg = AccessPackageEntity(
                name = name,
                durationMinutes = durationMin,
                dataLimitMB = dataLimitMB,
                speedLimitMbps = speedMbps,
                price = price,
                currency = currency,
                description = desc
            )
            repository.insertPackage(pkg)
            logSystemEvent("Created custom internet plan: '$name', Speed Limit: ${speedMbps}Mbps, Price: $currency $price.")
        }
    }

    fun removePackage(id: Int) {
        viewModelScope.launch {
            repository.deletePackage(id)
            logSystemEvent("Admin suspended internet package [ID: $id].")
        }
    }

    fun generateVouchers(packageId: Int, count: Int, prefix: String) {
        viewModelScope.launch {
            val list = repository.generateVouchers(packageId, count, prefix)
            logSystemEvent("Generated ${list.size} secure voucher tokens for package [ID: $packageId] with prefix '$prefix'.")
        }
    }

    fun terminateSession(id: Int, mac: String) {
        viewModelScope.launch {
            repository.forceTerminateSession(id)
            logSystemEvent("Admin manually disconnected client $mac. Network authorization revoked.")
            dispatchNotification("SMS", mac, "Your hot spot session has been terminated by administrator.")
        }
    }

    // Portal Actions
    fun updateClientDemoProfile(mac: String, device: String, ip: String) {
        _customerMacAddress.value = mac.trim()
        _customerDeviceName.value = device.trim()
        _customerIpAddress.value = ip.trim()
        logSystemEvent("Simulating newly connected device: $device MAC: $mac IP: $ip")
    }

    fun redeemVoucherInPortal(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (success, message) = repository.redeemVoucher(
                code = code,
                macAddress = _customerMacAddress.value,
                deviceName = _customerDeviceName.value,
                ipAddress = _customerIpAddress.value
            )
            if (success) {
                logSystemEvent("Portal authenticated successfully using code '$code'. Session activated.")
                dispatchNotification(
                    type = "SMS",
                    recipient = _customerMacAddress.value,
                    content = "Internet Activated! You have connected using Voucher Code $code. Speed limit: High-speed priority."
                )
            }
            onResult(success, message)
        }
    }

    fun purchasePackageInPortal(
        pkg: AccessPackageEntity,
        phoneOrEmail: String,
        gateway: String, // "M-Pesa", "Stripe", "PayPal", "MTN MoMo"
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            logSystemEvent("Sending API payment request to $gateway SDK... Pending manual/STK-push approval.")
            delay(1500) // Realistic delay simulating server network logic and SMS/STK checkout

            val (success, message) = repository.purchasePackage(
                pkg = pkg,
                macAddress = _customerMacAddress.value,
                deviceName = _customerDeviceName.value,
                ipAddress = _customerIpAddress.value,
                payPhoneOrEmail = phoneOrEmail,
                paymentMethod = gateway
            )

            if (success) {
                logSystemEvent("Billing confirmed. Dispatching CoovaChilli / FreeRADIUS authentication grant.")
                dispatchNotification(
                    type = if (phoneOrEmail.contains("@")) "Email" else "SMS",
                    recipient = phoneOrEmail,
                    content = "NetPortal payment confirmed! Amount: ${pkg.currency} ${pkg.price} via $gateway. Your session is now ACTIVE for ${pkg.durationMinutes / 60} hours."
                )
            }
            onResult(success, message)
        }
    }

    // AI Analytical Insight Tool (Gemini vs Offline)
    fun runAiNetworkAnalysis(onDone: () -> Unit) {
        _aiStatus.value = AiAnalysisStatus.Analyzing
        viewModelScope.launch {
            val allSess = repository.allSessions.first()
            val allTx = repository.allTransactions.first()
            val allVch = repository.allVouchers.first()
            val allRtr = repository.allRouters.first()

            val totalRevenue = allTx.filter { it.status == "SUCCESS" }.sumOf { it.amount }
            val activeDevicesCount = repository.activeSessions.first().size

            val prompt = """
                You are NetPortal-AI, a high-frequency security and automation analysis unit for a public Wi-Fi captive portal billing system.
                Analyze the following live network metrics and write a concise, highly strategic, 3-paragraph executive diagnostic:
                Metrics:
                - Active cloud routers: ${allRtr.size}
                - Connected sessions (Active today): ${allSess.size} (Current active connected: $activeDevicesCount)
                - Total pre-generated vouchers in database: ${allVch.size} (Used vouchers: ${allVch.count { it.isUsed }})
                - Cumulative income logged: $$totalRevenue USD from ${allTx.size} transaction entries.
                
                Please evaluate:
                1. AI Network Fraud & Vulnerability Checks (suspected MAC spoofing, bandwidth bottlenecks, or voucher brute-forcing attempts).
                2. Data allocation optimizations (speed throttling packages).
                3. High-growth business marketing strategy to double voucher revenue using promo mechanisms.
                
                Keep the response formatted beautifully in 3 readable bullet-blocks, using bold tags. Avoid developer paths or jargon. Focus only on actionable business and security interventions.
            """.trimIndent()

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // Return high-fidelity local analytical advice
                delay(1200)
                val mockReport = """
                    **1. SECURITY AUDIT & MAC SPLITTING DETECTED**
                    - Suspicious MAC-cloning behavior detected: MAC address 'AA:BB:CC:11:22:33' initiated 3 distinct portal handshakes from separate IP domains within 7 minutes. Dynamic Firewall isolation recommended.
                    - Vulnerability Note: Default ping sweep protocol is open. Advised to throttle response rate in RouterOS ICMP settings to mitigate reconnaissance probes.
                    
                    **2. BANDWIDTH & PACKAGING BOTTLENECKS**
                    - 82% of customer transactions centered on the "Turbo Hour" package, inducing intense micro-spike bandwidth usage at top-of-the-hour marks.
                    - Action: Implement fair-share queueing (SFQ) and throttle maximum packet sizes for bursts exceeding 4 seconds. Introduce a middle-tier 3-Hour plan to ease instantaneous transit strain.
                    
                    **3. REVENUE REDEEM OPTIMIZER**
                    - Idle Capacity: Portal charts report 35% surplus bandwidth between 11PM and 5AM daily.
                    - Strategy: Automatically send automated WhatsApp night-owl promos (e.g. 50% discount on 6-Hour midnight passes) to customers who disconnect before 11PM. This leverages unused ISP capacity of routers.
                """.trimIndent()
                _aiStatus.value = AiAnalysisStatus.Success(mockReport, isMock = true)
            } else {
                // Execute direct REST call to Gemini Server Beta
                try {
                    val reportStr = callGeminiRestApi(apiKey, prompt)
                    _aiStatus.value = AiAnalysisStatus.Success(reportStr, isMock = false)
                } catch (e: Exception) {
                    _aiStatus.value = AiAnalysisStatus.Error("API requested failed: ${e.message}. Displaying local diagnostics.")
                }
            }
            onDone()
        }
    }

    private suspend fun callGeminiRestApi(apiKey: String, promptText: String): String = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        // Post request to newer recommended models
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Response Code: ${response.code} Details: ${response.body?.string()}")
            }
            val resStr = response.body?.string() ?: throw Exception("Empty response body")
            val resJson = JSONObject(resStr)
            val candidates = resJson.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            text
        }
    }
}

class HotspotViewModelFactory(private val repository: HotspotRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HotspotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HotspotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
