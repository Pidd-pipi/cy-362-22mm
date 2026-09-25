// 角色开演台相关类型（与后端 dto 对齐）

export type GenderRequirement = "MALE" | "FEMALE" | "ANY";
export type Gender = "MALE" | "FEMALE";
export type SessionStatus = "STAGING" | "CONFIRMED" | "CANCELLED";
export type VersionKind = "DRAW" | "CONFIRM" | "MANUAL";
export type SwapStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface RoleView {
  id: number;
  scriptId: number;
  name: string;
  genderRequirement: GenderRequirement;
  description: string;
  positionNo: number;
}

export interface ScriptView {
  id: number;
  name: string;
  genre: string;
  difficulty: string;
  durationMinutes: number;
  playerCount: number;
  summary: string;
  roles: RoleView[];
}

export interface HostView {
  id: number;
  name: string;
  title: string;
  phone: string;
}

export interface PlayerView {
  id: number;
  name: string;
  gender: Gender;
  phone: string;
}

export interface RosterPlayerView {
  playerId: number;
  name: string;
  gender: Gender;
  phone: string;
  joinedOrder: number;
}

export interface SeatView {
  roleId: number;
  roleName: string;
  roleGenderRequirement: GenderRequirement;
  positionNo: number;
  playerId: number | null;
  playerName: string | null;
  playerGender: Gender | null;
}

export interface SessionView {
  id: number;
  scriptId: number;
  scriptName: string;
  hostId: number;
  hostName: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  startAt: string;
  endAt: string;
  cleanupMinutes: number;
  requiredPlayers: number;
  rosterCount: number;
  seatedCount: number;
  roleCount: number;
  full: boolean;
  status: SessionStatus;
  confirmedAt: string | null;
  note: string | null;
  createdAt: string;
  seats: SeatView[];
}

export interface VersionView {
  id: number;
  versionNo: number;
  kind: VersionKind;
  changeNote: string;
  createdBy: string;
  createdAt: string;
  seats: SeatView[];
}

export interface ConflictSessionView {
  sessionId: number;
  scriptName: string;
  hostName: string;
  sessionDate: string;
  startTime: string;
  endTime: string;
  status: SessionStatus;
  startAt: string;
  endAt: string;
}

export interface SwapRequestView {
  id: number;
  sessionId: number;
  roleId: number;
  roleName: string;
  fromPlayerId: number | null;
  fromPlayerName: string | null;
  toPlayerId: number;
  toPlayerName: string;
  reason: string | null;
  status: SwapStatus;
  reviewNote: string | null;
  createdBy: string | null;
  reviewedBy: string | null;
  createdAt: string;
  reviewedAt: string | null;
}

export interface SessionDetailView {
  session: SessionView;
  roles: RoleView[];
  roster: RosterPlayerView[];
  unseatedRoster: RosterPlayerView[];
  currentVersion: VersionView | null;
  history: VersionView[];
  swapRequests: SwapRequestView[];
}

/** 档期冲突错误体（HTTP 409）。 */
export interface ConflictErrorBody {
  message: string;
  conflicts: ConflictSessionView[];
}
