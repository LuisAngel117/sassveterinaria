import { readSession } from "@/lib/session/store";

const DEFAULT_API_BASE_URL = "http://localhost:8080";

type ApiRequestOptions = {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  body?: unknown;
  accessToken?: string;
  branchId?: string;
  headers?: Record<string, string>;
};

type ProblemDetails = {
  title?: string;
  detail?: string;
  status?: number;
  errorCode?: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly detail: string | null;
  readonly errorCode: string | null;

  constructor(
    message: string,
    options: { status: number; detail: string | null; errorCode: string | null },
  ) {
    super(message);
    this.name = "ApiError";
    this.status = options.status;
    this.detail = options.detail;
    this.errorCode = options.errorCode;
  }
}

function getApiBaseUrl(): string {
  const raw = process.env.NEXT_PUBLIC_API_BASE_URL?.trim();
  if (!raw) {
    return DEFAULT_API_BASE_URL;
  }
  return raw.endsWith("/") ? raw.slice(0, -1) : raw;
}

function buildUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${getApiBaseUrl()}${normalizedPath}`;
}

function toUserMessage(status: number, details: ProblemDetails): string {
  if (details.detail) {
    return details.detail;
  }
  if (status === 401) {
    return "Sesion invalida o expirada. Inicia sesion nuevamente.";
  }
  if (status === 403) {
    return "No tienes permisos para esta accion.";
  }
  if (status === 400) {
    return "Solicitud invalida. Verifica los datos enviados.";
  }
  return details.title ?? "No fue posible completar la solicitud.";
}

async function parseProblemDetails(response: Response): Promise<ProblemDetails> {
  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return {};
  }

  try {
    const payload: unknown = await response.json();
    if (!payload || typeof payload !== "object") {
      return {};
    }
    const data = payload as ProblemDetails;
    return {
      title: data.title ?? undefined,
      detail: data.detail ?? undefined,
      status: data.status ?? undefined,
      errorCode: data.errorCode ?? undefined,
    };
  } catch {
    return {};
  }
}

export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const headers: Record<string, string> = {
    ...(options.headers ?? {}),
  };

  if (options.accessToken) {
    headers.Authorization = `Bearer ${options.accessToken}`;
  }
  if (options.branchId) {
    headers["X-Branch-Id"] = options.branchId;
  }
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(buildUrl(path), {
    method: options.method ?? "GET",
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    const details = await parseProblemDetails(response);
    throw new ApiError(toUserMessage(response.status, details), {
      status: response.status,
      detail: details.detail ?? null,
      errorCode: details.errorCode ?? null,
    });
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export async function apiRequestWithSession<T>(
  path: string,
  options: Omit<ApiRequestOptions, "accessToken" | "branchId"> = {},
): Promise<T> {
  const session = readSession();
  return apiRequest<T>(path, {
    ...options,
    accessToken: session?.accessToken,
    branchId: session?.branchId ?? undefined,
  });
}
