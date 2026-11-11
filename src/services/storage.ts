import { Bookmark, Folder, Tag, FOLDER_COLORS, BrowserType } from '../types';

const FOLDERS_KEY = 'kkt_bookmark_folders';
const BOOKMARKS_KEY = 'kkt_bookmark_items';
const TAGS_KEY = 'kkt_bookmark_tags';
const DEFAULT_BROWSER_KEY = 'kkt_bookmark_default_browser';

const CLEANUP_KEY = 'kkt_bookmark_cleaned_samples_v1';

// 기본 데이터: 빈 상태로 시작
const initialFolders: Folder[] = [];
const initialTags: Tag[] = [];
const initialBookmarks: Bookmark[] = [];

// 기존 로컬스토리지에 저장된 예시 데이터 1회성 정리
function cleanupExistingSamples() {
  if (typeof window === 'undefined') return;
  if (localStorage.getItem(CLEANUP_KEY)) return;

  try {
    const rawFolders = localStorage.getItem(FOLDERS_KEY);
    if (rawFolders) {
      const folders: Folder[] = JSON.parse(rawFolders);
      const filtered = folders.filter(f => !['f-dev', 'f-life', 'f-read', 'f-android', 'f-ai'].includes(f.id));
      localStorage.setItem(FOLDERS_KEY, JSON.stringify(filtered));
    }

    const rawBookmarks = localStorage.getItem(BOOKMARKS_KEY);
    if (rawBookmarks) {
      const bms: Bookmark[] = JSON.parse(rawBookmarks);
      const filtered = bms.filter(b => !['b-1', 'b-2'].includes(b.id));
      localStorage.setItem(BOOKMARKS_KEY, JSON.stringify(filtered));
    }

    const rawTags = localStorage.getItem(TAGS_KEY);
    if (rawTags) {
      const tags: Tag[] = JSON.parse(rawTags);
      const filtered = tags.filter(t => !['튜토리얼', '레퍼런스', '꿀팁'].includes(t.name));
      localStorage.setItem(TAGS_KEY, JSON.stringify(filtered));
    }
  } catch (e) {
    console.error('Failed to clean up sample data:', e);
  } finally {
    localStorage.setItem(CLEANUP_KEY, 'true');
  }
}

cleanupExistingSamples();

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
    let items: Bookmark[] = initialBookmarks;
    if (raw) {
      try {
        items = JSON.parse(raw);
      } catch {
        items = initialBookmarks;
      }
    }
    // sortOrder 오름차순, sortOrder가 같거나 없으면 createdAt 내림차순
    return items.sort((a, b) => {
      const orderA = a.sortOrder ?? 0;
      const orderB = b.sortOrder ?? 0;
      if (orderA !== orderB) return orderA - orderB;
      return b.createdAt - a.createdAt;
    });
  },

  setBookmarks(bookmarks: Bookmark[]) {
    localStorage.setItem(BOOKMARKS_KEY, JSON.stringify(bookmarks));
  },

  moveBookmarkOrder(id: string, direction: 'up' | 'down') {
    const list = this.getBookmarks();
    const target = list.find(b => b.id === id);
    if (!target) return;

    // 동일 폴더 내의 북마크들 사이에서 순서 변경
    const folderItems = list.filter(b => b.folderId === target.folderId);
    const idx = folderItems.findIndex(b => b.id === id);
    if (idx === -1) return;

    const swapIdx = direction === 'up' ? idx - 1 : idx + 1;
    if (swapIdx < 0 || swapIdx >= folderItems.length) return;

    const other = folderItems[swapIdx];

    // 인덱스 기반으로 깨끗하게 10 단위 재정렬 후 swap
    folderItems.forEach((b, i) => {
      b.sortOrder = i * 10;
    });
    const tempOrder = target.sortOrder!;
    target.sortOrder = other.sortOrder!;
    other.sortOrder = tempOrder;

    this.setBookmarks(list);
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
    // 처음 지정한 브라우저를 기본 브라우저로 자동 기억
    if (bookmark.preferredBrowser && bookmark.preferredBrowser !== 'default') {
      this.setDefaultBrowser(bookmark.preferredBrowser);
    }
    const finalBrowser = bookmark.preferredBrowser || this.getDefaultBrowser();

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
      preferredBrowser: finalBrowser,
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
