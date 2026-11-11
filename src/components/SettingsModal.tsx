import React, { useRef, useState } from 'react';
import { X, ShieldCheck, Download, Upload, Smartphone, Check, AlertCircle, Globe } from 'lucide-react';
import { storage } from '../services/storage';
import { BrowserType, SUPPORTED_BROWSERS } from '../types';

interface SettingsModalProps {
  onClose: () => void;
  onDataChanged: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({ onClose, onDataChanged }) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [message, setMessage] = useState<{ text: string; isError?: boolean } | null>(null);
  const [defaultBrowser, setDefaultBrowser] = useState<BrowserType>(storage.getDefaultBrowser());

  const handleExport = () => {
    const json = storage.exportData();
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `kkt_bookmark_backup_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
    setMessage({ text: '백업 JSON 파일이 다운로드되었습니다.' });
  };

  const handleImportFile = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const content = event.target?.result as string;
      const res = storage.importData(content);
      if (res.success) {
        setMessage({ text: `성공적으로 복원되었습니다! (${res.count}개 항목)` });
        onDataChanged();
      } else {
        setMessage({ text: `복원 실패: ${res.error}`, isError: true });
      }
    };
    reader.readAsText(file);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-100 flex flex-col max-h-[85vh]">
        <div className="flex items-center justify-between px-5 py-4 border-b border-slate-100">
          <h3 className="font-bold text-slate-800 text-base">설정 및 데이터 백업</h3>
          <button onClick={onClose} className="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 overflow-y-auto space-y-4 text-sm flex-1">
          {message && (
            <div
              className={`p-3 rounded-xl flex items-center gap-2 text-xs font-medium ${
                message.isError ? 'bg-red-50 text-red-700' : 'bg-emerald-50 text-emerald-700'
              }`}
            >
              {message.isError ? <AlertCircle className="w-4 h-4 shrink-0" /> : <Check className="w-4 h-4 shrink-0" />}
              <span>{message.text}</span>
            </div>
          )}

          {/* 광고 제로 청정 안내 */}
          <div className="p-4 rounded-xl bg-blue-50/70 border border-blue-100 flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-blue-600 text-white flex items-center justify-center shrink-0 mt-0.5">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-sm">100% 광고 없는 순수 로컬 앱</h4>
              <p className="text-xs text-slate-600 mt-1 leading-relaxed">
                어떠한 광고나 외부 서버 추적도 없습니다. 모든 북마크 데이터는 브라우저 내부 LocalStorage에만 안전하게 보관됩니다.
              </p>
            </div>
          </div>

          {/* 스마트폰에서 앱처럼 쓰기 안내 */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200/80 flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-emerald-600 text-white flex items-center justify-center shrink-0 mt-0.5">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-sm">스마트폰 홈 화면에 앱으로 추가</h4>
              <p className="text-xs text-slate-600 mt-1 leading-relaxed">
                안드로이드 Chrome 또는 삼성 인터넷에서 이 페이지에 접속한 뒤, 브라우저 메뉴 [⋮] → <span className="font-semibold text-slate-800">"홈 화면에 추가" (또는 앱 설치)</span>를 누르면 일반 안드로이드 앱 아이콘으로 설치되어 주소창 없이 네이티브 앱처럼 동작합니다!
              </p>
            </div>
          </div>

          {/* 기본 브라우저 설정 */}
          <div className="p-4 rounded-xl bg-slate-50 border border-slate-200/80 flex items-start gap-3">
            <div className="w-8 h-8 rounded-lg bg-blue-500 text-white flex items-center justify-center shrink-0 mt-0.5">
              <Globe className="w-5 h-5" />
            </div>
            <div className="flex-1 min-w-0">
              <h4 className="font-bold text-slate-900 text-sm">기본 실행 브라우저</h4>
              <p className="text-xs text-slate-500 mt-0.5 mb-2.5">
                링크를 클릭했을 때 기본으로 열릴 브라우저를 지정합니다.
              </p>
              <select
                value={defaultBrowser}
                onChange={(e) => {
                  const b = e.target.value as BrowserType;
                  setDefaultBrowser(b);
                  storage.setDefaultBrowser(b);
                  onDataChanged();
                  setMessage({ text: '기본 실행 브라우저가 변경되었습니다.' });
                }}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 bg-white text-xs font-medium text-slate-700 outline-none focus:border-blue-500"
              >
                {SUPPORTED_BROWSERS.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.id === 'default' ? '🌐 시스템 기본 브라우저' : `🚀 ${b.name}`}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* 백업 및 복원 */}
          <div className="space-y-2 pt-2">
            <h4 className="font-semibold text-slate-800 text-xs">데이터 백업 및 복원</h4>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={handleExport}
                className="flex items-center justify-center gap-2 p-3 rounded-xl border border-slate-200 hover:bg-slate-50 font-medium text-slate-700 text-xs transition-colors"
              >
                <Download className="w-4 h-4 text-blue-600" />
                JSON 내보내기
              </button>

              <button
                onClick={() => fileInputRef.current?.click()}
                className="flex items-center justify-center gap-2 p-3 rounded-xl border border-slate-200 hover:bg-slate-50 font-medium text-slate-700 text-xs transition-colors"
              >
                <Upload className="w-4 h-4 text-emerald-600" />
                JSON 복원하기
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept=".json"
                onChange={handleImportFile}
                className="hidden"
              />
            </div>
          </div>
        </div>

        <div className="p-4 border-t border-slate-100 bg-slate-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-900 text-white text-xs font-semibold"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
};
