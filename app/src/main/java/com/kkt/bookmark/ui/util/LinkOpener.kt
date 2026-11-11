package com.kkt.bookmark.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

object LinkOpener {
    fun openUrl(context: Context, url: String, browserPackage: String? = null) {
        try {
            var validUrl = url.trim()
            if (!validUrl.startsWith("http://") && !validUrl.startsWith("https://")) {
                validUrl = "https://$validUrl"
            }
            val uri = Uri.parse(validUrl)

            // 특정 브라우저 패키지가 지정된 경우 (크롬, 삼성인터넷, 네이버 등)
            if (!browserPackage.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(browserPackage)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }

            // Chrome Custom Tabs 시도
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            // 일반 Intent 브라우저 Fallback
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "링크를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareLink(context: Context, title: String, url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n$url")
        }
        context.startActivity(Intent.createChooser(intent, "북마크 공유하기"))
    }
}
