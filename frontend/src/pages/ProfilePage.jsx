import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchProfile, updateProfile } from '../api';
import SectionCard from '../components/SectionCard';
import { useAuth } from '../context/AuthContext';

const emptyProfile = {
  fullName: '',
  phone: '',
  city: '',
  preferredRole: '',
  preferredLocation: '',
  currentRole: '',
  currentCompany: '',
  experienceYears: '',
  noticePeriodDays: '',
  expectedSalary: '',
  bio: '',
  skills: '',
  education: '',
  linkedinUrl: '',
  githubUrl: '',
  portfolioUrl: ''
};

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState(emptyProfile);
  const [saved, setSaved] = useState(false);

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
    }).catch(() => {});
  }, []);

  const submit = async (event) => {
    event.preventDefault();
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
  };

  return (
    <div className="page-grid">
      <SectionCard title="Applicant Profile" subtitle="Build a complete profile like a real hiring platform">
        <form className="form profile-grid" onSubmit={submit}>
          <input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="Full name" />
          <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="Phone number" />
          <input value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} placeholder="Current city" />
          <input value={form.preferredRole} onChange={(e) => setForm({ ...form, preferredRole: e.target.value })} placeholder="Preferred role" />
          <input value={form.preferredLocation} onChange={(e) => setForm({ ...form, preferredLocation: e.target.value })} placeholder="Preferred location" />
          <input value={form.currentRole} onChange={(e) => setForm({ ...form, currentRole: e.target.value })} placeholder="Current role" />
          <input value={form.currentCompany} onChange={(e) => setForm({ ...form, currentCompany: e.target.value })} placeholder="Current company" />
          <input value={form.experienceYears} onChange={(e) => setForm({ ...form, experienceYears: e.target.value })} placeholder="Years of experience" />
          <input value={form.noticePeriodDays} onChange={(e) => setForm({ ...form, noticePeriodDays: e.target.value })} placeholder="Notice period in days" />
          <input value={form.expectedSalary} onChange={(e) => setForm({ ...form, expectedSalary: e.target.value })} placeholder="Expected salary" />
          <textarea rows={4} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} placeholder="Profile summary" />
          <textarea rows={3} value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} placeholder="Skills, tools, certifications" />
          <textarea rows={3} value={form.education} onChange={(e) => setForm({ ...form, education: e.target.value })} placeholder="Education and training" />
          <input value={form.linkedinUrl} onChange={(e) => setForm({ ...form, linkedinUrl: e.target.value })} placeholder="LinkedIn URL" />
          <input value={form.githubUrl} onChange={(e) => setForm({ ...form, githubUrl: e.target.value })} placeholder="GitHub URL" />
          <input value={form.portfolioUrl} onChange={(e) => setForm({ ...form, portfolioUrl: e.target.value })} placeholder="Portfolio URL" />
          <button type="submit">Save Profile</button>
        </form>
        {saved && <p className="success-text">Profile updated.</p>}
      </SectionCard>
    </div>
  );
}
