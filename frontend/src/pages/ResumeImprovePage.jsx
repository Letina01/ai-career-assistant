import { useEffect, useState } from 'react';
import { fetchResumes, rewriteResume, downloadResumeFile } from '../api';
import SectionCard from '../components/SectionCard';

export default function ResumeImprovePage() {
  const [resumes, setResumes] = useState([]);
  const [form, setForm] = useState({
    resumeId: '',
    improvementInstructions: 'Add measurable achievements with quantified metrics. Improve action verbs and emphasize impact. Optimize for ATS with strong keywords.'
  });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    fetchResumes()
      .then((data) => {
        setResumes(data);
        if (data[0]) {
          setForm((prev) => ({ ...prev, resumeId: data[0].resumeId }));
        }
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
    
    if (!form.improvementInstructions.trim()) {
      setError('Please enter what you want to improve.');
      return;
    }

    setLoading(true);
    try {
      const response = await rewriteResume(form);
      setResult(response);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to rewrite resume.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (format) => {
    if (!result) return;
    
    setDownloading(true);
    try {
      const { blob, fileName } = await downloadResumeFile(
        result.improvedResume,
        result.candidateName,
        format
      );
      
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      alert('Failed to download resume. Please try again.');
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="page-grid">
      <SectionCard title="Resume Rewriter" subtitle="AI-powered resume enhancement with downloadable output">
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
          
          <label>
            <strong>What would you like to improve?</strong>
            <textarea 
              rows={6}
              value={form.improvementInstructions} 
              onChange={(e) => setForm({ ...form, improvementInstructions: e.target.value })}
              placeholder="Example: Add quantified achievements, improve action verbs, optimize for ATS with keywords, emphasize leadership experience..."
              className="form-textarea"
            />
          </label>

          {error && <p className="error-text">{error}</p>}
          
          <button type="submit" disabled={loading || !form.resumeId}>
            {loading ? 'Rewriting resume...' : 'Rewrite Resume'}
          </button>
        </form>
      </SectionCard>

      <SectionCard title="Rewritten Resume" subtitle="Download in PDF or Word format">
        {result ? (
          <div className="resume-output">
            <div className="resume-preview">
              <h3>{result.candidateName}</h3>
              <p className="stats">
                Estimated ATS Improvement: <strong>+{result.estimatedAtsImprovement}%</strong>
              </p>
              <div className="resume-content">
                {result.improvedResume.split('\n').map((line, idx) => (
                  line.trim() ? (
                    <p key={idx} className={line.toUpperCase() === line ? 'section-header' : ''}>
                      {line}
                    </p>
                  ) : (
                    <br key={idx} />
                  )
                ))}
              </div>
            </div>

            <div className="download-buttons">
              <button 
                onClick={() => handleDownload('pdf')}
                disabled={downloading}
                className="download-btn pdf"
              >
                {downloading ? 'Downloading...' : '📄 Download as PDF'}
              </button>
              <button 
                onClick={() => handleDownload('docx')}
                disabled={downloading}
                className="download-btn docx"
              >
                {downloading ? 'Downloading...' : '📝 Download as Word'}
              </button>
            </div>
          </div>
        ) : (
          <p className="muted">💡 Select a resume and specify improvements to generate your rewritten resume.</p>
        )}
      </SectionCard>

      <style>{`
        .form-textarea {
          width: 100%;
          padding: 10px;
          border: 1px solid #e5e7eb;
          border-radius: 6px;
          font-family: inherit;
          font-size: 14px;
          resize: vertical;
        }

        .resume-output {
          display: flex;
          flex-direction: column;
          gap: 20px;
        }

        .resume-preview {
          background: white;
          border: 1px solid #e5e7eb;
          border-radius: 8px;
          padding: 20px;
          max-height: 500px;
          overflow-y: auto;
        }

        .resume-preview h3 {
          text-align: center;
          margin: 0 0 5px 0;
          font-size: 18px;
        }

        .stats {
          text-align: center;
          color: #6366f1;
          font-size: 14px;
          margin: 0 0 15px 0;
        }

        .resume-content {
          font-size: 13px;
          line-height: 1.6;
          color: #374151;
        }

        .resume-content p {
          margin: 8px 0;
        }

        .section-header {
          font-weight: 600;
          color: #1f2937;
          margin-top: 12px !important;
          margin-bottom: 6px !important;
        }

        .download-buttons {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .download-btn {
          flex: 1;
          min-width: 150px;
          padding: 12px 16px;
          border: none;
          border-radius: 6px;
          font-weight: 600;
          cursor: pointer;
          transition: all 0.3s ease;
          font-size: 14px;
        }

        .download-btn.pdf {
          background: linear-gradient(135deg, #ff4757 0%, #ff6348 100%);
          color: white;
        }

        .download-btn.pdf:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4);
        }

        .download-btn.docx {
          background: linear-gradient(135deg, #0284c7 0%, #0369a1 100%);
          color: white;
        }

        .download-btn.docx:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(2, 132, 199, 0.4);
        }

        .download-btn:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        @media (max-width: 600px) {
          .download-btn {
            min-width: 100%;
          }
        }
      `}</style>
    </div>
  );
}
