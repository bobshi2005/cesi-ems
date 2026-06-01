<template>
  <div class="page">
    <div class="yc-container">
      <!-- 筛选栏 -->
      <div class="yc-filter-bar">
        <div class="yc-filter-item">
          <span class="yc-filter-label">能源类型</span>
          <el-select v-model="queryParams.energyType" placeholder="请选择能源类型" size="small" style="width: 160px">
            <el-option :label="item.enername" :value="item.enersno" v-for="item in energyTypeList" :key="item.enersno" />
          </el-select>
        </div>
        <div class="yc-filter-item">
          <span class="yc-filter-label">统计时间</span>
          <el-date-picker
            v-model="queryParams.dataTime"
            type="year"
            :clearable="false"
            value-format="YYYY"
            placeholder="选择年份"
            size="small"
            style="width: 160px"
          />
        </div>
        <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      </div>

      <!-- 内容区 -->
      <div class="yc-content" v-loading="loading">
        <div class="yc-card">
          <div class="yc-table-wrap">
            <el-table :data="energyList" stripe max-height="380">
              <el-table-column fixed prop="indexName" label="指标名称" width="210">
                <template #default="scope">
                  <div class="yc-index-name">
                    <el-button
                      icon="Search" circle size="small"
                      @click="selectChange(scope.row)"
                      :type="scope.row.indexId == queryParams.indexId ? 'primary' : ''"
                    />
                    <span>{{ scope.row.indexName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-for="index in 12" :key="index" :label="index + '月'" align="center" min-width="100">
                <template #default="scope">{{ numFilter(scope.row[`value${index}`]) }}</template>
              </el-table-column>
            </el-table>
          </div>

          <div class="yc-chart-wrap">
            <line-chart ref="LineChartRef" :chartData="lineChartData" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { getDataList, getlistChart } from "@/api/comprehensiveStatistics/yearComprehensive/yearComprehensive"
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
  timeType:       "MONTH",
})

function getEnergyTypeList() {
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data || []
    getList()
  })
}
getEnergyTypeList()

function getTime() {
  queryParams.value.dataTime = String(new Date().getFullYear())
}
getTime()

function numFilter(value) {
  if (!isNaN(value) && value !== "" && value !== null) return parseFloat(value).toFixed(2)
  return "--"
}

function getList() {
  queryParams.value.indexCode = proxy.$route.query.modelCode
  getDataList({ ...queryParams.value, dataTime: queryParams.value.dataTime ? queryParams.value.dataTime + "-01" : "" }).then((response) => {
    energyList.value = response.data || []
    if (energyList.value.length) {
      selectChange(energyList.value[0])
    } else {
      lineChartData.value = {}
    }
  })
}

function selectChange(row) {
  queryParams.value.indexId = row ? row.indexId : undefined
  getlistChart({ ...queryParams.value, dataTime: queryParams.value.dataTime ? queryParams.value.dataTime + "-01" : "" }).then((response) => {
    const title = response.data.length > 0 ? response.data[0].indexName + "(" + (response.data[0].unitId || "") + ")" : ""
    lineChartData.value = {
      xData: (response.data || []).map((item) => item.timeCode.slice(-2) + "月"),
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
  queryParams.value = { ...queryParams.value, pageNum: 1, pageSize: 10, dataTime: null, timeType: "MONTH" }
  getTime()
  getList()
}
</script>

<style scoped>
.yc-container { display: flex; flex-direction: column; min-height: calc(100vh - 148px); }
.yc-filter-bar { display: flex; align-items: center; gap: 12px; padding: 10px 16px; border-bottom: 1px solid var(--ems-border-dim); flex-shrink: 0; }
.yc-filter-label { font-size: 12px; color: var(--ems-text-secondary); white-space: nowrap; }
.yc-filter-item { display: flex; align-items: center; gap: 8px; }
.yc-content { flex: 1; padding: 14px 16px; overflow-y: auto; }
.yc-card { background: var(--ems-bg-card); border: 1px solid var(--ems-border-dim); border-radius: 8px; overflow: hidden; }
.yc-table-wrap { padding: 8px; }
.yc-index-name { display: flex; align-items: center; gap: 8px; }
.yc-chart-wrap { border-top: 1px solid var(--ems-border-dim); }
</style>
