"use client";

import Link from "next/link";
import { ReactNode } from "react";
import { useRouter } from "next/navigation";
import { logout } from "@/lib/api/auth";
import { hasPermission } from "@/lib/session/permissions";
import { clearSession } from "@/lib/session/store";
import { SessionData } from "@/lib/session/types";

type AppShellProps = {
  session: SessionData;
  activeNav: "home" | "agenda" | "clientes";
  children: ReactNode;
};

type NavItem = {
  key: AppShellProps["activeNav"];
  href: string;
  label: string;
  permission?: string;
};

const NAV_ITEMS: NavItem[] = [
  { key: "home", href: "/", label: "Inicio" },
  { key: "agenda", href: "/agenda", label: "Agenda", permission: "APPT_READ" },
  { key: "clientes", href: "/clientes", label: "Clientes", permission: "CLIENT_READ" },
];

const PLACEHOLDER_ITEMS = [
  "Historia clinica",
  "Facturacion",
  "Inventario",
  "Reportes",
  "Auditoria",
];

export function AppShell({ session, activeNav, children }: AppShellProps) {
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
            {NAV_ITEMS.filter((item) => !item.permission || hasPermission(session, item.permission)).map((item) => (
              <li key={item.key}>
                <Link
                  href={item.href}
                  className={`block rounded-lg border px-3 py-2 ${
                    activeNav === item.key
                      ? "border-slate-900 bg-slate-900 text-white"
                      : "border-slate-200 bg-slate-50 text-slate-700 hover:bg-slate-100"
                  }`}
                >
                  {item.label}
                </Link>
              </li>
            ))}
            {PLACEHOLDER_ITEMS.map((item) => (
              <li
                key={item}
                className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-slate-500"
              >
                {item} (placeholder)
              </li>
            ))}
          </ul>
        </aside>

        <section className="rounded-xl border border-slate-200 bg-white p-6">{children}</section>
      </main>
    </div>
  );
}
