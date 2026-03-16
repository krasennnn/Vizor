const TOKEN_KEY = "auth_token";
const USER_KEY = "auth_user";

/**
 * Get the authentication token from localStorage
 */
export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Get the user data from localStorage
 */
export function getUserFromStorage(): { userId: number; email: string; username: string; roles: string[] } | null {
  const userStr = localStorage.getItem(USER_KEY);
  if (!userStr) return null;
  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
}

/**
 * Save authentication data to localStorage
 */
export function saveAuth(token: string, user: { userId: number; email: string; username: string; roles: string[] }): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

/**
 * Clear authentication data from localStorage
 */
export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

