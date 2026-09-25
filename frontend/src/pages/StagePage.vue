<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  fetchHosts,
  fetchPlayers,
  fetchScripts,
  fetchSession,
  fetchSessions,
} from "../api/stage";
import type {
  HostView,
  PlayerView,
  ScriptView,
  SessionDetailView,
  SessionView,
} from "../types/stage";
import {
  GENDER_LABELS,
  SESSION_STATUS_LABELS,
  SESSION_STATUS_TAGS,
  toHM,
} from "../constants/stage";
import SessionCreateForm from "../components/stage/SessionCreateForm.vue";
import SessionList from "../components/stage/SessionList.vue";
import RosterPanel from "../components/stage/RosterPanel.vue";
import RoleBook from "../components/stage/RoleBook.vue";
import SwapPanel from "../components/stage/SwapPanel.vue";
import VersionHistory from "../components/stage/VersionHistory.vue";

const loading = ref(true);
const scripts = ref<ScriptView[]>([]);
const hosts = ref<HostView[]>([]);
const players = ref<PlayerView[]>([]);
const sessions = ref<SessionView[]>([]);
const activeId = ref<number | null>(null);
const detail = ref<SessionDetailView | null>(null);
const detailLoading = ref(false);

const activeSession = computed(() => detail.value?.session ?? null);

async function loadBase() {
  try {
    const [scriptList, hostList, playerList, sessionList] = await Promise.all([
      fetchScripts(),
      fetchHosts(),
      fetchPlayers(),
      fetchSessions(),
    ]);
    scripts.value = scriptList;
    hosts.value = hostList;
    players.value = playerList;
    sessions.value = sessionList;
    if (!activeId.value && sessionList.length) {
      await selectSession(sessionList[0].id);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "基础数据加载失败");
  } finally {
    loading.value = false;
  }
}

async function refreshSessions() {
  sessions.value = await fetchSessions();
}

async function selectSession(id: number) {
  activeId.value = id;
  detailLoading.value = true;
  try {
    detail.value = await fetchSession(id);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "场次详情加载失败");
  } finally {
    detailLoading.value = false;
  }
}

async function handleCreated(id: number) {
  await refreshSessions();
  await selectSession(id);
}

async function handleChanged(updated: SessionDetailView) {
  detail.value = updated;
  await refreshSessions();
}

onMounted(loadBase);
</script>

<template>
  <div v-loading="loading" class="stage-page">
    <section class="stage-head">
      <h2>角色开演台</h2>
      <p>开场前核对主持人与角色：选剧本、定时间、指派主持人 → 系统按脚本时长加 20 分钟清场期查档期 → 名单满员后按角色性别抽签入座 → 确认开演、角色定版，后续换角走店长确认。</p>
    </section>

    <div class="stage-layout">
      <div class="stage-side">
        <SessionList :sessions="sessions" :active-id="activeId" @select="selectSession" />
      </div>

      <div class="stage-main">
        <SessionCreateForm :scripts="scripts" :hosts="hosts" @created="handleCreated" />

        <template v-if="activeSession">
          <el-card class="panel session-banner" shadow="never" v-loading="detailLoading">
            <div class="banner-row">
              <div>
                <div class="banner-title">
                  #{{ activeSession.id }} {{ activeSession.scriptName }}
                  <el-tag
                    :type="SESSION_STATUS_TAGS[activeSession.status]"
                    size="small"
                    class="banner-status"
                  >
                    {{ SESSION_STATUS_LABELS[activeSession.status] }}
                  </el-tag>
                </div>
                <div class="banner-meta">
                  {{ activeSession.sessionDate }} {{ toHM(activeSession.startTime) }}–{{ toHM(activeSession.endTime) }}
                  · 主持人 {{ activeSession.hostName }}
                  · 脚本 {{ activeSession.requiredPlayers }} 角色
                  · 清场 {{ activeSession.cleanupMinutes }} 分钟
                </div>
                <div class="banner-role-genders">
                  <el-tag
                    v-for="role in detail?.roles ?? []"
                    :key="role.id"
                    size="small"
                    effect="plain"
                    class="banner-role-chip"
                  >
                    {{ role.name }}·{{ GENDER_LABELS[role.genderRequirement] }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <RosterPanel
            v-if="detail && activeSession.status === 'STAGING'"
            :detail="detail"
            :players="players"
            @changed="handleChanged"
          />

          <RoleBook v-if="detail" :detail="detail" @changed="handleChanged" />

          <SwapPanel v-if="detail" :detail="detail" @changed="handleChanged" />

          <VersionHistory v-if="detail" :detail="detail" />
        </template>

        <el-empty v-else-if="!loading" description="选择或创建一场开演台场次开始安排" />
      </div>
    </div>
  </div>
</template>
