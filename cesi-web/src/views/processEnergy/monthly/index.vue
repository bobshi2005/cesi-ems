<template>
  <div class="app-container monthly-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <span class="filter-label">能源类型</span>
      <el-select
        v-model="queryParams.energyType"
        placeholder="请选择能源类型"
        style="width: 150px"
      >
        <el-option
          :label="item.enername"
          :value="item.enersno"
          v-for="item in energyTypeList"
          :key="item.enersno"
        />
      </el-select>
      <span class="filter-label ml">统计时间</span>
      <el-date-picker
        v-model="queryParams.dataTime"
        type="month"
        value-format="YYYY-MM"
        placeholder="选择月份"
        style="width: 150px"
        :clearable="false"
      />
      <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
      <el-button icon="Refresh" @click="resetQuery" class="action-btn">重置</el-button>
    </div>

    <!-- 内容区 -->
    <div class="content-area">
      <!-- 数据表格 -->
      <div class="chart-card" v-loading="loading">
        <div class="chart-card-head">
          <span>工序能耗月数据</span>
          <span class="head-hint">点击行首按钮可查看该指标趋势图</span>
        </div>
        <div class="table-box">
          <el-table :data="energyList" border max-height="360px">
            <el-table-column fixed prop="indexName" label="指标名称" width="210px">
              <template #default="scope">
                <div style="display:flex;align-items:center">
                  <el-button
                    icon="search"
                    circle
                    size="small"
                    @click="selectChange(scope.row)"
                    :type="scope.row.indexId == queryParams.indexId ? 'primary' : 'default'"
                    style="margin-right:8px;flex-shrink:0"
                  />
                  <el-tooltip
                    v-if="scope.row.indexName && scope.row.indexName.length > 9"
                    effect="dark"
                    :content="scope.row.indexName"
                    placement="top-end"
                  >
                    <span>{{ scope.row.indexName.substr(0, 9) + "…" }}</span>
                  </el-tooltip>
                  <span v-else>{{ scope.row.indexName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column
              v-for="index in 31"
              :key="index"
              :label="index + '日'"
              align="center"
              min-width="90"
            >
              <template #default="scope">{{ numFilter(scope.row[`value${index}`]) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 趋势图 -->
      <div class="chart-card">
        <div class="chart-card-head">
          <span>{{ lineChartData.title || '请在上方表格中选择一条指标' }}</span>
        </div>
        <div class="chart-body">
          <line-chart ref="LineChartRef" :chartData="lineChartData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import processApi from "@/api/process/api"
import LineChart from "../comps/LineChart.vue"

const { proxy } = getCurrentInstance()

const energyTypeList = ref([])
const energyList     = ref([])
const lineChartData  = ref({})
const loading        = ref(false)
const LineChartRef   = ref()

const queryParams = ref({
  indexCode:  "",
  indexId:    undefined,
  pageNum:    1,
  pageSize:   10,
  dataTime:   "",
  timeType:   "DAY",
  energyType: undefined,
})

function getTime() {
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM")
}

function numFilter(value) {
  if (!isNaN(value) && value !== "" && value !== null) {
    return parseFloat(value).toFixed(2)
  }
  return "--"
}

function apiParams() {
  return {
    ...queryParams.value,
    dataTime: queryParams.value.dataTime ? queryParams.value.dataTime + "-01" : "",
  }
}

function getList() {
  queryParams.value.indexCode = proxy.$route.query.modelCode
  loading.value = true
  processApi
    .monthlyList(apiParams())
    .then((res) => {
      loading.value = false
      energyList.value = res.data || []
      if (res.data?.length) {
        selectChange(res.data[0])
      } else {
        lineChartData.value = {}
      }
    })
    .catch(() => { loading.value = false })
}

function selectChange(row) {
  queryParams.value.indexId = row?.indexId
  processApi.monthlyChart(apiParams()).then((res) => {
    const xData = [], yData = []
    let title = ""
    res.data.forEach((item) => {
      yData.push(numFilter(item.value))
      xData.push(item.timeCode.slice(-2) + "日")
      title = item.indexName + (item.unitId ? `(${item.unitId})` : "")
    })
    lineChartData.value = { xData, yData, title }
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value.dataTime   = ""
  queryParams.value.energyType = undefined
  queryParams.value.indexId    = undefined
  queryParams.value.pageNum    = 1
  lineChartData.value = {}
  getTime()
  getList()
}

listEnergyTypeList().then((res) => {
  energyTypeList.value = res.data || []
  getTime()
  getList()
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
.head-hint {
  font-size: 11px;
  font-weight: 400;
  color: var(--ems-text-muted);
}
.table-box  { padding: 0; }
.chart-body { width: 100%; height: 320px; }
</style>
