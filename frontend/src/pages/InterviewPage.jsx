import { useEffect, useState } from 'react';
import { fetchResumes, fetchProfile, generateInterviewPrep } from '../api';
import SectionCard from '../components/SectionCard';

export default function InterviewPage() {
  const [resumes, setResumes] = useState([]);
  const [form, setForm] = useState({ resumeId: '', targetRole: '', focusArea: '' });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([fetchResumes(), fetchProfile()])
      .then(([resumeData, profile]) => {
        setResumes(resumeData);
        setForm({
          resumeId: resumeData[0]?.resumeId || '',
          targetRole: profile?.preferredRole || profile?.currentRole || '',
          focusArea: 'Role fundamentals, technical skills, system design, and behavioral scenarios'
        });
      })
      .catch(() => {});
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    if (!form.resumeId) {
      setError('Please select a resume.');
      return;
    }
    if (!form.targetRole.trim() || !form.focusArea.trim()) {
      setError('Please enter target role and focus area.');
      return;
    }
    setLoading(true);
    try {
      const response = await generateInterviewPrep(form);
      setResult(response);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to generate interview preparation.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-grid">
      <SectionCard title="Interview Preparation" subtitle="Resume-based interview prep with Q&A and roadmap">
        <form className="form" onSubmit={submit}>
          <select 
            value={form.resumeId} 
            onChange={(e) => setForm({ ...form, resumeId: e.target.value })}
            className="form-input"
          >
            <option value="">Select your resume</option>
            {resumes.map((resume) => (
              <option key={resume.resumeId} value={resume.resumeId}>
                {resume.candidateName}
              </option>
            ))}
          </select>
          <input 
            value={form.targetRole} 
            onChange={(e) => setForm({ ...form, targetRole: e.target.value })} 
            placeholder="Target role (e.g., Senior Software Engineer, Product Manager)"
          />
          <input 
            value={form.focusArea} 
            onChange={(e) => setForm({ ...form, focusArea: e.target.value })} 
            placeholder="Interview focus areas (e.g., system design, behavioral, technical)"
          />
          {error && <p className="error-text">{error}</p>}
          <button type="submit" disabled={loading || !form.resumeId}>
            {loading ? 'Generating...' : 'Generate Interview Prep'}
          </button>
        </form>
      </SectionCard>

      <SectionCard title="Interview Preparation Output" subtitle="Personalized Q&A and 6-week learning roadmap">
        {result ? (
          <div className="stack">
            <div>
              <h3>Interview Questions & Answers for {result.role}</h3>
              <div className="qa-list">
                {result.questionAnswers.map((item, idx) => (
                  <div key={idx} className="qa-item">
                    <details>
                      <summary className="question">
                        <strong>Q{idx + 1}: {item.question}</strong>
                      </summary>
                      <p className="answer">{item.answer}</p>
                    </details>
                  </div>
                ))}
              </div>
            </div>
            <div>
              <h3>6-Week Learning Roadmap</h3>
              <ol className="roadmap">
                {result.roadmap.map((step, idx) => (
                  <li key={idx}>
                    <strong>Week {idx + 1}:</strong> {step}
                  </li>
                ))}
              </ol>
            </div>
          </div>
        ) : (
          <p className="muted">💡 Select a resume and enter your target role to generate personalized interview preparation.</p>
        )}
      </SectionCard>

      <style>{`
        .qa-list {
          display: flex;
          flex-direction: column;
          gap: 12px;
        }
        .qa-item {
          border-left: 4px solid #6366f1;
          padding-left: 12px;
        }
        .question {
          cursor: pointer;
          padding: 8px 0;
          color: #1f2937;
          font-weight: 600;
        }
        .question:hover {
          color: #6366f1;
        }
        .answer {
          margin-top: 8px;
          padding: 12px;
          background-color: #f3f4f6;
          border-radius: 6px;
          line-height: 1.6;
          color: #374151;
        }
        .roadmap {
          padding-left: 20px;
          gap: 12px;
          display: flex;
          flex-direction: column;
        }
        .roadmap li {
          padding: 10px;
          background-color: #f0f9ff;
          border-radius: 6px;
          border-left: 4px solid #0284c7;
        }
      `}</style>
    </div>
  );
}
