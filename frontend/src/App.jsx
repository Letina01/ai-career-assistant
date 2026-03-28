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
import ApplicationsPage from './pages/ApplicationsPage';
import ProfilePage from './pages/ProfilePage';

export default function App() {
  const { user, logout } = useAuth();
  const applicantDefaultRoute = !user?.profileComplete ? '/profile' : (!user?.resumeUploaded ? '/resume' : '/');
  const recruiterLinks = [
    ['/', 'Recruiter Hub']
  ];
  const applicantLinks = [
    ['/', 'Home'],
    ['/profile', 'Profile'],
    ['/resume', 'Resume'],
    ['/jobs', 'Jobs'],
    ['/applications', 'Applications'],
    ['/chatbot', 'Chatbot'],
    ['/interview', 'Interview Prep'],
    ['/skill-gap', 'Skill Gap'],
    ['/resume-improve', 'Resume Rewrite']
  ];
  const links = user?.role === 'RECRUITER' ? recruiterLinks : applicantLinks;

  return (
    <div className={user ? 'shell app-shell' : 'shell auth-shell'}>
      {user && (
        <aside className="sidebar">
          <div>
            <p className="eyebrow accent-eyebrow">AI Career Assistant</p>
            <h1>Career OS</h1>
            <p className="muted">{`${user.fullName} | ${user.role}`}</p>
          </div>
          <nav className="nav">
            {links.map(([to, label]) => (
              <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>
                {label}
              </NavLink>
            ))}
            <button type="button" onClick={logout}>Logout</button>
          </nav>
        </aside>
      )}

      <main className="content">
        <Routes>
          <Route path="/" element={user ? (user.role === 'RECRUITER' ? <RecruiterPage /> : (user.profileComplete && user.resumeUploaded ? <DashboardPage /> : <Navigate to={applicantDefaultRoute} replace />)) : <Navigate to="/auth" replace />} />
          <Route path="/auth" element={user ? <Navigate to={user.role === 'RECRUITER' ? '/' : applicantDefaultRoute} replace /> : <AuthPage />} />
          <Route path="/profile" element={user?.role === 'APPLICANT' ? <ProfilePage /> : <Navigate to="/auth" replace />} />
          <Route path="/resume" element={user?.role === 'APPLICANT' ? (user.profileComplete ? <ResumePage /> : <Navigate to="/profile" replace />) : <Navigate to="/auth" replace />} />
          <Route path="/jobs" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <JobsPage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
          <Route path="/applications" element={user?.role === 'APPLICANT' ? (user.profileComplete && user.resumeUploaded ? <ApplicationsPage /> : <Navigate to={applicantDefaultRoute} replace />) : <Navigate to="/auth" replace />} />
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
