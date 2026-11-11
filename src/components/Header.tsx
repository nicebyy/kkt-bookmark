import React from 'react';
import {
  Bookmark as BookmarkIcon,
  Search,
  FolderTree,
  Settings as SettingsIcon,
  Star,
  CheckCircle,
  Smartphone,
  Monitor,
  X,
  Download,
} from 'lucide-react';
import { FilterType } from '../types';

interface HeaderProps {
  filter: FilterType;
  onSelectFilter: (filter: FilterType) => void;
  searchQuery: string;
  onSearchChange: (query: string) => void;
  showSearch: boolean;
  onToggleSearch: () => void;
  isMobileFrame: boolean;
  onToggleFrame: () => void;
  onResetNavigation: () => void;
  onOpenFolderTree: () => void;
  onOpenSettings: () => void;
  onInstallApp: () => void;
  canInstall: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  filter,
  onSelectFilter,
  searchQuery,
  onSearchChange,
  showSearch,
  onToggleSearch,
  isMobileFrame,
  onToggleFrame,
  onResetNavigation,
  onOpenFolderTree,
  onOpenSettings,
  onInstallApp,
  canInstall,
}) => {
  return (
    <header className="bg-white border-b border-slate-200 shrink-0 z-10">
      <div className="flex items-center justify-between px-4 py-3">
        {/* 앱 로고 & 타이틀 */}
        <div onClick={onResetNavigation} className="flex items-center gap-2 cursor-pointer">
          <div className="w-8 h-8 rounded-xl bg-blue-600 text-white flex items-center justify-center shadow-sm">
            <BookmarkIcon className="w-4 h-4 fill-current" />
          </div>
          <h1 className="font-extrabold text-slate-800 text-base tracking-tight">KKT 북마크</h1>
        </div>

        {/* 우측 상단 액션들 */}
        <div className="flex items-center gap-1">
          {canInstall && (
            <button
              onClick={onInstallApp}
              title="내 기기에 어플로 설치하기"
              className="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-blue-50 text-blue-600 hover:bg-blue-100 text-xs font-bold transition-all border border-blue-200"
            >
              <Download className="w-3.5 h-3.5" />
              <span>앱 설치</span>
            </button>
          )}

          <button
            onClick={onToggleSearch}
            title="검색"
            className={`p-2 rounded-xl transition-colors ${
              showSearch ? 'bg-blue-50 text-blue-600' : 'text-slate-500 hover:bg-slate-100'
            }`}
          >
            <Search className="w-4 h-4" />
          </button>

          <button
            onClick={onOpenFolderTree}
            title="폴더 계층 트리 관리"
            className="p-2 rounded-xl text-slate-500 hover:bg-slate-100 hover:text-blue-600 transition-colors"
          >
            <FolderTree className="w-4 h-4" />
          </button>

          <button
            onClick={onOpenSettings}
            title="설정 및 백업"
            className="p-2 rounded-xl text-slate-500 hover:bg-slate-100 hover:text-blue-600 transition-colors"
          >
            <SettingsIcon className="w-4 h-4" />
          </button>

          {/* PC에서 모바일 프레임 토글 버튼 */}
          <button
            onClick={onToggleFrame}
            title={isMobileFrame ? '와이드 화면으로 보기' : '스마트폰 뷰로 보기'}
            className="hidden sm:block p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors ml-1"
          >
            {isMobileFrame ? <Monitor className="w-4 h-4" /> : <Smartphone className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* 실시간 검색창 인풋 */}
      {showSearch && (
        <div className="px-4 pb-3 animate-fade-in">
          <div className="relative flex items-center">
            <Search className="w-4 h-4 absolute left-3 text-slate-400" />
            <input
              type="text"
              autoFocus
              placeholder="제목, 메모, URL, #태그 검색..."
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              className="w-full pl-9 pr-8 py-2 rounded-xl bg-slate-100 border border-transparent focus:border-blue-500 focus:bg-white text-xs outline-none transition-all"
            />
            {searchQuery && (
              <button
                onClick={() => onSearchChange('')}
                className="absolute right-2.5 p-1 rounded-full text-slate-400 hover:text-slate-600"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        </div>
      )}

      {/* 3단 필터 탭 (전체, ★ 즐겨찾기, ✉ 읽지 않음) */}
      <div className="grid grid-cols-3 border-t border-slate-100 text-xs font-semibold">
        <button
          onClick={() => onSelectFilter('ALL')}
          className={`py-2.5 text-center transition-colors border-b-2 ${
            filter === 'ALL'
              ? 'border-blue-600 text-blue-600 bg-blue-50/30'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          전체
        </button>
        <button
          onClick={() => onSelectFilter('FAVORITES')}
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
          onClick={() => onSelectFilter('UNREAD')}
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
  );
};
