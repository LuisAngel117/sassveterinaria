"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError } from "@/lib/api/client";
import { getMe, login } from "@/lib/api/auth";
import { DEMO_CREDENTIALS } from "@/lib/demo-credentials";
import { readSession, writeSession } from "@/lib/session/store";
import { SessionData } from "@/lib/session/types";

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "No fue posible iniciar sesion.";
}

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const demoHint = useMemo(
    () =>
      "Credenciales demo obtenidas desde docs/08-runbook.md (usuarios seed del backend).",
    [],
  );

  useEffect(() => {
    const session = readSession();
    if (!session?.accessToken) {
      return;
    }
    if (session.branchId) {
      router.replace("/");
      return;
    }
    router.replace("/select-branch");
  }, [router]);

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const loginResponse = await login(username.trim(), password);

      if (loginResponse.challengeRequired) {
        throw new Error(
          "Tu usuario requiere challenge 2FA. El flujo 2FA se implementa en SPR-F007.",
        );
      }
      if (!loginResponse.accessToken || !loginResponse.refreshToken) {
        throw new Error("Respuesta de login invalida: faltan tokens.");
      }
      if (!loginResponse.branch?.id) {
        throw new Error("Respuesta de login invalida: falta branch.id.");
      }

      const me = await getMe(loginResponse.accessToken, loginResponse.branch.id);
      const selectedBranchId = me.branchId ?? loginResponse.branch.id;

      const session: SessionData = {
        accessToken: loginResponse.accessToken,
        refreshToken: loginResponse.refreshToken,
        expiresInSeconds: loginResponse.expiresInSeconds ?? null,
        user: {
          id: me.id,
          username: me.username,
          fullName: me.fullName,
          roleCode: me.roleCode,
        },
        branch: loginResponse.branch,
        branchId: selectedBranchId,
        permissions: Array.isArray(me.permissions) ? me.permissions : [],
      };

      writeSession(session);

      if (!selectedBranchId) {
        router.replace("/select-branch");
        return;
      }
      router.replace("/");
    } catch (error: unknown) {
      setErrorMessage(toErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="min-h-screen bg-slate-100 px-6 py-10">
      <section className="mx-auto grid w-full max-w-5xl gap-6 md:grid-cols-[1.2fr_1fr]">
        <article className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">
            SaaSVeterinaria
          </p>
          <h1 className="mt-4 text-3xl font-bold tracking-tight">Iniciar sesion</h1>
          <p className="mt-3 text-sm text-slate-600">
            Login conectado al backend real: <code>/api/v1/auth/login</code> y{" "}
            <code>/api/v1/me</code>.
          </p>

          <form onSubmit={onSubmit} className="mt-8 space-y-5">
            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-slate-700">
                Usuario
              </label>
              <input
                id="username"
                name="username"
                autoComplete="username"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                className="mt-2 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none ring-slate-400 focus:ring"
                placeholder="recepcion"
                required
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-slate-700">
                Contrasena
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                className="mt-2 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none ring-slate-400 focus:ring"
                placeholder="********"
                required
              />
            </div>

            {errorMessage ? (
              <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                {errorMessage}
              </div>
            ) : null}

            <button
              type="submit"
              disabled={isSubmitting}
              className="inline-flex items-center rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? "Ingresando..." : "Entrar"}
            </button>
          </form>
        </article>

        <aside className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-bold">Credenciales demo</h2>
          <p className="mt-2 text-xs text-slate-500">{demoHint}</p>
          <div className="mt-4 space-y-3">
            {DEMO_CREDENTIALS.map((credential) => (
              <button
                key={credential.username}
                type="button"
                onClick={() => {
                  setUsername(credential.username);
                  setPassword(credential.password);
                }}
                className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-3 text-left text-sm hover:bg-slate-100"
              >
                <p className="font-semibold text-slate-900">{credential.roleLabel}</p>
                <p className="mt-1 text-xs text-slate-600">
                  {credential.username} / {credential.password}
                </p>
              </button>
            ))}
          </div>
        </aside>
      </section>
    </main>
  );
}
