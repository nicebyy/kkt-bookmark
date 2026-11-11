export interface Folder {
  id: string;
  name: string;
  parentId: string | null; // null이면 루트 폴더
  color: string;
  createdAt: number;
}

export interface Tag {
  id: string;
  name: string;
  color: string;
}

export interface Bookmark {
  id: string;
  folderId: string | null; // null이면 미분류
  url: string;
  title: string;
  description?: string;
  memo?: string;
  imageUrl?: string;
  faviconUrl?: string;
  tags: string[];
  isFavorite: boolean;
  isRead: boolean;
  preferredBrowser?: BrowserType;
  sortOrder?: number;
  createdAt: number;
  updatedAt: number;
}

export type BrowserType = 'default' | 'chrome' | 'samsung' | 'naver' | 'whale' | 'edge' | 'firefox';

export interface BrowserOption {
  id: BrowserType;
  name: string;
  iconName: string;
  badgeColor: string;
}

export const SUPPORTED_BROWSERS: BrowserOption[] = [
  { id: 'default', name: '기본 브라우저', iconName: 'Globe', badgeColor: '#64748b' },
  { id: 'chrome', name: '구글 크롬 (Chrome)', iconName: 'Chrome', badgeColor: '#ea4335' },
  { id: 'samsung', name: '삼성 인터넷', iconName: 'Compass', badgeColor: '#1d4ed8' },
  { id: 'naver', name: '네이버 앱 (Naver)', iconName: 'Compass', badgeColor: '#03c75a' },
  { id: 'whale', name: '네이버 웨일 (Whale)', iconName: 'Compass', badgeColor: '#00c73c' },
  { id: 'edge', name: 'MS 엣지 (Edge)', iconName: 'Compass', badgeColor: '#0078d4' },
  { id: 'firefox', name: '파이어폭스 (Firefox)', iconName: 'Compass', badgeColor: '#ff7139' },
];

export type FilterType = 'ALL' | 'FAVORITES' | 'UNREAD';

export const FOLDER_COLORS = [
  '#2563eb', // Blue
  '#0d9488', // Teal
  '#10b981', // Green
  '#f59e0b', // Amber
  '#8b5cf6', // Purple
  '#ec4899', // Pink
  '#ef4444', // Red
];
