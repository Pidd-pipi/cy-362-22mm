<script setup lang="ts">
import type { StageSummary } from "../../types/stage";
import {
  formatDateTime,
  stageStatusLabel,
  stageStatusType,
} from "../../utils/stage-format";

const props = defineProps<{
  stages: StageSummary[];
  loading: boolean;
  activeId: number | null;
}>();

const emit = defineEmits<{ (e: "open", id: number): void; (e: "create"): void }>();

function rowClassName({ row }: { row: StageSummary }): string {
  return row.id === props.activeId ? "active-row" : "";
}

function openRow(row: StageSummary): void {
  emit("open", row.id);
}
</script>

<template>
  <el-card shadow="never" class="panel-card">
    <template #header>
      <div class="card-head">
        <span>开演台场次</span>
        <el-button type="primary" size="small" @click="emit('create')">+ 新增角色开演台</el-button>
      </div>
    </template>

    <el-table
      :data="stages"
      v-loading="loading"
      stripe
      highlight-current-row
      :row-class-name="rowClassName"
      @row-click="openRow"
    >
      <el-table-column label="剧本" min-width="150">
        <template #default="{ row }">
          <strong>《{{ row.scriptName }}》</strong>
          <div class="sub-text">{{ row.genre }}</div>
        </template>
      </el-table-column>
      <el-table-column label="主持人" prop="hostName" width="90" />
      <el-table-column label="开场" min-width="150">
        <template #default="{ row }">
          {{ formatDateTime(row.startAt) }}
          <div class="sub-text">清场至 {{ formatDateTime(row.clearEndAt).slice(11) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="名单" width="110">
        <template #default="{ row }">
          <el-tag :type="row.rosterReady ? 'success' : 'warning'" size="small">
            {{ row.activeCount }}/{{ row.requiredCount }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="座次版本" width="90">
        <template #default="{ row }">
          <el-text v-if="row.currentVersionNo" type="success" size="small">v{{ row.currentVersionNo }}</el-text>
          <el-text v-else type="info" size="small">未抽签</el-text>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="stageStatusType(row.status)" size="small">{{ stageStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
.panel-card {
  border-radius: 8px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
}

.sub-text {
  font-size: 12px;
  color: #909399;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.active-row) {
  background: #ecf2fb !important;
}
</style>
