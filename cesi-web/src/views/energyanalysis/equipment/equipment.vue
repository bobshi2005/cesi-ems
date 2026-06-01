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
          <div class="bar-divider"></div>
          <span class="filter-label">对比方式</span>
          <div class="period-btns">
            <button
              class="period-btn"
              :class="{ active: queryParams.analysisType === 'YOY' }"
              @click="handleAnalysisType('YOY')"
            >同比</button>
            <button
              class="period-btn"
              :class="{ active: queryParams.analysisType === 'MOM' }"
              @click="handleAnalysisType('MOM')"
            >环比</button>
          </div>
          <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
          <el-button type="warning" icon="Download" @click="handleExport" class="action-btn">导出</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area" v-loading="loading">
          <!-- 趋势图 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-设备能耗趋势</span>
              <span class="head-tag">{{ queryParams.analysisType === 'YOY' ? '同比' : '环比' }}</span>
            </div>
            <div class="chart-body">
              <div id="Chart1" style="width: 100%; height: 100%"></div>
            </div>
          </div>

          <!-- 统计分析表 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-设备能耗统计分析表-{{ queryParams.enername }}</span>
            </div>
            <div class="table-box">
              <el-table :data="departmentList" show-summary>
                <el-table-column
                  label="本期时间"
                  align="center"
                  key="currentTime"
                  prop="currentTime"
                  :show-overflow-tooltip="true"
                />
                <el-table-column
                  :label="'本期耗' + queryParams.enername + '(' + queryParams.muid + ')'"
                  align="center"
                  key="currentValue"
                  prop="currentValue"
                  :show-overflow-tooltip="true"
                />
                <el-table-column
                  label="同期时间"
                  align="center"
                  key="compareTime"
                  prop="compareTime"
                  :show-overflow-tooltip="true"
                />
                <el-table-column
                  :label="'同期耗' + queryParams.enername + '(' + queryParams.muid + ')'"
                  align="center"
                  key="compareValue"
                  prop="compareValue"
                  :show-overflow-tooltip="true"
                />
                <el-table-column
                  :label="(queryParams.analysisType == 'YOY' ? '同' : '环') + '比(%)'"
                  align="center"
                  key="ratio"
                  prop="ratio"
                  :show-overflow-tooltip="true"
                  width="200"
                />
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="equipment">
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import * as echarts from "echarts"
import request from "@/utils/request"
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
    analysisType: "YOY",
    energyType: null,
  },
  query: {
    modelCode: null,
  },
})
const { queryParams, query } = toRefs(data)

function handleNodeClick(data) {
  queryParams.value.nodeId = data.id
  queryParams.value.nodeName = data.label
  handleTimeType(period.value[0].value || "DAY")
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
function handleAnalysisType(analysisType) {
  queryParams.value.analysisType = analysisType
  getList()
}
function getList() {
  loading.value = true
  const chartDom = document.getElementById("Chart1")
  if (chartDom && echarts.getInstanceByDom(chartDom)) {
    echarts.dispose(chartDom)
  }

  request({
    url: "/consumptionanalysis/getByArea",
    method: "get",
    params: proxy.addDateRange({
      ...queryParams.value,
      ...query.value,
    }),
  }).then((res) => {
    if (!!res.code && res.code == 200) {
      loading.value = false
      const xdata = [], yvalue = [], ycompareValue = [], yqoq = []
      if (res.data.chartDataList) {
        res.data.chartDataList.forEach((item) => {
          xdata.push(
            proxy.dayjs(item.xdata).format(
              queryParams.value.timeType == "YEAR" ? "MM月"
              : queryParams.value.timeType == "MONTH" ? "DD日" : "HH时"
            )
          )
          yvalue.push(item.yvalue || 0)
          ycompareValue.push(item.ycompareValue || 0)
          yqoq.push(item.yqoq || 0)
        })
      }
      const isDark = settingsStore.sideTheme === "theme-dark"
      const textColor = isDark ? "#6e90b8" : "#475569"
      const lineColor = isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)"

      setTimeout(() => {
        const dom = document.getElementById("Chart1")
        if (!dom) return
        const myChart1 = echarts.init(dom)
        myChart1.setOption({
          color: ["#00aaff", "#00e896", "#ffc940", "#ff4d6a"],
          grid: { top: 55, left: "5%", right: "5%", bottom: 10, containLabel: true },
          tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
          legend: {
            icon: "rect", itemWidth: 14, itemHeight: 10,
            textStyle: { color: textColor },
          },
          xAxis: {
            type: "category",
            axisLine: { show: true, lineStyle: { color: textColor } },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { color: textColor, fontSize: 12, padding: [5, 0, 0, 0] },
            data: xdata,
          },
          yAxis: [
            {
              type: "value",
              name: "耗" + queryParams.value.enername + "量(" + queryParams.value.muid + ")",
              nameTextStyle: { color: textColor, fontSize: 12, padding: [0, 0, 5, 0] },
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { show: true, lineStyle: { type: "dashed", color: lineColor } },
              axisLabel: { color: textColor, fontSize: 12 },
            },
            {
              type: "value",
              name: queryParams.value.analysisType == "YOY" ? "同比(%)" : "环比(%)",
              alignTicks: true,
              nameTextStyle: { color: textColor, fontSize: 12, padding: [0, 0, 5, 0] },
              axisLine: { show: false },
              axisTick: { show: false },
              splitLine: { show: false },
              axisLabel: { color: textColor, fontSize: 12 },
            },
          ],
          series: [
            {
              name: "本期值", type: "bar", barWidth: "10",
              itemStyle: { borderRadius: [4, 4, 0, 0] },
              tooltip: { valueFormatter: (v) => v + queryParams.value.muid },
              markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
              data: yvalue,
            },
            {
              name: "同期值", type: "bar", barWidth: "10",
              itemStyle: { borderRadius: [4, 4, 0, 0] },
              tooltip: { valueFormatter: (v) => v + queryParams.value.muid },
              markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
              data: ycompareValue,
            },
            {
              name: queryParams.value.analysisType == "YOY" ? "同比" : "环比",
              type: "line", yAxisIndex: 1, symbol: "none",
              tooltip: { valueFormatter: (v) => v + "%" },
              data: yqoq,
            },
          ],
        })
        window.addEventListener("resize", () => myChart1.resize(), { passive: true })
      }, 100)

      departmentList.value = res.data.dataList || []
    }
  })
}
function handleQuery() {
  getList()
}
function handleExport() {
  proxy.download(
    "consumptionanalysis/energyExport",
    { ...queryParams.value, ...query.value },
    `${queryParams.value.nodeName}-设备能耗统计分析表-${queryParams.value.enername}_${new Date().getTime()}.xlsx`
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
.head-tag {
  font-size: 11px;
  font-weight: 400;
  color: var(--ems-text-muted);
  background: rgba(0, 140, 255, 0.08);
  border: 1px solid rgba(0, 140, 255, 0.18);
  border-radius: 10px;
  padding: 2px 10px;
}
.chart-body {
  width: 100%;
  height: 320px;
}
.table-box {
  padding: 0;
}
</style>
