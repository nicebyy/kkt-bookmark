package com.kkt.bookmark.data.model

enum class BrowserType(
    val id: String,
    val displayName: String,
    val packageName: String?,
    val badgeColor: Long
) {
    DEFAULT("default", "시스템 기본 브라우저", null, 0xFF64748B),
    SAMSUNG("samsung", "삼성 인터넷", "com.sec.android.app.sbrowser", 0xFF1D4ED8),
    CHROME("chrome", "구글 크롬 (Chrome)", "com.android.chrome", 0xFFEA4335),
    NAVER("naver", "네이버 앱 (Naver)", "com.nhn.android.search", 0xFF03C75A),
    WHALE("whale", "네이버 웨일 (Whale)", "com.naver.whale", 0xFF00C73C),
    EDGE("edge", "MS 엣지 (Edge)", "com.microsoft.emmx", 0xFF0078D4),
    FIREFOX("firefox", "파이어폭스 (Firefox)", "org.mozilla.firefox", 0xFFFF7139),
    INAPP("inapp", "자체 인앱 브라우저", "inapp", 0xFF8B5CF6);

    companion object {
        fun fromId(id: String?): BrowserType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }
    }
}
