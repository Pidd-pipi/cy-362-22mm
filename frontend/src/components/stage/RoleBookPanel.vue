<script setup lang="ts">
import { computed } from "vue";
import type { StageDetail } from "../../types/stage";
import {
  formatDateTime,
  genderLabel,
  genderTagType,
  triggerLabel,
  triggerTagType,
} from "../../utils/stage-format";

const props = defineProps<{ stage: StageDetail }>();

const confirmed = computed(() => props.stage.status === "CONFIRMED");
const currentVersion = computed(() => props.stage.versions[0] ?? null);
const historyVersions = computed(() => props.stage.versions.slice(1));

function seatOf(roleId: number) {
  return currentVersion.value?.seats.find((seat) => seat.roleId === roleId) ?? null;
}
</script>

<template>
  <el-card shadow="never" class="panel-card">
    <template #header>
      <div class="card-head">
        <span>角色册与当前座次</span>
        <el-tag v-if="confirmed" type="success" effect="dark">已定版</el-tag>
        <el-tag v-else-if="currentVersion" type="warning" effect="dark">待开演确认</el-tag>
        <el-tag v-else type="info" effect="plain">未抽签</el-tag>
      </div>
    </template>

    <el-table :data="stage.roles" stripe>
      <el-table-column label="角色" min-width="170">
        <template #default="{ row }">
          <strong>{{ row.name }}</strong>
          <el-tag :type="genderTagType(row.gender)" size="small" class="gender-tag">
            {{ genderLabel(row.gender) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="角色简介" prop="profile" min-width="220" show-overflow-tooltip />
      <el-table-column label="入座玩家" min-width="150">
        <template #default="{ row }">
          <template v-if="seatOf(row.id)">
            <el-tag :type="genderTagType(seatOf(row.id)?.playerGender)" effect="plain">
              {{ seatOf(row.id)?.playerName }} · {{ genderLabel(seatOf(row.id)?.playerGender) }}
            </el-tag>
          </template>
          <el-text v-else type="info" size="small">虚位以待</el-text>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!currentVersion" description="完成抽签后，每个角色仅保留一位玩家" :image-size="70" />

    <el-divider v-if="historyVersions.length > 0" content-position="left">历次抽签存档</el-divider>

    <el-collapse v-if="historyVersions.length > 0" class="history-collapse">
      <el-collapse-item
        v-for="version in historyVersions"
        :key="version.versionNo"
        :name="version.versionNo"
      >
        <template #title>
          <el-tag :type="triggerTagType(version.drawTrigger)" size="small" class="gender-tag">
            第 {{ version.versionNo }} 版 · {{ triggerLabel(version.drawTrigger) }}
          </el-tag>
          <el-text type="info" size="small" class="version-time">
            {{ formatDateTime(version.createdAt) }} · {{ version.note }}
          </el-text>
        </template>
        <div v-for="seat in version.seats" :key="seat.roleId" class="history-seat">
          <span class="role-name">{{ seat.roleName }}（{{ genderLabel(seat.roleGender) }}）</span>
          <span class="arrow" aria-hidden="true">→</span>
          <el-tag :type="genderTagType(seat.playerGender)" effect="plain" size="small">
            {{ seat.playerName }}
          </el-tag>
        </div>
      </el-collapse-item>
    </el-collapse>
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

.gender-tag {
  margin-left: 8px;
}

.history-seat {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0 4px 12px;
}

.history-seat .role-name {
  min-width: 220px;
}

.history-seat .arrow {
  color: #909399;
}

.version-time {
  margin-left: 10px;
}

.history-collapse {
  border-top: none;
}
</style>
