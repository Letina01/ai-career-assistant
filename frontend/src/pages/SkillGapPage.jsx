import { useEffect, useState } from 'react';
import { analyzeSkillGap, fetchResumes } from '../api';
import SectionCard from '../components/SectionCard';

export default function SkillGapPage() {
  const [resumes, setResumes] = useState([]);
  const [form, setForm] = useState({ resumeId: '', targetRole: 'Senior Java Developer' });
  const [result, setResult] = useState(null);

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
    const response = await analyzeSkillGap(form);
    setResult(response);
  };

  return (
    <div className="page-grid">
      <SectionCard title="Skill Gap Analyzer" subtitle="Compare resume skills against a target role">
        <form className="form" onSubmit={submit}>
          <select value={form.resumeId} onChange={(e) => setForm({ ...form, resumeId: e.target.value })}>
            <option value="">Select resume</option>
            {resumes.map((resume) => <option key={resume.resumeId} value={resume.resumeId}>{resume.candidateName}</option>)}
          </select>
          <input value={form.targetRole} onChange={(e) => setForm({ ...form, targetRole: e.target.value })} placeholder="Target role" />
          <button type="submit">Analyze Gap</button>
        </form>
      </SectionCard>

      <SectionCard title="Learning Roadmap" subtitle="Current skills, missing skills, and next steps">
        {result ? (
          <div className="stack">
            <p><strong>Current Skills:</strong> {result.currentSkills.join(', ')}</p>
            <p><strong>Missing Skills:</strong> {result.missingSkills.join(', ')}</p>
            <ul>
              {result.roadmap.map((item) => <li key={item}>{item}</li>)}
            </ul>
          </div>
        ) : <p className="muted">Run the analyzer after uploading a resume.</p>}
      </SectionCard>
    </div>
  );
}
