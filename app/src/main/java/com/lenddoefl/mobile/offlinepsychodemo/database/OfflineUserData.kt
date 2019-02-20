package com.lenddoefl.mobile.offlinepsychodemo.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Joey Mar Antonio on 20/02/2019.
 */
@Entity
data class OfflineUserData (
    @PrimaryKey var uId : Int,
    @ColumnInfo(name = "first_name") var firstName : String?,
    @ColumnInfo(name = "last_name") var lastName : String?,
    @ColumnInfo(name = "status") var status : String?,
    @ColumnInfo(name = "application_id") var applicationId : String?,
    @ColumnInfo(name = "partner_script_id") var parnerScriptId : String?,
    @ColumnInfo(name = "start_date") var startDate : Long,
    @ColumnInfo(name = "last_modified_date") var lastModifiedDate : Long
)