import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Calendar, Clock, Star, MapPin, Film, ArrowLeft, Tag, Globe, Sparkles, ChevronRight } from 'lucide-react';
import api from '../api/axios';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const MovieDetailsPage = () => {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [shows, setShows] = useState([]);
  const [selectedDate, setSelectedDate] = useState(() => {
    return new Date().toISOString().split('T')[0];
  });
  const [loading, setLoading] = useState(true);
  const [showsLoading, setShowsLoading] = useState(false);

  // Generate 5 days of tabs
  const dateTabs = Array.from({ length: 5 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() + i);
    const dateStr = d.toISOString().split('T')[0];
    const label =
      i === 0
        ? 'Today'
        : i === 1
        ? 'Tomorrow'
        : d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
    return { dateStr, label };
  });

  useEffect(() => {
    fetchMovieDetails();
  }, [id]);

  useEffect(() => {
    if (id && selectedDate) {
      fetchShowsForDate();
    }
  }, [id, selectedDate]);

  const fetchMovieDetails = async () => {
    try {
      const response = await api.get(`/api/movies/${id}`);
      setMovie(response.data);
    } catch (err) {
      console.error('Failed to fetch movie details:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchShowsForDate = async () => {
    setShowsLoading(true);
    try {
      const response = await api.get(`/api/shows/movie/${id}/date?date=${selectedDate}`);
      setShows(response.data);
    } catch (err) {
      console.error('Failed to fetch shows:', err);
      setShows([]);
    } finally {
      setShowsLoading(false);
    }
  };

  // Group shows by Theatre
  const groupedShows = shows.reduce((acc, show) => {
    const key = show.theatreName || 'Theatre';
    if (!acc[key]) {
      acc[key] = {
        theatreName: show.theatreName,
        theatreCity: show.theatreCity,
        theatreAddress: show.theatreAddress,
        shows: [],
      };
    }
    acc[key].shows.push(show);
    return acc;
  }, {});

  if (loading) return <LoadingSpinner text="Loading movie information..." />;
  if (!movie) {
    return (
      <div className="max-w-xl mx-auto py-20 text-center">
        <h2 className="text-xl font-bold text-slate-200">Movie Not Found</h2>
        <Link to="/" className="text-rose-400 hover:underline text-sm mt-3 inline-block">
          Return to Movies
        </Link>
      </div>
    );
  }

  return (
    <div className="min-h-screen pb-20">
      {/* Movie Hero Header */}
      <div className="relative border-b border-slate-800/80 bg-gradient-to-b from-slate-900/90 to-[#080c14] py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto">
          <Link
            to="/"
            className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-white mb-6 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Movies
          </Link>

          <div className="flex flex-col md:flex-row gap-8 items-start">
            {/* Poster Card */}
            <div className="w-48 sm:w-56 shrink-0 aspect-[2/3] rounded-2xl overflow-hidden border border-slate-700/80 shadow-2xl bg-slate-800">
              <img
                src={movie.posterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=60'}
                alt={movie.title}
                className="w-full h-full object-cover"
                onError={(e) => {
                  e.target.src = 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=60';
                }}
              />
            </div>

            {/* Info */}
            <div className="flex-1 space-y-4">
              <div className="flex flex-wrap items-center gap-2">
                <span className="bg-rose-600/90 text-white font-bold text-xs px-2.5 py-0.5 rounded-md uppercase tracking-wider">
                  {movie.language || 'English'}
                </span>
                <span className="bg-slate-800 text-slate-300 font-semibold text-xs px-2.5 py-0.5 rounded-md border border-slate-700">
                  {movie.genre || 'Action / Drama'}
                </span>
                {movie.durationMinutes && (
                  <span className="text-slate-400 text-xs flex items-center gap-1 ml-1">
                    <Clock className="w-3.5 h-3.5" /> {movie.durationMinutes} mins
                  </span>
                )}
                {movie.rating && (
                  <span className="text-amber-400 text-xs font-bold flex items-center gap-1 ml-2 bg-amber-500/10 border border-amber-500/30 px-2 py-0.5 rounded-md">
                    <Star className="w-3.5 h-3.5 fill-amber-400" /> {movie.rating.toFixed(1)} / 10
                  </span>
                )}
              </div>

              <h1 className="text-3xl sm:text-5xl font-black text-white tracking-tight">
                {movie.title}
              </h1>

              <p className="text-slate-300 text-sm sm:text-base leading-relaxed max-w-3xl">
                {movie.description || 'Experience this thrilling cinema presentation on our state-of-the-art screens with Dolby Atmos sound and crystal-clear laser projection.'}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Showtimes & Date Picker */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-10 space-y-8">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <Calendar className="w-5 h-5 text-rose-500" />
            Select Date & Showtimes
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Pick a date to view available cinemas and screening hours
          </p>
        </div>

        {/* Date Tabs */}
        <div className="flex items-center gap-3 overflow-x-auto pb-2 scrollbar-none border-b border-slate-800">
          {dateTabs.map((tab) => {
            const isSelected = selectedDate === tab.dateStr;
            return (
              <button
                key={tab.dateStr}
                onClick={() => setSelectedDate(tab.dateStr)}
                className={`flex flex-col items-center px-5 py-3 rounded-2xl border text-xs font-bold transition-all shrink-0 ${
                  isSelected
                    ? 'bg-rose-600 border-rose-500 text-white shadow-lg shadow-rose-950 scale-105'
                    : 'bg-slate-900 border-slate-800 text-slate-400 hover:text-white hover:border-slate-700'
                }`}
              >
                <span className="text-xs">{tab.label}</span>
                <span className="text-[11px] font-mono mt-0.5 opacity-80">{tab.dateStr}</span>
              </button>
            );
          })}
        </div>

        {/* Theatres & Show List */}
        {showsLoading ? (
          <LoadingSpinner text="Searching shows for selected date..." />
        ) : Object.keys(groupedShows).length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-10 text-center max-w-lg mx-auto space-y-3">
            <Film className="w-8 h-8 text-slate-500 mx-auto" />
            <h3 className="text-base font-bold text-slate-200">No Shows Scheduled for {selectedDate}</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              There are currently no active shows scheduled for this date. You can add one in the Admin tab!
            </p>
            <Link
              to="/admin"
              className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-400 bg-amber-500/10 border border-amber-500/30 px-4 py-2 rounded-xl hover:bg-amber-500/20 transition-colors"
            >
              <Sparkles className="w-3.5 h-3.5" /> Schedule a Show in Admin Lab
            </Link>
          </div>
        ) : (
          <div className="space-y-6">
            {Object.values(groupedShows).map((group) => (
              <div
                key={group.theatreName}
                className="bg-slate-900/60 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4"
              >
                {/* Theatre Header */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800/80 pb-4">
                  <div>
                    <h3 className="font-bold text-white text-base flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-rose-500" />
                      {group.theatreName}
                    </h3>
                    <p className="text-xs text-slate-400 ml-6">
                      {group.theatreAddress ? `${group.theatreAddress}, ` : ''}{group.theatreCity}
                    </p>
                  </div>
                  <span className="text-[11px] font-semibold text-emerald-400 bg-emerald-950/40 border border-emerald-500/30 px-3 py-1 rounded-full w-fit">
                    M-Ticket & Contactless Entry
                  </span>
                </div>

                {/* Showtimes Pills */}
                <div className="flex flex-wrap gap-4 pt-2">
                  {group.shows.map((show) => {
                    const startTime = show.startTime
                      ? new Date(show.startTime).toLocaleTimeString('en-US', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : 'Time N/A';

                    return (
                      <Link
                        key={show.showId}
                        to={`/booking/show/${show.showId}`}
                        className="group flex flex-col items-center bg-slate-800/80 hover:bg-rose-600 border border-slate-700/80 hover:border-rose-500 p-3.5 rounded-2xl transition-all duration-200 shadow-md hover:shadow-rose-950/40 hover:-translate-y-0.5"
                      >
                        <span className="text-sm font-bold text-slate-200 group-hover:text-white font-mono">
                          {startTime}
                        </span>
                        <div className="flex items-center gap-1.5 mt-1 text-[11px] text-slate-400 group-hover:text-rose-100">
                          <span>Screen {show.screenNumber}</span>
                          <span>•</span>
                          <span className="font-semibold text-emerald-400 group-hover:text-white">
                            ₹{show.ticketPrice}
                          </span>
                        </div>
                      </Link>
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
