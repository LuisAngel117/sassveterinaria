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
  weightKg: string | null;
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

export async function listClientPets(clientId: string): Promise<Pet[]> {
  return apiRequestWithSession<Pet[]>(`/api/v1/clients/${clientId}/pets`);
}
