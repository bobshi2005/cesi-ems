<template>
  <div class="app-container cost-trend-page">
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
    </div>

    <!-- 内容区 -->
    <div class="content-area" v-loading="loading">
      <!-- 成本汇总表 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>能源成本统计</span>
        </div>
        <div class="table-box">
          <el-table :data="tableData" :border="false">
            <el-table-column label="时间"       prop="dateCode" align="center" width="160" />
            <el-table-column label="总费用(元)"  prop="total"    align="center" width="120" />
            <el-table-column
              :label="col.energyName"
              v-for="(col, index) in columns"
              :key="index"
              align="center"
            >
              <el-table-column :prop="'energyTotal' + col.energyType" label="消耗量"    min-width="120" align="center" />
              <el-table-column :prop="'costTotal'   + col.energyType" label="费用（元）" min-width="120" align="center" />
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 各能源类型：费用趋势 + 用量趋势 -->
      <template v-for="item in chartDataList" :key="item.energyType">
        <div class="chart-row-2">
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ item.costLabel }}（元）</span>
            </div>
            <div class="chart-item-wrap">
              <LineChart
                :domId="'costDom_' + item.energyType"
                :chartData="{
                  title: item.costLabel,
                  chartType: 'line',
                  xAxis: item.costKeyList,
                  series: [{
                    name: item.costLabel,
                    data: item.costValueList,
                    markPoint: { data: [{ type: 'max', name: 'Max' }, { type: 'min', name: 'Min' }] },
                  }],
                }"
              />
            </div>
          </div>
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ item.accumulationLabel }}（{{ item.energyUnit }}）</span>
            </div>
            <div class="chart-item-wrap">
              <LineChart
                :domId="'accumulationDom_' + item.energyType"
                :chartType="'bar'"
                :chartData="{
                  title: item.accumulationLabel,
                  chartType: 'bar',
                  barWidth: '10',
                  xAxis: item.accumulationKeyList,
                  series: [{
                    name: item.accumulationLabel,
                    color: '#00e896',
                    data: item.accumulationValueList,
                    markPoint: { data: [{ type: 'max', name: 'Max' }, { type: 'min', name: 'Min' }] },
                  }],
                }"
              />
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup name="costTrendAnalysis">
import { listEnergyCostTrend, listEnergyCostTrendDetail } from "@/api/cost/api.js"
import LineChart from "@/components/Echarts/LineChart.vue"
import { deepClone } from "@/utils/index.js"
const { proxy } = getCurrentInstance()
import { useRoute } from "vue-router"
const { period } = proxy.useDict("period")
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(() => settingsStore.sideTheme, () => { getList() })

const loading    = ref(false)
const tableData  = ref([])
const columns    = ref([])
const chartDataList = ref([])

const data = reactive({
  queryParams: {
    nodeId:    null,
    nodeName:  null,
    timeType:  null,
    dataTime:  null,
    meterId:   "",
    modelCode: useRoute().query.modelCode,
  },
  query: { ...useRoute().query },
})
const { queryParams, query } = toRefs(data)

function buildTimeCode() {
  const fmt = queryParams.value.timeType == "YEAR"  ? "YYYY"
            : queryParams.value.timeType == "MONTH" ? "YYYY-MM"
            : "YYYY-MM-DD"
  return proxy.dayjs(new Date(queryParams.value.dataTime)).format(fmt)
}

function handleTimeType(e) {
  queryParams.value.timeType  = e
  queryParams.value.dataTime  = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  getTableData()
  getList()
}

function getTableData() {
  listEnergyCostTrend({
    ...queryParams.value,
    timeCode: buildTimeCode(),
  }).then((res) => {
    if (res.code !== 200) return
    const rows = deepClone(res.data.itemList || [])
    if (rows.length > 0 && rows[0].itemList?.length > 0) {
      columns.value = rows[0].itemList.map((item) => ({
        energyName: item.energyName,
        energyType: item.energyType,
      }))
    }
    tableData.value = rows.map((item) => {
      const extras = {}
      item.itemList.forEach((el) => {
        extras["energyTotal" + el.energyType] = el.accumulation
        extras["costTotal"   + el.energyType] = el.cost
      })
      return { ...item, ...extras }
    })
  })
}

function getList() {
  loading.value = true
  listEnergyCostTrendDetail({
    ...queryParams.value,
    timeCode: buildTimeCode(),
  })
    .then((res) => {
      loading.value = false
      if (res.code === 200) chartDataList.value = res.data
    })
    .catch(() => { loading.value = false })
}

function handleQuery() {
  getTableData()
  getList()
}

onMounted(() => {
  setTimeout(() => {
    handleTimeType(period.value[0]?.value || "DAY")
  }, 200)
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
  height: calc(100vh - 160px);
  max-height: calc(100vh - 160px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 12px;
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
.table-box { padding: 0; }

/* ─── 双图行 ─── */
.chart-row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  flex-shrink: 0;
}

/* LineChart 组件自带 height:360px，用 auto 容器顺应其高度 */
.chart-item-wrap {
  width: 100%;
}
</style>
