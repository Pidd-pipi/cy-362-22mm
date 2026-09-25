<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import { createChangeRequest, decideChange } from "../../api/stage";
import type { StageDetail } from "../../types/stage";
import {
  changeStatusLabel,
  changeStatusType,
  changeTypeLabel,
  formatDateTime,
  genderLabel,
} from "../../utils/stage-format";

const props = defineProps<{ stage: StageDetail }>();
const emit = defineEmits<{ (e: "refresh"): void }>();

const changeType = ref<"SWAP" | "REPLACE">("SWAP");
const roleId = ref<number | null>(null);
const toPlayerId = ref<number | null>(null);
const waitingPlayerId = ref<number | null>(null);
const reason = ref("");
const submitting = ref(false);
const decidingId = ref<number | null>(null);

const confirmed = computed(() => props.stage.status === "CONFIRMED");
const activePlayers = computed(() => props.stage.players.filter((player) => !player.waiting));
const waitingPlayers = computed(() => props.stage.players.filter((player) => player.waiting));
const currentVersion = computed(() => props.stage.versions[0] ?? null);
const pendingChanges = computed(() => props.stage.changes.filter((change) => change.status === "PENDING"));
const historyChanges = computed(() => props.stage.changes.filter((change) => change.status !== "PENDING"));

function currentPlayerOf(role: number): string {
  const seat = currentVersion.value?.seats.find((item) => item.roleId === role);
  return seat?.playerName ?? "";
}

function resetForm() {
  roleId.value = null;
  toPlayerId.value = null;
  waitingPlayerId.value = null;
  reason.value = "";
}

async function submitRequest() {
  if (roleId.value === null) {
    ElMessage.warning("请选择需要调整的角色");
    return;
  }
  if (changeType.value === "SWAP" && toPlayerId.value === null) {
    ElMessage.warning("请选择互换角色的另一位在场玩家");
    return;
  }
  if (changeType.value === "REPLACE" && waitingPlayerId.value === null) {
    ElMessage.warning("请选择顶替入座的候补玩家");
    return;
  }
  submitting.value = true;
  try {
    await createChangeRequest(props.stage.id, {
      changeType: changeType.value,
      roleId: roleId.value,
      toPlayerId: changeType.value === "SWAP" ? (toPlayerId.value ?? undefined) : undefined,
      waitingPlayerId: changeType.value === "REPLACE" ? (waitingPlayerId.value ?? undefined) : undefined,
      reason: reason.value.trim() || undefined,
    });
    ElMessage.success("换角申请已提交，等待店长确认");
    resetForm();
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "提交失败");
  } finally {
    submitting.value = false;
  }
}

async function decide(changeId: number, decision: "APPROVED" | "REJECTED") {
  decidingId.value = changeId;
  try {
    await decideChange(props.stage.id, changeId, {
      decision,
      managerNote: decision === "APPROVED" ? "店长确认放行" : "店长驳回本次调整",
    });
    ElMessage.success(decision === "APPROVED" ? "已批准并生成新版角色关系" : "已驳回该申请");
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "审批失败");
  } finally {
    decidingId.value = null;
  }
}

function describe(change: StageDetail["changes"][number]): string {
  if (change.changeType === "SWAP") {
    return `${change.fromPlayerName} 与 ${change.toPlayerName} 互换「${change.roleName}」相关角色`;
  }
  return `候补玩家 ${change.waitingPlayerName} 顶替 ${change.fromPlayerName} 出演「${change.roleName}」`;
}
</script>

<template>
  <el-card shadow="never" class="panel-card">
    <template #header>
      <div class="card-head">
        <span>换角审批与历次调整</span>
        <el-tag v-if="confirmed" type="success" effect="dark">开演后调整</el-tag>
      </div>
    </template>

    <el-alert
      v-if="!confirmed"
      title="确认开演前可直接重新抽签；开演定版后的换角必须提交申请并由店长确认。"
      type="info"
      :closable="false"
      show-icon
      class="locked-tip"
    />

    <template v-else>
      <el-form label-position="top" class="request-form">
        <el-form-item label="调整方式">
          <el-radio-group v-model="changeType">
            <el-radio-button label="SWAP">在场玩家互换</el-radio-button>
            <el-radio-button label="REPLACE">候补玩家顶替</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="需要调整的角色">
          <el-select v-model="roleId" placeholder="选择角色" style="width: 100%">
            <el-option
              v-for="role in stage.roles"
              :key="role.id"
              :label="`${role.name}（${genderLabel(role.gender)}）当前：${currentPlayerOf(role.id)}`"
              :value="role.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="changeType === 'SWAP'" label="互换对象（另一位在场玩家）">
          <el-select v-model="toPlayerId" placeholder="选择玩家" style="width: 100%">
            <el-option
              v-for="player in activePlayers.filter(p => p.id !== currentVersion?.seats.find(s => s.roleId === roleId)?.playerId)"
              :key="player.id"
              :label="`${player.playerName}（${genderLabel(player.gender)}）`"
              :value="player.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="changeType === 'REPLACE'" label="顶替入座的候补玩家">
          <el-select v-model="waitingPlayerId" placeholder="选择候补玩家" style="width: 100%" :disabled="waitingPlayers.length === 0">
            <el-option
              v-for="player in waitingPlayers"
              :key="player.id"
              :label="`${player.playerName}（${genderLabel(player.gender)}）`"
              :value="player.id"
            />
          </el-select>
          <el-text v-if="waitingPlayers.length === 0" type="warning" size="small">当前没有候补玩家，可先在名单中登记</el-text>
        </el-form-item>

        <el-form-item label="调整原因">
          <el-input v-model="reason" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="如：玩家身体不适需离场" />
        </el-form-item>

        <el-button type="primary" :loading="submitting" @click="submitRequest">提交换角申请</el-button>
      </el-form>
    </template>

    <el-divider v-if="pendingChanges.length > 0" content-position="left">待店长确认</el-divider>
    <div v-for="change in pendingChanges" :key="change.id" class="change-row pending">
      <div class="change-main">
        <div class="change-title">
          <el-tag size="small">{{ changeTypeLabel(change.changeType) }}</el-tag>
          <span>{{ describe(change) }}</span>
        </div>
        <el-text type="info" size="small">
          {{ formatDateTime(change.createdAt) }} 提交<template v-if="change.reason">；原因：{{ change.reason }}</template>
        </el-text>
      </div>
      <div class="change-actions">
        <el-button size="small" type="success" :loading="decidingId === change.id" @click="decide(change.id, 'APPROVED')">
          店长批准
        </el-button>
        <el-button size="small" type="danger" plain :loading="decidingId === change.id" @click="decide(change.id, 'REJECTED')">
          驳回
        </el-button>
      </div>
    </div>

    <el-divider content-position="left">历次调整</el-divider>
    <el-timeline v-if="historyChanges.length > 0">
      <el-timeline-item
        v-for="change in historyChanges"
        :key="change.id"
        :type="change.status === 'APPROVED' ? 'success' : 'danger'"
        :timestamp="`${formatDateTime(change.decidedAt ?? change.createdAt)} 提交于 ${formatDateTime(change.createdAt)}`"
      >
        <div class="change-title">
          <el-tag size="small" :type="changeStatusType(change.status)">{{ changeStatusLabel(change.status) }}</el-tag>
          <el-tag size="small" type="info" effect="plain">{{ changeTypeLabel(change.changeType) }}</el-tag>
          <span>{{ describe(change) }}</span>
        </div>
        <el-text v-if="change.versionNo" type="success" size="small">
          批准后已生成第 {{ change.versionNo }} 版角色关系
        </el-text>
        <el-text v-if="change.managerNote" type="info" size="small" class="manager-note">
          店长意见：{{ change.managerNote }}
        </el-text>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="暂无历史调整" :image-size="60" />
  </el-card>
</template>

<style scoped>
.panel-card {
  border-radius: 8px;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}

.locked-tip {
  margin-bottom: 8px;
}

.request-form {
  max-width: 560px;
}

.change-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 12px 14px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 10px;
}

.change-row.pending {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.change-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.change-title {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-weight: 600;
}

.change-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.manager-note {
  display: block;
  margin-top: 4px;
}
</style>
