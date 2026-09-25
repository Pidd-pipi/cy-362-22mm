<script setup lang="ts">
import { ref } from "vue";
import { APP_CODE, APP_NAME } from "./constants/app";
import OverviewPage from "./pages/OverviewPage.vue";
import StageConsolePage from "./pages/StageConsolePage.vue";

const activeTab = ref<"overview" | "stage">("stage");
</script>

<template>
  <main class="app-shell">
    <header class="topbar">
      <div>
        <span class="brand-code">{{ APP_CODE }}</span>
        <h1 class="brand-title">{{ APP_NAME }}</h1>
      </div>
      <el-radio-group v-model="activeTab" size="large">
        <el-radio-button label="stage">角色开演台</el-radio-button>
        <el-radio-button label="overview">运营总览</el-radio-button>
      </el-radio-group>
    </header>
    <section class="workspace">
      <OverviewPage v-if="activeTab === 'overview'" />
      <StageConsolePage v-else />
    </section>
  </main>
</template>
