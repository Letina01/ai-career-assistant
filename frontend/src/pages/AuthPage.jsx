import { useState } from 'react';
import { login, register, requestPasswordReset, resetPassword } from '../api';
import { useAuth } from '../context/AuthContext';

export default function AuthPage() {
  const { login: storeLogin } = useAuth();
  const [mode, setMode] = useState('register');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    resetToken: '',
    password: '',
    role: 'APPLICANT'
  });
  const [resetRequested, setResetRequested] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      if (mode === 'forgot') {
        if (!resetRequested) {
          await requestPasswordReset({ email: form.email });
          setResetRequested(true);
          setSuccess('If the email exists, we have sent a reset token to it.');
        } else {
          await resetPassword({ token: form.resetToken, newPassword: form.password });
          setSuccess('Password reset successful. Please login now.');
          setResetRequested(false);
          setMode('login');
          setForm((current) => ({ ...current, resetToken: '', password: '' }));
        }
      } else {
        const action = mode === 'login' ? login : register;
        const payload = mode === 'login'
          ? { email: form.email, password: form.password }
          : form;
        const response = await action(payload);
        storeLogin(response);
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Unable to continue. Check your details.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      <section className="auth-showcase">
        <div>
          <p className="eyebrow accent-eyebrow">AI Career Assistant</p>
          <h1>Your AI-powered career companion</h1>
          <p className="showcase-subtitle">
            Upload your resume, get AI insights, find perfect job matches, and ace interviews with personalized guidance.
          </p>
        </div>
        <div className="feature-stack">
          <div className="feature-tile">
            <strong>✨ For Applicants</strong>
            <span>Resume analysis, AI chatbot, job recommendations, interview prep, and skill gap analysis.</span>
          </div>
          <div className="feature-tile">
            <strong>💼 For Recruiters</strong>
            <span>Post jobs, review ATS-scored candidates, and manage applications seamlessly.</span>
          </div>
        </div>
      </section>

      <section className="auth-panel">
        <div className="auth-copy">
          <h2>{mode === 'register' ? 'Get started' : mode === 'login' ? 'Welcome back' : 'Reset your password'}</h2>
          <p className="muted">
            {mode === 'register'
              ? 'Create your account to access AI-powered career tools.'
              : mode === 'login'
                ? 'Login with your email and password.'
                : 'Set a new password using your registered email.'}
          </p>
        </div>

        <form className="form" onSubmit={submit}>
          {mode === 'register' && (
            <>
              <input 
                value={form.fullName} 
                onChange={(e) => setForm({ ...form, fullName: e.target.value })} 
                placeholder="Full name" 
                required
              />
              <select 
                value={form.role} 
                onChange={(e) => setForm({ ...form, role: e.target.value })}
              >
                <option value="APPLICANT">Applicant</option>
                <option value="RECRUITER">Recruiter</option>
              </select>
            </>
          )}
          <input 
            type="email" 
            value={form.email} 
            onChange={(e) => setForm({ ...form, email: e.target.value })} 
            placeholder="Email address" 
            required
          />
          <input 
            type="password" 
            value={form.password} 
            onChange={(e) => setForm({ ...form, password: e.target.value })} 
            placeholder={mode === 'forgot' ? (resetRequested ? 'New password' : 'Set new password') : 'Password'}
            required
          />
          {mode === 'forgot' && resetRequested && (
            <input
              value={form.resetToken}
              onChange={(e) => setForm({ ...form, resetToken: e.target.value })}
              placeholder="Reset token from email"
              required
            />
          )}
          {success && <p className="success-text">{success}</p>}
          {error && <p className="error-text">{error}</p>}
          <button type="submit" disabled={loading}>
            {loading ? 'Processing...' : (mode === 'register' ? 'Continue to workspace' : mode === 'login' ? 'Login to workspace' : (resetRequested ? 'Confirm reset' : 'Send reset token'))}
          </button>
        </form>

        <div className="auth-footer">
          <p className="muted">
            {mode === 'register' ? 'Already have an account? ' : "Don't have an account? "}
            <button 
              type="button" 
              className="text-link"
              onClick={() => {
                setMode(mode === 'register' ? 'login' : 'register');
                setError('');
                setSuccess('');
                setResetRequested(false);
              }}
            >
              {mode === 'register' ? 'Login' : 'Register'}
            </button>
          </p>
          {mode !== 'forgot' && (
            <p className="muted">
              Forgot password?{' '}
              <button
                type="button"
                className="text-link"
                onClick={() => {
                  setMode('forgot');
                  setError('');
                  setSuccess('');
                  setResetRequested(false);
                }}
              >
                Reset here
              </button>
            </p>
          )}
        </div>
      </section>
    </div>
  );
}
