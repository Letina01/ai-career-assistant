import { Navigate, NavLink, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import AuthPage from './pages/AuthPage';
import DashboardPage from './pages/DashboardPage';
import ResumePage from './pages/ResumePage';
import JobsPage from './pages/JobsPage';
import ChatbotPage from './pages/ChatbotPage';
import InterviewPage from './pages/InterviewPage';
import SkillGapPage from './pages/SkillGapPage';
import ResumeImprovePage from './pages/ResumeImprovePage';
import RecruiterPage from './pages/RecruiterPage';
import ProfilePage from './pages/ProfilePage';

export default function App() {
  const { user, logout } = useAuth();
  const applicantDefaultRoute = !user?.profileComplete ? '/profile' : (!user?.resumeUploaded ? '/resume' : '/');
  const recruiterLinks = [
    ['/', 'Recruiter Hub']
  ];
  const applicantLinks = [
    ['/', 'Dashboard'],
    ['/profile', 'Profile'],
    ['/resume', 'Resume'],
    ['/jobs', 'Jobs'],
    ['/chatbot', 'Chatbot'],
    ['/interview', 'Interview Prep'],
    ['/skill-gap', 'Skill Gap'],
    ['/resume-improve', 'Resume Rewrite']
  ];
  const links = user?.role === 'RECRUITER' ? recruiterLinks : applicantLinks;

  return (
    <div className={user ? 'shell app-shell' : 'shell'}>
      {user && (
        <aside className="sidebar">
          <div className="sidebar-brand">
            <div className="brand-icon-new">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div>
              <h1>CareerAI</h1>
              <p className="muted">{user.role}</p>
            </div>
          </div>
          <nav className="nav">
            {links.map(([to, label]) => (
              <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
                {label}
              </NavLink>
            ))}
          </nav>
          <div className="sidebar-footer">
            <div className="user-info">
              <span>{user.fullName}</span>
              <button type="button" onClick={logout} className="logout-btn">
                Logout
              </button>
            </div>
          </div>
        </aside>
      )}

      <main className="content">
        <Routes>
          <Route path="/" element={user ? (user.role === 'RECRUITER' ? <RecruiterPage /> : (user.profileComplete && user.resumeUploaded ? <DashboardPage /> : <Navigate to={applicantDefaultRoute} replace />)) : <Navigate to="/auth" replace />} />
          <Route path="/auth" element={user ? <Navigate to={user.role === 'RECRUITER' ? '/' : applicantDefaultRoute} replace /> : <AuthPage />} />
          <Route path="/profile" element={user?.role === 'APPLICANT' ? <ProfilePage /> : <Navigate to="/auth" replace />} />
          <Route path="/resume" element={user?.role === 'APPLICANT' ? (user.profileComplete ? <ResumePage /> : <Navigate to="/profile" replace />) : <Navigate to="/auth" replace />} />
          <Route path="/jobs" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <JobsPage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
          <Route path="/chatbot" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <ChatbotPage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
          <Route path="/interview" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <InterviewPage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
          <Route path="/skill-gap" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <SkillGapPage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
          <Route path="/resume-improve" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <ResumeImprovePage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
          <Route path="*" element={<Navigate to={user ? '/' : '/auth'} replace />} />
        </Routes>
      </main>
    </div>
  );
}
