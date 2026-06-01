<template>
  <div class="app-container">
    <div class="page-header">
      <div class="page-title"></div>
      <div class="header-stats">
        <div class="hstat">
          <div class="hstat-label">预算总排放</div>
          <div class="hstat-value">{{ formatNum(summary.totalBudget) }} <span class="hstat-unit">tCO₂e</span></div>
        </div>
        <div class="hdivider"></div>
        <div class="hstat">
          <div class="hstat-label">已核算排放</div>
          <div class="hstat-value" style="color:var(--ems-accent-orange)">{{ formatNum(summary.executed) }} <span class="hstat-unit">tCO₂e</span></div>
        </div>
        <div class="hdivider"></div>
        <div class="hstat">
          <div class="hstat-label">执行率</div>
          <div class="hstat-value" style="color:var(--ems-accent-yellow)">{{ summary.rate }} <span class="hstat-unit">%</span></div>
        </div>
      </div>
    </div>

    <el-form :inline="true" :model="queryParams" class="query-form">
      <el-form-item label="预算时间">
        <el-select v-model="queryParams.budgetTimeType" placeholder="类型" style="width:80px" clearable>
          <el-option label="年" value="YEAR" />
          <el-option label="月" value="MONTH" />
        </el-select>
        <el-input-number v-model="queryParams.budgetYear" :min="2020" :max="2099"
          :controls="false" style="width:88px;margin-left:6px" placeholder="年份" />
        <el-select v-if="queryParams.budgetTimeType === 'MONTH'" v-model="queryParams.budgetMonth"
          style="width:80px;margin-left:6px" placeholder="月" clearable>
          <el-option v-for="m in 12" :key="m" :label="m+'月'" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="预算类型">
        <el-select v-model="queryParams.budgetType" placeholder="请选择" style="width:120px" clearable>
          <el-option label="总量控制" value="TOTAL_CONTROL" />
          <el-option label="产值强度" value="VALUE_INTENSITY" />
          <el-option label="产量强度" value="QTY_INTENSITY" />
        </el-select>
      </el-form-item>
      <el-form-item label="对应边界">
        <el-input v-model="queryParams.boundaryName" placeholder="请输入" style="width:140px" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
      <el-form-item style="float:right">
        <el-button type="primary" icon="Plus" v-hasPermi="['budget:carbon:add']" @click="handleAdd">新建预算</el-button>
        <el-button type="danger" icon="Delete" v-hasPermi="['budget:carbon:delete']"
          :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button icon="Download" v-hasPermi="['budget:carbon:export']">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="44" align="center" />
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="预算边界" prop="boundaryName" min-width="140" />
      <el-table-column label="预算时间" min-width="100">
        <template #default="{ row }">
          {{ row.budgetYear }}年{{ row.budgetMonth ? row.budgetMonth + '月' : '' }}
        </template>
      </el-table-column>
      <el-table-column label="预算类型" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="budgetTypeTag(row.budgetType)" size="small">
            {{ budgetTypeLabel(row.budgetType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="监管标准" prop="regulationStandard" min-width="120" show-overflow-tooltip />
      <el-table-column label="计划产值/产量" min-width="120" align="right">
        <template #default="{ row }">
          <span v-if="row.planValue">{{ formatNum(row.planValue) }} {{ row.planValueUnit }}</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="预算排放量（tCO₂e）" min-width="150" align="right">
        <template #default="{ row }">
          <span class="td-value">{{ formatNum(row.budgetEmission) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行状态" width="100" align="center">
        <template #default="{ row }">
          <span class="status-dot" :class="execStatusClass(row)">{{ execStatusLabel(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <span class="action-btn action-view" @click="handleAnalysis(row)">分析</span>
          <span class="action-btn action-edit" v-hasPermi="['budget:carbon:edit']" @click="handleEdit(row)">修改</span>
          <span class="action-btn action-del" v-hasPermi="['budget:carbon:delete']" @click="handleDelete(row)">删除</span>
        </template>
      </el-table-column>
    </el-table>

    <EmsPageBar
      v-model:page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      @change="loadData"
    />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" destroy-on-close @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item label="预算边界" prop="boundaryName">
              <el-input v-model="form.boundaryName" placeholder="如：全厂区、一车间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算类型" prop="budgetType">
              <el-select v-model="form.budgetType" style="width:100%" @change="onBudgetTypeChange">
                <el-option label="总量控制" value="TOTAL_CONTROL" />
                <el-option label="产值强度" value="VALUE_INTENSITY" />
                <el-option label="产量强度" value="QTY_INTENSITY" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预算时间" prop="budgetYear">
              <div style="display:flex;gap:6px;width:100%">
                <el-select v-model="form.budgetTimeType" style="flex:1">
                  <el-option label="年" value="YEAR" />
                  <el-option label="月" value="MONTH" />
                </el-select>
                <el-input-number v-model="form.budgetYear" :min="2020" :max="2099"
                  :controls="false" style="flex:1" />
                <el-select v-if="form.budgetTimeType === 'MONTH'" v-model="form.budgetMonth" style="flex:1">
                  <el-option v-for="m in 12" :key="m" :label="m+'月'" :value="m" />
                </el-select>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="监管标准">
              <el-select v-model="form.regulationStandard" style="width:100%" clearable placeholder="请选择">
                <el-option label="GB/T 32151 工业企业温室气体" value="GB/T 32151" />
                <el-option label="电力行业排放核算指南" value="电力行业排放核算指南" />
                <el-option label="钢铁行业排放核算指南" value="钢铁行业排放核算指南" />
                <el-option label="企业自定义" value="企业自定义" />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 强度类型字段 -->
          <template v-if="form.budgetType !== 'TOTAL_CONTROL'">
            <el-col :span="12">
              <el-form-item :label="intensityLabel" prop="emissionIntensity">
                <el-input-number v-model="form.emissionIntensity" :min="0" :precision="4"
                  style="width:100%" @change="calcEmission" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="planLabel" prop="planValue">
                <el-input-number v-model="form.planValue" :min="0" :precision="2"
                  style="width:100%" @change="calcEmission" />
              </el-form-item>
            </el-col>
          </template>

          <!-- 总量控制字段 -->
          <el-col v-if="form.budgetType === 'TOTAL_CONTROL'" :span="24">
            <el-form-item label="预算排放总量" prop="budgetEmission">
              <el-input-number v-model="form.budgetEmission" :min="0" :precision="2"
                style="width:100%" placeholder="tCO₂e" />
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="calcEmissionResult !== null">
            <el-alert :title="`预算排放量（自动计算）：${calcEmissionResult} tCO₂e`"
              type="info" show-icon :closable="false" />
          </el-col>

          <el-col :span="12">
            <el-form-item label="预警阈值">
              <el-input-number v-model="form.warningThreshold" :min="1" :max="100"
                style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="通知方式">
              <el-select v-model="form.warningMethod" style="width:100%">
                <el-option label="系统消息" value="SYSTEM" />
                <el-option label="邮件通知" value="EMAIL" />
                <el-option label="系统消息+邮件" value="BOTH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CarbonBudget">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getCarbonBudgetPage, addCarbonBudget, editCarbonBudget,
  deleteCarbonBudget, deleteCarbonBudgetBatch
} from '@/api/budget/carbonBudget'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])
const summary = reactive({ totalBudget: 0, executed: 0, rate: '0.0' })
const dialogVisible = ref(false)
const dialogTitle = ref('新建碳排放预算')
const formRef = ref(null)

const queryParams = reactive({
  budgetTimeType: 'YEAR',
  budgetYear: new Date().getFullYear(),
  budgetMonth: null,
  budgetType: '',
  boundaryName: '',
  pageNum: 1,
  pageSize: 10
})

const defaultForm = () => ({
  id: null, boundaryName: '', budgetType: 'TOTAL_CONTROL',
  budgetTimeType: 'YEAR', budgetYear: new Date().getFullYear(), budgetMonth: null,
  regulationStandard: '', emissionIntensity: null,
  planValue: null, planValueUnit: '万元', budgetEmission: null,
  warningThreshold: 90, warningMethod: 'SYSTEM'
})
const form = reactive(defaultForm())

const intensityLabel = computed(() =>
  form.budgetType === 'VALUE_INTENSITY' ? '排放强度(tCO₂e/万元)' : '排放强度(tCO₂e/吨)')
const planLabel = computed(() =>
  form.budgetType === 'VALUE_INTENSITY' ? '计划产值（万元）' : '计划产量（吨）')

const calcEmissionResult = computed(() => {
  if (form.budgetType === 'TOTAL_CONTROL') return null
  if (form.emissionIntensity && form.planValue)
    return (Number(form.emissionIntensity) * Number(form.planValue))
      .toLocaleString('zh-CN', { maximumFractionDigits: 2 })
  return null
})

function calcEmission() {
  if (form.budgetType !== 'TOTAL_CONTROL' && form.emissionIntensity && form.planValue)
    form.budgetEmission =
      +(Number(form.emissionIntensity) * Number(form.planValue)).toFixed(4)
}

function onBudgetTypeChange() {
  form.emissionIntensity = null
  form.planValue = null
  form.budgetEmission = null
  form.planValueUnit = form.budgetType === 'VALUE_INTENSITY' ? '万元' : '吨'
}

const budgetTypeLabel = v =>
  ({ TOTAL_CONTROL:'总量控制', VALUE_INTENSITY:'产值强度', QTY_INTENSITY:'产量强度' }[v] || v)
const budgetTypeTag = v =>
  ({ TOTAL_CONTROL:'warning', VALUE_INTENSITY:'', QTY_INTENSITY:'success' }[v] || '')

function execStatusClass(row) {
  const s = row.execStatus
  if (s === 'OVER') return 'status-over'
  if (s === 'WARNING') return 'status-warning'
  return 'status-active'
}
function execStatusLabel(row) {
  const s = row.execStatus
  if (s === 'OVER') return '超标'
  if (s === 'WARNING') return '预警'
  return '执行中'
}
const formatNum = v =>
  v != null ? Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }) : '—'

const rules = {
  boundaryName:     [{ required: true, message: '请输入预算边界名称', trigger: 'blur' }],
  budgetType:       [{ required: true, message: '请选择预算类型',     trigger: 'change' }],
  budgetYear:       [{ required: true, message: '请输入年份',         trigger: 'blur' }],
  emissionIntensity:[{ required: true, message: '请输入排放强度',     trigger: 'blur' }],
  planValue:        [{ required: true, message: '请输入计划产值/产量',trigger: 'blur' }],
  budgetEmission:   [{ required: true, message: '请输入预算排放总量', trigger: 'blur' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCarbonBudgetPage({
      budgetTimeType: queryParams.budgetTimeType || undefined,
      budgetYear:     queryParams.budgetYear     || undefined,
      budgetMonth:    queryParams.budgetMonth    || undefined,
      budgetType:     queryParams.budgetType     || undefined,
      boundaryName:   queryParams.boundaryName   || undefined,
      pageNum:  queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })
    const rows = res.rows || []
    tableData.value = rows
    total.value = res.total || 0
    if (res.summary) {
      summary.totalBudget = res.summary.totalBudget || 0
      summary.executed    = res.summary.executed    || 0
    } else {
      summary.totalBudget = rows.reduce((s, r) => s + (Number(r.budgetEmission) || 0), 0)
      summary.executed    = rows.reduce((s, r) => s + (Number(r.actualEmission) || 0), 0)
    }
    summary.rate = summary.totalBudget > 0
      ? (summary.executed / summary.totalBudget * 100).toFixed(1) : '0.0'
  } finally { loading.value = false }
}

function handleSearch()  { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, {
    budgetTimeType: 'YEAR', budgetYear: new Date().getFullYear(),
    budgetMonth: null, budgetType: '', boundaryName: '', pageNum: 1
  })
  loadData()
}
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function handleAdd() {
  Object.assign(form, defaultForm())
  dialogTitle.value   = '新建碳排放预算'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value   = '修改碳排放预算'
  dialogVisible.value = true
}

function handleAnalysis() {
  router.push({ path: '/budget/budgetanalysis', query: { dimension: 'carbon' } })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确认删除「${row.boundaryName}」的碳排放预算？`, '提示', { type: 'warning' })
  await deleteCarbonBudget(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(
    `确认删除已选 ${selectedIds.value.length} 条记录？`, '提示', { type: 'warning' })
  await deleteCarbonBudgetBatch(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) {
    await editCarbonBudget({ ...form })
    ElMessage.success('修改成功')
  } else {
    await addCarbonBudget({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

onMounted(loadData)
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0 14px;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px;
  position: relative;
  &::after {
    content: '';
    position: absolute;
    left: 0; bottom: 0; right: 0; height: 1px;
    background: linear-gradient(90deg, transparent, var(--ems-accent), transparent);
    opacity: .35;
  }
}
.page-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ems-text-primary);
  letter-spacing: .8px;
  flex: 1;
  span { color: var(--ems-accent-bright); }
}
.header-stats { display: flex; align-items: center; gap: 20px; }
.hstat { display: flex; flex-direction: column; align-items: flex-end; gap: 1px; }
.hstat-label { font-size: 10px; color: var(--ems-text-muted); }
.hstat-value { font-size: 18px; font-weight: 700; color: var(--ems-accent-bright); line-height: 1; }
.hstat-unit  { font-size: 10px; color: var(--ems-text-secondary); }
.hdivider    { width: 1px; height: 30px; background: var(--ems-border-dim); }

.td-value { font-size: 13px; font-weight: 600; color: var(--ems-accent-bright); }

.status-dot {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11.5px; color: var(--ems-text-primary);
  &::before { content: ''; width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
}
.status-active::before  { background: var(--ems-accent-green);  box-shadow: 0 0 6px var(--ems-accent-green); }
.status-warning::before { background: var(--ems-accent-yellow); box-shadow: 0 0 6px var(--ems-accent-yellow); }
.status-over::before    { background: var(--ems-accent-red);    box-shadow: 0 0 6px var(--ems-accent-red); }

.action-btn {
  display: inline-flex; align-items: center;
  padding: 2px 8px; border-radius: 3px; font-size: 11px;
  cursor: pointer; border: 1px solid; transition: all .15s; margin: 0 2px;
}
.action-view { background: rgba(0,232,150,.08); border-color: rgba(0,232,150,.25); color: #44ddaa; }
.action-view:hover { background: rgba(0,232,150,.18); }
.action-edit { background: rgba(0,140,255,.1); border-color: rgba(0,140,255,.3); color: #66aaff; }
.action-edit:hover { background: rgba(0,140,255,.22); }
.action-del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-del:hover  { background: rgba(255,77,106,.18); }
</style>
