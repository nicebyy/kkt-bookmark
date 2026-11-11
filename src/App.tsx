import React, { useState } from 'react';
import { Bookmark, Folder } from './types';
import { useBookmarks } from './hooks/useBookmarks';
import { usePwaInstall } from './hooks/usePwaInstall';
import { Header } from './components/Header';
import { BreadcrumbBar } from './components/BreadcrumbBar';
import { TagFilterBar } from './components/TagFilterBar';
import { BookmarkList } from './components/BookmarkList';
import { FabMenu } from './components/FabMenu';
import { ModalManager } from './components/ModalManager';

export const App: React.FC = () => {
  // 비즈니스 로직 및 상태 캡슐화 훅
  const {
    folders,
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
    setCurrentFolderId,
    setSearchQuery,
    setSelectedTag,
    setFilter,
    resetNavigation,
    reloadData,
    saveBookmark,
    deleteBookmark,
    toggleFavorite,
    toggleRead,
    moveBookmark,
    moveBookmarkOrder,
    saveFolder,
    deleteFolder,
  } = useBookmarks();

  // PWA 설치 로직 훅
  const { canInstall, installApp } = usePwaInstall();

  // UI 로컬 상태
  const [showSearch, setShowSearch] = useState(false);
  const [isMobileFrame, setIsMobileFrame] = useState(true);

  // 모달 제어 상태
  const [showAddBookmark, setShowAddBookmark] = useState(false);
  const [editingBookmark, setEditingBookmark] = useState<Bookmark | null>(null);
  const [movingBookmark, setMovingBookmark] = useState<Bookmark | null>(null);

  const [showAddFolder, setShowAddFolder] = useState(false);
  const [editingFolder, setEditingFolder] = useState<Folder | null>(null);
  const [parentForNewFolder, setParentForNewFolder] = useState<string | null>(null);

  const [showFolderTree, setShowFolderTree] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  return (
    <div className="min-h-screen bg-slate-200/60 flex items-center justify-center sm:p-4 select-none">
      {/* 폰 목업 프레임 또는 반응형 컨테이너 */}
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

        {/* 상단 헤더 & 검색 & 탭 네비게이션 */}
        <Header
          filter={filter}
          onSelectFilter={(f) => {
            setFilter(f);
            setSelectedTag(null);
          }}
          searchQuery={searchQuery}
          onSearchChange={setSearchQuery}
          showSearch={showSearch}
          onToggleSearch={() => setShowSearch(!showSearch)}
          isMobileFrame={isMobileFrame}
          onToggleFrame={() => setIsMobileFrame(!isMobileFrame)}
          onResetNavigation={resetNavigation}
          onOpenFolderTree={() => setShowFolderTree(true)}
          onOpenSettings={() => setShowSettings(true)}
          onInstallApp={installApp}
          canInstall={canInstall}
        />

        {/* 폴더 계층 경로 바 (브레드크럼) */}
        {!searchQuery && filter === 'ALL' && !selectedTag && (
          <BreadcrumbBar breadcrumbs={breadcrumbs} onSelectFolder={setCurrentFolderId} />
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

        {/* 메인 리스트 (하위 폴더 그리드 + 북마크 목록) */}
        <BookmarkList
          bookmarks={filteredBookmarks}
          subFolders={subFolders}
          folderStats={folderStats}
          filter={filter}
          selectedTag={selectedTag}
          searchQuery={searchQuery}
          defaultBrowser={defaultBrowser}
          onSelectFolder={setCurrentFolderId}
          onEditFolder={(folder) => setEditingFolder(folder)}
          onDeleteFolder={(folder) => {
            if (confirm(`'${folder.name}' 폴더를 삭제하시겠습니까?`)) {
              deleteFolder(folder.id);
            }
          }}
          onToggleFavorite={toggleFavorite}
          onToggleRead={toggleRead}
          onEditBookmark={(bm) => setEditingBookmark(bm)}
          onMoveBookmark={(bm) => setMovingBookmark(bm)}
          onMoveOrder={moveBookmarkOrder}
          onDeleteBookmark={(id) => {
            if (confirm('이 북마크를 삭제하시겠습니까?')) {
              deleteBookmark(id);
            }
          }}
          onSelectTag={setSelectedTag}
          onOpenAddBookmark={() => setShowAddBookmark(true)}
        />

        {/* 플로팅 액션 버튼 (FAB) */}
        <FabMenu
          onAddBookmark={() => setShowAddBookmark(true)}
          onAddFolder={() => {
            setParentForNewFolder(currentFolderId);
            setShowAddFolder(true);
          }}
        />

        {/* 팝업 모달 통합 관리 */}
        <ModalManager
          folders={folders}
          tags={tags}
          currentFolderId={currentFolderId}
          defaultBrowser={defaultBrowser}
          showAddBookmark={showAddBookmark}
          onCloseAddBookmark={() => setShowAddBookmark(false)}
          onSaveBookmark={(data) => {
            saveBookmark(data);
            setShowAddBookmark(false);
          }}
          editingBookmark={editingBookmark}
          onCloseEditBookmark={() => setEditingBookmark(null)}
          onSaveEditBookmark={(data) => {
            saveBookmark({ ...data, id: editingBookmark!.id });
            setEditingBookmark(null);
          }}
          movingBookmark={movingBookmark}
          onCloseMoveBookmark={() => setMovingBookmark(null)}
          onMoveBookmark={(targetFolderId) => {
            if (movingBookmark) {
              moveBookmark(movingBookmark.id, targetFolderId);
              setMovingBookmark(null);
            }
          }}
          showAddFolder={showAddFolder}
          parentForNewFolder={parentForNewFolder}
          onCloseAddFolder={() => setShowAddFolder(false)}
          onSaveFolder={(data) => {
            saveFolder(data);
            setShowAddFolder(false);
          }}
          editingFolder={editingFolder}
          onCloseEditFolder={() => setEditingFolder(null)}
          onSaveEditFolder={(data) => {
            saveFolder({ ...data, id: editingFolder!.id });
            setEditingFolder(null);
          }}
          onDeleteFolder={(folderId) => {
            if (confirm('이 폴더를 삭제하시겠습니까? 하위 폴더도 함께 정리됩니다.')) {
              deleteFolder(folderId);
            }
          }}
          showFolderTree={showFolderTree}
          onCloseFolderTree={() => setShowFolderTree(false)}
          onSelectFolderFromTree={(fId) => {
            setCurrentFolderId(fId);
            setSelectedTag(null);
          }}
          onAddSubFolderFromTree={(pId) => {
            setParentForNewFolder(pId);
            setShowAddFolder(true);
          }}
          showSettings={showSettings}
          onCloseSettings={() => setShowSettings(false)}
          onDataChanged={reloadData}
        />
      </div>
    </div>
  );
};
