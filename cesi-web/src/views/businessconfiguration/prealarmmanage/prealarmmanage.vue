<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="ps-filter-bar">
          <span class="ps-filter-label" v-if="currentNode">{{ currentNode.label }}</span>
          <span class="ps-filter-label ps-filter-muted" v-else>请在左侧选择节点</span>

          <div class="ps-tabs">
            <button class="ps-tab-btn" :class="{ active: tab === '1' }" @click="handleTab('1')">采集点管理</button>
            <button class="ps-tab-btn" :class="{ active: tab === '2' }" @click="handleTab('2')">统计指标管理</button>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="ps-content">

          <!-- 空状态 -->
          <div v-if="!currentNode" class="ps-empty">
            <el-icon><InfoFilled /></el-icon>
            请在左侧选择节点
          </div>

          <!-- 内容卡片 -->
          <template v-else>
            <div class="ps-card">
              <div class="ps-card-head">{{ currentNode.label }} · 报警设置</div>
              <div class="ps-card-body">
                <div v-if="tab == '1'">
                  <CollectionPointManage ref="collectionPointManageRef" />
                </div>
                <div v-if="tab == '2'">
                  <StatisticalIndicatorsManage ref="statisticalIndicatorsManageRef" />
                </div>
              </div>
            </div>
          </template>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="preAlarmManage">
import CollectionPointManage from "./components/collectionpointmanage/CollectionPointManage.vue"
import StatisticalIndicatorsManage from "./components/statisticalindicatorsmanage/StatisticalIndicatorsManage.vue"
import { InfoFilled } from "@element-plus/icons-vue"

const currentNode                  = ref(null)
const tab                         = ref("1")
const collectionPointManageRef    = ref()
const statisticalIndicatorsManageRef = ref()

function handleTab(value) {
  tab.value = value
  nextTick(() => {
    if (value == 1 && collectionPointManageRef.value) {
      collectionPointManageRef.value.getList(currentNode.value)
    }
    if (value == 2 && statisticalIndicatorsManageRef.value) {
      statisticalIndicatorsManageRef.value.getList(currentNode.value)
    }
  })
}

function handleNodeClick(data) {
  currentNode.value = data
  handleTab(tab.value)
}
</script>

<style scoped>
/* ── 左右 flex 布局 ────────────────────────────────────────────────────── */
.page-container {
  display: flex;
  min-height: calc(100vh - 148px);
}
.page-container-left {
  flex-shrink: 0;
  width: 220px;
  min-height: calc(100vh - 148px);
  border-right: 1px solid var(--ems-border-dim);
  overflow-y: auto;
}
.page-container-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 筛选栏 ────────────────────────────────────────────────────────────── */
.ps-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.ps-filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  letter-spacing: .5px;
}
.ps-filter-muted {
  color: var(--ems-text-muted);
  font-weight: 400;
}

/* ── Tab 切换按钮 ──────────────────────────────────────────────────────── */
.ps-tabs {
  display: flex;
  margin-left: auto;
}
.ps-tab-btn {
  padding: 0 16px;
  height: 30px;
  line-height: 30px;
  background: rgba(0,170,255,.08);
  border: 1px solid var(--ems-border-dim);
  color: var(--ems-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all .2s;
  white-space: nowrap;
}
.ps-tab-btn:first-child { border-radius: 4px 0 0 4px; }
.ps-tab-btn:last-child  { border-radius: 0 4px 4px 0; }
.ps-tab-btn + .ps-tab-btn { border-left: none; }
.ps-tab-btn:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
.ps-tab-btn.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.ps-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.ps-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.ps-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 卡片 ──────────────────────────────────────────────────────────────── */
.ps-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.ps-card-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.ps-card-body {
  padding: 12px;
}
</style>
