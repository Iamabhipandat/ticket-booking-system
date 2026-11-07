import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Film, Ticket, User, LogOut, ShieldAlert, Sparkles, Menu, X, ChevronDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Navbar = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
    setDropdownOpen(false);
  };

  return (
    <nav className="sticky top-0 z-40 bg-[#080c14]/90 backdrop-blur-md border-b border-slate-800/80">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Brand Logo */}
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-rose-600 to-amber-500 flex items-center justify-center shadow-lg shadow-rose-950 group-hover:scale-105 transition-transform">
              <Film className="w-5 h-5 text-white" />
            </div>
            <div className="flex flex-col">
              <span className="text-xl font-extrabold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
                Cine<span className="text-rose-500">Pass</span>
              </span>
              <span className="text-[10px] text-slate-400 -mt-1 tracking-widest uppercase font-semibold">
                Tickets & Shows
              </span>
            </div>
          </Link>

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-6">
            <Link
              to="/"
              className="text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/50 px-3 py-2 rounded-lg transition-colors"
            >
              Movies
            </Link>
            {isAuthenticated && (
              <Link
                to="/my-bookings"
                className="text-sm font-medium text-slate-300 hover:text-white hover:bg-slate-800/50 px-3 py-2 rounded-lg transition-colors flex items-center gap-1.5"
              >
                <Ticket className="w-4 h-4 text-rose-400" />
                My Bookings
              </Link>
            )}
            {/* Admin / Dev Testing Suite */}
            <Link
              to="/admin"
              className="text-sm font-medium text-amber-400 hover:text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 px-3 py-1.5 rounded-lg transition-colors flex items-center gap-1.5 shadow-sm"
            >
              <Sparkles className="w-4 h-4" />
              <span>Admin & Testing Lab</span>
              {isAdmin && (
                <span className="bg-amber-500 text-slate-950 font-bold text-[10px] px-1.5 py-0.5 rounded uppercase">
                  Admin
                </span>
              )}
            </Link>
          </div>

          {/* User Auth Section */}
          <div className="hidden md:flex items-center gap-3">
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setDropdownOpen(!dropdownOpen)}
                  className="flex items-center gap-2 bg-slate-900 border border-slate-700/80 hover:border-slate-600 px-3 py-1.5 rounded-xl transition-all"
                >
                  <div className="w-7 h-7 rounded-lg bg-rose-600/20 border border-rose-500/30 flex items-center justify-center text-rose-400 font-bold text-xs uppercase">
                    {user?.name?.[0] || 'U'}
                  </div>
                  <div className="text-left text-xs">
                    <p className="font-semibold text-slate-200 leading-tight">{user?.name || user?.email}</p>
                    <span className="text-[10px] text-slate-400 uppercase font-mono">{user?.role?.replace('ROLE_', '')}</span>
                  </div>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400 ml-1" />
                </button>

                {/* Dropdown Menu */}
                {dropdownOpen && (
                  <div className="absolute right-0 mt-2 w-56 rounded-xl bg-slate-900 border border-slate-800 shadow-2xl p-1.5 z-50">
                    <div className="px-3 py-2 border-b border-slate-800 mb-1">
                      <p className="text-xs text-slate-400">Signed in as</p>
                      <p className="text-xs font-semibold text-slate-200 truncate">{user?.email}</p>
                    </div>

                    <Link
                      to="/my-bookings"
                      onClick={() => setDropdownOpen(false)}
                      className="flex items-center gap-2 px-3 py-2 text-xs text-slate-300 hover:bg-slate-800/80 rounded-lg transition-colors"
                    >
                      <Ticket className="w-4 h-4 text-slate-400" />
                      My Bookings
                    </Link>

                    <Link
                      to="/admin"
                      onClick={() => setDropdownOpen(false)}
                      className="flex items-center gap-2 px-3 py-2 text-xs text-amber-300 hover:bg-amber-950/40 rounded-lg transition-colors"
                    >
                      <Sparkles className="w-4 h-4 text-amber-400" />
                      Admin Control & Seeder
                    </Link>

                    <div className="border-t border-slate-800 my-1"></div>

                    <button
                      onClick={handleLogout}
                      className="w-full flex items-center gap-2 px-3 py-2 text-xs text-rose-400 hover:bg-rose-950/30 rounded-lg transition-colors"
                    >
                      <LogOut className="w-4 h-4" />
                      Sign Out
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/auth"
                  className="bg-rose-600 hover:bg-rose-700 text-white font-semibold text-sm px-4 py-2 rounded-xl transition-all shadow-lg shadow-rose-950/50 hover:shadow-rose-900/50 flex items-center gap-1.5"
                >
                  <User className="w-4 h-4" />
                  Sign In / Register
                </Link>
              </div>
            )}
          </div>

          {/* Mobile menu toggle */}
          <div className="md:hidden flex items-center">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden border-b border-slate-800 bg-[#080c14] px-4 pt-2 pb-4 space-y-2">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-slate-300 hover:text-white px-3 py-2 rounded-lg"
          >
            Movies
          </Link>
          {isAuthenticated && (
            <Link
              to="/my-bookings"
              onClick={() => setMobileMenuOpen(false)}
              className="block text-sm font-medium text-slate-300 hover:text-white px-3 py-2 rounded-lg"
            >
              My Bookings
            </Link>
          )}
          <Link
            to="/admin"
            onClick={() => setMobileMenuOpen(false)}
            className="block text-sm font-medium text-amber-400 hover:text-amber-300 px-3 py-2 rounded-lg"
          >
            Admin & Testing Lab
          </Link>
          <div className="pt-2 border-t border-slate-800">
            {isAuthenticated ? (
              <button
                onClick={handleLogout}
                className="w-full text-left text-sm font-medium text-rose-400 hover:text-rose-300 px-3 py-2"
              >
                Sign Out ({user?.email})
              </button>
            ) : (
              <Link
                to="/auth"
                onClick={() => setMobileMenuOpen(false)}
                className="block text-center text-sm font-semibold bg-rose-600 text-white px-4 py-2 rounded-xl"
              >
                Sign In
              </Link>
            )}
          </div>
        </div>
      )}
    </nav>
  );
};
