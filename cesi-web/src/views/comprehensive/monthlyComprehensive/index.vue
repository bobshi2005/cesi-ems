<template>
  <div class="page">
    <div class="mc-container">
      <!-- 筛选栏 -->
      <div class="mc-filter-bar">
        <div class="mc-filter-item">
          <span class="mc-filter-label">能源类型</span>
          <el-select v-model="queryParams.energyType" placeholder="请选择能源类型" size="small" style="width: 160px">
            <el-option :label="item.enername" :value="item.enersno" v-for="item in energyTypeList" :key="item.enersno" />
          </el-select>
        </div>
        <div class="mc-filter-item">
          <span class="mc-filter-label">统计时间</span>
          <el-date-picker
            v-model="queryParams.dataTime"
            type="month"
            :clearable="false"
            value-format="YYYY-MM"
            placeholder="选择月份"
            size="small"
            style="width: 160px"
          />
        </div>
        <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </div>

      <!-- 内容区 -->
      <div class="mc-content" v-loading="loading">
        <div class="mc-card">
          <div class="mc-table-wrap">
            <el-table :data="energyList" stripe max-height="380">
              <el-table-column fixed prop="indexName" label="指标名称" width="210">
                <template #default="scope">
                  <div class="mc-index-name">
                    <el-button
                      icon="Search" circle size="small"
                      @click="selectChange(scope.row)"
                      :type="scope.row.indexId == queryParams.indexId ? 'primary' : ''"
                    />
                    <span>{{ scope.row.indexName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-for="index in 31" :key="index" :label="index + '日'" align="center" min-width="100">
                <template #default="scope">{{ numFilter(scope.row[`value${index}`]) }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="mc-chart-wrap">
            <line-chart ref="LineChartRef" :chartData="lineChartData" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getDataList, getlistChart } from "@/api/comprehensiveStatistics/monthlyComprehensive/monthlyComprehensive"
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
  timeType:       "DAY",
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
  queryParams.value.dataTime = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`
}
getTime()

function numFilter(value) {
  if (!isNaN(value) && value !== "" && value !== null) return parseFloat(value).toFixed(2)
  return "--"
}

function getList() {
  queryParams.value.indexCode = proxy.$route.query.modelCode
  getDataList({ ...queryParams.value }).then((response) => {
    energyList.value = response.data?.tabledata || []
    if (energyList.value.length) {
      selectChange(energyList.value[0])
    } else {
      lineChartData.value = {}
    }
  })
}

function selectChange(row) {
  queryParams.value.indexId = row ? row.indexId : undefined
  getlistChart(queryParams.value).then((response) => {
    const title = response.data.length > 0 ? response.data[0].indexName + "(" + (response.data[0].unitId || "") + ")" : ""
    lineChartData.value = {
      xData: (response.data || []).map((item) => item.timeCode.slice(-2) + "日"),
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
  queryParams.value = { ...queryParams.value, pageNum: 1, pageSize: 10, dataTime: null, timeType: "DAY" }
  getTime()
  getList()
}
</script>

<style scoped>
.mc-container { display: flex; flex-direction: column; min-height: calc(100vh - 148px); }
.mc-filter-bar { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-bottom: 1px solid var(--ems-border-dim); flex-shrink: 0; }
.mc-filter-label { font-size: 12px; color: var(--ems-text-secondary); white-space: nowrap; }
.mc-filter-item { display: flex; align-items: center; gap: 8px; }
.mc-content { flex: 1; padding: 14px 16px; overflow-y: auto; }
.mc-card { background: var(--ems-bg-card); border: 1px solid var(--ems-border-dim); border-radius: 8px; overflow: hidden; }
.mc-table-wrap { padding: 8px; }
.mc-index-name { display: flex; align-items: center; gap: 8px; }
.mc-chart-wrap { border-top: 1px solid var(--ems-border-dim); }
</style>
