import React, { useState, useEffect, useMemo } from 'react';
import {
  Bookmark as BookmarkIcon,
  Search,
  FolderTree,
  Settings as SettingsIcon,
  Plus,
  FolderPlus,
  BookmarkPlus,
  Star,
  CheckCircle,
  Inbox,
  Smartphone,
  Monitor,
  X,
  Download,
} from 'lucide-react';
import { Bookmark, Folder, Tag, FilterType, BrowserType } from './types';
import { storage } from './services/storage';
import { BreadcrumbBar } from './components/BreadcrumbBar';
import { TagFilterBar } from './components/TagFilterBar';
import { BookmarkCard } from './components/BookmarkCard';
import { FolderCard } from './components/FolderCard';
import { AddEditBookmarkModal } from './components/AddEditBookmarkModal';
import { AddEditFolderModal } from './components/AddEditFolderModal';
import { FolderTreeModal } from './components/FolderTreeModal';
import { MoveFolderModal } from './components/MoveFolderModal';
import { SettingsModal } from './components/SettingsModal';

export const App: React.FC = () => {
  const [folders, setFolders] = useState<Folder[]>([]);
  const [bookmarks, setBookmarks] = useState<Bookmark[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [defaultBrowser, setDefaultBrowser] = useState<BrowserType>('default');

  // 뷰 상태
  const [currentFolderId, setCurrentFolderId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [showSearch, setShowSearch] = useState(false);
  const [selectedTag, setSelectedTag] = useState<string | null>(null);
  const [filter, setFilter] = useState<FilterType>('ALL');
  const [isMobileFrame, setIsMobileFrame] = useState(true);

  // 모달 상태들
  const [showAddBookmark, setShowAddBookmark] = useState(false);
  const [editingBookmark, setEditingBookmark] = useState<Bookmark | null>(null);
  const [movingBookmark, setMovingBookmark] = useState<Bookmark | null>(null);

  const [showAddFolder, setShowAddFolder] = useState(false);
  const [editingFolder, setEditingFolder] = useState<Folder | null>(null);
  const [parentForNewFolder, setParentForNewFolder] = useState<string | null>(null);

  const [showFolderTree, setShowFolderTree] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [fabOpen, setFabOpen] = useState(false);
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);

  // 데이터 로드
  const reloadData = () => {
    setFolders(storage.getFolders());
    setBookmarks(storage.getBookmarks());
    setTags(storage.getTags());
    setDefaultBrowser(storage.getDefaultBrowser());
  };

  useEffect(() => {
    reloadData();

    // PWA 설치 이벤트 감지
    const installHandler = (e: any) => {
      e.preventDefault();
      setDeferredPrompt(e);
    };
    window.addEventListener('beforeinstallprompt', installHandler);
    return () => window.removeEventListener('beforeinstallprompt', installHandler);
  }, []);

  const handleInstallApp = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      if (outcome === 'accepted') {
        setDeferredPrompt(null);
      }
    } else {
      alert('스마트폰 브라우저 메뉴 [⋮] → [홈 화면에 추가] 또는 [앱 설치]를 누르시면 어플로 즉시 설치됩니다!');
    }
  };

  // 브레드크럼 계산
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

  // 현재 폴더의 직계 하위 폴더들
  const subFolders = useMemo(() => {
    return folders.filter((f) => f.parentId === currentFolderId);
  }, [folders, currentFolderId]);

  // 필터링된 북마크들
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

      // 4. 폴더 필터 (검색어가 없을 때만 폴더 적용)
      if (!searchQuery.trim() && !selectedTag && filter === 'ALL') {
        if (b.folderId !== currentFolderId) return false;
      }

      return true;
    });
  }, [bookmarks, currentFolderId, searchQuery, selectedTag, filter]);

  // 하위 폴더 통계 맵
  const folderStats = useMemo(() => {
    const stats: { [key: string]: { bookmarks: number; subFolders: number } } = {};
    folders.forEach((f) => {
      const bCount = bookmarks.filter((b) => b.folderId === f.id).length;
      const subCount = folders.filter((sub) => sub.parentId === f.id).length;
      stats[f.id] = { bookmarks: bCount, subFolders: subCount };
    });
    return stats;
  }, [folders, bookmarks]);

  return (
    <div className="min-h-screen bg-slate-200/60 flex items-center justify-center sm:p-4 select-none">
      {/* 폰 프레임 또는 반응형 컨테이너 */}
      <div
        className={`w-full bg-slate-50 flex flex-col transition-all duration-300 ${
          isMobileFrame
            ? 'max-w-md h-screen sm:h-[860px] sm:rounded-[36px] sm:shadow-2xl sm:border-[8px] sm:border-slate-800 overflow-hidden relative'
            : 'max-w-4xl h-screen sm:h-[90vh] sm:rounded-2xl sm:shadow-xl sm:border border-slate-200 overflow-hidden relative'
        }`}
      >
        {/* 상단 스마트폰 노치 (모바일 프레임 모드일 때만 표시) */}
        {isMobileFrame && (
          <div className="hidden sm:flex justify-center items-center py-1.5 bg-slate-900 shrink-0">
            <div className="w-20 h-4 bg-slate-800 rounded-full" />
          </div>
        )}

        {/* 상단 앱 헤더 */}
        <header className="bg-white border-b border-slate-200 shrink-0 z-10">
          <div className="flex items-center justify-between px-4 py-3">
            <div
              onClick={() => {
                setCurrentFolderId(null);
                setSelectedTag(null);
                setFilter('ALL');
                setSearchQuery('');
              }}
              className="flex items-center gap-2 cursor-pointer"
            >
              <div className="w-8 h-8 rounded-xl bg-blue-600 text-white flex items-center justify-center shadow-sm">
                <BookmarkIcon className="w-4 h-4 fill-current" />
              </div>
              <h1 className="font-extrabold text-slate-800 text-base tracking-tight">KKT 북마크</h1>
            </div>

            <div className="flex items-center gap-1">
              <button
                onClick={handleInstallApp}
                title="내 기기에 어플로 설치하기"
                className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-blue-50 text-blue-600 hover:bg-blue-100 text-xs font-bold transition-all border border-blue-200"
              >
                <Download className="w-3.5 h-3.5" />
                <span>앱 설치</span>
              </button>

              <button
                onClick={() => setShowSearch(!showSearch)}
                title="검색"
                className={`p-2 rounded-xl transition-colors ${
                  showSearch ? 'bg-blue-50 text-blue-600' : 'text-slate-500 hover:bg-slate-100'
                }`}
              >
                <Search className="w-4 h-4" />
              </button>

              <button
                onClick={() => setShowFolderTree(true)}
                title="폴더 계층 트리 관리"
                className="p-2 rounded-xl text-slate-500 hover:bg-slate-100 hover:text-blue-600 transition-colors"
              >
                <FolderTree className="w-4 h-4" />
              </button>

              <button
                onClick={() => setShowSettings(true)}
                title="설정 및 백업"
                className="p-2 rounded-xl text-slate-500 hover:bg-slate-100 hover:text-blue-600 transition-colors"
              >
                <SettingsIcon className="w-4 h-4" />
              </button>

              {/* PC에서 모바일 프레임 토글 버튼 */}
              <button
                onClick={() => setIsMobileFrame(!isMobileFrame)}
                title={isMobileFrame ? '와이드 화면으로 보기' : '스마트폰 뷰로 보기'}
                className="hidden sm:block p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors ml-1"
              >
                {isMobileFrame ? <Monitor className="w-4 h-4" /> : <Smartphone className="w-4 h-4" />}
              </button>
            </div>
          </div>

          {/* 검색창 펼침 */}
          {showSearch && (
            <div className="px-4 pb-3">
              <div className="relative flex items-center">
                <Search className="w-4 h-4 absolute left-3 text-slate-400" />
                <input
                  type="text"
                  autoFocus
                  placeholder="제목, 메모, URL, #태그 검색..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-9 pr-8 py-2 rounded-xl bg-slate-100 border border-transparent focus:border-blue-500 focus:bg-white text-xs outline-none transition-all"
                />
                {searchQuery && (
                  <button
                    onClick={() => setSearchQuery('')}
                    className="absolute right-2.5 p-1 rounded-full text-slate-400 hover:text-slate-600"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            </div>
          )}

          {/* 3단 탭 (전체, ★ 즐겨찾기, ✉ 읽지 않음) */}
          <div className="grid grid-cols-3 border-t border-slate-100 text-xs font-semibold">
            <button
              onClick={() => {
                setFilter('ALL');
                setSelectedTag(null);
              }}
              className={`py-2.5 text-center transition-colors border-b-2 ${
                filter === 'ALL' && !selectedTag
                  ? 'border-blue-600 text-blue-600 bg-blue-50/30'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              전체
            </button>
            <button
              onClick={() => {
                setFilter('FAVORITES');
                setSelectedTag(null);
              }}
              className={`py-2.5 text-center transition-colors border-b-2 flex items-center justify-center gap-1 ${
                filter === 'FAVORITES'
                  ? 'border-amber-500 text-amber-600 bg-amber-50/30'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <Star className="w-3.5 h-3.5 fill-current" />
              즐겨찾기
            </button>
            <button
              onClick={() => {
                setFilter('UNREAD');
                setSelectedTag(null);
              }}
              className={`py-2.5 text-center transition-colors border-b-2 flex items-center justify-center gap-1 ${
                filter === 'UNREAD'
                  ? 'border-emerald-600 text-emerald-600 bg-emerald-50/30'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              <CheckCircle className="w-3.5 h-3.5" />
              읽지 않음
            </button>
          </div>
        </header>

        {/* 브레드크럼 바 (홈 > 폴더 > 하위폴더) */}
        {!searchQuery && filter === 'ALL' && !selectedTag && (
          <BreadcrumbBar breadcrumbs={breadcrumbs} onSelectFolder={(id) => setCurrentFolderId(id)} />
        )}

        {/* 다중 태그 필터 바 */}
        <TagFilterBar
          tags={tags}
          selectedTag={selectedTag}
          onSelectTag={(tag) => {
            setSelectedTag(tag);
            if (tag) setFilter('ALL');
          }}
        />

        {/* 메인 리스트 영역 */}
        <main className="flex-1 overflow-y-auto p-4 space-y-4">
          {/* 1. 하위 폴더 그리드 (기본 뷰일 때만) */}
          {!searchQuery && filter === 'ALL' && !selectedTag && subFolders.length > 0 && (
            <div>
              <div className="flex items-center justify-between mb-2">
                <h3 className="font-bold text-slate-700 text-xs tracking-wider uppercase">
                  하위 폴더 ({subFolders.length})
                </h3>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                {subFolders.map((folder) => {
                  const stats = folderStats[folder.id] || { bookmarks: 0, subFolders: 0 };
                  return (
                    <FolderCard
                      key={folder.id}
                      folder={folder}
                      bookmarkCount={stats.bookmarks}
                      subFolderCount={stats.subFolders}
                      onClick={() => setCurrentFolderId(folder.id)}
                      onEdit={() => setEditingFolder(folder)}
                      onDelete={() => {
                        if (confirm(`'${folder.name}' 폴더를 삭제하시겠습니까?`)) {
                          storage.deleteFolder(folder.id);
                          reloadData();
                        }
                      }}
                    />
                  );
                })}
              </div>
            </div>
          )}

          {/* 2. 북마크 목록 */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <h3 className="font-bold text-slate-700 text-xs tracking-wider uppercase">
                북마크 ({filteredBookmarks.length})
              </h3>
            </div>

            {filteredBookmarks.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-center text-slate-400">
                <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-3">
                  <Inbox className="w-8 h-8 text-slate-300" />
                </div>
                <p className="text-sm font-semibold text-slate-600">북마크가 없습니다.</p>
                <p className="text-xs text-slate-400 mt-1 max-w-[200px]">
                  {selectedTag
                    ? `'#${selectedTag}' 태그가 달린 항목이 없습니다.`
                    : filter === 'FAVORITES'
                    ? '즐겨찾기한 북마크가 없습니다.'
                    : filter === 'UNREAD'
                    ? '모든 북마크를 읽었습니다! 🎉'
                    : '우측 하단 + 버튼을 눌러 첫 번째 링크를 추가해보세요.'}
                </p>
                <button
                  onClick={() => setShowAddBookmark(true)}
                  className="mt-4 px-4 py-2 rounded-xl bg-blue-50 text-blue-600 hover:bg-blue-100 text-xs font-semibold transition-colors"
                >
                  + 북마크 추가하기
                </button>
              </div>
            ) : (
              <div className="space-y-2.5">
                {filteredBookmarks.map((bookmark) => (
                  <BookmarkCard
                    key={bookmark.id}
                    bookmark={bookmark}
                    defaultBrowser={defaultBrowser}
                    onToggleFavorite={() => {
                      storage.toggleFavorite(bookmark.id);
                      reloadData();
                    }}
                    onToggleRead={() => {
                      storage.toggleRead(bookmark.id);
                      reloadData();
                    }}
                    onEdit={() => setEditingBookmark(bookmark)}
                    onMove={() => setMovingBookmark(bookmark)}
                    onDelete={() => {
                      if (confirm('이 북마크를 삭제하시겠습니까?')) {
                        storage.deleteBookmark(bookmark.id);
                        reloadData();
                      }
                    }}
                    onTagClick={(tag) => setSelectedTag(tag)}
                  />
                ))}
              </div>
            )}
          </div>
        </main>

        {/* 플로팅 액션 버튼 (FAB) */}
        <div className="absolute bottom-5 right-5 z-20 flex flex-col items-end gap-2.5">
          {fabOpen && (
            <>
              {/* 폴더 생성 서브 버튼 */}
              <button
                onClick={() => {
                  setFabOpen(false);
                  setParentForNewFolder(currentFolderId);
                  setShowAddFolder(true);
                }}
                className="flex items-center gap-2 px-3.5 py-2 rounded-full bg-white text-slate-700 shadow-lg border border-slate-200 text-xs font-semibold hover:bg-slate-50 transition-all animate-bounce-short"
              >
                <FolderPlus className="w-4 h-4 text-emerald-600" />
                <span>폴더 생성</span>
              </button>

              {/* 북마크 추가 서브 버튼 */}
              <button
                onClick={() => {
                  setFabOpen(false);
                  setShowAddBookmark(true);
                }}
                className="flex items-center gap-2 px-3.5 py-2 rounded-full bg-white text-slate-700 shadow-lg border border-slate-200 text-xs font-semibold hover:bg-slate-50 transition-all animate-bounce-short"
              >
                <BookmarkPlus className="w-4 h-4 text-blue-600" />
                <span>북마크 추가</span>
              </button>
            </>
          )}

          {/* 메인 FAB 버튼 */}
          <button
            onClick={() => setFabOpen(!fabOpen)}
            className={`w-14 h-14 rounded-2xl flex items-center justify-center text-white shadow-xl transition-all duration-200 ${
              fabOpen ? 'bg-slate-800 rotate-45' : 'bg-blue-600 hover:bg-blue-700 hover:scale-105'
            }`}
          >
            <Plus className="w-6 h-6 stroke-[2.5]" />
          </button>
        </div>

        {/* 팝업 모달들 */}
        {/* 새 북마크 추가 모달 */}
        {showAddBookmark && (
          <AddEditBookmarkModal
            defaultFolderId={currentFolderId}
            folders={folders}
            tags={tags}
            onClose={() => setShowAddBookmark(false)}
            onSave={(data) => {
              storage.saveBookmark(data);
              reloadData();
              setShowAddBookmark(false);
            }}
          />
        )}

        {/* 북마크 수정 모달 */}
        {editingBookmark && (
          <AddEditBookmarkModal
            initialBookmark={editingBookmark}
            defaultFolderId={editingBookmark.folderId}
            folders={folders}
            tags={tags}
            onClose={() => setEditingBookmark(null)}
            onSave={(data) => {
              storage.saveBookmark({ ...data, id: editingBookmark.id });
              reloadData();
              setEditingBookmark(null);
            }}
          />
        )}

        {/* 새 폴더 추가 모달 */}
        {showAddFolder && (
          <AddEditFolderModal
            defaultParentId={parentForNewFolder}
            folders={folders}
            onClose={() => setShowAddFolder(false)}
            onSave={(data) => {
              storage.saveFolder(data);
              reloadData();
              setShowAddFolder(false);
            }}
          />
        )}

        {/* 폴더 수정 모달 */}
        {editingFolder && (
          <AddEditFolderModal
            initialFolder={editingFolder}
            defaultParentId={editingFolder.parentId}
            folders={folders}
            onClose={() => setEditingFolder(null)}
            onSave={(data) => {
              storage.saveFolder({ ...data, id: editingFolder.id });
              reloadData();
              setEditingFolder(null);
            }}
          />
        )}

        {/* 폴더 계층 관리 모달 */}
        {showFolderTree && (
          <FolderTreeModal
            folders={folders}
            onClose={() => setShowFolderTree(false)}
            onSelectFolder={(folderId) => {
              setCurrentFolderId(folderId);
              setSelectedTag(null);
            }}
            onAddFolder={(parentId) => {
              setParentForNewFolder(parentId);
              setShowAddFolder(true);
            }}
            onEditFolder={(folder) => setEditingFolder(folder)}
            onDeleteFolder={(folderId) => {
              if (confirm('이 폴더를 삭제하시겠습니까? 하위 폴더도 함께 정리됩니다.')) {
                storage.deleteFolder(folderId);
                reloadData();
              }
            }}
          />
        )}

        {/* 폴더 이동 모달 */}
        {movingBookmark && (
          <MoveFolderModal
            folders={folders}
            currentFolderId={movingBookmark.folderId}
            onClose={() => setMovingBookmark(null)}
            onMove={(targetFolderId) => {
              storage.moveBookmark(movingBookmark.id, targetFolderId);
              reloadData();
            }}
          />
        )}

        {/* 설정 및 백업 모달 */}
        {showSettings && (
          <SettingsModal
            onClose={() => setShowSettings(false)}
            onDataChanged={() => reloadData()}
          />
        )}
      </div>
    </div>
  );
};
