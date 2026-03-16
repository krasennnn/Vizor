import type { LoginRequest, RegisterRequest, JwtResponse } from "../types/Auth";
import { apiFetch } from "../lib/api";

const BASE_URL = "/api/auth";

export async function login(credentials: LoginRequest): Promise<JwtResponse> {
  const res = await apiFetch(`${BASE_URL}/login`, {
    method: "POST",
    body: JSON.stringify(credentials),
  });

  if (!res.ok) {
    let message = "Failed to login.";
    try {
      const contentType = res.headers.get("content-type");
      const data = contentType?.includes("application/json")
        ? await res.json()
        : { message: await res.text() };

      message = data.message?.trim()
        ? data.message
        : data.error?.trim()
        ? data.error
        : res.statusText || "Unknown error";
    } catch (err) {
      message = "Unknown error occurred";
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  return res.json();
}

export async function register(userData: RegisterRequest): Promise<void> {
  const res = await apiFetch(`${BASE_URL}/register`, {
    method: "POST",
    body: JSON.stringify(userData),
  });

  if (!res.ok) {
    let message = "Failed to register.";
    try {
      const contentType = res.headers.get("content-type");
      const data = contentType?.includes("application/json")
        ? await res.json()
        : { message: await res.text() };

      message = data.message?.trim()
        ? data.message
        : data.error?.trim()
        ? data.error
        : res.statusText || "Unknown error";
    } catch (err) {
      message = "Unknown error occurred";
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }
}

