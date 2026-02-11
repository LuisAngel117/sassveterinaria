"use client";

import { useRouter } from "next/navigation";
import { logout } from "@/lib/api/auth";
import { clearSession } from "@/lib/session/store";
import { SessionData } from "@/lib/session/types";

type AppShellProps = {
  session: SessionData;
};

const PLACEHOLDER_MODULES = [
  "Agenda",
  "Clientes",
  "Mascotas",
  "Historia clinica",
  "Facturacion",
  "Inventario",
  "Reportes",
  "Auditoria",
];

export function AppShell({ session }: AppShellProps) {
  const router = useRouter();

  const onLogout = async () => {
    try {
      if (session.refreshToken) {
        await logout(session.refreshToken);
      }
    } catch {
      // Ignorado: se limpia sesion local aunque logout remoto falle.
    } finally {
      clearSession();
      router.replace("/login");
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 text-slate-900">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">
              SaaSVeterinaria
            </p>
            <h1 className="text-xl font-bold">Shell Frontend</h1>
          </div>
          <button
            type="button"
            onClick={onLogout}
            className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-slate-50"
          >
            Cerrar sesion
          </button>
        </div>
      </header>

      <main className="mx-auto grid w-full max-w-6xl gap-6 px-6 py-6 md:grid-cols-[260px_1fr]">
        <aside className="rounded-xl border border-slate-200 bg-white p-4">
          <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-500">
            Navegacion
          </p>
          <ul className="space-y-2 text-sm">
            {PLACEHOLDER_MODULES.map((item) => (
              <li
                key={item}
                className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-slate-700"
              >
                {item} (placeholder)
              </li>
            ))}
          </ul>
        </aside>

        <section className="rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="text-xl font-bold">Home</h2>
          <p className="mt-4 text-sm text-slate-600">
            Sesion activa:{" "}
            <span className="font-semibold text-slate-900">{session.user.username}</span>
          </p>
          <p className="mt-1 text-sm text-slate-600">
            Nombre:{" "}
            <span className="font-semibold text-slate-900">{session.user.fullName}</span>
          </p>
          <p className="mt-1 text-sm text-slate-600">
            Rol:{" "}
            <span className="font-semibold text-slate-900">{session.user.roleCode}</span>
          </p>
          <p className="mt-1 text-sm text-slate-600">
            Sucursal:{" "}
            <span className="font-semibold text-slate-900">
              {session.branch?.name ?? session.branchId ?? "N/D"}
            </span>
          </p>
          <p className="mt-1 text-sm text-slate-600">
            BranchId:{" "}
            <span className="font-mono text-xs text-slate-900">
              {session.branchId ?? "N/D"}
            </span>
          </p>

          <div className="mt-6">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
              Permisos en sesion
            </p>
            <div className="mt-3 flex flex-wrap gap-2">
              {session.permissions.map((permission) => (
                <span
                  key={permission}
                  className="rounded-md border border-slate-200 bg-slate-50 px-2 py-1 font-mono text-xs"
                >
                  {permission}
                </span>
              ))}
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
