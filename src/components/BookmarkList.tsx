import React from 'react';
import { Inbox } from 'lucide-react';
import { Bookmark, Folder, FilterType, BrowserType } from '../types';
import { FolderCard } from './FolderCard';
import { BookmarkCard } from './BookmarkCard';

interface BookmarkListProps {
  bookmarks: Bookmark[];
  subFolders: Folder[];
  folderStats: { [key: string]: { bookmarks: number; subFolders: number } };
  filter: FilterType;
  selectedTag: string | null;
  searchQuery: string;
  defaultBrowser: BrowserType;
  onSelectFolder: (folderId: string) => void;
  onEditFolder: (folder: Folder) => void;
  onDeleteFolder: (folder: Folder) => void;
  onToggleFavorite: (id: string) => void;
  onToggleRead: (id: string) => void;
  onEditBookmark: (bookmark: Bookmark) => void;
  onMoveBookmark: (bookmark: Bookmark) => void;
  onMoveOrder?: (id: string, direction: 'up' | 'down') => void;
  onDeleteBookmark: (id: string) => void;
  onSelectTag: (tag: string) => void;
  onOpenAddBookmark: () => void;
}

export const BookmarkList: React.FC<BookmarkListProps> = ({
  bookmarks,
  subFolders,
  folderStats,
  filter,
  selectedTag,
  searchQuery,
  defaultBrowser,
  onSelectFolder,
  onEditFolder,
  onDeleteFolder,
  onToggleFavorite,
  onToggleRead,
  onEditBookmark,
  onMoveBookmark,
  onMoveOrder,
  onDeleteBookmark,
  onSelectTag,
  onOpenAddBookmark,
}) => {
  const showSubFolders = !searchQuery && filter === 'ALL' && !selectedTag && subFolders.length > 0;

  return (
    <main className="flex-1 overflow-y-auto p-4 space-y-4">
      {/* 1. 하위 폴더 그리드 */}
      {showSubFolders && (
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
                  onClick={() => onSelectFolder(folder.id)}
                  onEdit={() => onEditFolder(folder)}
                  onDelete={() => onDeleteFolder(folder)}
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
            북마크 ({bookmarks.length})
          </h3>
        </div>

        {bookmarks.length === 0 ? (
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
              onClick={onOpenAddBookmark}
              className="mt-4 px-4 py-2 rounded-xl bg-blue-50 text-blue-600 hover:bg-blue-100 text-xs font-semibold transition-colors"
            >
              + 북마크 추가하기
            </button>
          </div>
        ) : (
          <div className="space-y-2.5">
            {bookmarks.map((bookmark) => (
              <BookmarkCard
                key={bookmark.id}
                bookmark={bookmark}
                defaultBrowser={defaultBrowser}
                onToggleFavorite={() => onToggleFavorite(bookmark.id)}
                onToggleRead={() => onToggleRead(bookmark.id)}
                onEdit={() => onEditBookmark(bookmark)}
                onMove={() => onMoveBookmark(bookmark)}
                onMoveOrder={onMoveOrder ? (dir) => onMoveOrder(bookmark.id, dir) : undefined}
                onDelete={() => onDeleteBookmark(bookmark.id)}
                onTagClick={onSelectTag}
              />
            ))}
          </div>
        )}
      </div>
    </main>
  );
};
