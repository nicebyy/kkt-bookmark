import React, { useState } from 'react';
import { X, Folder as FolderIcon, Home } from 'lucide-react';
import { Folder } from '../types';

interface MoveFolderModalProps {
  folders: Folder[];
  currentFolderId: string | null;
  onClose: () => void;
  onMove: (targetFolderId: string | null) => void;
}

export const MoveFolderModal: React.FC<MoveFolderModalProps> = ({
  folders,
  currentFolderId,
  onClose,
  onMove,
}) => {
  const [selectedId, setSelectedId] = useState<string | null>(currentFolderId);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-sm bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-100 flex flex-col max-h-[80vh]">
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
          <h3 className="font-bold text-slate-800 text-base">이동할 대상 폴더 선택</h3>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-4 overflow-y-auto flex-1 space-y-1.5 text-sm">
          {/* 최상위 홈 */}
          <div
            onClick={() => setSelectedId(null)}
            className={`flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-colors ${
              selectedId === null ? 'bg-blue-50 border border-blue-200 text-blue-700 font-semibold' : 'hover:bg-slate-50 text-slate-700'
            }`}
          >
            <Home className="w-5 h-5 text-blue-600 shrink-0" />
            <span>최상위 (미분류 / 홈)</span>
          </div>

          {/* 폴더 목록 */}
          {folders.map((f) => (
            <div
              key={f.id}
              onClick={() => setSelectedId(f.id)}
              className={`flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-colors ${
                selectedId === f.id ? 'bg-blue-50 border border-blue-200 text-blue-700 font-semibold' : 'hover:bg-slate-50 text-slate-700'
              }`}
            >
              <div
                className="w-5 h-5 rounded-md flex items-center justify-center shrink-0"
                style={{ color: f.color }}
              >
                <FolderIcon className="w-5 h-5 fill-current" />
              </div>
              <span className="truncate">{f.name}</span>
            </div>
          ))}
        </div>

        <div className="flex justify-end gap-2 p-4 border-t border-slate-100 bg-slate-50">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-white text-xs font-medium"
          >
            취소
          </button>
          <button
            type="button"
            onClick={() => {
              onMove(selectedId);
              onClose();
            }}
            className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm transition-colors"
          >
            이동하기
          </button>
        </div>
      </div>
    </div>
  );
};
