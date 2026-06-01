<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="hd-filter-bar">
          <div class="hd-filter-left">
            <span class="hd-filter-label">时间粒度</span>
            <el-radio-group v-model="queryParams.timeType" @change="changeTimeType" class="hd-radio-group">
              <el-radio-button label="DAY">日</el-radio-button>
              <el-radio-button label="HOUR">小时</el-radio-button>
            </el-radio-group>
          </div>

          <div class="hd-filter-item">
            <span class="hd-filter-label">时间</span>
            <el-date-picker
              v-model="queryParams.dataTime"
              :type="queryParams.timeType == 'DAY' ? 'date' : 'datetime'"
              :format="queryParams.timeType == 'DAY' ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:00:00'"
              :value-format="queryParams.timeType == 'DAY' ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:00:00'"
              placeholder="时间"
              size="small"
              class="hd-datepicker"
            />
          </div>

          <div class="hd-filter-item">
            <span class="hd-filter-label">点位</span>
            <el-select v-model="queryParams.meterId" placeholder="选择点位" clearable size="small" class="hd-select">
              <el-option
                v-for="dict in electricityMeter"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              />
            </el-select>
          </div>

          <div class="hd-filter-actions">
            <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          </div>

          <div class="hd-display-btns">
            <button class="hd-display-btn" :class="{ active: activeKey === 1 }" @click="switchBtnType(1)">图形</button>
            <button class="hd-display-btn" :class="{ active: activeKey === 2 }" @click="switchBtnType(2)">数据</button>
          </div>
        </div>

        <!-- 图表区 -->
        <div class="hd-content" v-loading="loading">
          <div v-show="activeKey === 1" class="hd-chart-card">
            <div class="hd-chart-head">
              {{ queryParams.nodeName || '暂无节点' }}
            </div>
            <div class="hd-chart-box">
              <LineChart ref="LineChartRef" :chartData="lineChartData" :chartType="'bar'" />
            </div>
          </div>

          <!-- 表格区 -->
          <div v-show="activeKey === 2" class="hd-table-wrap">
            <el-table :data="tableData" v-loading="loading" height="calc(100vh - 300px)" stripe>
              <el-table-column label="点位名称" prop="indexName" align="center" />
              <el-table-column label="点位值" prop="value" align="center" />
              <el-table-column label="时间" prop="dataTime" align="center" />
            </el-table>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="loadAnalysis">
import { getHistoricalDataByIndexId, getEnergyIndexByModelId } from "@/api/realTimeMonitor/historyDataTrend.js"
import LineChart from "@/components/Echarts/LineChart.vue"

const { proxy }     = getCurrentInstance()
const route         = useRoute()

const activeKey         = ref(1)
const loading           = ref(false)
const tableData         = ref([])
const lineChartData     = ref({})
const electricityMeter  = ref([])
const LineChartRef      = ref()

const data = reactive({
  queryParams: {
    nodeId:   null,
    nodeName: null,
    timeType: "DAY",
    dataTime: null,
    meterId:  "",
  },
  query: { ...route.query },
})
const { queryParams, query } = toRefs(data)

function getElectricityMeter(params) {
  getEnergyIndexByModelId(params).then((res) => {
    if (res.code === 200) {
      electricityMeter.value = (res.data || []).map((item) => ({
        ...item,
        label: item.indexName,
        value: item.indexId,
      }))
      queryParams.value.meterId = res.data.length > 0 ? res.data[0].indexId : ""
      getList()
    }
  })
}

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  setTimeout(() => handleTimeType(queryParams.value.timeType), 200)
}

function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD HH:00:00")
  getElectricityMeter({ modelId: queryParams.value.nodeId })
}

function changeTimeType(e) {
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD HH:00:00")
  getElectricityMeter({ modelId: queryParams.value.nodeId })
}

function switchBtnType(e) {
  activeKey.value = e
  if (e === 1) getList()
}

function getList() {
  loading.value = true
  let params = {
    nodeId:   queryParams.value.nodeId,
    timeType: queryParams.value.timeType,
    dataTime: queryParams.value.dataTime,
    indexId:  queryParams.value.meterId,
  }
  if (queryParams.value.timeType == "DAY")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM-DD")
  else if (queryParams.value.timeType == "MONTH")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM")
  else if (queryParams.value.timeType == "YEAR")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY")

  getHistoricalDataByIndexId(params)
    .then((res) => {
      loading.value = false
      if (res.code == 200) {
        tableData.value = res.data || []
        const itemList = res.data || []
        lineChartData.value = {
          title: "",
          xAxis: itemList.map((item) =>
            queryParams.value.timeType == "DAY"
              ? item.dataTime.slice(11, 13) + "时"
              : item.dataTime.slice(11, 16)
          ),
          series: [{ name: "点位值", data: itemList.map((i) => i.value) }],
        }
      }
    })
    .catch(() => { loading.value = false })
}

function handleQuery() { getList() }
function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.timeType = null
  queryParams.value.dataTime = null
  handleQuery()
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
.hd-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.hd-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.hd-filter-left,
.hd-filter-item,
.hd-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.hd-radio-group {
  :deep(.el-radio-button__inner) {
    padding: 0 12px;
    height: 30px;
    line-height: 30px;
    font-size: 13px;
    background: rgba(0,170,255,.08);
    border: 1px solid var(--ems-border-dim);
    color: var(--ems-text-secondary);
    box-shadow: none !important;
  }
  :deep(.el-radio-button:first-child .el-radio-button__inner) {
    border-radius: 4px 0 0 4px;
  }
  :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-radius: 0 4px 4px 0;
  }
  :deep(.el-radio-button--active .el-radio-button__inner) {
    background: var(--ems-accent);
    border-color: var(--ems-accent);
    color: #fff;
  }
}
.hd-datepicker { width: 170px; }
.hd-select { width: 200px; }

/* ── 显示切换按钮 ──────────────────────────────────────────────────────── */
.hd-display-btns {
  display: flex;
  margin-left: auto;
}
.hd-display-btn {
  padding: 0 14px;
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
.hd-display-btn:first-child { border-radius: 4px 0 0 4px; }
.hd-display-btn:last-child  { border-radius: 0 4px 4px 0; }
.hd-display-btn + .hd-display-btn { border-left: none; }
.hd-display-btn:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
.hd-display-btn.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.hd-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 图表卡片 ──────────────────────────────────────────────────────────── */
.hd-chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.hd-chart-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.hd-chart-box {
  width: 100%;
  height: 400px;
  :deep(canvas) { width: 100% !important; }
}

/* ── 表格区 ────────────────────────────────────────────────────────────── */
.hd-table-wrap {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  padding: 8px;
}
</style>
