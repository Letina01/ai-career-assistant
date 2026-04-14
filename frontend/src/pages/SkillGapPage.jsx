import { useEffect, useState } from 'react';
import { analyzeSkillGap, fetchResumes } from '../api';
import SectionCard from '../components/SectionCard';

function RoadmapRenderer({ roadmap }) {
  if (!roadmap || roadmap.length === 0) {
    return <p className="muted">No roadmap available.</p>;
  }

  return (
    <div className="roadmap-container">
      {roadmap.map((item, index) => {
        // Phase header
        if (item.startsWith('[PHASE]')) {
          return (
            <div key={`phase-${index}`} className="roadmap-phase">
              <h4>{item.replace('[PHASE]', '')}</h4>
              <ul>
                {roadmap.slice(index + 1).map((step, i) => {
                  if (step.startsWith('[PHASE]')) return null;
                  if (!step.startsWith('[STEP]')) return null;
                  return <li key={`step-${index}-${i}`}>{step.replace('[STEP]', '')}</li>;
                }).filter(Boolean)}
              </ul>
            </div>
          );
        }
        return null;
      })}
    </div>
  );
}

export default function SkillGapPage() {
  const [resumes, setResumes] = useState([]);
  const [form, setForm] = useState({ resumeId: '', targetRole: 'Java Developer' });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchResumes().then((data) => {
      setResumes(data);
      if (data[0]) {
        setForm((prev) => ({ ...prev, resumeId: data[0].resumeId }));
      }
    }).catch(() => {});
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    if (!form.resumeId) {
      setError('Please select a resume first');
      return;
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const response = await analyzeSkillGap(form);
      setResult(response);
    } catch (err) {
      setError('Failed to analyze skill gap. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-grid">
      <SectionCard title="Skill Gap Analyzer" subtitle="Compare your skills against a target role">
        <form className="form" onSubmit={submit}>
          <select 
            value={form.resumeId} 
            onChange={(e) => setForm({ ...form, resumeId: e.target.value })}
            required
          >
            <option value="">Select resume</option>
            {resumes.map((resume) => (
              <option key={resume.resumeId} value={resume.resumeId}>
                {resume.candidateName}
              </option>
            ))}
          </select>
          
          <input 
            value={form.targetRole} 
            onChange={(e) => setForm({ ...form, targetRole: e.target.value })} 
            placeholder="Target role (e.g., Java Developer)"
            required
          />
          
          <button type="submit" disabled={loading}>
            {loading ? 'Analyzing...' : 'Analyze Gap'}
          </button>
        </form>
        
        {error && <p className="error-text">{error}</p>}
      </SectionCard>

      {result && (
        <>
          <SectionCard title="Your Current Skills" subtitle="Skills detected from your resume">
            {result.currentSkills && result.currentSkills.length > 0 ? (
              <div className="skills-chips">
                {result.currentSkills.map((skill, index) => (
                  <span key={`current-${index}`} className="chip">
                    {skill}
                  </span>
                ))}
              </div>
            ) : (
              <p className="muted">No skills detected from resume. Upload a resume first.</p>
            )}
          </SectionCard>

          <SectionCard title="Missing Skills" subtitle="Skills you need for this role">
            {result.missingSkills && result.missingSkills.length > 0 ? (
              <div className="skills-chips missing">
                {result.missingSkills.map((skill, index) => (
                  <span key={`missing-${index}`} className="chip missing-skill">
                    {skill}
                  </span>
                ))}
              </div>
            ) : (
              <p className="success-text">✓ You have all required skills!</p>
            )}
          </SectionCard>

          <SectionCard title="Learning Roadmap" subtitle="Follow this plan to bridge the gap">
            <RoadmapRenderer roadmap={result.roadmap} />
          </SectionCard>
        </>
      )}
    </div>
  );
}
