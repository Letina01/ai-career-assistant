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
          setSuccess('Check your email for the reset token.');
        } else {
          await resetPassword({ token: form.resetToken, newPassword: form.password });
          setSuccess('Password reset successful! Please login.');
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
      setError(err?.response?.data?.message || 'Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const switchMode = (newMode) => {
    setMode(newMode);
    setError('');
    setSuccess('');
    setResetRequested(false);
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-container">
        <div className="auth-visual">
          <div className="visual-content">
            <div className="brand-mark">
              <div className="brand-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                  <path d="M2 17l10 5 10-5"/>
                  <path d="M2 12l10 5 10-5"/>
                </svg>
              </div>
              <h1>CareerAI</h1>
            </div>
            <h2>Accelerate Your Career Journey</h2>
            <p>AI-powered tools to analyze resumes, find perfect job matches, and prepare for interviews.</p>
            
            <div className="visual-features">
              <div className="visual-feature">
                <span className="feature-icon">&#129309;</span>
                <span>Smart Resume Analysis</span>
              </div>
              <div className="visual-feature">
                <span className="feature-icon">&#128640;</span>
                <span>AI Job Matching</span>
              </div>
              <div className="visual-feature">
                <span className="feature-icon">&#9997;</span>
                <span>Interview Prep</span>
              </div>
              <div className="visual-feature">
                <span className="feature-icon">&#128200;</span>
                <span>Skill Gap Analysis</span>
              </div>
            </div>

            <div className="visual-testimonial">
              <p>"This platform helped me land my dream job at a top tech company!"</p>
              <div className="testimonial-author">
                <div className="author-avatar">A</div>
                <div>
                  <strong>Ayush Sharma</strong>
                  <span>Software Engineer at Google</span>
                </div>
              </div>
            </div>
          </div>
          <div className="visual-decoration">
            <div className="deco-circle circle-1"></div>
            <div className="deco-circle circle-2"></div>
            <div className="deco-circle circle-3"></div>
          </div>
        </div>

        <div className="auth-form-section">
          <div className="auth-card">
            <div className="card-header">
              <div className="card-logo">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                  <path d="M2 17l10 5 10-5"/>
                  <path d="M2 12l10 5 10-5"/>
                </svg>
              </div>
              <h3>
                {mode === 'register' ? 'Create Account' : 
                 mode === 'login' ? 'Welcome Back' : 'Reset Password'}
              </h3>
              <p className="muted">
                {mode === 'register' ? 'Start your career journey today' :
                 mode === 'login' ? 'Enter your credentials to continue' :
                 resetRequested ? 'Enter the token from your email' : 'Enter your registered email'}
              </p>
            </div>

            <form onSubmit={submit}>
              {mode === 'register' && (
                <div className="form-group">
                  <label>Full Name</label>
                  <input
                    value={form.fullName}
                    onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                    placeholder="Enter your full name"
                    required
                  />
                </div>
              )}

              {mode === 'register' && (
                <div className="form-group">
                  <label>I am a</label>
                  <div className="role-selector">
                    <button
                      type="button"
                      className={`role-btn ${form.role === 'APPLICANT' ? 'active' : ''}`}
                      onClick={() => setForm({ ...form, role: 'APPLICANT' })}
                    >
                      <span className="role-icon">&#128100;</span>
                      <span>Job Seeker</span>
                    </button>
                    <button
                      type="button"
                      className={`role-btn ${form.role === 'RECRUITER' ? 'active' : ''}`}
                      onClick={() => setForm({ ...form, role: 'RECRUITER' })}
                    >
                      <span className="role-icon">&#128188;</span>
                      <span>Recruiter</span>
                    </button>
                  </div>
                </div>
              )}

              <div className="form-group">
                <label>Email Address</label>
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  placeholder="name@company.com"
                  required
                />
              </div>

              <div className="form-group">
                <label>Password</label>
                <input
                  type="password"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  placeholder={mode === 'forgot' && resetRequested ? 'New password (min 8 chars)' : 'Enter your password'}
                  required
                  minLength={8}
                />
              </div>

              {mode === 'forgot' && resetRequested && (
                <div className="form-group">
                  <label>Reset Token</label>
                  <input
                    value={form.resetToken}
                    onChange={(e) => setForm({ ...form, resetToken: e.target.value })}
                    placeholder="Paste the token from your email"
                    required
                  />
                </div>
              )}

              {success && (
                <div className="alert alert-success">
                  <span>&#10003;</span> {success}
                </div>
              )}

              {error && (
                <div className="alert alert-error">
                  <span>&#9888;</span> {error}
                </div>
              )}

              <button type="submit" className="btn-submit" disabled={loading}>
                {loading ? (
                  <span className="loading-dots">
                    <span></span><span></span><span></span>
                  </span>
                ) : (
                  <>
                    {mode === 'register' ? 'Create Account' :
                     mode === 'login' ? 'Sign In' :
                     resetRequested ? 'Reset Password' : 'Send Reset Link'}
                    <span className="btn-arrow">&#8594;</span>
                  </>
                )}
              </button>
            </form>

            <div className="card-footer">
              {mode !== 'forgot' && (
                <p className="toggle-text">
                  {mode === 'register' ? 'Already have an account?' : "Don't have an account?"}
                  <button type="button" onClick={() => switchMode(mode === 'register' ? 'login' : 'register')}>
                    {mode === 'register' ? 'Sign In' : 'Create Account'}
                  </button>
                </p>
              )}
              
              {mode === 'login' && (
                <p className="toggle-text">
                  Forgot your password?
                  <button type="button" onClick={() => switchMode('forgot')}>
                    Reset Password
                  </button>
                </p>
              )}

              {mode === 'forgot' && (
                <p className="toggle-text">
                  Remember your password?
                  <button type="button" onClick={() => switchMode('login')}>
                    Sign In
                  </button>
                </p>
              )}
            </div>
          </div>

          <p className="auth-terms">
            By continuing, you agree to our Terms of Service and Privacy Policy
          </p>
        </div>
      </div>
    </div>
  );
}
