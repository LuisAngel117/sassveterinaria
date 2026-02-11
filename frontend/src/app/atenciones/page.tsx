"use client";

import Link from "next/link";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import { Service, listServices } from "@/lib/api/agenda";
import { ApiError } from "@/lib/api/client";
import {
  SoapTemplate,
  Visit,
  VisitCreateInput,
  createVisit,
  listPetVisits,
  listSoapTemplates,
} from "@/lib/api/clinical";
import { Client, Pet, listClientPets, searchClients } from "@/lib/api/crm";
import { hasPermission } from "@/lib/session/permissions";
import { readSession } from "@/lib/session/store";
import { SessionData } from "@/lib/session/types";

type VisitCreateForm = {
  clientId: string;
  petId: string;
  serviceId: string;
  templateId: string;
  sReason: string;
  sAnamnesis: string;
  oWeightKg: string;
  oTemperatureC: string;
  oFindings: string;
  aDiagnosis: string;
  aSeverity: string;
  pTreatment: string;
  pInstructions: string;
  pFollowupAt: string;
};

function emptyVisitCreateForm(): VisitCreateForm {
  return {
    clientId: "",
    petId: "",
    serviceId: "",
    templateId: "",
    sReason: "",
    sAnamnesis: "",
    oWeightKg: "",
    oTemperatureC: "",
    oFindings: "",
    aDiagnosis: "",
    aSeverity: "",
    pTreatment: "",
    pInstructions: "",
    pFollowupAt: "",
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

function toStatusLabel(status: string): string {
  if (status === "OPEN") {
    return "Abierta";
  }
  if (status === "CLOSED") {
    return "Cerrada";
  }
  return status;
}

function toDateTimeLabel(value: string): string {
  const date = new Date(value);
  return new Intl.DateTimeFormat("es-EC", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

function toNumberOrUndefined(raw: string): number | undefined {
  const normalized = raw.trim();
  if (!normalized) {
    return undefined;
  }
  const parsed = Number(normalized);
  if (Number.isNaN(parsed)) {
    return undefined;
  }
  return parsed;
}

function toStringOrUndefined(raw: string): string | undefined {
  const normalized = raw.trim();
  return normalized ? normalized : undefined;
}

function validateCreateForm(form: VisitCreateForm): Record<string, string> {
  const errors: Record<string, string> = {};
  if (!form.petId) {
    errors.petId = "Selecciona una mascota.";
  }
  if (!form.serviceId) {
    errors.serviceId = "Selecciona un servicio.";
  }
  if (!form.sReason.trim()) {
    errors.sReason = "sReason es requerido.";
  }
  if (!form.sAnamnesis.trim()) {
    errors.sAnamnesis = "sAnamnesis es requerido.";
  }
  if (form.oWeightKg.trim()) {
    const weight = Number(form.oWeightKg);
    if (Number.isNaN(weight) || weight <= 0) {
      errors.oWeightKg = "oWeightKg debe ser mayor a 0.";
    }
  }
  if (form.oTemperatureC.trim()) {
    const temp = Number(form.oTemperatureC);
    if (Number.isNaN(temp) || temp < 20 || temp > 50) {
      errors.oTemperatureC = "oTemperatureC debe estar entre 20.0 y 50.0.";
    }
  }
  return errors;
}

function buildCreatePayload(form: VisitCreateForm): VisitCreateInput {
  return {
    petId: form.petId,
    serviceId: form.serviceId,
    templateId: toStringOrUndefined(form.templateId),
    sReason: toStringOrUndefined(form.sReason),
    sAnamnesis: toStringOrUndefined(form.sAnamnesis),
    oWeightKg: toNumberOrUndefined(form.oWeightKg),
    oTemperatureC: toNumberOrUndefined(form.oTemperatureC),
    oFindings: toStringOrUndefined(form.oFindings),
    aDiagnosis: toStringOrUndefined(form.aDiagnosis),
    aSeverity: toStringOrUndefined(form.aSeverity),
    pTreatment: toStringOrUndefined(form.pTreatment),
    pInstructions: toStringOrUndefined(form.pInstructions),
    pFollowupAt: toStringOrUndefined(form.pFollowupAt),
  };
}

export default function VisitsPage() {
  const router = useRouter();
  const session = useMemo<SessionData | null>(() => readSession(), []);

  const [searchText, setSearchText] = useState("");
  const [clients, setClients] = useState<Client[]>([]);
  const [pets, setPets] = useState<Pet[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [templates, setTemplates] = useState<SoapTemplate[]>([]);
  const [visits, setVisits] = useState<Visit[]>([]);

  const [selectedClientId, setSelectedClientId] = useState("");
  const [selectedPetId, setSelectedPetId] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("OPEN");

  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingVisits, setIsLoadingVisits] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [createForm, setCreateForm] = useState<VisitCreateForm>(emptyVisitCreateForm());
  const [createErrors, setCreateErrors] = useState<Record<string, string>>({});
  const [createSubmitError, setCreateSubmitError] = useState<string | null>(null);
  const [isSubmittingCreate, setIsSubmittingCreate] = useState(false);

  const canVisitRead = hasPermission(session, "VISIT_READ");
  const canVisitCreate = hasPermission(session, "VISIT_CREATE");

  const selectedClient = useMemo(
    () => clients.find((item) => item.id === selectedClientId) ?? null,
    [clients, selectedClientId],
  );
  const selectedPet = useMemo(
    () => pets.find((item) => item.id === selectedPetId) ?? null,
    [pets, selectedPetId],
  );

  const loadClients = useCallback(async () => {
    if (!session?.branchId || !canVisitRead) {
      return;
    }
    setIsLoading(true);
    setPageError(null);
    try {
      const [clientsResponse, servicesResponse] = await Promise.all([
        searchClients(searchText, 0, 20),
        listServices(true),
      ]);
      setClients(clientsResponse.content);
      setServices(servicesResponse);
      if (!selectedClientId && clientsResponse.content.length > 0) {
        const nextClientId = clientsResponse.content[0].id;
        setSelectedClientId(nextClientId);
      }
      if (createForm.serviceId === "" && servicesResponse.length > 0) {
        setCreateForm((current) => ({ ...current, serviceId: servicesResponse[0].id }));
      }
    } catch (error) {
      setPageError(toApiMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, [canVisitRead, createForm.serviceId, searchText, selectedClientId, session?.branchId]);

  const loadPets = useCallback(async () => {
    if (!selectedClientId || !canVisitRead) {
      setPets([]);
      setSelectedPetId("");
      return;
    }
    try {
      const response = await listClientPets(selectedClientId);
      setPets(response);
      if (!response.find((pet) => pet.id === selectedPetId)) {
        setSelectedPetId(response[0]?.id ?? "");
      }
    } catch (error) {
      setPageError(toApiMessage(error));
      setPets([]);
      setSelectedPetId("");
    }
  }, [canVisitRead, selectedClientId, selectedPetId]);

  const loadVisits = useCallback(async () => {
    if (!selectedPetId || !canVisitRead) {
      setVisits([]);
      return;
    }
    setIsLoadingVisits(true);
    setPageError(null);
    try {
      const response = await listPetVisits(selectedPetId, {
        status: statusFilter || undefined,
      });
      setVisits(response);
    } catch (error) {
      setPageError(toApiMessage(error));
      setVisits([]);
    } finally {
      setIsLoadingVisits(false);
    }
  }, [canVisitRead, selectedPetId, statusFilter]);

  const loadTemplates = useCallback(async () => {
    if (!createForm.serviceId) {
      setTemplates([]);
      setCreateForm((current) => ({ ...current, templateId: "" }));
      return;
    }
    try {
      const response = await listSoapTemplates(createForm.serviceId);
      setTemplates(response);
      if (!response.find((item) => item.id === createForm.templateId)) {
        setCreateForm((current) => ({ ...current, templateId: "" }));
      }
    } catch {
      setTemplates([]);
      setCreateForm((current) => ({ ...current, templateId: "" }));
    }
  }, [createForm.serviceId, createForm.templateId]);

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

  useEffect(() => {
    void loadPets();
  }, [loadPets]);

  useEffect(() => {
    void loadVisits();
  }, [loadVisits]);

  useEffect(() => {
    if (!isCreateModalOpen) {
      return;
    }
    void loadTemplates();
  }, [isCreateModalOpen, loadTemplates]);

  const closeCreateModal = () => {
    setIsCreateModalOpen(false);
    setCreateForm((current) => ({
      ...emptyVisitCreateForm(),
      clientId: selectedClientId,
      petId: selectedPetId,
      serviceId: services[0]?.id ?? "",
    }));
    setCreateErrors({});
    setCreateSubmitError(null);
    setTemplates([]);
  };

  const openCreateModal = () => {
    setCreateForm({
      ...emptyVisitCreateForm(),
      clientId: selectedClientId,
      petId: selectedPetId,
      serviceId: services[0]?.id ?? "",
    });
    setCreateErrors({});
    setCreateSubmitError(null);
    setIsCreateModalOpen(true);
  };

  const onSearchSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    await loadClients();
  };

  const onSubmitCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const nextErrors = validateCreateForm(createForm);
    if (Object.keys(nextErrors).length > 0) {
      setCreateErrors(nextErrors);
      return;
    }

    setIsSubmittingCreate(true);
    setCreateErrors({});
    setCreateSubmitError(null);

    try {
      const visit = await createVisit(buildCreatePayload(createForm));
      closeCreateModal();
      router.push(`/atenciones/${visit.id}`);
    } catch (error) {
      if (error instanceof ApiError && error.validationErrors) {
        setCreateErrors(error.validationErrors);
      }
      setCreateSubmitError(toApiMessage(error));
    } finally {
      setIsSubmittingCreate(false);
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
    <AppShell session={session} activeNav="atenciones">
      <div className="space-y-5">
        <header className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold">Atenciones</h2>
            <p className="text-sm text-slate-600">
              Cola por mascota usando contrato real <code>/api/v1/pets/{`{petId}`}/visits</code>.
            </p>
          </div>
          {canVisitCreate ? (
            <button
              type="button"
              onClick={openCreateModal}
              className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
            >
              Nueva atencion (walk-in)
            </button>
          ) : (
            <span className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-xs text-amber-800">
              Sin permiso VISIT_CREATE
            </span>
          )}
        </header>

        {!canVisitRead ? (
          <p className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            Tu sesion no tiene permiso <code>VISIT_READ</code>.
          </p>
        ) : (
          <>
            <form onSubmit={onSearchSubmit} className="grid gap-3 md:grid-cols-[1fr_auto]">
              <input
                value={searchText}
                onChange={(event) => setSearchText(event.target.value)}
                placeholder="Buscar cliente por nombre, telefono o identificacion"
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

            <div className="grid gap-3 md:grid-cols-3">
              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Cliente</span>
                <select
                  value={selectedClientId}
                  onChange={(event) => setSelectedClientId(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                >
                  <option value="">Selecciona cliente</option>
                  {clients.map((client) => (
                    <option key={client.id} value={client.id}>
                      {client.fullName}
                    </option>
                  ))}
                </select>
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Mascota</span>
                <select
                  value={selectedPetId}
                  onChange={(event) => setSelectedPetId(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  disabled={!selectedClientId}
                >
                  <option value="">Selecciona mascota</option>
                  {pets.map((pet) => (
                    <option key={pet.id} value={pet.id}>
                      {pet.name} ({pet.internalCode})
                    </option>
                  ))}
                </select>
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Estado</span>
                <select
                  value={statusFilter}
                  onChange={(event) => setStatusFilter(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                >
                  <option value="">Todos</option>
                  <option value="OPEN">Abiertas</option>
                  <option value="CLOSED">Cerradas</option>
                </select>
              </label>
            </div>

            {selectedClient ? (
              <p className="text-xs text-slate-500">
                Cliente seleccionado: <span className="font-semibold text-slate-700">{selectedClient.fullName}</span>
              </p>
            ) : null}

            {selectedPet ? (
              <p className="text-xs text-slate-500">
                Mascota seleccionada: <span className="font-semibold text-slate-700">{selectedPet.name}</span>
              </p>
            ) : null}

            {pageError ? (
              <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                {pageError}
              </p>
            ) : null}

            {isLoadingVisits ? (
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
                Cargando atenciones...
              </div>
            ) : null}

            <div className="space-y-2">
              {visits.map((visit) => (
                <article key={visit.id} className="rounded-xl border border-slate-200 bg-white p-4">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <p className="text-sm font-semibold text-slate-900">Atencion {visit.id}</p>
                      <p className="text-xs text-slate-600">Creada: {toDateTimeLabel(visit.createdAt)}</p>
                    </div>
                    <span className="rounded border border-slate-300 bg-slate-50 px-2 py-1 text-xs font-semibold uppercase text-slate-700">
                      {toStatusLabel(visit.status)}
                    </span>
                  </div>
                  <div className="mt-2 grid gap-1 text-xs text-slate-700 md:grid-cols-2">
                    <p>Servicio: {visit.serviceId}</p>
                    <p>Motivo: {visit.sReason ?? "N/D"}</p>
                    <p>Diagnostico: {visit.aDiagnosis ?? "N/D"}</p>
                    <p>Actualizado: {toDateTimeLabel(visit.updatedAt)}</p>
                  </div>
                  <div className="mt-3">
                    <Link
                      href={`/atenciones/${visit.id}`}
                      className="inline-flex rounded border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold hover:bg-slate-100"
                    >
                      Abrir detalle
                    </Link>
                  </div>
                </article>
              ))}

              {!isLoadingVisits && visits.length === 0 ? (
                <div className="rounded-xl border border-dashed border-slate-200 bg-slate-50 px-3 py-4 text-sm text-slate-500">
                  No hay atenciones para los filtros actuales.
                </div>
              ) : null}
            </div>
          </>
        )}
      </div>

      {isCreateModalOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
          <div className="w-full max-w-4xl rounded-xl border border-slate-200 bg-white p-5 shadow-lg">
            <h3 className="text-lg font-bold">Nueva atencion (walk-in)</h3>
            <p className="mt-1 text-xs text-slate-500">Contrato real: <code>POST /api/v1/visits</code>.</p>

            <form onSubmit={onSubmitCreate} className="mt-4 space-y-4">
              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Cliente</span>
                  <select
                    value={createForm.clientId}
                    onChange={(event) => {
                      const clientId = event.target.value;
                      setCreateForm((current) => ({ ...current, clientId, petId: "" }));
                    }}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  >
                    <option value="">Selecciona cliente</option>
                    {clients.map((client) => (
                      <option key={client.id} value={client.id}>
                        {client.fullName}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Mascota</span>
                  <select
                    value={createForm.petId}
                    onChange={(event) => setCreateForm((current) => ({ ...current, petId: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  >
                    <option value="">Selecciona mascota</option>
                    {(createForm.clientId ? pets.filter((pet) => pet.clientId === createForm.clientId) : pets).map(
                      (pet) => (
                        <option key={pet.id} value={pet.id}>
                          {pet.name} ({pet.internalCode})
                        </option>
                      ),
                    )}
                  </select>
                  {createErrors.petId ? <span className="text-xs text-rose-700">{createErrors.petId}</span> : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Servicio</span>
                  <select
                    value={createForm.serviceId}
                    onChange={(event) =>
                      setCreateForm((current) => ({
                        ...current,
                        serviceId: event.target.value,
                        templateId: "",
                      }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  >
                    <option value="">Selecciona servicio</option>
                    {services.map((service) => (
                      <option key={service.id} value={service.id}>
                        {service.name}
                      </option>
                    ))}
                  </select>
                  {createErrors.serviceId ? (
                    <span className="text-xs text-rose-700">{createErrors.serviceId}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Plantilla SOAP (opcional)</span>
                  <select
                    value={createForm.templateId}
                    onChange={(event) => setCreateForm((current) => ({ ...current, templateId: event.target.value }))}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  >
                    <option value="">Sin plantilla</option>
                    {templates.map((template) => (
                      <option key={template.id} value={template.id}>
                        {template.name}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">S: motivo consulta</span>
                  <textarea
                    value={createForm.sReason}
                    onChange={(event) => setCreateForm((current) => ({ ...current, sReason: event.target.value }))}
                    className="min-h-[80px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {createErrors.sReason ? (
                    <span className="text-xs text-rose-700">{createErrors.sReason}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">S: anamnesis</span>
                  <textarea
                    value={createForm.sAnamnesis}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, sAnamnesis: event.target.value }))
                    }
                    className="min-h-[80px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  />
                  {createErrors.sAnamnesis ? (
                    <span className="text-xs text-rose-700">{createErrors.sAnamnesis}</span>
                  ) : null}
                </label>
              </div>

              <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">O: peso (kg)</span>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={createForm.oWeightKg}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, oWeightKg: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {createErrors.oWeightKg ? (
                    <span className="text-xs text-rose-700">{createErrors.oWeightKg}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">O: temperatura (C)</span>
                  <input
                    type="number"
                    min="20"
                    max="50"
                    step="0.1"
                    value={createForm.oTemperatureC}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, oTemperatureC: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                  {createErrors.oTemperatureC ? (
                    <span className="text-xs text-rose-700">{createErrors.oTemperatureC}</span>
                  ) : null}
                </label>

                <label className="space-y-1 text-sm xl:col-span-2">
                  <span className="font-semibold text-slate-700">O: hallazgos</span>
                  <input
                    value={createForm.oFindings}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, oFindings: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>
              </div>

              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">A: diagnostico</span>
                  <textarea
                    value={createForm.aDiagnosis}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, aDiagnosis: event.target.value }))
                    }
                    className="min-h-[70px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">A: severidad</span>
                  <input
                    value={createForm.aSeverity}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, aSeverity: event.target.value }))
                    }
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">P: tratamiento</span>
                  <textarea
                    value={createForm.pTreatment}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, pTreatment: event.target.value }))
                    }
                    className="min-h-[70px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">P: indicaciones</span>
                  <textarea
                    value={createForm.pInstructions}
                    onChange={(event) =>
                      setCreateForm((current) => ({ ...current, pInstructions: event.target.value }))
                    }
                    className="min-h-[70px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  />
                </label>
              </div>

              <label className="space-y-1 text-sm md:max-w-sm">
                <span className="font-semibold text-slate-700">P: fecha recontrol</span>
                <input
                  type="date"
                  value={createForm.pFollowupAt}
                  onChange={(event) =>
                    setCreateForm((current) => ({ ...current, pFollowupAt: event.target.value }))
                  }
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                />
              </label>

              {createSubmitError ? (
                <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {createSubmitError}
                </p>
              ) : null}

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={closeCreateModal}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmittingCreate}
                  className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmittingCreate ? "Creando..." : "Crear atencion"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </AppShell>
  );
}
