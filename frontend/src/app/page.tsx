"use client";

import { useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { readSession } from "@/lib/session/store";

export default function HomePage() {
  const router = useRouter();
  const session = useMemo(() => readSession(), []);

  useEffect(() => {
    if (!session?.accessToken) {
      router.replace("/login");
      return;
    }
    if (!session.branchId) {
      router.replace("/select-branch");
      return;
    }
  }, [router, session]);

  if (!session?.accessToken || !session.branchId) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-100 px-6 py-12">
        <div className="rounded-xl border border-slate-200 bg-white px-5 py-3 text-sm text-slate-600">
          Validando sesion...
        </div>
      </main>
    );
  }

  return (
    <AppShell session={session} activeNav="home">
      <h2 className="text-xl font-bold">Home</h2>
      <p className="mt-4 text-sm text-slate-600">
        Sesion activa:{" "}
        <span className="font-semibold text-slate-900">{session.user.username}</span>
      </p>
      <p className="mt-1 text-sm text-slate-600">
        Nombre: <span className="font-semibold text-slate-900">{session.user.fullName}</span>
      </p>
      <p className="mt-1 text-sm text-slate-600">
        Rol: <span className="font-semibold text-slate-900">{session.user.roleCode}</span>
      </p>
      <p className="mt-1 text-sm text-slate-600">
        Sucursal:{" "}
        <span className="font-semibold text-slate-900">
          {session.branch?.name ?? session.branchId ?? "N/D"}
        </span>
      </p>
      <p className="mt-1 text-sm text-slate-600">
        BranchId:{" "}
        <span className="font-mono text-xs text-slate-900">{session.branchId ?? "N/D"}</span>
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
    </AppShell>
  );
}
