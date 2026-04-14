import { useEffect, useState } from 'react';
import { fetchDashboard, fetchProfile, sendChatMessage } from '../api';
import { BarChart3, Briefcase, BookmarkPlus, TrendingUp, MessageCircle, Upload, Target, Zap } from 'lucide-react';

const quickPrompts = [
  { icon: '📄', text: 'Analyze my resume for Java backend roles' },
  { icon: '💼', text: 'Suggest jobs for my profile' },
  { icon: '☁️', text: 'Prepare me for AWS interview' },
  { icon: '🎯', text: 'What skills am I missing for senior full stack roles?' }
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

  const bestAts = dashboard.atsScores[0] ?? 0;
  const getScoreColor = (score) => score >= 75 ? '#10b981' : score >= 50 ? '#f59e0b' : '#ef4444';

  return (
    <div className="dash-wrapper">
      <header className="dash-header">
        <div className="dash-welcome">
          <div className="dash-avatar">
            {profile?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div>
            <p className="dash-greeting">{getGreeting()}</p>
            <h1 className="dash-name">{profile?.fullName || 'User'}</h1>
          </div>
        </div>
        <div className="dash-quick-stats">
          <div className="qs-item">
            <span className="qs-num">{dashboard.resumeHistory.length}</span>
            <span className="qs-label">Resumes</span>
          </div>
          <div className="qs-item">
            <span className="qs-num">{dashboard.savedJobs.length}</span>
            <span className="qs-label">Saved Jobs</span>
          </div>
          <div className="qs-item accent">
            <span className="qs-num">{bestAts}%</span>
            <span className="qs-label">Best ATS</span>
          </div>
        </div>
      </header>

      <div className="dash-grid">
        <div className="dash-main">
          <div className="dash-card chat-card">
            <div className="card-head">
              <div className="card-title">
                <MessageCircle size={20} />
                <h3>AI Career Assistant</h3>
              </div>
              <span className="card-badge">Powered by AI</span>
            </div>
            
            <div className="quick-prompts-grid">
              {quickPrompts.map((p, i) => (
                <button key={i} className="prompt-chip" onClick={() => setMessage(p.text)}>
                  <span>{p.icon}</span>
                  <span>{p.text}</span>
                </button>
              ))}
            </div>

            <div className="dash-chat-area">
              {history.length === 0 ? (
                <div className="chat-empty-state">
                  <div className="chat-icon">
                    <MessageCircle size={24} />
                  </div>
                  <p>Ask anything about your career, resume, or interview prep</p>
                </div>
              ) : (
                <div className="dash-chat-messages">
                  {history.map((entry, index) => (
                    <div key={index} className={`dash-msg ${entry.role}`}>
                      <div className="msg-avatar">{entry.role === 'user' ? 'U' : 'AI'}</div>
                      <div className="msg-bubble">{entry.content}</div>
                    </div>
                  ))}
                  {loading && (
                    <div className="dash-msg assistant">
                      <div className="msg-avatar">AI</div>
                      <div className="msg-bubble typing">
                        <span></span><span></span><span></span>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>

            <form className="dash-chat-form" onSubmit={submitChat}>
              <input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Ask about careers, jobs, interviews..."
                disabled={loading}
              />
              <button type="submit" disabled={loading || !message.trim()}>
                <Zap size={18} />
              </button>
            </form>
          </div>
        </div>

        <div className="dash-sidebar">
          <div className="dash-card profile-card">
            <div className="card-head">
              <div className="card-title">
                <Target size={20} />
                <h3>Your Profile</h3>
              </div>
            </div>
            <div className="profile-info-grid">
              <div className="pi-item">
                <span className="pi-label">Role</span>
                <span className="pi-value">{profile?.currentRole || 'Not set'}</span>
              </div>
              <div className="pi-item">
                <span className="pi-label">Location</span>
                <span className="pi-value">{profile?.city || 'Not set'}</span>
              </div>
              <div className="pi-item">
                <span className="pi-label">Experience</span>
                <span className="pi-value">{profile?.experienceYears ? `${profile.experienceYears} years` : 'Not set'}</span>
              </div>
              <div className="pi-item full">
                <span className="pi-label">Top Skills</span>
                <span className="pi-value">{profile?.skills || 'Not set'}</span>
              </div>
            </div>
          </div>

          <div className="dash-card">
            <div className="card-head">
              <div className="card-title">
                <BarChart3 size={20} />
                <h3>Resume Scores</h3>
              </div>
            </div>
            {dashboard.resumeHistory.length > 0 ? (
              <div className="resume-scores">
                {dashboard.resumeHistory.map((item, i) => (
                  <div key={i} className="score-item">
                    <div className="score-info">
                      <span className="score-name">{item.candidateName}</span>
                      <span className="score-file">{item.fileName}</span>
                    </div>
                    <div className="score-circle" style={{ borderColor: getScoreColor(item.atsScore) }}>
                      <span style={{ color: getScoreColor(item.atsScore) }}>{item.atsScore}%</span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="empty-mini">
                <Upload size={20} />
                <p>No resumes uploaded yet</p>
              </div>
            )}
          </div>

          <div className="dash-card">
            <div className="card-head">
              <div className="card-title">
                <BookmarkPlus size={20} />
                <h3>Saved Jobs</h3>
              </div>
            </div>
            {dashboard.savedJobs.length > 0 ? (
              <div className="saved-jobs-mini">
                {dashboard.savedJobs.slice(0, 3).map((job, i) => (
                  <a key={i} href={job.applyLink} target="_blank" rel="noreferrer" className="saved-job-mini">
                    <Briefcase size={14} />
                    <div>
                      <span className="sj-title">{job.title}</span>
                      <span className="sj-company">{job.company}</span>
                    </div>
                    <span className="sj-score" style={{ color: getScoreColor(job.matchScore) }}>{job.matchScore}%</span>
                  </a>
                ))}
              </div>
            ) : (
              <div className="empty-mini">
                <BookmarkPlus size={20} />
                <p>No saved jobs yet</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function getGreeting() {
  const hour = new Date().getHours();
  if (hour < 12) return 'Good morning';
  if (hour < 17) return 'Good afternoon';
  return 'Good evening';
}
