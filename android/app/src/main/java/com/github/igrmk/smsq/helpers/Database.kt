package com.github.igrmk.smsq.helpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.github.igrmk.smsq.BuildConfig
import com.github.igrmk.smsq.entities.Sms
import org.jetbrains.anko.db.*

class DbHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "database", version = BuildConfig.VERSION_CODE) {
    private val tag = this::class.simpleName!!

    companion object {
        const val TABLE_SMS = "sms"
        const val COLUMN_SMS_ID = "id"
        const val COLUMN_SMS_TEXT = "text"
        const val COLUMN_SMS_SENDER = "sender"
        const val COLUMN_SMS_CARRIER = "carrier"
        const val COLUMN_SMS_SIM = "sim"
        const val COLUMN_SMS_TIMESTAMP = "timestamp"
        const val COLUMN_SMS_OFFSET = "offset"

        private var instance: DbHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): DbHelper {
            if (instance == null) {
                instance = DbHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(TABLE_SMS, true,
                COLUMN_SMS_ID to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                COLUMN_SMS_TEXT to TEXT,
                COLUMN_SMS_SENDER to TEXT,
                COLUMN_SMS_CARRIER to TEXT,
                COLUMN_SMS_SIM to TEXT,
                COLUMN_SMS_TIMESTAMP to INTEGER,
                COLUMN_SMS_OFFSET to INTEGER
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }
}

val Context.database: DbHelper
    get() = DbHelper.getInstance(applicationContext)

fun Context.storeSms(sms: Sms) {
    database.use {
        insert(DbHelper.TABLE_SMS,
                DbHelper.COLUMN_SMS_TEXT to sms.text,
                DbHelper.COLUMN_SMS_SENDER to sms.sender,
                DbHelper.COLUMN_SMS_CARRIER to sms.carrier,
                DbHelper.COLUMN_SMS_SIM to sms.sim,
                DbHelper.COLUMN_SMS_TIMESTAMP to sms.timestamp,
                DbHelper.COLUMN_SMS_OFFSET to sms.offset
        )
    }
}

fun Context.allSmses(): List<Sms> {
    return database.use {
        select(
                DbHelper.TABLE_SMS,
                DbHelper.COLUMN_SMS_ID,
                DbHelper.COLUMN_SMS_TEXT,
                DbHelper.COLUMN_SMS_SENDER,
                DbHelper.COLUMN_SMS_CARRIER,
                DbHelper.COLUMN_SMS_SIM,
                DbHelper.COLUMN_SMS_TIMESTAMP,
                DbHelper.COLUMN_SMS_OFFSET
        )
                .orderBy(DbHelper.COLUMN_SMS_TIMESTAMP)
                .parseList(rowParser { id: Int,
                                       text: String,
                                       sender: String,
                                       carrier: String,
                                       sim: String,
                                       timestamp: Long,
                                       offset: Int ->
                    Sms().apply {
                        this.id = id
                        this.text = text
                        this.sender = sender
                        this.carrier = carrier
                        this.sim = sim
                        this.timestamp = timestamp
                        this.offset = offset
                    }
                })
    }
}

fun Context.deleteSms(id: Int) {
    database.use {
        delete(DbHelper.TABLE_SMS, "${DbHelper.COLUMN_SMS_ID} = {id}", "id" to id)
    }
}

fun Context.deleteAllSmses() = database.use { delete(DbHelper.TABLE_SMS) }

