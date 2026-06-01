<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="cv-filter-bar">
          <span class="cv-filter-label" v-if="queryParams.nodeName">{{ queryParams.nodeName }}</span>
          <span class="cv-filter-label cv-filter-muted" v-else>请在左侧选择节点</span>
        </div>

        <!-- 内容区 -->
        <div class="cv-content">

          <!-- 空状态 -->
          <div v-if="!queryParams.nodeId" class="cv-empty">
            <el-icon><InfoFilled /></el-icon>
            请在左侧选择节点
          </div>

          <!-- SVG 组态图 -->
          <template v-else>
            <div class="cv-chart-card">
              <div class="cv-chart-head">{{ queryParams.nodeName }} · 组态图监测</div>
              <div class="cv-chart-body">
                <ConfigureChartView ref="ConfigureChartViewRef" />
              </div>
            </div>
          </template>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="energy-real-time-monitor">
import ConfigureChartView from "../components/configureView.vue"
import { getConfigure } from "@/api/svg/equipmentfile"
import { InfoFilled } from "@element-plus/icons-vue"

const { proxy } = getCurrentInstance()
const route         = useRoute()

const data = reactive({
  queryParams: {
    nodeId:   null,
    nodeName: null,
  },
  query: { ...route.query },
})
const { queryParams, query } = toRefs(data)
const ConfigureChartViewRef = ref()

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  if (!data.id) return

  getConfigure(data.id).then((response) => {
    if (response.code === 200) {
      ConfigureChartViewRef.value.show(data.id)
    } else {
      proxy.$modal.msgError(response.msg)
    }
  })
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
.cv-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.cv-filter-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  white-space: nowrap;
  letter-spacing: .5px;
}
.cv-filter-muted {
  color: var(--ems-text-muted);
  font-weight: 400;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.cv-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.cv-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.cv-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 组态图卡片 ────────────────────────────────────────────────────────── */
.cv-chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  min-height: calc(100vh - 230px);
  display: flex;
  flex-direction: column;
}
.cv-chart-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
  flex-shrink: 0;
}
.cv-chart-body {
  flex: 1;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 16px;
}
.cv-chart-body :deep(svg) {
  max-width: 100%;
  height: auto;
}
</style>
