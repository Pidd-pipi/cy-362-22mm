<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  GENDER_LABELS,
  GENDER_TAGS,
  SESSION_STATUS_LABELS,
  SESSION_STATUS_TAGS,
} from "../../constants/stage";
import { confirmSession, drawSeats } from "../../api/stage";
import type { SessionDetailView } from "../../types/stage";

const props = defineProps<{
  detail: SessionDetailView;
}>();

const emit = defineEmits<{
  (e: "changed", detail: SessionDetailView): void;
}>();

const busy = ref(false);
const session = computed(() => props.detail.session);
const seats = computed(() => session.value.seats);
const hasVersion = computed(() => !!props.detail.currentVersion);
const isConfirmed = computed(() => session.value.status === "CONFIRMED");
const allSeated = computed(
  () => seats.value.length > 0 && seats.value.every((seat) => seat.playerId !== null),
);

async function runDraw(reDraw: boolean) {
  if (reDraw) {
    try {
      await ElMessageBox.confirm(
        "重新抽签将生成新版本，上一版入座结果会被完整保留在历次调整中。是否继续？",
        "重新抽签",
        { type: "warning", confirmButtonText: "重抽并保留上一版", cancelButtonText: "取消" },
      );
    } catch {
      return;
    }
  }
  busy.value = true;
  try {
    emit("changed", await drawSeats(session.value.id, reDraw));
    ElMessage.success(reDraw ? "已重新抽签，上一版已存档。" : "抽签入座完成。");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "抽签失败");
  } finally {
    busy.value = false;
  }
}

async function confirm() {
  try {
    await ElMessageBox.confirm(
      "确认开演后角色关系定版，名单锁定，后续换角需走店长确认。是否确认？",
      "确认开演",
      { type: "success", confirmButtonText: "确认开演", cancelButtonText: "再检查一下" },
    );
  } catch {
    return;
  }
  busy.value = true;
  try {
    emit("changed", await confirmSession(session.value.id));
    ElMessage.success("已确认开演，角色关系定版。");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "确认失败");
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <el-card class="panel" shadow="never">
    <template #header>
      <div class="panel-head">
        <span class="panel-title">③ 角色册 · 抽签入座</span>
        <el-tag :type="SESSION_STATUS_TAGS[session.status]" size="small">
          {{ SESSION_STATUS_LABELS[session.status] }}
        </el-tag>
      </div>
    </template>

    <p class="role-rule">
      按角色性别要求抽签：每个角色仅留一位玩家，多余玩家自动候补；重抽前保存上一版。
    </p>

    <div class="seat-grid">
      <div
        v-for="seat in seats.length ? seats : detail.roles.map(role => ({
          roleId: role.id, roleName: role.name, roleGenderRequirement: role.genderRequirement,
          positionNo: role.positionNo, playerId: null, playerName: null, playerGender: null,
        }))"
        :key="seat.roleId"
        class="seat-card"
        :class="{ filled: seat.playerId !== null, locked: isConfirmed }"
      >
        <div class="seat-position">{{ seat.positionNo }} 号位</div>
        <div class="seat-role">{{ seat.roleName }}</div>
        <el-tag :type="GENDER_TAGS[seat.roleGenderRequirement]" size="small">
          {{ GENDER_LABELS[seat.roleGenderRequirement] }}
        </el-tag>
        <div class="seat-player">
          <template v-if="seat.playerId">
            <span class="seat-player-name">{{ seat.playerName }}</span>
            <el-tag
              :type="GENDER_TAGS[seat.playerGender ?? 'ANY']"
              size="small"
              effect="plain"
            >
              {{ GENDER_LABELS[(seat.playerGender ?? 'ANY') as 'MALE' | 'FEMALE' | 'ANY'] }}
            </el-tag>
          </template>
          <span v-else class="seat-empty">虚位以待</span>
        </div>
      </div>
    </div>

    <div v-if="detail.unseatedRoster.length" class="bench">
      <span class="bench-label">候补（{{ detail.unseatedRoster.length }}）：</span>
      <el-tag
        v-for="player in detail.unseatedRoster"
        :key="player.playerId"
        size="small"
        type="info"
        effect="plain"
        class="bench-chip"
      >
        {{ player.name }}·{{ GENDER_LABELS[player.gender] }}
      </el-tag>
    </div>

    <div v-if="!isConfirmed" class="seat-actions">
      <el-button
        type="primary"
        :loading="busy"
        :disabled="detail.roster.length < session.requiredPlayers"
        @click="runDraw(false)"
      >
        抽签入座
      </el-button>
      <el-button
        :loading="busy"
        :disabled="!hasVersion"
        @click="runDraw(true)"
      >
        重新抽签（保留上一版）
      </el-button>
      <el-button
        type="success"
        :loading="busy"
        :disabled="!allSeated"
        @click="confirm"
      >
        确认开演 · 角色定版
      </el-button>
      <span v-if="detail.roster.length < session.requiredPlayers" class="seat-hint">
        名单达到 {{ session.requiredPlayers }} 人后开放抽签
      </span>
      <span v-else-if="!allSeated" class="seat-hint">尚未完成入座</span>
    </div>
    <el-alert
      v-else
      class="locked-tip"
      type="success"
      :closable="false"
      show-icon
      title="角色关系已定版。如需换角，请在下方「换角调整」中发起，由店长确认。"
    />
  </el-card>
</template>
