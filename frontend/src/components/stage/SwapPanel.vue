<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  GENDER_LABELS,
  SWAP_STATUS_LABELS,
  SWAP_STATUS_TAGS,
  formatDateTime,
} from "../../constants/stage";
import { approveSwap, rejectSwap, requestSwap } from "../../api/stage";
import type { SessionDetailView } from "../../types/stage";

const props = defineProps<{
  detail: SessionDetailView;
}>();

const emit = defineEmits<{
  (e: "changed", detail: SessionDetailView): void;
}>();

const roleId = ref<number>();
const toPlayerId = ref<number>();
const reason = ref("");
const busy = ref(false);

const session = computed(() => props.detail.session);

/** 当前每个角色的持有者。 */
const holderByRole = computed(() => {
  const map = new Map<number, number | null>();
  for (const seat of session.value.seats) {
    map.set(seat.roleId, seat.playerId);
  }
  return map;
});

/** 发起换角候选：名单内、性别符合、非当前持有者。 */
const candidates = computed(() => {
  const role = props.detail.roles.find((item) => item.id === roleId.value);
  if (!role) {
    return [];
  }
  return props.detail.roster
    .filter((player) => player.playerId !== holderByRole.value.get(role.id))
    .filter(
      (player) =>
        role.genderRequirement === "ANY" || player.gender === role.genderRequirement,
    );
});

function onRoleChange() {
  toPlayerId.value = undefined;
}

async function submitSwap() {
  if (!roleId.value || !toPlayerId.value) {
    ElMessage.warning("请选择要调整的角色和接替玩家。");
    return;
  }
  busy.value = true;
  try {
    emit("changed", await requestSwap(session.value.id, roleId.value, toPlayerId.value, reason.value));
    ElMessage.success("换角申请已提交，等待店长确认。");
    roleId.value = undefined;
    toPlayerId.value = undefined;
    reason.value = "";
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "提交失败");
  } finally {
    busy.value = false;
  }
}

async function review(id: number, approve: boolean) {
  try {
    const { value } = await ElMessageBox.prompt(
      approve ? "店长确认该换角？可填写确认意见。" : "请填写驳回原因。",
      approve ? "店长确认换角" : "驳回换角",
      {
        type: approve ? "success" : "warning",
        confirmButtonText: approve ? "确认换角" : "驳回",
        cancelButtonText: "取消",
        inputPlaceholder: approve ? "确认意见（可选）" : "驳回原因",
      },
    );
    busy.value = true;
    emit(
      "changed",
      approve
        ? await approveSwap(id, value || (approve ? "同意" : ""))
        : await rejectSwap(id, value || "店长驳回"),
    );
    ElMessage.success(approve ? "已确认换角，角色册已更新。" : "已驳回换角。");
  } catch (error) {
    if (error === "cancel" || error === "close") {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : "操作失败");
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <el-card class="panel" shadow="never">
    <template #header>
      <div class="panel-head">
        <span class="panel-title">④ 换角调整（店长确认）</span>
        <span class="panel-sub">角色定版后，任何换角先申请、再由店长确认生效</span>
      </div>
    </template>

    <el-form label-position="top" class="swap-form">
      <div class="swap-grid">
        <el-form-item label="调整角色">
          <el-select v-model="roleId" placeholder="选择角色" class="grow" @change="onRoleChange">
            <el-option
              v-for="seat in session.seats"
              :key="seat.roleId"
              :label="`${seat.roleName}（${GENDER_LABELS[seat.roleGenderRequirement]}）现：${seat.playerName ?? '空位'}`"
              :value="seat.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="接替玩家（须符合性别要求）">
          <el-select v-model="toPlayerId" placeholder="选择接替玩家" filterable class="grow">
            <el-option
              v-for="player in candidates"
              :key="player.playerId"
              :label="`${player.name}（${GENDER_LABELS[player.gender]}）`"
              :value="player.playerId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="换角原因">
          <el-input v-model="reason" maxlength="200" placeholder="可选" />
        </el-form-item>
      </div>
      <el-button type="primary" :loading="busy" @click="submitSwap">提交换角申请</el-button>
    </el-form>

    <el-divider content-position="left">换角记录</el-divider>
    <el-table :data="detail.swapRequests" size="default">
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <span>{{ row.roleName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="换出 → 换入" min-width="200">
        <template #default="{ row }">
          <span class="swap-flow">
            {{ row.fromPlayerName ?? "空位" }}
            <span class="swap-arrow">→</span>
            {{ row.toPlayerName }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" min-width="140" />
      <el-table-column label="状态" width="130">
        <template #default="{ row }">
          <el-tag :type="SWAP_STATUS_TAGS[row.status as 'PENDING' | 'APPROVED' | 'REJECTED']" size="small">
            {{ SWAP_STATUS_LABELS[row.status as 'PENDING' | 'APPROVED' | 'REJECTED'] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间 / 意见" min-width="180">
        <template #default="{ row }">
          <div class="swap-meta">{{ formatDateTime(row.createdAt) }} · {{ row.createdBy }}</div>
          <div v-if="row.reviewNote" class="swap-review">
            {{ row.reviewedBy }}：{{ row.reviewNote }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="店长操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button link type="success" :disabled="busy" @click="review(row.id, true)">确认</el-button>
            <el-button link type="danger" :disabled="busy" @click="review(row.id, false)">驳回</el-button>
          </template>
          <span v-else class="swap-done">已处理</span>
        </template>
      </el-table-column>
      <template #empty>暂无换角记录。</template>
    </el-table>
  </el-card>
</template>
