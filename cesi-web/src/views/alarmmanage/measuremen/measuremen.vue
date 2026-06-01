<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="am-filter-bar">
          <div class="am-filter-item">
            <span class="am-filter-label">期间</span>
            <el-select v-model="queryParams.timeType" size="small" style="width: 110px" @change="handleTimeType">
              <el-option v-for="dict in period" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
          <div class="am-filter-item">
            <span class="am-filter-label">时间</span>
            <el-date-picker
              v-model="queryParams.dataTime"
              :clearable="false"
              :type="queryParams.timeType == 'YEAR' ? 'year' : queryParams.timeType == 'MONTH' ? 'month' : 'date'"
              :format="queryParams.timeType == 'YEAR' ? 'YYYY' : queryParams.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
              value-format="YYYY-MM-DD"
              placeholder="时间"
              size="small"
              style="width: 150px"
            />
          </div>
          <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>

        <!-- 内容区 -->
        <div class="am-content" v-loading="loading">

          <!-- 空状态 -->
          <div v-if="!queryParams.nodeId" class="am-empty">
            <el-icon><InfoFilled /></el-icon>
            请在左侧选择节点
          </div>

          <template v-if="queryParams.nodeId">
            <!-- 统计概要 -->
            <div class="am-summary">
              本用能单元指标<span>{{ dataArray.indexCount }}</span>个，本年度报警<span>{{ dataArray.yearCount }}</span>次，本月<span>{{ dataArray.monthCount }}</span>次
            </div>

            <!-- 两个饼图 -->
            <div class="am-charts-row">
              <div class="am-chart-card">
                <div class="am-chart-head">报警类型占比</div>
                <div class="am-chart-body"><div id="Chart1" /></div>
              </div>
              <div class="am-chart-card">
                <div class="am-chart-head">能源类型占比</div>
                <div class="am-chart-body"><div id="Chart2" /></div>
              </div>
            </div>

            <!-- 报警次数柱线图 -->
            <div class="am-chart-card">
              <div class="am-chart-head">报警次数</div>
              <div class="am-chart-body am-chart-body-bar"><div id="Chart3" /></div>
            </div>
          </template>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick } from "vue"
import * as echarts from "echarts"
import { InfoFilled } from "@element-plus/icons-vue"
import { getByNodeId, getCountInfo } from "@/api/alarmManage/alarmManage"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"

const { proxy } = getCurrentInstance()
const route         = useRoute()
const { period }                = proxy.useDict("period")
const { alarm_record_category } = proxy.useDict("alarm_record_category")

const loading       = ref(false)
const energyTypeList = ref([])

const queryParams = ref({
  timeType: null,
  dataTime: null,
  nodeId:   null,
})

const dataArray = ref({
  indexCount: 0,
  yearCount:  0,
  monthCount: 0,
})

function getEnergyTypeList() {
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data || []
    queryParams.value.energyType = energyTypeList.value[0]?.enersno
  })
}
getEnergyTypeList()

function formatterLabel(list, value) {
  const dict = list.find((item) => item.enersno == value)
  return dict ? dict.enername : ""
}

function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
}

function handleNodeClick(e) {
  queryParams.value.nodeId = e.id
  handleTimeType(period.value[0].value)
  getByNodeIdFun()
  getCountInfoFun()
}

function getByNodeIdFun() {
  loading.value = true
  getByNodeId(queryParams.value).then((res) => {
    loading.value = false
    if (res.code == 200) {
      const { data } = res
      const alarmList = (data.alarmProportion || []).map((item) => ({
        name:       proxy.selectDictLabel(alarm_record_category.value, item.energyName),
        value:      item.count,
        percentage: item.percentage,
      }))
      const energyList = (data.energyProportion || []).map((item) => ({
        name:       formatterLabel(energyTypeList.value, item.energyName),
        value:      item.count,
        percentage: item.percentage,
      }))
      const alarmNumberList = { data: [], xAxisData: [] }
      for (const item of (data.chartDataList || [])) {
        alarmNumberList.xAxisData.push(item.xdata)
        alarmNumberList.data.push(item.yvalue)
      }
      nextTick(() => {
        pieChart("Chart1", alarmList, "报警类型占比")
        pieChart("Chart2", energyList, "能源类型占比")
        getChart("Chart3", alarmNumberList)
      })
    }
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  getByNodeIdFun()
  getCountInfoFun()
}

function getCountInfoFun() {
  getCountInfo({ ...queryParams.value, modelCode: route.query.modelCode }).then((res) => {
    if (res.code == 200) dataArray.value = res.data
  })
}

// ── 饼图 ──────────────────────────────────────────────────────────────────
function pieChart(Id, data, name) {
  const myChart = echarts.init(document.getElementById(Id))
  myChart.setOption({
    color: ["#4D94FF","#00C27C","#F0142F","#F2D261","#0E7CE2","#FF8352","#E271DE","#F8456B","#00FFFF","#4AEAB0"],
    grid: { top: "20%", left: "1%", right: "1%", bottom: "0%", containLabel: true },
    tooltip: { trigger: "item" },
    legend: {
      orient: "vertical", top: "center", icon: "circle",
      itemWidth: 14, itemHeight: 14, right: "2%", itemGap: 10,
      textStyle: { align: "left", verticalAlign: "middle",
        rich: { name: { color: "#999", fontSize: 14 }, value: { color: "#999", fontSize: 14 }, rate: { color: "#999", fontSize: 14 } },
      },
      formatter: (name) => {
        for (let i = 0; i < data.length; i++) {
          if (data[i].name === name) return `{name|${data[i].name}  }{value| ${data[i].value}} {rate| ${data[i].percentage}%}`
        }
      },
    },
    series: [{ name, type: "pie", radius: ["45%","70%"], center: ["35%","50%"], avoidLabelOverlap: false, label: { show: false }, data }],
  })
}

// ── 柱线图 ────────────────────────────────────────────────────────────────
function getChart(Id, dataList) {
  const myChart = echarts.init(document.getElementById(Id))
  myChart.setOption({
    grid: { left: "3%", right: "2%", bottom: "2%", containLabel: true },
    tooltip: { trigger: "axis" },
    xAxis: {
      type: "category", data: dataList.xAxisData,
      axisTick: { show: false }, splitLine: { show: false },
      axisLabel: { color: "#999", fontSize: 12, padding: [5, 0, 0, 0] },
    },
    yAxis: {
      type: "value", name: "（次）",
      nameTextStyle: { color: "#999", fontSize: 12 },
      axisTick: { show: false },
      splitLine: { show: true, lineStyle: { type: "dashed", color: "rgba(220,222,226,0.4)" } },
      axisLabel: { color: "#999", fontSize: 12 },
    },
    series: [
      { name: "报警次数", type: "bar", barWidth: "12", stack: "number", data: dataList.data, tooltip: { show: false } },
      { name: "报警次数", type: "line", symbol: "none", lineStyle: { color: "#EE0303" }, data: dataList.data },
    ],
  })

  window.addEventListener("resize", () => { myChart.resize() }, { passive: true })
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
.am-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.am-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.am-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.am-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.am-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.am-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 统计概要 ──────────────────────────────────────────────────────────── */
.am-summary {
  font-size: 13px;
  color: var(--ems-text-secondary);
  text-align: center;
  padding: 2px 0;
}
.am-summary span {
  color: var(--ems-accent);
  font-weight: 600;
  margin: 0 4px;
}

/* ── 双饼图行 ──────────────────────────────────────────────────────────── */
.am-charts-row {
  display: flex;
  gap: 14px;
}
.am-charts-row > .am-chart-card {
  flex: 1;
  min-width: 0;
}

/* ── 图表卡片 ──────────────────────────────────────────────────────────── */
.am-chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.am-chart-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.am-chart-body {
  width: 100%;
  height: 300px;
}
.am-chart-body > div {
  width: 100%;
  height: 100%;
}
.am-chart-body-bar {
  height: 340px;
}
</style>
