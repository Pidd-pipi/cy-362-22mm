<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { createStage } from "../../api/stage";
import type { ConflictStage, Host, Script } from "../../types/stage";
import { formatTimeRange } from "../../utils/stage-format";

const props = defineProps<{
  modelValue: boolean;
  scripts: Script[];
  hosts: Host[];
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: boolean): void;
  (e: "created"): void;
}>();

const scriptId = ref<number | null>(null);
const hostId = ref<number | null>(null);
const startAt = ref<Date | null>(null);
const conflicts = ref<ConflictStage[]>([]);
const checking = ref(false);
const submitting = ref(false);
const checkError = ref("");

const selectedScript = computed(() => props.scripts.find((item) => item.id === scriptId.value) ?? null);
const selectedHost = computed(() => props.hosts.find((item) => item.id === hostId.value) ?? null);
const readyToCheck = computed(() => scriptId.value !== null && hostId.value !== null && startAt.value !== null);

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit("update:modelValue", value),
});

function isoDateTime(date: Date): string {
  const pad = (value: number) => (value < 10 ? `0${value}` : String(value));
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
}

function disablePast(date: Date): boolean {
  return date.getTime() < Date.now() - 24 * 3600 * 1000;
}

let checkToken = 0;
async function runScheduleCheck() {
  conflicts.value = [];
  checkError.value = "";
  if (!readyToCheck.value || !startAt.value) return;
  const token = ++checkToken;
  checking.value = true;
  try {
    const params = new URLSearchParams({
      scriptId: String(scriptId.value),
      hostId: String(hostId.value),
      startAt: isoDateTime(startAt.value),
    });
    const response = await fetch(`/api/stages/conflicts?${params.toString()}`);
    if (token !== checkToken) return;
    if (response.ok) {
      conflicts.value = (await response.json()) as ConflictStage[];
    } else {
      const body = (await response.json().catch(() => null)) as { message?: string } | null;
      checkError.value = body?.message ?? "档期预检失败";
    }
  } catch {
    if (token === checkToken) checkError.value = "档期预检服务暂不可用";
  } finally {
    if (token === checkToken) checking.value = false;
  }
}

watch([scriptId, hostId, startAt], runScheduleCheck);

watch(dialogVisible, (visible) => {
  if (visible) {
    scriptId.value = null;
    hostId.value = null;
    startAt.value = null;
    conflicts.value = [];
    checkError.value = "";
  }
});

async function submit() {
  if (!scriptId.value || !hostId.value || !startAt.value) {
    ElMessage.warning("请选择剧本、开始时间与主持人");
    return;
  }
  if (conflicts.value.length > 0) {
    ElMessage.error("主持人档期冲突，请更换主持人或调整开场时间");
    return;
  }
  submitting.value = true;
  try {
    await createStage({
      scriptId: scriptId.value,
      hostId: hostId.value,
      startAt: isoDateTime(startAt.value),
    });
    ElMessage.success("开演台已创建，系统已按脚本时长加 20 分钟清场期锁定档期");
    dialogVisible.value = false;
    emit("created");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "创建失败");
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <el-dialog v-model="dialogVisible" title="新增角色开演台" width="560px" destroy-on-close>
    <el-form label-position="top">
      <el-form-item label="剧本（从已有剧本中选择）" required>
        <el-select v-model="scriptId" placeholder="选择剧本" filterable style="width: 100%">
          <el-option
            v-for="script in scripts"
            :key="script.id"
            :label="`${script.name}（${script.playerCount}人 · ${script.durationMinutes}分钟）`"
            :value="script.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="selectedScript" class="script-hint">
        <el-text type="info" size="small">
          {{ selectedScript.genre }} · 难度{{ selectedScript.difficulty }}；
          系统按 {{ selectedScript.durationMinutes }} 分钟脚本时长 + 20 分钟清场期计算主持人占用至散场。
        </el-text>
      </el-form-item>

      <el-form-item label="开场日期与开始时间" required>
        <el-date-picker
          v-model="startAt"
          type="datetime"
          placeholder="选择日期和开始时间"
          format="YYYY-MM-DD HH:mm"
          :disabled-date="disablePast"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="主持人" required>
        <el-select v-model="hostId" placeholder="指派主持人" filterable style="width: 100%">
          <el-option
            v-for="host in hosts"
            :key="host.id"
            :label="`${host.name}（${host.title}）`"
            :value="host.id"
          />
        </el-select>
      </el-form-item>

      <div v-if="checking" class="check-line">
        <span class="spinner" aria-hidden="true" />
        <el-text type="info" size="small">正在核对{{ selectedHost?.name }}的档期…</el-text>
      </div>

      <el-alert
        v-if="readyToCheck && !checking && conflicts.length === 0 && !checkError"
        title="档期空闲，主持人此时段无撞档场次"
        type="success"
        :closable="false"
        show-icon
        class="conflict-box"
      />

      <el-alert
        v-if="checkError"
        :title="checkError"
        type="info"
        :closable="false"
        class="conflict-box"
      />

      <el-alert
        v-if="conflicts.length > 0"
        :title="`撞档：${selectedHost?.name} 该时段已有 ${conflicts.length} 场（已计入 20 分钟清场期）`"
        type="error"
        :closable="false"
        show-icon
        class="conflict-box"
      >
        <div v-for="conflict in conflicts" :key="conflict.id" class="conflict-item">
          <strong>《{{ conflict.scriptName }}》</strong>
          <span>{{ formatTimeRange(conflict.startAt, conflict.clearEndAt) }}（含清场）</span>
        </div>
      </el-alert>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" :disabled="conflicts.length > 0" @click="submit">
        创建开演台
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.script-hint :deep(.el-form-item__content) {
  margin-top: -10px;
}

.check-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid #c0c4cc;
  border-top-color: #3268b8;
  border-radius: 50%;
  display: inline-block;
  animation: stage-spin 0.8s linear infinite;
}

@keyframes stage-spin {
  to {
    transform: rotate(360deg);
  }
}

.conflict-box {
  margin-top: 4px;
}

.conflict-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  padding: 2px 0;
}
</style>
