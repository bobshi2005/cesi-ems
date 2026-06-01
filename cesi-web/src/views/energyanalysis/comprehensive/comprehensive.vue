<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" />
      </div>
      <div class="page-container-right">
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
          <span class="filter-label ml">时间</span>
          <el-date-picker
            v-model="queryParams.dataTime"
            :type="queryParams.timeType == 'YEAR' ? 'year' : queryParams.timeType == 'MONTH' ? 'month' : 'date'"
            :format="queryParams.timeType == 'YEAR' ? 'YYYY' : queryParams.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
            value-format="YYYY-MM-DD"
            placeholder="时间"
            style="width: 140px"
          />
          <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
          <el-button type="warning" icon="Download" @click="handleExport" class="action-btn">导出</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area" v-loading="loading">
          <!-- 综合能耗趋势 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-综合能耗趋势</span>
            </div>
            <div class="chart-body">
              <div id="Chart1" style="width:100%;height:100%"></div>
            </div>
          </div>

          <!-- 中间三列 -->
          <div class="chart-row-3">
            <!-- 同比环比卡片 -->
            <div class="chart-card">
              <div class="chart-card-head">
                <span>{{ queryParams.nodeName }}-综合能耗同比环比</span>
              </div>
              <div class="yoy-list">
                <div
                  v-for="(item, idx) in comprehensiveTable"
                  :key="item.title"
                  class="yoy-card"
                  :class="idx === 0 ? 'blue' : 'orange'"
                >
                  <div class="yoy-top">
                    <span class="yoy-title">{{ item.title }}</span>
                    <span class="yoy-ratio" :class="item.icon > 0 ? 'up' : item.icon < 0 ? 'down' : 'flat'">
                      <el-icon v-if="item.icon > 0" style="vertical-align:-2px"><Top /></el-icon>
                      <el-icon v-else-if="item.icon < 0" style="vertical-align:-2px"><Bottom /></el-icon>
                      {{ Math.abs(item.icon) }}%
                    </span>
                  </div>
                  <div v-for="(node, ni) in item.data" :key="ni" class="yoy-row">
                    <span class="yoy-row-label">{{ node.label }}</span>
                    <span class="yoy-row-value">{{ Number(node.value.toFixed(2)) }} tce</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 各介质综合能耗占比 -->
            <div class="chart-card">
              <div class="chart-card-head">
                <span>{{ queryParams.nodeName }}-各介质综合能耗占比</span>
              </div>
              <div class="chart-body-sm">
                <div id="Chart2" style="width:100%;height:100%"></div>
              </div>
            </div>

            <!-- 用能单元综合能耗排名 -->
            <div class="chart-card">
              <div class="chart-card-head">
                <span>{{ queryParams.nodeName }}-用能单元综合能耗排名</span>
              </div>
              <div class="chart-body-sm">
                <div id="Chart3" style="width:100%;height:100%"></div>
              </div>
            </div>
          </div>

          <!-- 综合能耗统计分析表 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-综合能耗统计分析表</span>
            </div>
            <div class="table-box">
              <el-table :data="comprehensiveList" show-summary>
                <el-table-column
                  label="日期"
                  align="center"
                  key="currentTime"
                  prop="currentTime"
                  :show-overflow-tooltip="true"
                />
                <el-table-column
                  label="综合能耗量(tce)"
                  align="center"
                  key="currentValue"
                  prop="currentValue"
                  :show-overflow-tooltip="true"
                />
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="comprehensive">
import { listComprehensive, listYoY, listEnergyRanking } from "@/api/energyAnalysis/energyAnalysis"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
const { period } = proxy.useDict("period")
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(
  () => settingsStore.sideTheme,
  () => { getList() }
)

const comprehensiveTable = ref([
  { icon: 0, title: "同比", data: [{ label: null, value: 0 }, { label: null, value: 0 }] },
  { icon: 0, title: "环比", data: [{ label: null, value: 0 }, { label: null, value: 0 }] },
])
const comprehensiveList = ref([])
const loading = ref(false)
const data = reactive({
  queryParams: { nodeId: null, timeType: null, dataTime: null },
  query: { modelCode: null },
})
const { queryParams, query } = toRefs(data)

function handleNodeClick(data) {
  queryParams.value.nodeId = data.id
  queryParams.value.nodeName = data.label
  handleTimeType(period.value[0].value)
}
function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  handleQuery()
}

function chartColors() {
  const isDark = settingsStore.sideTheme === "theme-dark"
  return {
    text: isDark ? "#6e90b8" : "#475569",
    line: isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)",
    bg:   isDark ? "rgba(255,255,255,.04)" : "rgba(0,0,0,.04)",
  }
}

function disposeAll() {
  ["Chart1", "Chart2", "Chart3"].forEach((id) => {
    const dom = document.getElementById(id)
    if (dom && echarts.getInstanceByDom(dom)) echarts.dispose(dom)
  })
}

function getList() {
  loading.value = true
  disposeAll()

  const params = proxy.addDateRange({ ...queryParams.value, ...query.value })
  const { text, line, bg } = chartColors()

  // ── 趋势图 ────────────────────────────────────────────────────────────────
  listComprehensive(params).then((res) => {
    if (!res.code || res.code !== 200) return
    loading.value = false
    const xdata = [], yvalue = [], ycompareValue = []
    if (res.data.chartDataList) {
      res.data.chartDataList.forEach((item) => {
        xdata.push(
          proxy.dayjs(item.xdata).format(
            queryParams.value.timeType == "YEAR" ? "MM月"
            : queryParams.value.timeType == "MONTH" ? "DD日" : "HH时"
          )
        )
        yvalue.push(item.yvalue || 0)
        ycompareValue.push(item.ycompareValue || 0)
      })
    }

    setTimeout(() => {
      const dom1 = document.getElementById("Chart1")
      if (!dom1) return
      echarts.init(dom1).setOption({
        color: ["#00aaff", "#00e896", "#ffc940", "#ff4d6a"],
        grid: { top: 55, left: "5%", right: "5%", bottom: 10, containLabel: true },
        tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
        legend: { icon: "rect", itemWidth: 14, itemHeight: 10, textStyle: { color: text } },
        xAxis: {
          type: "category",
          axisLine: { show: true, lineStyle: { color: text } },
          axisTick: { show: false }, splitLine: { show: false },
          axisLabel: { color: text, fontSize: 12, padding: [5, 0, 0, 0] },
          data: xdata,
        },
        yAxis: {
          type: "value", name: "tce",
          nameTextStyle: { color: text, fontSize: 12, padding: [0, 0, 5, 0] },
          axisLine: { show: false }, axisTick: { show: false },
          splitLine: { show: true, lineStyle: { type: "dashed", color: line } },
          axisLabel: { color: text, fontSize: 12 },
        },
        series: [
          {
            name: "本期值", type: "bar", barWidth: "10",
            itemStyle: { borderRadius: [4, 4, 0, 0] },
            tooltip: { valueFormatter: (v) => v + "tce" },
            markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
            data: yvalue,
          },
          {
            name: "同期值", type: "bar", barWidth: "10",
            itemStyle: { borderRadius: [4, 4, 0, 0] },
            tooltip: { valueFormatter: (v) => v + "tce" },
            markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
            data: ycompareValue,
          },
        ],
      })
      window.addEventListener("resize", () => echarts.getInstanceByDom(dom1)?.resize(), { passive: true })
    }, 100)

    // ── 占比饼图 ────────────────────────────────────────────────────────────
    let seriesData = []
    let total = 0
    if (res.data.energyProportion?.length) {
      res.data.energyProportion.forEach((item) => {
        seriesData.push({ name: item.energyName, value: Number(item.count).toFixed(2) })
      })
      seriesData.forEach((v) => { total += Number(v.value) })
      renderPie(seriesData, total, text)
    } else {
      listEnergyTypeList().then((r) => {
        r.data.forEach((item) => seriesData.push({ name: item.enername, value: 0 }))
        renderPie(seriesData, 0, text)
      })
    }

    comprehensiveList.value = res.data.dataList || []
    window.addEventListener("resize", () => {
      echarts.getInstanceByDom(document.getElementById("Chart2"))?.resize()
    }, { passive: true })
  })

  // ── 同比环比 ──────────────────────────────────────────────────────────────
  listYoY(proxy.addDateRange({ ...queryParams.value })).then((res) => {
    if (res.data.tongbi) {
      const t = res.data.tongbi
      comprehensiveTable.value[0].icon          = t.ratio        || 0
      comprehensiveTable.value[0].data[0].label = t.currentTime  || null
      comprehensiveTable.value[0].data[0].value = t.currentValue || 0
      comprehensiveTable.value[0].data[1].label = t.compareTime  || null
      comprehensiveTable.value[0].data[1].value = t.compareValue || 0
    }
    if (res.data.huanbi) {
      const h = res.data.huanbi
      comprehensiveTable.value[1].icon          = h.ratio        || 0
      comprehensiveTable.value[1].data[0].label = h.currentTime  || null
      comprehensiveTable.value[1].data[0].value = h.currentValue || 0
      comprehensiveTable.value[1].data[1].label = h.compareTime  || null
      comprehensiveTable.value[1].data[1].value = h.compareValue || 0
    }
  })

  // ── 能耗排名 ──────────────────────────────────────────────────────────────
  listEnergyRanking(proxy.addDateRange({ ...queryParams.value })).then((res) => {
    const nodeName = [], energyConsumption = []
    if (res.data) {
      res.data.forEach((item) => {
        nodeName.push(item.nodeName)
        energyConsumption.push(item.energyConsumption || 0)
      })
    }
    setTimeout(() => {
      const dom3 = document.getElementById("Chart3")
      if (!dom3) return
      echarts.init(dom3).setOption({
        grid: { left: "2%", right: "2%", top: "4%", bottom: 0, containLabel: true },
        tooltip: {
          trigger: "axis", axisPointer: { type: "none" },
          formatter: (p) => `${p[0].name} : ${p[0].value}`,
        },
        xAxis: { show: false, type: "value" },
        yAxis: [
          {
            type: "category", inverse: true,
            splitLine: { show: false }, axisTick: { show: false }, axisLine: { show: false },
            axisLabel: {
              interval: 0, color: text, fontSize: 13,
              formatter: (value, index) => {
                const n = index + 1
                if (index === 0) return `{idx0|${n}}{title|${value}}`
                if (index === 1) return `{idx1|${n}}{title|${value}}`
                if (index === 2) return `{idx2|${n}}{title|${value}}`
                return `{idx|${n}}{title|${value}}`
              },
              rich: {
                idx0:  { color: "#FF0004", backgroundColor: "#FF000426", borderRadius: 100, padding: [5, 8] },
                idx1:  { color: "#FF8400", backgroundColor: "#FF84001F", borderRadius: 100, padding: [5, 8] },
                idx2:  { color: "#FFDD00", backgroundColor: "#FFDD001F", borderRadius: 100, padding: [5, 8] },
                idx:   { color: "#3371EB", backgroundColor: "#3371EB26", borderRadius: 100, padding: [5, 8] },
                title: { padding: [5, 8] },
              },
            },
            data: nodeName,
          },
          {
            type: "category", inverse: true, axisTick: "none", axisLine: "none", show: true,
            axisLabel: { color: text, fontSize: 12 },
            data: energyConsumption,
          },
        ],
        series: [
          {
            type: "bar", showBackground: true,
            backgroundStyle: { color: bg },
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: "#0437FF" }, { offset: 1, color: "#55C6FF" },
              ]),
            },
            barWidth: "10", data: energyConsumption,
          },
          {
            type: "pictorialBar", symbol: "rect", symbolSize: [4, 14],
            symbolPosition: "end", itemStyle: { color: "#488BFF" },
            data: energyConsumption,
          },
        ],
      })
      window.addEventListener("resize", () => echarts.getInstanceByDom(dom3)?.resize(), { passive: true })
    }, 100)
  })
}

function renderPie(seriesData, total, text) {
  setTimeout(() => {
    const dom2 = document.getElementById("Chart2")
    if (!dom2) return
    echarts.init(dom2).setOption({
      color: ["#00aaff", "#00e896", "#ffc940", "#ff9240", "#8b5cf6"],
      grid: { top: "20%", left: "1%", right: "1%", bottom: "0%", containLabel: true },
      tooltip: { trigger: "item" },
      legend: {
        type: "scroll", orient: "vertical", top: "center", icon: "circle", right: "5%",
        itemWidth: 14, itemHeight: 14, itemGap: 16,
        textStyle: {
          align: "left", verticalAlign: "middle",
          rich: {
            name:  { color: text, fontSize: 13 },
            value: { color: text, fontSize: 13 },
            rate:  { color: text, fontSize: 13 },
          },
        },
        data: seriesData,
        formatter: (name) => {
          for (const d of seriesData) {
            if (d.name === name) {
              const pct = total ? ((d.value / total) * 100).toFixed(2) : 0
              return `{name|${name}(tce)  }{value| ${d.value}} {rate| ${pct}%}`
            }
          }
        },
      },
      series: [{
        name: "各介质能耗占比", type: "pie",
        radius: ["40%", "65%"], center: ["28%", "50%"],
        avoidLabelOverlap: false,
        label: { show: false }, labelLine: { show: false },
        data: seriesData,
      }],
    })
  }, 100)
}

function handleQuery() {
  getList()
}
function handleExport() {
  proxy.download(
    "consumptionanalysis/comprehensiveEnergyExport",
    { ...queryParams.value, ...query.value },
    `${queryParams.value.nodeName}-综合能耗统计分析表_${new Date().getTime()}.xlsx`
  )
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
  &.ml { margin-left: 8px; }
}
.period-btns { display: flex; }
.period-btn {
  padding: 0 14px;
  height: 30px;
  line-height: 30px;
  background: rgba(0, 170, 255, 0.08);
  border: 1px solid var(--ems-border-dim);
  color: var(--ems-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  &:first-child { border-radius: 4px 0 0 4px; }
  &:last-child  { border-radius: 0 4px 4px 0; }
  & + & { border-left: none; }
  &:hover { background: rgba(0, 170, 255, 0.15); color: var(--ems-accent-bright); }
  &.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }
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
.chart-body    { width: 100%; height: 300px; }
.chart-body-sm { width: 100%; height: 260px; }
.table-box     { padding: 0; }

/* ─── 中间三列 ─── */
.chart-row-3 {
  display: grid;
  grid-template-columns: 7fr 8fr 9fr;
  gap: 12px;
  flex-shrink: 0;
}

/* ─── 同比环比卡片 ─── */
.yoy-list {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(260px - 2px);
  box-sizing: border-box;
  justify-content: center;
}
.yoy-card {
  border-radius: 6px;
  border: 1px solid var(--ems-border-dim);
  background: var(--ems-bg-card);
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
  &.orange::before { background: linear-gradient(90deg, transparent, var(--ems-accent-orange), transparent); }
}
.yoy-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.yoy-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ems-text-primary);
}
.yoy-ratio {
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 2px;
  &.up   { color: var(--ems-accent-green); }
  &.down { color: var(--ems-accent-red); }
  &.flat { color: var(--ems-text-muted); }
}
.yoy-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  border-top: 1px solid var(--ems-border-dim);
  font-size: 12px;
}
.yoy-row-label { color: var(--ems-text-muted); }
.yoy-row-value {
  font-weight: 600;
  color: var(--ems-text-primary);
}
</style>
