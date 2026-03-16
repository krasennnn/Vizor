import type { VideoResponse, VideoSyncRequest } from "../types/Video";
import type { VideoAnalyticsResponse, VideoDailyAnalyticsResponse } from "../types/VideoAnalytics";
import { apiFetch } from "../lib/api";

const BASE_URL = "/api/videos";

export async function getVideo(id: number): Promise<VideoResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}`);

  if (!res.ok) {
    let message = "Failed to fetch video.";

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

export async function getVideosByAccount(accountId: number): Promise<VideoResponse[]> {
  const res = await apiFetch(`${BASE_URL}/account/${accountId}`);

  if (!res.ok) {
    // If 404, return empty array instead of error (account just doesn't have videos yet)
    if (res.status === 404) {
      return [];
    }

    let message = "Failed to fetch videos.";

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

export async function getVideosByContract(contractId: number): Promise<VideoResponse[]> {
  const res = await apiFetch(`${BASE_URL}/contract/${contractId}`);

  if (!res.ok) {
    let message = "Failed to fetch videos.";

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

export async function getVideosByCampaign(campaignId: number): Promise<VideoResponse[]> {
  const res = await apiFetch(`${BASE_URL}/campaign/${campaignId}`);

  if (!res.ok) {
    let message = "Failed to fetch videos.";

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

export async function syncVideo(request: VideoSyncRequest): Promise<VideoResponse> {
  const res = await apiFetch(`${BASE_URL}/sync`, {
    method: "POST",
    body: JSON.stringify(request),
  });

  if (!res.ok) {
    let message = "Failed to sync video.";

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

  const video = await res.json();
  window.showAppAlert?.("success", "Success", "Video synced successfully");
  return video;
}

export async function deleteVideo(id: number): Promise<void> {
  const res = await apiFetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    let message = "Failed to delete video.";

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

  window.showAppAlert?.("success", "Success", "Video deleted successfully");
}

export async function getVideoAnalytics(videoId: number): Promise<VideoAnalyticsResponse[]> {
  const res = await apiFetch(`${BASE_URL}/${videoId}/analytics`);

  if (!res.ok) {
    let message = "Failed to fetch video analytics.";

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

export async function getContractAnalytics(contractId: number): Promise<VideoDailyAnalyticsResponse[]> {
  const res = await apiFetch(`${BASE_URL}/contract/${contractId}/analytics`);

  if (!res.ok) {
    let message = "Failed to fetch contract analytics.";

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

export async function getCampaignAnalytics(campaignId: number): Promise<VideoDailyAnalyticsResponse[]> {
  const res = await apiFetch(`${BASE_URL}/campaign/${campaignId}/analytics`);

  if (!res.ok) {
    // Return empty array if 404 (no analytics yet)
    if (res.status === 404) {
      return [];
    }

    let message = "Failed to fetch campaign analytics.";

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

