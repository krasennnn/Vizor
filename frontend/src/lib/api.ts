import { getAuthToken } from "@/lib/auth";

/**
 * Global logout handler that will be set by AuthContext
 * This allows apiFetch to trigger logout when token expires
 * The handler receives a boolean indicating if it's an automatic logout due to token expiration
 */
let globalLogoutHandler: ((isAutomatic: boolean) => void) | null = null;

/**
 * Register a logout handler that will be called when token expires
 */
export function setLogoutHandler(handler: (isAutomatic: boolean) => void) {
  globalLogoutHandler = handler;
}

/**
 * Clear the logout handler
 */
export function clearLogoutHandler() {
  globalLogoutHandler = null;
}

/**
 * Helper function to make authenticated API requests
 * Automatically adds Authorization header with JWT token if available
 * Automatically logs out user if 401 (Unauthorized) response is received
 */
export async function apiFetch(
  url: string,
  options: RequestInit = {}
): Promise<Response> {
  const token = getAuthToken();
  const headers = new Headers(options.headers);

  // Set Content-Type if not already set and body is provided
  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  // Add Authorization header if token exists
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  // If we get a 401 Unauthorized, the token has expired or is invalid
  // Automatically log out the user
  if (response.status === 401 && globalLogoutHandler) {
    globalLogoutHandler(true);
  }

  return response;
}

