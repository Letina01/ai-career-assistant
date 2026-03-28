import { useEffect, useState } from 'react';
import { fetchJobRecommendations, fetchProfile, uploadResume } from '../api';
import SectionCard from '../components/SectionCard';
import FileUpload from '../components/FileUpload';
import { BarChart3, CheckCircle, AlertTriangle, Lightbulb } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export default function ResumePage() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({ candidateName: '', email: '', file: null });
  const [profilePreferences, setProfilePreferences] = useState({ preferredRole: 'Software Engineer', preferredLocation: 'India' });
  const [result, setResult] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [fileError, setFileError] = useState(null);

  useEffect(() => {
    fetchProfile().then((profile) => {
      setForm((current) => ({
        ...current,
        candidateName: profile.fullName || user?.fullName || '',
        email: profile.email || user?.email || ''
      }));
      setProfilePreferences({
        preferredRole: profile.preferredRole || 'Software Engineer',
        preferredLocation: profile.preferredLocation || 'India'
      });
    }).catch(() => {});
  }, [user?.email, user?.fullName]);

  const handleFileSelect = (file, err) => {
    setFileError(err);
    setForm({ ...form, file });
  };

  const submit = async (event) => {
    event.preventDefault();
    if (!form.file) {
      setError('Please select a resume file');
      return;
    }
    if (!form.candidateName || !form.email) {
      setError('Please fill in all fields');
      return;
    }

    setError(null);
    setLoading(true);
    try {
      const data = new FormData();
      data.append('candidateName', form.candidateName);
      data.append('email', form.email);
      data.append('file', form.file);
      const response = await uploadResume(data);
      setResult(response);
      updateUser({ resumeUploaded: true });
      const jobs = await fetchJobRecommendations(
        response.resumeId,
        profilePreferences.preferredRole,
        profilePreferences.preferredLocation
      );
      setRecommendations(jobs.slice(0, 5));
      setForm((current) => ({ ...current, file: null }));
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to analyze resume. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const getAtsColor = (score) => {
    if (score >= 75) return '#059669';
    if (score >= 50) return '#d97706';
    return '#dc2626';
  };

  return (
    <div className="applicant-home">
      <div className="hero-panel">
        <div className="hero-content">
          <p className="eyebrow accent-eyebrow">Resume Analysis</p>
          <h2>Upload & Analyze Your Resume</h2>
          <p className="hero-copy">
            Get AI-powered insights including ATS score, skills extraction, missing competencies, and actionable improvement suggestions.
          </p>
        </div>
        <div className="hero-side-card">
          <strong>What you'll get</strong>
          <div className="feature-mini">
            <span>✓ ATS Score</span>
            <span>✓ Skill Extraction</span>
            <span>✓ Gap Analysis</span>
            <span>✓ Suggestions</span>
          </div>
        </div>
      </div>

      <div className="page-grid">
        <SectionCard title="Upload Your Resume" subtitle="Support PDF, DOC, DOCX files up to 10MB">
          <form className="form" onSubmit={submit}>
            <input
              placeholder="Your full name"
              value={form.candidateName}
              onChange={(e) => setForm({ ...form, candidateName: e.target.value })}
              required
            />
            <input
              type="email"
              placeholder="Your email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />

            <FileUpload
              onFileSelect={handleFileSelect}
              accept=".pdf,.doc,.docx"
              maxSizeMB={10}
              loading={loading}
              error={fileError}
              label="Choose Resume File"
            />

            {error && <p className="error-text">{error}</p>}
            <button type="submit" disabled={loading || !form.file}>
              {loading ? 'Analyzing Resume...' : 'Analyze Resume'}
            </button>
          </form>
        </SectionCard>

        {result && (
          <div>
            <SectionCard title="Analysis Results" subtitle="AI-generated insights for your resume">
              <div className="analysis-result">
                <div className="ats-score-section">
                  <div className="ats-header">
                    <BarChart3 size={24} color={getAtsColor(result.atsScore)} />
                    <div>
                      <p className="ats-label">ATS Score</p>
                      <p className="ats-value" style={{ color: getAtsColor(result.atsScore) }}>
                        {result.atsScore}%
                      </p>
                    </div>
                  </div>
                  <div className="ats-progress">
                    <div className="progress-bar">
                      <div
                        className="progress-fill"
                        style={{
                          width: `${result.atsScore}%`,
                          backgroundColor: getAtsColor(result.atsScore)
                        }}
                      />
                    </div>
                  </div>
                </div>

                <div className="skills-section">
                  <h3>
                    <CheckCircle size={20} /> Extracted Skills
                  </h3>
                  <div className="chip-row">
                    {result.extractedSkills.map((skill) => (
                      <span key={skill} className="chip">
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="skills-section">
                  <h3>
                    <AlertTriangle size={20} /> Missing Skills
                  </h3>
                  {result.missingSkills.length > 0 ? (
                    <div className="chip-row">
                      {result.missingSkills.map((skill) => (
                        <span key={skill} className="chip missing-skill">
                          {skill}
                        </span>
                      ))}
                    </div>
                  ) : (
                    <p className="muted">Great! No critical missing skills detected.</p>
                  )}
                </div>

                <div className="suggestions-section">
                  <h3>
                    <Lightbulb size={20} /> Improvement Suggestions
                  </h3>
                  <ul className="suggestions-list">
                    {result.suggestions.map((item, idx) => (
                      <li key={idx}>{item}</li>
                    ))}
                  </ul>
                </div>

                <div className="preview-section">
                  <h3>Extracted Text Preview</h3>
                  <pre className="text-preview">{result.extractedText.slice(0, 1500)}</pre>
                </div>

                <div className="skills-section">
                  <h3>Recommended Jobs (Based on Preferences)</h3>
                  {recommendations.length > 0 ? (
                    <ul className="suggestions-list">
                      {recommendations.map((job, idx) => (
                        <li key={`${job.title}-${idx}`}>
                          {job.title} at {job.company} ({job.matchScore}% match) -{' '}
                          <a href={job.applyLink} target="_blank" rel="noreferrer">Apply</a>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="muted">No recommendations available yet.</p>
                  )}
                </div>
              </div>
            </SectionCard>
          </div>
        )}
      </div>
    </div>
  );
}
