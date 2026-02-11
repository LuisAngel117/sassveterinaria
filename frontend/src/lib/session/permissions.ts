import { SessionData } from "@/lib/session/types";

export function hasPermission(
  session: SessionData | null,
  permission: string,
): boolean {
  if (!session) {
    return false;
  }
  return session.permissions.includes(permission);
}
