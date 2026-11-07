import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Film, Lock, Mail, User, Shield, CheckCircle, Sparkles, AlertCircle } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

export const AuthPage = () => {
  const [tab, setTab] = useState('login'); // 'login' | 'register'
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [role, setRole] = useState('ROLE_CUSTOMER'); // 'ROLE_CUSTOMER' | 'ROLE_ADMIN'
  const [loading, setLoading] = useState(false);
  const [registeredMsg, setRegisteredMsg] = useState('');

  const { login, register } = useAuth();
  const { success, error } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || '/';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setRegisteredMsg('');

    try {
      if (tab === 'login') {
        await login(email, password);
        success('Signed in successfully!');
        navigate(from, { replace: true });
      } else {
        const res = await register(name, email, password, role);
        setRegisteredMsg(res.message || 'Registration successful! Please check your email to verify your account.');
        success('Account created! Please verify your email.');
      }
    } catch (err) {
      console.error('Auth error:', err);
      const msg = err.response?.data?.message || err.message || 'Authentication failed';
      error(msg);
    } finally {
      setLoading(false);
    }
  };

  const fillDemoCredentials = (type) => {
    if (type === 'admin') {
      setEmail('admin@cinepass.com');
      setPassword('Admin@123456');
      setName('System Admin');
      setRole('ROLE_ADMIN');
    } else {
      setEmail('customer@cinepass.com');
      setPassword('Customer@123456');
      setName('Demo Customer');
      setRole('ROLE_CUSTOMER');
    }
  };

  return (
    <div className="min-h-[85vh] flex items-center justify-center px-4 py-16">
      <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl space-y-6">
        {/* Header Icon & Title */}
        <div className="text-center space-y-2">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-rose-600 to-amber-500 flex items-center justify-center mx-auto shadow-lg shadow-rose-950">
            <Film className="w-6 h-6 text-white" />
          </div>
          <h1 className="text-2xl font-black text-white">
            {tab === 'login' ? 'Welcome Back' : 'Create an Account'}
          </h1>
          <p className="text-xs text-slate-400">
            {tab === 'login'
              ? 'Sign in to access your movie bookings and tickets'
              : 'Sign up to start reserving seats in seconds'}
          </p>
        </div>

        {/* Tab Toggle */}
        <div className="flex bg-slate-950 p-1.5 rounded-2xl border border-slate-800">
          <button
            type="button"
            onClick={() => {
              setTab('login');
              setRegisteredMsg('');
            }}
            className={`flex-1 text-xs font-bold py-2.5 rounded-xl transition-all ${
              tab === 'login'
                ? 'bg-rose-600 text-white shadow-md shadow-rose-950'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => {
              setTab('register');
              setRegisteredMsg('');
            }}
            className={`flex-1 text-xs font-bold py-2.5 rounded-xl transition-all ${
              tab === 'register'
                ? 'bg-rose-600 text-white shadow-md shadow-rose-950'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Register
          </button>
        </div>

        {/* Registration Success Banner */}
        {registeredMsg && (
          <div className="bg-emerald-950/60 border border-emerald-500/50 rounded-2xl p-4 text-xs text-emerald-300 space-y-2">
            <div className="flex items-center gap-2 font-bold">
              <CheckCircle className="w-4 h-4 text-emerald-400" />
              <span>Registration Completed!</span>
            </div>
            <p className="leading-relaxed">{registeredMsg}</p>
            <p className="text-[11px] text-slate-400">
              Check your email for the verification link, or use the "Verify Email" tab if testing locally.
            </p>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          {tab === 'register' && (
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Full Name</label>
              <div className="relative">
                <User className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <input
                  type="text"
                  required
                  placeholder="e.g. John Doe"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-4 py-2.5 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-rose-500"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">Email Address</label>
            <div className="relative">
              <Mail className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
              <input
                type="email"
                required
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-4 py-2.5 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-rose-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">Password</label>
            <div className="relative">
              <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
              <input
                type="password"
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl pl-10 pr-4 py-2.5 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-rose-500"
              />
            </div>
          </div>

          {/* Role selector in register */}
          {tab === 'register' && (
            <div className="p-3 bg-slate-950/80 border border-slate-800 rounded-xl space-y-2">
              <label className="block text-xs font-bold text-slate-300 flex items-center gap-1.5">
                <Shield className="w-3.5 h-3.5 text-amber-400" />
                Select Account Role:
              </label>
              <div className="grid grid-cols-2 gap-2 text-xs">
                <button
                  type="button"
                  onClick={() => setRole('ROLE_CUSTOMER')}
                  className={`p-2 rounded-lg border font-semibold text-center transition-all ${
                    role === 'ROLE_CUSTOMER'
                      ? 'bg-rose-600/20 border-rose-500 text-rose-300 font-bold'
                      : 'bg-slate-900 border-slate-700 text-slate-400'
                  }`}
                >
                  Customer
                </button>
                <button
                  type="button"
                  onClick={() => setRole('ROLE_ADMIN')}
                  className={`p-2 rounded-lg border font-semibold text-center transition-all ${
                    role === 'ROLE_ADMIN'
                      ? 'bg-amber-500/20 border-amber-500 text-amber-300 font-bold'
                      : 'bg-slate-900 border-slate-700 text-slate-400'
                  }`}
                >
                  Admin / Staff
                </button>
              </div>
              <p className="text-[10px] text-slate-500 leading-tight">
                * Select Admin to test creating movies, theatres, screens, and shows.
              </p>
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs py-3 rounded-xl transition-all shadow-lg shadow-rose-950 disabled:opacity-50 mt-2"
          >
            {loading ? 'Please wait...' : tab === 'login' ? 'Sign In to CinePass' : 'Complete Registration'}
          </button>
        </form>

        {/* Quick Demo Fill Buttons for Testing */}
        <div className="border-t border-slate-800 pt-4 space-y-2 text-center">
          <span className="text-[10px] uppercase tracking-wider font-semibold text-slate-500 block">
            Quick Testing Presets
          </span>
          <div className="flex gap-2 justify-center">
            <button
              type="button"
              onClick={() => fillDemoCredentials('customer')}
              className="text-[11px] bg-slate-800 hover:bg-slate-700 text-slate-300 px-3 py-1.5 rounded-lg border border-slate-700 transition-colors"
            >
              Fill Customer Demo
            </button>
            <button
              type="button"
              onClick={() => fillDemoCredentials('admin')}
              className="text-[11px] bg-amber-950/40 hover:bg-amber-900/40 text-amber-400 px-3 py-1.5 rounded-lg border border-amber-700/50 transition-colors"
            >
              Fill Admin Demo
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
