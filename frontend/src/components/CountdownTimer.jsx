import React, { useState, useEffect } from 'react';
import { Timer, AlertTriangle } from 'lucide-react';

export const CountdownTimer = ({ expiresAt, onExpire }) => {
  const [timeLeft, setTimeLeft] = useState(() => {
    if (!expiresAt) return 0;
    const diff = new Date(expiresAt).getTime() - Date.now();
    return Math.max(0, Math.floor(diff / 1000));
  });

  useEffect(() => {
    if (!expiresAt) return;

    const interval = setInterval(() => {
      const diff = new Date(expiresAt).getTime() - Date.now();
      const seconds = Math.max(0, Math.floor(diff / 1000));
      setTimeLeft(seconds);

      if (seconds <= 0) {
        clearInterval(interval);
        if (onExpire) onExpire();
      }
    }, 1000);

    return () => clearInterval(interval);
  }, [expiresAt, onExpire]);

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;
  const isUrgent = timeLeft < 60;

  if (timeLeft <= 0) {
    return (
      <div className="flex items-center gap-2 bg-rose-950/80 border border-rose-500/50 text-rose-300 px-3 py-1.5 rounded-lg text-xs font-semibold">
        <AlertTriangle className="w-4 h-4 text-rose-400" />
        <span>Seat hold expired!</span>
      </div>
    );
  }

  return (
    <div
      className={`flex items-center gap-2 px-3 py-1.5 rounded-lg border text-xs font-mono font-bold transition-all ${
        isUrgent
          ? 'bg-rose-950/80 border-rose-500/80 text-rose-300 animate-pulse'
          : 'bg-amber-950/40 border-amber-500/40 text-amber-300'
      }`}
    >
      <Timer className={`w-4 h-4 ${isUrgent ? 'text-rose-400' : 'text-amber-400'}`} />
      <span>
        Hold expires in {String(minutes).padStart(2, '0')}:{String(seconds).padStart(2, '0')}
      </span>
    </div>
  );
};
