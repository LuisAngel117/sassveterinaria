"use client";

import { useEffect, useMemo } from "react";
import { useRouter } from "next/navigation";
import { clearSession, readSession, setSelectedBranch } from "@/lib/session/store";

type BranchOption = {
  id: string;
  code: string;
  name: string;
};

function getBranchOption(): BranchOption | null {
  const session = readSession();
  if (!session) {
    return null;
  }

  if (session.branch && session.branch.id) {
    return {
      id: session.branch.id,
      code: session.branch.code,
      name: session.branch.name,
    };
  }

  if (session.branchId) {
    return {
      id: session.branchId,
      code: "N/D",
      name: `Sucursal ${session.branchId}`,
    };
  }

  return null;
}

export default function SelectBranchPage() {
  const router = useRouter();
  const session = useMemo(() => readSession(), []);
  const branchOption = useMemo(() => getBranchOption(), []);
  const errorMessage =
    session?.accessToken && !session.branchId && !branchOption
      ? "No fue posible resolver una sucursal valida desde la sesion actual. Inicia sesion nuevamente."
      : null;

  useEffect(() => {
    if (!session?.accessToken) {
      router.replace("/login");
      return;
    }

    if (session.branchId) {
      router.replace("/");
      return;
    }
  }, [router, session]);

  const onSelectBranch = () => {
    if (!branchOption) {
      return;
    }
    setSelectedBranch(branchOption.id);
    router.replace("/");
  };

  return (
    <main className="min-h-screen bg-slate-100 px-6 py-12">
      <section className="mx-auto w-full max-w-2xl rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
        <h1 className="text-2xl font-bold">Seleccion de sucursal</h1>
        <p className="mt-3 text-sm text-slate-600">
          Este backend devuelve una sucursal por defecto en login. Si existe una sola, se
          confirma aqui para habilitar <code>X-Branch-Id</code> en el cliente API.
        </p>

        {errorMessage ? (
          <div className="mt-6 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
            {errorMessage}
          </div>
        ) : null}

        {branchOption ? (
          <div className="mt-6 rounded-lg border border-slate-200 bg-slate-50 p-4">
            <p className="text-sm font-semibold text-slate-900">{branchOption.name}</p>
            <p className="mt-1 text-xs text-slate-600">Codigo: {branchOption.code}</p>
            <p className="mt-1 font-mono text-xs text-slate-600">{branchOption.id}</p>
          </div>
        ) : null}

        <div className="mt-6 flex gap-3">
          <button
            type="button"
            onClick={onSelectBranch}
            disabled={!branchOption}
            className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            Continuar
          </button>
          <button
            type="button"
            onClick={() => {
              clearSession();
              router.replace("/login");
            }}
            className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-50"
          >
            Volver a login
          </button>
        </div>
      </section>
    </main>
  );
}
