export interface FetchedMetadata {
  url: string;
  title: string;
  description?: string;
  imageUrl?: string;
  faviconUrl?: string;
}

export async function fetchMetadata(rawUrl: string): Promise<FetchedMetadata> {
  let url = rawUrl.trim();
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    url = 'https://' + url;
  }

  let host = url;
  try {
    host = new URL(url).hostname;
  } catch {
    // ignore
  }

  const defaultFavicon = `https://www.google.com/s2/favicons?domain=${host}&sz=128`;

  try {
    // Microlink 무료 메타데이터 API 시도
    const res = await fetch(`https://api.microlink.io?url=${encodeURIComponent(url)}`, {
      signal: AbortSignal.timeout(5000)
    });
    const json = await res.json();
    if (json.status === 'success' && json.data) {
      const data = json.data;
      return {
        url,
        title: data.title || host,
        description: data.description,
        imageUrl: data.image?.url,
        faviconUrl: data.logo?.url || defaultFavicon
      };
    }
  } catch {
    // fallback
  }

  // Fallback: 기본 정보 반환
  return {
    url,
    title: host,
    description: undefined,
    imageUrl: undefined,
    faviconUrl: defaultFavicon
  };
}
