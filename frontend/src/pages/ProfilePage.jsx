import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchProfile, updateProfile } from '../api';
import { useAuth } from '../context/AuthContext';
import { User, MapPin, Briefcase, Link, Save, Check, Phone, Mail, GraduationCap, Clock, DollarSign } from 'lucide-react';

const emptyProfile = {
  fullName: '', phone: '', city: '', preferredRole: '', preferredLocation: '',
  currentRole: '', currentCompany: '', experienceYears: '', noticePeriodDays: '',
  expectedSalary: '', bio: '', skills: '', education: '', linkedinUrl: '', githubUrl: '', portfolioUrl: ''
};

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState(emptyProfile);
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(true);
  const [activeSection, setActiveSection] = useState('basic');

  useEffect(() => {
    fetchProfile().then((profile) => {
      setForm({
        fullName: profile.fullName || '',
        phone: profile.phone || '',
        city: profile.city || '',
        preferredRole: profile.preferredRole || '',
        preferredLocation: profile.preferredLocation || '',
        currentRole: profile.currentRole || '',
        currentCompany: profile.currentCompany || '',
        experienceYears: profile.experienceYears || '',
        noticePeriodDays: profile.noticePeriodDays || '',
        expectedSalary: profile.expectedSalary || '',
        bio: profile.bio || '',
        skills: profile.skills || '',
        education: profile.education || '',
        linkedinUrl: profile.linkedinUrl || '',
        githubUrl: profile.githubUrl || '',
        portfolioUrl: profile.portfolioUrl || ''
      });
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    try {
      const response = await updateProfile({
        ...form,
        experienceYears: form.experienceYears ? Number(form.experienceYears) : null,
        noticePeriodDays: form.noticePeriodDays ? Number(form.noticePeriodDays) : null
      });
      updateUser({
        fullName: response.fullName || user?.fullName,
        profileComplete: response.profileComplete,
        resumeUploaded: response.resumeUploaded
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
      if (response.profileComplete && !response.resumeUploaded) {
        navigate('/resume');
      }
    } catch (err) {
      alert('Failed to save profile');
    }
  };

  if (loading) {
    return <div className="loading-screen"><div className="spinner-lg"></div></div>;
  }

  return (
    <div className="profile-wrapper">
      <header className="profile-header">
        <div className="profile-avatar-lg">
          {form.fullName?.charAt(0)?.toUpperCase() || 'U'}
        </div>
        <div className="profile-header-info">
          <h1>{form.fullName || 'Your Profile'}</h1>
          <p>{form.currentRole || 'Add your role'} {form.currentCompany ? `at ${form.currentCompany}` : ''}</p>
        </div>
        <button type="submit" form="profile-form" className="btn-save-profile" disabled={saved}>
          {saved ? <><Check size={18} /> Saved!</> : <><Save size={18} /> Save</>}
        </button>
      </header>

      <div className="profile-body">
        <nav className="profile-nav">
          <button className={`nav-item ${activeSection === 'basic' ? 'active' : ''}`} onClick={() => setActiveSection('basic')}>
            <User size={18} /> Basic Info
          </button>
          <button className={`nav-item ${activeSection === 'work' ? 'active' : ''}`} onClick={() => setActiveSection('work')}>
            <Briefcase size={18} /> Work Experience
          </button>
          <button className={`nav-item ${activeSection === 'preferences' ? 'active' : ''}`} onClick={() => setActiveSection('preferences')}>
            <MapPin size={18} /> Preferences
          </button>
          <button className={`nav-item ${activeSection === 'about' ? 'active' : ''}`} onClick={() => setActiveSection('about')}>
            <GraduationCap size={18} /> Skills & Education
          </button>
          <button className={`nav-item ${activeSection === 'links' ? 'active' : ''}`} onClick={() => setActiveSection('links')}>
            <Link size={18} /> Links
          </button>
        </nav>

        <form id="profile-form" className="profile-form" onSubmit={submit}>
          {activeSection === 'basic' && (
            <div className="form-section-new">
              <h3>Basic Information</h3>
              <div className="form-grid-2">
                <div className="field">
                  <label><User size={14} /> Full Name</label>
                  <input type="text" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="Your full name" />
                </div>
                <div className="field">
                  <label><Phone size={14} /> Phone</label>
                  <input type="tel" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="+91 98765 43210" />
                </div>
                <div className="field">
                  <label><MapPin size={14} /> City</label>
                  <input type="text" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} placeholder="Bangalore" />
                </div>
                <div className="field">
                  <label><Mail size={14} /> Email</label>
                  <input type="email" value={user?.email || ''} disabled placeholder="Email from registration" />
                </div>
              </div>
            </div>
          )}

          {activeSection === 'work' && (
            <div className="form-section-new">
              <h3>Work Experience</h3>
              <div className="form-grid-2">
                <div className="field">
                  <label><Briefcase size={14} /> Current Role</label>
                  <input type="text" value={form.currentRole} onChange={(e) => setForm({ ...form, currentRole: e.target.value })} placeholder="Software Engineer" />
                </div>
                <div className="field">
                  <label>Company</label>
                  <input type="text" value={form.currentCompany} onChange={(e) => setForm({ ...form, currentCompany: e.target.value })} placeholder="Company name" />
                </div>
                <div className="field">
                  <label><Clock size={14} /> Experience (Years)</label>
                  <input type="number" value={form.experienceYears} onChange={(e) => setForm({ ...form, experienceYears: e.target.value })} placeholder="3" min="0" max="50" />
                </div>
                <div className="field">
                  <label><Clock size={14} /> Notice Period (Days)</label>
                  <input type="number" value={form.noticePeriodDays} onChange={(e) => setForm({ ...form, noticePeriodDays: e.target.value })} placeholder="30" min="0" />
                </div>
              </div>
            </div>
          )}

          {activeSection === 'preferences' && (
            <div className="form-section-new">
              <h3>Job Preferences</h3>
              <div className="form-grid-2">
                <div className="field">
                  <label>Preferred Role</label>
                  <input type="text" value={form.preferredRole} onChange={(e) => setForm({ ...form, preferredRole: e.target.value })} placeholder="Full Stack Developer" />
                </div>
                <div className="field">
                  <label><MapPin size={14} /> Preferred Location</label>
                  <input type="text" value={form.preferredLocation} onChange={(e) => setForm({ ...form, preferredLocation: e.target.value })} placeholder="Remote / Bangalore" />
                </div>
                <div className="field">
                  <label><DollarSign size={14} /> Expected Salary</label>
                  <input type="text" value={form.expectedSalary} onChange={(e) => setForm({ ...form, expectedSalary: e.target.value })} placeholder="12 LPA" />
                </div>
              </div>
            </div>
          )}

          {activeSection === 'about' && (
            <div className="form-section-new">
              <h3>About You</h3>
              <div className="form-grid-1">
                <div className="field full">
                  <label>Bio / Summary</label>
                  <textarea rows={4} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} placeholder="A brief professional summary..." />
                </div>
                <div className="field full">
                  <label>Skills (comma separated)</label>
                  <input type="text" value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} placeholder="Java, Spring Boot, React, AWS" />
                </div>
                <div className="field full">
                  <label><GraduationCap size={14} /> Education</label>
                  <input type="text" value={form.education} onChange={(e) => setForm({ ...form, education: e.target.value })} placeholder="B.Tech Computer Science, IIT Delhi" />
                </div>
              </div>
            </div>
          )}

          {activeSection === 'links' && (
            <div className="form-section-new">
              <h3>Professional Links</h3>
              <div className="form-grid-1">
                <div className="field full">
                  <label>LinkedIn URL</label>
                  <input type="url" value={form.linkedinUrl} onChange={(e) => setForm({ ...form, linkedinUrl: e.target.value })} placeholder="linkedin.com/in/yourprofile" />
                </div>
                <div className="field full">
                  <label>GitHub URL</label>
                  <input type="url" value={form.githubUrl} onChange={(e) => setForm({ ...form, githubUrl: e.target.value })} placeholder="github.com/yourusername" />
                </div>
                <div className="field full">
                  <label>Portfolio URL</label>
                  <input type="url" value={form.portfolioUrl} onChange={(e) => setForm({ ...form, portfolioUrl: e.target.value })} placeholder="yourportfolio.com" />
                </div>
              </div>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}
