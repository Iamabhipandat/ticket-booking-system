import React from 'react';
import { Check, Armchair, Shield, Sparkles } from 'lucide-react';

export const SeatMatrix = ({ layout, selectedSeatIds, onToggleSeat }) => {
  if (!layout || layout.length === 0) {
    return (
      <div className="text-center py-12 text-slate-500">
        No seat layout available for this show.
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center select-none w-full max-w-4xl mx-auto">
      {/* Cinema Screen Curve */}
      <div className="w-full mb-10 flex flex-col items-center">
        <div className="cinema-screen w-3/4 h-3 bg-gradient-to-b from-rose-500/80 via-rose-500/30 to-transparent rounded-t-full shadow-[0_15px_30px_-5px_rgba(244,63,94,0.3)]"></div>
        <p className="text-[11px] uppercase tracking-widest text-slate-400 font-semibold mt-3">
          SCREEN THIS WAY
        </p>
      </div>

      {/* Rows & Seats */}
      <div className="flex flex-col gap-3 w-full items-center">
        {layout.map((row) => (
          <div key={row.rowName} className="flex items-center gap-3">
            {/* Row Label */}
            <span className="w-6 text-center text-xs font-bold text-slate-400 font-mono">
              {row.rowName}
            </span>

            {/* Row Seats */}
            <div className="flex items-center gap-2">
              {row.seats.map((seat) => {
                const isSelected = selectedSeatIds.includes(seat.showSeatId);
                const isAvailable = seat.status === 'AVAILABLE';
                const isBooked = seat.status === 'BOOKED';
                const isHeld = seat.status === 'HELD';

                // Seat Type Styling
                let typeBadge = '';
                if (seat.seatType === 'RECLINER') typeBadge = 'border-purple-500/50';
                else if (seat.seatType === 'PREMIUM') typeBadge = 'border-amber-500/50';

                // Base Seat State Colors
                let seatColor = 'bg-slate-800 text-slate-300 border-slate-700 hover:border-slate-500 hover:bg-slate-700';
                if (isSelected) {
                  seatColor = 'bg-emerald-500 text-slate-950 font-bold border-emerald-400 shadow-lg shadow-emerald-500/40 scale-105';
                } else if (isBooked) {
                  seatColor = 'bg-rose-950/30 text-rose-500/40 border-rose-900/30 cursor-not-allowed';
                } else if (isHeld) {
                  seatColor = 'bg-amber-950/40 text-amber-500/60 border-amber-900/40 cursor-not-allowed';
                }

                return (
                  <button
                    key={seat.showSeatId}
                    disabled={!isAvailable}
                    onClick={() => onToggleSeat(seat)}
                    title={`${row.rowName}${seat.seatNumber} • ${seat.seatType} • ₹${seat.price} (${seat.status})`}
                    className={`relative w-8 h-8 rounded-lg text-xs font-semibold flex items-center justify-center border transition-all duration-200 group ${seatColor} ${typeBadge}`}
                  >
                    {isSelected ? (
                      <Check className="w-4 h-4 stroke-[3]" />
                    ) : (
                      <span>{seat.seatNumber}</span>
                    )}

                    {/* Small category dot indicator */}
                    {seat.seatType === 'RECLINER' && !isSelected && (
                      <span className="absolute -top-1 -right-1 w-2 h-2 rounded-full bg-purple-500 ring-2 ring-slate-900" />
                    )}
                    {seat.seatType === 'PREMIUM' && !isSelected && (
                      <span className="absolute -top-1 -right-1 w-2 h-2 rounded-full bg-amber-400 ring-2 ring-slate-900" />
                    )}
                  </button>
                );
              })}
            </div>

            {/* Row Label (Right) */}
            <span className="w-6 text-center text-xs font-bold text-slate-400 font-mono">
              {row.rowName}
            </span>
          </div>
        ))}
      </div>

      {/* Seat Category & Status Legend */}
      <div className="mt-12 flex flex-wrap items-center justify-center gap-6 pt-6 border-t border-slate-800/80 text-xs text-slate-400">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-slate-800 border border-slate-700"></div>
          <span>Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-emerald-500 border border-emerald-400"></div>
          <span className="text-emerald-400 font-medium">Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-amber-950/40 border border-amber-900/40 text-amber-500"></div>
          <span>Held (Checkout)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 rounded bg-rose-950/30 border border-rose-900/30"></div>
          <span>Booked</span>
        </div>
        <span className="text-slate-600">|</span>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-purple-500"></span>
          <span>Recliner (2x)</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-amber-400"></span>
          <span>Premium (1.5x)</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-full bg-slate-500"></span>
          <span>Regular (1x)</span>
        </div>
      </div>
    </div>
  );
};
