import React, { useState, useEffect } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle2, AlertCircle, Mail, ArrowRight } from 'lucide-react';
import api from '../api/axios';
import { LoadingSpinner } from '../components/LoadingSpinner';

export const VerifyEmailPage = () => {
  const [searchParams] = useSearchParams();
  const tokenFromQuery = searchParams.get('token');
  const [tokenInput, setTokenInput] = useState(tokenFromQuery || '');
  const [status, setStatus] = useState('idle'); // 'idle' | 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (tokenFromQuery) {
      verifyToken(tokenFromQuery);
    }
  }, [tokenFromQuery]);

  const verifyToken = async (tok) => {
    if (!tok.trim()) return;
    setStatus('loading');
    try {
      const res = await api.get(`/api/auth/verify-email?token=${encodeURIComponent(tok.trim())}`);
      setStatus('success');
      setMessage(res.data.message || 'Your email address has been successfully verified! You can now sign in.');
    } catch (err) {
      setStatus('error');
      setMessage(err.response?.data?.message || 'Invalid or expired verification token.');
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-16">
      <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl text-center space-y-6">
        {status === 'loading' ? (
          <LoadingSpinner text="Verifying token with backend..." />
        ) : status === 'success' ? (
          <div className="space-y-4">
            <div className="w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-8 h-8" />
            </div>
            <h2 className="text-2xl font-bold text-white">Email Verified!</h2>
            <p className="text-xs text-slate-300 leading-relaxed">{message}</p>
            <Link
              to="/auth"
              className="inline-flex items-center gap-2 bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs px-6 py-3 rounded-xl transition-all shadow-lg shadow-rose-950 mt-4"
            >
              Sign In to Your Account <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        ) : status === 'error' ? (
          <div className="space-y-4">
            <div className="w-16 h-16 rounded-full bg-rose-500/10 border border-rose-500/30 text-rose-400 flex items-center justify-center mx-auto">
              <AlertCircle className="w-8 h-8" />
            </div>
            <h2 className="text-2xl font-bold text-white">Verification Failed</h2>
            <p className="text-xs text-rose-300 leading-relaxed">{message}</p>
            <div className="pt-2">
              <Link
                to="/auth"
                className="text-xs text-slate-400 hover:text-white underline transition-colors"
              >
                Return to Login
              </Link>
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <div className="w-14 h-14 rounded-2xl bg-rose-600/10 border border-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
              <Mail className="w-6 h-6" />
            </div>
            <h2 className="text-xl font-bold text-white">Verify Your Account</h2>
            <p className="text-xs text-slate-400 leading-relaxed">
              If you have a verification token from your email, paste it below to verify:
            </p>
            <div className="space-y-3">
              <input
                type="text"
                placeholder="Paste token UUID here..."
                value={tokenInput}
                onChange={(e) => setTokenInput(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-4 py-2.5 text-xs text-slate-100 placeholder:text-slate-500 focus:outline-none focus:border-rose-500 text-center font-mono"
              />
              <button
                onClick={() => verifyToken(tokenInput)}
                disabled={!tokenInput.trim()}
                className="w-full bg-rose-600 hover:bg-rose-700 disabled:opacity-50 text-white font-bold text-xs py-3 rounded-xl transition-all shadow-lg shadow-rose-950"
              >
                Verify Token
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
