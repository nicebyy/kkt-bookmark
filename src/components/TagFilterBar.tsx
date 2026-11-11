import React from 'react';
import { Tag as TagType } from '../types';

interface TagFilterBarProps {
  tags: TagType[];
  selectedTag: string | null;
  onSelectTag: (tag: string | null) => void;
}

export const TagFilterBar: React.FC<TagFilterBarProps> = ({ tags, selectedTag, onSelectTag }) => {
  return (
    <div className="flex items-center gap-2 px-4 py-2 overflow-x-auto no-scrollbar bg-white border-b border-slate-100 text-xs">
      <button
        onClick={() => onSelectTag(null)}
        className={`px-3 py-1.5 rounded-full font-medium transition-all shrink-0 ${
          selectedTag === null
            ? 'bg-blue-600 text-white shadow-sm'
            : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
        }`}
      >
        전체 태그
      </button>

      {tags.map((tag) => {
        const isSelected = selectedTag === tag.name;
        return (
          <button
            key={tag.id}
            onClick={() => onSelectTag(isSelected ? null : tag.name)}
            className={`px-3 py-1.5 rounded-full font-medium transition-all shrink-0 flex items-center gap-1 ${
              isSelected
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
            }`}
          >
            <span
              className="w-2 h-2 rounded-full inline-block"
              style={{ backgroundColor: tag.color }}
            />
            #{tag.name}
          </button>
        );
      })}
    </div>
  );
};
