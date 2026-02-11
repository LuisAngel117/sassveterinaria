"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import {
  Client,
  ClientPatchInput,
  Pet,
  PetCreateInput,
  PetPatchInput,
  createPet,
  getClient,
  getPet,
  listClientPets,
  updateClient,
  updatePet,
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

type PetFormState = {
  internalCode: string;
  name: string;
  species: string;
  breed: string;
  sex: string;
  birthDate: string;
  weightKg: string;
  neutered: "null" | "true" | "false";
  alerts: string;
  history: string;
};

function toApiMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "No fue posible completar la operacion.";
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

function emptyPetForm(): PetFormState {
  return {
    internalCode: "",
    name: "",
    species: "",
    breed: "",
    sex: "",
    birthDate: "",
    weightKg: "",
    neutered: "null",
    alerts: "",
    history: "",
  };
}

function toPetForm(pet: Pet): PetFormState {
  return {
    internalCode: pet.internalCode ?? "",
    name: pet.name ?? "",
    species: pet.species ?? "",
    breed: pet.breed ?? "",
    sex: pet.sex ?? "",
    birthDate: pet.birthDate ?? "",
    weightKg: pet.weightKg == null ? "" : String(pet.weightKg),
    neutered: pet.neutered == null ? "null" : pet.neutered ? "true" : "false",
    alerts: pet.alerts ?? "",
    history: pet.history ?? "",
  };
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

function validatePetForm(form: PetFormState): Record<string, string> {
  const errors: Record<string, string> = {};

  if (!form.internalCode.trim()) {
    errors.internalCode = "El codigo interno es requerido.";
  }
  if (!form.name.trim()) {
    errors.name = "El nombre de la mascota es requerido.";
  }
  if (!form.species.trim()) {
    errors.species = "La especie es requerida.";
  }

  if (form.weightKg.trim()) {
    const parsed = Number(form.weightKg);
    if (Number.isNaN(parsed) || parsed <= 0) {
      errors.weightKg = "El peso debe ser un numero mayor que 0.";
    }
  }

  return errors;
}

function buildClientPatchPayload(form: ClientFormState): ClientPatchInput {
  return {
    fullName: form.fullName.trim(),
    identification: form.identification.trim(),
    phone: form.phone.trim(),
    email: form.email.trim(),
    address: form.address.trim(),
    notes: form.notes.trim(),
  };
}

function buildPetPayload(form: PetFormState): PetCreateInput | PetPatchInput {
  return {
    internalCode: form.internalCode.trim(),
    name: form.name.trim(),
    species: form.species.trim(),
    breed: form.breed.trim(),
    sex: form.sex.trim(),
    birthDate: form.birthDate.trim() || undefined,
    weightKg: form.weightKg.trim() ? Number(form.weightKg) : undefined,
    neutered:
      form.neutered === "null"
        ? undefined
        : form.neutered === "true"
          ? true
          : false,
    alerts: form.alerts.trim(),
    history: form.history.trim(),
  };
}

export default function ClientDetailPage() {
  const router = useRouter();
  const params = useParams<{ clientId: string }>();
  const clientId = typeof params?.clientId === "string" ? params.clientId : "";
  const session = useMemo<SessionData | null>(() => readSession(), []);

  const [client, setClient] = useState<Client | null>(null);
  const [pets, setPets] = useState<Pet[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);

  const [isClientModalOpen, setIsClientModalOpen] = useState(false);
  const [clientForm, setClientForm] = useState<ClientFormState>({
    fullName: "",
    identification: "",
    phone: "",
    email: "",
    address: "",
    notes: "",
  });
  const [clientFormErrors, setClientFormErrors] = useState<Record<string, string>>({});
  const [clientSubmitError, setClientSubmitError] = useState<string | null>(null);
  const [isSubmittingClient, setIsSubmittingClient] = useState(false);

  const [petModalMode, setPetModalMode] = useState<"create" | "edit" | null>(null);
  const [editingPetId, setEditingPetId] = useState<string | null>(null);
  const [petForm, setPetForm] = useState<PetFormState>(emptyPetForm());
  const [petFormErrors, setPetFormErrors] = useState<Record<string, string>>({});
  const [petSubmitError, setPetSubmitError] = useState<string | null>(null);
  const [isSubmittingPet, setIsSubmittingPet] = useState(false);

  const canClientRead = hasPermission(session, "CLIENT_READ");
  const canClientUpdate = hasPermission(session, "CLIENT_UPDATE");
  const canPetRead = hasPermission(session, "PET_READ");
  const canPetCreate = hasPermission(session, "PET_CREATE");
  const canPetUpdate = hasPermission(session, "PET_UPDATE");

  const loadData = useCallback(async () => {
    if (!session?.branchId || !clientId || !canClientRead) {
      return;
    }

    setIsLoading(true);
    setPageError(null);

    try {
      const [clientData, petsData] = await Promise.all([
        getClient(clientId),
        canPetRead ? listClientPets(clientId) : Promise.resolve([] as Pet[]),
      ]);
      setClient(clientData);
      setPets(petsData);
    } catch (error) {
      setPageError(toApiMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, [canClientRead, canPetRead, clientId, session?.branchId]);

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
    void loadData();
  }, [loadData]);

  const openClientModal = () => {
    if (!client) {
      return;
    }
    setClientForm(toClientForm(client));
    setClientFormErrors({});
    setClientSubmitError(null);
    setIsClientModalOpen(true);
  };

  const closeClientModal = () => {
    setIsClientModalOpen(false);
    setClientFormErrors({});
    setClientSubmitError(null);
  };

  const closePetModal = () => {
    setPetModalMode(null);
    setEditingPetId(null);
    setPetForm(emptyPetForm());
    setPetFormErrors({});
    setPetSubmitError(null);
  };

  const openCreatePetModal = () => {
    setPetModalMode("create");
    setEditingPetId(null);
    setPetForm(emptyPetForm());
    setPetFormErrors({});
    setPetSubmitError(null);
  };

  const openEditPetModal = async (petId: string) => {
    try {
      const pet = await getPet(petId);
      setPetModalMode("edit");
      setEditingPetId(petId);
      setPetForm(toPetForm(pet));
      setPetFormErrors({});
      setPetSubmitError(null);
    } catch (error) {
      setPageError(toApiMessage(error));
    }
  };

  const onSubmitClient = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!clientId) {
      return;
    }

    const nextErrors = validateClientForm(clientForm);
    if (Object.keys(nextErrors).length > 0) {
      setClientFormErrors(nextErrors);
      return;
    }

    setIsSubmittingClient(true);
    setClientFormErrors({});
    setClientSubmitError(null);

    try {
      await updateClient(clientId, buildClientPatchPayload(clientForm));
      closeClientModal();
      await loadData();
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.validationErrors) {
          setClientFormErrors(error.validationErrors);
        }
        if (error.errorCode === "CLIENT_IDENTIFICATION_INVALID") {
          setClientFormErrors((current) => ({
            ...current,
            identification: "La identificacion debe tener 10 o 13 digitos.",
          }));
        }
      }
      setClientSubmitError(toApiMessage(error));
    } finally {
      setIsSubmittingClient(false);
    }
  };

  const onSubmitPet = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!clientId || !petModalMode) {
      return;
    }

    const nextErrors = validatePetForm(petForm);
    if (Object.keys(nextErrors).length > 0) {
      setPetFormErrors(nextErrors);
      return;
    }

    setIsSubmittingPet(true);
    setPetFormErrors({});
    setPetSubmitError(null);

    try {
      const payload = buildPetPayload(petForm);
      if (petModalMode === "create") {
        await createPet(clientId, payload as PetCreateInput);
      } else {
        if (!editingPetId) {
          throw new Error("No se encontro la mascota a editar.");
        }
        await updatePet(editingPetId, payload as PetPatchInput);
      }
      closePetModal();
      await loadData();
    } catch (error) {
      if (error instanceof ApiError) {
        if (error.validationErrors) {
          setPetFormErrors(error.validationErrors);
        }
        if (error.errorCode === "PET_INTERNAL_CODE_CONFLICT") {
          setPetFormErrors((current) => ({
            ...current,
            internalCode: "Conflicto: el codigo interno ya existe en esta sucursal.",
          }));
        }
      }
      setPetSubmitError(toApiMessage(error));
    } finally {
      setIsSubmittingPet(false);
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
            <h2 className="text-xl font-bold">Ficha de cliente</h2>
            <p className="text-sm text-slate-600">Gestion de datos del propietario y sus mascotas.</p>
          </div>
          <div className="flex gap-2">
            <Link
              href="/clientes"
              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-slate-100"
            >
              Volver
            </Link>
            {canClientUpdate ? (
              <button
                type="button"
                onClick={openClientModal}
                className="rounded-lg bg-slate-900 px-3 py-2 text-sm font-semibold text-white hover:bg-slate-800"
              >
                Editar cliente
              </button>
            ) : null}
          </div>
        </header>

        {!canClientRead ? (
          <div className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            Tu sesion no tiene permiso <code>CLIENT_READ</code>.
          </div>
        ) : null}

        {pageError ? (
          <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
            {pageError}
          </p>
        ) : null}

        {isLoading ? (
          <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
            Cargando ficha...
          </div>
        ) : null}

        {client ? (
          <section className="grid gap-4 xl:grid-cols-[1.1fr_1fr]">
            <article className="rounded-xl border border-slate-200 bg-white p-4">
              <h3 className="text-sm font-bold uppercase tracking-wide text-slate-600">Datos del cliente</h3>
              <dl className="mt-3 grid gap-2 text-sm">
                <div>
                  <dt className="font-semibold text-slate-700">Nombre</dt>
                  <dd>{client.fullName}</dd>
                </div>
                <div>
                  <dt className="font-semibold text-slate-700">Identificacion</dt>
                  <dd>{client.identification ?? "N/D"}</dd>
                </div>
                <div>
                  <dt className="font-semibold text-slate-700">Telefono</dt>
                  <dd>{client.phone ?? "N/D"}</dd>
                </div>
                <div>
                  <dt className="font-semibold text-slate-700">Email</dt>
                  <dd>{client.email ?? "N/D"}</dd>
                </div>
                <div>
                  <dt className="font-semibold text-slate-700">Direccion</dt>
                  <dd>{client.address ?? "N/D"}</dd>
                </div>
                <div>
                  <dt className="font-semibold text-slate-700">Notas</dt>
                  <dd>{client.notes ?? "N/D"}</dd>
                </div>
              </dl>
            </article>

            <article className="rounded-xl border border-slate-200 bg-white p-4">
              <div className="flex items-center justify-between gap-2">
                <h3 className="text-sm font-bold uppercase tracking-wide text-slate-600">Mascotas</h3>
                {canPetCreate ? (
                  <button
                    type="button"
                    onClick={openCreatePetModal}
                    className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold hover:bg-slate-100"
                  >
                    Agregar mascota
                  </button>
                ) : (
                  <span className="rounded-lg border border-amber-300 bg-amber-50 px-2 py-1 text-[11px] text-amber-800">
                    Sin permiso PET_CREATE
                  </span>
                )}
              </div>

              {!canPetRead ? (
                <p className="mt-3 rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
                  Tu sesion no tiene permiso <code>PET_READ</code>.
                </p>
              ) : (
                <div className="mt-3 space-y-2">
                  {pets.map((pet) => (
                    <div key={pet.id} className="rounded-lg border border-slate-200 bg-slate-50 p-3">
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <p className="text-sm font-semibold text-slate-900">{pet.name}</p>
                          <p className="text-xs text-slate-600">
                            {pet.species ?? "N/D"} | Codigo: {pet.internalCode}
                          </p>
                          <p className="text-xs text-slate-600">Alertas: {pet.alerts ?? "N/D"}</p>
                        </div>
                        {canPetUpdate ? (
                          <button
                            type="button"
                            onClick={() => void openEditPetModal(pet.id)}
                            className="rounded border border-slate-300 bg-white px-2 py-1 text-xs font-semibold hover:bg-slate-100"
                          >
                            Editar
                          </button>
                        ) : null}
                      </div>
                    </div>
                  ))}
                  {pets.length === 0 ? (
                    <div className="rounded-lg border border-dashed border-slate-200 bg-slate-50 px-3 py-3 text-sm text-slate-500">
                      Sin mascotas registradas para este cliente.
                    </div>
                  ) : null}
                </div>
              )}
            </article>
          </section>
        ) : null}
      </div>

      {isClientModalOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
          <div className="w-full max-w-2xl rounded-xl border border-slate-200 bg-white p-5 shadow-lg">
            <h3 className="text-lg font-bold">Editar cliente</h3>
            <form onSubmit={onSubmitClient} className="mt-4 space-y-4">
              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Nombre completo</span>
                  <input
                    value={clientForm.fullName}
                    onChange={(event) =>
                      setClientForm((current) => ({ ...current, fullName: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {clientFormErrors.fullName ? (
                    <span className="text-xs text-rose-700">{clientFormErrors.fullName}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Identificacion (opcional)</span>
                  <input
                    value={clientForm.identification}
                    onChange={(event) =>
                      setClientForm((current) => ({ ...current, identification: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {clientFormErrors.identification ? (
                    <span className="text-xs text-rose-700">{clientFormErrors.identification}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Telefono</span>
                  <input
                    value={clientForm.phone}
                    onChange={(event) => setClientForm((current) => ({ ...current, phone: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {clientFormErrors.phone ? (
                    <span className="text-xs text-rose-700">{clientFormErrors.phone}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Email</span>
                  <input
                    type="email"
                    value={clientForm.email}
                    onChange={(event) => setClientForm((current) => ({ ...current, email: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {clientFormErrors.email ? (
                    <span className="text-xs text-rose-700">{clientFormErrors.email}</span>
                  ) : null}
                </label>
              </div>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Direccion</span>
                <input
                  value={clientForm.address}
                  onChange={(event) => setClientForm((current) => ({ ...current, address: event.target.value }))}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Notas</span>
                <textarea
                  value={clientForm.notes}
                  onChange={(event) => setClientForm((current) => ({ ...current, notes: event.target.value }))}
                  className="min-h-[100px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              {clientSubmitError ? (
                <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {clientSubmitError}
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
                  disabled={isSubmittingClient}
                  className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmittingClient ? "Guardando..." : "Guardar cambios"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {petModalMode ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
          <div className="w-full max-w-2xl rounded-xl border border-slate-200 bg-white p-5 shadow-lg">
            <h3 className="text-lg font-bold">
              {petModalMode === "create" ? "Agregar mascota" : "Editar mascota"}
            </h3>
            <p className="mt-1 text-xs text-slate-500">
              Propietario fijo: esta mascota permanece asociada al cliente actual (v1).
            </p>

            <form onSubmit={onSubmitPet} className="mt-4 space-y-4">
              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Codigo interno</span>
                  <input
                    value={petForm.internalCode}
                    onChange={(event) =>
                      setPetForm((current) => ({ ...current, internalCode: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {petFormErrors.internalCode ? (
                    <span className="text-xs text-rose-700">{petFormErrors.internalCode}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Nombre</span>
                  <input
                    value={petForm.name}
                    onChange={(event) => setPetForm((current) => ({ ...current, name: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {petFormErrors.name ? <span className="text-xs text-rose-700">{petFormErrors.name}</span> : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Especie</span>
                  <input
                    value={petForm.species}
                    onChange={(event) => setPetForm((current) => ({ ...current, species: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {petFormErrors.species ? (
                    <span className="text-xs text-rose-700">{petFormErrors.species}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Raza</span>
                  <input
                    value={petForm.breed}
                    onChange={(event) => setPetForm((current) => ({ ...current, breed: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Sexo</span>
                  <input
                    value={petForm.sex}
                    onChange={(event) => setPetForm((current) => ({ ...current, sex: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Fecha de nacimiento</span>
                  <input
                    type="date"
                    value={petForm.birthDate}
                    onChange={(event) => setPetForm((current) => ({ ...current, birthDate: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Peso (kg)</span>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={petForm.weightKg}
                    onChange={(event) => setPetForm((current) => ({ ...current, weightKg: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {petFormErrors.weightKg ? (
                    <span className="text-xs text-rose-700">{petFormErrors.weightKg}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Esterilizado</span>
                  <select
                    value={petForm.neutered}
                    onChange={(event) =>
                      setPetForm((current) => ({
                        ...current,
                        neutered: event.target.value as PetFormState["neutered"],
                      }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  >
                    <option value="null">No especificado</option>
                    <option value="true">Si</option>
                    <option value="false">No</option>
                  </select>
                </label>
              </div>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Alertas</span>
                <textarea
                  value={petForm.alerts}
                  onChange={(event) => setPetForm((current) => ({ ...current, alerts: event.target.value }))}
                  className="min-h-[80px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Antecedentes</span>
                <textarea
                  value={petForm.history}
                  onChange={(event) => setPetForm((current) => ({ ...current, history: event.target.value }))}
                  className="min-h-[80px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              {petSubmitError ? (
                <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {petSubmitError}
                </p>
              ) : null}

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={closePetModal}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmittingPet}
                  className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmittingPet
                    ? "Guardando..."
                    : petModalMode === "create"
                      ? "Crear mascota"
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
