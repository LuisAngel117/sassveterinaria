"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppShell } from "@/components/app-shell";
import {
  Appointment,
  CreateAppointmentInput,
  UpdateAppointmentInput,
  Room,
  RoomBlock,
  Service,
  cancelAppointment,
  checkInAppointment,
  closeAppointment,
  confirmAppointment,
  createAppointment,
  createRoomBlock,
  listAppointments,
  listRoomBlocks,
  listRooms,
  listServices,
  startAppointment,
  updateAppointment,
} from "@/lib/api/agenda";
import { Client, Pet, listClientPets, searchClients } from "@/lib/api/crm";
import { ApiError } from "@/lib/api/client";
import { hasPermission } from "@/lib/session/permissions";
import { readSession } from "@/lib/session/store";
import { SessionData } from "@/lib/session/types";

const BUSINESS_TIMEZONE = "America/Guayaquil";

type AppointmentFormMode = "create" | "edit";

type PendingOverbook =
  | {
      mode: "create";
      payload: CreateAppointmentInput;
    }
  | {
      mode: "edit";
      appointmentId: string;
      payload: UpdateAppointmentInput;
    };

const STATUS_LABELS: Record<string, string> = {
  RESERVED: "Reservada",
  CONFIRMED: "Confirmada",
  IN_ATTENTION: "En atencion",
  CLOSED: "Cerrada",
  CANCELLED: "Cancelada",
};

const BASE_STATUS_ORDER = ["RESERVED", "CONFIRMED", "IN_ATTENTION", "CLOSED", "CANCELLED"];

function startOfWeekMonday(input: Date): Date {
  const date = new Date(input);
  date.setHours(0, 0, 0, 0);
  const day = date.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  date.setDate(date.getDate() + diff);
  return date;
}

function addDays(input: Date, days: number): Date {
  const next = new Date(input);
  next.setDate(next.getDate() + days);
  return next;
}

function formatWeekRangeLabel(weekStart: Date): string {
  const weekEnd = addDays(weekStart, 6);
  const formatter = new Intl.DateTimeFormat("es-EC", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    timeZone: BUSINESS_TIMEZONE,
  });
  return `${formatter.format(weekStart)} - ${formatter.format(weekEnd)}`;
}

function toBusinessDateKey(value: string | Date): string {
  const date = value instanceof Date ? value : new Date(value);
  const formatter = new Intl.DateTimeFormat("en-CA", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    timeZone: BUSINESS_TIMEZONE,
  });
  return formatter.format(date);
}

function formatDayLabel(day: Date): string {
  const formatter = new Intl.DateTimeFormat("es-EC", {
    weekday: "short",
    day: "2-digit",
    month: "2-digit",
    timeZone: BUSINESS_TIMEZONE,
  });
  return formatter.format(day);
}

function formatTime(isoDateTime: string): string {
  const date = new Date(isoDateTime);
  const formatter = new Intl.DateTimeFormat("es-EC", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
    timeZone: BUSINESS_TIMEZONE,
  });
  return formatter.format(date);
}

function toDateTimeLocalValue(isoDateTime: string): string {
  const date = new Date(isoDateTime);
  const offsetMinutes = date.getTimezoneOffset();
  const local = new Date(date.getTime() - offsetMinutes * 60 * 1000);
  return local.toISOString().slice(0, 16);
}

function toIsoDateTime(localDateTime: string): string | null {
  if (!localDateTime.trim()) {
    return null;
  }
  const parsed = new Date(localDateTime);
  if (Number.isNaN(parsed.getTime())) {
    return null;
  }
  return parsed.toISOString();
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
  return STATUS_LABELS[status] ?? status;
}

function isSensitiveCancel(status: string): boolean {
  return status === "CONFIRMED" || status === "IN_ATTENTION";
}

function byStartsAt(a: Appointment, b: Appointment): number {
  return new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime();
}

function byStartsAtBlock(a: RoomBlock, b: RoomBlock): number {
  return new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime();
}

export default function AgendaPage() {
  const router = useRouter();
  const session = useMemo<SessionData | null>(() => readSession(), []);

  const [weekStart, setWeekStart] = useState<Date>(() => startOfWeekMonday(new Date()));
  const [selectedRoomId, setSelectedRoomId] = useState<string>("");
  const [selectedStatus, setSelectedStatus] = useState<string>("");

  const [rooms, setRooms] = useState<Room[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [roomBlocks, setRoomBlocks] = useState<RoomBlock[]>([]);

  const [isBootLoading, setIsBootLoading] = useState(false);
  const [isAgendaLoading, setIsAgendaLoading] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);

  const [isAppointmentModalOpen, setIsAppointmentModalOpen] = useState(false);
  const [appointmentModalMode, setAppointmentModalMode] = useState<AppointmentFormMode>("create");
  const [editingAppointmentId, setEditingAppointmentId] = useState<string | null>(null);
  const [appointmentFormError, setAppointmentFormError] = useState<string | null>(null);
  const [isSubmittingAppointment, setIsSubmittingAppointment] = useState(false);

  const [formRoomId, setFormRoomId] = useState("");
  const [formServiceId, setFormServiceId] = useState("");
  const [formStartsAt, setFormStartsAt] = useState("");
  const [formReason, setFormReason] = useState("");
  const [formNotes, setFormNotes] = useState("");
  const [formClientId, setFormClientId] = useState("");
  const [formPetId, setFormPetId] = useState("");

  const [clientSearchQuery, setClientSearchQuery] = useState("");
  const [clientSearchResults, setClientSearchResults] = useState<Client[]>([]);
  const [clientPets, setClientPets] = useState<Pet[]>([]);
  const [isSearchingClients, setIsSearchingClients] = useState(false);
  const [isLoadingPets, setIsLoadingPets] = useState(false);

  const [pendingOverbook, setPendingOverbook] = useState<PendingOverbook | null>(null);
  const [pendingCancelAppointment, setPendingCancelAppointment] = useState<Appointment | null>(null);
  const [reasonInput, setReasonInput] = useState("");
  const [reasonModalError, setReasonModalError] = useState<string | null>(null);
  const [isSubmittingReason, setIsSubmittingReason] = useState(false);

  const [actionBusyKey, setActionBusyKey] = useState<string | null>(null);

  const [blockRoomId, setBlockRoomId] = useState("");
  const [blockStartsAt, setBlockStartsAt] = useState("");
  const [blockEndsAt, setBlockEndsAt] = useState("");
  const [blockReason, setBlockReason] = useState("");
  const [blockError, setBlockError] = useState<string | null>(null);
  const [isSubmittingBlock, setIsSubmittingBlock] = useState(false);

  const canRead = hasPermission(session, "APPT_READ");
  const canCreate = hasPermission(session, "APPT_CREATE");
  const canUpdate = hasPermission(session, "APPT_UPDATE");
  const canCancel = hasPermission(session, "APPT_CANCEL");
  const canOverbook = hasPermission(session, "APPT_OVERBOOK");
  const canCheckIn = hasPermission(session, "APPT_CHECKIN");
  const canStartVisit = hasPermission(session, "APPT_START_VISIT");
  const canClose = hasPermission(session, "APPT_CLOSE");
  const canManageBlocks = hasPermission(session, "BRANCH_MANAGE");

  const weekDays = useMemo(() => {
    return Array.from({ length: 7 }, (_, index) => addDays(weekStart, index));
  }, [weekStart]);

  const weekRange = useMemo(() => {
    const from = new Date(weekStart);
    from.setHours(0, 0, 0, 0);

    const to = addDays(from, 7);
    to.setHours(0, 0, 0, 0);

    return {
      from: from.toISOString(),
      to: to.toISOString(),
    };
  }, [weekStart]);

  const roomNameById = useMemo(() => {
    return new Map(rooms.map((room) => [room.id, room.name]));
  }, [rooms]);

  const serviceNameById = useMemo(() => {
    return new Map(services.map((service) => [service.id, service.name]));
  }, [services]);

  const statusOptions = useMemo(() => {
    const set = new Set<string>(BASE_STATUS_ORDER);
    appointments.forEach((item) => {
      set.add(item.status);
    });
    return Array.from(set.values());
  }, [appointments]);

  const appointmentsByDay = useMemo(() => {
    const grouped = new Map<string, Appointment[]>();
    weekDays.forEach((day) => grouped.set(toBusinessDateKey(day), []));
    appointments.forEach((appointment) => {
      const key = toBusinessDateKey(appointment.startsAt);
      const list = grouped.get(key);
      if (list) {
        list.push(appointment);
      }
    });
    grouped.forEach((list) => list.sort(byStartsAt));
    return grouped;
  }, [appointments, weekDays]);

  const roomBlocksByDay = useMemo(() => {
    const grouped = new Map<string, RoomBlock[]>();
    weekDays.forEach((day) => grouped.set(toBusinessDateKey(day), []));
    roomBlocks.forEach((block) => {
      const key = toBusinessDateKey(block.startsAt);
      const list = grouped.get(key);
      if (list) {
        list.push(block);
      }
    });
    grouped.forEach((list) => list.sort(byStartsAtBlock));
    return grouped;
  }, [roomBlocks, weekDays]);

  const reasonModalMode: "overbook" | "cancel" | null = pendingOverbook
    ? "overbook"
    : pendingCancelAppointment
      ? "cancel"
      : null;

  const reasonModalTitle = reasonModalMode === "cancel" ? "Motivo de cancelacion" : "Motivo de sobre-cupo";

  const selectedClient = useMemo(() => {
    if (!formClientId) {
      return null;
    }
    return clientSearchResults.find((client) => client.id === formClientId) ?? null;
  }, [clientSearchResults, formClientId]);

  const resetAppointmentForm = useCallback(() => {
    setAppointmentFormError(null);
    setEditingAppointmentId(null);
    setFormRoomId("");
    setFormServiceId("");
    setFormStartsAt("");
    setFormReason("");
    setFormNotes("");
    setFormClientId("");
    setFormPetId("");
    setClientSearchQuery("");
    setClientSearchResults([]);
    setClientPets([]);
  }, []);

  const closeReasonModal = () => {
    setPendingOverbook(null);
    setPendingCancelAppointment(null);
    setReasonInput("");
    setReasonModalError(null);
  };

  const loadBootstrapData = useCallback(async () => {
    if (!session?.branchId) {
      return;
    }

    setIsBootLoading(true);
    setPageError(null);

    try {
      const [roomsData, servicesData] = await Promise.all([listRooms(), listServices(true)]);
      setRooms(roomsData);
      setServices(servicesData);

      if (!formRoomId && roomsData.length > 0) {
        setFormRoomId(roomsData[0].id);
      }
      if (!formServiceId && servicesData.length > 0) {
        setFormServiceId(servicesData[0].id);
      }
      if (!blockRoomId && roomsData.length > 0) {
        setBlockRoomId(roomsData[0].id);
      }
      if (!selectedRoomId && roomsData.length > 0) {
        setSelectedRoomId("");
      }
    } catch (error) {
      setPageError(toApiMessage(error));
    } finally {
      setIsBootLoading(false);
    }
  }, [blockRoomId, formRoomId, formServiceId, selectedRoomId, session?.branchId]);

  const loadAgenda = useCallback(async () => {
    if (!session?.branchId || !canRead) {
      return;
    }

    setIsAgendaLoading(true);
    setPageError(null);

    try {
      const [appointmentsData, blocksData] = await Promise.all([
        listAppointments({
          from: weekRange.from,
          to: weekRange.to,
          roomId: selectedRoomId || undefined,
          status: selectedStatus || undefined,
        }),
        listRoomBlocks(weekRange.from, weekRange.to, selectedRoomId || undefined),
      ]);

      setAppointments(appointmentsData);
      setRoomBlocks(blocksData);
    } catch (error) {
      setPageError(toApiMessage(error));
    } finally {
      setIsAgendaLoading(false);
    }
  }, [canRead, selectedRoomId, selectedStatus, session?.branchId, weekRange.from, weekRange.to]);

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
    void loadBootstrapData();
  }, [loadBootstrapData]);

  useEffect(() => {
    void loadAgenda();
  }, [loadAgenda]);

  const openCreateModal = () => {
    resetAppointmentForm();
    setAppointmentModalMode("create");
    setFormRoomId(selectedRoomId || rooms[0]?.id || "");
    setFormServiceId(services[0]?.id || "");
    const oneHourAhead = new Date(Date.now() + 60 * 60 * 1000).toISOString();
    setFormStartsAt(toDateTimeLocalValue(oneHourAhead));
    setIsAppointmentModalOpen(true);
  };

  const openEditModal = (appointment: Appointment) => {
    resetAppointmentForm();
    setAppointmentModalMode("edit");
    setEditingAppointmentId(appointment.id);
    setFormRoomId(appointment.roomId);
    setFormServiceId(appointment.serviceId);
    setFormStartsAt(toDateTimeLocalValue(appointment.startsAt));
    setFormReason(appointment.reason ?? "");
    setFormNotes(appointment.notes ?? "");
    setFormClientId(appointment.clientId ?? "");
    setFormPetId(appointment.petId ?? "");
    setIsAppointmentModalOpen(true);
  };

  const closeAppointmentModal = () => {
    setIsAppointmentModalOpen(false);
    resetAppointmentForm();
  };

  const loadPetsForClient = async (clientId: string) => {
    setIsLoadingPets(true);
    setAppointmentFormError(null);
    try {
      const pets = await listClientPets(clientId);
      setClientPets(pets);
      if (!pets.find((pet) => pet.id === formPetId)) {
        setFormPetId(pets[0]?.id ?? "");
      }
    } catch (error) {
      setAppointmentFormError(toApiMessage(error));
      setClientPets([]);
      setFormPetId("");
    } finally {
      setIsLoadingPets(false);
    }
  };

  const onSearchClients = async () => {
    setIsSearchingClients(true);
    setAppointmentFormError(null);
    try {
      const response = await searchClients(clientSearchQuery, 0, 20);
      setClientSearchResults(response.content);
      if (!response.content.find((client) => client.id === formClientId)) {
        setFormClientId("");
        setFormPetId("");
        setClientPets([]);
      }
    } catch (error) {
      setAppointmentFormError(toApiMessage(error));
      setClientSearchResults([]);
    } finally {
      setIsSearchingClients(false);
    }
  };

  const submitCreateOrUpdate = async (payload: CreateAppointmentInput | UpdateAppointmentInput) => {
    if (appointmentModalMode === "create") {
      await createAppointment(payload as CreateAppointmentInput);
    } else {
      if (!editingAppointmentId) {
        throw new Error("No se encontro la cita a editar.");
      }
      await updateAppointment(editingAppointmentId, payload as UpdateAppointmentInput);
    }
  };

  const onSubmitAppointment = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAppointmentFormError(null);

    if (!formRoomId) {
      setAppointmentFormError("Selecciona una sala.");
      return;
    }

    const startsAtIso = toIsoDateTime(formStartsAt);
    if (!startsAtIso) {
      setAppointmentFormError("Ingresa una fecha y hora validas.");
      return;
    }

    if (appointmentModalMode === "create" && !formServiceId) {
      setAppointmentFormError("Selecciona un servicio.");
      return;
    }

    if (appointmentModalMode === "create" && !formClientId) {
      setAppointmentFormError("Selecciona un cliente.");
      return;
    }

    if (appointmentModalMode === "create" && !formPetId) {
      setAppointmentFormError("Selecciona una mascota.");
      return;
    }

    const payload: CreateAppointmentInput | UpdateAppointmentInput =
      appointmentModalMode === "create"
        ? {
            roomId: formRoomId,
            serviceId: formServiceId,
            startsAt: startsAtIso,
            reason: formReason.trim() || undefined,
            notes: formNotes.trim() || undefined,
            clientId: formClientId || undefined,
            petId: formPetId || undefined,
          }
        : {
            roomId: formRoomId,
            startsAt: startsAtIso,
          };

    setIsSubmittingAppointment(true);
    try {

      await submitCreateOrUpdate(payload);
      closeAppointmentModal();
      await loadAgenda();
    } catch (error) {
      if (error instanceof ApiError && error.errorCode === "APPT_OVERLAP" && canOverbook) {
        setPendingOverbook(
          appointmentModalMode === "create"
            ? {
                mode: "create",
                payload: payload as CreateAppointmentInput,
              }
            : {
                mode: "edit",
                appointmentId: editingAppointmentId ?? "",
                payload: payload as UpdateAppointmentInput,
              },
        );
        setReasonInput("");
        setReasonModalError("Existe conflicto de sala. Ingresa motivo para reintentar con sobre-cupo.");
      } else {
        setAppointmentFormError(toApiMessage(error));
      }
    } finally {
      setIsSubmittingAppointment(false);
    }
  };

  const executeTransition = async (
    action: "checkin" | "confirm" | "start" | "close" | "cancel",
    appointment: Appointment,
    reason?: string,
  ) => {
    const busyKey = `${action}:${appointment.id}`;
    setActionBusyKey(busyKey);
    setPageError(null);
    try {
      if (action === "checkin") {
        await checkInAppointment(appointment.id);
      } else if (action === "confirm") {
        await confirmAppointment(appointment.id);
      } else if (action === "start") {
        await startAppointment(appointment.id);
      } else if (action === "close") {
        await closeAppointment(appointment.id);
      } else {
        await cancelAppointment(appointment.id, reason);
      }
      await loadAgenda();
    } catch (error) {
      if (
        action === "cancel" &&
        error instanceof ApiError &&
        error.errorCode === "REASON_REQUIRED" &&
        !reason
      ) {
        setPendingCancelAppointment(appointment);
        setReasonInput("");
        setReasonModalError("Esta cancelacion requiere motivo (minimo 10 caracteres).");
      } else {
        setPageError(toApiMessage(error));
      }
    } finally {
      setActionBusyKey(null);
    }
  };

  const onCancelClicked = async (appointment: Appointment) => {
    if (isSensitiveCancel(appointment.status)) {
      setPendingCancelAppointment(appointment);
      setReasonInput("");
      setReasonModalError(null);
      return;
    }
    await executeTransition("cancel", appointment);
  };

  const onSubmitReason = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const reason = reasonInput.trim();

    if (reason.length < 10) {
      setReasonModalError("Ingresa un motivo de al menos 10 caracteres.");
      return;
    }

    setIsSubmittingReason(true);
    setReasonModalError(null);

    try {
      if (pendingOverbook) {
        if (pendingOverbook.mode === "create") {
          await createAppointment({
            ...pendingOverbook.payload,
            overbookReason: reason,
          });
          closeAppointmentModal();
        } else {
          await updateAppointment(pendingOverbook.appointmentId, {
            ...pendingOverbook.payload,
            overbookReason: reason,
          });
          closeAppointmentModal();
        }
      } else if (pendingCancelAppointment) {
        await executeTransition("cancel", pendingCancelAppointment, reason);
      }

      closeReasonModal();
      await loadAgenda();
    } catch (error) {
      setReasonModalError(toApiMessage(error));
    } finally {
      setIsSubmittingReason(false);
    }
  };

  const onSubmitBlock = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setBlockError(null);

    if (!blockRoomId) {
      setBlockError("Selecciona una sala para bloquear.");
      return;
    }

    const startsAtIso = toIsoDateTime(blockStartsAt);
    const endsAtIso = toIsoDateTime(blockEndsAt);
    if (!startsAtIso || !endsAtIso) {
      setBlockError("Ingresa fecha y hora validas para el bloqueo.");
      return;
    }

    if (!blockReason.trim()) {
      setBlockError("Ingresa un motivo para el bloqueo.");
      return;
    }

    setIsSubmittingBlock(true);
    try {
      await createRoomBlock({
        roomId: blockRoomId,
        startsAt: startsAtIso,
        endsAt: endsAtIso,
        reason: blockReason.trim(),
      });

      setBlockReason("");
      const nextStart = new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString();
      const nextEnd = new Date(Date.now() + 3 * 60 * 60 * 1000).toISOString();
      setBlockStartsAt(toDateTimeLocalValue(nextStart));
      setBlockEndsAt(toDateTimeLocalValue(nextEnd));
      await loadAgenda();
    } catch (error) {
      setBlockError(toApiMessage(error));
    } finally {
      setIsSubmittingBlock(false);
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
    <AppShell session={session} activeNav="agenda">
      <div className="space-y-6">
        <header className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold">Agenda semanal</h2>
              <p className="text-sm text-slate-600">
                Rango actual: <span className="font-semibold">{formatWeekRangeLabel(weekStart)}</span>
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => setWeekStart((current) => addDays(current, -7))}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-slate-50"
              >
                Semana anterior
              </button>
              <button
                type="button"
                onClick={() => setWeekStart(startOfWeekMonday(new Date()))}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-slate-50"
              >
                Semana actual
              </button>
              <button
                type="button"
                onClick={() => setWeekStart((current) => addDays(current, 7))}
                className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-semibold hover:bg-slate-50"
              >
                Semana siguiente
              </button>
            </div>
          </div>

          <div className="grid gap-3 md:grid-cols-[1fr_1fr_auto]">
            <label className="space-y-1 text-sm">
              <span className="font-semibold text-slate-700">Sala</span>
              <select
                value={selectedRoomId}
                onChange={(event) => setSelectedRoomId(event.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
              >
                <option value="">Todas las salas</option>
                {rooms.map((room) => (
                  <option key={room.id} value={room.id}>
                    {room.name}
                  </option>
                ))}
              </select>
            </label>

            <label className="space-y-1 text-sm">
              <span className="font-semibold text-slate-700">Estado</span>
              <select
                value={selectedStatus}
                onChange={(event) => setSelectedStatus(event.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
              >
                <option value="">Todos los estados</option>
                {statusOptions.map((status) => (
                  <option key={status} value={status}>
                    {toStatusLabel(status)}
                  </option>
                ))}
              </select>
            </label>

            <div className="flex items-end gap-2">
              {canCreate ? (
                <button
                  type="button"
                  onClick={openCreateModal}
                  className="w-full rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800"
                >
                  Crear cita
                </button>
              ) : (
                <div className="w-full rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-xs text-amber-800">
                  Sin permiso APPT_CREATE
                </div>
              )}
            </div>
          </div>

          {pageError ? (
            <div className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
              {pageError}
            </div>
          ) : null}

          {!canRead ? (
            <div className="rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              Tu sesion no tiene permiso <code>APPT_READ</code>. No se puede consultar agenda.
            </div>
          ) : null}
        </header>

        {canManageBlocks ? (
          <section className="rounded-xl border border-slate-200 bg-slate-50 p-4">
            <h3 className="text-sm font-bold uppercase tracking-wide text-slate-600">Bloqueo manual</h3>
            <form onSubmit={onSubmitBlock} className="mt-3 grid gap-3 md:grid-cols-2 xl:grid-cols-5">
              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Sala</span>
                <select
                  value={blockRoomId}
                  onChange={(event) => setBlockRoomId(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  required
                >
                  <option value="">Selecciona sala</option>
                  {rooms.map((room) => (
                    <option key={room.id} value={room.id}>
                      {room.name}
                    </option>
                  ))}
                </select>
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Inicio</span>
                <input
                  type="datetime-local"
                  value={blockStartsAt}
                  onChange={(event) => setBlockStartsAt(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  required
                />
              </label>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Fin</span>
                <input
                  type="datetime-local"
                  value={blockEndsAt}
                  onChange={(event) => setBlockEndsAt(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  required
                />
              </label>

              <label className="space-y-1 text-sm xl:col-span-2">
                <span className="font-semibold text-slate-700">Motivo</span>
                <input
                  type="text"
                  value={blockReason}
                  onChange={(event) => setBlockReason(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  placeholder="Ej: mantenimiento sala"
                  required
                />
              </label>

              <div className="flex items-end xl:col-span-5">
                <button
                  type="submit"
                  disabled={isSubmittingBlock}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmittingBlock ? "Guardando bloqueo..." : "Crear bloqueo"}
                </button>
              </div>
            </form>
            {blockError ? (
              <p className="mt-3 rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                {blockError}
              </p>
            ) : null}
          </section>
        ) : null}

        {isBootLoading || isAgendaLoading ? (
          <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
            Cargando agenda...
          </div>
        ) : null}

        {canRead ? (
          <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-7">
            {weekDays.map((day) => {
              const dayKey = toBusinessDateKey(day);
              const dayAppointments = appointmentsByDay.get(dayKey) ?? [];
              const dayBlocks = roomBlocksByDay.get(dayKey) ?? [];

              return (
                <article key={dayKey} className="rounded-xl border border-slate-200 bg-white p-3">
                  <h3 className="text-sm font-bold uppercase tracking-wide text-slate-600">
                    {formatDayLabel(day)}
                  </h3>

                  <div className="mt-3 space-y-2">
                    {dayBlocks.map((block) => (
                      <div
                        key={block.id}
                        className="rounded-lg border border-amber-300 bg-amber-50 px-2 py-2 text-xs text-amber-900"
                      >
                        <p className="font-semibold">Bloqueo</p>
                        <p>
                          {formatTime(block.startsAt)} - {formatTime(block.endsAt)}
                        </p>
                        <p className="break-words">{block.reason}</p>
                      </div>
                    ))}

                    {dayAppointments.map((appointment) => {
                      const canEditAppointment =
                        canUpdate && appointment.status !== "CANCELLED" && appointment.status !== "CLOSED";
                      const canCancelAppointment =
                        canCancel && appointment.status !== "CANCELLED" && appointment.status !== "CLOSED";
                      const canConfirmAppointment = canStartVisit && appointment.status === "RESERVED";
                      const canStartAppointment = canStartVisit && appointment.status === "CONFIRMED";
                      const canCloseAppointment = canClose && appointment.status === "IN_ATTENTION";
                      const canCheckinAppointment =
                        canCheckIn && appointment.status !== "CANCELLED" && appointment.status !== "CLOSED";

                      return (
                        <div key={appointment.id} className="rounded-lg border border-slate-200 bg-slate-50 p-2">
                          <div className="flex items-center justify-between gap-2">
                            <p className="text-xs font-semibold text-slate-900">
                              {formatTime(appointment.startsAt)} - {formatTime(appointment.endsAt)}
                            </p>
                            <span className="rounded border border-slate-300 bg-white px-1.5 py-0.5 text-[10px] font-semibold uppercase text-slate-700">
                              {toStatusLabel(appointment.status)}
                            </span>
                          </div>

                          <p className="mt-1 text-[11px] text-slate-700">
                            Sala: {roomNameById.get(appointment.roomId) ?? appointment.roomId}
                          </p>
                          <p className="text-[11px] text-slate-700">
                            Servicio: {serviceNameById.get(appointment.serviceId) ?? appointment.serviceId}
                          </p>
                          <p className="text-[11px] text-slate-700">
                            Cliente: {appointment.clientId ?? "N/D"}
                          </p>
                          <p className="text-[11px] text-slate-700">Mascota: {appointment.petId ?? "N/D"}</p>

                          {appointment.isOverbook ? (
                            <p className="mt-1 rounded bg-amber-100 px-1.5 py-1 text-[10px] font-semibold text-amber-900">
                              Sobre-cupo
                            </p>
                          ) : null}

                          {appointment.checkedInAt ? (
                            <p className="mt-1 text-[10px] text-emerald-700">
                              Check-in: {formatTime(appointment.checkedInAt)}
                            </p>
                          ) : null}

                          <div className="mt-2 flex flex-wrap gap-1">
                            {canEditAppointment ? (
                              <button
                                type="button"
                                onClick={() => openEditModal(appointment)}
                                className="rounded border border-slate-300 bg-white px-2 py-1 text-[10px] font-semibold hover:bg-slate-100"
                              >
                                Editar
                              </button>
                            ) : null}

                            {canCheckinAppointment ? (
                              <button
                                type="button"
                                disabled={actionBusyKey === `checkin:${appointment.id}`}
                                onClick={() => void executeTransition("checkin", appointment)}
                                className="rounded border border-slate-300 bg-white px-2 py-1 text-[10px] font-semibold hover:bg-slate-100 disabled:opacity-60"
                              >
                                Check-in
                              </button>
                            ) : null}

                            {canConfirmAppointment ? (
                              <button
                                type="button"
                                disabled={actionBusyKey === `confirm:${appointment.id}`}
                                onClick={() => void executeTransition("confirm", appointment)}
                                className="rounded border border-slate-300 bg-white px-2 py-1 text-[10px] font-semibold hover:bg-slate-100 disabled:opacity-60"
                              >
                                Confirmar
                              </button>
                            ) : null}

                            {canStartAppointment ? (
                              <button
                                type="button"
                                disabled={actionBusyKey === `start:${appointment.id}`}
                                onClick={() => void executeTransition("start", appointment)}
                                className="rounded border border-slate-300 bg-white px-2 py-1 text-[10px] font-semibold hover:bg-slate-100 disabled:opacity-60"
                              >
                                Iniciar atencion
                              </button>
                            ) : null}

                            {canCloseAppointment ? (
                              <button
                                type="button"
                                disabled={actionBusyKey === `close:${appointment.id}`}
                                onClick={() => void executeTransition("close", appointment)}
                                className="rounded border border-slate-300 bg-white px-2 py-1 text-[10px] font-semibold hover:bg-slate-100 disabled:opacity-60"
                              >
                                Cerrar
                              </button>
                            ) : null}

                            {canCancelAppointment ? (
                              <button
                                type="button"
                                disabled={actionBusyKey === `cancel:${appointment.id}`}
                                onClick={() => void onCancelClicked(appointment)}
                                className="rounded border border-rose-300 bg-rose-50 px-2 py-1 text-[10px] font-semibold text-rose-700 hover:bg-rose-100 disabled:opacity-60"
                              >
                                Cancelar
                              </button>
                            ) : null}
                          </div>
                        </div>
                      );
                    })}

                    {dayBlocks.length === 0 && dayAppointments.length === 0 ? (
                      <div className="rounded-lg border border-dashed border-slate-200 bg-slate-50 px-2 py-3 text-center text-xs text-slate-500">
                        Sin actividad para este dia
                      </div>
                    ) : null}
                  </div>
                </article>
              );
            })}
          </section>
        ) : null}
      </div>

      {isAppointmentModalOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
          <div className="w-full max-w-3xl rounded-xl border border-slate-200 bg-white p-5 shadow-lg">
            <h3 className="text-lg font-bold">
              {appointmentModalMode === "create" ? "Crear cita" : "Editar cita"}
            </h3>
            {appointmentModalMode === "edit" ? (
              <p className="mt-1 text-xs text-slate-500">
                El contrato backend de edicion permite actualizar solo sala y fecha/hora de inicio.
              </p>
            ) : null}

            <form onSubmit={onSubmitAppointment} className="mt-4 space-y-4">
              <div className="grid gap-3 md:grid-cols-2">
                <label className="space-y-1 text-sm">
                  <span className="font-semibold text-slate-700">Sala</span>
                  <select
                    value={formRoomId}
                    onChange={(event) => setFormRoomId(event.target.value)}
                    className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    required
                  >
                    <option value="">Selecciona sala</option>
                    {rooms.map((room) => (
                      <option key={room.id} value={room.id}>
                        {room.name}
                      </option>
                    ))}
                  </select>
                </label>

                {appointmentModalMode === "create" ? (
                  <label className="space-y-1 text-sm">
                    <span className="font-semibold text-slate-700">Servicio</span>
                    <select
                      value={formServiceId}
                      onChange={(event) => setFormServiceId(event.target.value)}
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
                  </label>
                ) : (
                  <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600">
                    Servicio: {(serviceNameById.get(formServiceId) ?? formServiceId) || "N/D"}
                  </div>
                )}
              </div>

              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Fecha y hora de inicio</span>
                <input
                  type="datetime-local"
                  value={formStartsAt}
                  onChange={(event) => setFormStartsAt(event.target.value)}
                  className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  required
                />
              </label>

              {appointmentModalMode === "create" ? (
                <div className="space-y-3 rounded-lg border border-slate-200 bg-slate-50 p-3">
                  <h4 className="text-sm font-bold text-slate-700">Cliente y mascota</h4>
                  <div className="grid gap-2 md:grid-cols-[1fr_auto]">
                    <input
                      type="text"
                      value={clientSearchQuery}
                      onChange={(event) => setClientSearchQuery(event.target.value)}
                      placeholder="Buscar cliente por nombre/telefono/identificacion"
                      className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm"
                    />
                    <button
                      type="button"
                      disabled={isSearchingClients}
                      onClick={() => void onSearchClients()}
                      className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100 disabled:opacity-60"
                    >
                      {isSearchingClients ? "Buscando..." : "Buscar"}
                    </button>
                  </div>

                  <div className="grid gap-3 md:grid-cols-2">
                    <label className="space-y-1 text-sm">
                      <span className="font-semibold text-slate-700">Cliente</span>
                      <select
                        value={formClientId}
                        onChange={(event) => {
                          const nextClientId = event.target.value;
                          setFormClientId(nextClientId);
                          setFormPetId("");
                          if (!nextClientId) {
                            setClientPets([]);
                            return;
                          }
                          void loadPetsForClient(nextClientId);
                        }}
                        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                        required
                      >
                        <option value="">Selecciona cliente</option>
                        {clientSearchResults.map((client) => (
                          <option key={client.id} value={client.id}>
                            {client.fullName} ({client.identification ?? "sin id"})
                          </option>
                        ))}
                      </select>
                    </label>

                    <label className="space-y-1 text-sm">
                      <span className="font-semibold text-slate-700">Mascota</span>
                      <select
                        value={formPetId}
                        onChange={(event) => setFormPetId(event.target.value)}
                        className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                        disabled={!formClientId || isLoadingPets}
                        required
                      >
                        <option value="">Selecciona mascota</option>
                        {clientPets.map((pet) => (
                          <option key={pet.id} value={pet.id}>
                            {pet.name} ({pet.internalCode})
                          </option>
                        ))}
                      </select>
                    </label>
                  </div>

                  {selectedClient ? (
                    <p className="text-xs text-slate-500">
                      Cliente seleccionado: <span className="font-semibold">{selectedClient.fullName}</span>
                    </p>
                  ) : null}
                </div>
              ) : null}

              {appointmentModalMode === "create" ? (
                <div className="grid gap-3 md:grid-cols-2">
                  <label className="space-y-1 text-sm">
                    <span className="font-semibold text-slate-700">Motivo (opcional)</span>
                    <input
                      type="text"
                      value={formReason}
                      onChange={(event) => setFormReason(event.target.value)}
                      className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                      placeholder="Ej: control vacunal"
                    />
                  </label>

                  <label className="space-y-1 text-sm">
                    <span className="font-semibold text-slate-700">Notas (opcional)</span>
                    <input
                      type="text"
                      value={formNotes}
                      onChange={(event) => setFormNotes(event.target.value)}
                      className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                    />
                  </label>
                </div>
              ) : null}

              {appointmentFormError ? (
                <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {appointmentFormError}
                </p>
              ) : null}

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={closeAppointmentModal}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100"
                >
                  Cerrar
                </button>
                <button
                  type="submit"
                  disabled={isSubmittingAppointment}
                  className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmittingAppointment
                    ? "Guardando..."
                    : appointmentModalMode === "create"
                      ? "Crear cita"
                      : "Guardar cambios"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}

      {reasonModalMode ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 px-4 py-6">
          <div className="w-full max-w-lg rounded-xl border border-slate-200 bg-white p-5 shadow-lg">
            <h3 className="text-lg font-bold">{reasonModalTitle}</h3>
            <p className="mt-1 text-sm text-slate-600">Este cambio requiere un motivo (minimo 10 caracteres).</p>

            <form onSubmit={onSubmitReason} className="mt-4 space-y-4">
              <label className="space-y-1 text-sm">
                <span className="font-semibold text-slate-700">Motivo</span>
                <textarea
                  value={reasonInput}
                  onChange={(event) => setReasonInput(event.target.value)}
                  className="min-h-[110px] w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
                  placeholder="Describe el motivo"
                  required
                />
              </label>

              {reasonModalError ? (
                <p className="rounded-lg border border-rose-200 bg-rose-50 px-3 py-2 text-sm text-rose-700">
                  {reasonModalError}
                </p>
              ) : null}

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={closeReasonModal}
                  className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-semibold hover:bg-slate-100"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={isSubmittingReason}
                  className="rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSubmittingReason ? "Enviando..." : "Confirmar"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </AppShell>
  );
}
