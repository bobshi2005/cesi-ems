<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="tf-filter-bar">
          <div class="tf-filter-item">
            <span class="tf-filter-label">期间</span>
            <el-select v-model="queryParams.timeType" size="small" style="width: 110px" @change="handleTimeType">
              <el-option v-for="dict in period" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
          <div class="tf-filter-item">
            <span class="tf-filter-label">时间</span>
            <el-date-picker
              v-model="queryParams.dataTime"
              :type="queryParams.timeType == 'YEAR' ? 'year' : queryParams.timeType == 'MONTH' ? 'month' : 'date'"
              :format="queryParams.timeType == 'YEAR' ? 'YYYY' : queryParams.timeType == 'MONTH' ? 'YYYY-MM' : 'YYYY-MM-DD'"
              value-format="YYYY-MM-DD"
              placeholder="时间"
              size="small"
              style="width: 150px"
            />
          </div>
          <div class="tf-filter-item">
            <span class="tf-filter-label">选择电表</span>
            <el-select v-model="queryParams.meterId" placeholder="选择电表" clearable size="small" style="width: 200px">
              <el-option v-for="dict in electricityMeter" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>
          <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>

          <div class="tf-display-btns">
            <button class="tf-display-btn" :class="{ active: activeKey === 1 }" @click="switchBtnType(1)">图形</button>
            <button class="tf-display-btn" :class="{ active: activeKey === 2 }" @click="switchBtnType(2)">数据</button>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="tf-content" v-loading="loading">

          <!-- 空状态 -->
          <div v-if="showEmptyHint" class="tf-empty">
            <el-icon><InfoFilled /></el-icon>
            该节点没有监测数据，请选择其下的子节点
          </div>

          <template v-if="!showEmptyHint">

            <!-- Tab 切换 -->
            <div class="tf-tabs">
              <button class="tf-tab-btn" :class="{ active: activeTabKey === '1' }" @click="switchTab('1')">电压不平衡</button>
              <button class="tf-tab-btn" :class="{ active: activeTabKey === '2' }" @click="switchTab('2')">电流不平衡</button>
            </div>

            <!-- 图表卡片 -->
            <div v-show="activeKey === 1" class="tf-chart-card">
              <div class="tf-chart-head">{{ queryParams.nodeName }} · 三相不平衡分析</div>
              <div class="tf-chart-body">
                <LineChart ref="LineChartRef" :chartData="lineChartData" :grid="chartGrid" />
              </div>
              <!-- 极值表格 -->
              <div class="tf-table-wrap" style="margin: 0 12px 12px">
                <el-table :data="tableData1" stripe>
                  <el-table-column label="类型" prop="type" align="center" />
                  <el-table-column label="三相不平衡极值" prop="value" align="center" />
                  <el-table-column label="发生时间" prop="time" align="center" />
                  <el-table-column v-if="activeTabKey == 1" label="A项电压(V)" prop="valueA" align="center" />
                  <el-table-column v-if="activeTabKey == 1" label="B项电压(V)" prop="valueB" align="center" />
                  <el-table-column v-if="activeTabKey == 1" label="C项电压(V)" prop="valueC" align="center" />
                  <el-table-column v-if="activeTabKey == 2" label="A项电流(A)" prop="valueA" align="center" />
                  <el-table-column v-if="activeTabKey == 2" label="B项电流(A)" prop="valueB" align="center" />
                  <el-table-column v-if="activeTabKey == 2" label="C项电流(A)" prop="valueC" align="center" />
                </el-table>
              </div>
            </div>

            <!-- 数据表格 -->
            <div v-show="activeKey === 2" class="tf-table-wrap">
              <el-table :data="tableData2" v-loading="loading" height="calc(100vh - 300px)" stripe>
                <el-table-column label="时间" prop="timeCode" align="center" />
                <el-table-column label="电表名称" prop="name" align="center" />
                <el-table-column v-if="activeTabKey == 1 && queryParams.timeType == 'DAY'" label="A项电压(V)" prop="valueA" align="center" />
                <el-table-column v-if="activeTabKey == 1 && queryParams.timeType == 'DAY'" label="B项电压(V)" prop="valueB" align="center" />
                <el-table-column v-if="activeTabKey == 1 && queryParams.timeType == 'DAY'" label="C项电压(V)" prop="valueC" align="center" />
                <el-table-column v-if="activeTabKey == 2 && queryParams.timeType == 'DAY'" label="A项电流(A)" prop="valueA" align="center" />
                <el-table-column v-if="activeTabKey == 2 && queryParams.timeType == 'DAY'" label="B项电流(A)" prop="valueB" align="center" />
                <el-table-column v-if="activeTabKey == 2 && queryParams.timeType == 'DAY'" label="C项电流(A)" prop="valueC" align="center" />
                <el-table-column v-if="activeTabKey == 1 && queryParams.timeType !== 'DAY'" label="最大值(V)" prop="max" align="center" />
                <el-table-column v-if="activeTabKey == 1 && queryParams.timeType !== 'DAY'" label="最小值(V)" prop="min" align="center" />
                <el-table-column v-if="activeTabKey == 2 && queryParams.timeType !== 'DAY'" label="最大值(A)" prop="max" align="center" />
                <el-table-column v-if="activeTabKey == 2 && queryParams.timeType !== 'DAY'" label="最小值(A)" prop="min" align="center" />
              </el-table>
            </div>

          </template>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="loadAnalysis">
import { threePhaseUnbalanceAnalysisDetail } from "@/api/powerquality/electricThreePhase/api.js"
import { listElectricityDeviceMeter } from "@/api/powerquality/load-analysis/api.js"
import LineChart from "@/components/Echarts/LineChart.vue"
import { InfoFilled } from "@element-plus/icons-vue"

const { proxy }     = getCurrentInstance()
const route         = useRoute()
const { period }    = proxy.useDict("period")

const activeTabKey      = ref("1")
const activeKey         = ref(1)
const loading           = ref(false)
const showEmptyHint     = ref(false)
const tableData1        = ref([])
const tableData2        = ref([])
const lineChartData     = ref({})
const electricityMeter  = ref([])
const LineChartRef      = ref()
const chartGrid         = ref({
  top: "25",
  left: "6",
  right: "6",
  bottom: "28",
  containLabel: true,
})

const data = reactive({
  queryParams: {
    nodeId:   null,
    nodeName: null,
    timeType: null,
    dataTime: null,
    meterId:  "",
  },
  query: { ...route.query },
})
const { queryParams, query } = toRefs(data)

function getElectricityMeter(params) {
  listElectricityDeviceMeter(params).then((res) => {
    if (res.code === 200) {
      electricityMeter.value = (res.data || []).map((item) => ({ ...item, value: item.code }))
      queryParams.value.meterId = res.data.length > 0 ? res.data[0].code : ""
      getList()
    }
  })
}

function handleNodeClick(data) {
  queryParams.value.nodeId   = data.id
  queryParams.value.nodeName = data.label
  showEmptyHint.value        = data.children?.length > 0
  if (showEmptyHint.value) return
  setTimeout(() => handleTimeType(period.value[0].value), 200)
}

function handleTimeType(e) {
  queryParams.value.timeType = e
  queryParams.value.dataTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  getElectricityMeter({ nodeId: queryParams.value.nodeId })
}

function switchTab(e) {
  activeTabKey.value = e
  getList()
}

function switchBtnType(e) {
  activeKey.value = e
  if (e === 1) getList()
}

function getList() {
  if (!queryParams.value.meterId) return

  loading.value = true
  let params = {
    nodeId:      queryParams.value.nodeId,
    timeType:    queryParams.value.timeType,
    timeCode:    queryParams.value.dataTime,
    meterId:     queryParams.value.meterId,
    requestType: activeTabKey.value == 1 ? 0 : 1,
  }
  if (queryParams.value.timeType == "DAY")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM-DD")
  else if (queryParams.value.timeType == "MONTH")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY-MM")
  else if (queryParams.value.timeType == "YEAR")
    params.timeCode = proxy.dayjs(new Date(queryParams.value.dataTime)).format("YYYY")

  threePhaseUnbalanceAnalysisDetail(params)
    .then((res) => {
      loading.value = false
      if (res.code == 200) {
        const itemList = res.data.itemList || []
        const detail   = res.data.detail || {}

        // 极值表
        tableData1.value = detail ? [
          { type: "最大值", value: detail.max || "--", time: detail.maxTime, valueA: detail.valueMaxA, valueB: detail.valueMaxB, valueC: detail.valueMaxC },
          { type: "最小值", value: detail.min || "--", time: detail.minTime, valueA: detail.valueMinA, valueB: detail.valueMinB, valueC: detail.valueMinC },
        ] : []

        // 数据表
        tableData2.value = itemList || []

        // 图表
        const isMonth = queryParams.value.timeType !== "DAY"
        const labelAB = activeTabKey.value == 1 ? "电压" : "电流"
        if (isMonth) {
          lineChartData.value = {
            title: "三相不平衡分析",
            xAxis: itemList.map((item) => item.timeCodeChart),
            series: [
              { name: "最小值", data: itemList.map((item) => item.min === "--" || !item.min ? null : Number(item.min)) },
              { name: "最大值", data: itemList.map((item) => item.max === "--" || !item.max ? null : Number(item.max)) },
            ],
            yAxisLabelFontSize: 10,
          }
        } else {
          lineChartData.value = {
            title: "三相不平衡分析",
            xAxis: itemList.map((item) => (item.timeCode?.slice(-2) ?? "") + "时"),
            series: [
              { name: "A相" + labelAB, data: itemList.map((item) => item.valueA === "--" || !item.valueA ? null : Number(item.valueA)) },
              { name: "B相" + labelAB, data: itemList.map((item) => item.valueB === "--" || !item.valueB ? null : Number(item.valueB)) },
              { name: "C相" + labelAB, data: itemList.map((item) => item.valueC === "--" || !item.valueC ? null : Number(item.valueC)) },
            ],
            yAxisLabelFontSize: 10,
          }
        }
      }
    })
    .catch(() => { loading.value = false })
}

function handleQuery() { getList() }
</script>

<style scoped>
/* ── 左右 flex 布局 ────────────────────────────────────────────────────── */
.page-container {
  display: flex;
  min-height: calc(100vh - 148px);
}
.page-container-left {
  flex-shrink: 0;
  width: 220px;
  min-height: calc(100vh - 148px);
  border-right: 1px solid var(--ems-border-dim);
  overflow-y: auto;
}
.page-container-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 筛选栏 ────────────────────────────────────────────────────────────── */
.tf-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.tf-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.tf-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── 显示切换按钮 ──────────────────────────────────────────────────────── */
.tf-display-btns {
  display: flex;
  margin-left: auto;
}
.tf-display-btn {
  padding: 0 14px;
  height: 30px;
  line-height: 30px;
  background: rgba(0,170,255,.08);
  border: 1px solid var(--ems-border-dim);
  color: var(--ems-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all .2s;
  white-space: nowrap;
}
.tf-display-btn:first-child { border-radius: 4px 0 0 4px; }
.tf-display-btn:last-child  { border-radius: 0 4px 4px 0; }
.tf-display-btn + .tf-display-btn { border-left: none; }
.tf-display-btn:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
.tf-display-btn.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }

/* ── Tab 切换按钮 ────────────────────────────────────────────────────────── */
.tf-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 14px;
}
.tf-tab-btn {
  padding: 0 18px;
  height: 30px;
  line-height: 30px;
  background: rgba(0,170,255,.08);
  border: 1px solid var(--ems-border-dim);
  color: var(--ems-text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all .2s;
  white-space: nowrap;
}
.tf-tab-btn:first-child { border-radius: 4px 0 0 4px; }
.tf-tab-btn:last-child  { border-radius: 0 4px 4px 0; }
.tf-tab-btn + .tf-tab-btn { border-left: none; }
.tf-tab-btn:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
.tf-tab-btn.active { background: var(--ems-accent); border-color: var(--ems-accent); color: #fff; }

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.tf-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.tf-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.tf-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 图表卡片 ──────────────────────────────────────────────────────────── */
.tf-chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.tf-chart-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.tf-chart-body {
  width: 100%;
  height: 340px;
  padding: 8px 0;
}

/* ── 表格区 ────────────────────────────────────────────────────────────── */
.tf-table-wrap {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  padding: 8px;
}
</style>
