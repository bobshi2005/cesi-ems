<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="pf-filter-bar">
          <div class="pf-filter-item">
            <span class="pf-filter-label">时间</span>
            <el-date-picker
              v-model="queryParams.dataTime"
              :type="queryParams.timeType == 'YEAR' ? 'year' : queryParams.timeType == 'MONTH' ? 'month' : 'date'"
              :format="queryParams.timeType == 'YEAR' ? 'YYYY' : queryParams.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
              value-format="YYYY-MM-DD"
              placeholder="时间"
              size="small"
              style="width: 150px"
            />
          </div>
          <div class="pf-filter-item">
            <span class="pf-filter-label">选择电表</span>
            <el-select v-model="queryParams.meterId" placeholder="选择电表" clearable size="small" style="width: 200px">
              <el-option v-for="dict in electricityMeter" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
          <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>

        <!-- 内容区 -->
        <div class="pf-content" v-loading="loading">

          <!-- 空状态 -->
          <div v-if="showEmptyHint" class="pf-empty">
            <el-icon><InfoFilled /></el-icon>
            该节点没有监测数据，请选择其下的子节点
          </div>

          <template v-if="!showEmptyHint">
            <!-- 图表卡片 -->
            <div class="pf-chart-card">
              <div class="pf-chart-head">{{ queryParams.nodeName }} · 功率因数分析</div>
              <div class="pf-chart-body">
                <LineChart ref="LineChartRef" :chartData="lineChartData" :grid="chartGrid" />
              </div>
            </div>

            <!-- 统计表格 -->
            <div class="pf-table-wrap">
              <el-table :data="tableData" stripe>
                <el-table-column label="功率因数" prop="title" align="center" />
                <el-table-column label="最大值" prop="max" align="center" />
                <el-table-column label="最小值" prop="min" align="center" />
                <el-table-column label="平均值" prop="avg" align="center" />
              </el-table>
            </div>
          </template>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="loadAnalysis">
import { powerFactorAnalysisDetail } from "@/api/powerquality/electric-power-factor/api.js"
import { listElectricityDeviceMeter } from "@/api/powerquality/load-analysis/api.js"
import LineChart from "@/components/Echarts/LineChart.vue"
import { InfoFilled } from "@element-plus/icons-vue"

const { proxy }     = getCurrentInstance()
const route         = useRoute()

const loading           = ref(false)
const showEmptyHint     = ref(false)
const tableData         = ref([])
const lineChartData     = ref({})
const electricityMeter  = ref([])
const LineChartRef      = ref()
const chartGrid         = ref({
  top: "25",
  left: "6",
  right: "6",
  bottom: "28",
  containLabel: true,
})

const data = reactive({
  queryParams: {
    nodeId:   null,
    nodeName: null,
    timeType: "DAY",
    dataTime: proxy.dayjs(new Date()).format("YYYY-MM-DD"),
    meterId:  "",
  },
  query: { ...route.query },
})
const { queryParams, query } = toRefs(data)

function getElectricityMeter(params) {
  listElectricityDeviceMeter(params).then((res) => {
    if (res.code === 200) {
      electricityMeter.value = (res.data || []).map((item) => ({ ...item, value: item.code }))
      queryParams.value.meterId = res.data.length > 0 ? res.data[0].code : ""
      getList()
    }
  })
}

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  showEmptyHint.value        = data.children?.length > 0
  if (showEmptyHint.value) return
  getElectricityMeter({ nodeId: queryParams.value.nodeId })
}

function getList() {
  tableData.value = []
  lineChartData.value = { series: [] }
  if (!queryParams.value.meterId) return

  loading.value = true
  let params = {
    nodeId:   queryParams.value.nodeId,
    timeType: queryParams.value.timeType,
    timeCode: queryParams.value.dataTime,
    meterId:  queryParams.value.meterId,
  }
  if (queryParams.value.timeType == "DAY")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM-DD")
  else if (queryParams.value.timeType == "MONTH")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM")
  else if (queryParams.value.timeType == "YEAR")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY")

  powerFactorAnalysisDetail(params)
    .then((res) => {
      loading.value = false
      if (res.code == 200) {
        const detail = res.data.detail || {}
        tableData.value = [
          { title: "发生值", max: detail.max, min: detail.min, avg: detail.avg },
          { title: "发生时间", max: detail.maxTime, min: detail.minTime, avg: "--" },
        ]
        const itemList = res.data.itemList || []
        lineChartData.value = {
          title: "功率因数分析",
          xAxis: itemList.map((i) => (i.timeCode?.slice(0, 2) ?? "") + "时"),
          series: [{ name: "功率因数", data: itemList.map((i) => i.value) }],
          yAxisLabelFontSize: 10,
        }
      }
    })
    .catch(() => { loading.value = false })
}

function handleQuery() { getList() }
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
.pf-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.pf-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.pf-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.pf-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.pf-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.pf-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 图表卡片 ──────────────────────────────────────────────────────────── */
.pf-chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.pf-chart-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.pf-chart-body {
  width: 100%;
  height: 340px;
  padding: 8px 0;
}

/* ── 表格区 ────────────────────────────────────────────────────────────── */
.pf-table-wrap {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  padding: 8px;
}
</style>
