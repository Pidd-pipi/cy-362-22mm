import { API_BASE_URL } from "../constants/app";
import type {
  ApiConflictError,
  ConflictStage,
  Host,
  Player,
  RoleChange,
  RoleInfo,
  Script,
  StageDetail,
  StageSummary,
} from "../types/stage";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    ...init,
  });
  if (!response.ok) {
    let payload: unknown = null;
    try {
      payload = await response.json();
    } catch {
      payload = null;
    }
    if (response.status === 409 && payload && typeof payload === "object" && "conflicts" in payload) {
      throw new ScheduleConflictError(payload as ApiConflictError);
    }
    const message =
      payload && typeof payload === "object" && "message" in payload
        ? String((payload as { message: unknown }).message)
        : `请求失败（${response.status}）`;
    throw new Error(message);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export class ScheduleConflictError extends Error {
  conflicts: ConflictStage[];

  constructor(body: ApiConflictError) {
    super(body.message);
    this.name = "ScheduleConflictError";
    this.conflicts = body.conflicts;
  }
}

export function fetchScripts(): Promise<Script[]> {
  return request<Script[]>("/scripts");
}

export function fetchScriptRoles(scriptId: number): Promise<RoleInfo[]> {
  return request<RoleInfo[]>(`/scripts/${scriptId}/roles`);
}

export function fetchHosts(): Promise<Host[]> {
  return request<Host[]>("/hosts");
}

export function fetchStages(): Promise<StageSummary[]> {
  return request<StageSummary[]>("/stages");
}

export function fetchStageDetail(id: number): Promise<StageDetail> {
  return request<StageDetail>(`/stages/${id}`);
}

export function createStage(input: {
  scriptId: number;
  hostId: number;
  startAt: string;
}): Promise<StageSummary> {
  return request<StageSummary>("/stages", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function addPlayer(
  stageId: number,
  input: { playerName: string; gender: "MALE" | "FEMALE" }
): Promise<Player> {
  return request<Player>(`/stages/${stageId}/players`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function removePlayer(stageId: number, playerId: number): Promise<void> {
  return request<void>(`/stages/${stageId}/players/${playerId}`, { method: "DELETE" });
}

export function drawSeats(stageId: number, note?: string): Promise<StageDetail> {
  return request<StageDetail>(`/stages/${stageId}/draw`, {
    method: "POST",
    body: JSON.stringify({ note: note ?? "" }),
  });
}

export function redrawSeats(stageId: number, note?: string): Promise<StageDetail> {
  return request<StageDetail>(`/stages/${stageId}/redraw`, {
    method: "POST",
    body: JSON.stringify({ note: note ?? "" }),
  });
}

export function confirmStage(stageId: number): Promise<StageDetail> {
  return request<StageDetail>(`/stages/${stageId}/confirm`, { method: "POST" });
}

export function createChangeRequest(
  stageId: number,
  input: {
    changeType: "SWAP" | "REPLACE";
    roleId: number;
    toPlayerId?: number;
    waitingPlayerId?: number;
    reason?: string;
  }
): Promise<RoleChange> {
  return request<RoleChange>(`/stages/${stageId}/changes`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function decideChange(
  stageId: number,
  changeId: number,
  input: { decision: "APPROVED" | "REJECTED"; managerNote?: string }
): Promise<StageDetail> {
  return request<StageDetail>(`/stages/${stageId}/changes/${changeId}/decision`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}
