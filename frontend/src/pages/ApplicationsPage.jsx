import { useEffect, useState } from 'react';
import { fetchMyApplications } from '../api';
import SectionCard from '../components/SectionCard';

export default function ApplicationsPage() {
  const [applications, setApplications] = useState([]);

  useEffect(() => {
    fetchMyApplications().then(setApplications).catch(() => {});
  }, []);

  return (
    <div className="page-grid single">
      <SectionCard title="My Applications" subtitle="Applicant view of applied jobs and recruiter decisions">
        <div className="list">
          {applications.map((item) => (
            <div key={item.applicationId} className="list-item">
              <div>
                <strong>{item.jobTitle}</strong>
                <p>{item.company}</p>
                <p>Status: {item.status}</p>
                <p>ATS used: {item.atsScore ?? 'N/A'}</p>
              </div>
            </div>
          ))}
          {!applications.length && <p className="muted">No applications yet.</p>}
        </div>
      </SectionCard>
    </div>
  );
}
