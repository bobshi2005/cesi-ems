<template>
  <div class="chart-box">
    <div ref="chartRef" style="width: 100%; height: 100%"></div>
  </div>
</template>

<script setup>
import { nextTick, ref } from "vue"
import * as echarts from "echarts"
const { proxy } = getCurrentInstance()
import useSettingsStore from "@/store/modules/settings"
const settingsStore = useSettingsStore()

const chartRef = ref(null)

const props = defineProps({
  chartData: {
    type: Object,
    default: () => {},
  },
})

watch(
  () => props.chartData,
  (val) => {
    nextTick(() => initChart())
  }
)
watch(
  () => settingsStore.sideTheme,
  (val) => {
    initChart()
  }
)

onMounted(() => {
  nextTick(() => initChart())
})

function initChart(value) {
  const chartDom = chartRef.value
  if (!chartDom) return
  if (echarts.getInstanceByDom(chartDom)) {
    echarts.dispose(chartDom)
  }
  const myChart = echarts.init(chartDom)
  let option = {
    title: {
      text: props.chartData.title,
      left: "40",
      textStyle: {
        color: "#2979ff",
      },
    },
    color: ["#40c2ff", "#2979ff", "#ff9900", "#fa3534"],
    tooltip: {
      trigger: "axis",
      axisPointer: {
        type: "shadow",
      },
    },
    legend: {
      icon: "rect",
      itemWidth: 14,
      itemHeight: 10,
      textStyle: {
        color: "#6e90b8",
      },
    },
    grid: {
      top: "60",
      left: "50",
      right: "40",
      bottom: "20",
      containLabel: true,
    },
    xAxis: {
      type: "category",
      axisPointer: {
        type: "shadow",
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: "#6e90b8",
        },
      },
      axisTick: {
        show: false,
      },
      splitArea: {
        show: false,
      },
      splitLine: {
        show: false,
      },
      axisLabel: {
        color: "#6e90b8",
        fontSize: 14,
        padding: [5, 0, 0, 0],
      },
      data: props.chartData.xData,
    },
    yAxis: [
      {
        type: "value",
        nameTextStyle: {
          color: "#6e90b8",
          fontSize: 14,
          padding: [0, 0, 5, 0],
        },
        axisLine: {
          show: false,
        },
        splitLine: {
          show: true,
          lineStyle: {
            type: "dashed",
            color: "#6e90b8",
          },
        },
        axisTick: {
          show: false,
        },
        splitArea: {
          show: false,
        },
        axisLabel: {
          color: "#6e90b8",
          fontSize: 14,
        },
      },
    ],
    series: [
      {
        name: props.chartData.title,
        type: "bar",
        barWidth: "16",
        itemStyle: {
          borderRadius: [15, 15, 0, 0],
        },
        data: props.chartData.yData,
        markPoint: {
          data: [
            { type: "max", name: "Max" },
            { type: "min", name: "Min" },
          ],
        },
      },
    ],
  }
  setTimeout(() => {
    myChart.setOption(option)
  }, 200)

  window.addEventListener(
    "resize",
    () => {
      myChart.resize()
    },
    { passive: true }
  )
}
</script>

<style lang="scss" scoped>
.chart-box {
  width: 100%;
  height: 360px;
  padding: 20px 0 10px;
}
</style>
