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
          <span class="filter-label ml">能源类型</span>
          <el-select v-model="queryParams.energyType" placeholder="能源类型" style="width: 120px">
            <el-option
              :label="item.enername"
              :value="item.enersno"
              v-for="item in energyTypeList"
              :key="item.enersno"
              @click="handleEnergyType(item)"
            />
          </el-select>
          <el-button type="primary" icon="Search" @click="handleQuery" class="search-btn">搜索</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area" v-loading="loading">
          <!-- 折线图 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-支路用能分析</span>
            </div>
            <div class="chart-body">
              <line-chart ref="LineChartRef" :chartData="lineChartData" :chartType="'bar'" />
            </div>
          </div>

          <!-- 明细表格 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-支路用能详情-{{ queryParams.enername }}</span>
            </div>
            <div class="table-box">
              <el-table :data="departmentList">
                <el-table-column label="节点" align="center" key="nodeName" prop="nodeName" fixed="left" />
                <el-table-column label="合计" align="center" key="total" prop="total" width="100" fixed="left" />
                <template v-if="queryParams.timeType == 'DAY'">
                  <el-table-column
                    v-for="index in 24"
                    :key="index"
                    :label="index - 1 + '时'"
                    align="center"
                    min-width="130"
                  >
                    <template #default="scope">{{ scope.row[`value${index - 1}`] }}</template>
                  </el-table-column>
                </template>
                <template v-if="queryParams.timeType == 'MONTH'">
                  <el-table-column
                    v-for="index in 31"
                    :key="index"
                    :label="index + '日'"
                    align="center"
                    min-width="130"
                  >
                    <template #default="scope">{{ scope.row[`value${index - 1}`] }}</template>
                  </el-table-column>
                </template>
                <template v-if="queryParams.timeType == 'YEAR'">
                  <el-table-column
                    v-for="index in 12"
                    :key="index"
                    :label="index + '月'"
                    align="center"
                    min-width="130"
                  >
                    <template #default="scope">{{ scope.row[`value${index - 1}`] }}</template>
                  </el-table-column>
                </template>
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="branchanalysis">
import buildApi from "@/api/buildingConsumption/api"
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import LineChart from "@/components/Echarts/LineChart.vue"
const { proxy } = getCurrentInstance()
const { period } = proxy.useDict("period")
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(
  () => settingsStore.sideTheme,
  () => {
    getList()
  }
)
const energyTypeList = ref(undefined)
const departmentList = ref([])
const loading = ref(false)
const data = reactive({
  queryParams: {
    nodeId: null,
    timeType: null,
    dataTime: null,
    energyType: null,
  },
  query: {
    modelCode: null,
  },
})
const { queryParams, query } = toRefs(data)
const LineChartRef = ref()
const lineChartData = ref({})

function handleNodeClick(data) {
  queryParams.value.nodeId = data.id
  queryParams.value.nodeName = data.label
  handleTimeType(period.value[0].value)
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data
    queryParams.value.energyType = energyTypeList.value[0].enersno
    queryParams.value.enername = energyTypeList.value[0].enername
    queryParams.value.muid = energyTypeList.value[0].muid
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
  queryParams.value.muid = item.muid
  handleQuery()
}
function getList() {
  loading.value = true
  buildApi
    .branchanalysis(
      proxy.addDateRange({
        ...queryParams.value,
        ...query.value,
      })
    )
    .then((res) => {
      if (!!res.code && res.code == 200) {
        loading.value = false
        let yData = []
        let xData = []
        if (!!res.data) {
          departmentList.value = [res.data] || []
        }
        let dataList = res.data || {}
        if (queryParams.value.timeType == "DAY") {
          for (let i = 0; i < 24; i++) {
            xData.push(i + "时")
            yData.push(dataList[`value${i}`])
          }
        } else if (queryParams.value.timeType == "MONTH") {
          for (let i = 0; i < 31; i++) {
            xData.push(i + 1 + "日")
            yData.push(dataList[`value${i}`])
          }
        } else {
          for (let i = 0; i < 12; i++) {
            xData.push(i + 1 + "月")
            yData.push(dataList[`value${i}`])
          }
        }

        lineChartData.value = {
          title: queryParams.value.muid,
          xAxis: xData,
          series: [
            {
              name: queryParams.value.enername,
              data: yData,
            },
          ],
        }
      }
    })
    .catch(() => {
      loading.value = false
    })
}
function handleQuery() {
  getList()
}
function handleExport() {
  proxy.download(
    "consumptionanalysis/energyExport",
    {
      ...queryParams.value,
      ...query.value,
    },
    `${queryParams.value.nodeName}-厂区能耗分析_${new Date().getTime()}.xlsx`
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
.search-btn { height: 30px !important; padding: 0 14px !important; margin-left: 4px; }

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
.chart-body {
  width: 100%;
}
.table-box {
  padding: 0;
}
</style>
