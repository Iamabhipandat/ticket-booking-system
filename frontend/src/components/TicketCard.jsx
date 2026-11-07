import React from 'react';
import { QrCode, Calendar, Clock, MapPin, Film, Download, CheckCircle, Copy, AlertCircle } from 'lucide-react';
import { useToast } from '../context/ToastContext';

export const TicketCard = ({ ticket }) => {
  const { success } = useToast();

  if (!ticket) return null;

  const copyCode = () => {
    navigator.clipboard.writeText(ticket.ticketCode);
    success('Ticket code copied to clipboard!');
  };

  const qrImageUrl = `http://localhost:8080/api/tickets/${ticket.ticketCode}/qr`;

  const formattedDate = ticket.showStartTime
    ? new Date(ticket.showStartTime).toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      })
    : 'N/A';

  const formattedTime = ticket.showStartTime
    ? new Date(ticket.showStartTime).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
      })
    : 'N/A';

  return (
    <div className="max-w-xl w-full mx-auto bg-gradient-to-br from-slate-900 via-slate-900 to-[#121826] border border-slate-700/80 rounded-3xl overflow-hidden shadow-2xl relative">
      {/* Top Banner */}
      <div className="bg-gradient-to-r from-rose-600 to-rose-800 px-6 py-4 flex items-center justify-between text-white">
        <div className="flex items-center gap-2">
          <Film className="w-5 h-5 text-rose-200" />
          <span className="font-bold tracking-wider text-sm uppercase">Official Movie Pass</span>
        </div>
        <div className="flex items-center gap-1.5 bg-black/30 backdrop-blur-sm px-3 py-1 rounded-full text-xs font-semibold text-emerald-300">
          <CheckCircle className="w-3.5 h-3.5 text-emerald-400" />
          {ticket.used ? 'ALREADY USED' : 'CONFIRMED'}
        </div>
      </div>

      {/* Ticket Details */}
      <div className="p-6 md:p-8 space-y-6">
        <div>
          <span className="text-xs uppercase font-bold text-rose-400 tracking-wider">Movie</span>
          <h2 className="text-2xl md:text-3xl font-extrabold text-white mt-0.5">
            {ticket.movieTitle || 'Movie Ticket'}
          </h2>
        </div>

        <div className="grid grid-cols-2 gap-4 text-xs">
          <div className="space-y-1">
            <span className="text-slate-400 flex items-center gap-1">
              <MapPin className="w-3.5 h-3.5 text-slate-500" /> Theatre & Screen
            </span>
            <p className="font-semibold text-slate-200 text-sm">{ticket.theatreName}</p>
            <p className="text-slate-400 text-xs">
              Screen {ticket.screenNumber} • {ticket.theatreCity}
            </p>
          </div>

          <div className="space-y-1">
            <span className="text-slate-400 flex items-center gap-1">
              <Calendar className="w-3.5 h-3.5 text-slate-500" /> Date & Time
            </span>
            <p className="font-semibold text-slate-200 text-sm">{formattedDate}</p>
            <p className="text-rose-400 font-mono text-xs">{formattedTime}</p>
          </div>
        </div>

        {/* Seats Badge Grid */}
        <div className="p-4 rounded-2xl bg-slate-800/50 border border-slate-700/60 flex items-center justify-between">
          <div>
            <span className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold block mb-1.5">
              Assigned Seats ({ticket.seatNumbers?.length || 0})
            </span>
            <div className="flex flex-wrap gap-1.5">
              {ticket.seatNumbers?.map((seat) => (
                <span
                  key={seat}
                  className="bg-rose-500/20 border border-rose-500/40 text-rose-300 font-mono font-bold px-2.5 py-1 rounded-lg text-xs"
                >
                  {seat}
                </span>
              ))}
            </div>
          </div>
          <div className="text-right pl-4">
            <span className="text-[11px] uppercase tracking-wider text-slate-400 font-semibold block mb-0.5">
              Total Paid
            </span>
            <span className="text-xl font-extrabold text-emerald-400 font-mono">
              ₹{ticket.totalAmount}
            </span>
          </div>
        </div>

        {/* Perforated Divider Styling */}
        <div className="relative my-4">
          <div className="border-t-2 border-dashed border-slate-700"></div>
          <div className="absolute -left-10 -top-3 w-6 h-6 rounded-full bg-[#080c14] border-r border-slate-700"></div>
          <div className="absolute -right-10 -top-3 w-6 h-6 rounded-full bg-[#080c14] border-l border-slate-700"></div>
        </div>

        {/* QR Code & Code Validator Box */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6 pt-2">
          {/* QR Code image */}
          <div className="flex flex-col items-center gap-2">
            <div className="p-2 bg-white rounded-2xl shadow-xl">
              <img
                src={qrImageUrl}
                alt="Ticket QR Code"
                className="w-32 h-32 object-contain"
                onError={(e) => {
                  e.target.style.display = 'none';
                }}
              />
            </div>
            <a
              href={qrImageUrl}
              download={`ticket-${ticket.ticketCode}.png`}
              className="text-[11px] text-slate-400 hover:text-white flex items-center gap-1 transition-colors"
            >
              <Download className="w-3 h-3" /> Download QR
            </a>
          </div>

          {/* Ticket Security Code */}
          <div className="flex-1 space-y-2 text-center sm:text-left">
            <span className="text-[10px] uppercase tracking-wider text-slate-400 font-bold">
              Entry Gate Code
            </span>
            <div className="flex items-center gap-2 bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono text-xs text-slate-300">
              <span className="truncate flex-1 font-bold tracking-wider">{ticket.ticketCode}</span>
              <button
                onClick={copyCode}
                title="Copy code"
                className="text-slate-400 hover:text-white p-1 rounded hover:bg-slate-800 transition-colors"
              >
                <Copy className="w-4 h-4" />
              </button>
            </div>
            <p className="text-[11px] text-slate-500">
              Scan this QR code or provide the code at the cinema entry gate for validation.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
