import type { GenderCode } from "../types/stage";

export function genderLabel(gender: GenderCode | string | null | undefined): string {
  switch (gender) {
    case "MALE":
      return "男";
    case "FEMALE":
      return "女";
    case "ANY":
      return "不限";
    default:
      return "—";
  }
}

export function genderTagType(gender: GenderCode | string | null | undefined): "primary" | "danger" | "info" {
  if (gender === "MALE") return "primary";
  if (gender === "FEMALE") return "danger";
  return "info";
}

export function stageStatusLabel(status: string): string {
  switch (status) {
    case "SCHEDULED":
      return "已排期";
    case "CONFIRMED":
      return "已开演";
    default:
      return "草稿";
  }
}

export function stageStatusType(status: string): "warning" | "success" | "info" {
  if (status === "CONFIRMED") return "success";
  if (status === "SCHEDULED") return "warning";
  return "info";
}

export function triggerLabel(trigger: string): string {
  switch (trigger) {
    case "DRAW":
      return "首次抽签";
    case "REDRAW":
      return "重新抽签";
    case "CHANGE":
      return "换角调整";
    default:
      return trigger;
  }
}

export function triggerTagType(trigger: string): "primary" | "warning" | "success" {
  if (trigger === "DRAW") return "primary";
  if (trigger === "REDRAW") return "warning";
  return "success";
}

export function changeTypeLabel(type: string): string {
  return type === "SWAP" ? "玩家互换" : "候补顶替";
}

export function changeStatusLabel(status: string): string {
  switch (status) {
    case "PENDING":
      return "待店长确认";
    case "APPROVED":
      return "已批准";
    case "REJECTED":
      return "已驳回";
    default:
      return status;
  }
}

export function changeStatusType(status: string): "warning" | "success" | "danger" {
  if (status === "PENDING") return "warning";
  if (status === "APPROVED") return "success";
  return "danger";
}

function pad(value: number): string {
  return value < 10 ? `0${value}` : String(value);
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function formatTimeRange(start: string, end: string): string {
  return `${formatDateTime(start)} ~ ${formatDateTime(end).slice(11)}`;
}
