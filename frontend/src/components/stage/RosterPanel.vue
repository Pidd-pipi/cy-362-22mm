<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { GENDER_LABELS, GENDER_TAGS } from "../../constants/stage";
import { addRoster, removeRoster } from "../../api/stage";
import type { PlayerView, SessionDetailView } from "../../types/stage";

const props = defineProps<{
  detail: SessionDetailView;
  players: PlayerView[];
}>();

const emit = defineEmits<{
  (e: "changed", detail: SessionDetailView): void;
}>();

const selectedPlayerId = ref<number>();
const busy = ref(false);

const session = computed(() => props.detail.session);
const rosterIds = computed(() => new Set(props.detail.roster.map((item) => item.playerId)));
const availablePlayers = computed(() => props.players.filter((player) => !rosterIds.value.has(player.id)));
const remaining = computed(() => session.value.requiredPlayers - props.detail.roster.length);
const full = computed(() => remaining.value <= 0);

async function join() {
  if (!selectedPlayerId.value) {
    ElMessage.warning("请先选择玩家加入名单。");
    return;
  }
  busy.value = true;
  try {
    const updated = await addRoster(session.value.id, selectedPlayerId.value);
    selectedPlayerId.value = undefined;
    emit("changed", updated);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "加入失败");
  } finally {
    busy.value = false;
  }
}

async function leave(playerId: number, name: string) {
  try {
    await ElMessageBox.confirm(`将「${name}」移出本场名单？`, "移出名单", {
      type: "warning",
      confirmButtonText: "移出",
      cancelButtonText: "取消",
    });
  } catch {
    return;
  }
  busy.value = true;
  try {
    emit("changed", await removeRoster(session.value.id, playerId));
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "移出失败");
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <el-card class="panel" shadow="never">
    <template #header>
      <div class="panel-head">
        <span class="panel-title">② 开场名单</span>
        <span class="panel-sub">达到开演人数（{{ session.requiredPlayers }} 人）后即可抽签入座</span>
      </div>
    </template>

    <div class="roster-progress">
      <el-tag :type="full ? 'success' : 'warning'" size="large">
        {{ detail.roster.length }} / {{ session.requiredPlayers }} 人
      </el-tag>
      <el-progress
        :percentage="Math.min(100, Math.round((detail.roster.length / session.requiredPlayers) * 100))"
        :status="full ? 'success' : ''"
        class="roster-bar"
      />
      <span v-if="!full" class="roster-tip">还差 {{ remaining }} 人</span>
      <span v-else class="roster-tip ok">已满员，可抽签入座</span>
    </div>

    <div class="roster-join">
      <el-select
        v-model="selectedPlayerId"
        placeholder="从玩家池选择加入"
        filterable
        class="roster-select"
        :disabled="busy"
      >
        <el-option
          v-for="player in availablePlayers"
          :key="player.id"
          :label="`${player.name}（${GENDER_LABELS[player.gender]}）`"
          :value="player.id"
        />
      </el-select>
      <el-button type="primary" :loading="busy" :disabled="!selectedPlayerId" @click="join">
        加入名单
      </el-button>
    </div>

    <el-table :data="detail.roster" size="default" class="roster-table">
      <el-table-column label="序号" width="64" type="index" />
      <el-table-column prop="name" label="玩家" />
      <el-table-column label="性别" width="90">
        <template #default="{ row }">
          <el-tag :type="GENDER_TAGS[row.gender]" size="small">{{ GENDER_LABELS[row.gender as 'MALE' | 'FEMALE'] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="联系电话" />
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="danger" :disabled="busy" @click="leave(row.playerId, row.name)">移出</el-button>
        </template>
      </el-table-column>
      <template #empty>名单暂无玩家，请先加入。</template>
    </el-table>
  </el-card>
</template>
