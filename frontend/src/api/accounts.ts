import type { AccountResponse, AccountSyncRequest } from "../types/Account";
import { apiFetch } from "../lib/api";

const BASE_URL = "/api/accounts";

export async function getAccounts(): Promise<AccountResponse[]> {
  const res = await apiFetch(`${BASE_URL}/creator`);

  if (!res.ok) {
    // If 404, return empty array instead of error (user just doesn't have accounts yet)
    if (res.status === 404) {
      return [];
    }

    let message = "Failed to fetch accounts.";

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

  // Handle empty response gracefully
  const text = await res.text();
  if (!text || text.trim() === "") {
    return [];
  }

  try {
    return JSON.parse(text);
  } catch (err) {
    // If JSON parse fails but we got a 200, return empty array
    return [];
  }
}

export async function getActiveAccounts(): Promise<AccountResponse[]> {
  const res = await apiFetch(`${BASE_URL}/creator/active`);

  if (!res.ok) {
    let message = "Failed to fetch active accounts.";

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

export async function getAccount(id: number): Promise<AccountResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}`);

  if (!res.ok) {
    let message = "Failed to fetch account.";

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

export async function syncAccount(request: AccountSyncRequest): Promise<AccountResponse> {
  const res = await apiFetch(`${BASE_URL}/sync`, {
    method: "POST",
    body: JSON.stringify(request),
  });

  if (!res.ok) {
    let message = "Failed to sync account.";

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

  const account = await res.json();
  window.showAppAlert?.("success", "Success", "Account synced successfully");
  return account;
}

export async function deleteAccount(id: number): Promise<void> {
  const res = await apiFetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    let message = "Failed to delete account.";

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

  window.showAppAlert?.("success", "Success", "Account deleted successfully");
}

