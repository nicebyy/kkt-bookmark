package com.kkt.bookmark.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URI

data class UrlMetadata(
    val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val faviconUrl: String?
)

object MetadataFetcher {

    /**
     * URL에 접속하여 OpenGraph 및 HTML 태그로부터 메타데이터를 추출합니다.
     */
    suspend fun fetch(rawUrl: String): UrlMetadata = withContext(Dispatchers.IO) {
        val sanitizedUrl = sanitizeUrl(rawUrl)
        try {
            val doc = Jsoup.connect(sanitizedUrl)
                .userAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .timeout(6000)
                .followRedirects(true)
                .get()

            // 제목: og:title -> twitter:title -> title 태그 -> 도메인명
            val title = doc.select("meta[property=og:title]").attr("content").takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=twitter:title]").attr("content").takeIf { it.isNotBlank() }
                ?: doc.title().takeIf { it.isNotBlank() }
                ?: extractHost(sanitizedUrl)

            // 설명: og:description -> twitter:description -> meta description
            val description = doc.select("meta[property=og:description]").attr("content").takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=twitter:description]").attr("content").takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=description]").attr("content").takeIf { it.isNotBlank() }

            // 이미지: og:image -> twitter:image
            var imageUrl = doc.select("meta[property=og:image]").attr("content").takeIf { it.isNotBlank() }
                ?: doc.select("meta[name=twitter:image]").attr("content").takeIf { it.isNotBlank() }

            // 상대 경로 이미지인 경우 절대 경로로 보정
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                imageUrl = resolveRelativeUrl(sanitizedUrl, imageUrl)
            }

            // 파비콘
            var faviconUrl = doc.select("link[rel~=(?i)^(shortcut|icon)$]").attr("href").takeIf { it.isNotBlank() }
            if (faviconUrl != null && !faviconUrl.startsWith("http")) {
                faviconUrl = resolveRelativeUrl(sanitizedUrl, faviconUrl)
            } else if (faviconUrl == null) {
                // 구글 파비콘 서비스 fallback
                val host = extractHost(sanitizedUrl)
                faviconUrl = "https://www.google.com/s2/favicons?domain=$host&sz=128"
            }

            UrlMetadata(
                url = sanitizedUrl,
                title = title.trim(),
                description = description?.trim(),
                imageUrl = imageUrl,
                faviconUrl = faviconUrl
            )
        } catch (e: Exception) {
            // 네트워크 오류 또는 크롤링 차단 시 기본 도메인 정보로 대체
            val host = extractHost(sanitizedUrl)
            UrlMetadata(
                url = sanitizedUrl,
                title = host,
                description = null,
                imageUrl = null,
                faviconUrl = "https://www.google.com/s2/favicons?domain=$host&sz=128"
            )
        }
    }

    private fun sanitizeUrl(url: String): String {
        var trimmed = url.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://$trimmed"
        }
        return trimmed
    }

    private fun extractHost(url: String): String {
        return try {
            URI(url).host ?: url
        } catch (e: Exception) {
            url
        }
    }

    private fun resolveRelativeUrl(baseUrl: String, relativePath: String): String {
        return try {
            val baseUri = URI(baseUrl)
            baseUri.resolve(relativePath).toString()
        } catch (e: Exception) {
            relativePath
        }
    }

    /**
     * 카카오톡이나 브라우저에서 공유된 텍스트 중 URL 정규식 패턴을 찾아 첫 번째 URL을 반환합니다.
     */
    fun extractUrlFromText(text: String): String? {
        val urlRegex = Regex("""(https?://[^\s]+)""", RegexOption.IGNORE_CASE)
        val match = urlRegex.find(text)
        return match?.value
    }
}
