import type { GenderRequirement, SessionStatus, SwapStatus, VersionKind } from "../types/stage";

export const CLEANUP_MINUTES_DEFAULT = 20;

export const GENDER_LABELS: Record<GenderRequirement, string> = {
  MALE: "男性",
  FEMALE: "女性",
  ANY: "不限",
};

export const GENDER_TAGS: Record<string, "" | "primary" | "success" | "warning" | "danger" | "info"> = {
  MALE: "primary",
  FEMALE: "danger",
  ANY: "info",
};

export const SESSION_STATUS_LABELS: Record<SessionStatus, string> = {
  STAGING: "待开演·入座中",
  CONFIRMED: "已确认·角色定版",
  CANCELLED: "已取消",
};

export const SESSION_STATUS_TAGS: Record<SessionStatus, "" | "primary" | "success" | "warning" | "danger" | "info"> = {
  STAGING: "warning",
  CONFIRMED: "success",
  CANCELLED: "info",
};

export const VERSION_KIND_LABELS: Record<VersionKind, string> = {
  DRAW: "抽签",
  CONFIRM: "确认定版",
  MANUAL: "店长调整",
};

export const VERSION_KIND_TAGS: Record<VersionKind, "" | "primary" | "success" | "warning" | "danger" | "info"> = {
  DRAW: "primary",
  CONFIRM: "success",
  MANUAL: "warning",
};

export const SWAP_STATUS_LABELS: Record<SwapStatus, string> = {
  PENDING: "待店长确认",
  APPROVED: "已批准",
  REJECTED: "已驳回",
};

export const SWAP_STATUS_TAGS: Record<SwapStatus, "" | "primary" | "success" | "warning" | "danger" | "info"> = {
  PENDING: "warning",
  APPROVED: "success",
  REJECTED: "info",
};

/** "HH:mm:ss" 或 ISO 日期时间 -> HH:mm。 */
export function toHM(value: string | null | undefined): string {
  if (!value) {
    return "--:--";
  }
  const time = value.includes("T") ? value.slice(11) : value;
  return time.slice(0, 5);
}

/** 后端 ISO 时间 -> 本地可读字符串。 */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return "—";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}`;
}
