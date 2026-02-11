import { apiRequestWithSession } from "@/lib/api/client";
import { readSession } from "@/lib/session/store";

export type Visit = {
  id: string;
  branchId: string;
  petId: string;
  serviceId: string;
  appointmentId: string | null;
  status: string;
  sReason: string | null;
  sAnamnesis: string | null;
  oWeightKg: number | null;
  oTemperatureC: number | null;
  oFindings: string | null;
  aDiagnosis: string | null;
  aSeverity: string | null;
  pTreatment: string | null;
  pInstructions: string | null;
  pFollowupAt: string | null;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
};

export type VisitCreateInput = {
  petId: string;
  serviceId: string;
  appointmentId?: string;
  templateId?: string;
  sReason?: string;
  sAnamnesis?: string;
  oWeightKg?: number;
  oTemperatureC?: number;
  oFindings?: string;
  aDiagnosis?: string;
  aSeverity?: string;
  pTreatment?: string;
  pInstructions?: string;
  pFollowupAt?: string;
};

export type VisitPatchInput = {
  sReason?: string;
  sAnamnesis?: string;
  oWeightKg?: number;
  oTemperatureC?: number;
  oFindings?: string;
  aDiagnosis?: string;
  aSeverity?: string;
  pTreatment?: string;
  pInstructions?: string;
  pFollowupAt?: string;
};

export type VisitListQuery = {
  status?: string;
  from?: string;
  to?: string;
};

export type SoapTemplate = {
  id: string;
  branchId: string;
  serviceId: string;
  name: string;
  sReason: string | null;
  sAnamnesis: string | null;
  oFindings: string | null;
  aDiagnosis: string | null;
  pTreatment: string | null;
  pInstructions: string | null;
  isActive: boolean;
  createdAt: string;
};

export type Prescription = {
  id: string;
  branchId: string;
  visitId: string;
  medication: string;
  dose: string;
  unit: string;
  frequency: string;
  duration: string;
  route: string;
  notes: string | null;
  createdAt: string;
};

export type PrescriptionCreateInput = {
  medication: string;
  dose: string;
  unit: string;
  frequency: string;
  duration: string;
  route: string;
  notes?: string;
};

export type VisitAttachment = {
  id: string;
  branchId: string;
  visitId: string;
  originalFilename: string;
  contentType: string;
  sizeBytes: number;
  createdBy: string;
  createdAt: string;
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

function getApiBaseUrl(): string {
  const fallback = "http://localhost:8080";
  const raw = process.env.NEXT_PUBLIC_API_BASE_URL?.trim();
  if (!raw) {
    return fallback;
  }
  return raw.endsWith("/") ? raw.slice(0, -1) : raw;
}

export async function listPetVisits(petId: string, query: VisitListQuery = {}): Promise<Visit[]> {
  const qs = toQueryString({
    status: query.status,
    from: query.from,
    to: query.to,
  });
  return apiRequestWithSession<Visit[]>(`/api/v1/pets/${petId}/visits${qs}`);
}

export async function createVisit(input: VisitCreateInput): Promise<Visit> {
  return apiRequestWithSession<Visit>("/api/v1/visits", {
    method: "POST",
    body: input,
  });
}

export async function getVisit(visitId: string): Promise<Visit> {
  return apiRequestWithSession<Visit>(`/api/v1/visits/${visitId}`);
}

export async function updateVisitSoap(visitId: string, input: VisitPatchInput): Promise<Visit> {
  return apiRequestWithSession<Visit>(`/api/v1/visits/${visitId}`, {
    method: "PATCH",
    body: input,
  });
}

export async function closeVisit(visitId: string): Promise<Visit> {
  return apiRequestWithSession<Visit>(`/api/v1/visits/${visitId}/close`, {
    method: "POST",
  });
}

export async function reopenVisit(visitId: string, reason: string): Promise<Visit> {
  return apiRequestWithSession<Visit>(`/api/v1/visits/${visitId}/reopen`, {
    method: "POST",
    body: { reason },
  });
}

export async function listSoapTemplates(serviceId: string): Promise<SoapTemplate[]> {
  const qs = toQueryString({ serviceId });
  return apiRequestWithSession<SoapTemplate[]>(`/api/v1/soap-templates${qs}`);
}

export async function addPrescription(
  visitId: string,
  input: PrescriptionCreateInput,
): Promise<Prescription> {
  return apiRequestWithSession<Prescription>(`/api/v1/visits/${visitId}/prescriptions`, {
    method: "POST",
    body: input,
  });
}

export async function listPrescriptions(visitId: string): Promise<Prescription[]> {
  return apiRequestWithSession<Prescription[]>(`/api/v1/visits/${visitId}/prescriptions`);
}

export async function uploadVisitAttachment(
  visitId: string,
  file: File,
): Promise<VisitAttachment> {
  const formData = new FormData();
  formData.set("file", file);
  return apiRequestWithSession<VisitAttachment>(`/api/v1/visits/${visitId}/attachments`, {
    method: "POST",
    body: formData,
  });
}

export async function listVisitAttachments(visitId: string): Promise<VisitAttachment[]> {
  return apiRequestWithSession<VisitAttachment[]>(`/api/v1/visits/${visitId}/attachments`);
}

export async function downloadVisitAttachment(
  attachmentId: string,
): Promise<{ blob: Blob; filename: string }> {
  const session = readSession();
  if (!session?.accessToken || !session.branchId) {
    throw new Error("Sesion invalida o sin sucursal seleccionada.");
  }

  const response = await fetch(`${getApiBaseUrl()}/api/v1/attachments/${attachmentId}/download`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${session.accessToken}`,
      "X-Branch-Id": session.branchId,
    },
  });

  if (!response.ok) {
    throw new Error("No fue posible descargar el adjunto.");
  }

  const blob = await response.blob();
  const disposition = response.headers.get("content-disposition") ?? "";
  const filenameMatch = disposition.match(/filename\*?=(?:UTF-8''|\"?)([^\";]+)/i);
  const filename = filenameMatch ? decodeURIComponent(filenameMatch[1]) : "adjunto";

  return { blob, filename };
}
