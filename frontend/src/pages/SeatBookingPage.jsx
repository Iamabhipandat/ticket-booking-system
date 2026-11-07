import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, CreditCard, ShieldCheck, Ticket, CheckCircle2, AlertCircle, Sparkles } from 'lucide-react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { SeatMatrix } from '../components/SeatMatrix';
import { CountdownTimer } from '../components/CountdownTimer';
import { TicketCard } from '../components/TicketCard';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const SeatBookingPage = () => {
  const { showId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const { success, error, info } = useToast();

  const [show, setShow] = useState(null);
  const [layout, setLayout] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(true);

  // Booking states
  const [booking, setBooking] = useState(null);
  const [ticket, setTicket] = useState(null);
  const [step, setStep] = useState('SELECTING'); // 'SELECTING' | 'HELD' | 'PAID'
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    fetchShowAndLayout();
  }, [showId]);

  const fetchShowAndLayout = async () => {
    setLoading(true);
    try {
      const [showRes, layoutRes] = await Promise.all([
        api.get(`/api/shows/${showId}`),
        api.get(`/api/show-seats/show/${showId}/layout`),
      ]);
      setShow(showRes.data);
      setLayout(layoutRes.data);
    } catch (err) {
      console.error('Failed to load show details:', err);
      error(err.response?.data?.message || 'Failed to load show seat layout');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleSeat = (seat) => {
    if (step !== 'SELECTING') return;

    setSelectedSeats((prev) => {
      const exists = prev.some((s) => s.showSeatId === seat.showSeatId);
      if (exists) {
        return prev.filter((s) => s.showSeatId !== seat.showSeatId);
      } else {
        if (prev.length >= 8) {
          info('You can select a maximum of 8 seats per booking');
          return prev;
        }
        return [...prev, seat];
      }
    });
  };

  const totalPrice = selectedSeats.reduce((sum, s) => sum + Number(s.price || 0), 0);

  // 1. Hold Seats
  const handleHoldSeats = async () => {
    if (!isAuthenticated) {
      info('Please sign in or register to hold seats');
      navigate('/auth');
      return;
    }

    if (selectedSeats.length === 0) {
      error('Please select at least one seat to proceed');
      return;
    }

    setActionLoading(true);
    try {
      const seatIds = selectedSeats.map((s) => s.showSeatId);
      const res = await api.post('/api/bookings/hold', {
        showId: Number(showId),
        showSeatIds: seatIds,
        customerEmail: user.email,
      });

      setBooking(res.data);
      setStep('HELD');
      success('Seats held successfully for 5 minutes!');
    } catch (err) {
      console.error('Failed to hold seats:', err);
      error(err.response?.data?.message || 'Failed to hold seats. They may have been taken.');
      // Refresh seat layout to get latest status
      fetchShowAndLayout();
    } finally {
      setActionLoading(false);
    }
  };

  // 2. Pay via Razorpay
  const handlePayNow = async () => {
    if (!booking) return;

    setActionLoading(true);
    try {
      // Step A: Create Razorpay Order
      const orderRes = await api.post('/api/payments/create-order', {
        bookingId: booking.bookingId,
      });

      const { razorpayOrderId, razorpayKeyId, amount, currency } = orderRes.data;

      // Check if Razorpay script is loaded in browser
      if (!window.Razorpay) {
        error('Razorpay SDK failed to load. Please check your internet connection.');
        setActionLoading(false);
        return;
      }

      // Step B: Launch Razorpay Modal
      const options = {
        key: razorpayKeyId,
        amount: amount,
        currency: currency,
        name: 'CinePass Cinema',
        description: `Booking for ${show?.theatreName || 'Show'}`,
        order_id: razorpayOrderId,
        prefill: {
          email: user?.email,
          name: user?.name,
        },
        theme: {
          color: '#e50914',
        },
        handler: async function (paymentResponse) {
          try {
            // Step C: Verify Payment
            await api.post('/api/payments/verify', {
              razorpayOrderId: paymentResponse.razorpay_order_id,
              razorpayPaymentId: paymentResponse.razorpay_payment_id,
              razorpaySignature: paymentResponse.razorpay_signature,
            });

            // Step D: Fetch Confirmed Ticket
            const ticketRes = await api.get(`/api/tickets/booking/${booking.bookingId}`);
            setTicket(ticketRes.data);
            setStep('PAID');
            success('Payment verified! Your ticket has been issued.');
          } catch (verifyErr) {
            console.error('Payment verification failed:', verifyErr);
            error(verifyErr.response?.data?.message || 'Payment verification failed');
          }
        },
        modal: {
          ondismiss: function () {
            info('Payment window was dismissed');
          },
        },
      };

      const razorpayInstance = new window.Razorpay(options);
      razorpayInstance.on('payment.failed', function (failRes) {
        error(`Payment failed: ${failRes.error.description || 'Transaction declined'}`);
      });

      razorpayInstance.open();
    } catch (err) {
      console.error('Failed to create payment order:', err);
      error(err.response?.data?.message || 'Could not initiate payment order');
    } finally {
      setActionLoading(false);
    }
  };

  const handleHoldExpired = () => {
    error('Your seat hold has expired. The seats have been released back.');
    setBooking(null);
    setStep('SELECTING');
    setSelectedSeats([]);
    fetchShowAndLayout();
  };

  if (loading) return <LoadingSpinner text="Loading seat configuration..." />;
  if (!show) {
    return (
      <div className="max-w-xl mx-auto py-20 text-center">
        <h2 className="text-xl font-bold text-slate-200">Show Details Unavailable</h2>
        <Link to="/" className="text-rose-400 hover:underline text-sm mt-3 inline-block">
          Return to Movies
        </Link>
      </div>
    );
  }

  const startTimeStr = show.startTime
    ? new Date(show.startTime).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
      })
    : 'Time';

  const dateStr = show.startTime
    ? new Date(show.startTime).toLocaleDateString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
      })
    : 'Date';

  return (
    <div className="min-h-screen pb-24">
      {/* Show Header */}
      <div className="bg-slate-900 border-b border-slate-800 py-6 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <Link
              to={`/movie/${show.movieId}`}
              className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white mb-2 transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" /> Change Showtime
            </Link>
            <h1 className="text-xl sm:text-2xl font-black text-white">{show.theatreName}</h1>
            <p className="text-xs text-slate-400 mt-0.5">
              Screen {show.screenNumber} • {dateStr} at <span className="text-rose-400 font-bold">{startTimeStr}</span> • {show.theatreCity}
            </p>
          </div>

          {/* Step Badge / Timer */}
          <div className="flex items-center gap-3">
            {step === 'HELD' && booking?.holdExpiresAt && (
              <CountdownTimer expiresAt={booking.holdExpiresAt} onExpire={handleHoldExpired} />
            )}
            <span className="text-xs font-mono font-bold bg-slate-800 text-slate-300 border border-slate-700 px-3 py-1.5 rounded-lg">
              Base Price: ₹{show.ticketPrice}
            </span>
          </div>
        </div>
      </div>

      {/* Main Container */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-10">
        {step === 'PAID' && ticket ? (
          /* Confirmation Screen */
          <div className="space-y-8 animate-fade-in">
            <div className="text-center max-w-lg mx-auto space-y-2">
              <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center mx-auto">
                <CheckCircle2 className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-black text-white">Booking Confirmed!</h2>
              <p className="text-xs text-slate-400 leading-relaxed">
                Your payment was verified and tickets have been issued. Your QR code pass is ready below.
              </p>
            </div>

            <TicketCard ticket={ticket} />

            <div className="flex justify-center gap-4 pt-4">
              <Link
                to="/my-bookings"
                className="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold px-6 py-3 rounded-xl transition-colors"
              >
                Go to My Bookings
              </Link>
              <Link
                to="/"
                className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold px-6 py-3 rounded-xl transition-colors shadow-lg shadow-rose-950/40"
              >
                Book Another Movie
              </Link>
            </div>
          </div>
        ) : (
          /* Seat Selection & Checkout Layout */
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
            {/* Seat Matrix (Left/Center 8 cols) */}
            <div className="lg:col-span-8 bg-slate-900/50 border border-slate-800/80 rounded-3xl p-6 sm:p-8 shadow-xl">
              <SeatMatrix
                layout={layout}
                selectedSeatIds={selectedSeats.map((s) => s.showSeatId)}
                onToggleSeat={handleToggleSeat}
              />
            </div>

            {/* Checkout & Summary Panel (Right 4 cols) */}
            <div className="lg:col-span-4 sticky top-24 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl space-y-6">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-rose-400">Order Summary</span>
                <h3 className="text-lg font-black text-white mt-1">Selected Tickets</h3>
              </div>

              {/* Selected Seats Badges */}
              {selectedSeats.length === 0 ? (
                <div className="bg-slate-950/60 border border-slate-800 rounded-2xl p-6 text-center text-slate-500 text-xs">
                  No seats selected yet. Click on the seats in the theater layout to choose your spots.
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="max-h-48 overflow-y-auto space-y-2 pr-1">
                    {selectedSeats.map((s) => (
                      <div
                        key={s.showSeatId}
                        className="flex items-center justify-between bg-slate-800/60 px-3 py-2 rounded-xl text-xs border border-slate-700/50"
                      >
                        <div>
                          <span className="font-bold text-slate-200">
                            Seat {s.rowName}{s.seatNumber}
                          </span>
                          <span className="text-[10px] text-slate-400 ml-2">({s.seatType})</span>
                        </div>
                        <span className="font-mono font-bold text-emerald-400">₹{s.price}</span>
                      </div>
                    ))}
                  </div>

                  {/* Price Calculation Box */}
                  <div className="border-t border-slate-800 pt-3 space-y-2 text-xs">
                    <div className="flex justify-between text-slate-400">
                      <span>Seats Subtotal ({selectedSeats.length})</span>
                      <span className="font-mono text-slate-300">₹{totalPrice.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-slate-400">
                      <span>Convenience Fee & Taxes</span>
                      <span className="font-mono text-emerald-400">FREE (Demo)</span>
                    </div>
                    <div className="border-t border-slate-800 pt-2 flex justify-between text-base font-extrabold text-white">
                      <span>Total Amount</span>
                      <span className="font-mono text-emerald-400">₹{totalPrice.toFixed(2)}</span>
                    </div>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="space-y-3 pt-2">
                {step === 'SELECTING' ? (
                  <button
                    disabled={selectedSeats.length === 0 || actionLoading}
                    onClick={handleHoldSeats}
                    className="w-full bg-rose-600 hover:bg-rose-700 disabled:opacity-50 disabled:cursor-not-allowed text-white font-bold text-sm py-3.5 rounded-xl transition-all shadow-lg shadow-rose-950 flex items-center justify-center gap-2"
                  >
                    {actionLoading ? 'Locking Seats...' : `Hold Seats & Proceed (₹${totalPrice.toFixed(2)})`}
                  </button>
                ) : (
                  <div className="space-y-3">
                    <button
                      disabled={actionLoading}
                      onClick={handlePayNow}
                      className="w-full bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white font-bold text-sm py-3.5 rounded-xl transition-all shadow-lg shadow-emerald-950 flex items-center justify-center gap-2"
                    >
                      <CreditCard className="w-4 h-4" />
                      {actionLoading ? 'Connecting Razorpay...' : `Pay ₹${totalPrice.toFixed(2)} with Razorpay`}
                    </button>
                  </div>
                )}

                <div className="flex items-center justify-center gap-2 text-[11px] text-slate-500 pt-1">
                  <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                  <span>5-Minute Concurrency Seat Lock Engine</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
