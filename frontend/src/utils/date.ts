/**
 * Formats a date string to a readable format
 * @param dateString - ISO date string
 * @returns Formatted date string (e.g., "Jan 15, 2024")
 */
export function formatDate(dateString: string): string {
  return new Date(dateString).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  })
}


