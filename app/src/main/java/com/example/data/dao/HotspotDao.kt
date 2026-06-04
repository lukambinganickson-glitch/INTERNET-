package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HotspotDao {

    // Router management
    @Query("SELECT * FROM routers ORDER BY id DESC")
    fun getAllRoutersFlow(): Flow<List<RouterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: RouterEntity): Long

    @Query("DELETE FROM routers WHERE id = :id")
    suspend fun deleteRouterById(id: Int)

    // Access Package management
    @Query("SELECT * FROM access_packages ORDER BY price ASC")
    fun getAllPackagesFlow(): Flow<List<AccessPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: AccessPackageEntity): Long

    @Query("DELETE FROM access_packages WHERE id = :id")
    suspend fun deletePackageById(id: Int)

    @Query("SELECT * FROM access_packages WHERE id = :id LIMIT 1")
    suspend fun getPackageById(id: Int): AccessPackageEntity?

    // Voucher management
    @Query("SELECT * FROM vouchers ORDER BY createdAt DESC")
    fun getAllVouchersFlow(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE code = :code LIMIT 1")
    suspend fun getVoucherByCode(code: String): VoucherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Update
    suspend fun updateVoucher(voucher: VoucherEntity)

    // Session management
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE isActive = 1 ORDER BY startTime DESC")
    fun getActiveSessionsFlow(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE macAddress = :mac LIMIT 1")
    suspend fun getSessionByMac(mac: String): SessionEntity?

    @Query("SELECT * FROM sessions WHERE macAddress = :mac AND isActive = 1 LIMIT 1")
    suspend fun getActiveSessionByMac(mac: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("UPDATE sessions SET isActive = 0 WHERE id = :id")
    suspend fun terminateSession(id: Int)

    @Query("UPDATE sessions SET isActive = 0 WHERE endTime <= :now")
    suspend fun terminateExpiredSessions(now: Long)

    // Transaction Log management
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLogEntity): Long
}
