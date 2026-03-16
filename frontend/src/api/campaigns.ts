import type { CampaignResponse, CreateCampaignRequest, UpdateCampaignRequest } from "../types/Campaign";
import { apiFetch } from "../lib/api";

const BASE_URL = "/api/campaigns";

export async function getCampaigns(): Promise<CampaignResponse[]> {
  const res = await fetch(BASE_URL)

  if (!res.ok) {
    let message = "Failed to fetch campaigns."

    try {
      // Read body *once*
      const contentType = res.headers.get("content-type")
      const data = contentType?.includes("application/json")
        ? await res.json()
        : { message: await res.text() }

      message = message = data.message?.trim()
        ? data.message
        : data.error?.trim()
        ? data.error
        : res.statusText || "Unknown error"

    } catch (err) {
      message = "Unknown error occurred"
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message)
    throw new Error(message)
  }

  return res.json()
}

export async function getCampaignsByOwner(): Promise<CampaignResponse[]> {
  const res = await apiFetch(`${BASE_URL}/owner`)

  if (!res.ok) {
    let message = "Failed to fetch campaigns."

    try {
      const contentType = res.headers.get("content-type")
      const data = contentType?.includes("application/json")
        ? await res.json()
        : { message: await res.text() }

      message = data.message?.trim()
        ? data.message
        : data.error?.trim()
        ? data.error
        : res.statusText || "Unknown error"

    } catch (err) {
      message = "Unknown error occurred"
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message)
    throw new Error(message)
  }

  return res.json()
}

export async function getCampaignsByCreator(): Promise<CampaignResponse[]> {
  const res = await apiFetch(`${BASE_URL}/creator`)

  if (!res.ok) {
    let message = "Failed to fetch campaigns."

    try {
      const contentType = res.headers.get("content-type")
      const data = contentType?.includes("application/json")
        ? await res.json()
        : { message: await res.text() }

      message = data.message?.trim()
        ? data.message
        : data.error?.trim()
        ? data.error
        : res.statusText || "Unknown error"

    } catch (err) {
      message = "Unknown error occurred"
    }

    // Don't show error for empty list - just return empty array
    if (res.status === 404 || message.includes("not have any")) {
      return []
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message)
    throw new Error(message)
  }

  return res.json()
}

export async function createCampaign(campaign: CreateCampaignRequest): Promise<CampaignResponse> {
  const res = await apiFetch(BASE_URL, {
    method: "POST",
    body: JSON.stringify(campaign),
  })

  if (!res.ok) {
    let message = "Failed to create campaign."
    try {
      const data = await res.json()
      message = data.message || data.error || JSON.stringify(data)
    } catch {
      message = await res.text()
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message)
    throw new Error(message)
  }

  // Success message handled here
  window.showAppAlert?.("success", "Campaign created successfully")
  return res.json()
}

export async function updateCampaign(id: number, campaign: UpdateCampaignRequest): Promise<CampaignResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}`, {
    method: "PATCH",
    body: JSON.stringify(campaign),
  })

  if (!res.ok) {
    let message = "Failed to update campaign."
    try {
      const data = await res.json()
      message = data.message || data.error || JSON.stringify(data)
    } catch {
      message = await res.text()
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message)
    throw new Error(message)
  }

  window.showAppAlert?.("success", "Campaign updated successfully")
  return res.json()
}

export async function deleteCampaign(id: number): Promise<void> {
  const res = await apiFetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
  })

  if (!res.ok) {
    let message = "Failed to delete campaign."
    try {
      const data = await res.json()
      message = data.message || data.error || JSON.stringify(data)
    } catch {
      message = await res.text()
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message)
    throw new Error(message)
  }

  window.showAppAlert?.("success", "Campaign deleted successfully")
}