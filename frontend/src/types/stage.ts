export type GenderCode = "MALE" | "FEMALE" | "ANY";

export interface Script {
  id: number;
  name: string;
  genre: string;
  difficulty: string;
  durationMinutes: number;
  playerCount: number;
  description: string;
}

export interface Host {
  id: number;
  name: string;
  title: string;
  phone: string;
}

export interface RoleInfo {
  id: number;
  name: string;
  gender: GenderCode;
  profile: string;
}

export interface Player {
  id: number;
  playerName: string;
  gender: "MALE" | "FEMALE";
  waiting: boolean;
}

export interface Seat {
  roleId: number;
  roleName: string;
  roleGender: GenderCode;
  playerId: number;
  playerName: string;
  playerGender: GenderCode;
}

export interface DrawVersion {
  versionNo: number;
  drawTrigger: "DRAW" | "REDRAW" | "CHANGE";
  note: string;
  createdAt: string;
  seats: Seat[];
}

export interface RoleChange {
  id: number;
  changeType: "SWAP" | "REPLACE";
  status: "PENDING" | "APPROVED" | "REJECTED";
  roleId: number;
  roleName: string;
  fromPlayerId: number;
  fromPlayerName: string;
  toPlayerId: number | null;
  toPlayerName: string | null;
  waitingPlayerId: number | null;
  waitingPlayerName: string | null;
  versionNo: number | null;
  reason: string | null;
  managerNote: string | null;
  createdAt: string;
  decidedAt: string | null;
}

export interface StageSummary {
  id: number;
  scriptName: string;
  genre: string;
  hostName: string;
  startAt: string;
  endAt: string;
  clearEndAt: string;
  status: "DRAFT" | "SCHEDULED" | "CONFIRMED";
  requiredCount: number;
  activeCount: number;
  rosterReady: boolean;
  currentVersionNo: number | null;
}

export interface StageDetail extends StageSummary {
  scriptId: number;
  hostId: number;
  difficulty: string;
  durationMinutes: number;
  hostTitle: string;
  confirmedAt: string | null;
  roles: RoleInfo[];
  players: Player[];
  versions: DrawVersion[];
  changes: RoleChange[];
}

export interface ConflictStage {
  id: number;
  scriptName: string;
  hostName: string;
  startAt: string;
  endAt: string;
  clearEndAt: string;
  status: string;
}

export interface ApiConflictError {
  message: string;
  conflicts: ConflictStage[];
}
