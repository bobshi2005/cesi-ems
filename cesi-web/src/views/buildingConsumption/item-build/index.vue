<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" />
      </div>
      <div class="page-container-right">
        <!-- 页面头部 -->
        <div class="page-header">
          <div class="page-title">{{ queryParams.nodeName || '分项能耗分析' }}</div>
          <div class="header-actions">
            <div class="header-tag">{{ headerTag }}</div>
          </div>
        </div>

        <!-- 筛选栏 -->
        <div class="filter-bar">
          <span class="filter-label">时间周期</span>
          <div class="period-btns">
            <button
              v-for="dict in period" :key="dict.value"
              class="period-btn"
              :class="{ active: queryParams.timeType === dict.value }"
              @click="handleTimeType(dict.value)"
            >{{ dict.label }}</button>
          </div>
          <el-date-picker
            v-model="queryParams.dataTime"
            :type="queryParams.timeType == 'YEAR' ? 'year' : queryParams.timeType == 'MONTH' ? 'month' : 'date'"
            :format="queryParams.timeType == 'YEAR' ? 'YYYY' : queryParams.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
            value-format="YYYY-MM-DD"
            placeholder="时间"
            style="width: 160px"
          />
          <el-select v-model="queryParams.energyType" placeholder="能源类型" style="width: 140px">
            <el-option
              :label="item.enername" :value="item.enersno"
              v-for="item in energyTypeList" :key="item.enersno"
              @click="handleEnergyType(item)"
            />
          </el-select>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
        </div>

        <!-- 内容区域 -->
        <div class="content-area" v-loading="loading">
          <!-- 汇总卡片 -->
          <div class="summary-strip">
            <div class="scard blue">
              <div class="scard-label">总用量 / {{ queryParams.muid }}</div>
              <div class="scard-value">{{ itemBuildData.total ?? '—' }}</div>
            </div>
            <div class="scard orange">
              <div class="scard-label">最大用量 / {{ queryParams.muid }}</div>
              <div class="scard-value">{{ itemBuildData.max ?? '—' }}</div>
            </div>
            <div class="scard yellow">
              <div class="scard-label">最小用量 / {{ queryParams.muid }}</div>
              <div class="scard-value">{{ itemBuildData.min ?? '—' }}</div>
            </div>
            <div class="scard green">
              <div class="scard-label">平均用量 / {{ queryParams.muid }}</div>
              <div class="scard-value">{{ itemBuildData.avg ?? '—' }}</div>
            </div>
          </div>

          <!-- 图表卡片 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-分项用能分析</span>
            </div>
            <div ref="chartRef" class="chart-box"></div>
          </div>

          <!-- 详情表格卡片 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-分项用能详情-{{ queryParams.enername }}</span>
            </div>
            <div class="table-wrap">
              <el-table :data="departmentList" size="small" stripe>
                <el-table-column label="节点" align="center" prop="nodeName" fixed="left" />
                <el-table-column label="合计" align="center" prop="total" width="100" fixed="left" />
                <template v-if="queryParams.timeType == 'DAY'">
                  <el-table-column
                    v-for="index in 24" :key="index"
                    :label="index - 1 + '时'" align="center" min-width="130"
                  >
                    <template #default="scope">{{ scope.row[`value${index - 1}`] }}</template>
                  </el-table-column>
                </template>
                <template v-if="queryParams.timeType == 'MONTH'">
                  <el-table-column
                    v-for="index in 31" :key="index"
                    :label="index + '日'" align="center" min-width="130"
                  >
                    <template #default="scope">{{ scope.row[`value${index - 1}`] }}</template>
                  </el-table-column>
                </template>
                <template v-if="queryParams.timeType == 'YEAR'">
                  <el-table-column
                    v-for="index in 12" :key="index"
                    :label="index + '月'" align="center" min-width="130"
                  >
                    <template #default="scope">{{ scope.row[`value${index - 1}`] }}</template>
                  </el-table-column>
                </template>
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="itemBuild">
import * as echarts from "echarts"
import buildApi from "@/api/buildingConsumption/api"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import useSettingsStore from "@/store/modules/settings"

const { proxy } = getCurrentInstance()
const { period } = proxy.useDict("period")
const settingsStore = useSettingsStore()

const energyTypeList = ref([])
const departmentList = ref([])
const loading = ref(false)
const itemBuildData = ref({})

const data = reactive({
  queryParams: {
    nodeId: null,
    nodeName: null,
    timeType: null,
    dataTime: null,
    energyType: null,
    enername: null,
    muid: null,
  },
  query: {
    modelCode: null,
  },
})
const { queryParams, query } = toRefs(data)

// ── chart ref ──
const chartRef = ref(null)
let myChart = null
let cachedXData = []
let cachedYData = []

// ── header tag ──
const headerTag = computed(() => {
  const map = { DAY: '日', MONTH: '月', YEAR: '年' }
  return `分项能耗 · ${map[queryParams.value.timeType] || ''}视图`
})

// ── chart colors helper ──
function chartColors() {
  const isDark = settingsStore.sideTheme === 'theme-dark'
  return {
    text: isDark ? '#6e90b8' : '#475569',
    line: isDark ? 'rgba(0,140,220,.12)' : 'rgba(0,0,0,.06)',
  }
}

// ── theme watch ──
watch(
  () => settingsStore.sideTheme,
  () => {
    if (cachedXData.length) {
      renderChart(cachedXData, cachedYData)
    }
  }
)

// ── node click ──
function handleNodeClick(data) {
  queryParams.value.nodeId = data.id
  queryParams.value.nodeName = data.label
  queryParams.value.timeType = period.value[0].value
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data
    if (energyTypeList.value.length) {
      queryParams.value.energyType = energyTypeList.value[0].enersno
      queryParams.value.enername = energyTypeList.value[0].enername
      queryParams.value.muid = energyTypeList.value[0].muid
    }
    handleQuery()
  })
}

function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  handleQuery()
}

function handleEnergyType(item) {
  queryParams.value.enername = item.enername
  queryParams.value.muid = item.muid
}

function handleQuery() {
  getList()
}

function getList() {
  loading.value = true
  buildApi
    .itemizedEnergyAnalysis(
      proxy.addDateRange({
        ...queryParams.value,
        ...query.value,
      })
    )
    .then((res) => {
      loading.value = false
      if (res.code && res.code === 200) {
        itemBuildData.value = res.data
        const dataList = res.data.dataList || []
        departmentList.value = dataList

        const xData = []
        const yData = []
        if (dataList.length) {
          if (queryParams.value.timeType === "DAY") {
            for (let i = 0; i < 24; i++) {
              xData.push(i + "时")
              yData.push(dataList[0][`value${i}`])
            }
          } else if (queryParams.value.timeType === "MONTH") {
            for (let i = 0; i < 31; i++) {
              xData.push(i + 1 + "日")
              yData.push(dataList[0][`value${i}`])
            }
          } else {
            for (let i = 0; i < 12; i++) {
              xData.push(i + 1 + "月")
              yData.push(dataList[0][`value${i}`])
            }
          }
          cachedXData = xData
          cachedYData = yData
          renderChart(xData, yData)
        }
      }
    })
    .catch(() => {
      loading.value = false
    })
}

function renderChart(xData, yData) {
  if (!xData || !xData.length) return
  if (!myChart) {
    const dom = chartRef.value
    if (!dom) return
    myChart = echarts.init(dom)
  }
  const { text, line } = chartColors()
  myChart.setOption({
    color: ['#00aaff'],
    tooltip: { trigger: 'axis' },
    grid: { top: 40, left: '2%', right: '5%', bottom: 10, containLabel: true },
    xAxis: {
      type: 'category', data: xData,
      axisLine: { lineStyle: { color: text } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { color: text, fontSize: 12, padding: [5, 0, 0, 0] },
    },
    yAxis: {
      type: 'value', name: queryParams.value.muid || '',
      nameTextStyle: { color: text, fontSize: 12, padding: [0, 0, 5, 0] },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { show: true, lineStyle: { type: 'dashed', color: line } },
      axisLabel: { color: text, fontSize: 12 },
    },
    series: [{
      name: queryParams.value.enername || '',
      type: 'line',
      data: yData,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(0,170,255,.25)' },
          { offset: 1, color: 'rgba(0,170,255,.02)' },
        ]),
      },
    }],
  })
}

onMounted(() => {
  window.addEventListener('resize', () => myChart?.resize(), { passive: true })
})
</script>

<style scoped lang="scss">
/* ─── LAYOUT ─── */
.page-container {
  display: flex;
  height: calc(100vh - 86px);
}

.page-container-left {
  flex-shrink: 0;
  width: 220px;
  border-right: 1px solid var(--ems-border-dim);
  background: var(--ems-bg-card);
}

.page-container-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--ems-bg-main);
}

/* ─── PAGE HEADER ─── */
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px 14px; border-bottom: 1px solid var(--ems-border-dim);
  position: relative; flex-shrink: 0;
}
.page-header::after {
  content: ''; position: absolute; left: 0; bottom: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, var(--ems-accent), transparent);
  opacity: .35;
}
.page-title {
  font-size: 17px; font-weight: 600;
  color: var(--ems-text-primary); letter-spacing: .8px;
}
.header-actions { display: flex; align-items: center; gap: 10px; }
.header-tag {
  font-size: 12px; color: var(--ems-text-muted);
  background: rgba(0,140,255,.08); border: 1px solid rgba(0,140,255,.18);
  border-radius: 12px; padding: 3px 12px;
}

/* ─── FILTER BAR ─── */
.filter-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 20px; border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
  :deep(.el-input__wrapper) { background: var(--ems-bg-card); }
  :deep(.el-input__inner) { color: var(--ems-text-primary); }
}
.filter-label { font-size: 12px; color: var(--ems-text-secondary); white-space: nowrap; }
.period-btns { display: flex; }
.period-btn {
  padding: 0 14px; height: 30px; line-height: 30px;
  background: rgba(0,170,255,.08); border: 1px solid var(--ems-border-dim);
  color: var(--ems-text-secondary); font-size: 13px; cursor: pointer;
  transition: all .2s; white-space: nowrap;
}
.period-btn:first-child { border-radius: 4px 0 0 4px; }
.period-btn:last-child  { border-radius: 0 4px 4px 0; }
.period-btn + .period-btn { border-left: none; }
.period-btn:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
.period-btn.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }

/* ─── CONTENT AREA ─── */
.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 20px;
}

/* ─── SUMMARY STRIP ─── */
.summary-strip {
  display: flex; gap: 12px; padding: 14px 0;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px; flex-wrap: wrap;
}
.scard {
  flex: 1; min-width: 140px;
  background: var(--ems-bg-card); border: 1px solid var(--ems-border-dim);
  border-radius: 8px; padding: 12px 16px;
  position: relative; overflow: hidden;
  display: flex; flex-direction: column; gap: 4px;
}
.scard::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px;
}
.scard.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
.scard.orange::before { background: linear-gradient(90deg, transparent, var(--ems-accent-orange), transparent); }
.scard.yellow::before { background: linear-gradient(90deg, transparent, #ffc940, transparent); }
.scard.green::before  { background: linear-gradient(90deg, transparent, var(--ems-accent-green), transparent); }
.scard-label { font-size: 11px; color: var(--ems-text-muted); letter-spacing: .5px; }
.scard-value { font-size: 22px; font-weight: 700; line-height: 1.2; color: var(--ems-text-primary); }
.scard.blue   .scard-value { color: var(--ems-accent-bright); }
.scard.orange .scard-value { color: var(--ems-accent-orange); }
.scard.yellow .scard-value { color: #ffc940; }
.scard.green  .scard-value { color: var(--ems-accent-green); }

/* ─── CHART CARD ─── */
.chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px; overflow: hidden;
  margin-bottom: 14px;
}
.chart-card-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 16px; font-size: 11.5px; font-weight: 600;
  color: var(--ems-text-secondary); border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.chart-box {
  width: 100%;
  height: 320px;
}

/* ─── TABLE WRAP ─── */
.table-wrap {
  padding: 0;
  :deep(.el-table) {
    --el-table-bg-color: var(--ems-bg-card);
    --el-table-tr-bg-color: var(--ems-bg-card);
    --el-table-header-bg-color: var(--ems-bg-main);
    --el-table-border-color: var(--ems-border-dim);
    --el-table-text-color: var(--ems-text-primary);
    --el-table-header-text-color: var(--ems-text-secondary);
    --el-table-row-hover-bg-color: rgba(0,170,255,.06);
  }
  :deep(.el-table__body-wrapper) { max-height: 420px; overflow-y: auto; }
}

/* ─── LEFT TREE PANEL ─── */
.page-container-left {
  :deep(.tree) {
    height: calc(100vh - 170px);
    max-height: calc(100vh - 170px);
    overflow-y: auto;
  }
  :deep(.el-tree) {
    background: transparent;
    --el-tree-text-color: var(--ems-text-primary);
  }
  :deep(.el-tree-node__content) {
    padding-left: 8px;
  }
  :deep(.el-tree-node__label) {
    font-size: 14px;
  }
  :deep(.el-tree-node__expand-icon) {
    font-size: 14px;
    color: var(--ems-text-muted);
  }
}
</style>
