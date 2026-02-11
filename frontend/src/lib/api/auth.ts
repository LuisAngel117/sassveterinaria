import { apiRequest } from "@/lib/api/client";

export type LoginUserPayload = {
  id: string;
  username: string;
  fullName: string;
  roleCode: string;
};

export type LoginBranchPayload = {
  id: string;
  code: string;
  name: string;
};

export type LoginResponse = {
  accessToken: string | null;
  refreshToken: string | null;
  expiresInSeconds: number | null;
  user: LoginUserPayload | null;
  branch: LoginBranchPayload | null;
  challengeRequired: boolean | null;
  challengeToken: string | null;
  challengeExpiresInSeconds: number | null;
  requiresTotpSetup: boolean | null;
  message: string | null;
};

export type MeResponse = {
  id: string;
  username: string;
  fullName: string;
  roleCode: string;
  branchId: string;
  permissions: string[];
};

export async function login(username: string, password: string): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: { username, password },
  });
}

export async function getMe(
  accessToken: string,
  branchId: string,
): Promise<MeResponse> {
  return apiRequest<MeResponse>("/api/v1/me", {
    method: "GET",
    accessToken,
    branchId,
  });
}

export async function logout(refreshToken: string): Promise<void> {
  await apiRequest<void>("/api/v1/auth/logout", {
    method: "POST",
    body: { refreshToken },
  });
}
