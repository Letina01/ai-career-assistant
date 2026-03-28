import { useState } from 'react';
import { improveResume } from '../api';
import SectionCard from '../components/SectionCard';

export default function ResumeImprovePage() {
  const [form, setForm] = useState({
    resumeId: '',
    sectionName: 'Professional Summary',
    sectionContent: 'Worked on backend APIs and responsible for deployments.'
  });
  const [result, setResult] = useState(null);

  const submit = async (event) => {
    event.preventDefault();
    const response = await improveResume(form);
    setResult(response);
  };

  return (
    <div className="page-grid">
      <SectionCard title="Resume Improvement Generator" subtitle="Rewrite weak sections with stronger language">
        <form className="form" onSubmit={submit}>
          <input value={form.sectionName} onChange={(e) => setForm({ ...form, sectionName: e.target.value })} placeholder="Section name" />
          <textarea rows={8} value={form.sectionContent} onChange={(e) => setForm({ ...form, sectionContent: e.target.value })} placeholder="Section content" />
          <button type="submit">Rewrite Section</button>
        </form>
      </SectionCard>

      <SectionCard title="Improved Output" subtitle="Original content and AI-enhanced version">
        {result ? (
          <div className="stack">
            <p><strong>Original:</strong> {result.originalContent}</p>
            <p><strong>Improved:</strong> {result.improvedContent}</p>
          </div>
        ) : <p className="muted">Enter a resume section to rewrite it.</p>}
      </SectionCard>
    </div>
  );
}
