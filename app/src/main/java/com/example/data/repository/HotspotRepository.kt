package com.example.data.repository

import com.example.data.dao.HotspotDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import kotlin.random.Random

class HotspotRepository(private val hotspotDao: HotspotDao) {

    val allRouters: Flow<List<RouterEntity>> = hotspotDao.getAllRoutersFlow()
    val allPackages: Flow<List<AccessPackageEntity>> = hotspotDao.getAllPackagesFlow()
    val allVouchers: Flow<List<VoucherEntity>> = hotspotDao.getAllVouchersFlow()
    val allSessions: Flow<List<SessionEntity>> = hotspotDao.getAllSessionsFlow()
    val activeSessions: Flow<List<SessionEntity>> = hotspotDao.getActiveSessionsFlow()
    val allTransactions: Flow<List<TransactionLogEntity>> = hotspotDao.getAllTransactionsFlow()

    suspend fun insertRouter(router: RouterEntity) {
        hotspotDao.insertRouter(router)
    }

    suspend fun deleteRouter(id: Int) {
        hotspotDao.deleteRouterById(id)
    }

    suspend fun insertPackage(pkg: AccessPackageEntity) {
        hotspotDao.insertPackage(pkg)
    }

    suspend fun deletePackage(id: Int) {
        hotspotDao.deletePackageById(id)
    }

    suspend fun generateVouchers(packageId: Int, count: Int, customPrefix: String = ""): List<VoucherEntity> {
        val pkg = hotspotDao.getPackageById(packageId) ?: return emptyList()
        val list = mutableListOf<VoucherEntity>()
        val characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Easily readable, no ambiguous O, I, 1, 0
        
        repeat(count) {
            val code = buildString {
                if (customPrefix.isNotEmpty()) {
                    append(customPrefix.uppercase().trim())
                    append("-")
                }
                repeat(6) {
                    append(characters[Random.nextInt(characters.length)])
                }
            }
            val voucher = VoucherEntity(
                code = code,
                packageId = pkg.id,
                packageName = pkg.name,
                price = pkg.price,
                currency = pkg.currency
            )
            val insertId = hotspotDao.insertVoucher(voucher)
            list.add(voucher.copy(id = insertId.toInt()))
        }
        return list
    }

    suspend fun redeemVoucher(code: String, macAddress: String, deviceName: String, ipAddress: String): Pair<Boolean, String> {
        val voucher = hotspotDao.getVoucherByCode(code.uppercase().trim())
        if (voucher == null) {
            return Pair(false, "Voucher code not found.")
        }
        if (voucher.isUsed) {
            return Pair(false, "Voucher is already used by ${voucher.usedByMac ?: "another device"}.")
        }

        val pkg = hotspotDao.getPackageById(voucher.packageId)
            ?: return Pair(false, "Associated internet package was not found.")

        // Mark voucher as used
        val updatedVoucher = voucher.copy(
            isUsed = true,
            usedByMac = macAddress,
            usedAt = System.currentTimeMillis()
        )
        hotspotDao.updateVoucher(updatedVoucher)

        // Terminate any active session for this MAC to avoid overlaps
        val existingSession = hotspotDao.getActiveSessionByMac(macAddress)
        if (existingSession != null) {
            hotspotDao.terminateSession(existingSession.id)
        }

        // Create new session
        val durationMs = pkg.durationMinutes * 60 * 1000
        val session = SessionEntity(
            macAddress = macAddress,
            deviceName = deviceName,
            ipAddress = ipAddress,
            packageName = pkg.name,
            packageId = pkg.id,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + durationMs,
            speedLimitMbps = pkg.speedLimitMbps,
            isActive = true,
            paymentMethod = "Voucher",
            paymentPhone = code
        )
        hotspotDao.insertSession(session)

        // Log transaction
        val transaction = TransactionLogEntity(
            transactionId = "VCH-${System.currentTimeMillis() % 1000000}-${Random.nextInt(100, 999)}",
            code = code,
            macAddress = macAddress,
            phoneOrEmail = "Voucher Redeem",
            packageName = pkg.name,
            amount = pkg.price,
            currency = pkg.currency,
            paymentMethod = "Voucher",
            status = "SUCCESS"
        )
        hotspotDao.insertTransaction(transaction)

        return Pair(true, "Successfully activated ${pkg.name} package!")
    }

    suspend fun purchasePackage(
        pkg: AccessPackageEntity,
        macAddress: String,
        deviceName: String,
        ipAddress: String,
        payPhoneOrEmail: String,
        paymentMethod: String // "M-Pesa", "Stripe", "PayPal", "MTN MoMo"
    ): Pair<Boolean, String> {
        // Log transaction initially
        val txId = "TX-${System.currentTimeMillis() % 1000000}-${Random.nextInt(100, 999)}"
        val transaction = TransactionLogEntity(
            transactionId = txId,
            code = null,
            macAddress = macAddress,
            phoneOrEmail = payPhoneOrEmail,
            packageName = pkg.name,
            amount = pkg.price,
            currency = pkg.currency,
            paymentMethod = paymentMethod,
            status = "SUCCESS"
        )
        hotspotDao.insertTransaction(transaction)

        // Terminate old sessions for this device
        val existingSession = hotspotDao.getActiveSessionByMac(macAddress)
        if (existingSession != null) {
            hotspotDao.terminateSession(existingSession.id)
        }

        // Auto activate session
        val durationMs = pkg.durationMinutes * 60 * 1000
        val session = SessionEntity(
            macAddress = macAddress,
            deviceName = deviceName,
            ipAddress = ipAddress,
            packageName = pkg.name,
            packageId = pkg.id,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + durationMs,
            speedLimitMbps = pkg.speedLimitMbps,
            isActive = true,
            paymentPhone = payPhoneOrEmail,
            paymentMethod = paymentMethod
        )
        hotspotDao.insertSession(session)

        return Pair(true, "Payment Approved! Session for $macAddress activated automatically.")
    }

    suspend fun tickSessions() {
        val now = System.currentTimeMillis()
        hotspotDao.terminateExpiredSessions(now)
    }

    suspend fun updateSessionUsage(session: SessionEntity, up: Long, down: Long) {
        val updated = session.copy(
            bytesUploaded = session.bytesUploaded + up,
            bytesDownloaded = session.bytesDownloaded + down
        )
        hotspotDao.updateSession(updated)
    }

    suspend fun forceTerminateSession(id: Int) {
        hotspotDao.terminateSession(id)
    }
}
