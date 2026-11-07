import React, { useState, useEffect } from 'react';
import {
  Sparkles,
  Film,
  Building2,
  Calendar,
  QrCode,
  Plus,
  Trash2,
  CheckCircle,
  AlertCircle,
  Clock,
  Layers,
  ShieldCheck,
  RefreshCw,
} from 'lucide-react';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const AdminDashboardPage = () => {
  const { user, isAdmin, isAuthenticated } = useAuth();
  const { success, error, info } = useToast();

  const [activeTab, setActiveTab] = useState('seeder'); // 'seeder' | 'movies' | 'theatres' | 'shows' | 'validator'

  // Data lists
  const [movies, setMovies] = useState([]);
  const [theatres, setTheatres] = useState([]);
  const [screens, setScreens] = useState([]);
  const [selectedTheatreId, setSelectedTheatreId] = useState('');
  const [seederLogs, setSeederLogs] = useState([]);
  const [seeding, setSeeding] = useState(false);

  // Forms
  const [movieForm, setMovieForm] = useState({
    title: '',
    description: '',
    durationMinutes: 150,
    language: 'English',
    genre: 'Action',
    posterUrl: '',
    rating: 8.5,
  });

  const [theatreForm, setTheatreForm] = useState({
    name: '',
    city: 'Delhi',
    address: '',
  });

  const [screenForm, setScreenForm] = useState({
    screenNumber: 1,
    totalSeats: 24,
  });

  const [showForm, setShowForm] = useState({
    movieId: '',
    screenId: '',
    startTime: '',
    endTime: '',
    ticketPrice: 250,
  });

  const [validateCode, setValidateCode] = useState('');
  const [validationResult, setValidationResult] = useState(null);
  const [validating, setValidating] = useState(false);

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    if (selectedTheatreId) {
      fetchScreensForTheatre(selectedTheatreId);
    }
  }, [selectedTheatreId]);

  const fetchInitialData = async () => {
    try {
      const [moviesRes, theatresRes] = await Promise.all([
        api.get('/api/movies'),
        api.get('/api/theatres/city/Delhi').catch(() => ({ data: [] })),
      ]);
      setMovies(moviesRes.data);
      setTheatres(theatresRes.data);
      if (theatresRes.data.length > 0) {
        setSelectedTheatreId(theatresRes.data[0].theatreId);
      }
    } catch (err) {
      console.error('Failed to load admin data:', err);
    }
  };

  const fetchScreensForTheatre = async (theatreId) => {
    try {
      const res = await api.get(`/api/screens/theatre/${theatreId}`);
      setScreens(res.data);
    } catch (err) {
      setScreens([]);
    }
  };

  // 1-Click Demo Data Seeder
  const handleSeedDemoData = async () => {
    setSeeding(true);
    setSeederLogs([]);

    const log = (msg) => {
      setSeederLogs((prev) => [...prev, `${new Date().toLocaleTimeString()} - ${msg}`]);
    };

    try {
      log('Starting automated cinema ecosystem generation...');

      // 1. Create Theatre
      log('1/5 Creating Theatre: PVR IMAX Vegas Mall...');
      const theatreRes = await api.post('/api/theatres', {
        name: 'PVR IMAX Vegas Mall',
        city: 'Delhi',
        address: 'Sector 14, Dwarka',
      });
      const theatreId = theatreRes.data.theatreId;
      log(`Theatre created with ID #${theatreId}`);

      // 2. Create Screen
      log('2/5 Creating Screen 1 (24 seats capacity)...');
      const screenRes = await api.post(`/api/screens/theatre/${theatreId}`, {
        screenNumber: 1,
        totalSeats: 24,
      });
      const screenId = screenRes.data.screenId;
      log(`Screen 1 created with ID #${screenId}`);

      // 3. Create Seats (3 rows of 8 seats each)
      log('3/5 Generating 24 seats across 3 tiers (Regular, Premium, Recliner)...');
      const rows = [
        { name: 'A', type: 'REGULAR', count: 8 },
        { name: 'B', type: 'PREMIUM', count: 8 },
        { name: 'C', type: 'RECLINER', count: 8 },
      ];

      for (const row of rows) {
        for (let num = 1; num <= row.count; num++) {
          await api.post(`/api/seats/screen/${screenId}`, {
            rowName: row.name,
            seatNumber: num,
            seatType: row.type,
          });
        }
      }
      log('All 24 seats (A1-A8, B1-B8, C1-C8) configured successfully!');

      // 4. Create Movies
      log('4/5 Creating Movies (Interstellar & Oppenheimer)...');
      const movie1 = await api.post('/api/movies', {
        title: 'Interstellar',
        description: 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity survival.',
        durationMinutes: 169,
        language: 'English',
        genre: 'Sci-Fi',
        posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop&q=80',
        rating: 8.7,
      });

      const movie2 = await api.post('/api/movies', {
        title: 'Oppenheimer',
        description: 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.',
        durationMinutes: 180,
        language: 'English',
        genre: 'Drama',
        posterUrl: 'https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=600&auto=format&fit=crop&q=80',
        rating: 8.9,
      });
      log(`Movies created: Interstellar (#${movie1.data.id}) & Oppenheimer (#${movie2.data.id})`);

      // 5. Schedule Shows (Today at 2:00 PM, Today at 7:00 PM, Tomorrow at 6:00 PM)
      log('5/5 Scheduling shows with auto-initialized seats...');
      const today = new Date();
      const show1Start = new Date(today);
      show1Start.setHours(14, 0, 0, 0);
      const show1End = new Date(today);
      show1End.setHours(17, 0, 0, 0);

      const show2Start = new Date(today);
      show2Start.setHours(19, 0, 0, 0);
      const show2End = new Date(today);
      show2End.setHours(22, 0, 0, 0);

      await api.post(`/api/shows/movie/${movie1.data.id}/screen/${screenId}`, {
        startTime: show1Start.toISOString(),
        endTime: show1End.toISOString(),
        ticketPrice: 250.0,
      });

      await api.post(`/api/shows/movie/${movie1.data.id}/screen/${screenId}`, {
        startTime: show2Start.toISOString(),
        endTime: show2End.toISOString(),
        ticketPrice: 300.0,
      });

      log('Shows successfully scheduled and all show-seats initialized!');
      log('🎉 Cinema Ecosystem Seeded Successfully! You can now book tickets!');
      success('Complete Cinema Ecosystem Seeded Successfully!');
      fetchInitialData();
    } catch (err) {
      console.error('Seeding failed:', err);
      log(`ERROR: ${err.response?.data?.message || err.message}`);
      error(err.response?.data?.message || 'Seeding failed. Ensure you are signed in as an Admin.');
    } finally {
      setSeeding(false);
    }
  };

  // Movie Management
  const handleCreateMovie = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/movies', {
        ...movieForm,
        durationMinutes: Number(movieForm.durationMinutes),
        rating: Number(movieForm.rating),
      });
      success('Movie added successfully!');
      setMovieForm({
        title: '',
        description: '',
        durationMinutes: 150,
        language: 'English',
        genre: 'Action',
        posterUrl: '',
        rating: 8.5,
      });
      fetchInitialData();
    } catch (err) {
      error(err.response?.data?.message || 'Failed to add movie');
    }
  };

  const handleDeleteMovie = async (movieId) => {
    if (!window.confirm('Are you sure you want to delete this movie?')) return;
    try {
      await api.delete(`/api/movies/${movieId}`);
      success('Movie deleted successfully!');
      fetchInitialData();
    } catch (err) {
      error(err.response?.data?.message || 'Failed to delete movie');
    }
  };

  // Theatre Management
  const handleCreateTheatre = async (e) => {
    e.preventDefault();
    try {
      await api.post('/api/theatres', theatreForm);
      success('Theatre created successfully!');
      setTheatreForm({ name: '', city: 'Delhi', address: '' });
      fetchInitialData();
    } catch (err) {
      error(err.response?.data?.message || 'Failed to create theatre');
    }
  };

  // Screen Management
  const handleCreateScreen = async (e) => {
    e.preventDefault();
    if (!selectedTheatreId) {
      error('Please select a theatre first');
      return;
    }
    try {
      await api.post(`/api/screens/theatre/${selectedTheatreId}`, {
        screenNumber: Number(screenForm.screenNumber),
        totalSeats: Number(screenForm.totalSeats),
      });
      success('Screen added to theatre successfully!');
      fetchScreensForTheatre(selectedTheatreId);
    } catch (err) {
      error(err.response?.data?.message || 'Failed to create screen');
    }
  };

  // Show Scheduling
  const handleCreateShow = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/api/shows/movie/${showForm.movieId}/screen/${showForm.screenId}`, {
        startTime: new Date(showForm.startTime).toISOString(),
        endTime: new Date(showForm.endTime).toISOString(),
        ticketPrice: Number(showForm.ticketPrice),
      });
      success('Show scheduled! Seats have been auto-initialized.');
      setShowForm({
        movieId: '',
        screenId: '',
        startTime: '',
        endTime: '',
        ticketPrice: 250,
      });
    } catch (err) {
      error(err.response?.data?.message || 'Failed to schedule show');
    }
  };

  // Ticket Code Validator
  const handleValidateTicket = async (e) => {
    e.preventDefault();
    if (!validateCode.trim()) return;

    setValidating(true);
    setValidationResult(null);
    try {
      const res = await api.post(`/api/tickets/validate/${encodeURIComponent(validateCode.trim())}`);
      setValidationResult({ success: true, ticket: res.data });
      success('Ticket validated successfully! Checked-in.');
    } catch (err) {
      setValidationResult({
        success: false,
        message: err.response?.data?.message || 'Ticket invalid or already used',
      });
      error(err.response?.data?.message || 'Ticket validation failed');
    } finally {
      setValidating(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 min-h-screen">
      {/* Header with Admin Status */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8 pb-6 border-b border-slate-800">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-400">
              <Sparkles className="w-5 h-5" />
            </span>
            <h1 className="text-2xl sm:text-3xl font-black text-white">Admin & Testing Lab</h1>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Test and manage movies, theatres, screens, seat layouts, and entry-gate ticket scanners.
          </p>
        </div>

        {/* User Role Indicator */}
        <div className="flex items-center gap-3">
          <div
            className={`flex items-center gap-2 px-3 py-1.5 rounded-xl border text-xs font-bold ${
              isAdmin
                ? 'bg-emerald-950/60 border-emerald-500/50 text-emerald-300'
                : 'bg-amber-950/60 border-amber-500/50 text-amber-300'
            }`}
          >
            <ShieldCheck className="w-4 h-4" />
            <span>Role: {user?.role || 'Guest (Not logged in)'}</span>
          </div>

          {!isAdmin && (
            <span className="text-[11px] text-amber-400/90 font-medium">
              * Log in with an Admin account to execute admin endpoints
            </span>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none border-b border-slate-800 mb-8">
        {[
          { id: 'seeder', label: '1-Click Demo Seeder', icon: Sparkles },
          { id: 'movies', label: 'Movie Manager', icon: Film },
          { id: 'theatres', label: 'Theatres & Screens', icon: Building2 },
          { id: 'shows', label: 'Show Scheduler', icon: Calendar },
          { id: 'validator', label: 'Ticket Entry Scanner', icon: QrCode },
        ].map((t) => {
          const Icon = t.icon;
          const isSelected = activeTab === t.id;
          return (
            <button
              key={t.id}
              onClick={() => setActiveTab(t.id)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold transition-all shrink-0 ${
                isSelected
                  ? 'bg-amber-500 text-slate-950 shadow-lg shadow-amber-950/40'
                  : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{t.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab 1: 1-Click Demo Seeder */}
      {activeTab === 'seeder' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-3xl p-6 sm:p-8 space-y-6 shadow-xl">
            <div className="space-y-2">
              <span className="text-[10px] font-bold uppercase tracking-wider text-amber-400">
                Instant Automation
              </span>
              <h2 className="text-xl font-black text-white">Full Cinema Ecosystem Seeder</h2>
              <p className="text-xs text-slate-400 leading-relaxed">
                Clicking the button below will automatically create a full movie theater ecosystem with 1 click:
              </p>
            </div>

            <ul className="space-y-2.5 text-xs text-slate-300">
              <li className="flex items-start gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>Creates <strong>PVR IMAX Multiplex</strong> in Delhi</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>Creates <strong>Screen 1</strong> with 24 seats</span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>Configures 3 tiers: <strong>Regular (A)</strong>, <strong>Premium (B)</strong>, <strong>Recliner (C)</strong></span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>Adds movies: <strong>Interstellar</strong> & <strong>Oppenheimer</strong></span>
              </li>
              <li className="flex items-start gap-2">
                <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <span>Schedules shows and <strong>auto-initializes all seats</strong></span>
              </li>
            </ul>

            <button
              disabled={seeding}
              onClick={handleSeedDemoData}
              className="w-full bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-sm py-4 rounded-2xl transition-all shadow-xl shadow-amber-950/50 flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Sparkles className="w-4 h-4" />
              {seeding ? 'Generating Cinema Data...' : 'Run 1-Click Cinema Seeder'}
            </button>
          </div>

          {/* Seeder Console / Output */}
          <div className="lg:col-span-7 bg-slate-950 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-3 font-mono">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <span className="text-xs font-bold text-slate-400 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-amber-400" /> Seeder Output Terminal
              </span>
              <button
                onClick={() => setSeederLogs([])}
                className="text-[10px] text-slate-500 hover:text-slate-300"
              >
                Clear
              </button>
            </div>

            <div className="min-h-[280px] max-h-[380px] overflow-y-auto space-y-1.5 text-xs text-slate-300 pr-2">
              {seederLogs.length === 0 ? (
                <p className="text-slate-600 italic py-10 text-center font-sans text-xs">
                  Click the button on the left to run the seeder and view live logs.
                </p>
              ) : (
                seederLogs.map((logStr, idx) => (
                  <div key={idx} className="leading-relaxed">
                    <span className="text-amber-500 font-bold">&gt;</span> {logStr}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* Tab 2: Movie Manager */}
      {activeTab === 'movies' && (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          <form
            onSubmit={handleCreateMovie}
            className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-3xl p-6 space-y-4 shadow-xl"
          >
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <Plus className="w-4 h-4 text-rose-500" /> Add New Movie
            </h3>

            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Title</label>
              <input
                type="text"
                required
                value={movieForm.title}
                onChange={(e) => setMovieForm({ ...movieForm, title: e.target.value })}
                placeholder="e.g. Inception"
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Genre</label>
                <input
                  type="text"
                  required
                  value={movieForm.genre}
                  onChange={(e) => setMovieForm({ ...movieForm, genre: e.target.value })}
                  placeholder="Action, Sci-Fi"
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Language</label>
                <input
                  type="text"
                  required
                  value={movieForm.language}
                  onChange={(e) => setMovieForm({ ...movieForm, language: e.target.value })}
                  placeholder="English, Hindi"
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Duration (mins)</label>
                <input
                  type="number"
                  required
                  value={movieForm.durationMinutes}
                  onChange={(e) => setMovieForm({ ...movieForm, durationMinutes: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Rating (out of 10)</label>
                <input
                  type="number"
                  step="0.1"
                  max="10"
                  value={movieForm.rating}
                  onChange={(e) => setMovieForm({ ...movieForm, rating: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Poster Image URL</label>
              <input
                type="url"
                value={movieForm.posterUrl}
                onChange={(e) => setMovieForm({ ...movieForm, posterUrl: e.target.value })}
                placeholder="https://images.unsplash.com/..."
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>

            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Description</label>
              <textarea
                rows="3"
                value={movieForm.description}
                onChange={(e) => setMovieForm({ ...movieForm, description: e.target.value })}
                placeholder="Brief synopsis..."
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>

            <button
              type="submit"
              className="w-full bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs py-3 rounded-xl transition-all shadow-lg shadow-rose-950"
            >
              Add Movie to Backend
            </button>
          </form>

          {/* Current Movies List */}
          <div className="lg:col-span-7 bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-xl space-y-4">
            <h3 className="font-bold text-white text-base">Current Movies in Database ({movies.length})</h3>
            <div className="space-y-3 max-h-[460px] overflow-y-auto pr-1">
              {movies.map((m) => (
                <div
                  key={m.id || m.movieId}
                  className="flex items-center justify-between p-3 bg-slate-950 rounded-2xl border border-slate-800"
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={m.posterUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=100'}
                      alt={m.title}
                      className="w-10 h-14 object-cover rounded-lg bg-slate-800"
                    />
                    <div>
                      <p className="font-bold text-sm text-white">{m.title}</p>
                      <p className="text-xs text-slate-400">
                        {m.language} • {m.genre} • {m.durationMinutes}m • ★ {m.rating}
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => handleDeleteMovie(m.id || m.movieId)}
                    className="p-2 text-rose-400 hover:bg-rose-950/40 rounded-xl transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Tab 3: Theatres & Screens */}
      {activeTab === 'theatres' && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 items-start">
          {/* Create Theatre */}
          <form
            onSubmit={handleCreateTheatre}
            className="bg-slate-900 border border-slate-800 rounded-3xl p-6 space-y-4 shadow-xl"
          >
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <Building2 className="w-4 h-4 text-amber-400" /> Create Theatre
            </h3>
            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Theatre Name</label>
              <input
                type="text"
                required
                value={theatreForm.name}
                onChange={(e) => setTheatreForm({ ...theatreForm, name: e.target.value })}
                placeholder="e.g. Cinepolis Mall of India"
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">City</label>
              <input
                type="text"
                required
                value={theatreForm.city}
                onChange={(e) => setTheatreForm({ ...theatreForm, city: e.target.value })}
                placeholder="Delhi, Mumbai, Bengaluru"
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>
            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Address</label>
              <input
                type="text"
                value={theatreForm.address}
                onChange={(e) => setTheatreForm({ ...theatreForm, address: e.target.value })}
                placeholder="Sector 18, Noida"
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-amber-500 hover:bg-amber-600 text-slate-950 font-bold text-xs py-3 rounded-xl transition-all shadow-md"
            >
              Create Theatre
            </button>
          </form>

          {/* Create Screen */}
          <form
            onSubmit={handleCreateScreen}
            className="bg-slate-900 border border-slate-800 rounded-3xl p-6 space-y-4 shadow-xl"
          >
            <h3 className="font-bold text-white text-base flex items-center gap-2">
              <Layers className="w-4 h-4 text-emerald-400" /> Add Screen to Theatre
            </h3>
            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Select Theatre</label>
              <select
                value={selectedTheatreId}
                onChange={(e) => setSelectedTheatreId(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              >
                <option value="">Select theatre...</option>
                {theatres.map((t) => (
                  <option key={t.theatreId} value={t.theatreId}>
                    {t.name} ({t.city})
                  </option>
                ))}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Screen Number</label>
                <input
                  type="number"
                  min="1"
                  required
                  value={screenForm.screenNumber}
                  onChange={(e) => setScreenForm({ ...screenForm, screenNumber: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Total Seats</label>
                <input
                  type="number"
                  min="1"
                  required
                  value={screenForm.totalSeats}
                  onChange={(e) => setScreenForm({ ...screenForm, totalSeats: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
            </div>
            <button
              type="submit"
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs py-3 rounded-xl transition-all shadow-md"
            >
              Add Screen
            </button>

            {/* List Screens for selected theatre */}
            {selectedTheatreId && (
              <div className="pt-2 text-xs text-slate-400">
                <span className="font-bold text-slate-300">Existing Screens ({screens.length}): </span>
                {screens.map((s) => `Screen ${s.screenNumber} (${s.totalSeats} seats)`).join(', ') || 'None yet'}
              </div>
            )}
          </form>
        </div>
      )}

      {/* Tab 4: Show Scheduler */}
      {activeTab === 'shows' && (
        <div className="max-w-xl mx-auto bg-slate-900 border border-slate-800 rounded-3xl p-8 space-y-5 shadow-2xl">
          <div className="space-y-1">
            <h3 className="font-bold text-white text-lg flex items-center gap-2">
              <Calendar className="w-5 h-5 text-rose-500" /> Schedule a New Show
            </h3>
            <p className="text-xs text-slate-400">
              When a show is created, the backend will auto-initialize all ShowSeats for booking!
            </p>
          </div>

          <form onSubmit={handleCreateShow} className="space-y-4">
            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Select Movie</label>
              <select
                required
                value={showForm.movieId}
                onChange={(e) => setShowForm({ ...showForm, movieId: e.target.value })}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              >
                <option value="">Select movie...</option>
                {movies.map((m) => (
                  <option key={m.id || m.movieId} value={m.id || m.movieId}>
                    {m.title} ({m.language})
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Select Theatre</label>
                <select
                  value={selectedTheatreId}
                  onChange={(e) => setSelectedTheatreId(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                >
                  <option value="">Select theatre...</option>
                  {theatres.map((t) => (
                    <option key={t.theatreId} value={t.theatreId}>
                      {t.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Select Screen</label>
                <select
                  required
                  value={showForm.screenId}
                  onChange={(e) => setShowForm({ ...showForm, screenId: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                >
                  <option value="">Select screen...</option>
                  {screens.map((s) => (
                    <option key={s.screenId} value={s.screenId}>
                      Screen {s.screenNumber} ({s.totalSeats} seats)
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">Start Time</label>
                <input
                  type="datetime-local"
                  required
                  value={showForm.startTime}
                  onChange={(e) => setShowForm({ ...showForm, startTime: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>

              <div>
                <label className="block text-xs text-slate-300 font-medium mb-1">End Time</label>
                <input
                  type="datetime-local"
                  required
                  value={showForm.endTime}
                  onChange={(e) => setShowForm({ ...showForm, endTime: e.target.value })}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs text-slate-300 font-medium mb-1">Base Ticket Price (₹)</label>
              <input
                type="number"
                min="50"
                required
                value={showForm.ticketPrice}
                onChange={(e) => setShowForm({ ...showForm, ticketPrice: e.target.value })}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3 py-2 text-xs text-slate-100"
              />
            </div>

            <button
              type="submit"
              className="w-full bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs py-3.5 rounded-xl transition-all shadow-lg shadow-rose-950"
            >
              Schedule Show & Initialize Seats
            </button>
          </form>
        </div>
      )}

      {/* Tab 5: Ticket Entry Scanner */}
      {activeTab === 'validator' && (
        <div className="max-w-md mx-auto bg-slate-900 border border-slate-800 rounded-3xl p-8 space-y-6 shadow-2xl text-center">
          <div className="w-14 h-14 rounded-2xl bg-rose-600/10 border border-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
            <QrCode className="w-7 h-7" />
          </div>

          <div className="space-y-1">
            <h2 className="text-xl font-bold text-white">Cinema Gate Entry Scanner</h2>
            <p className="text-xs text-slate-400">
              Enter the ticket UUID code from customer's pass to check-in and mark as used.
            </p>
          </div>

          <form onSubmit={handleValidateTicket} className="space-y-3">
            <input
              type="text"
              required
              placeholder="Paste ticket code (e.g. 550e8400-e29b-...)"
              value={validateCode}
              onChange={(e) => setValidateCode(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-xs text-slate-100 text-center font-mono focus:outline-none focus:border-rose-500"
            />
            <button
              type="submit"
              disabled={validating}
              className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs py-3 rounded-xl transition-all shadow-lg shadow-emerald-950 disabled:opacity-50"
            >
              {validating ? 'Checking code...' : 'Validate & Admit Entry'}
            </button>
          </form>

          {/* Validation Result Box */}
          {validationResult && (
            <div
              className={`p-4 rounded-2xl border text-xs text-left space-y-1.5 ${
                validationResult.success
                  ? 'bg-emerald-950/60 border-emerald-500/40 text-emerald-300'
                  : 'bg-rose-950/60 border-rose-500/40 text-rose-300'
              }`}
            >
              <div className="font-bold flex items-center gap-1.5">
                {validationResult.success ? (
                  <CheckCircle className="w-4 h-4 text-emerald-400" />
                ) : (
                  <AlertCircle className="w-4 h-4 text-rose-400" />
                )}
                <span>
                  {validationResult.success ? 'VALID PASS - ENTRY ALLOWED' : 'ENTRY REJECTED'}
                </span>
              </div>
              {validationResult.success ? (
                <>
                  <p>Movie: <strong>{validationResult.ticket.movieTitle}</strong></p>
                  <p>Seats: <strong>{validationResult.ticket.seatNumbers?.join(', ')}</strong></p>
                  <p>Status: Checked-in & marked as USED.</p>
                </>
              ) : (
                <p>{validationResult.message}</p>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};
