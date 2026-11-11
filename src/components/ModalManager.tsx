import React from 'react';
import { Bookmark, Folder, Tag } from '../types';
import { AddEditBookmarkModal } from './AddEditBookmarkModal';
import { AddEditFolderModal } from './AddEditFolderModal';
import { FolderTreeModal } from './FolderTreeModal';
import { MoveFolderModal } from './MoveFolderModal';
import { SettingsModal } from './SettingsModal';

interface ModalManagerProps {
  folders: Folder[];
  tags: Tag[];
  currentFolderId: string | null;

  // 북마크 모달 상태
  showAddBookmark: boolean;
  onCloseAddBookmark: () => void;
  onSaveBookmark: (data: any) => void;
  editingBookmark: Bookmark | null;
  onCloseEditBookmark: () => void;
  onSaveEditBookmark: (data: any) => void;
  movingBookmark: Bookmark | null;
  onCloseMoveBookmark: () => void;
  onMoveBookmark: (targetFolderId: string | null) => void;

  // 폴더 모달 상태
  showAddFolder: boolean;
  parentForNewFolder: string | null;
  onCloseAddFolder: () => void;
  onSaveFolder: (data: any) => void;
  editingFolder: Folder | null;
  onCloseEditFolder: () => void;
  onSaveEditFolder: (data: any) => void;
  onDeleteFolder: (folderId: string) => void;

  defaultBrowser?: any;
  showFolderTree: boolean;
  onCloseFolderTree: () => void;
  onSelectFolderFromTree: (folderId: string) => void;
  onAddSubFolderFromTree: (parentId: string | null) => void;
  showSettings: boolean;
  onCloseSettings: () => void;
  onDataChanged: () => void;
}

export const ModalManager: React.FC<ModalManagerProps> = ({
  folders,
  tags,
  currentFolderId,
  defaultBrowser,

  showAddBookmark,
  onCloseAddBookmark,
  onSaveBookmark,
  editingBookmark,
  onCloseEditBookmark,
  onSaveEditBookmark,
  movingBookmark,
  onCloseMoveBookmark,
  onMoveBookmark,

  showAddFolder,
  parentForNewFolder,
  onCloseAddFolder,
  onSaveFolder,
  editingFolder,
  onCloseEditFolder,
  onSaveEditFolder,
  onDeleteFolder,

  showFolderTree,
  onCloseFolderTree,
  onSelectFolderFromTree,
  onAddSubFolderFromTree,
  showSettings,
  onCloseSettings,
  onDataChanged,
}) => {
  return (
    <>
      {/* 1. 새 북마크 추가 모달 */}
      {showAddBookmark && (
        <AddEditBookmarkModal
          defaultFolderId={currentFolderId}
          folders={folders}
          tags={tags}
          defaultBrowser={defaultBrowser}
          onClose={onCloseAddBookmark}
          onSave={onSaveBookmark}
        />
      )}

      {/* 2. 북마크 수정 모달 */}
      {editingBookmark && (
        <AddEditBookmarkModal
          initialBookmark={editingBookmark}
          defaultFolderId={editingBookmark.folderId}
          folders={folders}
          tags={tags}
          defaultBrowser={defaultBrowser}
          onClose={onCloseEditBookmark}
          onSave={onSaveEditBookmark}
        />
      )}

      {/* 3. 새 폴더 추가 모달 */}
      {showAddFolder && (
        <AddEditFolderModal
          defaultParentId={parentForNewFolder}
          folders={folders}
          onClose={onCloseAddFolder}
          onSave={onSaveFolder}
        />
      )}

      {/* 4. 폴더 수정 모달 */}
      {editingFolder && (
        <AddEditFolderModal
          initialFolder={editingFolder}
          defaultParentId={editingFolder.parentId}
          folders={folders}
          onClose={onCloseEditFolder}
          onSave={onSaveEditFolder}
        />
      )}

      {/* 5. 폴더 계층 관리 모달 */}
      {showFolderTree && (
        <FolderTreeModal
          folders={folders}
          onClose={onCloseFolderTree}
          onSelectFolder={onSelectFolderFromTree}
          onAddFolder={onAddSubFolderFromTree}
          onEditFolder={(folder) => {
            // 트리에서 수정 클릭
          }}
          onDeleteFolder={onDeleteFolder}
        />
      )}

      {/* 6. 폴더 이동 모달 */}
      {movingBookmark && (
        <MoveFolderModal
          folders={folders}
          currentFolderId={movingBookmark.folderId}
          onClose={onCloseMoveBookmark}
          onMove={onMoveBookmark}
        />
      )}

      {/* 7. 설정 및 백업 모달 */}
      {showSettings && (
        <SettingsModal onClose={onCloseSettings} onDataChanged={onDataChanged} />
      )}
    </>
  );
};
