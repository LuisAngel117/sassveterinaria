import { apiRequestWithSession } from "@/lib/api/client";

export type Room = {
  id: string;
  branchId: string;
  name: string;
  isActive: boolean;
  createdAt: string;
};

export type Service = {
  id: string;
  branchId: string;
  name: string;
  durationMinutes: number;
  priceBase: string;
  isActive: boolean;
  createdAt: string;
};

export type Appointment = {
  id: string;
  branchId: string;
  roomId: string;
  serviceId: string;
  startsAt: string;
  endsAt: string;
  status: string;
  checkedInAt: string | null;
  isOverbook: boolean;
  overbookReason: string | null;
  reason: string | null;
  notes: string | null;
  clientId: string | null;
  petId: string | null;
  veterinarianId: string | null;
  createdAt: string;
};

export type CreateAppointmentInput = {
  roomId: string;
  serviceId: string;
  startsAt: string;
  reason?: string;
  notes?: string;
  clientId?: string;
  petId?: string;
  veterinarianId?: string;
  overbookReason?: string;
};

export type UpdateAppointmentInput = {
  roomId: string;
  startsAt: string;
  overbookReason?: string;
};

export type AppointmentListQuery = {
  from?: string;
  to?: string;
  roomId?: string;
  status?: string;
};

export type RoomBlock = {
  id: string;
  branchId: string;
  roomId: string;
  startsAt: string;
  endsAt: string;
  reason: string;
  createdBy: string;
  createdAt: string;
};

export type CreateRoomBlockInput = {
  roomId: string;
  startsAt: string;
  endsAt: string;
  reason: string;
};

function toQueryString(query: Record<string, string | undefined>): string {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (!value) {
      return;
    }
    params.set(key, value);
  });
  const raw = params.toString();
  return raw ? `?${raw}` : "";
}

export async function listRooms(): Promise<Room[]> {
  return apiRequestWithSession<Room[]>("/api/v1/rooms");
}

export async function listServices(active = true): Promise<Service[]> {
  const query = toQueryString({ active: active ? "true" : undefined });
  return apiRequestWithSession<Service[]>(`/api/v1/services${query}`);
}

export async function listAppointments(query: AppointmentListQuery): Promise<Appointment[]> {
  const qs = toQueryString({
    from: query.from,
    to: query.to,
    roomId: query.roomId,
    status: query.status,
  });
  return apiRequestWithSession<Appointment[]>(`/api/v1/appointments${qs}`);
}

export async function createAppointment(input: CreateAppointmentInput): Promise<Appointment> {
  return apiRequestWithSession<Appointment>("/api/v1/appointments", {
    method: "POST",
    body: input,
  });
}

export async function updateAppointment(
  appointmentId: string,
  input: UpdateAppointmentInput,
): Promise<Appointment> {
  return apiRequestWithSession<Appointment>(`/api/v1/appointments/${appointmentId}`, {
    method: "PATCH",
    body: input,
  });
}

export async function checkInAppointment(appointmentId: string): Promise<Appointment> {
  return apiRequestWithSession<Appointment>(`/api/v1/appointments/${appointmentId}/checkin`, {
    method: "POST",
  });
}

export async function confirmAppointment(appointmentId: string): Promise<Appointment> {
  return apiRequestWithSession<Appointment>(`/api/v1/appointments/${appointmentId}/confirm`, {
    method: "POST",
  });
}

export async function startAppointment(appointmentId: string): Promise<Appointment> {
  return apiRequestWithSession<Appointment>(`/api/v1/appointments/${appointmentId}/start`, {
    method: "POST",
  });
}

export async function closeAppointment(appointmentId: string): Promise<Appointment> {
  return apiRequestWithSession<Appointment>(`/api/v1/appointments/${appointmentId}/close`, {
    method: "POST",
  });
}

export async function cancelAppointment(
  appointmentId: string,
  reason?: string,
): Promise<Appointment> {
  return apiRequestWithSession<Appointment>(`/api/v1/appointments/${appointmentId}/cancel`, {
    method: "POST",
    body: reason ? { reason } : undefined,
  });
}

export async function listRoomBlocks(from: string, to: string, roomId?: string): Promise<RoomBlock[]> {
  const qs = toQueryString({ from, to, roomId });
  return apiRequestWithSession<RoomBlock[]>(`/api/v1/room-blocks${qs}`);
}

export async function createRoomBlock(input: CreateRoomBlockInput): Promise<RoomBlock> {
  return apiRequestWithSession<RoomBlock>("/api/v1/room-blocks", {
    method: "POST",
    body: input,
  });
}
