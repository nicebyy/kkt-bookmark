package com.kkt.bookmark.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import com.kkt.bookmark.data.model.BrowserType

object LinkOpener {

    /**
     * 지정된 브라우저로 URL을 엽니다.
     * @param browserIdentifier BrowserType id ('default', 'samsung', 'chrome', 'naver', 'whale', 'edge', 'firefox', 'inapp')
     */
    fun openUrl(context: Context, url: String, browserIdentifier: String? = null) {
        try {
            var validUrl = url.trim()
            if (!validUrl.startsWith("http://") && !validUrl.startsWith("https://")) {
                validUrl = "https://$validUrl"
            }
            val uri = Uri.parse(validUrl)
            val browserType = BrowserType.fromId(browserIdentifier)

            // 1. 인앱 브라우저 (Chrome Custom Tabs) 명시적 선택 시
            if (browserType == BrowserType.INAPP) {
                openInCustomTabs(context, uri)
                return
            }

            // 2. 특정 외부 브라우저(삼성인터넷, 크롬, 네이버 등) 지정 시
            val targetPackage = browserType.packageName
            if (targetPackage != null) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage(targetPackage)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context,
                        "${browserType.displayName} 앱이 설치되어 있지 않아 기본 브라우저로 엽니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // 3. 기본값: 자체 인앱브라우저가 아니라 스마트폰의 외부 기본 브라우저 앱으로 실행!
            val defaultBrowserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(defaultBrowserIntent)

        } catch (e: Exception) {
            // 외부 브라우저 호출 실패 시 인앱 커스텀 탭으로 최후의 Fallback
            try {
                openInCustomTabs(context, Uri.parse(url))
            } catch (ex: Exception) {
                Toast.makeText(context, "링크를 열 수 있는 브라우저를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openInCustomTabs(context: Context, uri: Uri) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        customTabsIntent.launchUrl(context, uri)
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
