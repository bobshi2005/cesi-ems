<template>
  <div class="app-container deep-analysis-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <span class="filter-label">能源类型</span>
      <el-select
        v-model="form.energyType"
        placeholder="请选择能源类型"
        style="width: 140px"
      >
        <el-option
          :label="item.enername"
          :value="item.enersno"
          v-for="item in energyTypeList"
          :key="item.enersno"
        />
      </el-select>
      <div class="bar-divider"></div>
      <span class="filter-label">时间周期</span>
      <div class="period-btns">
        <button
          v-for="dict in period" :key="dict.value"
          class="period-btn"
          :class="{ active: form.timeType === dict.value }"
          @click="handleTimeType(dict.value)"
        >{{ dict.label }}</button>
      </div>
      <el-date-picker
        v-model="form.dataTime"
        :type="form.timeType == 'YEAR' ? 'year' : form.timeType == 'MONTH' ? 'month' : 'date'"
        :format="form.timeType == 'YEAR' ? 'YYYY' : form.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
        :value-format="form.timeType == 'YEAR' ? 'YYYY' : form.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
        placeholder="时间"
        style="width: 150px"
        :clearable="false"
      />
      <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
      <el-button icon="Refresh" @click="resetQuery" class="action-btn">重置</el-button>
    </div>

    <!-- 内容区 -->
    <div class="content-area">
      <!-- 汇总卡片 -->
      <div class="summary-strip">
        <div
          v-for="(item, idx) in periodList"
          :key="item.title"
          class="pcard"
          :class="item.accentClass"
        >
          <div class="pcard-title">{{ item.title }}</div>
          <div class="pcard-value" :style="{ color: item.color }">{{ item.data }}</div>
        </div>
      </div>

      <!-- 能源流向图 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>能源流向分析</span>
        </div>
        <div class="chart-body">
          <div id="Chart1" style="width:100%;height:100%"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import useSettingsStore from "@/store/modules/settings"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import { getFlowCharts } from "@/api/energyAnalysis/energyAnalysis"
const settingsStore = useSettingsStore()
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
const { period } = proxy.useDict("period")

const form = ref({
  energyType: null,
  timeType:   null,
  dataTime:   null,
})

const energyTypeList = ref([])
const flowData       = ref([])

const periodList = ref([
  { accentClass: "blue",   color: "#3371eb", title: "累计能耗", data: "—" },
  { accentClass: "orange", color: "#ff6200", title: "分表能耗", data: "—" },
  { accentClass: "yellow", color: "#ffce0c", title: "损失量",   data: "—" },
  { accentClass: "green",  color: "#78e801", title: "损失比例", data: "—" },
])

function handleTimeType(e) {
  form.value.timeType = e
  form.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  getData()
}

function getData() {
  getFlowCharts({
    energyType: form.value.energyType,
    nodeId:     "",
    queryTime:  proxy.dayjs(new Date(form.value.dataTime)).format("YYYY-MM-DD"),
    dataTime:   proxy.dayjs(new Date(form.value.dataTime)).format("YYYY-MM-DD"),
    timeType:   form.value.timeType,
    modelCode:  proxy.$route.query.modelCode,
  }).then((res) => {
    periodList.value[0].data = res.data.totalAccumulatedAmount
    periodList.value[1].data = res.data.childNodeAccumulatedAmount
    periodList.value[2].data = res.data.difference
    periodList.value[3].data = res.data.energyLossRatio + "%"
    flowData.value = res.data.itemVOList || []
    nextTick(() => { renderChart() })
  })
}

function renderChart() {
  const dom = document.getElementById("Chart1")
  if (!dom) return
  if (echarts.getInstanceByDom(dom)) echarts.dispose(dom)
  const myChart = echarts.init(dom)

  const COLORS = ["#FBB4AE", "#B3CDE3", "#CCEBC5", "#DECBE4", "#5470C6"]
  let mydata = [], mylinks = []

  if (flowData.value?.length) {
    mylinks = flowData.value
    const nodeNames = new Set()
    mylinks.forEach((el) => { nodeNames.add(el.source); nodeNames.add(el.target) })
    mydata = [...nodeNames].map((name) => ({ name }))
  }

  myChart.setOption({
    tooltip: { trigger: "item", triggerOn: "mousemove" },
    series: [{
      type: "sankey",
      left: 50, top: 20, right: 100, bottom: 25,
      data: mydata,
      links: mylinks,
      nodeGap: 18,
      layoutIterations: 1,
      nodeAlign: "left",
      lineStyle: { color: "source", curveness: 0.7, opacity: 0.3 },
      itemStyle: { color: "#333", borderColor: "#1f77b4" },
      levels: COLORS.map((color, i) => ({
        depth: i,
        itemStyle: { color },
        lineStyle: { color: "source", opacity: 0.3 },
      })),
    }],
  })
  window.addEventListener("resize", () => myChart.resize(), { passive: true })
}

watch(() => settingsStore.sideTheme, () => { renderChart() })

function handleQuery() { getData() }

function resetQuery() {
  form.value.energyType = energyTypeList.value[0]?.enersno || null
  handleTimeType(period.value[0]?.value || "DAY")
}

onMounted(() => {
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data || []
    if (res.data?.length) {
      form.value.energyType = res.data[0].enersno
    }
    handleTimeType(period.value[0]?.value || "DAY")
  })
})
</script>

<style scoped lang="scss">
/* ─── FILTER BAR ─── */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.bar-divider {
  width: 1px;
  height: 20px;
  background: var(--ems-border-dim);
  margin: 0 4px;
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
  height: calc(100vh - 155px);
  max-height: calc(100vh - 155px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 12px;
}

/* ─── 汇总卡片行 ─── */
.summary-strip {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}
.pcard {
  flex: 1;
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  padding: 16px 18px;
  position: relative;
  overflow: hidden;
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
  }
  &.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
  &.orange::before { background: linear-gradient(90deg, transparent, #ff6200, transparent); }
  &.yellow::before { background: linear-gradient(90deg, transparent, #ffce0c, transparent); }
  &.green::before  { background: linear-gradient(90deg, transparent, #78e801, transparent); }
}
.pcard-title {
  font-size: 12px;
  color: var(--ems-text-muted);
  margin-bottom: 10px;
  letter-spacing: 0.3px;
}
.pcard-value {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

/* ─── CHART CARD ─── */
.chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  flex: 1;
  min-height: 300px;
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
.chart-body {
  width: 100%;
  height: calc(100% - 38px);
  min-height: 300px;
}
</style>
