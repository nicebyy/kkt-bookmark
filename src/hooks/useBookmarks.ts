import { useState, useEffect, useMemo, useCallback } from 'react';
import { Bookmark, Folder, Tag, FilterType, BrowserType } from '../types';
import { storage } from '../services/storage';

export function useBookmarks() {
  const [folders, setFolders] = useState<Folder[]>([]);
  const [bookmarks, setBookmarks] = useState<Bookmark[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [defaultBrowser, setDefaultBrowser] = useState<BrowserType>('default');

  // 탐색 및 필터 상태
  const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTag, setSelectedTag] = useState<string | null>(null);
  const [filter, setFilter] = useState<FilterType>('ALL');

  // 데이터 동기화
  const reloadData = useCallback(() => {
    setFolders(storage.getFolders());
    setBookmarks(storage.getBookmarks());
    setTags(storage.getTags());
    setDefaultBrowser(storage.getDefaultBrowser());
  }, []);

  useEffect(() => {
    reloadData();
  }, [reloadData]);

  // 브레드크럼 (계층 경로) 계산
  const breadcrumbs = useMemo(() => {
    if (!currentFolderId) return [];
    const trail: Folder[] = [];
    let curr = folders.find((f) => f.id === currentFolderId);
    const visited = new Set<string>();

    while (curr && !visited.has(curr.id)) {
      visited.add(curr.id);
      trail.unshift(curr);
      curr = curr.parentId ? folders.find((f) => f.id === curr!.parentId) : undefined;
    }
    return trail;
  }, [currentFolderId, folders]);

  // 현재 폴더의 직계 하위 폴더 목록
  const subFolders = useMemo(() => {
    return folders.filter((f) => f.parentId === currentFolderId);
  }, [folders, currentFolderId]);

  // 폴더별 통계 (하위 폴더 수, 북마크 수)
  const folderStats = useMemo(() => {
    const stats: { [key: string]: { bookmarks: number; subFolders: number } } = {};
    folders.forEach((f) => {
      const bCount = bookmarks.filter((b) => b.folderId === f.id).length;
      const subCount = folders.filter((sub) => sub.parentId === f.id).length;
      stats[f.id] = { bookmarks: bCount, subFolders: subCount };
    });
    return stats;
  }, [folders, bookmarks]);

  // 필터링된 북마크 목록 계산
  const filteredBookmarks = useMemo(() => {
    return bookmarks.filter((b) => {
      // 1. 검색어 필터
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const matchTitle = b.title.toLowerCase().includes(q);
        const matchUrl = b.url.toLowerCase().includes(q);
        const matchMemo = (b.memo || '').toLowerCase().includes(q);
        const matchDesc = (b.description || '').toLowerCase().includes(q);
        const matchTag = b.tags.some((t) => t.toLowerCase().includes(q));
        if (!matchTitle && !matchUrl && !matchMemo && !matchDesc && !matchTag) return false;
      }

      // 2. 태그 필터
      if (selectedTag && !b.tags.includes(selectedTag)) {
        return false;
      }

      // 3. 상태 필터 (전체, 즐겨찾기, 안읽음)
      if (filter === 'FAVORITES' && !b.isFavorite) return false;
      if (filter === 'UNREAD' && b.isRead) return false;

      // 4. 폴더 필터 (검색어나 태그가 없을 때만 폴더 적용)
      if (!searchQuery.trim() && !selectedTag && filter === 'ALL') {
        if (b.folderId !== currentFolderId) return false;
      }

      return true;
    });
  }, [bookmarks, currentFolderId, searchQuery, selectedTag, filter]);

  // 액션 핸들러들
  const saveBookmark = useCallback((data: Parameters<typeof storage.saveBookmark>[0]) => {
    storage.saveBookmark(data);
    reloadData();
  }, [reloadData]);

  const deleteBookmark = useCallback((id: string) => {
    storage.deleteBookmark(id);
    reloadData();
  }, [reloadData]);

  const toggleFavorite = useCallback((id: string) => {
    storage.toggleFavorite(id);
    reloadData();
  }, [reloadData]);

  const toggleRead = useCallback((id: string) => {
    storage.toggleRead(id);
    reloadData();
  }, [reloadData]);

  const moveBookmark = useCallback((id: string, targetFolderId: string | null) => {
    storage.moveBookmark(id, targetFolderId);
    reloadData();
  }, [reloadData]);

  const moveBookmarkOrder = useCallback((id: string, direction: 'up' | 'down') => {
    storage.moveBookmarkOrder(id, direction);
    reloadData();
  }, [reloadData]);

  // 브라우저 뒤로가기(History Popstate) 연동: 하위 폴더에서 뒤로가기 시 이전/부모 디렉토리로 이동
  const navigateToFolder = useCallback((folderId: string | null) => {
    setCurrentFolderId(folderId);
    if (typeof window !== 'undefined') {
      window.history.pushState({ folderId }, '');
    }
  }, []);

  const navigateToParentFolder = useCallback(() => {
    if (!currentFolderId) return;
    const currentFolder = folders.find(f => f.id === currentFolderId);
    navigateToFolder(currentFolder?.parentId ?? null);
  }, [currentFolderId, folders, navigateToFolder]);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (!window.history.state) {
      window.history.replaceState({ folderId: null }, '');
    }

    const handlePopState = (e: PopStateEvent) => {
      const stateFolderId = e.state?.folderId ?? null;
      setCurrentFolderId(stateFolderId);
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const saveFolder = useCallback((data: Parameters<typeof storage.saveFolder>[0]) => {
    storage.saveFolder(data);
    reloadData();
  }, [reloadData]);

  const deleteFolder = useCallback((folderId: string) => {
    storage.deleteFolder(folderId);
    reloadData();
  }, [reloadData]);

  const resetNavigation = useCallback(() => {
    navigateToFolder(null);
    setSelectedTag(null);
    setFilter('ALL');
    setSearchQuery('');
  }, [navigateToFolder]);

  return {
    // 상태
    folders,
    bookmarks,
    tags,
    defaultBrowser,
    currentFolderId,
    searchQuery,
    selectedTag,
    filter,
    breadcrumbs,
    subFolders,
    filteredBookmarks,
    folderStats,

    // 세터
    setCurrentFolderId: navigateToFolder,
    navigateToParentFolder,
    setSearchQuery,
    setSelectedTag,
    setFilter,
    resetNavigation,

    // 액션
    reloadData,
    saveBookmark,
    deleteBookmark,
    toggleFavorite,
    toggleRead,
    moveBookmark,
    moveBookmarkOrder,
    saveFolder,
    deleteFolder,
  };
}
