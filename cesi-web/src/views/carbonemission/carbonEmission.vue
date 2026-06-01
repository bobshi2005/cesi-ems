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
          <el-button icon="Refresh" @click="resetQuery" class="action-btn">重置</el-button>
          <el-button type="warning" icon="Download" @click="handleExport" class="action-btn">导出</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area" v-loading="loading">
          <!-- 碳排放汇总卡片 -->
          <div class="summary-strip" v-if="listTop.length">
            <div
              v-for="(item, idx) in listTop[0]"
              :key="idx"
              class="ecard"
              :class="accentClasses[idx % accentClasses.length]"
            >
              <div class="ecard-label">{{ item.allEneryType }}</div>
              <div class="ecard-value">{{ item.totalEnery ?? '—' }}</div>
              <div class="ecard-unit">tCO₂e</div>
              <div class="ecard-sub">
                <span>同比</span>
                <span
                  class="yoy-val"
                  :class="item.yoyEnery > 0 ? 'up' : item.yoyEnery < 0 ? 'down' : 'flat'"
                >
                  <el-icon v-if="item.yoyEnery > 0" style="vertical-align:-2px"><Top /></el-icon>
                  <el-icon v-else-if="item.yoyEnery < 0" style="vertical-align:-2px"><Bottom /></el-icon>
                  {{ Math.abs(item.yoyEnery ?? 0) }}%
                </span>
              </div>
            </div>
          </div>

          <!-- 碳排放量同环比图 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-碳排放量同环比（{{ queryParams.dataTime }}）</span>
            </div>
            <div class="chart-body">
              <div id="Chart1" style="width:100%;height:100%"></div>
            </div>
          </div>

          <!-- 统计分析表 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-碳排放量统计分析表（{{ queryParams.dataTime }}）</span>
            </div>
            <div class="table-box">
              <el-table :data="listBottom">
                <el-table-column label="时间"             align="center" prop="xaxis" :show-overflow-tooltip="true" />
                <el-table-column label="碳排放量(tCO₂e)"  align="center" prop="yaxis" :show-overflow-tooltip="true" />
                <el-table-column label="同比"             align="center" prop="yoy"   :show-overflow-tooltip="true" />
                <el-table-column label="环比"             align="center" prop="qoq"   :show-overflow-tooltip="true" />
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="carbonEmission">
import { listUpCarbonemission, listMiddleCarbonemission } from "@/api/carbonemission/carbonemission"
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
import { useRoute } from "vue-router"
const { period } = proxy.useDict("period")
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(() => settingsStore.sideTheme, () => { getList() })

const loading   = ref(false)
const listTop   = ref([])
const listBottom = ref([])
const accentClasses = ["blue", "red", "orange", "yellow", "green"]

const data = reactive({
  queryParams: {
    nodeId:   null,
    nodeName: null,
    timeType: null,
    dataTime: null,
  },
  query: { ...useRoute().query },
})
const { queryParams, query } = toRefs(data)

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  handleTimeType(period.value[0].value)
}

function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  handleQuery()
}

function getList() {
  loading.value = true
  listTop.value = []

  // ── 汇总卡片 ──────────────────────────────────────────────────────────
  listUpCarbonemission(
    proxy.addDateRange({ ...queryParams.value, ...query.value })
  ).then((res) => {
    if (res.data?.upData?.length) {
      listTop.value = [res.data.upData.slice(0, 5)]
    }
  })

  // ── 图表 + 表格 ───────────────────────────────────────────────────────
  const chartDom = document.getElementById("Chart1")
  if (chartDom && echarts.getInstanceByDom(chartDom)) echarts.dispose(chartDom)

  listMiddleCarbonemission(
    proxy.addDateRange({ emissionType: "allType", ...queryParams.value, ...query.value })
  ).then((res) => {
    if (!res.code || res.code !== 200) return
    loading.value = false

    const xaxis = [], yaxis = [], yoy = [], qoq = []
    if (res.data) {
      res.data.forEach((item) => {
        xaxis.push(item.xaxis)
        yaxis.push(item.yaxis || 0)
        yoy.push(item.yoy   || 0)
        qoq.push(item.qoq   || 0)
      })
      listBottom.value = res.data
    }

    const isDark    = settingsStore.sideTheme === "theme-dark"
    const textColor = isDark ? "#6e90b8" : "#475569"
    const lineColor = isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)"

    setTimeout(() => {
      const dom = document.getElementById("Chart1")
      if (!dom) return
      echarts.init(dom).setOption({
        color: ["#00aaff", "#00e896", "#ff4d6a"],
        grid: { top: 55, left: "7%", right: "5%", bottom: 10, containLabel: true },
        tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
        legend: { icon: "rect", itemWidth: 14, itemHeight: 10, textStyle: { color: textColor } },
        xAxis: {
          type: "category",
          axisLine: { show: true, lineStyle: { color: textColor } },
          axisTick: { show: false }, splitLine: { show: false },
          axisLabel: { color: textColor, fontSize: 12, padding: [5, 0, 0, 0] },
          data: xaxis,
        },
        yAxis: [
          {
            name: "tCO₂e", type: "value",
            nameTextStyle: { color: textColor, fontSize: 12, padding: [0, 0, 5, 0] },
            axisLine: { show: false }, axisTick: { show: false },
            splitLine: { show: true, lineStyle: { type: "dashed", color: lineColor } },
            axisLabel: { color: textColor, fontSize: 12 },
          },
          {
            name: "%", type: "value", alignTicks: true,
            nameTextStyle: { color: textColor, fontSize: 12, padding: [0, 0, 5, 0] },
            axisLine: { show: false }, axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { color: textColor, fontSize: 12 },
          },
        ],
        series: [
          {
            name: "碳排放量", type: "bar", barWidth: "10",
            itemStyle: { borderRadius: [4, 4, 0, 0] },
            markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
            data: yaxis,
          },
          {
            name: "同比", type: "line", yAxisIndex: 1, symbol: "none",
            tooltip: { valueFormatter: (v) => v + "%" },
            data: yoy,
          },
          {
            name: "环比", type: "line", yAxisIndex: 1, symbol: "none",
            tooltip: { valueFormatter: (v) => v + "%" },
            data: qoq,
          },
        ],
      })
      window.addEventListener("resize", () => echarts.getInstanceByDom(dom)?.resize(), { passive: true })
    }, 100)
  })
}

function handleQuery() { getList() }

function resetQuery() {
  handleTimeType(period.value[0]?.value || "DAY")
}

function handleExport() {
  proxy.download(
    "carbonEmission/export",
    { emissionType: "allType", ...queryParams.value, ...query.value },
    `${queryParams.value.nodeName}-碳排放量核算_${new Date().getTime()}.xlsx`
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

/* ─── 碳排放汇总卡片 ─── */
.summary-strip {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.ecard {
  flex: 1;
  min-width: 140px;
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  padding: 12px 14px;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 2px;
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; height: 2px;
  }
  &.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
  &.red::before    { background: linear-gradient(90deg, transparent, #f52528, transparent); }
  &.orange::before { background: linear-gradient(90deg, transparent, #ff6200, transparent); }
  &.yellow::before { background: linear-gradient(90deg, transparent, #ffce0c, transparent); }
  &.green::before  { background: linear-gradient(90deg, transparent, #78e801, transparent); }
}
.ecard-label {
  font-size: 11px;
  color: var(--ems-text-muted);
  letter-spacing: 0.3px;
}
.ecard-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--ems-text-primary);
}
.ecard-unit {
  font-size: 10px;
  color: var(--ems-text-muted);
}
.ecard-sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--ems-text-muted);
  margin-top: 4px;
}
.yoy-val {
  font-weight: 600;
  &.up   { color: var(--ems-accent-green); }
  &.down { color: var(--ems-accent-red); }
  &.flat { color: var(--ems-text-muted); }
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
.chart-body { width: 100%; height: 300px; }
.table-box  { padding: 0; }
</style>
