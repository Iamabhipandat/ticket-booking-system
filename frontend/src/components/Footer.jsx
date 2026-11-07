import React from 'react';
import { Film, ShieldCheck, Heart } from 'lucide-react';

export const Footer = () => {
  return (
    <footer className="mt-auto border-t border-slate-800/80 bg-[#060910] text-slate-400 py-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-rose-600/20 border border-rose-500/30 flex items-center justify-center text-rose-400">
              <Film className="w-4 h-4" />
            </div>
            <span className="text-lg font-bold text-slate-200">
              Cine<span className="text-rose-500">Pass</span>
            </span>
            <span className="text-xs text-slate-500 border-l border-slate-700 pl-2.5 ml-1">
              Complete Ticket Booking Architecture
            </span>
          </div>

          <div className="flex items-center gap-6 text-xs text-slate-400">
            <span className="flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-emerald-400" />
              Razorpay Secured Checkout
            </span>
            <span>•</span>
            <span>5-Min Concurrency Seat Hold Engine</span>
            <span>•</span>
            <span>JWT Role-Based Access Control</span>
          </div>

          <div className="text-xs text-slate-500 flex items-center gap-1">
            Built for seamless end-to-end testing
          </div>
        </div>
      </div>
    </footer>
  );
};
