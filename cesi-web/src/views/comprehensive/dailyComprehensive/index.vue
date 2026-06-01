<template>
  <div class="page">
    <div class="dc-container">
      <!-- 筛选栏 -->
      <div class="dc-filter-bar">
        <div class="dc-filter-item">
          <span class="dc-filter-label">能源类型</span>
          <el-select v-model="queryParams.energyType" placeholder="请选择能源类型" size="small" style="width: 160px">
            <el-option :label="item.enername" :value="item.enersno" v-for="item in energyTypeList" :key="item.enersno" />
          </el-select>
        </div>
        <div class="dc-filter-item">
          <span class="dc-filter-label">统计时间</span>
          <el-date-picker
            v-model="queryParams.dataTime"
            type="date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            size="small"
            style="width: 160px"
            :clearable="false"
          />
        </div>
        <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </div>

      <!-- 内容区 -->
      <div class="dc-content" v-loading="loading">
        <div class="dc-card">
          <div class="dc-table-wrap">
            <el-table :data="energyList" stripe max-height="380">
              <el-table-column fixed prop="indexName" label="指标名称" width="210">
                <template #default="scope">
                  <div class="dc-index-name">
                    <el-button
                      icon="Search"
                      circle
                      size="small"
                      @click="selectChange(scope.row)"
                      :type="scope.row.indexId == queryParams.indexId ? 'primary' : ''"
                    />
                    <span>{{ scope.row.indexName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-for="index in 24" :key="index" :label="index - 1 + '时'" align="center" min-width="100">
                <template #default="scope">{{ numFilter(scope.row[`value${index - 1}`]) }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="dc-chart-wrap">
            <line-chart ref="LineChartRef" :chartData="lineChartData" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getDataList, getlistChart, exportList } from "@/api/comprehensiveStatistics/dailyComprehensive/dailyComprehensive"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import LineChart from "../comps/LineChart.vue"

const { proxy } = getCurrentInstance()

const energyTypeList  = ref([])
const loading         = ref(false)
const energyList      = ref([])
const lineChartData   = ref({})
const LineChartRef    = ref()

const queryParams = ref({
  indexStorageId: "",
  indexCode:      "",
  pageNum:        1,
  pageSize:       10,
  dataTime:       "",
  energyType:     "",
})

function getEnergyTypeList() {
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data || []
    getList()
  })
}
getEnergyTypeList()

function getTime() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, "0")
  const day = String(d.getDate()).padStart(2, "0")
  queryParams.value.dataTime = `${y}-${m}-${day}`
}
getTime()

function numFilter(value) {
  if (!isNaN(value) && value !== "" && value !== null) {
    return parseFloat(value).toFixed(2)
  }
  return "--"
}

function getList() {
  queryParams.value.indexCode = proxy.$route.query.modelCode
  getDataList({ ...queryParams.value, timeType: "HOUR" }).then((response) => {
    energyList.value = response.data || []
    if (response.data && response.data.length !== 0) {
      selectChange(response.data[0])
    } else {
      lineChartData.value = {}
    }
  })
}

function selectChange(row) {
  queryParams.value.indexId   = row ? row.indexId : undefined
  queryParams.value.timeType  = "HOUR"
  getlistChart(queryParams.value).then((response) => {
    const title = response.data.length > 0 ? response.data[0].indexName + "(" + (response.data[0].unitId || "") + ")" : ""
    lineChartData.value = {
      xData: (response.data || []).map((item) => item.timeCode.slice(-2) + "时"),
      yData: (response.data || []).map((item) => numFilter(item.value)),
      title,
    }
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { ...queryParams.value, pageNum: 1, pageSize: 10, dataTime: null }
  getTime()
  getList()
}
</script>

<style scoped>
/* ── 容器 ──────────────────────────────────────────────────────────────── */
.dc-container {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 148px);
}

/* ── 筛选栏 ────────────────────────────────────────────────────────────── */
.dc-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.dc-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.dc-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.dc-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 卡片 ──────────────────────────────────────────────────────────────── */
.dc-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.dc-table-wrap {
  padding: 8px;
}
.dc-index-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── 图表区 ────────────────────────────────────────────────────────────── */
.dc-chart-wrap {
  border-top: 1px solid var(--ems-border-dim);
}
</style>
