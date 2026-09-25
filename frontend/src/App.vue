<script setup lang="ts">
import { onMounted, ref } from "vue";
import { fetchOverview } from "./api/client";
import { APP_CODE, APP_NAME } from "./constants/app";
import { REQUEST_MESSAGES } from "./constants/messages";
import { createFallbackOverview } from "./state/dashboard";
import type { OverviewResponse } from "./types";
import FeatureStrip from "./components/FeatureStrip.vue";
import MetricGrid from "./components/MetricGrid.vue";
import OperationsTable from "./components/OperationsTable.vue";
import StagePage from "./pages/StagePage.vue";

const overview = ref<OverviewResponse>(createFallbackOverview());
const notice = ref(REQUEST_MESSAGES.overviewFallback);
const activeView = ref<"overview" | "stage">("stage");

onMounted(async () => {
  try {
    overview.value = await fetchOverview();
    notice.value = "后端服务已联通，当前展示实时接口数据。";
  } catch {
    notice.value = REQUEST_MESSAGES.overviewFallback;
  }
});
</script>

<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <span class="brand-code">{{ APP_CODE }}</span>
        <h1 class="brand-title">{{ APP_NAME }}</h1>
      </div>
      <el-radio-group v-model="activeView" size="large">
        <el-radio-button label="stage">角色开演台</el-radio-button>
        <el-radio-button label="overview">运营总览</el-radio-button>
      </el-radio-group>
    </header>

    <section v-if="activeView === 'stage'" class="workspace stage-workspace">
      <StagePage />
    </section>

    <section v-else class="workspace">
      <div class="lead-grid">
        <article class="hero-panel">
          <span class="pill">{{ notice }}</span>
          <h2>{{ overview.appName }}</h2>
          <p>{{ overview.description }}</p>
        </article>
        <MetricGrid :items="overview.kpis" />
      </div>
      <FeatureStrip :items="overview.features" />
      <section class="work-panel">
        <h2>运营任务流</h2>
        <OperationsTable :records="overview.records" />
      </section>
    </section>
  </main>
</template>
