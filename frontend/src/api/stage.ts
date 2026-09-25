import { API_BASE_URL } from "../constants/app";
import type {
  ConflictErrorBody,
  HostView,
  PlayerView,
  ScriptView,
  SessionDetailView,
  SessionView,
} from "../types/stage";

/** 档期冲突（HTTP 409），携带冲突场次。 */
export class ConflictError extends Error {
  conflicts: ConflictErrorBody["conflicts"];

  constructor(body: ConflictErrorBody) {
    super(body.message);
    this.conflicts = body.conflicts;
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { Accept: "application/json", ...(options?.body ? { "Content-Type": "application/json" } : {}) },
    ...options,
  });

  if (response.status === 409) {
    const body = (await response.json()) as ConflictErrorBody;
    throw new ConflictError(body);
  }
  if (!response.ok) {
    let message = `请求失败：${response.status}`;
    try {
      const body = await response.json();
      if (body?.message) {
        message = body.message;
      }
    } catch {
      // 忽略非 JSON 错误体
    }
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}

export function fetchScripts(): Promise<ScriptView[]> {
  return request<ScriptView[]>("/stage/scripts");
}

export function fetchHosts(): Promise<HostView[]> {
  return request<HostView[]>("/stage/hosts");
}

export function fetchPlayers(): Promise<PlayerView[]> {
  return request<PlayerView[]>("/stage/players");
}

export function fetchSessions(): Promise<SessionView[]> {
  return request<SessionView[]>("/stage/sessions");
}

export function fetchSession(id: number): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/sessions/${id}`);
}

export interface CreateSessionPayload {
  scriptId: number;
  hostId: number;
  date: string;
  startTime: string;
  cleanupMinutes?: number;
  note?: string;
}

export function createSession(payload: CreateSessionPayload): Promise<SessionDetailView> {
  return request<SessionDetailView>("/stage/sessions", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function addRoster(sessionId: number, playerId: number): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/sessions/${sessionId}/roster`, {
    method: "POST",
    body: JSON.stringify({ playerId }),
  });
}

export function removeRoster(sessionId: number, playerId: number): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/sessions/${sessionId}/roster/${playerId}`, {
    method: "DELETE",
  });
}

export function drawSeats(sessionId: number, reDraw: boolean): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/sessions/${sessionId}/draw`, {
    method: "POST",
    body: JSON.stringify({ reDraw, operator: "店长" }),
  });
}

export function confirmSession(sessionId: number): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/sessions/${sessionId}/confirm`, {
    method: "POST",
    body: JSON.stringify({ operator: "店长" }),
  });
}

export function requestSwap(
  sessionId: number,
  roleId: number,
  toPlayerId: number,
  reason: string,
): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/sessions/${sessionId}/swaps`, {
    method: "POST",
    body: JSON.stringify({ roleId, toPlayerId, reason, operator: "店长" }),
  });
}

export function approveSwap(requestId: number, reviewNote: string): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/swaps/${requestId}/approve`, {
    method: "POST",
    body: JSON.stringify({ reviewer: "店长", reviewNote }),
  });
}

export function rejectSwap(requestId: number, reviewNote: string): Promise<SessionDetailView> {
  return request<SessionDetailView>(`/stage/swaps/${requestId}/reject`, {
    method: "POST",
    body: JSON.stringify({ reviewer: "店长", reviewNote }),
  });
}
