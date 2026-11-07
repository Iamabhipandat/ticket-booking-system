import React from 'react';
import { Film } from 'lucide-react';

export const LoadingSpinner = ({ text = 'Loading...' }) => {
  return (
    <div className="flex flex-col items-center justify-center py-20 gap-4">
      <div className="relative">
        <div className="w-12 h-12 rounded-full border-4 border-slate-800 border-t-rose-500 animate-spin"></div>
        <Film className="w-5 h-5 text-rose-400 absolute inset-0 m-auto animate-pulse" />
      </div>
      <p className="text-xs text-slate-400 font-medium tracking-wide animate-pulse">{text}</p>
    </div>
  );
};
