<template>
  <div class="carbon-asset-page">
    <!-- 顶部查询条件 -->
    <el-form :inline="true" class="search-bar">
      <el-form-item label="年份">
        <el-date-picker
          v-model="queryYear"
          type="year"
          placeholder="选择年份"
          format="YYYY"
          value-format="YYYY"
          style="width: 150px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
      </el-form-item>
    </el-form>

    <div class="content-layout">
      <!-- 左栏：三类碳资产 -->
      <div class="left-panel">
        <!-- 碳配额 -->
        <div class="section-title">碳配额</div>
        <el-table :data="quotaList" border size="small" class="asset-table">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="年度" prop="year" width="80" align="center" />
          <el-table-column label="类型" prop="typeName" align="center" />
          <el-table-column label="数量" align="center">
            <template #default="{ row }">
              <el-input
                v-model="row.quantity"
                v-hasPermi="['carbonAsset:asset:save']"
                size="small"
                @change="onQuotaChange"
              />
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" width="70" align="center" />
        </el-table>

        <!-- CCER -->
        <div class="section-title" style="margin-top: 20px">CCER</div>
        <el-table :data="ccerList" border size="small" class="asset-table">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="年度" prop="year" width="80" align="center" />
          <el-table-column label="类型" prop="typeName" align="center" />
          <el-table-column label="数量" align="center">
            <template #default="{ row }">
              <el-input v-model="row.quantity" v-hasPermi="['carbonAsset:asset:save']" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" width="70" align="center" />
        </el-table>

        <!-- 绿证 -->
        <div class="section-title" style="margin-top: 20px">绿证</div>
        <el-table :data="greenCertList" border size="small" class="asset-table">
          <el-table-column label="序号" type="index" width="60" align="center" />
          <el-table-column label="年度" prop="year" width="80" align="center" />
          <el-table-column label="类型" prop="typeName" align="center" />
          <el-table-column label="数量" align="center">
            <template #default="{ row }">
              <el-input v-model="row.quantity" v-hasPermi="['carbonAsset:asset:save']" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" width="70" align="center" />
        </el-table>
      </div>

      <!-- 右栏：月度碳配额 -->
      <div class="right-panel">
        <div class="right-header">
          <span class="total-quota-label">
            碳配额总量：<strong>{{ formatNumber(totalQuota) }}</strong>（tCO₂）
          </span>
          <el-button
            type="primary"
            size="small"
            v-hasPermi="['carbonAsset:asset:save']"
            @click="handleAvgByMonth"
          >
            按月均分
          </el-button>
        </div>

        <el-table
          :data="monthlyTableData"
          border
          size="small"
          class="monthly-table"
        >
          <el-table-column label="年度" prop="year" width="80" align="center" />
          <el-table-column label="月份" prop="monthLabel" width="70" align="center" />
          <el-table-column label="月度碳配额" align="center">
            <template #default="{ row }">
              <el-input
                v-if="!row.isTotal"
                v-model="row.monthlyQuota"
                v-hasPermi="['carbonAsset:asset:save']"
                size="small"
                @change="onMonthlyChange"
              />
              <span v-else>{{ formatNumber(monthlyTotal) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="单位" prop="unit" width="70" align="center" />
        </el-table>

        <div class="save-btn-row">
          <el-button
            type="primary"
            v-hasPermi="['carbonAsset:asset:save']"
            @click="handleSave"
          >
            保存数据
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup name="CarbonAsset">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCarbonAssetData, saveCarbonAsset } from '@/api/carbonasset/carbonAsset'

const queryYear = ref(String(new Date().getFullYear()))

const quotaList = ref([])
const ccerList = ref([])
const greenCertList = ref([])
const monthlyList = ref([])

// 碳配额总量（后端返回 + 前端实时计算）
const totalQuota = computed(() => {
  const find = (subType) => {
    const row = quotaList.value.find(r => r.subType === subType)
    return row ? Number(row.quantity) || 0 : 0
  }
  return find('GOVT_ISSUE') + find('PREV_SURPLUS') + find('BUY_IN') - find('SELL_OUT')
})

// 月度合计
const monthlyTotal = computed(() =>
  monthlyList.value.reduce((sum, r) => sum + (Number(r.monthlyQuota) || 0), 0)
)

// 月度表格数据（12行 + 合计行）
const monthlyTableData = computed(() => {
  const rows = monthlyList.value.map(r => ({
    ...r,
    monthLabel: `${r.month}月`,
    isTotal: false
  }))
  rows.push({
    year: '',
    monthLabel: '合计',
    monthlyQuota: null,
    unit: 'tCO₂',
    isTotal: true
  })
  return rows
})

async function handleSearch() {
  if (!queryYear.value) {
    ElMessage.warning('请选择年份')
    return
  }
  await loadData()
}

async function loadData() {
  const { data } = await getCarbonAssetData(Number(queryYear.value))
  quotaList.value = data.quotaList || []
  ccerList.value = data.ccerList || []
  greenCertList.value = data.greenCertList || []
  monthlyList.value = data.monthlyQuotaList || []
}

function handleAvgByMonth() {
  const avg = totalQuota.value / 12
  monthlyList.value.forEach(row => {
    row.monthlyQuota = avg.toFixed(4)
  })
}

// 触发合计重算（el-input change 事件）
function onQuotaChange() {}
function onMonthlyChange() {}

async function handleSave() {
  const assets = [
    ...quotaList.value.map(r => ({ assetType: r.assetType, subType: r.subType, quantity: Number(r.quantity) || 0 })),
    ...ccerList.value.map(r => ({ assetType: r.assetType, subType: r.subType, quantity: Number(r.quantity) || 0 })),
    ...greenCertList.value.map(r => ({ assetType: r.assetType, subType: r.subType, quantity: Number(r.quantity) || 0 }))
  ]
  const monthlyQuotas = monthlyList.value.map(r => ({
    month: r.month,
    monthlyQuota: Number(r.monthlyQuota) || 0
  }))
  await saveCarbonAsset({ year: Number(queryYear.value), assets, monthlyQuotas })
  ElMessage.success('保存成功')
}

function formatNumber(val) {
  if (val === null || val === undefined) return '0'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 4 })
}

onMounted(loadData)
</script>

<style scoped>
.carbon-asset-page {
  padding: 16px;
}
.search-bar {
  margin-bottom: 16px;
}
.content-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.left-panel {
  flex: 1;
  min-width: 0;
}
.right-panel {
  width: 480px;
  flex-shrink: 0;
}
.section-title {
  font-weight: 600;
  color: #00bcd4;
  margin-bottom: 6px;
  font-size: 14px;
}
.asset-table {
  width: 100%;
}
.right-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.total-quota-label {
  color: #00bcd4;
  font-size: 14px;
}
.monthly-table {
  width: 100%;
}
.save-btn-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
