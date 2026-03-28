import { useEffect, useState } from 'react';
import { fetchDashboard, fetchProfile, sendChatMessage } from '../api';
import SectionCard from '../components/SectionCard';
import { BarChart3, Briefcase, BookmarkPlus, TrendingUp, MessageCircle } from 'lucide-react';

const quickPrompts = [
  'Analyze my resume for Java backend roles',
  'Suggest jobs for my profile',
  'Prepare me for AWS interview',
  'What skills am I missing for senior full stack roles?'
];

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState({ resumeHistory: [], atsScores: [], savedJobs: [] });
  const [profile, setProfile] = useState(null);
  const [message, setMessage] = useState('');
  const [sessionId, setSessionId] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchDashboard().then(setDashboard).catch(() => {});
    fetchProfile().then(setProfile).catch(() => {});
  }, []);

  const submitChat = async (event) => {
    event.preventDefault();
    if (!message.trim()) return;

    setLoading(true);
    try {
      const response = await sendChatMessage({ sessionId, message });
      setSessionId(response.sessionId);
      setHistory(response.history);
      setMessage('');
    } catch (err) {
      alert('Failed to send message');
    } finally {
      setLoading(false);
    }
  };

  const handleQuickPrompt = (prompt) => {
    setMessage(prompt);
  };

  const bestAts = dashboard.atsScores[0] ?? null;

  return (
    <div className="applicant-home">
      <section className="hero-panel">
        <div className="hero-content">
          <p className="eyebrow accent-eyebrow">Applicant Workspace</p>
          <h2>{profile?.fullName ? `Welcome back, ${profile.fullName}! 👋` : 'Build your career profile'}</h2>
          <p className="hero-copy">
            Your AI-powered career assistant is ready. Chat with us, upload your resume, discover matching jobs, and prepare for interviews—all in one place.
          </p>
          <div className="stats-chips">
            <div className="stat-chip">
              <span className="stat-number">{dashboard.resumeHistory.length}</span>
              <span className="stat-label">Resumes Uploaded</span>
            </div>
            <div className="stat-chip">
              <span className="stat-number">{dashboard.savedJobs.length}</span>
              <span className="stat-label">Saved Jobs</span>
            </div>
            {bestAts && (
              <div className="stat-chip highlight">
                <span className="stat-number">{bestAts}%</span>
                <span className="stat-label">Best ATS Score</span>
              </div>
            )}
          </div>
        </div>
        <div className="hero-side-card">
          <strong>🎯 Your Profile</strong>
          <div className="profile-snapshot">
            <div>
              <p className="snapshot-label">Current Role</p>
              <p className="snapshot-value">{profile?.currentRole || '—'}</p>
            </div>
            <div>
              <p className="snapshot-label">Location & Experience</p>
              <p className="snapshot-value">
                {profile?.city || 'N/A'} {profile?.experienceYears ? `| ${profile.experienceYears} yrs` : ''}
              </p>
            </div>
            <div>
              <p className="snapshot-label">Top Skills</p>
              <p className="snapshot-value">{profile?.skills || '—'}</p>
            </div>
          </div>
        </div>
      </section>

      <div className="page-grid">
        <SectionCard title="Chat Assistant" subtitle="Get personalized career guidance">
          <div className="quick-prompts">
            {quickPrompts.map((prompt) => (
              <button
                key={prompt}
                type="button"
                className="quick-prompt-btn"
                onClick={() => handleQuickPrompt(prompt)}
              >
                <MessageCircle size={16} />
                {prompt}
              </button>
            ))}
          </div>

          <form className="form inline-form chat-form-dashboard" onSubmit={submitChat}>
            <input
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Ask for resume tips, job suggestions, or interview prep..."
              disabled={loading}
            />
            <button type="submit" disabled={loading}>
              Send
            </button>
          </form>

          <div className="chat-window home-chat">
            {history.length === 0 && (
              <div className="empty-state">
                <p className="muted">💬 Start by asking about your career goals or resume improvement.</p>
              </div>
            )}
            {history.map((entry, index) => (
              <div key={`${entry.role}-${index}`} className={`chat-bubble ${entry.role}`}>
                <p>{entry.content}</p>
              </div>
            ))}
            {loading && (
              <div className="chat-bubble assistant">
                <p className="typing-indicator">
                  <span></span><span></span><span></span>
                </p>
              </div>
            )}
          </div>
        </SectionCard>

        <SectionCard title="Resume & ATS Scores" subtitle="Your recent uploads and analysis">
          {dashboard.resumeHistory.length > 0 ? (
            <div className="resume-list">
              {dashboard.resumeHistory.map((item) => (
                <div key={item.resumeId} className="resume-item">
                  <div className="resume-info">
                    <BarChart3 size={20} color="var(--accent)" />
                    <div>
                      <p className="resume-name">{item.fileName}</p>
                      <p className="resume-candidate">{item.candidateName}</p>
                    </div>
                  </div>
                  <div className="resume-score">
                    <span className="score-badge" style={{ color: item.atsScore >= 75 ? '#059669' : item.atsScore >= 50 ? '#d97706' : '#dc2626' }}>
                      {item.atsScore ?? 'N/A'}%
                    </span>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="muted">📄 Upload your first resume in the Resume section to get ATS insights.</p>
          )}
        </SectionCard>

        <SectionCard title="Saved Opportunities" subtitle="Jobs you bookmarked">
          {dashboard.savedJobs.length > 0 ? (
            <div className="saved-jobs-list">
              {dashboard.savedJobs.map((job, index) => (
                <a
                  key={`${job.title}-${index}`}
                  href={job.applyLink}
                  target="_blank"
                  rel="noreferrer"
                  className="saved-job-item"
                >
                  <div className="job-info">
                    <Briefcase size={20} color="var(--forest)" />
                    <div>
                      <p className="job-title">{job.title}</p>
                      <p className="job-company">{job.company}</p>
                    </div>
                  </div>
                  <span className="match-badge-small">
                    <TrendingUp size={14} />
                    {job.matchScore}%
                  </span>
                </a>
              ))}
            </div>
          ) : (
            <p className="muted">⭐ Save jobs from recommendations to see them here.</p>
          )}
        </SectionCard>
      </div>
    </div>
  );
}
