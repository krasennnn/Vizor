import type { ContractResponse, CreateContractRequest, UpdateContractRequest } from "../types/Contract";
import { apiFetch } from "../lib/api";

const BASE_URL = "/api/contracts";

export async function getContracts(): Promise<ContractResponse[]> {
  const res = await apiFetch(BASE_URL);

  if (!res.ok) {
    let message = "Failed to fetch contracts.";

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

export async function getContract(id: number): Promise<ContractResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}`);

  if (!res.ok) {
    let message = "Failed to fetch contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  return res.json();
}

export async function getContractsByCreator(creatorId: number): Promise<ContractResponse[]> {
  const res = await apiFetch(`${BASE_URL}/creator/${creatorId}`);

  if (!res.ok) {
    let message = "Failed to fetch contracts by creator.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  return res.json();
}

export async function getContractsByCampaign(campaignId: number): Promise<ContractResponse[]> {
  const res = await apiFetch(`${BASE_URL}/campaign/${campaignId}`);

  if (!res.ok) {
    let message = "Failed to fetch contracts by campaign.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  return res.json();
}

export async function createContract(
  contract: CreateContractRequest,
  isOwner: boolean = false
): Promise<ContractResponse> {
  const params = new URLSearchParams({
    isOwner: String(isOwner),
  });

  const res = await apiFetch(`${BASE_URL}?${params}`, {
    method: "POST",
    body: JSON.stringify(contract),
  });

  if (!res.ok) {
    let message = "Failed to create contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  window.showAppAlert?.("success", "Contract created successfully");
  return res.json();
}

export async function updateContract(
  id: number,
  contract: UpdateContractRequest
): Promise<ContractResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}`, {
    method: "PATCH",
    body: JSON.stringify(contract),
  });

  if (!res.ok) {
    let message = "Failed to update contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  window.showAppAlert?.("success", "Contract updated successfully");
  return res.json();
}

export async function approveContract(id: number): Promise<ContractResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}/approve`, {
    method: "POST",
  });

  if (!res.ok) {
    let message = "Failed to approve contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  window.showAppAlert?.("success", "Contract approved successfully");
  return res.json();
}

export async function rejectContract(id: number): Promise<ContractResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}/reject`, {
    method: "POST",
  });

  if (!res.ok) {
    let message = "Failed to reject contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  window.showAppAlert?.("success", "Contract rejected successfully");
  return res.json();
}

export async function completeContract(id: number): Promise<ContractResponse> {
  const res = await apiFetch(`${BASE_URL}/${id}/complete`, {
    method: "POST",
  });

  if (!res.ok) {
    let message = "Failed to complete contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  window.showAppAlert?.("success", "Contract completed successfully");
  return res.json();
}

export async function deleteContract(id: number): Promise<void> {
  const res = await apiFetch(`${BASE_URL}/${id}`, {
    method: "DELETE",
  });

  if (!res.ok) {
    let message = "Failed to delete contract.";
    try {
      const data = await res.json();
      message = data.message || data.error || JSON.stringify(data);
    } catch {
      message = await res.text();
    }

    window.showAppAlert?.("error", `Error ${res.status}`, message);
    throw new Error(message);
  }

  window.showAppAlert?.("success", "Contract deleted successfully");
}

