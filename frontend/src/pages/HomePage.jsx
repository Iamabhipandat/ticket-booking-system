import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Film, Star, Clock, Globe, Tag, Sparkles, AlertCircle } from 'lucide-react';
import api from '../api/axios';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const HomePage = () => {
  const [movies, setMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTitle, setSearchTitle] = useState('');
  const [selectedGenre, setSelectedGenre] = useState('');
  const [selectedLanguage, setSelectedLanguage] = useState('');

  const genres = ['All', 'Action', 'Sci-Fi', 'Drama', 'Comedy', 'Thriller', 'Animation'];
  const languages = ['All', 'English', 'Hindi', 'Tamil', 'Telugu'];

  useEffect(() => {
    fetchMovies();
  }, [selectedGenre, selectedLanguage]);

  const fetchMovies = async () => {
    setLoading(true);
    try {
      let endpoint = '/api/movies';
      if (selectedGenre && selectedGenre !== 'All' && selectedLanguage && selectedLanguage !== 'All') {
        endpoint = `/api/movies/filter?language=${encodeURIComponent(selectedLanguage)}&genre=${encodeURIComponent(selectedGenre)}`;
      } else if (selectedGenre && selectedGenre !== 'All') {
        endpoint = `/api/movies/genre/${encodeURIComponent(selectedGenre)}`;
      } else if (selectedLanguage && selectedLanguage !== 'All') {
        endpoint = `/api/movies/language/${encodeURIComponent(selectedLanguage)}`;
      }

      const response = await api.get(endpoint);
      setMovies(response.data);
    } catch (err) {
      console.error('Failed to fetch movies:', err);
      setMovies([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchTitle.trim()) {
      fetchMovies();
      return;
    }
    setLoading(true);
    try {
      const response = await api.get(`/api/movies/search?title=${encodeURIComponent(searchTitle.trim())}`);
      setMovies(response.data);
    } catch (err) {
      console.error('Search failed:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen pb-20">
      {/* Hero Banner */}
      <section className="relative overflow-hidden pt-12 pb-20 px-4 sm:px-6 lg:px-8 border-b border-slate-800/60 bg-gradient-to-b from-slate-900/60 via-[#0b1120] to-[#080c14]">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(229,9,20,0.12),transparent_50%)]" />
        
        <div className="max-w-5xl mx-auto text-center relative z-10 space-y-6">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-semibold tracking-wide">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Next-Gen Cinema Experience</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-black text-white tracking-tight leading-tight">
            Book Your Seats. <br />
            <span className="bg-gradient-to-r from-rose-500 via-rose-400 to-amber-400 bg-clip-text text-transparent">
              Feel The Big Screen.
            </span>
          </h1>

          <p className="text-slate-400 text-sm sm:text-base max-w-2xl mx-auto leading-relaxed">
            Instant booking with live 5-minute seat concurrency locks, interactive seat selection, and automated Razorpay payments.
          </p>

          {/* Search Box */}
          <form onSubmit={handleSearch} className="max-w-xl mx-auto flex items-center gap-2 bg-slate-900/90 border border-slate-700/80 p-2 rounded-2xl shadow-2xl focus-within:border-rose-500/80 transition-all">
            <Search className="w-5 h-5 text-slate-400 ml-3" />
            <input
              type="text"
              placeholder="Search movies by title..."
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
              className="bg-transparent flex-1 text-sm text-slate-100 placeholder:text-slate-500 focus:outline-none px-2"
            />
            <button
              type="submit"
              className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold px-5 py-2.5 rounded-xl transition-all shadow-md shadow-rose-950"
            >
              Search
            </button>
          </form>
        </div>
      </section>

      {/* Filters Bar */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-6 relative z-20">
        <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-xl backdrop-blur-md flex flex-wrap items-center justify-between gap-4">
          {/* Genre Buttons */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 sm:pb-0 scrollbar-none max-w-full">
            <span className="text-xs text-slate-400 font-semibold flex items-center gap-1 shrink-0 mr-1">
              <Tag className="w-3.5 h-3.5" /> Genre:
            </span>
            {genres.map((g) => (
              <button
                key={g}
                onClick={() => setSelectedGenre(g === 'All' ? '' : g)}
                className={`text-xs px-3 py-1.5 rounded-xl font-medium transition-all shrink-0 ${
                  (g === 'All' && !selectedGenre) || selectedGenre === g
                    ? 'bg-rose-600 text-white shadow-md shadow-rose-950 font-bold'
                    : 'bg-slate-800/80 text-slate-400 hover:text-white hover:bg-slate-800'
                }`}
              >
                {g}
              </button>
            ))}
          </div>

          {/* Language Selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-semibold flex items-center gap-1">
              <Globe className="w-3.5 h-3.5" /> Language:
            </span>
            <select
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value === 'All' ? '' : e.target.value)}
              className="bg-slate-800 border border-slate-700 text-slate-200 text-xs rounded-xl px-3 py-1.5 focus:outline-none focus:border-rose-500"
            >
              {languages.map((l) => (
                <option key={l} value={l === 'All' ? '' : l}>
                  {l}
                </option>
              ))}
            </select>
          </div>
        </div>
      </section>

      {/* Movie Grid */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-extrabold text-white">Now Showing</h2>
            <p className="text-xs text-slate-400 mt-1">Select a movie to pick your showtime and seats</p>
          </div>
          <span className="text-xs font-mono text-slate-400 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-lg">
            {movies.length} {movies.length === 1 ? 'Movie' : 'Movies'} Available
          </span>
        </div>

        {loading ? (
          <LoadingSpinner text="Fetching movies from backend..." />
        ) : movies.length === 0 ? (
          <div className="bg-slate-900/40 border border-slate-800/80 rounded-3xl p-12 text-center max-w-xl mx-auto space-y-4">
            <div className="w-16 h-16 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-amber-400 flex items-center justify-center mx-auto">
              <Film className="w-8 h-8" />
            </div>
            <h3 className="text-lg font-bold text-slate-200">No Movies Found</h3>
            <p className="text-xs text-slate-400 leading-relaxed">
              Your database currently has no movies matching the search. Visit the Admin & Testing Lab to auto-populate movies and shows with 1 click!
            </p>
            <Link
              to="/admin"
              className="inline-flex items-center gap-2 bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs px-5 py-2.5 rounded-xl transition-all shadow-lg shadow-amber-950/40"
            >
              <Sparkles className="w-4 h-4" />
              Go to 1-Click Demo Data Seeder
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
            {movies.map((movie) => (
              <div
                key={movie.id || movie.movieId}
                className="group bg-slate-900/60 border border-slate-800/80 hover:border-slate-700 rounded-2xl overflow-hidden flex flex-col transition-all duration-300 hover:shadow-2xl hover:shadow-rose-950/30 hover:-translate-y-1"
              >
                {/* Poster */}
                <div className="relative aspect-[2/3] w-full overflow-hidden bg-slate-800">
                  <img
                    src={movie.posterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=60'}
                    alt={movie.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                    onError={(e) => {
                      e.target.src = 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop&q=60';
                    }}
                  />
                  {movie.rating && (
                    <div className="absolute top-2.5 right-2.5 bg-black/70 backdrop-blur-md px-2 py-1 rounded-lg flex items-center gap-1 text-[11px] font-bold text-amber-400 border border-white/10">
                      <Star className="w-3 h-3 fill-amber-400 text-amber-400" />
                      {movie.rating.toFixed(1)}
                    </div>
                  )}
                  {movie.language && (
                    <span className="absolute bottom-2.5 left-2.5 bg-rose-600/90 text-white font-bold text-[10px] px-2 py-0.5 rounded-md uppercase tracking-wider shadow">
                      {movie.language}
                    </span>
                  )}
                </div>

                {/* Details */}
                <div className="p-4 flex-1 flex flex-col justify-between space-y-3">
                  <div>
                    <h3 className="font-bold text-slate-100 text-sm leading-snug line-clamp-1 group-hover:text-rose-400 transition-colors">
                      {movie.title}
                    </h3>
                    <div className="flex items-center gap-2 mt-1 text-[11px] text-slate-400">
                      <span>{movie.genre || 'General'}</span>
                      {movie.durationMinutes && (
                        <>
                          <span>•</span>
                          <span className="flex items-center gap-0.5">
                            <Clock className="w-3 h-3" /> {movie.durationMinutes}m
                          </span>
                        </>
                      )}
                    </div>
                  </div>

                  <Link
                    to={`/movie/${movie.id || movie.movieId}`}
                    className="w-full text-center bg-slate-800 hover:bg-rose-600 text-slate-200 hover:text-white font-bold text-xs py-2 rounded-xl transition-all shadow-sm"
                  >
                    View Shows
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
};
