import React, { useState } from 'react';
import { X, Sparkles, Loader2, Tag as TagIcon, Folder as FolderIcon, Globe } from 'lucide-react';
import { Bookmark, Folder, Tag, BrowserType, SUPPORTED_BROWSERS } from '../types';
import { fetchMetadata } from '../services/metadata';

interface AddEditBookmarkModalProps {
  initialBookmark?: Bookmark;
  defaultFolderId: string | null;
  folders: Folder[];
  tags: Tag[];
  onClose: () => void;
  onSave: (data: {
    url: string;
    title: string;
    memo?: string;
    imageUrl?: string;
    faviconUrl?: string;
    folderId: string | null;
    tags: string[];
    preferredBrowser?: BrowserType;
  }) => void;
  defaultBrowser?: BrowserType;
}

export const AddEditBookmarkModal: React.FC<AddEditBookmarkModalProps> = ({
  initialBookmark,
  defaultFolderId,
  folders,
  tags,
  defaultBrowser,
  onClose,
  onSave,
}) => {
  const [url, setUrl] = useState(initialBookmark?.url || '');
  const [title, setTitle] = useState(initialBookmark?.title || '');
  const [memo, setMemo] = useState(initialBookmark?.memo || '');
  const [imageUrl, setImageUrl] = useState(initialBookmark?.imageUrl || '');
  const [faviconUrl, setFaviconUrl] = useState(initialBookmark?.faviconUrl || '');
  const [selectedFolderId, setSelectedFolderId] = useState<string | null>(
    initialBookmark ? initialBookmark.folderId : defaultFolderId
  );
  const [preferredBrowser, setPreferredBrowser] = useState<BrowserType>(
    initialBookmark?.preferredBrowser || defaultBrowser || 'default'
  );
  const [currentTags, setCurrentTags] = useState<string[]>(initialBookmark?.tags || []);
  const [tagInput, setTagInput] = useState('');
  const [loadingMeta, setLoadingMeta] = useState(false);

  const handleFetchMetadata = async () => {
    if (!url.trim()) return;
    setLoadingMeta(true);
    try {
      const meta = await fetchMetadata(url);
      if (!title) setTitle(meta.title);
      if (meta.imageUrl && !imageUrl) setImageUrl(meta.imageUrl);
      if (meta.faviconUrl && !faviconUrl) setFaviconUrl(meta.faviconUrl);
    } finally {
      setLoadingMeta(false);
    }
  };

  const handleAddTag = (raw: string) => {
    const clean = raw.trim().replace(/^#/, '');
    if (clean && !currentTags.includes(clean)) {
      setCurrentTags([...currentTags, clean]);
    }
    setTagInput('');
  };

  const handleRemoveTag = (tag: string) => {
    setCurrentTags(currentTags.filter((t) => t !== tag));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!url.trim() || !title.trim()) return;
    onSave({
      url: url.trim(),
      title: title.trim(),
      memo: memo.trim() || undefined,
      imageUrl: imageUrl.trim() || undefined,
      faviconUrl: faviconUrl.trim() || undefined,
      folderId: selectedFolderId,
      tags: currentTags,
      preferredBrowser,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-lg bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-100 flex flex-col max-h-[90vh]">
        {/* 모달 헤더 */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
          <h3 className="font-bold text-slate-800 text-base">
            {initialBookmark ? '북마크 수정' : '새 북마크 추가'}
          </h3>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* 폼 입력창 */}
        <form onSubmit={handleSubmit} className="p-5 overflow-y-auto space-y-4 text-sm flex-1">
          {/* URL 입력 + 자동 추출 버튼 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">웹 링크 URL</label>
            <div className="relative flex items-center">
              <input
                type="text"
                required
                placeholder="https://..."
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                className="w-full pl-3 pr-24 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 text-sm"
              />
              <button
                type="button"
                onClick={handleFetchMetadata}
                disabled={!url.trim() || loadingMeta}
                className="absolute right-1.5 px-3 py-1.5 rounded-lg bg-blue-50 text-blue-600 hover:bg-blue-100 disabled:opacity-50 text-xs font-medium flex items-center gap-1 transition-all"
              >
                {loadingMeta ? <Loader2 className="w-3.5 h-3.5 animate-spin" /> : <Sparkles className="w-3.5 h-3.5" />}
                정보 추출
              </button>
            </div>
          </div>

          {/* 썸네일 미리보기 */}
          {imageUrl && (
            <div className="relative rounded-xl overflow-hidden border border-slate-200 h-32 bg-slate-100 group">
              <img src={imageUrl} alt="" className="w-full h-full object-cover" />
              <button
                type="button"
                onClick={() => setImageUrl('')}
                className="absolute top-2 right-2 p-1 rounded-full bg-slate-900/60 text-white hover:bg-slate-900"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          )}

          {/* 제목 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">제목</label>
            <input
              type="text"
              required
              placeholder="페이지 제목"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 text-sm"
            />
          </div>

          {/* 메모 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">메모 / 코멘트 (선택사항)</label>
            <textarea
              rows={2}
              placeholder="나만의 메모를 기록해보세요."
              value={memo}
              onChange={(e) => setMemo(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 text-sm resize-none"
            />
          </div>

          {/* 보관할 폴더 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">보관할 폴더</label>
            <div className="relative">
              <select
                value={selectedFolderId || ''}
                onChange={(e) => setSelectedFolderId(e.target.value || null)}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 bg-white focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 text-sm appearance-none cursor-pointer"
              >
                <option value="">📂 최상위 (미분류 / 홈)</option>
                {folders.map((f) => (
                  <option key={f.id} value={f.id}>
                    📁 {f.name}
                  </option>
                ))}
              </select>
              <div className="absolute right-3 top-3 pointer-events-none text-slate-400">
                <FolderIcon className="w-4 h-4" />
              </div>
            </div>
          </div>

          {/* 실행할 브라우저 지정 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">
              실행할 브라우저 지정
            </label>
            <div className="relative">
              <select
                value={preferredBrowser}
                onChange={(e) => setPreferredBrowser(e.target.value as BrowserType)}
                className="w-full px-3 py-2.5 rounded-xl border border-slate-200 bg-white focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 text-sm appearance-none cursor-pointer"
              >
                {SUPPORTED_BROWSERS.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.id === 'default' ? '🌐 시스템 기본 브라우저' : `🚀 ${b.name}`}
                  </option>
                ))}
              </select>
              <div className="absolute right-3 top-3 pointer-events-none text-slate-400">
                <Globe className="w-4 h-4" />
              </div>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              모바일 기기에서는 해당 브라우저 앱으로 바로 실행됩니다.
            </p>
          </div>

          {/* 다중 태그 입력 */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">다중 태그 분류</label>
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="태그 입력 후 Enter (#개발, #팁)"
                value={tagInput}
                onChange={(e) => {
                  if (e.target.value.endsWith(' ') || e.target.value.endsWith(',')) {
                    handleAddTag(e.target.value.slice(0, -1));
                  } else {
                    setTagInput(e.target.value);
                  }
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault();
                    handleAddTag(tagInput);
                  }
                }}
                className="flex-1 px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:border-blue-500 text-sm"
              />
              <button
                type="button"
                onClick={() => handleAddTag(tagInput)}
                className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 font-medium text-slate-700 text-xs shrink-0"
              >
                추가
              </button>
            </div>

            {/* 현재 추가된 태그들 */}
            {currentTags.length > 0 && (
              <div className="flex flex-wrap gap-1.5 mt-2">
                {currentTags.map((tag) => (
                  <span
                    key={tag}
                    onClick={() => handleRemoveTag(tag)}
                    className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-700 cursor-pointer hover:bg-red-100 hover:text-red-700 transition-colors"
                  >
                    #{tag}
                    <X className="w-3 h-3" />
                  </span>
                ))}
              </div>
            )}

            {/* 빠른 추천 태그들 */}
            {tags.filter((t) => !currentTags.includes(t.name)).length > 0 && (
              <div className="mt-2 text-xs text-slate-400">
                <span>추천 태그: </span>
                {tags
                  .filter((t) => !currentTags.includes(t.name))
                  .slice(0, 6)
                  .map((t) => (
                    <button
                      type="button"
                      key={t.id}
                      onClick={() => handleAddTag(t.name)}
                      className="ml-1 text-slate-600 hover:text-blue-600 underline"
                    >
                      +{t.name}
                    </button>
                  ))}
              </div>
            )}
          </div>

          {/* 하단 버튼 */}
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
              disabled={!url.trim() || !title.trim()}
              className="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-medium shadow-sm disabled:opacity-50 transition-colors"
            >
              저장하기
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
