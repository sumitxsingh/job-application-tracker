const BASE_URL = 'http://localhost:8080';

export const getToken = () => localStorage.getItem('token');

export const setSession = (data) => {
  localStorage.setItem('token', data.token);
  localStorage.setItem('name', data.fullName);
};

export const clearSession = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('name');
};

async function request(path, options = {}) {
  const token = getToken();
  const isAuthPath = path.startsWith('/api/auth');

  const res = await fetch(BASE_URL + path, {
    method: options.method || 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(token && !isAuthPath ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  if ((res.status === 401 || res.status === 403) && token && !isAuthPath) {
    clearSession();
    window.location.reload();
    throw new Error('Session expired, please log in again');
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error(data?.message || `Request failed (${res.status})`);
  return data;
}

export const api = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body }),
  patch: (path, body) => request(path, { method: 'PATCH', body }),
};