package com.kkt.bookmark

import android.app.Application
import com.kkt.bookmark.data.local.AppDatabase
import com.kkt.bookmark.data.repository.BackupRestoreManager
import com.kkt.bookmark.data.repository.BookmarkRepository

class BookmarkApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: BookmarkRepository by lazy { BookmarkRepository(database) }
    val backupRestoreManager: BackupRestoreManager by lazy { BackupRestoreManager(this, database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: BookmarkApp
            private set
    }
}
