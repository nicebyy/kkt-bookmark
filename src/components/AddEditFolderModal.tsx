import React, { useState } from 'react';
import { X } from 'lucide-react';
import { Folder, FOLDER_COLORS } from '../types';

interface AddEditFolderModalProps {
  initialFolder?: Folder;
  defaultParentId: string | null;
  folders: Folder[];
  onClose: () => void;
  onSave: (data: { name: string; parentId: string | null; color: string }) => void;
}

export const AddEditFolderModal: React.FC<AddEditFolderModalProps> = ({
  initialFolder,
  defaultParentId,
  folders,
  onClose,
  onSave,
}) => {
  const [name, setName] = useState(initialFolder?.name || '');
  const [parentId, setParentId] = useState<string | null>(
    initialFolder ? initialFolder.parentId : defaultParentId
  );
  const [color, setColor] = useState(initialFolder?.color || FOLDER_COLORS[0]);

  // 자기 자신 및 자신의 자식은 부모로 선택 불가
  const eligibleParents = folders.filter((f) => f.id !== initialFolder?.id);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    onSave({
      name: name.trim(),
      parentId,
      color,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-100">
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
          <h3 className="font-bold text-slate-800 text-base">
            {initialFolder ? '폴더 수정' : '새 폴더 생성'}
          </h3>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4 text-sm">
          {/* 폴더 이름 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">폴더 이름</label>
            <input
              type="text"
              required
              placeholder="예: 📚 독서 노트, 🎨 디자인"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 text-sm"
            />
          </div>

          {/* 상위 폴더 선택 (트리 구조) */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">상위 폴더 위치 (계층 구조)</label>
            <select
              value={parentId || ''}
              onChange={(e) => setParentId(e.target.value || null)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 bg-white focus:outline-none focus:border-blue-500 text-sm cursor-pointer"
            >
              <option value="">📂 최상위 (루트 폴더)</option>
              {eligibleParents.map((f) => (
                <option key={f.id} value={f.id}>
                  📁 {f.name}
                </option>
              ))}
            </select>
          </div>

          {/* 폴더 색상 선택 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-2">폴더 테마 색상</label>
            <div className="flex items-center justify-between gap-2">
              {FOLDER_COLORS.map((c) => (
                <button
                  type="button"
                  key={c}
                  onClick={() => setColor(c)}
                  className={`w-9 h-9 rounded-full transition-all flex items-center justify-center ${
                    color === c ? 'ring-4 ring-blue-200 scale-110 shadow-sm' : 'hover:scale-105'
                  }`}
                  style={{ backgroundColor: c }}
                />
              ))}
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-100 font-medium"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={!name.trim()}
              className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-medium shadow-sm disabled:opacity-50 transition-colors"
            >
              {initialFolder ? '수정 완료' : '생성하기'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
