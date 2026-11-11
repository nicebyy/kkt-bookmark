import React, { useState } from 'react';
import { Plus, FolderPlus, BookmarkPlus } from 'lucide-react';

interface FabMenuProps {
  onAddBookmark: () => void;
  onAddFolder: () => void;
}

export const FabMenu: React.FC<FabMenuProps> = ({ onAddBookmark, onAddFolder }) => {
  const [open, setOpen] = useState(false);

  return (
    <div className="absolute bottom-5 right-5 z-20 flex flex-col items-end gap-2.5">
      {open && (
        <>
          {/* 폴더 생성 서브 버튼 */}
          <button
            onClick={() => {
              setOpen(false);
              onAddFolder();
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-full bg-white text-slate-700 shadow-lg border border-slate-200 text-xs font-semibold hover:bg-slate-50 transition-all animate-bounce-short"
          >
            <FolderPlus className="w-4 h-4 text-emerald-600" />
            <span>폴더 생성</span>
          </button>

          {/* 북마크 추가 서브 버튼 */}
          <button
            onClick={() => {
              setOpen(false);
              onAddBookmark();
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
        onClick={() => setOpen(!open)}
        className={`w-14 h-14 rounded-2xl flex items-center justify-center text-white shadow-xl transition-all duration-200 ${
          open ? 'bg-slate-800 rotate-45' : 'bg-blue-600 hover:bg-blue-700 hover:scale-105'
        }`}
      >
        <Plus className="w-6 h-6 stroke-[2.5]" />
      </button>
    </div>
  );
};
