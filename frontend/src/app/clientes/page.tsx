"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import {
  Client,
  ClientCreateInput,
  ClientPatchInput,
  createClient,
  searchClients,
  updateClient,
} from "@/lib/api/crm";
import { ApiError } from "@/lib/api/client";
import { hasPermission } from "@/lib/session/permissions";
import { readSession } from "@/lib/session/store";
import { SessionData } from "@/lib/session/types";

type ClientFormState = {
  fullName: string;
  identification: string;
  phone: string;
  email: string;
  address: string;
  notes: string;
};

function emptyClientForm(): ClientFormState {
  return {
    fullName: "",
    identification: "",
    phone: "",
    email: "",
    address: "",
    notes: "",
  };
}

function toClientForm(client: Client): ClientFormState {
  return {
    fullName: client.fullName ?? "",
    identification: client.identification ?? "",
    phone: client.phone ?? "",
    email: client.email ?? "",
    address: client.address ?? "",
    notes: client.notes ?? "",
  };
}

function toApiMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "No fue posible completar la operacion.";
}

function formatOptional(value: string): string | undefined {
  const normalized = value.trim();
  return normalized.length > 0 ? normalized : undefined;
}

function validateClientForm(form: ClientFormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const fullName = form.fullName.trim();
  const identification = form.identification.trim();
  const phone = form.phone.trim();
  const email = form.email.trim();

  if (!fullName) {
    errors.fullName = "El nombre es requerido.";
  }

  if (identification && !/^(\d{10}|\d{13})$/.test(identification)) {
    errors.identification = "La identificacion debe tener 10 o 13 digitos.";
  }

  if (phone && !/^[\d+\s()-]+$/.test(phone)) {
    errors.phone = "Telefono invalido. Usa digitos, espacios o +.";
  }

  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.email = "Email invalido.";
  }

  return errors;
}

function toClientCreatePayload(form: ClientFormState): ClientCreateInput {
  return {
    fullName: form.fullName.trim(),
    identification: formatOptional(form.identification),
    phone: formatOptional(form.phone),
    email: formatOptional(form.email),
    address: formatOptional(form.address),
    notes: formatOptional(form.notes),
  };
}

function toClientPatchPayload(form: ClientFormState): ClientPatchInput {
  return {
    fullName: form.fullName.trim(),
    identification: form.identification.trim(),
    phone: form.phone.trim(),
    email: form.email.trim(),
    address: form.address.trim(),
    notes: form.notes.trim(),
  };
}

export default function ClientsPage() {
  const router = useRouter();
  const session = useMemo<SessionData | null>(() => readSession(), []);

  const [searchText, setSearchText] = useState("");
  const [clients, setClients] = useState<Client[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);

  const [clientModalMode, setClientModalMode] = useState<"create" | "edit" | null>(null);
  const [editingClientId, setEditingClientId] = useState<string | null>(null);
  const [form, setForm] = useState<ClientFormState>(emptyClientForm());
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const canClientRead = hasPermission(session, "CLIENT_READ");
  const canClientCreate = hasPermission(session, "CLIENT_CREATE");
  const canClientUpdate = hasPermission(session, "CLIENT_UPDATE");
  const canPetRead = hasPermission(session, "PET_READ");

  const canOpenFicha = canClientRead && canPetRead;

  const loadClients = useCallback(async () => {
    if (!session?.branchId || !canClientRead) {
      return;
    }

    setIsLoading(true);
    setPageError(null);

    try {
      const response = await searchClients(searchText, 0, 20);
      setClients(response.content);
    } catch (error) {
      setPageError(toApiMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, [canClientRead, searchText, session?.branchId]);

  useEffect(() => {
    if (!session?.accessToken) {
      router.replace("/login");
      return;
    }
    if (!session.branchId) {
      router.replace("/select-branch");
    }
  }, [router, session]);

  useEffect(() => {
    void loadClients();
  }, [loadClients]);

  const closeClientModal = () => {
    setClientModalMode(null);
    setEditingClientId(null);
    setForm(emptyClientForm());
    setFormErrors({});
    setSubmitError(null);
  };

  const openCreateModal = () => {
    setClientModalMode("create");
    setEditingClientId(null);
    setForm(emptyClientForm());
    setFormErrors({});
    setSubmitError(null);
  };

  const openEditModal = (client: Client) => {
    setClientModalMode("edit");
    setEditingClientId(client.id);
    setForm(toClientForm(client));
    setFormErrors({});
    setSubmitError(null);
  };

  const onSubmitSearch = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await loadClients();
  };

  const onSubmitClient = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!clientModalMode) {
      return;
    }

    const nextErrors = validateClientForm(form);
    if (Object.keys(nextErrors).length > 0) {
      setFormErrors(nextErrors);
      return;
    }

    setIsSubmitting(true);
    setSubmitError(null);
    setFormErrors({});

    try {
      if (clientModalMode === "create") {
        const created = await createClient(toClientCreatePayload(form));
        closeClientModal();
        await loadClients();
        if (canOpenFicha) {
          router.push(`/clientes/${created.id}`);
        }
      } else {
        if (!editingClientId) {
          throw new Error("No se encontro el cliente a editar.");
        }
        await updateClient(editingClientId, toClientPatchPayload(form));
        closeClientModal();
        await loadClients();
      }
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.validationErrors) {
          setFormErrors(error.validationErrors);
        }
        if (error.errorCode === "CLIENT_IDENTIFICATION_INVALID") {
          setFormErrors((current) => ({
            ...current,
            identification: "La identificacion debe tener 10 o 13 digitos.",
          }));
        }
      }
      setSubmitError(toApiMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

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
    <AppShell session={session} activeNav="clientes">
      <div className="space-y-5">
        <header className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold">Clientes</h2>
            <p className="text-sm text-slate-600">Busqueda y gestion de propietarios.</p>
          </div>
          {canClientCreate ? (
            <button
              type="button"
              onClick={openCreateModal}
              className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
            >
              Nuevo cliente
            </button>
          ) : (
            <span className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-xs text-amber-800">
              Sin permiso CLIENT_CREATE
            </span>
          )}
        </header>

        {!canClientRead ? (
          <div className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            Tu sesion no tiene permiso <code>CLIENT_READ</code>.
          </div>
        ) : (
          <>
            <form onSubmit={onSubmitSearch} className="grid gap-3 md:grid-cols-[1fr_auto]">
              <input
                value={searchText}
                onChange={(event) => setSearchText(event.target.value)}
                placeholder="Buscar por nombre, telefono o identificacion"
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm"
              />
              <button
                type="submit"
                disabled={isLoading}
                className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {isLoading ? "Buscando..." : "Buscar"}
              </button>
            </form>

            {pageError ? (
              <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                {pageError}
              </p>
            ) : null}

            <div className="overflow-x-auto rounded-xl border border-slate-200">
              <table className="min-w-full bg-white text-sm">
                <thead className="bg-slate-50 text-slate-700">
                  <tr>
                    <th className="px-3 py-2 text-left font-semibold">Nombre</th>
                    <th className="px-3 py-2 text-left font-semibold">Telefono</th>
                    <th className="px-3 py-2 text-left font-semibold">Identificacion</th>
                    <th className="px-3 py-2 text-right font-semibold">Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {clients.map((client) => (
                    <tr key={client.id} className="border-t border-slate-200">
                      <td className="px-3 py-2">{client.fullName}</td>
                      <td className="px-3 py-2">{client.phone ?? "N/D"}</td>
                      <td className="px-3 py-2">{client.identification ?? "N/D"}</td>
                      <td className="px-3 py-2">
                        <div className="flex justify-end gap-2">
                          {canOpenFicha ? (
                            <Link
                              href={`/clientes/${client.id}`}
                              className="rounded border border-slate-300 bg-white px-2 py-1 text-xs font-semibold hover:bg-slate-100"
                            >
                              Ver ficha
                            </Link>
                          ) : null}
                          {canClientUpdate ? (
                            <button
                              type="button"
                              onClick={() => openEditModal(client)}
                              className="rounded border border-slate-300 bg-white px-2 py-1 text-xs font-semibold hover:bg-slate-100"
                            >
                              Editar
                            </button>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {!isLoading && clients.length === 0 ? (
                <div className="border-t border-slate-200 px-3 py-5 text-center text-sm text-slate-500">
                  No hay clientes para los filtros actuales.
                </div>
              ) : null}
            </div>
          </>
        )}
      </div>

      {clientModalMode ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
          <div className="w-full max-w-2xl rounded-xl border border-slate-200 bg-white p-5 shadow-lg">
            <h3 className="text-lg font-bold">
              {clientModalMode === "create" ? "Crear cliente" : "Editar cliente"}
            </h3>

            <form onSubmit={onSubmitClient} className="mt-4 space-y-4">
              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Nombre completo</span>
                  <input
                    value={form.fullName}
                    onChange={(event) => setForm((current) => ({ ...current, fullName: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {formErrors.fullName ? <span className="text-xs text-rose-700">{formErrors.fullName}</span> : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Identificacion (opcional)</span>
                  <input
                    value={form.identification}
                    onChange={(event) => setForm((current) => ({ ...current, identification: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {formErrors.identification ? (
                    <span className="text-xs text-rose-700">{formErrors.identification}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Telefono</span>
                  <input
                    value={form.phone}
                    onChange={(event) => setForm((current) => ({ ...current, phone: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {formErrors.phone ? <span className="text-xs text-rose-700">{formErrors.phone}</span> : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Email</span>
                  <input
                    type="email"
                    value={form.email}
                    onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {formErrors.email ? <span className="text-xs text-rose-700">{formErrors.email}</span> : null}
                </label>
              </div>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Direccion</span>
                <input
                  value={form.address}
                  onChange={(event) => setForm((current) => ({ ...current, address: event.target.value }))}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Notas</span>
                <textarea
                  value={form.notes}
                  onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                  className="min-h-[100px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              {submitError ? (
                <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {submitError}
                </p>
              ) : null}

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={closeClientModal}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmitting
                    ? "Guardando..."
                    : clientModalMode === "create"
                      ? "Crear cliente"
                      : "Guardar cambios"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </AppShell>
  );
}
