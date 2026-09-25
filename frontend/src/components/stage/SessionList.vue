<script setup lang="ts">
import { computed } from "vue";
import {
  SESSION_STATUS_LABELS,
  SESSION_STATUS_TAGS,
  toHM,
} from "../../constants/stage";
import type { SessionView } from "../../types/stage";

const props = defineProps<{
  sessions: SessionView[];
  activeId: number | null;
}>();

const emit = defineEmits<{
  (e: "select", id: number): void;
}>();

const ordered = computed(() =>
  [...props.sessions].sort((a, b) => (a.startAt < b.startAt ? 1 : -1)),
);
</script>

<template>
  <el-card class="panel session-list" shadow="never">
    <template #header>
      <div class="panel-head">
        <span class="panel-title">开演台场次</span>
        <span class="panel-sub">{{ sessions.length }} 场</span>
      </div>
    </template>

    <div
      v-for="session in ordered"
      :key="session.id"
      class="session-item"
      :class="{ active: session.id === activeId }"
      @click="emit('select', session.id)"
    >
      <div class="session-item-top">
        <span class="session-name">{{ session.scriptName }}</span>
        <el-tag :type="SESSION_STATUS_TAGS[session.status]" size="small">
          {{ SESSION_STATUS_LABELS[session.status] }}
        </el-tag>
      </div>
      <div class="session-item-meta">
        {{ session.sessionDate }} {{ toHM(session.startTime) }}–{{ toHM(session.endTime) }}
      </div>
      <div class="session-item-meta">
        DM {{ session.hostName }} · 名单 {{ session.rosterCount }}/{{ session.requiredPlayers }}
      </div>
    </div>
    <el-empty v-if="!ordered.length" description="还没有场次，先在右侧创建" :image-size="64" />
  </el-card>
</template>
