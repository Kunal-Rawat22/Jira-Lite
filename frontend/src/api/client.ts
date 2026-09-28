export type Envelope<T> = {
  message: string | null;
  code: string | null;
  status: "success" | "failed";
  data: T | null;
};

const GENERIC = "Something went wrong. Try again.";

export class ApiClientError extends Error {
  constructor(
    message: string,
    readonly code: string | null,
    readonly recognized: boolean,
  ) {
    super(message);
  }
}

export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = sessionStorage.getItem("jwt");
  const headers = new Headers(init.headers);
  headers.set("Content-Type", "application/json");
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  let response: Response;
  try {
    response = await fetch(path, { ...init, headers });
  } catch {
    throw new ApiClientError(GENERIC, null, false);
  }
  let body: Envelope<T>;
  try {
    body = (await response.json()) as Envelope<T>;
  } catch {
    throw new ApiClientError(GENERIC, null, false);
  }
  if (body.status !== "success") {
    const recognized = Boolean(body.code);
    throw new ApiClientError(recognized ? body.message || GENERIC : GENERIC, body.code, recognized);
  }
  return body.data as T;
}
