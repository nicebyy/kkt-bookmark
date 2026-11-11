package com.kkt.bookmark.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kkt.bookmark.data.local.dao.BookmarkDao
import com.kkt.bookmark.data.local.dao.FolderDao
import com.kkt.bookmark.data.local.dao.TagDao
import com.kkt.bookmark.data.local.entity.BookmarkEntity
import com.kkt.bookmark.data.local.entity.BookmarkTagCrossRef
import com.kkt.bookmark.data.local.entity.FolderEntity
import com.kkt.bookmark.data.local.entity.TagEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        FolderEntity::class,
        TagEntity::class,
        BookmarkEntity::class,
        BookmarkTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kkt_bookmark.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 첫 실행 시 기본 예시 데이터(폴더 구조 및 태그) 생성
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val folderDao = db.folderDao()
            val tagDao = db.tagDao()
            val bookmarkDao = db.bookmarkDao()

            // 1단계 기본 폴더들
            val devFolderId = folderDao.insertFolder(
                FolderEntity(name = "💻 개발 및 기술", color = 0xFF1E88E5, sortOrder = 0)
            )
            val lifeFolderId = folderDao.insertFolder(
                FolderEntity(name = "☕ 일상 및 취미", color = 0xFF43A047, sortOrder = 1)
            )
            val readLaterFolderId = folderDao.insertFolder(
                FolderEntity(name = "📚 나중에 읽을 글", color = 0xFFFB8C00, sortOrder = 2)
            )

            // 2단계 하위 폴더들 (트리 구조 예시)
            val androidFolderId = folderDao.insertFolder(
                FolderEntity(name = "안드로이드 & Kotlin", parentId = devFolderId, color = 0xFF3D5AFE, sortOrder = 0)
            )
            val aiFolderId = folderDao.insertFolder(
                FolderEntity(name = "인공지능 & LLM", parentId = devFolderId, color = 0xFF8E24AA, sortOrder = 1)
            )

            // 기본 태그들
            val tagTutorialId = tagDao.insertTag(TagEntity(name = "튜토리얼", color = 0xFF00ACC1))
            val tagReferenceId = tagDao.insertTag(TagEntity(name = "레퍼런스", color = 0xFF5E35B1))
            val tagTipsId = tagDao.insertTag(TagEntity(name = "꿀팁", color = 0xFFE53935))

            // 샘플 북마크
            val sampleBookmarkId = bookmarkDao.insertBookmark(
                BookmarkEntity(
                    folderId = androidFolderId,
                    url = "https://developer.android.com/jetpack/compose",
                    title = "Jetpack Compose 공식 문서",
                    description = "선언형 UI 툴킷으로 현대적인 네이티브 안드로이드 앱을 빠르게 제작하세요.",
                    memo = "광고 없이 깔끔하게 관리하는 첫 번째 공식 튜토리얼 북마크!",
                    imageUrl = "https://developer.android.com/static/images/social/android-developers.png",
                    isFavorite = true
                )
            )

            tagDao.insertCrossRef(BookmarkTagCrossRef(bookmarkId = sampleBookmarkId, tagId = tagTutorialId))
            tagDao.insertCrossRef(BookmarkTagCrossRef(bookmarkId = sampleBookmarkId, tagId = tagReferenceId))
        }
    }
}
