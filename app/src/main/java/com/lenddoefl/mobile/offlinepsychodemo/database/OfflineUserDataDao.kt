package com.lenddoefl.mobile.offlinepsychodemo.database

import android.arch.persistence.room.*

/**
 * Created by Joey Mar Antonio on 20/02/2019.
 */
@Dao
interface OfflineUserDataDao {
    // waiting for variables
    @Query("SELECT * FROM offlineUserData")
    fun getAll(): List<OfflineUserData>

    @Query("SELECT * FROM offlineUserData WHERE uId IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<OfflineUserData>

    @Query("SELECT * FROM offlineUserData WHERE first_name LIKE :firstName AND last_name LIKE :lastName LIMIT 1")
    fun findByName(firstName: String, lastName: String): OfflineUserData

    @Query("SELECT * FROM offlineUserData WHERE application_id LIKE :applicationId LIMIT 1")
    fun findByApplicationId(applicationId: String): OfflineUserData

    @Insert
    fun insertAll(vararg userData: OfflineUserData)

    @Delete
    fun delete(userData: OfflineUserData)
}