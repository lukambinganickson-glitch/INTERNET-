package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routers")
data class RouterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val vendor: String, // "MikroTik", "UniFi", "pfSense", "OpenWRT", "Cisco"
    val ipAddress: String,
    val status: String, // "ONLINE", "OFFLINE"
    val uptime: String = "0h",
    val activeClients: Int = 0,
    val lastSyncTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "access_packages")
data class AccessPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val durationMinutes: Long, // 60, 180, 1440, etc.
    val dataLimitMB: Long, // 0 for unlimited
    val speedLimitMbps: Int, // 1, 5, 10
    val price: Double,
    val currency: String = "USD",
    val description: String
)

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val packageId: Int,
    val packageName: String,
    val price: Double,
    val currency: String = "USD",
    val isUsed: Boolean = false,
    val usedByMac: String? = null,
    val usedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val macAddress: String,
    val deviceName: String,
    val ipAddress: String,
    val packageName: String,
    val packageId: Int,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long,
    val bytesUploaded: Long = 0L,
    val bytesDownloaded: Long = 0L,
    val speedLimitMbps: Int = 5,
    val isActive: Boolean = true,
    val paymentPhone: String? = null,
    val paymentMethod: String? = null
)

@Entity(tableName = "transactions")
data class TransactionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionId: String,
    val code: String?, // Voucher code matched, or phone
    val macAddress: String,
    val phoneOrEmail: String,
    val packageName: String,
    val amount: Double,
    val currency: String = "USD",
    val paymentMethod: String, // "M-Pesa", "MTN MoMo", "Stripe", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "SUCCESS", "PENDING", "FAILED"
)
