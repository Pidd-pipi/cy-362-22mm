<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import { CLEANUP_MINUTES_DEFAULT, GENDER_LABELS, toHM } from "../../constants/stage";
import { ConflictError, createSession } from "../../api/stage";
import type { ConflictSessionView, HostView, ScriptView } from "../../types/stage";

const props = defineProps<{
  scripts: ScriptView[];
  hosts: HostView[];
}>();

const emit = defineEmits<{
  (e: "created", sessionId: number): void;
}>();

const scriptId = ref<number>();
const hostId = ref<number>();
const date = ref<string>(new Date().toISOString().slice(0, 10));
const startTime = ref<string>("19:00");
const cleanupMinutes = ref<number>(CLEANUP_MINUTES_DEFAULT);
const note = ref("");
const submitting = ref(false);
const conflicts = ref<ConflictSessionView[]>([]);

const selectedScript = computed(() => props.scripts.find((item) => item.id === scriptId.value));
const endPreview = computed(() => {
  if (!selectedScript.value) {
    return null;
  }
  const [hour, minute] = startTime.value.split(":").map(Number);
  const total = hour * 60 + minute + selectedScript.value.durationMinutes + cleanupMinutes.value;
  const nextDay = Math.floor(total / (60 * 24));
  const end = total % (60 * 24);
  return {
    text: `${String(Math.floor(end / 60)).padStart(2, "0")}:${String(end % 60).padStart(2, "0")}`,
    nextDay,
  };
});

function resetConflict() {
  conflicts.value = [];
}

async function submit() {
  if (!scriptId.value || !hostId.value || !date.value || !startTime.value) {
    ElMessage.warning("请选择剧本、主持人、日期与开始时间。");
    return;
  }
  submitting.value = true;
  conflicts.value = [];
  try {
    const detail = await createSession({
      scriptId: scriptId.value,
      hostId: hostId.value,
      date: date.value,
      startTime: `${startTime.value}:00`,
      cleanupMinutes: cleanupMinutes.value,
      note: note.value || undefined,
    });
    ElMessage.success("场次已创建，可开始核对名单与入座。");
    note.value = "";
    emit("created", detail.session.id);
  } catch (error) {
    if (error instanceof ConflictError) {
      conflicts.value = error.conflicts;
    } else {
      ElMessage.error(error instanceof Error ? error.message : "创建失败");
    }
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <el-card class="panel create-panel" shadow="never">
    <template #header>
      <div class="panel-head">
        <span class="panel-title">① 选剧本 · 定日期时间 · 指派主持人</span>
        <span class="panel-sub">系统按脚本时长 + {{ CLEANUP_MINUTES_DEFAULT }} 分钟清场期校验主持人档期</span>
      </div>
    </template>

    <el-form label-position="top" class="create-form" @submit.prevent="submit">
      <el-form-item label="已有剧本">
        <el-select
          v-model="scriptId"
          placeholder="选择剧本"
          filterable
          class="grow"
          @change="resetConflict"
        >
          <el-option
            v-for="script in scripts"
            :key="script.id"
            :label="`${script.name}（${script.durationMinutes}分钟 / ${script.playerCount}人）`"
            :value="script.id"
          />
        </el-select>
      </el-form-item>

      <div v-if="selectedScript" class="script-meta">
        <el-tag size="small" type="info">{{ selectedScript.genre }}</el-tag>
        <el-tag size="small">难度 {{ selectedScript.difficulty }}</el-tag>
        <el-tag size="small" type="warning">时长 {{ selectedScript.durationMinutes }} 分钟</el-tag>
        <el-tag size="small" type="success">开演 {{ selectedScript.playerCount }} 人</el-tag>
        <span class="role-hint">
          角色性别：
          <el-tag
            v-for="role in selectedScript.roles"
            :key="role.id"
            size="small"
            class="role-chip"
          >
            {{ role.name }}·{{ GENDER_LABELS[role.genderRequirement] }}
          </el-tag>
        </span>
      </div>

      <div class="form-grid">
        <el-form-item label="日期">
          <el-date-picker
            v-model="date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            class="grow"
            @change="resetConflict"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker
            v-model="startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="开始时间"
            class="grow"
            @change="resetConflict"
          />
        </el-form-item>
        <el-form-item label="清场期（分钟）">
          <el-input-number v-model="cleanupMinutes" :min="0" :max="120" :step="10" class="grow" />
        </el-form-item>
        <el-form-item label="主持人（DM）">
          <el-select v-model="hostId" placeholder="指派主持人" filterable class="grow" @change="resetConflict">
            <el-option
              v-for="host in hosts"
              :key="host.id"
              :label="`${host.name}（${host.title}）`"
              :value="host.id"
            />
          </el-select>
        </el-form-item>
      </div>

      <el-form-item label="备注">
        <el-input v-model="note" maxlength="200" placeholder="可选，例如包间、玩家偏好" />
      </el-form-item>

      <div class="create-actions">
        <el-button type="primary" :loading="submitting" @click="submit">核对档期并开演台</el-button>
        <span v-if="endPreview" class="end-preview">
          预计结束 <strong>{{ endPreview.text }}</strong>
          <el-tag v-if="endPreview.nextDay > 0" size="small" type="warning">跨天 +{{ endPreview.nextDay }} 天</el-tag>
        </span>
      </div>
    </el-form>

    <el-alert
      v-if="conflicts.length"
      class="conflict-box"
      type="error"
      :closable="false"
      show-icon
      title="主持人档期撞档，请改派主持人或调整时间"
    >
      <div class="conflict-list">
        <div v-for="conflict in conflicts" :key="conflict.sessionId" class="conflict-item">
          <el-tag size="small" type="danger">冲突场次 #{{ conflict.sessionId }}</el-tag>
          <span class="conflict-script">{{ conflict.scriptName }}</span>
          <span>{{ conflict.sessionDate }} {{ toHM(conflict.startTime) }}–{{ toHM(conflict.endTime) }}</span>
        </div>
      </div>
    </el-alert>
  </el-card>
</template>
