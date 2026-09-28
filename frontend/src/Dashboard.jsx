import { useEffect, useState } from 'react';
import { api } from './api';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/dashboard/stats').then(setStats).catch((e) => setError(e.message));
  }, []);

  if (error) return <p className="error">{error}</p>;
  if (!stats) return <p className="muted">Loading...</p>;

  const total = stats.totalApplications || 1;

  return (
    <>
      <div className="stats">
        <div className="card stat"><span className="muted">Total applications</span><b>{stats.totalApplications}</b></div>
        <div className="card stat"><span className="muted">Interview rate</span><b>{stats.interviewRate}%</b></div>
        <div className="card stat"><span className="muted">Offer rate</span><b>{stats.offerRate}%</b></div>
        <div className="card stat"><span className="muted">Stale (14+ days)</span><b>{stats.staleApplications}</b></div>
      </div>
      <div className="card">
        <h3>By status</h3>
        {Object.keys(stats.countByStatus).length === 0 && <p className="muted">No applications yet.</p>}
        {Object.entries(stats.countByStatus).map(([status, count]) => (
          <div key={status} style={{ marginBottom: 10 }}>
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <span>{status}</span>
              <span>{count}</span>
            </div>
            <div className="bar"><div style={{ width: `${(count / total) * 100}%` }} /></div>
          </div>
        ))}
      </div>
    </>
  );
}