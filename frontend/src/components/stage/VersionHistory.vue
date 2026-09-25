<script setup lang="ts">
import { computed } from "vue";
import {
  GENDER_LABELS,
  VERSION_KIND_LABELS,
  VERSION_KIND_TAGS,
  formatDateTime,
} from "../../constants/stage";
import type { SessionDetailView } from "../../types/stage";

const props = defineProps<{
  detail: SessionDetailView;
}>();

const history = computed(() => props.detail.history);
</script>

<template>
  <el-card class="panel" shadow="never">
    <template #header>
      <div class="panel-head">
        <span class="panel-title">历次调整（版本留档）</span>
        <span class="panel-sub">每次抽签/重抽/确认/换角均不可变地保存一版</span>
      </div>
    </template>

    <el-timeline v-if="history.length">
      <el-timeline-item
        v-for="version in history"
        :key="version.id"
        :type="VERSION_KIND_TAGS[version.kind] === 'warning'
          ? 'warning'
          : VERSION_KIND_TAGS[version.kind] === 'success'
            ? 'success'
            : 'primary'"
        :timestamp="formatDateTime(version.createdAt)"
        placement="top"
      >
        <div class="version-head">
          <el-tag :type="VERSION_KIND_TAGS[version.kind]" size="small">
            第 {{ version.versionNo }} 版 · {{ VERSION_KIND_LABELS[version.kind] }}
          </el-tag>
          <span class="version-note">{{ version.changeNote }}</span>
          <span class="version-by">{{ version.createdBy }}</span>
        </div>
        <div class="version-seats">
          <el-tag
            v-for="seat in version.seats"
            :key="seat.roleId"
            size="small"
            effect="plain"
            class="version-seat"
          >
            {{ seat.roleName }}
            <span :class="seat.playerId ? 'seat-ok' : 'seat-blank'">
              {{ seat.playerName ? `${seat.playerName}（${GENDER_LABELS[seat.playerGender ?? 'ANY']}）` : '空位' }}
            </span>
          </el-tag>
        </div>
      </el-timeline-item>
    </el-timeline>
    <el-empty v-else description="尚未抽签入座" :image-size="72" />
  </el-card>
</template>
