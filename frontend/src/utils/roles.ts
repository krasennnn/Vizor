/**
 * Utility functions for role checking
 */

export function hasRole(user: { roles: string[] } | null | undefined, role: string): boolean {
  return user?.roles?.includes(role) ?? false;
}

export function isCreator(user: { roles: string[] } | null | undefined): boolean {
  return hasRole(user, "CREATOR");
}

export function isOwner(user: { roles: string[] } | null | undefined): boolean {
  return hasRole(user, "OWNER");
}

