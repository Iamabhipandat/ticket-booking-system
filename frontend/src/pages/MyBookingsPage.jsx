import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Ticket, Calendar, QrCode, AlertTriangle, ArrowRight, X, Clock, CheckCircle, RefreshCw } from 'lucide-react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { TicketCard } from '../components/TicketCard';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const MyBookingsPage = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const { success, error, info } = useToast();

  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTicket, setSelectedTicket] = useState(null);
  const [ticketLoading, setTicketLoading] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/auth');
      return;
    }
    fetchMyBookings();
  }, [isAuthenticated]);

  const fetchMyBookings = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/bookings/my-bookings');
      setBookings(response.data);
    } catch (err) {
      console.error('Failed to fetch bookings:', err);
      error(err.response?.data?.message || 'Failed to fetch booking history');
    } finally {
      setLoading(false);
    }
  };

  const handleViewTicket = async (bookingId) => {
    setTicketLoading(true);
    try {
      const res = await api.get(`/api/tickets/booking/${bookingId}`);
      setSelectedTicket(res.data);
    } catch (err) {
      console.error('Failed to load ticket:', err);
      error('Could not retrieve ticket details for this booking');
    } finally {
      setTicketLoading(false);
    }
  };

  const handleCancelBooking = async (bookingId) => {
    const confirmCancel = window.confirm(
      'Are you sure you want to cancel this booking? A refund will be initiated to your payment method (allowed up to 2 hours before the show).'
    );
    if (!confirmCancel) return;

    setCancellingId(bookingId);
    try {
      await api.post(`/api/bookings/${bookingId}/cancel`);
      success('Booking cancelled successfully and refund initiated!');
      fetchMyBookings();
    } catch (err) {
      console.error('Cancellation failed:', err);
      error(err.response?.data?.message || 'Unable to cancel booking');
    } finally {
      setCancellingId(null);
    }
  };

  if (loading) return <LoadingSpinner text="Retrieving your bookings..." />;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 min-h-screen">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8 pb-6 border-b border-slate-800">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black text-white flex items-center gap-2.5">
            <Ticket className="w-7 h-7 text-rose-500" />
            My Bookings & Tickets
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Signed in as <span className="font-semibold text-slate-200">{user?.email}</span>
          </p>
        </div>
        <button
          onClick={fetchMyBookings}
          className="inline-flex items-center gap-2 bg-slate-900 border border-slate-700 hover:border-slate-600 text-slate-300 text-xs font-semibold px-4 py-2 rounded-xl transition-all"
        >
          <RefreshCw className="w-3.5 h-3.5" /> Refresh List
        </button>
      </div>

      {bookings.length === 0 ? (
        <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-16 text-center max-w-md mx-auto space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
            <Ticket className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-slate-200">No Bookings Yet</h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            You haven't booked any movie tickets yet. Browse our movies to pick your favorite cinema!
          </p>
          <Link
            to="/"
            className="inline-flex items-center gap-2 bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs px-5 py-2.5 rounded-xl transition-all shadow-lg shadow-rose-950/40"
          >
            Explore Movies Now <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {bookings.map((b) => {
            const isConfirmed = b.status === 'CONFIRMED';
            const isPending = b.status === 'PENDING';
            const isCancelled = b.status === 'CANCELLED';
            const isExpired = b.status === 'EXPIRED';

            let statusBadge = 'bg-slate-800 text-slate-300 border-slate-700';
            if (isConfirmed) statusBadge = 'bg-emerald-950/60 border-emerald-500/50 text-emerald-400 font-bold';
            else if (isPending) statusBadge = 'bg-amber-950/60 border-amber-500/50 text-amber-400 font-bold';
            else if (isCancelled) statusBadge = 'bg-rose-950/60 border-rose-500/50 text-rose-400 font-bold';

            const createdDate = b.createdAt
              ? new Date(b.createdAt).toLocaleDateString('en-US', {
                  month: 'short',
                  day: 'numeric',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })
              : 'Date N/A';

            return (
              <div
                key={b.bookingId}
                className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-3xl p-6 shadow-xl flex flex-col justify-between space-y-5 transition-all"
              >
                <div>
                  <div className="flex items-center justify-between gap-2 mb-3">
                    <span className="text-[11px] font-mono text-slate-500">Booking #{b.bookingId}</span>
                    <span className={`text-[10px] px-2.5 py-0.5 rounded-full border uppercase tracking-wider ${statusBadge}`}>
                      {b.status}
                    </span>
                  </div>

                  <h3 className="font-extrabold text-white text-base">Show #{b.showId}</h3>
                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-slate-500" /> {createdDate}
                  </p>

                  <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
                    <span className="text-slate-400">Seats Reserved:</span>
                    <span className="font-bold text-slate-200">
                      {b.showSeatIds?.length || 0} {b.showSeatIds?.length === 1 ? 'Seat' : 'Seats'}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-xs mt-1">
                    <span className="text-slate-400">Total Amount:</span>
                    <span className="font-mono font-bold text-emerald-400 text-sm">₹{b.totalAmount}</span>
                  </div>
                </div>

                {/* Actions */}
                <div className="pt-2 flex flex-col gap-2">
                  {isConfirmed && (
                    <>
                      <button
                        onClick={() => handleViewTicket(b.bookingId)}
                        className="w-full bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold py-2.5 rounded-xl border border-slate-700 flex items-center justify-center gap-1.5 transition-colors"
                      >
                        <QrCode className="w-4 h-4 text-rose-400" /> View QR Code & Pass
                      </button>

                      <button
                        disabled={cancellingId === b.bookingId}
                        onClick={() => handleCancelBooking(b.bookingId)}
                        className="w-full bg-rose-950/20 hover:bg-rose-950/40 text-rose-400 border border-rose-900/30 text-xs font-semibold py-2 rounded-xl transition-colors disabled:opacity-50"
                      >
                        {cancellingId === b.bookingId ? 'Cancelling...' : 'Cancel & Request Refund'}
                      </button>
                    </>
                  )}

                  {isPending && (
                    <Link
                      to={`/booking/show/${b.showId}`}
                      className="w-full bg-amber-600 hover:bg-amber-700 text-slate-950 font-bold text-xs py-2 rounded-xl text-center transition-colors"
                    >
                      Complete Checkout
                    </Link>
                  )}

                  {isCancelled && (
                    <p className="text-[11px] text-slate-500 text-center py-1">
                      Refund has been processed to payment source.
                    </p>
                  )}

                  {isExpired && (
                    <p className="text-[11px] text-slate-500 text-center py-1">
                      Hold period ended. Seats released.
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Ticket Modal */}
      {selectedTicket && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="relative max-w-xl w-full">
            <button
              onClick={() => setSelectedTicket(null)}
              className="absolute -top-12 right-0 bg-slate-800 hover:bg-slate-700 text-white p-2 rounded-full transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
            <TicketCard ticket={selectedTicket} />
          </div>
        </div>
      )}
    </div>
  );
};
