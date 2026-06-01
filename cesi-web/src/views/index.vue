<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title"></div>
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
          :class="{ active: mainPeriod === dict.value }"
          @click="onMainPeriod(dict.value)"
        >{{ dict.label }}</button>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-strip" v-loading="loading02">
      <div
        v-for="(item, index) in summaryCards" :key="index"
        class="scard" :class="accentClasses[index % accentClasses.length]"
      >
        <div class="scard-label">
          {{ item.energyName }}
          <span v-if="item.energyUnit" class="scard-unit">（{{ item.energyUnit }}）</span>
        </div>
        <div class="scard-value">{{ item.count ?? '—' }}</div>
        <div class="scard-sub">
          <span :class="trendClass(item.tongbi)">同比 {{ fmtTrend(item.tongbi) }}</span>
          <span class="tc-sep"> · </span>
          <span :class="trendClass(item.huanbi)">环比 {{ fmtTrend(item.huanbi) }}</span>
        </div>
      </div>
    </div>

    <!-- 图表区 2×2 -->
    <div class="chart-grid">
      <!-- 能耗趋势 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>能耗趋势</span>
        </div>
        <div ref="chart1Ref" class="chart-box" v-loading="loading1"></div>
      </div>

      <!-- 全厂能耗占比 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>全厂能耗占比</span>
        </div>
        <div ref="chart2Ref" class="chart-box" v-loading="loading02"></div>
      </div>

      <!-- 厂区能耗排名 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>厂区能耗排名 TOP{{ listEnergyConsumptionRankingLength || '' }}</span>
          <div class="period-btns">
            <button
              v-for="dict in period" :key="dict.value"
              class="period-btn sm" :class="{ active: rankPeriod === dict.value }"
              @click="onRankPeriod(dict.value)"
            >{{ dict.label }}</button>
          </div>
        </div>
        <div class="rank-header">
          <span>排名 · 设备</span>
          <span>能耗量(tce)</span>
        </div>
        <div ref="chart3Ref" class="chart-box rank-box" v-loading="loading3"></div>
      </div>

      <!-- 尖峰平谷占比 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>尖峰平谷占比</span>
          <div class="period-btns">
            <button
              v-for="dict in period" :key="dict.value"
              class="period-btn sm" :class="{ active: peakPeriod === dict.value }"
              @click="onPeakPeriod(dict.value)"
            >{{ dict.label }}</button>
          </div>
        </div>
        <div ref="chart4Ref" class="chart-box" v-loading="loading4"></div>
      </div>
    </div>

    <!-- 查看更多弹窗 -->
    <el-dialog v-model="dialogVisible" title="查看全厂能耗统计" width="90%" v-if="dialogVisible">
      <div class="summary-strip dialog-strip">
        <div
          v-for="(item, index) in allCards" :key="index"
          class="scard" :class="accentClasses[index % accentClasses.length]"
        >
          <div class="scard-label">
            {{ item.energyName }}
            <span v-if="item.energyUnit" class="scard-unit">（{{ item.energyUnit }}）</span>
          </div>
          <div class="scard-value">{{ item.count ?? '—' }}</div>
          <div class="scard-sub">
            <span :class="trendClass(item.tongbi)">同比 {{ fmtTrend(item.tongbi) }}</span>
            <span class="tc-sep"> · </span>
            <span :class="trendClass(item.huanbi)">环比 {{ fmtTrend(item.huanbi) }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="Index">
import * as echarts from "echarts"
import {
  listEnergyConsumptionSummation,
  listEnergyConsumptionTrend,
  listEnergyConsumptionRanking,
  listPeakValley,
} from "@/api/home"
import useSettingsStore from "@/store/modules/settings"

const { proxy }     = getCurrentInstance()
const { period }    = proxy.useDict("period")
const settingsStore = useSettingsStore()

// ── state ──────────────────────────────────────────────────────────────
const list                               = ref([[{}, {}, {}, {}, {}]])
const loading02                          = ref(false)
const loading1                           = ref(false)
const loading3                           = ref(false)
const loading4                           = ref(false)
const dialogVisible                      = ref(false)
const listEnergyConsumptionRankingLength = ref(0)
const mainPeriod                         = ref('DAY')
const rankPeriod                         = ref('DAY')
const peakPeriod                         = ref('DAY')

const summaryCards  = computed(() => list.value[0] || [])
const allCards      = computed(() => list.value.flat())
const accentClasses = ['blue', 'orange', 'yellow', 'green', 'purple']

const headerTag = computed(() => {
  const map = { DAY: '日', MONTH: '月', YEAR: '年' }
  return `全厂能耗 · ${map[mainPeriod.value] || ''}视图`
})

// ── chart refs ──────────────────────────────────────────────────────────
const chart1Ref = ref(null)
const chart2Ref = ref(null)
const chart3Ref = ref(null)
const chart4Ref = ref(null)
let myChart1 = null, myChart2 = null, myChart3 = null, myChart4 = null

// ── helpers ─────────────────────────────────────────────────────────────
function trendClass(v) {
  return v > 0 ? 'tc-up' : v < 0 ? 'tc-down' : 'tc-flat'
}
function fmtTrend(v) {
  if (v == null) return '—'
  return `${Math.abs(v).toFixed(1)}${v > 0 ? '↑' : v < 0 ? '↓' : ''}`
}
function buildQuery(timeType) {
  return proxy.addDateRange({
    timeType,
    dataTime: proxy.dayjs(new Date()).format('YYYY-MM-DD'),
    nodeId: null,
    nodeName: null,
  })
}
function chartColors() {
  const isDark = settingsStore.sideTheme === 'theme-dark'
  return {
    text: isDark ? '#6e90b8' : '#475569',
    line: isDark ? 'rgba(0,140,220,.12)' : 'rgba(0,0,0,.06)',
  }
}

// ── period handlers ──────────────────────────────────────────────────────
function onMainPeriod(val) {
  mainPeriod.value = val
  getListEnergyConsumptionSummation(val)
  getListEnergyConsumptionTrend(val)
}
function onRankPeriod(val) {
  rankPeriod.value = val
  getListEnergyConsumptionRanking(val)
}
function onPeakPeriod(val) {
  peakPeriod.value = val
  getListPeakValley(val)
}

// ── theme watch ───────────────────────────────────────────────────────────
watch(() => settingsStore.sideTheme, () => {
  onMainPeriod(mainPeriod.value)
  onRankPeriod(rankPeriod.value)
  onPeakPeriod(peakPeriod.value)
})

// ── 全厂能耗统计 + 占比饼图 ────────────────────────────────────────────────
function getListEnergyConsumptionSummation(timeType) {
  loading02.value = true
  list.value = []
  listEnergyConsumptionSummation(buildQuery(timeType)).then((res) => {
    loading02.value = false
    if (!res.code || res.code !== 200) return
    let total = 0
    const seriesData = []
    if (res.data?.length) {
      res.data.forEach((item, index) => {
        total += Number(item.tonCount)
        item.name  = item.energyName
        item.value = Number(item.tonCount).toFixed(2)
        if (index % 5 === 0) list.value.push(res.data.slice(index, index + 5))
        seriesData.push(item)
      })
    }
    const { text } = chartColors()
    setTimeout(() => {
      myChart2?.setOption({
        grid: { top: '20%', left: '15%', right: '5%', bottom: '0%', containLabel: true },
        tooltip: { trigger: 'item' },
        legend: {
          type: 'scroll', orient: 'vertical', top: 'center', icon: 'circle', right: '5%',
          itemWidth: 14, itemHeight: 14, itemGap: 16,
          textStyle: {
            align: 'left', verticalAlign: 'middle',
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
                return `{name|${name}(${d.energyUnit})  }{value| ${d.value}} {rate| ${pct}%}`
              }
            }
          },
        },
        series: [{
          name: '全厂能耗类型占比', type: 'pie',
          center: ['30%', '50%'], radius: ['50%', '70%'],
          label: { show: false }, labelLine: { show: false },
          data: seriesData,
        }],
      })
      window.addEventListener('resize', () => myChart2?.resize(), { passive: true })
    }, 100)
  })
}

// ── 能耗趋势 ──────────────────────────────────────────────────────────────
function getListEnergyConsumptionTrend(timeType) {
  loading1.value = true
  listEnergyConsumptionTrend(buildQuery(timeType)).then((res) => {
    loading1.value = false
    if (!res.code || res.code !== 200) return
    const xdata = []
    let series  = []
    if (res.data?.xdata) {
      res.data.xdata.forEach((item) => {
        xdata.push(proxy.dayjs(item).format(
          timeType === 'YEAR' ? 'MM月' : timeType === 'MONTH' ? 'DD日' : 'HH时'
        ))
      })
    }
    if (res.data?.legend && res.data?.ydata) {
      series = res.data.legend.map((name, i) => ({
        name, type: 'bar', stack: 'total', barWidth: '16',
        data: res.data.ydata[i] || [],
      }))
    }
    const { text, line } = chartColors()
    setTimeout(() => {
      myChart1?.setOption({
        color: ['#00aaff', '#00e896', '#ffc940', '#ff9240', '#ff4d6a'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { icon: 'rect', itemWidth: 14, itemHeight: 10, textStyle: { color: text } },
        grid: { top: 45, left: '2%', right: '5%', bottom: 10, containLabel: true },
        xAxis: {
          type: 'category', data: xdata,
          axisLine: { lineStyle: { color: text } }, axisTick: { show: false },
          splitLine: { show: false }, axisLabel: { color: text, fontSize: 12, padding: [5, 0, 0, 0] },
        },
        yAxis: {
          type: 'value', name: 'tce',
          nameTextStyle: { color: text, fontSize: 12, padding: [0, 0, 5, 0] },
          axisLine: { show: false }, axisTick: { show: false },
          splitLine: { show: true, lineStyle: { type: 'dashed', color: line } },
          axisLabel: { color: text, fontSize: 12 },
        },
        series,
      })
      window.addEventListener('resize', () => myChart1?.resize(), { passive: true })
    }, 100)
  })
}

// ── 能耗排名 ──────────────────────────────────────────────────────────────
function getListEnergyConsumptionRanking(timeType) {
  loading3.value = true
  listEnergyConsumptionRanking(buildQuery(timeType)).then((res) => {
    loading3.value = false
    const nodeName = [], energyConsumption = []
    if (res.data) {
      res.data.forEach((item) => {
        nodeName.push(item.nodeName)
        energyConsumption.push(item.energyConsumption || 0)
      })
      listEnergyConsumptionRankingLength.value = res.data.length
    }
    const { text } = chartColors()
    setTimeout(() => {
      myChart3?.setOption({
        grid: { left: '1%', right: '2%', bottom: '2%', top: '2%', containLabel: true },
        tooltip: {
          trigger: 'axis', axisPointer: { type: 'none' },
          formatter: (p) => `${p[0].name} : ${p[0].value}`,
        },
        xAxis: { show: false, type: 'value' },
        yAxis: [
          {
            type: 'category', inverse: true,
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
                idx0:  { color: '#FF0004', backgroundColor: '#FF000426', borderRadius: 100, padding: [5, 8] },
                idx1:  { color: '#FF8400', backgroundColor: '#FF84001F', borderRadius: 100, padding: [5, 8] },
                idx2:  { color: '#FFDD00', backgroundColor: '#FFDD001F', borderRadius: 100, padding: [5, 8] },
                idx:   { color: '#3371EB', backgroundColor: '#3371EB26', borderRadius: 100, padding: [5, 8] },
                title: { padding: [5, 8] },
              },
            },
            data: nodeName,
          },
          {
            type: 'category', inverse: true, axisTick: 'none', axisLine: 'none', show: true,
            axisLabel: { color: text, fontSize: 12 },
            data: energyConsumption,
          },
        ],
        series: [
          {
            type: 'bar', showBackground: true,
            backgroundStyle: { color: 'rgba(255,255,255,.04)' },
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#0437FF' }, { offset: 1, color: '#55C6FF' },
              ]),
            },
            barWidth: '10', data: energyConsumption,
          },
          {
            type: 'pictorialBar', symbol: 'rect', symbolSize: [4, 14],
            symbolPosition: 'end', itemStyle: { color: '#488BFF' }, data: energyConsumption,
          },
        ],
      })
      window.addEventListener('resize', () => myChart3?.resize(), { passive: true })
    }, 100)
  })
}

// ── 尖峰平谷 ──────────────────────────────────────────────────────────────
function getListPeakValley(timeType) {
  loading4.value = true
  listPeakValley(buildQuery(timeType)).then((res) => {
    loading4.value = false
    if (!res.code || res.code !== 200) return
    let total = 0
    const seriesData = []
    if (res.data?.length) {
      res.data.forEach((item) => {
        total += Number(item.count)
        seriesData.push({ name: item.timeName, value: Number(item.count).toFixed(2) })
      })
    }
    const { text } = chartColors()
    setTimeout(() => {
      myChart4?.setOption({
        color: ['#00aaff', '#00e896', '#8b5cf6', '#ff9240', '#ffc940', '#33ccff'],
        grid: { top: '20%', left: '15%', right: '5%', bottom: '0%', containLabel: true },
        tooltip: { trigger: 'item' },
        legend: {
          type: 'scroll', orient: 'vertical', top: 'center', icon: 'circle', right: '10%',
          itemWidth: 14, itemHeight: 14, itemGap: 16,
          textStyle: {
            align: 'left', verticalAlign: 'middle',
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
                return `{name|${name}(kWh)  }{value| ${d.value}} {rate| ${pct}%}`
              }
            }
          },
        },
        series: [
          {
            name: '尖峰平谷占比图', type: 'pie',
            center: ['30%', '50%'], radius: ['0%', '50%'],
            avoidLabelOverlap: false,
            label: { fontSize: 11, color: text },
            labelLine: { show: true, length: 50 },
            data: seriesData,
          },
          {
            name: '尖峰平谷占比图', type: 'pie',
            center: ['30%', '50%'], radius: ['60%', '70%'],
            avoidLabelOverlap: false,
            label: { position: 'inner', fontSize: 11, show: false },
            labelLine: { show: false },
            data: seriesData,
          },
        ],
      })
      window.addEventListener('resize', () => myChart4?.resize(), { passive: true })
    }, 100)
  })
}

onMounted(() => {
  myChart1 = echarts.init(chart1Ref.value)
  myChart2 = echarts.init(chart2Ref.value)
  myChart3 = echarts.init(chart3Ref.value)
  myChart4 = echarts.init(chart4Ref.value)
  onMainPeriod('DAY')
  onRankPeriod('DAY')
  onPeakPeriod('DAY')
})
</script>

<style scoped>
/* ─── PAGE HEADER ─── */
.page-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 0 14px; border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 0; position: relative; flex-shrink: 0;
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
.page-title span { color: var(--ems-accent-bright); }
.header-actions { display: flex; align-items: center; gap: 10px; }
.header-tag {
  font-size: 12px; color: var(--ems-text-muted);
  background: rgba(0,140,255,.08); border: 1px solid rgba(0,140,255,.18);
  border-radius: 12px; padding: 3px 12px;
}
.btn-more {
  background: rgba(255,255,255,.04) !important;
  border-color: var(--ems-border-mid) !important;
  color: var(--ems-text-secondary) !important;
  font-size: 12px !important;
}

/* ─── FILTER BAR ─── */
.filter-bar {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 0; border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px;
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
.period-btn.sm { padding: 0 10px; height: 26px; line-height: 26px; font-size: 12px; }

/* ─── SUMMARY STRIP ─── */
.summary-strip {
  display: flex; gap: 12px; padding: 12px 0;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px; flex-wrap: wrap;
}
.dialog-strip { border-bottom: none; }
.scard {
  flex: 1; min-width: 150px;
  background: var(--ems-bg-card); border: 1px solid var(--ems-border-dim);
  border-radius: 8px; padding: 12px 16px;
  position: relative; overflow: hidden;
  display: flex; flex-direction: column; gap: 3px;
}
.scard::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px;
}
.scard.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
.scard.orange::before { background: linear-gradient(90deg, transparent, var(--ems-accent-orange), transparent); }
.scard.yellow::before { background: linear-gradient(90deg, transparent, #ffc940, transparent); }
.scard.green::before  { background: linear-gradient(90deg, transparent, var(--ems-accent-green), transparent); }
.scard.purple::before { background: linear-gradient(90deg, transparent, #8b5cf6, transparent); }
.scard-label { font-size: 11px; color: var(--ems-text-muted); letter-spacing: .5px; }
.scard-unit  { font-size: 10px; color: var(--ems-text-muted); }
.scard-value { font-size: 22px; font-weight: 700; line-height: 1.2; color: var(--ems-text-primary); }
.scard.blue   .scard-value { color: var(--ems-accent-bright); }
.scard.orange .scard-value { color: var(--ems-accent-orange); }
.scard.yellow .scard-value { color: #ffc940; }
.scard.green  .scard-value { color: var(--ems-accent-green); }
.scard.purple .scard-value { color: #8b5cf6; }
.scard-sub { font-size: 11px; color: var(--ems-text-muted); margin-top: 2px; }
.tc-up   { color: var(--ems-accent-green); }
.tc-down { color: var(--ems-accent-red); }
.tc-flat { color: var(--ems-text-muted); }
.tc-sep  { color: var(--ems-border-mid); }

/* ─── CHART GRID ─── */
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px; overflow: hidden;
}
.chart-card-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 16px; font-size: 11.5px; font-weight: 600;
  color: var(--ems-text-secondary); border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.chart-box { width: 100%; height: 280px; }
.rank-box  { height: 254px; }
.rank-header {
  display: flex; justify-content: space-between;
  padding: 7px 16px; font-size: 12px;
  color: var(--ems-text-secondary); border-bottom: 1px solid var(--ems-border-dim);
  background: rgba(0,0,0,.03);
}
</style>
