<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="la-filter-bar">
          <div class="la-filter-item">
            <span class="la-filter-label">期间</span>
            <el-select v-model="queryParams.timeType" size="small" style="width: 110px" @change="handleTimeType">
              <el-option v-for="dict in period" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
          <div class="la-filter-item">
            <span class="la-filter-label">时间</span>
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
          <div class="la-filter-item">
            <span class="la-filter-label">选择电表</span>
            <el-select v-model="queryParams.meterId" placeholder="选择电表" clearable size="small" style="width: 200px">
              <el-option v-for="dict in electricityMeter" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
          <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <div class="la-display-btns">
            <button class="la-display-btn" :class="{ active: activeKey === 1 }" @click="switchBtnType(1)">图形</button>
            <button class="la-display-btn" :class="{ active: activeKey === 2 }" @click="switchBtnType(2)">数据</button>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="la-content" v-loading="loading">

          <!-- 空状态提示 -->
          <div v-if="showEmptyHint" class="la-empty">
            <el-icon><InfoFilled /></el-icon>
            该节点没有监测数据，请选择其下的子节点
          </div>

          <!-- 图表卡片 -->
          <div v-show="activeKey === 1 && !showEmptyHint" class="la-chart-card">
            <div class="la-chart-head">{{ queryParams.nodeName }} · 负荷分析</div>
            <div class="la-chart-body">
              <LineChart ref="LineChartRef" :chartData="lineChartData" :grid="chartGrid" />
            </div>
            <div class="la-stats">
              <div class="la-stat-item">
                <span class="la-stat-label">最大负荷</span>
                <span class="la-stat-value la-max">{{ detailData.max || '--' }}</span>
                <span class="la-stat-time">{{ detailData.maxTime || '' }}</span>
              </div>
              <div class="la-stat-item">
                <span class="la-stat-label">最小负荷</span>
                <span class="la-stat-value la-min">{{ detailData.min || '--' }}</span>
                <span class="la-stat-time">{{ detailData.minTime || '--' }}</span>
              </div>
              <div class="la-stat-item">
                <span class="la-stat-label">平均负荷</span>
                <span class="la-stat-value la-avg">{{ detailData.avg || '--' }}</span>
                <span class="la-stat-time">&nbsp;</span>
              </div>
              <div class="la-stat-item">
                <span class="la-stat-label">负荷率</span>
                <span class="la-stat-value la-rate">{{ detailData.rate || '--' }}</span>
                <span class="la-stat-time">&nbsp;</span>
              </div>
            </div>
          </div>

          <!-- 表格 -->
          <div v-show="activeKey === 2 && !showEmptyHint" class="la-table-wrap">
            <el-table :data="tableData" v-loading="loading" height="calc(100vh - 300px)" stripe>
              <el-table-column label="时间" prop="timeCode" align="center" />
              <el-table-column label="电表名称" prop="name" align="center" />
              <el-table-column v-if="queryParams.timeType == 'DAY'" label="负荷" prop="value" align="center" />
              <el-table-column v-if="queryParams.timeType != 'DAY'" label="最大负荷" prop="max" align="center" />
              <el-table-column v-if="queryParams.timeType != 'DAY'" label="最小负荷" prop="min" align="center" />
              <el-table-column v-if="queryParams.timeType != 'DAY'" label="平均负荷" prop="avg" align="center" />
              <el-table-column v-if="queryParams.timeType != 'DAY'" label="负荷率(%)" prop="rate" align="center" />
            </el-table>
          </div>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="loadAnalysis">
import { loadAnalysisDetail, listElectricityDeviceMeter } from "@/api/powerquality/load-analysis/api.js"
import LineChart from "@/components/Echarts/LineChart.vue"
import { InfoFilled } from "@element-plus/icons-vue"

const { proxy }     = getCurrentInstance()
const route         = useRoute()
const { period }    = proxy.useDict("period")

const activeKey         = ref(1)
const loading           = ref(false)
const showEmptyHint     = ref(false)
const tableData         = ref([])
const detailData        = ref({})
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
    timeType: null,
    dataTime: null,
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
  setTimeout(() => handleTimeType(period.value[0].value), 200)
}

function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  getElectricityMeter({ nodeId: queryParams.value.nodeId })
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
    timeCode: queryParams.value.dataTime,
    meterId:  queryParams.value.meterId,
  }
  if (queryParams.value.timeType == "DAY")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM-DD")
  else if (queryParams.value.timeType == "MONTH")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM")
  else if (queryParams.value.timeType == "YEAR")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY")

  loadAnalysisDetail(params)
    .then((res) => {
      loading.value = false
      if (res.code == 200) {
        tableData.value  = res.data.itemList || []
        detailData.value = res.data.detail || {}
        const itemList   = res.data.itemList || []
        if (queryParams.value.timeType == "DAY") {
          lineChartData.value = {
            title: "负荷分析",
            xAxis: itemList.map((item) => (item.timeCode?.slice(-2) ?? "") + "时"),
            series: [{ name: "负荷值", data: itemList.map((i) => i.value) }],
            yAxisLabelFontSize: 10,
          }
        } else {
          lineChartData.value = {
            title: "负荷分析",
            xAxis: itemList.map((item) => item.timeCodeChart),
            series: [
              { name: "平均负荷", data: itemList.map((i) => i.avg) },
              { name: "最大负荷", data: itemList.map((i) => i.max) },
              { name: "最小负荷", data: itemList.map((i) => i.min) },
            ],
            yAxisLabelFontSize: 10,
          }
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
.la-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.la-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.la-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── 显示切换按钮 ──────────────────────────────────────────────────────── */
.la-display-btns {
  display: flex;
  margin-left: auto;
}
.la-display-btn {
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
.la-display-btn:first-child { border-radius: 4px 0 0 4px; }
.la-display-btn:last-child  { border-radius: 0 4px 4px 0; }
.la-display-btn + .la-display-btn { border-left: none; }
.la-display-btn:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
.la-display-btn.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.la-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 图表卡片 ──────────────────────────────────────────────────────────── */
.la-chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.la-chart-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.la-chart-body {
  width: 100%;
  height: 340px;
  padding: 8px 0;
}

/* ── 统计条 ────────────────────────────────────────────────────────────── */
.la-stats {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
}
.la-stat-item {
  flex: 1;
  min-width: 120px;
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  padding: 10px 14px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.la-stat-item::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--ems-accent), transparent);
}
.la-stat-label {
  font-size: 11px;
  color: var(--ems-text-muted);
  letter-spacing: .4px;
}
.la-stat-value {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.2;
}
.la-max  { color: var(--ems-accent-bright); }
.la-min  { color: var(--ems-accent-orange); }
.la-avg  { color: var(--ems-accent-green); }
.la-rate { color: #ffc940; }
.la-stat-time {
  font-size: 10px;
  color: var(--ems-text-muted);
  word-break: break-all;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.la-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.la-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 表格区 ────────────────────────────────────────────────────────────── */
.la-table-wrap {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  padding: 8px;
}
</style>
