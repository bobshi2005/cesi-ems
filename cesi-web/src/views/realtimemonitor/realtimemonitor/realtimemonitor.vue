<template>
  <div class="page">
    <div class="page-container">
      <!-- 左侧树 -->
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" ParentModelCode="YSCJMX" autoSelectFirstLeaf />
      </div>

      <!-- 右侧内容 -->
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="rt-filter-bar">
          <span class="rt-filter-label">能源类型</span>
          <el-select
            v-model="queryParams.energyType"
            placeholder="能源类型"
            size="small"
            class="rt-select"
            @change="handleQuery"
          >
            <el-option
              v-for="item in energyTypeList"
              :key="item.enersno"
              :label="item.enername"
              :value="item.enersno"
            />
          </el-select>
          <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button size="small" icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button size="small" type="primary" icon="Refresh" circle @click="handleQuery" />
        </div>

        <!-- 数据区 -->
        <div class="rt-content" v-loading="loading">

          <div v-if="!queryParams.nodeId" class="rt-empty">
            <el-icon><InfoFilled /></el-icon>
            请在左侧选择节点
          </div>
          <div v-else-if="loading !== true && energyRealTimeMonitorList.length === 0 && !isLeafNode()" class="rt-empty">
            <el-icon><FolderOpened /></el-icon>
            该节点没有监测数据，请选择其下的子节点
          </div>

          <template v-else>
            <div
              v-for="(group, gi) in energyRealTimeMonitorList"
              :key="gi"
              v-show="group.deviceArray && group.deviceArray.length > 0"
              class="rt-group"
            >
              <!-- 分组标题行 -->
              <div class="rt-group-head">
                <div class="rt-group-title">
                  <span class="rt-group-dot"></span>
                  {{ queryParams.nodeName }} &nbsp;·&nbsp; {{ group.energyTypeName }}
                </div>
                <!-- 设备选择芯片 -->
                <div class="rt-chips">
                  <button
                    v-for="(device, di) in group.deviceArray"
                    :key="di"
                    class="rt-chip"
                    :class="{ active: di === group.activeIndex }"
                    @click="handleClick(group, di)"
                  >
                    {{ device.deviceName }}
                  </button>
                </div>
              </div>

              <!-- 测点指标卡片网格 -->
              <div
                v-if="group.deviceArray[group.activeIndex]"
                class="rt-metrics-grid"
              >
                <div
                  v-for="(point, pi) in group.deviceArray[group.activeIndex].energyIndexArray"
                  :key="pi"
                  class="rt-metric-card"
                  :class="accentClasses[pi % accentClasses.length]"
                  @click="handleChartModal(point)"
                >
                  <div class="rt-metric-label">
                    {{ point.name }}
                    <span v-if="point.unit" class="rt-metric-unit">（{{ point.unit }}）</span>
                  </div>
                  <div class="rt-metric-value">
                    {{ point.value != null ? point.value.toFixed(2) : '—' }}
                  </div>
                  <div class="rt-metric-time">
                    <el-icon><Timer /></el-icon>
                    {{ point.dataTime || '—' }}
                  </div>
                </div>
              </div>
            </div>

            <div
              v-if="energyRealTimeMonitorList.length === 0 && !loading"
              class="rt-empty"
            >
              <el-icon><DataLine /></el-icon>
              暂无数据
            </div>
          </template>
        </div>
      </div>
    </div>

    <chartModal ref="chartRef" />
  </div>
</template>

<script setup name="energy-real-time-monitor">
import chartModal from "./components/chart-modal.vue"
import { listEnergyRealTimeMonitor } from "@/api/realTimeMonitor/realTimeMonitor"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import { InfoFilled, DataLine, FolderOpened } from "@element-plus/icons-vue"

const { proxy }     = getCurrentInstance()
const route         = useRoute()

const energyTypeList            = ref([])
const energyRealTimeMonitorList = ref([])
const loading                   = ref(false)
const chartRef                  = ref()
const accentClasses             = ['blue', 'orange', 'yellow', 'green', 'purple']
const hasChildren               = ref(false)

const data = reactive({
  queryParams: {
    nodeId:     null,
    nodeName:   null,
    energyType: null,
  },
  query: { ...route.query },
})
const { queryParams, query } = toRefs(data)

function handleNodeClick(node, treeData) {
  queryParams.value.nodeId   = node.id
  queryParams.value.nodeName = node.label
  hasChildren.value          = node.children?.length > 0
  listEnergyTypeList().then((res) => {
    energyTypeList.value           = res.data || []
    queryParams.value.energyType   = energyTypeList.value[0]?.enersno ?? null
    handleQuery()
  })
}

function isLeafNode() {
  return !hasChildren.value
}

function handleClick(group, index) {
  group.activeIndex = index
}

function getList() {
  loading.value = true
  listEnergyRealTimeMonitor(
    proxy.addDateRange({ ...queryParams.value, ...query.value })
  ).then((res) => {
    loading.value = false
    if (res.code === 200) {
      energyRealTimeMonitorList.value = (res.data || []).map(item => ({
        ...item,
        activeIndex: 0,
      }))
    }
  })
}

function handleQuery() {
  energyRealTimeMonitorList.value = []
  getList()
}

function resetQuery() {
  queryParams.value.energyType = null
  energyRealTimeMonitorList.value = []
  handleQuery()
}

function handleChartModal(point) {
  if (chartRef.value) {
    point.nodeName = queryParams.value.nodeName
    chartRef.value.handleOpen(point)
  }
}
</script>

<style scoped>
/* ── 左右布局（page-container 默认 block，需手动设 flex） ──────────────── */
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

/* ── 筛选栏 ─────────────────────────────────────────────────────────── */
.rt-filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.rt-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.rt-select { width: 160px; }

/* ── 内容区 ─────────────────────────────────────────────────────────── */
.rt-content {
  flex: 1;
  overflow-y: auto;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  height: calc(100vh - 210px);
}

/* ── 空状态 ─────────────────────────────────────────────────────────── */
.rt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 60px;
}
.rt-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 分组卡片 ────────────────────────────────────────────────────────── */
.rt-group {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}

/* ── 分组头部 ────────────────────────────────────────────────────────── */
.rt-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  gap: 8px;
}
.rt-group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  letter-spacing: .4px;
}
.rt-group-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--ems-accent);
  flex-shrink: 0;
}

/* ── 设备芯片 ────────────────────────────────────────────────────────── */
.rt-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.rt-chip {
  padding: 0 12px;
  height: 26px;
  line-height: 26px;
  background: rgba(0,170,255,.08);
  border: 1px solid var(--ems-border-dim);
  color: var(--ems-text-secondary);
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all .2s;
  white-space: nowrap;
}
.rt-chip:hover {
  background: rgba(0,170,255,.15);
  color: var(--ems-accent-bright);
}
.rt-chip.active {
  background: var(--ems-accent);
  border-color: var(--ems-accent);
  color: #fff;
}

/* ── 测点网格 ────────────────────────────────────────────────────────── */
.rt-metrics-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 12px 16px;
}

/* ── 测点卡片 ────────────────────────────────────────────────────────── */
.rt-metric-card {
  flex: 1 1 160px;
  min-width: 160px;
  max-width: 220px;
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  padding: 12px 14px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: border-color .2s, transform .1s;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.rt-metric-card:hover {
  border-color: var(--ems-accent);
  transform: translateY(-1px);
}
/* 顶部彩色渐变线 */
.rt-metric-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
}
.rt-metric-card.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
.rt-metric-card.orange::before { background: linear-gradient(90deg, transparent, var(--ems-accent-orange), transparent); }
.rt-metric-card.yellow::before { background: linear-gradient(90deg, transparent, #ffc940, transparent); }
.rt-metric-card.green::before  { background: linear-gradient(90deg, transparent, var(--ems-accent-green), transparent); }
.rt-metric-card.purple::before { background: linear-gradient(90deg, transparent, #8b5cf6, transparent); }

.rt-metric-label {
  font-size: 11px;
  color: var(--ems-text-muted);
  letter-spacing: .4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rt-metric-unit { font-size: 10px; }

.rt-metric-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}
.rt-metric-card.blue   .rt-metric-value { color: var(--ems-accent-bright); }
.rt-metric-card.orange .rt-metric-value { color: var(--ems-accent-orange); }
.rt-metric-card.yellow .rt-metric-value { color: #ffc940; }
.rt-metric-card.green  .rt-metric-value { color: var(--ems-accent-green); }
.rt-metric-card.purple .rt-metric-value { color: #8b5cf6; }

.rt-metric-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--ems-text-muted);
  margin-top: 2px;
}
.rt-metric-time .el-icon { font-size: 11px; }
</style>
