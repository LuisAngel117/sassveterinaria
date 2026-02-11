export type DemoCredential = {
  username: string;
  password: string;
  roleLabel: string;
};

// Fuente: docs/08-runbook.md (seccion "Usuarios demo seed").
export const DEMO_CREDENTIALS: DemoCredential[] = [
  { username: "superadmin", password: "SuperAdmin123!", roleLabel: "SUPERADMIN" },
  { username: "admin", password: "Admin123!", roleLabel: "ADMIN" },
  { username: "recepcion", password: "Recepcion123!", roleLabel: "RECEPCION" },
  { username: "veterinario", password: "Veterinario123!", roleLabel: "VETERINARIO" },
];
