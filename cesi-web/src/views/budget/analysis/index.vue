<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">预算 <span>执行分析</span></div>
      <div class="header-tag">{{ headerTag }}</div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <span class="filter-label">分析维度</span>
      <el-select v-model="queryParams.dimension" style="width:120px" @change="onDimensionChange">
        <el-option label="用能预算" value="energy" />
        <el-option label="碳排放预算" value="carbon" />
      </el-select>

      <div class="filter-divider"></div>

      <span class="filter-label">时间周期</span>
      <el-select v-model="queryParams.period" style="width:80px" @change="onPeriodChange">
        <el-option label="年" value="year" />
        <el-option label="月" value="month" />
        <el-option label="自定义" value="custom" />
      </el-select>
      <el-input-number v-if="queryParams.period !== 'custom'"
        v-model="queryParams.year" :min="2020" :max="2099" :controls="false"
        style="width:80px;margin-left:6px" />
      <el-select v-if="queryParams.period === 'month'" v-model="queryParams.month"
        style="width:72px;margin-left:4px">
        <el-option v-for="m in 12" :key="m" :label="m+'月'" :value="m" />
      </el-select>
      <template v-if="queryParams.period === 'custom'">
        <el-date-picker v-model="queryParams.startDate" type="date" value-format="YYYY-MM-DD"
          style="width:126px;margin-left:6px" placeholder="开始日期" />
        <span style="margin:0 4px;color:var(--ems-text-muted);font-size:12px">至</span>
        <el-date-picker v-model="queryParams.endDate" type="date" value-format="YYYY-MM-DD"
          style="width:126px" placeholder="结束日期" />
      </template>

      <div class="filter-divider"></div>

      <span class="filter-label">{{ scopeLabel }}</span>
      <el-select v-model="queryParams.scope" style="width:120px" clearable>
        <el-option v-for="o in scopeOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>

      <el-button type="primary" class="btn-search" :loading="loading" @click="handleQuery">
        <svg class="btn-icon" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="7" cy="7" r="5"/><line x1="11" y1="11" x2="14" y2="14"/>
        </svg>
        查询
      </el-button>

      <div style="margin-left:auto">
        <el-button class="btn-export" @click="handleExport">
          <svg class="btn-icon" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12L8 7L13 12M8 7V2"/><rect x="2" y="13" width="12" height="1.5" rx=".75"/>
          </svg>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-strip" v-if="analysisData">
      <div class="scard blue">
        <div class="scard-label">{{ isEnergy ? '预算总量' : '预算总排放' }}</div>
        <div class="scard-value">{{ formatNum(analysisData.budgetTotal) }}</div>
        <div class="scard-unit">{{ analysisData.unit }}</div>
        <div class="scard-sub">{{ isEnergy ? '全年用能预算合计' : '全年碳排放预算合计' }}</div>
      </div>
      <div class="scard green">
        <div class="scard-label">{{ isEnergy ? '实际用能' : '实际排放量' }}</div>
        <div class="scard-value">
          {{ analysisData.actualTotal != null ? formatNum(analysisData.actualTotal) : '待核算' }}
        </div>
        <div class="scard-unit">{{ analysisData.unit }}</div>
        <div class="scard-sub">已核算累计{{ isEnergy ? '消耗' : '排放' }}</div>
      </div>
      <div class="scard orange">
        <div class="scard-label">执行进度</div>
        <div class="scard-value">
          {{ analysisData.executionRate != null ? analysisData.executionRate + '%' : '—' }}
        </div>
        <div class="scard-unit"></div>
        <div class="scard-sub">{{ analysisData.rateYoy ? '同比去年 ' + analysisData.rateYoy : '' }}</div>
      </div>
      <div class="scard" :class="analysisData.overCount > 0 ? 'red' : 'green'">
        <div class="scard-label">{{ isEnergy ? '超标单元' : '超标边界' }}</div>
        <div class="scard-value">{{ analysisData.overCount }}</div>
        <div class="scard-unit">个</div>
        <div class="scard-sub">{{ analysisData.overHint || '' }}</div>
      </div>
    </div>

    <template v-if="analysisData && analysisData.units && analysisData.units.length">
      <!-- 进度对比图 -->
      <div class="chart-card" style="margin-bottom:14px">
        <div class="chart-card-head">
          <span>{{ isEnergy ? '各预算单元执行情况' : '各预算边界执行情况' }}（{{ analysisData.unit }}）</span>
          <span class="chart-head-sub">{{ periodLabel }}</span>
        </div>
        <!-- 表头行 -->
        <div class="progress-header">
          <div class="progress-name" style="font-size:10px;color:var(--ems-text-muted)">
            {{ isEnergy ? '预算单元' : '预算边界' }}
          </div>
          <div style="flex:1;font-size:10px;color:var(--ems-text-muted)">执行进度</div>
          <div class="progress-labels">
            <span style="font-size:10px;color:var(--ems-text-muted)">实际</span>
            <span class="progress-budget-head">预算</span>
            <span class="progress-pct-head">进度</span>
          </div>
        </div>
        <!-- 数据行 -->
        <div v-for="u in analysisData.units" :key="u.name" class="progress-row">
          <div class="progress-name" :class="u.over ? 'over' : ''">{{ u.name }}</div>
          <div class="progress-bar-wrap">
            <div class="progress-bar"
              :style="{
                width: u.rate != null ? Math.min(u.rate, 100) + '%' : '0%',
                background: u.over
                  ? 'linear-gradient(90deg,rgba(255,77,106,.6),rgba(255,77,106,.9))'
                  : u.rate >= 80
                    ? 'linear-gradient(90deg,rgba(255,146,64,.5),rgba(255,146,64,.8))'
                    : 'linear-gradient(90deg,rgba(0,140,255,.5),rgba(0,200,255,.8))'
              }"
            ></div>
          </div>
          <div class="progress-labels">
            <span class="progress-actual" :class="u.over ? 'over' : ''">
              {{ u.actual != null ? formatNum(u.actual) : '待核算' }}
            </span>
            <span class="progress-budget">/ {{ formatNum(u.budget) }} {{ analysisData.unit }}</span>
            <span class="progress-pct"
              :class="u.over ? 'pct-over' : u.rate >= 80 ? 'pct-warn' : 'pct-ok'">
              {{ u.rate != null ? u.rate + '%' : u.statusText }}
            </span>
          </div>
        </div>
      </div>

      <!-- 趋势图 -->
      <div class="chart-card" style="margin-bottom:14px">
        <div class="chart-card-head">
          <span>{{ isEnergy ? '月度用能趋势' : '月度排放趋势' }} — 预算 vs 实际</span>
          <div class="chart-legend">
            <span class="legend-item budget">预算</span>
            <span class="legend-item actual">实际</span>
            <span class="legend-item over">超标</span>
          </div>
        </div>
        <div ref="trendChartRef" class="trend-chart"></div>
      </div>

      <!-- 明细表 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>查询结果明细</span>
          <span class="chart-head-sub">共 {{ analysisData.units.length }} 条</span>
        </div>
        <div style="overflow-x:auto">
          <el-table :data="analysisData.units" stripe size="small">
            <el-table-column :label="isEnergy ? '预算范围' : '预算边界'" prop="name" min-width="140" />
            <el-table-column :label="`预算量（${analysisData.unit}）`" min-width="130" align="right">
              <template #default="{ row }">{{ formatNum(row.budget) }}</template>
            </el-table-column>
            <el-table-column :label="`实际量（${analysisData.unit}）`" min-width="130" align="right">
              <template #default="{ row }">
                <span v-if="row.actual != null" class="td-value" :class="row.over ? 'over' : ''">
                  {{ formatNum(row.actual) }}
                </span>
                <span v-else class="td-empty">待核算</span>
              </template>
            </el-table-column>
            <el-table-column label="执行进度" width="100" align="center">
              <template #default="{ row }">
                <span v-if="row.rate != null"
                  :class="row.over ? 'pct-over' : row.rate >= 80 ? 'pct-warn' : 'pct-ok'">
                  {{ row.rate }}%
                </span>
                <span v-else class="td-empty">—</span>
              </template>
            </el-table-column>
            <el-table-column label="执行状态" width="100" align="center">
              <template #default="{ row }">
                <span class="status-dot"
                  :class="row.over ? 'status-over' : row.rate >= 80 ? 'status-warning' : 'status-active'">
                  {{ row.statusText }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </template>

    <div v-if="!loading && !analysisData" class="empty-state">
      <div class="empty-icon">📊</div>
      <div class="empty-text">请选择分析条件后点击查询</div>
    </div>
  </div>
</template>

<script setup name="BudgetAnalysis">
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getBudgetAnalysis } from '@/api/budget/budgetAnalysis'
import useSettingsStore from '@/store/modules/settings'

const route         = useRoute()
const settingsStore = useSettingsStore()
const loading       = ref(false)
const analysisData  = ref(null)
const trendChartRef = ref(null)
let   trendChart    = null

const queryParams = reactive({
  dimension: 'energy',
  period: 'year',
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1,
  startDate: null,
  endDate: null,
  scope: ''
})

const isEnergy   = computed(() => queryParams.dimension === 'energy')
const scopeLabel = computed(() => isEnergy.value ? '核算范围' : '预算边界')

const headerTag = computed(() => {
  const dim    = isEnergy.value ? '用能预算' : '碳排放预算'
  let period = ''
  if (queryParams.period === 'year')   period = `${queryParams.year}年`
  else if (queryParams.period === 'month') period = `${queryParams.year}年${queryParams.month}月`
  else period = `${queryParams.startDate || ''}至${queryParams.endDate || ''}`
  const scope = queryParams.scope || '全部范围'
  const pLabel = queryParams.period === 'year' ? '年视图' : queryParams.period === 'month' ? '月视图' : '自定义'
  return `${dim} · ${pLabel} · ${period} · ${scope}`
})

const periodLabel = computed(() => {
  if (queryParams.period === 'year')  return `${queryParams.year}年`
  if (queryParams.period === 'month') return `${queryParams.year}年${queryParams.month}月`
  return `${queryParams.startDate} 至 ${queryParams.endDate}`
})

const scopeOptions = computed(() => isEnergy.value
  ? [
      { label: '全部',  value: '' },
      { label: '全厂',  value: '全厂' },
      { label: '一车间', value: '一车间' },
      { label: '一工段', value: '一工段' },
      { label: '1号间',  value: '1号间' },
    ]
  : [
      { label: '全部',  value: '' },
      { label: '全厂区', value: '全厂区' },
      { label: '一车间', value: '一车间' },
      { label: '一工段', value: '一工段' },
    ]
)

function onDimensionChange() { queryParams.scope = '' }
function onPeriodChange()    { queryParams.startDate = null; queryParams.endDate = null }

function formatNum(v) {
  return v != null
    ? Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
    : '—'
}

function handleExport() {
  ElMessage.info('导出功能开发中')
}

function renderTrendChart(data) {
  if (!trendChartRef.value || !data?.trend) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const isDark = settingsStore.sideTheme === 'theme-dark'
  const textColor  = isDark ? '#6e90b8' : '#475569'
  const lineColor  = isDark ? 'rgba(0,140,220,.12)' : 'rgba(0,0,0,.06)'
  trendChart.setOption({
    color: ['rgba(0,140,255,.5)', '#33ccff', '#ff4d6a'],
    tooltip: { trigger: 'axis', backgroundColor: isDark ? '#0c1833' : '#fff',
      borderColor: isDark ? 'rgba(0,170,255,.2)' : 'rgba(0,0,0,.08)',
      textStyle: { color: isDark ? '#d8e8f8' : '#1e293b' } },
    legend: {
      show: false  // 用自定义图例
    },
    grid: { top: 20, left: '3%', right: '4%', bottom: 24, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.trend.months || [],
      axisLine:  { lineStyle: { color: lineColor } },
      axisTick:  { show: false },
      axisLabel: { color: textColor, fontSize: 11 },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: textColor, fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: lineColor } },
      axisLine:  { show: false },
      axisTick:  { show: false },
    },
    series: [
      {
        name: '预算',
        type: 'line',
        data: data.trend.budget || [],
        lineStyle: { color: 'rgba(0,140,255,.5)', width: 2, type: 'dashed' },
        itemStyle: { color: 'rgba(0,140,255,.5)' },
        symbol: 'none',
      },
      {
        name: '实际',
        type: 'line',
        data: data.trend.actual || [],
        lineStyle: { color: '#33ccff', width: 2 },
        itemStyle: { color: '#33ccff' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1,
          [{ offset: 0, color: 'rgba(0,200,255,.15)' }, { offset: 1, color: 'rgba(0,200,255,.01)' }]) },
        symbol: 'circle',
        symbolSize: 4,
      },
    ],
  })
  window.addEventListener('resize', () => trendChart?.resize(), { passive: true })
}

async function handleQuery() {
  loading.value = true
  try {
    const res = await getBudgetAnalysis({
      dimension: queryParams.dimension,
      period:    queryParams.period,
      year:      queryParams.period !== 'custom' ? queryParams.year    : undefined,
      month:     queryParams.period === 'month'  ? queryParams.month   : undefined,
      startDate: queryParams.period === 'custom' ? queryParams.startDate : undefined,
      endDate:   queryParams.period === 'custom' ? queryParams.endDate   : undefined,
      scope:     queryParams.scope || undefined,
    })
    analysisData.value = res.data
    await nextTick()
    renderTrendChart(res.data)
  } catch {
    ElMessage.error('查询失败，请重试')
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.dimension,
  (dim) => {
    if (dim && dim !== queryParams.dimension) {
      queryParams.dimension = dim
      queryParams.scope = ''
    }
  },
  { immediate: true }
)

onMounted(handleQuery)
</script>

<style scoped>
/* ─── PAGE HEADER ─── */
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 14px;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 0;
  position: relative;
  flex-shrink: 0;
}
.page-header::after {
  content: '';
  position: absolute;
  left: 0; bottom: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, var(--ems-accent), transparent);
  opacity: .35;
}
.page-title {
  font-size: 17px; font-weight: 600;
  color: var(--ems-text-primary); letter-spacing: .8px; flex: 1;
}
.page-title span { color: var(--ems-accent-bright); }
.header-tag {
  font-size: 12px; color: var(--ems-text-muted);
  background: rgba(0,140,255,.08); border: 1px solid rgba(0,140,255,.18);
  border-radius: 12px; padding: 3px 12px;
}

/* ─── FILTER BAR ─── */
.filter-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 0; border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px; flex-wrap: wrap;
}
.filter-label { font-size: 12px; color: var(--ems-text-secondary); white-space: nowrap; }
.filter-divider { width: 1px; height: 20px; background: var(--ems-border-dim); margin: 0 2px; }
.btn-search {
  background: linear-gradient(135deg, #0055bb, #0099ee) !important;
  border-color: transparent !important;
  color: #fff !important;
}
.btn-search:hover { background: linear-gradient(135deg, #0066cc, #00aaff) !important; }
.btn-icon { width: 14px; height: 14px; margin-right: 4px; vertical-align: middle; }
.btn-export {
  background: rgba(255,255,255,.04) !important;
  border-color: var(--ems-border-mid) !important;
  color: var(--ems-text-secondary) !important;
}
.btn-export:hover { background: rgba(255,255,255,.08) !important; color: var(--ems-text-primary) !important; }

/* ─── SUMMARY STRIP ─── */
.summary-strip {
  display: flex; gap: 12px; padding: 12px 0;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px; flex-wrap: wrap;
}
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
.scard.blue::before  { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
.scard.green::before { background: linear-gradient(90deg, transparent, var(--ems-accent-green), transparent); }
.scard.orange::before{ background: linear-gradient(90deg, transparent, var(--ems-accent-orange), transparent); }
.scard.red::before   { background: linear-gradient(90deg, transparent, var(--ems-accent-red), transparent); }
.scard-label { font-size: 11px; color: var(--ems-text-muted); letter-spacing: .5px; }
.scard-value { font-size: 22px; font-weight: 700; line-height: 1.1; color: var(--ems-text-primary); }
.scard.blue   .scard-value { color: var(--ems-accent-bright); }
.scard.green  .scard-value { color: var(--ems-accent-green); }
.scard.orange .scard-value { color: var(--ems-accent-orange); }
.scard.red    .scard-value { color: var(--ems-accent-red); }
.scard-unit { font-size: 11px; color: var(--ems-text-secondary); }
.scard-sub  { font-size: 11px; color: var(--ems-text-muted); margin-top: 1px; }

/* ─── CHART CARD ─── */
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
.chart-head-sub { font-size: 11px; color: var(--ems-text-muted); font-weight: 400; }
.chart-legend { display: flex; gap: 14px; font-size: 11px; color: var(--ems-text-muted); font-weight: 400; }
.legend-item { display: flex; align-items: center; gap: 4px; }
.legend-item::before { content: ''; display: inline-block; width: 10px; height: 3px; border-radius: 1px; }
.legend-item.budget::before { background: rgba(0,140,255,.5); }
.legend-item.actual::before { background: rgba(0,200,255,.85); }
.legend-item.over::before   { background: rgba(255,77,106,.8); }

/* ─── TREND CHART ─── */
.trend-chart { width: 100%; height: 220px; }

/* ─── PROGRESS ROWS ─── */
.progress-header {
  display: flex; align-items: center; gap: 12px;
  padding: 6px 14px; background: rgba(0,0,0,.06);
}
.progress-row {
  display: flex; align-items: center; gap: 12px;
  padding: 9px 14px; border-bottom: 1px solid var(--ems-border-dim);
}
.progress-row:last-child { border-bottom: none; }
.progress-name {
  width: 120px; font-size: 12.5px; color: var(--ems-text-primary);
  flex-shrink: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.progress-name.over { color: var(--ems-accent-red); }
.progress-bar-wrap {
  flex: 1; height: 8px; background: rgba(255,255,255,.04); border-radius: 4px; overflow: hidden;
}
.progress-bar { height: 100%; border-radius: 4px; transition: width .8s ease; }
.progress-labels {
  display: flex; align-items: center; gap: 8px;
  min-width: 190px; justify-content: flex-end; flex-shrink: 0;
}
.progress-budget-head { font-size: 10px; color: var(--ems-text-muted); min-width: 100px; text-align: right; }
.progress-pct-head    { font-size: 10px; color: var(--ems-text-muted); width: 44px; text-align: right; }
.progress-actual { font-size: 13px; font-weight: 600; color: var(--ems-accent-bright); }
.progress-actual.over { color: var(--ems-accent-red); }
.progress-budget { font-size: 11px; color: var(--ems-text-muted); min-width: 100px; text-align: right; white-space: nowrap; }
.progress-pct    { font-size: 11.5px; width: 44px; text-align: right; }
.pct-ok   { color: var(--ems-accent-green); }
.pct-warn { color: var(--ems-accent-yellow); }
.pct-over { color: var(--ems-accent-red); }

/* ─── TABLE ─── */
.td-value { font-size: 13px; font-weight: 600; color: var(--ems-accent-bright); }
.td-value.over { color: var(--ems-accent-red); }
.td-empty { color: var(--ems-text-muted); }

/* ─── STATUS DOT ─── */
.status-dot {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11.5px; color: var(--ems-text-primary);
}
.status-dot::before { content: ''; width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.status-active::before  { background: var(--ems-accent-green);  box-shadow: 0 0 5px var(--ems-accent-green); }
.status-warning::before { background: var(--ems-accent-yellow); box-shadow: 0 0 5px var(--ems-accent-yellow); }
.status-over::before    { background: var(--ems-accent-red);    box-shadow: 0 0 5px var(--ems-accent-red); }

/* ─── EMPTY STATE ─── */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 60px 20px;
  color: var(--ems-text-muted); gap: 12px;
}
.empty-icon { font-size: 40px; opacity: .2; }
.empty-text { font-size: 13px; }
</style>
