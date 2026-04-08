const TOKEN_KEY = 'tm_access_token';
const USER_KEY = 'tm_user';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function saveSession(authResponse) {
  localStorage.setItem(TOKEN_KEY, authResponse.accessToken);
  localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

async function parseError(response) {
  try {
    const payload = await response.json();
    return payload.message || payload.error || 'Request failed';
  } catch {
    return `Request failed (${response.status})`;
  }
}

export async function apiFetch(path, options = {}) {
  const { auth = true, headers: customHeaders, ...restOptions } = options;
  const headers = {
    'Content-Type': 'application/json',
    ...(customHeaders || {})
  };

  const token = getToken();
  if (auth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(path, { ...restOptions, headers });
  if (!response.ok) {
    throw new Error(await parseError(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export async function login(username, password) {
  return apiFetch('/api/auth/login', {
    method: 'POST',
    auth: false,
    body: JSON.stringify({ username, password })
  });
}

export async function register(username, email, password) {
  return apiFetch('/api/auth/register', {
    method: 'POST',
    auth: false,
    body: JSON.stringify({ username, email, password })
  });
}
