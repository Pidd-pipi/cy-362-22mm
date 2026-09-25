<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { addPlayer, confirmStage, drawSeats, redrawSeats, removePlayer } from "../../api/stage";
import type { StageDetail } from "../../types/stage";
import { genderLabel, genderTagType, stageStatusLabel, stageStatusType } from "../../utils/stage-format";

const props = defineProps<{ stage: StageDetail }>();
const emit = defineEmits<{ (e: "refresh"): void }>();

const newName = ref("");
const newGender = ref<"MALE" | "FEMALE">("MALE");
const adding = ref(false);
const busy = ref(false);

const activePlayers = computed(() => props.stage.players.filter((player) => !player.waiting));
const waitingPlayers = computed(() => props.stage.players.filter((player) => player.waiting));
const confirmed = computed(() => props.stage.status === "CONFIRMED");
const drawn = computed(() => props.stage.versions.length > 0);
const rosterReady = computed(() => activePlayers.value.length >= props.stage.requiredCount);

async function handleAdd() {
  const name = newName.value.trim();
  if (!name) {
    ElMessage.warning("请填写玩家姓名");
    return;
  }
  adding.value = true;
  try {
    const player = await addPlayer(props.stage.id, { playerName: name, gender: newGender.value });
    ElMessage.success(player.waiting ? "到场人数已满，已加入候补名单" : `${name} 已加入到场名单`);
    newName.value = "";
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "添加失败");
  } finally {
    adding.value = false;
  }
}

async function handleRemove(playerId: number, playerName: string) {
  try {
    await ElMessageBox.confirm(`确认从本场名单移除「${playerName}」？`, "移除玩家", {
      type: "warning",
      confirmButtonText: "移除",
      cancelButtonText: "取消",
    });
  } catch {
    return;
  }
  try {
    await removePlayer(props.stage.id, playerId);
    ElMessage.success("已移除");
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "移除失败");
  }
}

async function handleDraw() {
  busy.value = true;
  try {
    await drawSeats(props.stage.id, "首次抽签入座");
    ElMessage.success("抽签完成，每个角色仅保留一位玩家");
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "抽签失败");
  } finally {
    busy.value = false;
  }
}

async function handleRedraw() {
  let note = "";
  try {
    const result = await ElMessageBox.prompt("重抽将保留上一版座次，可填写重抽原因（选填）", "重新抽签", {
      confirmButtonText: "确认重抽",
      cancelButtonText: "取消",
      inputPlaceholder: "如：玩家性别与角色要求不符",
    });
    note = result.value ?? "";
  } catch {
    return;
  }
  busy.value = true;
  try {
    await redrawSeats(props.stage.id, note);
    ElMessage.success("已重新抽签，上一版已存档");
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "重抽失败");
  } finally {
    busy.value = false;
  }
}

async function handleConfirm() {
  try {
    await ElMessageBox.confirm(
      "确认开演后角色关系定版，后续换角需提交申请并由店长确认。确认现在开演？",
      "确认开演",
      { type: "success", confirmButtonText: "确认开演", cancelButtonText: "再检查一下" }
    );
  } catch {
    return;
  }
  busy.value = true;
  try {
    await confirmStage(props.stage.id);
    ElMessage.success("已开演，角色关系定版");
    emit("refresh");
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "确认失败");
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <el-card shadow="never" class="panel-card">
    <template #header>
      <div class="card-head">
        <span>到场名单与抽签入座</span>
        <el-tag :type="stageStatusType(stage.status)" effect="dark">{{ stageStatusLabel(stage.status) }}</el-tag>
      </div>
    </template>

    <el-progress
      :percentage="Math.min(100, Math.round((activePlayers.length / stage.requiredCount) * 100))"
      :status="rosterReady ? 'success' : undefined"
      :stroke-width="14"
      :format="() => `${activePlayers.length}/${stage.requiredCount} 人`"
    />
    <el-text v-if="!rosterReady" type="warning" size="small" class="hint-text">
      名单达到开演人数（{{ stage.requiredCount }} 人）后才可抽签入座
    </el-text>
    <el-text v-else-if="!drawn" type="success" size="small" class="hint-text">
      名单已满，可以按角色性别要求抽签
    </el-text>
    <el-text v-else-if="!confirmed" type="info" size="small" class="hint-text">
      已生成第 {{ stage.versions[0].versionNo }} 版座次，可重抽（上一版自动存档）或确认开演
    </el-text>
    <el-text v-else type="success" size="small" class="hint-text">
      已确认开演，角色关系定版；换角请提交申请走店长确认
    </el-text>

    <el-divider content-position="left">到场玩家</el-divider>
    <div class="player-grid">
      <el-tag
        v-for="player in activePlayers"
        :key="player.id"
        :type="genderTagType(player.gender)"
        size="large"
        closable
        :disable-transitions="false"
        class="player-tag"
        @close="handleRemove(player.id, player.playerName)"
      >
        {{ player.playerName }} · {{ genderLabel(player.gender) }}
      </el-tag>
      <el-text v-if="activePlayers.length === 0" type="info" size="small">暂未登记到场玩家</el-text>
    </div>

    <el-divider content-position="left">候补名单</el-divider>
    <div class="player-grid">
      <el-tag
        v-for="player in waitingPlayers"
        :key="player.id"
        type="info"
        size="large"
        effect="plain"
        class="player-tag"
      >
        {{ player.playerName }} · {{ genderLabel(player.gender) }}（候补）
      </el-tag>
      <el-text v-if="waitingPlayers.length === 0" type="info" size="small">暂无候补玩家</el-text>
    </div>

    <el-divider />

    <div class="add-row">
      <el-input
        v-model="newName"
        placeholder="玩家姓名"
        :disabled="confirmed"
        style="width: 180px"
        @keyup.enter="handleAdd"
      />
      <el-radio-group v-model="newGender" :disabled="confirmed">
        <el-radio-button label="MALE">男</el-radio-button>
        <el-radio-button label="FEMALE">女</el-radio-button>
      </el-radio-group>
      <el-button type="primary" plain :loading="adding" :disabled="confirmed" @click="handleAdd">
        登记玩家
      </el-button>
      <el-text v-if="confirmed" type="info" size="small">开演后新增玩家默认候补，经换角审批后入座</el-text>
    </div>

    <div class="action-row">
      <el-button
        v-if="!drawn"
        type="primary"
        :disabled="!rosterReady || confirmed"
        :loading="busy"
        @click="handleDraw"
      >
        按角色性别要求抽签入座
      </el-button>
      <template v-else>
        <el-button type="warning" plain :disabled="confirmed" :loading="busy" @click="handleRedraw">
          重新抽签（保留上一版）
        </el-button>
        <el-button type="success" :disabled="confirmed" :loading="busy" @click="handleConfirm">
          确认开演，角色定版
        </el-button>
      </template>
    </div>
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

.hint-text {
  display: block;
  margin-top: 8px;
}

.player-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.player-tag {
  font-size: 14px;
  padding: 0 12px;
  height: 32px;
}

.add-row,
.action-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}

.action-row {
  margin-top: 18px;
}
</style>
