<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" />
      </div>
      <div class="page-container-right">
        <!-- 筛选栏（仅月/年，无日） -->
        <div class="filter-bar">
          <span class="filter-label">时间周期</span>
          <div class="period-btns">
            <button
              v-for="dict in period.filter(d => d.value !== 'DAY')"
              :key="dict.value"
              class="period-btn"
              :class="{ active: queryParams.timeType === dict.value }"
              @click="handleTimeType(dict.value)"
            >{{ dict.label }}</button>
          </div>
          <span class="filter-label ml">时间</span>
          <el-date-picker
            v-model="queryParams.dataTime"
            :type="queryParams.timeType == 'YEAR' ? 'year' : 'month'"
            :format="queryParams.timeType == 'YEAR' ? 'YYYY' : 'YYYY-MM'"
            value-format="YYYY-MM-DD"
            placeholder="时间"
            style="width: 140px"
          />
          <span class="filter-label ml">能源类型</span>
          <el-select v-model="queryParams.energyType" placeholder="能源类型" style="width: 120px">
            <el-option
              :label="item.enername" :value="item.enersno"
              v-for="item in energyTypeList" :key="item.enersno"
              @click="handleEnergyType(item)"
            />
          </el-select>
          <span class="filter-label ml">产品类型</span>
          <el-select v-model="queryParams.prodType" placeholder="产品类型" style="width: 120px">
            <el-option v-for="dict in product_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
          <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery" class="action-btn">重置</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area">
          <!-- 卡片1：批次单耗趋势 -->
          <div class="chart-card" v-loading="loading">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-批次单耗趋势分析</span>
            </div>
            <div class="split-row">
              <!-- 左侧统计 -->
              <div class="stat-panel">
                <div class="stat-item" v-for="item in consumptionAnalysisList[0]" :key="item.title">
                  <div class="stat-icon">
                    <el-icon size="32" :color="item.icon ? item.color : (item.num > 0 ? 'var(--ems-accent-green)' : item.num < 0 ? 'var(--ems-accent-red)' : 'var(--ems-text-muted)')">
                      <component :is="item.icon || (item.num > 0 ? 'Top' : item.num < 0 ? 'Bottom' : 'SemiSelect')" />
                    </el-icon>
                  </div>
                  <div class="stat-text">
                    <div class="stat-label">{{ item.title }}（{{ item.unit }}）</div>
                    <div class="stat-value">{{ item.num }}</div>
                  </div>
                </div>
              </div>
              <!-- 右侧图表 -->
              <div class="chart-area">
                <div id="Chart1" style="width:100%;height:100%"></div>
              </div>
            </div>
          </div>

          <!-- 卡片2：批次能耗与产量趋势 -->
          <div class="chart-card" v-loading="loading">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-批次能耗与产量趋势分析</span>
            </div>
            <div class="split-row">
              <!-- 左侧统计 -->
              <div class="stat-panel">
                <div class="stat-item" v-for="item in consumptionAnalysisList[1]" :key="item.title">
                  <div class="stat-icon">
                    <el-icon size="32" :color="item.color">
                      <component :is="item.icon" />
                    </el-icon>
                  </div>
                  <div class="stat-text">
                    <div class="stat-label">{{ item.title }}（{{ item.unit }}）</div>
                    <div class="stat-value">{{ item.num }}</div>
                  </div>
                </div>
              </div>
              <!-- 右侧图表 -->
              <div class="chart-area">
                <div id="Chart2" style="width:100%;height:100%"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="consumptionAnalysis">
import { listConsumptionanalysis } from "@/api/consumptionanalysis/consumptionanalysis"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
const { period, product_type } = proxy.useDict("period", "product_type")
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(() => settingsStore.sideTheme, () => { getList() })

const energyTypeList = ref([])
const loading        = ref(false)
const data = reactive({
  queryParams: {
    nodeId:    null,
    timeType:  null,
    dataTime:  null,
    energyType: null,
    prodType:  null,
  },
  query: { ...useRoute().query },
})
const { queryParams, query } = toRefs(data)

const consumptionAnalysisList = ref([
  [
    { icon: "Stopwatch",    color: "#00e896", title: "平均批次单耗", unit: null, num: 0 },
  ],
  [
    { icon: "TrendCharts",  color: "#00e896", title: "批次总能耗",   unit: null, num: 0 },
    { icon: "Histogram",    color: "#00aaff", title: "批次总产量",   unit: "ton", num: 0 },
  ],
])

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  // 默认选月视图（取 period 中第一个非 DAY 的值）
  const defaultPeriod = period.value.find(d => d.value !== "DAY")?.value || "MONTH"
  handleTimeType(defaultPeriod)
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data || []
    if (res.data?.length) {
      const first = res.data[0]
      queryParams.value.energyType = first.enersno
      queryParams.value.enername   = first.enername
      queryParams.value.muid       = first.muid
      consumptionAnalysisList.value[0][0].unit = first.muid + "/kg"
      consumptionAnalysisList.value[1][0].unit = first.muid
    }
    queryParams.value.prodType = product_type.value[0]?.value || null
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
  queryParams.value.muid     = item.muid
  consumptionAnalysisList.value[0][0].unit = item.muid + "/kg"
  consumptionAnalysisList.value[1][0].unit = item.muid
  handleQuery()
}

function chartColors() {
  const isDark = settingsStore.sideTheme === "theme-dark"
  return {
    text: isDark ? "#6e90b8" : "#475569",
    line: isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)",
  }
}

function disposeCharts() {
  ["Chart1", "Chart2"].forEach((id) => {
    const dom = document.getElementById(id)
    if (dom && echarts.getInstanceByDom(dom)) echarts.dispose(dom)
  })
}

function getList() {
  loading.value = true
  disposeCharts()

  listConsumptionanalysis(
    proxy.addDateRange({ ...queryParams.value, ...query.value })
  ).then((res) => {
    if (!res.code || res.code !== 200) return
    loading.value = false

    consumptionAnalysisList.value[0][0].num = res.data.averageEnergy || 0
    consumptionAnalysisList.value[1][0].num = res.data.totalEnergy   || 0
    consumptionAnalysisList.value[1][1].num = res.data.totalProduct  || 0

    const dateTime = [], average = [], energyCount = [], productCount = []
    if (res.data.chart) {
      res.data.chart.forEach((item) => {
        dateTime.push(
          proxy.dayjs(item.dateTime).format(
            queryParams.value.timeType == "YEAR" ? "MM月" : "DD日"
          )
        )
        average.push(item.average     || 0)
        energyCount.push(item.energyCount  || 0)
        productCount.push(item.productCount || 0)
      })
    }

    const { text, line } = chartColors()
    const baseAxis = {
      type: "category",
      axisLine: { show: true, lineStyle: { color: text } },
      axisTick: { show: false }, splitLine: { show: false },
      axisLabel: { color: text, fontSize: 12, padding: [5, 0, 0, 0] },
      data: dateTime,
    }
    const baseYAxis = (name) => ({
      type: "value", name,
      nameTextStyle: { color: text, fontSize: 12, padding: [0, 0, 5, 0] },
      axisLine: { show: false }, axisTick: { show: false },
      splitLine: { show: true, lineStyle: { type: "dashed", color: line } },
      axisLabel: { color: text, fontSize: 12 },
    })

    setTimeout(() => {
      // Chart1 — 批次单耗
      const dom1 = document.getElementById("Chart1")
      if (dom1) {
        echarts.init(dom1).setOption({
          color: ["#00aaff"],
          grid: { top: 55, left: "7%", right: "5%", bottom: 10, containLabel: true },
          tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
          legend: { icon: "rect", itemWidth: 14, itemHeight: 10, textStyle: { color: text } },
          xAxis: baseAxis,
          yAxis: baseYAxis(`耗${queryParams.value.enername}量(${queryParams.value.muid}/kg)`),
          series: [{
            name: `耗${queryParams.value.enername}量`,
            type: "bar", barWidth: "10",
            itemStyle: { borderRadius: [4, 4, 0, 0] },
            tooltip: { valueFormatter: (v) => v + queryParams.value.muid },
            markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
            data: average,
          }],
        })
        window.addEventListener("resize", () => echarts.getInstanceByDom(dom1)?.resize(), { passive: true })
      }

      // Chart2 — 批次能耗与产量
      const dom2 = document.getElementById("Chart2")
      if (dom2) {
        echarts.init(dom2).setOption({
          color: ["#00aaff", "#ffc940"],
          grid: { top: 55, left: "7%", right: "5%", bottom: 10, containLabel: true },
          tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
          legend: { icon: "rect", itemWidth: 14, itemHeight: 10, textStyle: { color: text } },
          xAxis: baseAxis,
          yAxis: [
            baseYAxis(`耗${queryParams.value.enername}量(${queryParams.value.muid})`),
            {
              ...baseYAxis("产量(kg)"),
              alignTicks: true,
              splitLine: { show: false },
            },
          ],
          series: [
            {
              name: `耗${queryParams.value.enername}量`,
              type: "bar", barWidth: "10",
              itemStyle: { borderRadius: [4, 4, 0, 0] },
              tooltip: { valueFormatter: (v) => v + queryParams.value.muid },
              markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
              data: energyCount,
            },
            {
              name: "产量", type: "line", yAxisIndex: 1, symbol: "none",
              tooltip: { valueFormatter: (v) => v + "ton" },
              data: productCount,
            },
          ],
        })
        window.addEventListener("resize", () => echarts.getInstanceByDom(dom2)?.resize(), { passive: true })
      }
    }, 100)
  })
}

function handleQuery() { getList() }

function resetQuery() {
  if (energyTypeList.value?.length) {
    const first = energyTypeList.value[0]
    queryParams.value.energyType = first.enersno
    queryParams.value.enername   = first.enername
    queryParams.value.muid       = first.muid
    consumptionAnalysisList.value[0][0].unit = first.muid + "/kg"
    consumptionAnalysisList.value[1][0].unit = first.muid
  }
  queryParams.value.prodType = null
  const defaultPeriod = period.value.find(d => d.value !== "DAY")?.value || "MONTH"
  handleTimeType(defaultPeriod)
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

/* ─── 左右分栏 ─── */
.split-row {
  display: flex;
  height: 300px;
}
.stat-panel {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid var(--ems-border-dim);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24px;
  padding: 0 24px;
}
.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-icon { flex-shrink: 0; }
.stat-text { min-width: 0; }
.stat-label {
  font-size: 11px;
  color: var(--ems-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--ems-text-primary);
  line-height: 1.2;
}
.chart-area {
  flex: 1;
  min-width: 0;
  height: 100%;
}
</style>
