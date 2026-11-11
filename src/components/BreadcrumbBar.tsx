import React from 'react';
import { Home, ChevronRight } from 'lucide-react';
import { Folder } from '../types';

interface BreadcrumbBarProps {
  breadcrumbs: Folder[];
  onSelectFolder: (folderId: string | null) => void;
}

export const BreadcrumbBar: React.FC<BreadcrumbBarProps> = ({ breadcrumbs, onSelectFolder }) => {
  return (
    <div className="flex items-center gap-1.5 px-4 py-2.5 overflow-x-auto no-scrollbar bg-slate-50 border-b border-slate-200 text-sm">
      <button
        onClick={() => onSelectFolder(null)}
        className={`flex items-center gap-1 px-2 py-1 rounded-md transition-colors ${
          breadcrumbs.length === 0
            ? 'font-bold text-blue-600 bg-blue-50'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
        }`}
      >
        <Home className="w-4 h-4" />
        <span>홈</span>
      </button>

      {breadcrumbs.map((folder, index) => {
        const isLast = index === breadcrumbs.length - 1;
        return (
          <React.Fragment key={folder.id}>
            <ChevronRight className="w-3.5 h-3.5 text-slate-400 shrink-0" />
            <button
              onClick={() => onSelectFolder(folder.id)}
              className={`px-2 py-1 rounded-md truncate max-w-[140px] transition-colors ${
                isLast
                  ? 'font-bold text-blue-600 bg-blue-50'
                  : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
              }`}
            >
              {folder.name}
            </button>
          </React.Fragment>
        );
      })}
    </div>
  );
};
