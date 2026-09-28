import { useCallback, useEffect, useState } from 'react';
import { api } from './api';

const STATUSES = ['APPLIED', 'SCREENING', 'INTERVIEWING', 'OFFERED', 'REJECTED', 'WITHDRAWN'];
const INTERVIEW_TYPES = ['PHONE_SCREEN', 'TECHNICAL', 'SYSTEM_DESIGN', 'BEHAVIORAL', 'ONSITE', 'FINAL'];

const fmt = (iso) => (iso ? new Date(iso).toLocaleString() : '-');

function StatusBadge({ status }) {
  return <span className={`badge ${status}`}>{status}</span>;
}

function CreateForm({ onCreated, onCancel }) {
  const [form, setForm] = useState({ companyName: '', jobTitle: '', source: '', appliedDate: '' });
  const [error, setError] = useState('');

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const body = { ...form };
      if (!body.appliedDate) delete body.appliedDate;
      if (!body.source) delete body.source;
      await api.post('/api/applications', body);
      onCreated();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <form className="card" onSubmit={submit}>
      <h3>New application</h3>
      <div className="row">
        <input name="companyName" placeholder="Company" value={form.companyName} onChange={change} required />
        <input name="jobTitle" placeholder="Job title" value={form.jobTitle} onChange={change} required />
        <input name="source" placeholder="Source (LinkedIn, referral...)" value={form.source} onChange={change} />
        <input name="appliedDate" type="date" value={form.appliedDate} onChange={change} />
      </div>
      {error && <div className="error">{error}</div>}
      <div className="row" style={{ marginTop: 10 }}>
        <button className="btn">Save</button>
        <button type="button" className="btn secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  );
}

function Detail({ id, onClose, onChanged }) {
  const [app, setApp] = useState(null);
  const [interviews, setInterviews] = useState([]);
  const [error, setError] = useState('');
  const [statusForm, setStatusForm] = useState({ status: 'SCREENING', note: '' });
  const [iv, setIv] = useState({ type: 'PHONE_SCREEN', round: 1, scheduledAt: '', interviewerName: '' });

  const reload = useCallback(async () => {
    try {
      const [a, i] = await Promise.all([
        api.get(`/api/applications/${id}`),
        api.get(`/api/applications/${id}/interviews`),
      ]);
      setApp(a);
      setInterviews(i);
    } catch (err) {
      setError(err.message);
    }
  }, [id]);

  useEffect(() => {
    reload();
  }, [reload]);

  const changeStatus = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.patch(`/api/applications/${id}/status`, statusForm);
      setStatusForm({ ...statusForm, note: '' });
      await reload();
      onChanged();
    } catch (err) {
      setError(err.message);
    }
  };

  const schedule = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post(`/api/applications/${id}/interviews`, {
        ...iv,
        round: Number(iv.round),
        scheduledAt: new Date(iv.scheduledAt).toISOString(),
      });
      setIv({ ...iv, scheduledAt: '', interviewerName: '' });
      await reload();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="modal-bg" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        {!app ? (
          <p className="muted">{error || 'Loading...'}</p>
        ) : (
          <>
            <div className="row" style={{ justifyContent: 'space-between' }}>
              <h2 style={{ margin: 0 }}>{app.companyName} — {app.jobTitle}</h2>
              <button className="btn secondary" onClick={onClose}>Close</button>
            </div>
            <p><StatusBadge status={app.status} /> <span className="muted">via {app.source || 'n/a'}</span></p>
            {error && <div className="error">{error}</div>}

            <h3>Timeline</h3>
            <ul className="timeline">
              {app.stageHistory.map((h, i) => (
                <li key={i}>
                  <StatusBadge status={h.status} /> <span className="muted">{fmt(h.changedAt)}</span>
                  {h.note && <div>{h.note}</div>}
                </li>
              ))}
            </ul>

            <h3>Change status</h3>
            <form className="row" onSubmit={changeStatus}>
              <select value={statusForm.status} onChange={(e) => setStatusForm({ ...statusForm, status: e.target.value })}>
                {STATUSES.map((s) => <option key={s}>{s}</option>)}
              </select>
              <input
                placeholder="Note (optional)"
                value={statusForm.note}
                onChange={(e) => setStatusForm({ ...statusForm, note: e.target.value })}
              />
              <button className="btn">Update</button>
            </form>

            <h3>Interviews</h3>
            {interviews.length === 0 && <p className="muted">No interviews scheduled.</p>}
            <ul className="timeline">
              {interviews.map((i) => (
                <li key={i.id}>
                  <b>{i.type}</b> (round {i.round}) — {fmt(i.scheduledAt)}
                  {i.interviewerName && <span className="muted"> with {i.interviewerName}</span>}
                </li>
              ))}
            </ul>
            <form className="row" onSubmit={schedule}>
              <select value={iv.type} onChange={(e) => setIv({ ...iv, type: e.target.value })}>
                {INTERVIEW_TYPES.map((t) => <option key={t}>{t}</option>)}
              </select>
              <input
                type="number"
                min="1"
                style={{ width: 70 }}
                value={iv.round}
                onChange={(e) => setIv({ ...iv, round: e.target.value })}
              />
              <input
                type="datetime-local"
                required
                value={iv.scheduledAt}
                onChange={(e) => setIv({ ...iv, scheduledAt: e.target.value })}
              />
              <input
                placeholder="Interviewer"
                value={iv.interviewerName}
                onChange={(e) => setIv({ ...iv, interviewerName: e.target.value })}
              />
              <button className="btn">Schedule</button>
            </form>
          </>
        )}
      </div>
    </div>
  );
}

export default function Applications() {
  const [filters, setFilters] = useState({ status: '', company: '' });
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [selectedId, setSelectedId] = useState(null);

  const load = useCallback(async () => {
    const params = new URLSearchParams({ page, size: 8 });
    if (filters.status) params.set('status', filters.status);
    if (filters.company) params.set('company', filters.company);
    try {
      setData(await api.get(`/api/applications?${params}`));
      setError('');
    } catch (err) {
      setError(err.message);
    }
  }, [filters, page]);

  useEffect(() => {
    load();
  }, [load]);

  const updateFilter = (patch) => {
    setFilters({ ...filters, ...patch });
    setPage(0);
  };

  return (
    <>
      <div className="row" style={{ marginBottom: 16 }}>
        <select value={filters.status} onChange={(e) => updateFilter({ status: e.target.value })}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
        <input
          placeholder="Search company..."
          value={filters.company}
          onChange={(e) => updateFilter({ company: e.target.value })}
        />
        <div style={{ flex: 1 }} />
        <button className="btn" onClick={() => setShowForm(true)}>+ New application</button>
      </div>

      {showForm && (
        <CreateForm
          onCancel={() => setShowForm(false)}
          onCreated={() => {
            setShowForm(false);
            load();
          }}
        />
      )}

      {error && <p className="error">{error}</p>}

      <div className="card">
        {!data ? (
          <p className="muted">Loading...</p>
        ) : data.content.length === 0 ? (
          <p className="muted">No applications found.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Company</th><th>Title</th><th>Status</th><th>Applied</th><th>Updated</th></tr>
            </thead>
            <tbody>
              {data.content.map((a) => (
                <tr key={a.id} className="clickable" onClick={() => setSelectedId(a.id)}>
                  <td>{a.companyName}</td>
                  <td>{a.jobTitle}</td>
                  <td><StatusBadge status={a.status} /></td>
                  <td>{a.appliedDate || '-'}</td>
                  <td>{fmt(a.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {data && data.totalPages > 1 && (
          <div className="pager">
            <button className="btn secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
            <span className="muted">Page {data.page + 1} of {data.totalPages}</span>
            <button className="btn secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>
              Next
            </button>
          </div>
        )}
      </div>

      {selectedId && (
        <Detail id={selectedId} onClose={() => setSelectedId(null)} onChanged={load} />
      )}
    </>
  );
}