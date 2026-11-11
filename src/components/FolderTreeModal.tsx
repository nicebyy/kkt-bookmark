import React from 'react';
import { X, Folder as FolderIcon, Plus, Edit2, Trash2, CornerDownRight } from 'lucide-react';
import { Folder } from '../types';

interface FolderTreeModalProps {
  folders: Folder[];
  onClose: () => void;
  onSelectFolder: (folderId: string) => void;
  onAddFolder: (parentId: string | null) => void;
  onEditFolder: (folder: Folder) => void;
  onDeleteFolder: (folderId: string) => void;
}

interface TreeNode {
  folder: Folder;
  depth: number;
}

export const FolderTreeModal: React.FC<FolderTreeModalProps> = ({
  folders,
  onClose,
  onSelectFolder,
  onAddFolder,
  onEditFolder,
  onDeleteFolder,
}) => {
  // 계층 트리 리스트 구성
  const treeNodes: TreeNode[] = [];
  const byParent: { [key: string]: Folder[] } = {};

  folders.forEach((f) => {
    const p = f.parentId || 'root';
    if (!byParent[p]) byParent[p] = [];
    byParent[p].push(f);
  });

  const traverse = (parentId: string | null, depth: number) => {
    const children = byParent[parentId || 'root'] || [];
    children.forEach((c) => {
      treeNodes.push({ folder: c, depth });
      traverse(c.id, depth + 1);
    });
  };

  traverse(null, 0);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-100 flex flex-col max-h-[85vh]">
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
          <div>
            <h3 className="font-bold text-slate-800 text-base">폴더 계층 트리 관리</h3>
            <p className="text-xs text-slate-400 mt-0.5">폴더를 클릭하면 해당 위치로 이동합니다.</p>
          </div>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto flex-1 space-y-2">
          {treeNodes.length === 0 ? (
            <div className="text-center py-10 text-slate-400 text-sm">생성된 폴더가 없습니다.</div>
          ) : (
            treeNodes.map(({ folder, depth }) => (
              <div
                key={folder.id}
                style={{ paddingLeft: `${depth * 20 + 8}px` }}
                className="flex items-center justify-between p-2.5 rounded-xl border border-slate-100 hover:border-blue-200 hover:bg-slate-50 transition-all group"
              >
                <div
                  onClick={() => {
                    onSelectFolder(folder.id);
                    onClose();
                  }}
                  className="flex items-center gap-2 cursor-pointer min-w-0 flex-1"
                >
                  {depth > 0 && <CornerDownRight className="w-3.5 h-3.5 text-slate-400 shrink-0" />}
                  <div
                    className="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
                    style={{ backgroundColor: `${folder.color}20`, color: folder.color }}
                  >
                    <FolderIcon className="w-4 h-4 fill-current" />
                  </div>
                  <div className="min-w-0">
                    <span className="text-sm font-semibold text-slate-800 truncate block group-hover:text-blue-600">
                      {folder.name}
                    </span>
                    <span className="text-[10px] text-slate-400">{depth + 1}단계 계층</span>
                  </div>
                </div>

                <div className="flex items-center gap-1 shrink-0">
                  <button
                    onClick={() => onAddFolder(folder.id)}
                    title="이 폴더 안에 하위 폴더 추가"
                    className="p-1.5 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => onEditFolder(folder)}
                    title="수정"
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => onDeleteFolder(folder.id)}
                    title="삭제"
                    className="p-1.5 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        <div className="p-4 border-t border-slate-100 bg-slate-50 flex justify-between items-center">
          <button
            onClick={() => onAddFolder(null)}
            className="flex items-center gap-1.5 px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold shadow-sm transition-colors"
          >
            <Plus className="w-4 h-4" />
            최상위 루트 폴더 추가
          </button>
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-white text-xs font-medium"
          >
            닫기
          </button>
        </div>
      </div>
    </div>
  );
};
