import { useState } from 'react';
import { getToken, clearSession } from './api';
import Auth from './Auth';
import Dashboard from './Dashboard';
import Applications from './Applications';

export default function App() {
  const [loggedIn, setLoggedIn] = useState(!!getToken());
  const [tab, setTab] = useState('applications');

  if (!loggedIn) return <Auth onLogin={() => setLoggedIn(true)} />;

  const logout = () => {
    clearSession();
    setLoggedIn(false);
  };

  return (
    <div className="container">
      <header className="topbar">
        <h1>Job Tracker</h1>
        <nav>
          <button className={tab === 'applications' ? 'tab active' : 'tab'} onClick={() => setTab('applications')}>
            Applications
          </button>
          <button className={tab === 'dashboard' ? 'tab active' : 'tab'} onClick={() => setTab('dashboard')}>
            Dashboard
          </button>
        </nav>
        <div className="user">
          <span>{localStorage.getItem('name')}</span>
          <button className="btn secondary" onClick={logout}>Logout</button>
        </div>
      </header>
      {tab === 'applications' ? <Applications /> : <Dashboard />}
    </div>
  );
}