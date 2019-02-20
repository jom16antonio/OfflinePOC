package com.lenddoefl.mobile.offlinepsychodemo.database

import android.arch.persistence.room.*

/**
 * Created by Joey Mar Antonio on 20/02/2019.
 */
@Database(entities = arrayOf(OfflineUserData::class), version = 1)
abstract class OfflineAppDatabase : RoomDatabase() {
    abstract fun offlineUserDataDao(): OfflineUserDataDao
}
