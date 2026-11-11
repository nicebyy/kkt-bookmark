import React, { useState } from 'react';
import { Folder as FolderIcon, MoreVertical, Edit2, Trash2 } from 'lucide-react';
import { Folder } from '../types';

interface FolderCardProps {
  folder: Folder;
  bookmarkCount: number;
  subFolderCount: number;
  onClick: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

export const FolderCard: React.FC<FolderCardProps> = ({
  folder,
  bookmarkCount,
  subFolderCount,
  onClick,
  onEdit,
  onDelete,
}) => {
  const [showMenu, setShowMenu] = useState(false);

  return (
    <div
      onClick={onClick}
      className="relative flex items-center justify-between p-3.5 bg-white rounded-xl border border-slate-200/80 shadow-sm hover:shadow-md hover:border-blue-300 transition-all cursor-pointer group"
    >
      <div className="flex items-center gap-3 min-w-0">
        <div
          className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
          style={{ backgroundColor: `${folder.color}15`, color: folder.color }}
        >
          <FolderIcon className="w-5 h-5 fill-current" />
        </div>
        <div className="min-w-0">
          <h4 className="font-semibold text-slate-800 text-sm truncate group-hover:text-blue-600 transition-colors">
            {folder.name}
          </h4>
          <p className="text-xs text-slate-400 mt-0.5">
            {subFolderCount > 0 && `하위 ${subFolderCount}개 • `}북마크 {bookmarkCount}개
          </p>
        </div>
      </div>

      <div className="relative">
        <button
          onClick={(e) => {
            e.stopPropagation();
            setShowMenu(!showMenu);
          }}
          className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
        >
          <MoreVertical className="w-4 h-4" />
        </button>

        {showMenu && (
          <>
            <div className="fixed inset-0 z-20" onClick={(e) => { e.stopPropagation(); setShowMenu(false); }} />
            <div className="absolute right-0 top-8 w-36 bg-white rounded-xl shadow-lg border border-slate-100 py-1 z-30 text-xs">
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu(false);
                  onEdit();
                }}
                className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-slate-50 text-slate-700"
              >
                <Edit2 className="w-3.5 h-3.5" />
                이름/색상 수정
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setShowMenu(false);
                  onDelete();
                }}
                className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-red-50 text-red-600"
              >
                <Trash2 className="w-3.5 h-3.5" />
                폴더 삭제
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
};
