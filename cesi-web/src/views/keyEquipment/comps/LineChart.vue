<template>
  <div class="chart-wrap">
    <div id="ChartDom" style="width:100%;height:100%"></div>
  </div>
</template>

<script setup>
import * as echarts from "echarts"
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()

const props = defineProps({
  chartData: { type: Object, default: () => ({}) },
})

watch(() => props.chartData, () => { initChart() })
watch(() => settingsStore.sideTheme, () => { initChart() })
onMounted(() => { initChart() })

function initChart() {
  const chartDom = document.getElementById("ChartDom")
  if (!chartDom) return
  if (echarts.getInstanceByDom(chartDom)) echarts.dispose(chartDom)
  const myChart = echarts.init(chartDom)

  const isDark = settingsStore.sideTheme === "theme-dark"
  const textColor = isDark ? "#6e90b8" : "#475569"
  const lineColor = isDark ? "rgba(0,140,220,.12)" : "rgba(0,0,0,.06)"

  setTimeout(() => {
    myChart.setOption({
      color: ["#00aaff", "#00e896", "#ffc940", "#ff4d6a"],
      title: {
        text: props.chartData.title || "",
        left: 40,
        textStyle: { color: textColor, fontSize: 13, fontWeight: 600 },
      },
      tooltip: { trigger: "axis", axisPointer: { type: "shadow" } },
      legend: {
        icon: "rect", itemWidth: 14, itemHeight: 10,
        textStyle: { color: textColor },
      },
      grid: { top: 55, left: 50, right: 40, bottom: 20, containLabel: true },
      xAxis: {
        type: "category",
        axisLine: { show: true, lineStyle: { color: textColor } },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: textColor, fontSize: 12, padding: [5, 0, 0, 0] },
        data: props.chartData.xData,
      },
      yAxis: {
        type: "value",
        nameTextStyle: { color: textColor, fontSize: 12, padding: [0, 0, 5, 0] },
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: true, lineStyle: { type: "dashed", color: lineColor } },
        axisLabel: { color: textColor, fontSize: 12 },
      },
      series: [{
        name: props.chartData.title,
        type: "bar",
        barWidth: "10",
        itemStyle: { borderRadius: [4, 4, 0, 0] },
        markPoint: { data: [{ type: "max", name: "Max" }, { type: "min", name: "Min" }] },
        data: props.chartData.yData,
      }],
    })
    window.addEventListener("resize", () => myChart.resize(), { passive: true })
  }, 200)
}
</script>

<style lang="scss" scoped>
.chart-wrap {
  width: 100%;
  height: 100%;
}
</style>
