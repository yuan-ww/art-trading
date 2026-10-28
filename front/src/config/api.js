const configuredBaseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:10202';

export const API_BASE_URL = configuredBaseUrl.replace(/\/$/, '');

export function apiUrl(path = '') {
  return `${API_BASE_URL}${path}`;
}
