<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { fetchHosts, fetchStageDetail, fetchStages, fetchScripts, ScheduleConflictError } from "../api/stage";
import type { Host, Script, StageDetail, StageSummary } from "../types/stage";
import {
  formatDateTime,
  stageStatusLabel,
  stageStatusType,
} from "../utils/stage-format";
import StageListPanel from "../components/stage/StageListPanel.vue";
import CreateStageDialog from "../components/stage/CreateStageDialog.vue";
import RosterPanel from "../components/stage/RosterPanel.vue";
import RoleBookPanel from "../components/stage/RoleBookPanel.vue";
import ChangeApprovalPanel from "../components/stage/ChangeApprovalPanel.vue";

const scripts = ref<Script[]>([]);
const hosts = ref<Host[]>([]);
const stages = ref<StageSummary[]>([]);
const listLoading = ref(false);
const detailLoading = ref(false);
const detail = ref<StageDetail | null>(null);
const createVisible = ref(false);

async function loadCatalog() {
  try {
    const [scriptList, hostList] = await Promise.all([fetchScripts(), fetchHosts()]);
    scripts.value = scriptList;
    hosts.value = hostList;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "基础数据加载失败");
  }
}

async function loadStages(preferId?: number) {
  listLoading.value = true;
  try {
    stages.value = await fetchStages();
    const target = preferId ?? detail.value?.id ?? stages.value[0]?.id ?? null;
    if (target !== null) {
      await openStage(target);
    } else {
      detail.value = null;
    }
  } catch (error) {
    if (error instanceof ScheduleConflictError) {
      ElMessage.error(error.message);
    } else {
      ElMessage.error(error instanceof Error ? error.message : "场次列表加载失败");
    }
  } finally {
    listLoading.value = false;
  }
}

async function openStage(id: number) {
  detailLoading.value = true;
  try {
    detail.value = await fetchStageDetail(id);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "开演台详情加载失败");
  } finally {
    detailLoading.value = false;
  }
}

function handleCreated() {
  loadStages();
}

onMounted(async () => {
  await loadCatalog();
  await loadStages();
});
</script>

<template>
  <section class="stage-console">
    <div class="console-intro">
      <h2>角色开演台</h2>
      <p>
        从已有剧本选定日期与开始时间并指派主持人，系统按脚本时长加 20 分钟清场期查档期；
        名单达到开演人数后按角色性别要求抽签入座、每版存档；确认开演后角色定版，换角须店长确认。
      </p>
    </div>

    <StageListPanel
      :stages="stages"
      :loading="listLoading"
      :active-id="detail?.id ?? null"
      @open="openStage"
      @create="createVisible = true"
    />

    <div v-loading="detailLoading" class="detail-wrap">
      <template v-if="detail">
        <el-card shadow="never" class="head-card">
          <div class="head-main">
            <div>
              <h3>《{{ detail.scriptName }}》角色开演台</h3>
              <el-descriptions :column="3" border size="small" class="head-desc">
                <el-descriptions-item label="主持人">
                  {{ detail.hostName }}（{{ detail.hostTitle }}）
                </el-descriptions-item>
                <el-descriptions-item label="开场时间">{{ formatDateTime(detail.startAt) }}</el-descriptions-item>
                <el-descriptions-item label="预计散场">{{ formatDateTime(detail.endAt) }}</el-descriptions-item>
                <el-descriptions-item label="清场截止（+20 分钟）">{{ formatDateTime(detail.clearEndAt) }}</el-descriptions-item>
                <el-descriptions-item label="剧本时长">{{ detail.durationMinutes }} 分钟</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="stageStatusType(detail.status)" size="small">{{ stageStatusLabel(detail.status) }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>

        <el-row :gutter="16">
          <el-col :xs="24" :lg="11">
            <RosterPanel :stage="detail" @refresh="openStage(detail.id)" />
          </el-col>
          <el-col :xs="24" :lg="13">
            <RoleBookPanel :stage="detail" />
          </el-col>
        </el-row>

        <ChangeApprovalPanel :stage="detail" class="change-panel" @refresh="openStage(detail.id)" />
      </template>

      <el-empty v-else-if="!detailLoading" description="点击上方场次查看开演台，或新增一场" />
    </div>

    <CreateStageDialog
      v-model="createVisible"
      :scripts="scripts"
      :hosts="hosts"
      @created="handleCreated"
    />
  </section>
</template>

<style scoped>
.stage-console {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.console-intro h2 {
  margin: 0 0 6px;
  font-size: 22px;
}

.console-intro p {
  margin: 0;
  color: #5c6675;
  line-height: 1.7;
}

.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 160px;
}

.head-card {
  border-radius: 8px;
}

.head-card h3 {
  margin: 0 0 12px;
}

.head-desc {
  max-width: 880px;
}

.change-panel {
  margin-top: 0;
}

:deep(.el-col) {
  margin-bottom: 16px;
}
</style>
