export type SessionUser = {
  id: string;
  username: string;
  fullName: string;
  roleCode: string;
};

export type SessionBranch = {
  id: string;
  code: string;
  name: string;
};

export type SessionData = {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number | null;
  user: SessionUser;
  branch: SessionBranch | null;
  branchId: string | null;
  permissions: string[];
};
