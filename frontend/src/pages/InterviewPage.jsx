import { useEffect, useState } from 'react';
import { fetchProfile, generateInterviewPrep } from '../api';
import SectionCard from '../components/SectionCard';

export default function InterviewPage() {
  const [form, setForm] = useState({ role: '', focusArea: '' });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchProfile().then((profile) => {
      setForm({
        role: profile?.preferredRole || profile?.currentRole || '',
        focusArea: 'Role fundamentals, project deep dive, and practical scenarios'
      });
    }).catch(() => {});
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    if (!form.role.trim() || !form.focusArea.trim()) {
      setError('Please enter role and focus area.');
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
      <SectionCard title="Interview Preparation" subtitle="Questions, answers, and roadmap">
        <form className="form" onSubmit={submit}>
          <input value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })} placeholder="Target role" />
          <input value={form.focusArea} onChange={(e) => setForm({ ...form, focusArea: e.target.value })} placeholder="Focus area" />
          {error && <p className="error-text">{error}</p>}
          <button type="submit" disabled={loading}>{loading ? 'Generating...' : 'Generate Prep'}</button>
        </form>
      </SectionCard>

      <SectionCard title="Preparation Output" subtitle="Role-specific interview content">
        {result ? (
          <div className="stack">
            <div>
              <strong>Questions & Answers</strong>
              <ul>
                {result.questionAnswers.map((item) => (
                  <li key={item.question}>
                    <strong>{item.question}</strong>: {item.answer}
                  </li>
                ))}
              </ul>
            </div>
            <div>
              <strong>Roadmap</strong>
              <ul>
                {result.roadmap.map((item) => <li key={item}>{item}</li>)}
              </ul>
            </div>
          </div>
        ) : <p className="muted">Generate a role-specific interview plan.</p>}
      </SectionCard>
    </div>
  );
}
