import { BrowserType } from '../types';

/**
 * URL과 지정된 브라우저 타입에 따라 해당 브라우저를 직접 실행합니다.
 * 스마트폰(안드로이드/PWA) 환경에서는 안드로이드 Intent Scheme을 통해 해당 브라우저 앱을 다이렉트로 호출합니다.
 */
export function openInBrowser(rawUrl: string, browser: BrowserType = 'default') {
  let url = rawUrl.trim();
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    url = 'https://' + url;
  }

  const isAndroid = typeof navigator !== 'undefined' && /Android/i.test(navigator.userAgent);
  const cleanUrl = url.replace(/^https?:\/\//, '');

  if (browser === 'default') {
    window.open(url, '_blank', 'noopener,noreferrer');
    return;
  }

  if (isAndroid) {
    // 안드로이드 기기에서는 패키지 인텐트(Android Intent URI)를 통해 대상 브라우저를 직접 실행
    let intentUrl = '';
    switch (browser) {
      case 'chrome':
        intentUrl = `intent://${cleanUrl}#Intent;scheme=https;package=com.android.chrome;end`;
        break;
      case 'samsung':
        intentUrl = `intent://${cleanUrl}#Intent;scheme=https;package=com.sec.android.app.sbrowser;end`;
        break;
      case 'naver':
        // 네이버 앱 인앱브라우저 스킴 또는 패키지
        intentUrl = `intent://${cleanUrl}#Intent;scheme=https;package=com.nhn.android.search;end`;
        break;
      case 'whale':
        intentUrl = `intent://${cleanUrl}#Intent;scheme=https;package=com.naver.whale;end`;
        break;
      case 'edge':
        intentUrl = `intent://${cleanUrl}#Intent;scheme=https;package=com.microsoft.emmx;end`;
        break;
      case 'firefox':
        intentUrl = `intent://${cleanUrl}#Intent;scheme=https;package=org.mozilla.firefox;end`;
        break;
      default:
        intentUrl = url;
        break;
    }

    try {
      window.location.href = intentUrl;
    } catch {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  } else {
    // 데스크톱 PC 환경
    if (browser === 'edge') {
      try {
        window.location.href = `microsoft-edge:${url}`;
        return;
      } catch {
        // fallback
      }
    }

    // PC 환경에서 다른 브라우저는 새 탭으로 실행
    window.open(url, '_blank', 'noopener,noreferrer');
  }
}
