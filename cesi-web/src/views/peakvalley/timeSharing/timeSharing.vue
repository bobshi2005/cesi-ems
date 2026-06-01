<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" />
      </div>
      <div class="page-container-right">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <span class="filter-label">日期</span>
          <el-date-picker
            v-model="queryParams.queryTime"
            value-format="YYYY-MM-DD"
            type="date"
            placeholder="选择日期"
            style="width: 150px"
          />
          <el-button type="primary" icon="Search" @click="handleQuery" class="action-btn">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery" class="action-btn">重置</el-button>
          <el-button type="warning" icon="Download" @click="handleExport" class="action-btn">导出</el-button>
        </div>

        <!-- 内容区 -->
        <div class="content-area" v-loading="loading">
          <!-- 上方双图 -->
          <div class="chart-row-2">
            <!-- 峰平谷电量图 -->
            <div class="chart-card">
              <div class="chart-card-head">
                <span>{{ queryParams.nodeName }}-峰平谷电量图</span>
              </div>
              <div class="chart-body">
                <div id="Chart1" style="width:100%;height:100%"></div>
              </div>
            </div>

            <!-- 峰平谷占比图 -->
            <div class="chart-card">
              <div class="chart-card-head">
                <span>{{ queryParams.nodeName }}-峰平谷占比图</span>
              </div>
              <div class="chart-body">
                <div id="Chart2" style="width:100%;height:100%"></div>
              </div>
            </div>
          </div>

          <!-- 分时统计表 -->
          <div class="chart-card">
            <div class="chart-card-head">
              <span>{{ queryParams.nodeName }}-峰平谷分时统计</span>
            </div>
            <div class="table-box">
              <el-table :data="timeSharingList" style="width:100%" show-summary>
                <el-table-column label="时间" align="center" show-overflow-tooltip>
                  <template #default="scope">
                    {{ proxy.dayjs(scope.row.time).format("HH时") }}
                  </template>
                </el-table-column>
                <el-table-column label="尖" align="center">
                  <el-table-column prop="sharpPower"  label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
                  <el-table-column prop="sharpFee"    label="电费(元)"    align="center" show-overflow-tooltip />
                </el-table-column>
                <el-table-column label="峰" align="center">
                  <el-table-column prop="peakPower"   label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
                  <el-table-column prop="peakFee"     label="电费(元)"    align="center" show-overflow-tooltip />
                </el-table-column>
                <el-table-column label="平" align="center">
                  <el-table-column prop="flatPower"   label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
                  <el-table-column prop="flatFee"     label="电费(元)"    align="center" show-overflow-tooltip />
                </el-table-column>
                <el-table-column label="谷" align="center">
                  <el-table-column prop="valleyPower" label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
                  <el-table-column prop="valleyFee"   label="电费(元)"    align="center" show-overflow-tooltip />
                </el-table-column>
                <el-table-column label="合计" align="center">
                  <el-table-column prop="totalPower"  label="总电量(kWh)" align="center" show-overflow-tooltip width="120" />
                  <el-table-column prop="totalFee"    label="总电费(元)"  align="center" show-overflow-tooltip />
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog v-model="timeSharingDialogTableVisible" title="查看分时统计详情" width="1300">
      <div class="table-box">
        <el-table :data="viewTimeSharingList" style="width:100%" height="420" border>
          <el-table-column label="时间" align="center" show-overflow-tooltip width="200">
            <template #default="scope">{{ scope.row.time }}时</template>
          </el-table-column>
          <el-table-column label="尖" align="center">
            <el-table-column prop="sharpPower"  label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
            <el-table-column prop="sharpFee"    label="电费(元)"    align="center" show-overflow-tooltip width="90" />
          </el-table-column>
          <el-table-column label="峰" align="center">
            <el-table-column prop="peakPower"   label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
            <el-table-column prop="peakFee"     label="电费(元)"    align="center" show-overflow-tooltip width="90" />
          </el-table-column>
          <el-table-column label="平" align="center">
            <el-table-column prop="flatPower"   label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
            <el-table-column prop="flatFee"     label="电费(元)"    align="center" show-overflow-tooltip width="90" />
          </el-table-column>
          <el-table-column label="谷" align="center">
            <el-table-column prop="valleyPower" label="耗电量(kWh)" align="center" show-overflow-tooltip width="120" />
            <el-table-column prop="valleyFee"   label="电费(元)"    align="center" show-overflow-tooltip width="90" />
          </el-table-column>
          <el-table-column label="合计" align="center">
            <el-table-column label="总电量(kWh)" align="center" show-overflow-tooltip width="120">
              <template #default="scope">
                {{ scope.row.sharpPower + scope.row.peakPower + scope.row.flatPower + scope.row.valleyPower }}
              </template>
            </el-table-column>
            <el-table-column label="总电费(元)" align="center" show-overflow-tooltip width="100">
              <template #default="scope">
                {{ scope.row.sharpFee + scope.row.peakFee + scope.row.flatFee + scope.row.valleyFee }}
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column prop="nodeName" label="用能单元" align="center" show-overflow-tooltip width="200" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="timeSharing">
import { listTimeSharing } from "@/api/peakValley/timeSharing"
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
import { useRoute } from "vue-router"
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()
watch(() => settingsStore.sideTheme, () => { getList() })

const loading = ref(false)
const timeSharingList = ref([])
const timeSharingDialogTableVisible = ref(false)
const viewTimeSharingList = ref([])

const data = reactive({
  queryParams: {
    nodeId:    null,
    nodeName:  null,
    timeType:  "DAY",
    queryTime: null,
  },
  query: { ...useRoute().query },
})
const { queryParams, query } = toRefs(data)

function handleNodeClick(data) {
  queryParams.value.nodeId    = data.id
  queryParams.value.nodeName  = data.label
  queryParams.value.queryTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  handleQuery()
}

function chartColors() {
  const isDark = settingsStore.sideTheme === "theme-dark"
  return {
    text: isDark ? "#6e90b8" : "#475569",
    line: isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)",
  }
}

function disposeChart(id) {
  const dom = document.getElementById(id)
  if (dom && echarts.getInstanceByDom(dom)) echarts.dispose(dom)
}

function getList() {
  loading.value = true
  listTimeSharing(
    proxy.addDateRange({ ...queryParams.value, ...query.value })
  ).then((res) => {
    loading.value = false
    if (!res.code || res.code !== 200) return

    disposeChart("Chart1")
    disposeChart("Chart2")
    const { text, line } = chartColors()

    // ── 电量堆叠柱图 ──────────────────────────────────────────────────────
    if (res.data.lineChat) {
      const xdata = [], ytip = [], ypeak = [], yflat = [], ytrough = []
      res.data.lineChat.forEach((item) => {
        xdata.push(Number(item.xdata.slice(-8).substring(0, 2)) + "时")
        ytip.push(item.ytip     || 0)
        ypeak.push(item.ypeak   || 0)
        yflat.push(item.yflat   || 0)
        ytrough.push(item.ytrough || 0)
      })
      setTimeout(() => {
        const dom1 = document.getElementById("Chart1")
        if (!dom1) return
        echarts.init(dom1).setOption({
          color: ["#f52528", "#ff6200", "#ffce0c", "#78e801"],
          grid: { top: 55, left: "5%", right: "5%", bottom: 10, containLabel: true },
          tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
          legend: { icon: "rect", itemWidth: 14, itemHeight: 10, textStyle: { color: text } },
          xAxis: {
            type: "category",
            axisLine: { show: true, lineStyle: { color: text } },
            axisTick: { show: false }, splitLine: { show: false },
            axisLabel: { color: text, fontSize: 12, padding: [5, 0, 0, 0] },
            data: xdata,
          },
          yAxis: {
            name: "耗电量(KWH)", type: "value",
            nameTextStyle: { color: text, fontSize: 12, padding: [0, 0, 5, 0] },
            axisLine: { show: false }, axisTick: { show: false },
            splitLine: { show: true, lineStyle: { type: "dashed", color: line } },
            axisLabel: { color: text, fontSize: 12 },
          },
          series: [
            { name: "尖", type: "bar", stack: "total", barWidth: "12", data: ytip },
            { name: "峰", type: "bar", stack: "total", barWidth: "12", data: ypeak },
            { name: "平", type: "bar", stack: "total", barWidth: "12", data: yflat },
            { name: "谷", type: "bar", stack: "total", barWidth: "12", data: ytrough },
          ],
        })
        window.addEventListener("resize", () => echarts.getInstanceByDom(dom1)?.resize(), { passive: true })
      }, 100)
    }

    // ── 占比饼图 ──────────────────────────────────────────────────────────
    if (res.data.pieChat) {
      const pc = res.data.pieChat
      setTimeout(() => {
        const dom2 = document.getElementById("Chart2")
        if (!dom2) return
        echarts.init(dom2).setOption({
          color: ["#f52528", "#ff6200", "#ffce0c", "#78e801"],
          tooltip: { trigger: "item" },
          legend: {
            icon: "circle", itemWidth: 14, itemHeight: 14, itemGap: 10,
            textStyle: { color: text },
          },
          series: [{
            name: "峰平谷占比图", type: "pie",
            radius: ["30%", "55%"],
            avoidLabelOverlap: false,
            label: { overflow: "none", formatter: "{b} {c}%", color: text, fontSize: 12 },
            data: [
              { value: pc.tip    ? Number(pc.tip)    : 0, name: "尖" },
              { value: pc.peak   ? Number(pc.peak)   : 0, name: "峰" },
              { value: pc.flat   ? Number(pc.flat)   : 0, name: "平" },
              { value: pc.trough ? Number(pc.trough) : 0, name: "谷" },
            ],
          }],
        })
        window.addEventListener("resize", () => echarts.getInstanceByDom(dom2)?.resize(), { passive: true })
      }, 100)
    }

    timeSharingList.value = res.data.dataList || []
  })
}

function handleView(row) {
  viewTimeSharingList.value = [row]
  timeSharingDialogTableVisible.value = true
}
function handleQuery() { getList() }
function resetQuery() {
  queryParams.value.queryTime = proxy.dayjs(new Date()).format("YYYY-MM-DD")
  handleQuery()
}
function handleExport() {
  proxy.download(
    "peakValley/segmentAnalysis/hour/export",
    { ...queryParams.value, ...query.value },
    `${queryParams.value.nodeName}_峰平谷分时统计_${new Date().getTime()}.xlsx`
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

/* ─── 上方双图行 ─── */
.chart-row-2 {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 12px;
  flex-shrink: 0;
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
.chart-body { width: 100%; height: 300px; }
.table-box  { padding: 0; }
</style>
