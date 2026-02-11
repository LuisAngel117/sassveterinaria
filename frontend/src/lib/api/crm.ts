import { apiRequestWithSession } from "@/lib/api/client";

export type Client = {
  id: string;
  branchId: string;
  fullName: string;
  identification: string | null;
  phone: string | null;
  email: string | null;
  address: string | null;
  notes: string | null;
  createdAt: string;
};

export type Pet = {
  id: string;
  branchId: string;
  clientId: string;
  internalCode: string;
  name: string;
  species: string | null;
  breed: string | null;
  sex: string | null;
  birthDate: string | null;
  weightKg: number | null;
  neutered: boolean | null;
  alerts: string | null;
  history: string | null;
  createdAt: string;
};

export type PagedResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type ClientCreateInput = {
  fullName: string;
  identification?: string;
  phone?: string;
  email?: string;
  address?: string;
  notes?: string;
};

export type ClientPatchInput = {
  fullName?: string;
  identification?: string;
  phone?: string;
  email?: string;
  address?: string;
  notes?: string;
};

export type PetCreateInput = {
  internalCode: string;
  name: string;
  species: string;
  breed?: string;
  sex?: string;
  birthDate?: string;
  weightKg?: number;
  neutered?: boolean;
  alerts?: string;
  history?: string;
};

export type PetPatchInput = {
  internalCode?: string;
  name?: string;
  species?: string;
  breed?: string;
  sex?: string;
  birthDate?: string;
  weightKg?: number;
  neutered?: boolean;
  alerts?: string;
  history?: string;
};

export async function searchClients(
  query: string,
  page = 0,
  size = 20,
): Promise<PagedResponse<Client>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  if (query.trim()) {
    params.set("q", query.trim());
  }
  return apiRequestWithSession<PagedResponse<Client>>(`/api/v1/clients?${params.toString()}`);
}

export async function getClient(clientId: string): Promise<Client> {
  return apiRequestWithSession<Client>(`/api/v1/clients/${clientId}`);
}

export async function createClient(payload: ClientCreateInput): Promise<Client> {
  return apiRequestWithSession<Client>("/api/v1/clients", {
    method: "POST",
    body: payload,
  });
}

export async function updateClient(clientId: string, payload: ClientPatchInput): Promise<Client> {
  return apiRequestWithSession<Client>(`/api/v1/clients/${clientId}`, {
    method: "PATCH",
    body: payload,
  });
}

export async function listClientPets(clientId: string): Promise<Pet[]> {
  return apiRequestWithSession<Pet[]>(`/api/v1/clients/${clientId}/pets`);
}

export async function getPet(petId: string): Promise<Pet> {
  return apiRequestWithSession<Pet>(`/api/v1/pets/${petId}`);
}

export async function createPet(clientId: string, payload: PetCreateInput): Promise<Pet> {
  return apiRequestWithSession<Pet>(`/api/v1/clients/${clientId}/pets`, {
    method: "POST",
    body: payload,
  });
}

export async function updatePet(petId: string, payload: PetPatchInput): Promise<Pet> {
  return apiRequestWithSession<Pet>(`/api/v1/pets/${petId}`, {
    method: "PATCH",
    body: payload,
  });
}
