import { SessionData } from "@/lib/session/types";

const SESSION_STORAGE_KEY = "sassveterinaria.session.v1";

function canUseStorage(): boolean {
  return typeof window !== "undefined" && typeof window.localStorage !== "undefined";
}

function isSessionData(value: unknown): value is SessionData {
  if (!value || typeof value !== "object") {
    return false;
  }

  const candidate = value as Partial<SessionData>;
  return (
    typeof candidate.accessToken === "string" &&
    typeof candidate.refreshToken === "string" &&
    typeof candidate.user?.id === "string" &&
    typeof candidate.user?.username === "string" &&
    typeof candidate.user?.fullName === "string" &&
    typeof candidate.user?.roleCode === "string" &&
    Array.isArray(candidate.permissions)
  );
}

export function readSession(): SessionData | null {
  if (!canUseStorage()) {
    return null;
  }

  const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed: unknown = JSON.parse(raw);
    if (!isSessionData(parsed)) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

export function writeSession(session: SessionData): void {
  if (!canUseStorage()) {
    return;
  }

  // TODO: endurecer storage cuando backend soporte cookie httpOnly.
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

export function setSelectedBranch(branchId: string): SessionData | null {
  const current = readSession();
  if (!current) {
    return null;
  }

  const updated: SessionData = {
    ...current,
    branchId,
  };
  writeSession(updated);
  return updated;
}

export function clearSession(): void {
  if (!canUseStorage()) {
    return;
  }
  window.localStorage.removeItem(SESSION_STORAGE_KEY);
}
