import { Bookmark, Folder, Tag, FOLDER_COLORS, BrowserType } from '../types';

const FOLDERS_KEY = 'kkt_bookmark_folders';
const BOOKMARKS_KEY = 'kkt_bookmark_items';
const TAGS_KEY = 'kkt_bookmark_tags';
const DEFAULT_BROWSER_KEY = 'kkt_bookmark_default_browser';

// 기본 샘플 데이터
const initialFolders: Folder[] = [
  { id: 'f-dev', name: '💻 개발 및 기술', parentId: null, color: FOLDER_COLORS[0], createdAt: Date.now() - 50000 },
  { id: 'f-life', name: '☕ 일상 및 취미', parentId: null, color: FOLDER_COLORS[1], createdAt: Date.now() - 40000 },
  { id: 'f-read', name: '📚 나중에 읽을 글', parentId: null, color: FOLDER_COLORS[3], createdAt: Date.now() - 30000 },
  // 2단계 하위 폴더 (트리 구조)
  { id: 'f-android', name: '안드로이드 & Kotlin', parentId: 'f-dev', color: FOLDER_COLORS[4], createdAt: Date.now() - 20000 },
  { id: 'f-ai', name: '인공지능 & LLM', parentId: 'f-dev', color: FOLDER_COLORS[5], createdAt: Date.now() - 10000 },
];

const initialTags: Tag[] = [
  { id: 't-1', name: '튜토리얼', color: '#00acc1' },
  { id: 't-2', name: '레퍼런스', color: '#5e35b1' },
  { id: 't-3', name: '꿀팁', color: '#e53935' },
];

const initialBookmarks: Bookmark[] = [
  {
    id: 'b-1',
    folderId: 'f-android',
    url: 'https://developer.android.com/jetpack/compose',
    title: 'Jetpack Compose 공식 가이드',
    description: '선언형 UI 툴킷으로 현대적인 네이티브 안드로이드 앱을 빠르게 제작하세요.',
    memo: '광고 없이 깔끔하게 관리하는 첫 번째 공식 튜토리얼 북마크!',
    imageUrl: 'https://developer.android.com/static/images/social/android-developers.png',
    faviconUrl: 'https://www.google.com/s2/favicons?domain=developer.android.com&sz=128',
    tags: ['튜토리얼', '레퍼런스'],
    isFavorite: true,
    isRead: false,
    createdAt: Date.now() - 10000,
    updatedAt: Date.now() - 10000,
  },
  {
    id: 'b-2',
    folderId: 'f-ai',
    url: 'https://github.com',
    title: 'GitHub - 빌더와 개발자들의 오픈소스 허브',
    description: '전 세계 개발자들이 함께 코드를 공유하고 협업하는 플랫폼입니다.',
    memo: '오픈소스 프로젝트 검색용',
    imageUrl: 'https://github.githubassets.com/images/modules/open_graph/github-logo.png',
    faviconUrl: 'https://www.google.com/s2/favicons?domain=github.com&sz=128',
    tags: ['레퍼런스', '꿀팁'],
    isFavorite: false,
    isRead: true,
    createdAt: Date.now() - 5000,
    updatedAt: Date.now() - 5000,
  }
];

export const storage = {
  getFolders(): Folder[] {
    const raw = localStorage.getItem(FOLDERS_KEY);
    if (!raw) {
      this.setFolders(initialFolders);
      return initialFolders;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return initialFolders;
    }
  },

  setFolders(folders: Folder[]) {
    localStorage.setItem(FOLDERS_KEY, JSON.stringify(folders));
  },

  saveFolder(folder: Omit<Folder, 'id' | 'createdAt'> & { id?: string }): Folder {
    const list = this.getFolders();
    if (folder.id) {
      const idx = list.findIndex(f => f.id === folder.id);
      if (idx !== -1) {
        list[idx] = { ...list[idx], ...folder };
        this.setFolders(list);
        return list[idx];
      }
    }
    const newFolder: Folder = {
      ...folder,
      id: 'f-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      createdAt: Date.now()
    };
    list.push(newFolder);
    this.setFolders(list);
    return newFolder;
  },

  deleteFolder(folderId: string) {
    let folders = this.getFolders();
    // 하위 폴더들 재귀 탐색
    const toDeleteIds = new Set<string>([folderId]);
    let added = true;
    while (added) {
      added = false;
      folders.forEach(f => {
        if (f.parentId && toDeleteIds.has(f.parentId) && !toDeleteIds.has(f.id)) {
          toDeleteIds.add(f.id);
          added = true;
        }
      });
    }

    folders = folders.filter(f => !toDeleteIds.has(f.id));
    this.setFolders(folders);

    // 삭제된 폴더에 속한 북마크는 루트(null)로 이동
    const bookmarks = this.getBookmarks().map(b => {
      if (b.folderId && toDeleteIds.has(b.folderId)) {
        return { ...b, folderId: null };
      }
      return b;
    });
    this.setBookmarks(bookmarks);
  },

  getBookmarks(): Bookmark[] {
    const raw = localStorage.getItem(BOOKMARKS_KEY);
    if (!raw) {
      this.setBookmarks(initialBookmarks);
      return initialBookmarks;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return initialBookmarks;
    }
  },

  setBookmarks(bookmarks: Bookmark[]) {
    localStorage.setItem(BOOKMARKS_KEY, JSON.stringify(bookmarks));
  },

  getDefaultBrowser(): BrowserType {
    const raw = localStorage.getItem(DEFAULT_BROWSER_KEY);
    return (raw as BrowserType) || 'default';
  },

  setDefaultBrowser(browser: BrowserType) {
    localStorage.setItem(DEFAULT_BROWSER_KEY, browser);
  },

  saveBookmark(bookmark: {
    id?: string;
    url: string;
    title: string;
    description?: string;
    memo?: string;
    imageUrl?: string;
    faviconUrl?: string;
    folderId: string | null;
    tags: string[];
    isFavorite?: boolean;
    isRead?: boolean;
    preferredBrowser?: BrowserType;
  }): Bookmark {
    const list = this.getBookmarks();
    if (bookmark.id) {
      const idx = list.findIndex(b => b.id === bookmark.id);
      if (idx !== -1) {
        list[idx] = {
          ...list[idx],
          ...bookmark,
          isFavorite: bookmark.isFavorite ?? list[idx].isFavorite,
          isRead: bookmark.isRead ?? list[idx].isRead,
          preferredBrowser: bookmark.preferredBrowser !== undefined ? bookmark.preferredBrowser : list[idx].preferredBrowser,
          updatedAt: Date.now()
        };
        this.setBookmarks(list);
        this.syncTags(bookmark.tags);
        return list[idx];
      }
    }
    const newBookmark: Bookmark = {
      id: 'b-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      url: bookmark.url,
      title: bookmark.title,
      description: bookmark.description,
      memo: bookmark.memo,
      imageUrl: bookmark.imageUrl,
      faviconUrl: bookmark.faviconUrl,
      folderId: bookmark.folderId,
      tags: bookmark.tags,
      isFavorite: bookmark.isFavorite ?? false,
      isRead: bookmark.isRead ?? false,
      preferredBrowser: bookmark.preferredBrowser,
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    list.unshift(newBookmark);
    this.setBookmarks(list);
    this.syncTags(bookmark.tags);
    return newBookmark;
  },

  deleteBookmark(id: string) {
    const list = this.getBookmarks().filter(b => b.id !== id);
    this.setBookmarks(list);
  },

  toggleFavorite(id: string) {
    const list = this.getBookmarks().map(b => b.id === id ? { ...b, isFavorite: !b.isFavorite } : b);
    this.setBookmarks(list);
  },

  toggleRead(id: string) {
    const list = this.getBookmarks().map(b => b.id === id ? { ...b, isRead: !b.isRead } : b);
    this.setBookmarks(list);
  },

  moveBookmark(id: string, newFolderId: string | null) {
    const list = this.getBookmarks().map(b => b.id === id ? { ...b, folderId: newFolderId } : b);
    this.setBookmarks(list);
  },

  getTags(): Tag[] {
    const raw = localStorage.getItem(TAGS_KEY);
    if (!raw) {
      this.setTags(initialTags);
      return initialTags;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return initialTags;
    }
  },

  setTags(tags: Tag[]) {
    localStorage.setItem(TAGS_KEY, JSON.stringify(tags));
  },

  syncTags(tagNames: string[]) {
    const existing = this.getTags();
    const existingNames = new Set(existing.map(t => t.name));
    let updated = false;

    tagNames.forEach(name => {
      const clean = name.trim().replace(/^#/, '');
      if (clean && !existingNames.has(clean)) {
        existing.push({
          id: 't-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
          name: clean,
          color: FOLDER_COLORS[Math.floor(Math.random() * FOLDER_COLORS.length)]
        });
        existingNames.add(clean);
        updated = true;
      }
    });

    if (updated) {
      this.setTags(existing);
    }
  },

  // 백업 JSON 내보내기
  exportData(): string {
    const data = {
      version: 1,
      exportedAt: Date.now(),
      folders: this.getFolders(),
      bookmarks: this.getBookmarks(),
      tags: this.getTags()
    };
    return JSON.stringify(data, null, 2);
  },

  // 백업 JSON 복원
  importData(jsonString: string): { success: boolean; count: number; error?: string } {
    try {
      const data = JSON.parse(jsonString);
      if (!data.bookmarks || !Array.isArray(data.bookmarks)) {
        return { success: false, count: 0, error: '올바른 백업 형식이 아닙니다.' };
      }
      if (Array.isArray(data.folders)) {
        this.setFolders(data.folders);
      }
      if (Array.isArray(data.tags)) {
        this.setTags(data.tags);
      }
      this.setBookmarks(data.bookmarks);
      return { success: true, count: data.bookmarks.length };
    } catch (e: any) {
      return { success: false, count: 0, error: e.message || 'JSON 파싱 오류' };
    }
  }
};
