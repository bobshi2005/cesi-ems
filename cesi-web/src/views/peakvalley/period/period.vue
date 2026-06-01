<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" />
      </div>
      <div class="page-container-right">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <span class="filter-label">时间</span>
          <el-date-picker
            v-model="queryParams.queryTime"
            format="YYYY-MM"
            value-format="YYYY-MM"
            type="month"
            placeholder="选择月份"
            style="width: 150px"
          />
          <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery" class="action-btn">重置</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area" v-loading="loading">
          <!-- 汇总卡片行 -->
          <div class="summary-strip">
            <div
              v-for="(item, idx) in periodList"
              :key="item.title"
              class="pcard"
              :class="item.accentClass"
            >
              <div class="pcard-title">{{ item.title }}</div>
              <div
                v-for="(node, ni) in item.data"
                :key="ni"
                class="pcard-row"
              >
                <span class="pcard-label">{{ node.label }}</span>
                <span class="pcard-value" :style="{ color: item.color }">{{ node.value }}</span>
              </div>
            </div>
          </div>

          <!-- 尖峰平谷电量图 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-尖峰平谷电量图</span>
            </div>
            <div class="chart-body">
              <div id="Chart1" style="width:100%;height:100%"></div>
            </div>
          </div>

          <!-- 尖峰平谷电费图 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-尖峰平谷电费图</span>
            </div>
            <div class="chart-body">
              <div id="Chart2" style="width:100%;height:100%"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="period">
import { listPeriod } from "@/api/peakValley/period"
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
import { useRoute } from "vue-router"
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(() => settingsStore.sideTheme, () => { getList() })

const loading = ref(false)

const periodList = ref([
  {
    accentClass: "blue",   color: "#3371eb", title: "合计",
    data: [
      { label: "耗电量/KWh", value: 0 },
      { label: "用电费用/¥", value: 0 },
    ],
  },
  {
    accentClass: "red",    color: "#f52528", title: "尖时段",
    data: [
      { label: "耗电量/KWh", value: 0 },
      { label: "耗电占比",   value: "0%" },
      { label: "用电费用/¥", value: 0 },
      { label: "费用占比",   value: "0%" },
    ],
  },
  {
    accentClass: "orange", color: "#ff6200", title: "峰时段",
    data: [
      { label: "耗电量/KWh", value: 0 },
      { label: "耗电占比",   value: "0%" },
      { label: "用电费用/¥", value: 0 },
      { label: "费用占比",   value: "0%" },
    ],
  },
  {
    accentClass: "yellow", color: "#ffce0c", title: "平时段",
    data: [
      { label: "耗电量/KWh", value: 0 },
      { label: "耗电占比",   value: "0%" },
      { label: "用电费用/¥", value: 0 },
      { label: "费用占比",   value: "0%" },
    ],
  },
  {
    accentClass: "green",  color: "#78e801", title: "谷时段",
    data: [
      { label: "耗电量/KWh", value: 0 },
      { label: "耗电占比",   value: "0%" },
      { label: "用电费用/¥", value: 0 },
      { label: "费用占比",   value: "0%" },
    ],
  },
])

const data = reactive({
  queryParams: {
    nodeId:    null,
    nodeName:  null,
    timeType:  "MONTH",
    queryTime: null,
  },
  query: { ...useRoute().query },
})
const { queryParams, query } = toRefs(data)

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  queryParams.value.queryTime = proxy.dayjs(new Date()).format("YYYY-MM")
  handleQuery()
}

function chartColors() {
  const isDark = settingsStore.sideTheme === "theme-dark"
  return {
    text: isDark ? "#6e90b8" : "#475569",
    line: isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)",
  }
}

function disposeChart(id) {
  const dom = document.getElementById(id)
  if (dom && echarts.getInstanceByDom(dom)) echarts.dispose(dom)
}

function buildBarOption(yName, xdata, ytip, ypeak, yflat, ytrough, text, line) {
  return {
    color: ["#f52528", "#ff6200", "#ffce0c", "#78e801"],
    tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
    grid: { top: 55, left: "5%", right: "5%", bottom: 10, containLabel: true },
    legend: { icon: "rect", itemWidth: 14, itemHeight: 10, textStyle: { color: text } },
    xAxis: {
      type: "category",
      axisLine: { show: true, lineStyle: { color: text } },
      axisTick: { show: false }, splitLine: { show: false },
      axisLabel: { color: text, fontSize: 12, padding: [5, 0, 0, 0] },
      data: xdata,
    },
    yAxis: {
      name: yName, type: "value",
      nameTextStyle: { color: text, fontSize: 12, padding: [0, 0, 5, 0] },
      axisLine: { show: false }, axisTick: { show: false },
      splitLine: { show: true, lineStyle: { type: "dashed", color: line } },
      axisLabel: { color: text, fontSize: 12 },
    },
    series: [
      { name: "尖", type: "bar", stack: "total", barWidth: "12", data: ytip },
      { name: "峰", type: "bar", stack: "total", barWidth: "12", data: ypeak },
      { name: "平", type: "bar", stack: "total", barWidth: "12", data: yflat },
      { name: "谷", type: "bar", stack: "total", barWidth: "12", data: ytrough },
    ],
  }
}

function getList() {
  loading.value = true
  listPeriod(
    proxy.addDateRange({ ...queryParams.value, ...query.value })
  ).then((res) => {
    loading.value = false
    if (!res.code || res.code !== 200) return

    // ── 汇总卡片数据 ─────────────────────────────────────────────────────────
    if (res.data.totalVO) {
      const t = res.data.totalVO
      periodList.value[0].data[0].value = t.totalPowerConsumption || 0
      periodList.value[0].data[1].value = t.totalCost             || 0

      periodList.value[1].data[0].value = t.tipPowerConsumption   || 0
      periodList.value[1].data[1].value = (t.tipPowerProportion   || 0) + "%"
      periodList.value[1].data[2].value = t.tipPowerCost          || 0
      periodList.value[1].data[3].value = (t.tipPowerCostProportion || 0) + "%"

      periodList.value[2].data[0].value = t.peakPowerConsumption  || 0
      periodList.value[2].data[1].value = (t.peakPowerProportion  || 0) + "%"
      periodList.value[2].data[2].value = t.peakPowerCost         || 0
      periodList.value[2].data[3].value = (t.peakPowerCostProportion || 0) + "%"

      periodList.value[3].data[0].value = t.flatPowerConsumption  || 0
      periodList.value[3].data[1].value = (t.flatPowerProportion  || 0) + "%"
      periodList.value[3].data[2].value = t.flatPowerCost         || 0
      periodList.value[3].data[3].value = (t.flatPowerCostProportion || 0) + "%"

      periodList.value[4].data[0].value = t.troughPowerConsumption || 0
      periodList.value[4].data[1].value = (t.troughPowerProportion || 0) + "%"
      periodList.value[4].data[2].value = t.troughPowerCost        || 0
      periodList.value[4].data[3].value = (t.troughPowerCostProportion || 0) + "%"
    }

    // ── 图表 ─────────────────────────────────────────────────────────────────
    disposeChart("Chart1")
    disposeChart("Chart2")
    const { text, line } = chartColors()

    if (res.data.powerConsumptionList) {
      const xdata = [], ytip = [], ypeak = [], yflat = [], ytrough = []
      res.data.powerConsumptionList.forEach((item) => {
        xdata.push(Number(item.xdata.slice(-2)) + "日")
        ytip.push(item.ytip     || 0)
        ypeak.push(item.ypeak   || 0)
        yflat.push(item.yflat   || 0)
        ytrough.push(item.ytrough || 0)
      })
      setTimeout(() => {
        const dom1 = document.getElementById("Chart1")
        if (dom1) {
          echarts.init(dom1).setOption(buildBarOption("耗电量(KWH)", xdata, ytip, ypeak, yflat, ytrough, text, line))
          window.addEventListener("resize", () => echarts.getInstanceByDom(dom1)?.resize(), { passive: true })
        }
      }, 100)
    }

    if (res.data.costList) {
      const xdata = [], ytip = [], ypeak = [], yflat = [], ytrough = []
      res.data.costList.forEach((item) => {
        xdata.push(Number(item.xdata.slice(-2)) + "日")
        ytip.push(item.ytip     || 0)
        ypeak.push(item.ypeak   || 0)
        yflat.push(item.yflat   || 0)
        ytrough.push(item.ytrough || 0)
      })
      setTimeout(() => {
        const dom2 = document.getElementById("Chart2")
        if (dom2) {
          echarts.init(dom2).setOption(buildBarOption("电费(元)", xdata, ytip, ypeak, yflat, ytrough, text, line))
          window.addEventListener("resize", () => echarts.getInstanceByDom(dom2)?.resize(), { passive: true })
        }
      }, 100)
    }
  })
}

function handleQuery() { getList() }

function resetQuery() {
  queryParams.value.queryTime = proxy.dayjs(new Date()).format("YYYY-MM")
  handleQuery()
}
</script>

<style scoped lang="scss">
@import "@/assets/styles/page.scss";

/* ─── FILTER BAR ─── */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.action-btn { height: 30px !important; padding: 0 14px !important; }

/* ─── CONTENT AREA ─── */
.content-area {
  padding: 0 12px 12px;
  height: calc(100vh - 215px);
  max-height: calc(100vh - 215px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ─── 汇总卡片行 ─── */
.summary-strip {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.pcard {
  flex: 1;
  min-width: 150px;
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  padding: 12px 14px;
  position: relative;
  overflow: hidden;
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
  }
  &.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
  &.red::before    { background: linear-gradient(90deg, transparent, #f52528, transparent); }
  &.orange::before { background: linear-gradient(90deg, transparent, #ff6200, transparent); }
  &.yellow::before { background: linear-gradient(90deg, transparent, #ffce0c, transparent); }
  &.green::before  { background: linear-gradient(90deg, transparent, #78e801, transparent); }
}
.pcard-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ems-text-primary);
  margin-bottom: 10px;
}
.pcard-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 3px 0;
  border-top: 1px solid var(--ems-border-dim);
  font-size: 12px;
}
.pcard-label { color: var(--ems-text-muted); }
.pcard-value { font-weight: 600; }

/* ─── CHART CARD ─── */
.chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}
.chart-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 16px;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: 0.5px;
}
.chart-body { width: 100%; height: 320px; }
</style>
