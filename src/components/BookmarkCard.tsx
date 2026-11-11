import React, { useState } from 'react';
import { Star, CheckCircle, Circle, MoreVertical, Edit2, FolderInput, Copy, Trash2, Globe, ArrowUp, ArrowDown } from 'lucide-react';
import { Bookmark, BrowserType, SUPPORTED_BROWSERS } from '../types';
import { openInBrowser } from '../services/browserLauncher';

interface BookmarkCardProps {
  bookmark: Bookmark;
  defaultBrowser?: BrowserType;
  onToggleFavorite: () => void;
  onToggleRead: () => void;
  onEdit: () => void;
  onMove: () => void;
  onMoveOrder?: (direction: 'up' | 'down') => void;
  onDelete: () => void;
  onTagClick: (tag: string) => void;
}

export const BookmarkCard: React.FC<BookmarkCardProps> = ({
  bookmark,
  defaultBrowser = 'default',
  onToggleFavorite,
  onToggleRead,
  onEdit,
  onMove,
  onMoveOrder,
  onDelete,
  onTagClick,
}) => {
  const [showMenu, setShowMenu] = useState(false);
  const [showBrowserSubmenu, setShowBrowserSubmenu] = useState(false);
  const [copied, setCopied] = useState(false);
  const [faviconError, setFaviconError] = useState(false);

  let host = bookmark.url;
  try {
    host = new URL(bookmark.url).hostname.replace(/^www\./, '');
  } catch {
    // ignore
  }

  const faviconUrl = bookmark.faviconUrl || `https://www.google.com/s2/favicons?domain=${host}&sz=128`;
  const firstLetter = (host[0] || 'W').toUpperCase();

  const browserOption = SUPPORTED_BROWSERS.find((b) => b.id === (bookmark.preferredBrowser || defaultBrowser));

  const handleOpenLink = (specifiedBrowser?: BrowserType) => {
    if (!bookmark.isRead) {
      onToggleRead();
    }
    const targetBrowser = specifiedBrowser || bookmark.preferredBrowser || defaultBrowser;
    openInBrowser(bookmark.url, targetBrowser);
  };

  const handleCopyLink = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigator.clipboard.writeText(bookmark.url);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
    setShowMenu(false);
  };

  return (
    <div
      onClick={() => handleOpenLink()}
      onContextMenu={(e) => {
        e.preventDefault();
        onEdit();
      }}
      className={`relative p-3.5 bg-white rounded-xl border transition-all cursor-pointer group shadow-sm hover:shadow-md ${
        bookmark.isRead ? 'border-slate-200/60 opacity-90' : 'border-slate-200 hover:border-blue-400'
      }`}
    >
      <div className="flex items-start gap-3">
        {/* 좌측: 직관적인 사이트 대표 아이콘 (42x42px 둥근 컨테이너) */}
        <div className="w-10 h-10 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-center shrink-0 overflow-hidden shadow-xs group-hover:scale-105 transition-transform mt-0.5">
          {!faviconError ? (
            <img
              src={faviconUrl}
              alt={host}
              className="w-5 h-5 object-contain"
              onError={() => setFaviconError(true)}
            />
          ) : (
            <div className="w-full h-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white font-bold text-sm">
              {firstLetter}
            </div>
          )}
        </div>

        {/* 우측 전체: 도메인/액션 + 제목 + 메모 + 태그 */}
        <div className="flex-1 min-w-0">
          {/* 상단 행: 도메인 + 브라우저 뱃지 + 액션 버튼들 */}
          <div className="flex items-center justify-between gap-1 mb-1">
            <div className="flex items-center gap-1.5 min-w-0">
              <span className="text-xs text-slate-400 truncate font-medium">{host}</span>

              {/* 특정 브라우저 지정 뱃지 */}
              {bookmark.preferredBrowser && bookmark.preferredBrowser !== 'default' && browserOption && (
                <span
                  className="px-1.5 py-0.2 rounded text-[10px] font-bold tracking-tight shrink-0"
                  style={{ backgroundColor: `${browserOption.badgeColor}18`, color: browserOption.badgeColor }}
                >
                  {browserOption.name.split(' ')[0]}
                </span>
              )}
            </div>

            <div className="flex items-center gap-0.5 shrink-0">
              {/* 북마크 수정 바로가기 버튼 */}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onEdit();
                }}
                title="북마크 수정"
                className="p-1 rounded-md text-slate-400 hover:text-blue-600 hover:bg-slate-100 transition-colors"
              >
                <Edit2 className="w-4 h-4" />
              </button>

              {/* 읽음 토글 */}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onToggleRead();
                }}
                title={bookmark.isRead ? '읽지 않음으로 표시' : '읽음으로 표시'}
                className="p-1 rounded-md text-slate-400 hover:text-emerald-600 hover:bg-slate-100 transition-colors"
              >
                {bookmark.isRead ? (
                  <CheckCircle className="w-4 h-4 text-emerald-500 fill-emerald-50" />
                ) : (
                  <Circle className="w-4 h-4" />
                )}
              </button>

              {/* 즐겨찾기 토글 */}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onToggleFavorite();
                }}
                title={bookmark.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 추가'}
                className="p-1 rounded-md text-slate-400 hover:text-amber-500 hover:bg-slate-100 transition-colors"
              >
                <Star
                  className={`w-4 h-4 ${
                    bookmark.isFavorite ? 'text-amber-500 fill-amber-500' : ''
                  }`}
                />
              </button>

              {/* 더보기 메뉴 */}
              <div className="relative">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    setShowMenu(!showMenu);
                    setShowBrowserSubmenu(false);
                  }}
                  className="p-1.5 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                >
                  <MoreVertical className="w-4 h-4" />
                </button>

                {showMenu && (
                  <>
                    <div
                      className="fixed inset-0 z-20"
                      onClick={(e) => {
                        e.stopPropagation();
                        setShowMenu(false);
                        setShowBrowserSubmenu(false);
                      }}
                    />
                    <div className="absolute right-0 top-8 w-44 bg-white rounded-xl shadow-lg border border-slate-100 py-1 z-30 text-xs">
                      <div>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            setShowBrowserSubmenu(!showBrowserSubmenu);
                          }}
                          className="w-full px-3 py-2 text-left flex items-center justify-between hover:bg-slate-50 text-slate-700 font-medium"
                        >
                          <span className="flex items-center gap-2">
                            <Globe className="w-3.5 h-3.5 text-blue-600" />
                            브라우저 선택 실행
                          </span>
                          <span className="text-[10px] text-slate-400">{showBrowserSubmenu ? '▲' : '▶'}</span>
                        </button>

                        {showBrowserSubmenu && (
                          <div className="bg-slate-50 py-1 border-y border-slate-100">
                            {SUPPORTED_BROWSERS.map((b) => (
                              <button
                                key={b.id}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setShowMenu(false);
                                  handleOpenLink(b.id);
                                }}
                                className="w-full px-4 py-1.5 text-left text-[11px] hover:bg-blue-50 hover:text-blue-600 flex items-center justify-between text-slate-600"
                              >
                                <span>{b.name}</span>
                                {b.id === (bookmark.preferredBrowser || defaultBrowser) && (
                                  <span className="text-[10px] text-blue-600 font-bold">✓</span>
                                )}
                              </button>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="my-1 border-t border-slate-100" />

                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setShowMenu(false);
                          onEdit();
                        }}
                        className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-slate-50 text-slate-700"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                        북마크 수정
                      </button>
                      {onMoveOrder && (
                        <>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setShowMenu(false);
                              onMoveOrder('up');
                            }}
                            className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-slate-50 text-slate-700"
                          >
                            <ArrowUp className="w-3.5 h-3.5 text-slate-500" />
                            위로 이동 (▲)
                          </button>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              setShowMenu(false);
                              onMoveOrder('down');
                            }}
                            className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-slate-50 text-slate-700"
                          >
                            <ArrowDown className="w-3.5 h-3.5 text-slate-500" />
                            아래로 이동 (▼)
                          </button>
                        </>
                      )}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setShowMenu(false);
                          onMove();
                        }}
                        className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-slate-50 text-slate-700"
                      >
                        <FolderInput className="w-3.5 h-3.5" />
                        다른 폴더로 이동
                      </button>
                      <button
                        onClick={handleCopyLink}
                        className="w-full px-3 py-2 text-left flex items-center gap-2 hover:bg-slate-50 text-slate-700"
                      >
                        <Copy className="w-3.5 h-3.5" />
                        링크 복사
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
                        삭제
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>

          {/* 제목 & 우측 OG 이미지 */}
          <div className="flex gap-2 items-start justify-between">
            <div className="flex-1 min-w-0">
              <h3 className="font-semibold text-slate-800 text-sm leading-snug line-clamp-2 group-hover:text-blue-600 transition-colors">
                {bookmark.title}
              </h3>
              {(bookmark.memo || bookmark.description) && (
                <p className="text-xs text-slate-500 mt-1 line-clamp-2 leading-relaxed">
                  {bookmark.memo || bookmark.description}
                </p>
              )}
            </div>

            {bookmark.imageUrl && (
              <img
                src={bookmark.imageUrl}
                alt=""
                className="w-14 h-14 rounded-lg object-cover bg-slate-100 shrink-0 border border-slate-100 ml-2"
                onError={(e) => { (e.currentTarget as HTMLElement).style.display = 'none'; }}
              />
            )}
          </div>

          {/* 하단: 태그 칩들 */}
          {bookmark.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2 pt-1.5 border-t border-slate-100">
              {bookmark.tags.map((tag, idx) => (
                <button
                  key={idx}
                  onClick={(e) => {
                    e.stopPropagation();
                    onTagClick(tag);
                  }}
                  className="px-1.5 py-0.5 rounded text-[10px] font-medium bg-blue-50 text-blue-600 hover:bg-blue-100 transition-colors"
                >
                  #{tag}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {copied && (
        <div className="absolute bottom-2 right-2 px-2.5 py-1 bg-slate-900 text-white text-xs rounded-md shadow-md z-10">
          링크가 복사되었습니다!
        </div>
      )}
    </div>
  );
};
