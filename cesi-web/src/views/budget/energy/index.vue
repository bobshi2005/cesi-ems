<template>
  <div class="app-container">
    <!-- 页面头部：标题 + 汇总统计 -->
    <div class="page-header">
      <div class="page-title"></div>
      <div class="header-stats">
        <div class="hstat">
          <div class="hstat-label">预算总量</div>
          <div class="hstat-value">{{ formatNum(summary.totalBudget) }} <span class="hstat-unit">KWh</span></div>
        </div>
        <div class="hdivider"></div>
        <div class="hstat">
          <div class="hstat-label">已执行</div>
          <div class="hstat-value" style="color:var(--ems-accent-green)">{{ formatNum(summary.executed) }} <span class="hstat-unit">KWh</span></div>
        </div>
        <div class="hdivider"></div>
        <div class="hstat">
          <div class="hstat-label">执行率</div>
          <div class="hstat-value" style="color:var(--ems-accent-orange)">{{ summary.rate }} <span class="hstat-unit">%</span></div>
        </div>
      </div>
    </div>

    <el-form :inline="true" :model="queryParams" class="query-form">
      <el-form-item label="预算时间">
        <el-select v-model="queryParams.budgetType" placeholder="类型" style="width:80px" clearable>
          <el-option label="年" value="YEAR" />
          <el-option label="月" value="MONTH" />
          <el-option label="自定义" value="CUSTOM" />
        </el-select>
        <el-input-number v-model="queryParams.budgetYear" :min="2020" :max="2099"
          :controls="false" style="width:88px;margin-left:6px" placeholder="年份" />
        <el-select v-if="queryParams.budgetType === 'MONTH'" v-model="queryParams.budgetMonth"
          placeholder="月份" style="width:80px;margin-left:6px" clearable>
          <el-option v-for="m in 12" :key="m" :label="m+'月'" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="地区类型">
        <el-select v-model="queryParams.regionType" placeholder="请选择" style="width:100px" clearable>
          <el-option label="厂区" value="FACTORY" />
          <el-option label="车间" value="WORKSHOP" />
          <el-option label="工段" value="SECTION" />
          <el-option label="设备" value="DEVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="对应地区">
        <el-input v-model="queryParams.regionName" placeholder="请输入" style="width:140px" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
      <el-form-item style="float:right">
        <el-button type="primary" icon="Plus" v-hasPermi="['budget:energy:add']" @click="handleAdd">新建预算</el-button>
        <el-button type="danger" icon="Delete" v-hasPermi="['budget:energy:delete']" :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button icon="Download" v-hasPermi="['budget:energy:export']">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="44" align="center" />
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="预算范围" prop="budgetName" min-width="140" />
      <el-table-column label="预算时间" min-width="100">
        <template #default="{ row }">{{ formatBudgetTime(row) }}</template>
      </el-table-column>
      <el-table-column label="能源类型" width="90" align="center">
        <template #default="{ row }">
          <el-tag type="primary" size="small">{{ energyTypeLabel(row.energyType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="能源单位" prop="energyUnit" width="80" align="center" />
      <el-table-column label="产品单耗" min-width="120" align="right">
        <template #default="{ row }">
          <span v-if="row.unitConsumption">{{ row.unitConsumption }} {{ row.energyUnit }}/吨</span>
          <span v-else style="color:#c0c4cc">—</span>
        </template>
      </el-table-column>
      <el-table-column label="计划产量" min-width="110" align="right">
        <template #default="{ row }">{{ row.planQuantity }} {{ row.planQuantityUnit }}</template>
      </el-table-column>
      <el-table-column label="预算能耗" min-width="130" align="right">
        <template #default="{ row }">
          <span class="td-value">{{ formatNum(row.budgetEnergy) }}</span>
          <span class="td-unit"> {{ row.energyUnit }}</span>
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
          <span class="action-btn action-edit" v-hasPermi="['budget:energy:edit']" @click="handleEdit(row)">修改</span>
          <span class="action-btn action-del" v-hasPermi="['budget:energy:delete']" @click="handleDelete(row)">删除</span>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="860px" destroy-on-close @close="resetForm">
      <div style="display:flex;gap:0;min-height:380px">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px"
          style="flex:1;padding-right:16px;overflow-y:auto">
          <el-row :gutter="12">
            <el-col :span="24">
              <el-form-item label="预算名称" prop="budgetName">
                <el-input v-model="form.budgetName" placeholder="请输入预算名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预算类型" prop="budgetType">
                <el-select v-model="form.budgetType" style="width:100%" @change="onBudgetTypeChange">
                  <el-option label="年" value="YEAR" />
                  <el-option label="月" value="MONTH" />
                  <el-option label="自定义" value="CUSTOM" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预算年份" prop="budgetYear">
                <el-input-number v-model="form.budgetYear" :min="2020" :max="2099"
                  :controls="false" style="width:100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="form.budgetType === 'MONTH'">
              <el-form-item label="预算月份" prop="budgetMonth">
                <el-select v-model="form.budgetMonth" style="width:100%">
                  <el-option v-for="m in 12" :key="m" :label="m+'月'" :value="m" />
                </el-select>
              </el-form-item>
            </el-col>
            <template v-if="form.budgetType === 'CUSTOM'">
              <el-col :span="12">
                <el-form-item label="起始日期" prop="budgetStart">
                  <el-date-picker v-model="form.budgetStart" type="date"
                    value-format="YYYY-MM-DD" style="width:100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束日期" prop="budgetEnd">
                  <el-date-picker v-model="form.budgetEnd" type="date"
                    value-format="YYYY-MM-DD" style="width:100%" />
                </el-form-item>
              </el-col>
            </template>
            <el-col :span="12">
              <el-form-item label="能源类型" prop="energyType">
                <el-select v-model="form.energyType" style="width:100%">
                  <el-option label="电" value="ELECTRICITY" />
                  <el-option label="天然气" value="GAS" />
                  <el-option label="蒸汽" value="STEAM" />
                  <el-option label="煤" value="COAL" />
                  <el-option label="燃油" value="OIL" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="能源单位">
                <el-select v-model="form.energyUnit" style="width:100%">
                  <el-option label="KWh" value="KWh" />
                  <el-option label="m³" value="m³" />
                  <el-option label="GJ" value="GJ" />
                  <el-option label="吨" value="吨" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="地区类型" prop="regionType">
                <el-select v-model="form.regionType" style="width:100%">
                  <el-option label="厂区" value="FACTORY" />
                  <el-option label="车间" value="WORKSHOP" />
                  <el-option label="工段" value="SECTION" />
                  <el-option label="设备" value="DEVICE" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="对应地区" prop="regionName">
                <el-input v-model="form.regionName" placeholder="请输入" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="产品单耗">
                <el-input :model-value="form.unitConsumption" readonly
                  placeholder="从右侧选择后自动填充" :style="{ '--el-input-bg-color': 'var(--ems-bg-main)' }">
                  <template #append>{{ form.energyUnit }}/吨</template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="计划产量" prop="planQuantity">
                <el-input-number v-model="form.planQuantity" :min="0" :precision="2"
                  style="width:100%" @change="calcBudgetEnergy" />
              </el-form-item>
            </el-col>
            <el-col :span="24" v-if="calcResult">
              <el-alert :title="`预算能耗（自动计算）：${calcResult} ${form.energyUnit}`"
                type="info" show-icon :closable="false" />
            </el-col>
            <el-col :span="24">
              <el-form-item label="关联节点">
                <el-select v-model="form.nodeId" placeholder="选择模型节点以接入实际能耗数据"
                  style="width:100%" clearable filterable>
                  <el-option v-for="n in modelNodes" :key="n.value"
                    :label="n.label" :value="n.value" />
                </el-select>
                <div style="font-size:11px;color:var(--ems-text-muted);margin-top:3px">
                  关联后分析页可自动读取实际能耗，进行预算对比
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>

        <div style="width:240px;border-left:1px solid var(--ems-border-dim);padding-left:12px;
          display:flex;flex-direction:column;flex-shrink:0">
          <div style="font-size:12px;font-weight:600;color:var(--ems-text-muted);margin-bottom:8px">产品单耗来源</div>
          <el-select v-model="treeYear" style="width:100%;margin-bottom:8px" size="small">
            <el-option v-for="y in [2024,2025,2026,2027]" :key="y" :label="y+'年'" :value="y" />
          </el-select>
          <el-tree :data="sourceTree" :props="{ label:'label', children:'children' }"
            node-key="id" highlight-current default-expand-all
            @node-click="onTreeNodeClick" style="flex:1;font-size:12px;overflow-y:auto" />
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="EnergyBudget">
import { ref, reactive, computed, onMounted } from 'vue'

import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getEnergyBudgetPage, addEnergyBudget, editEnergyBudget,
  deleteEnergyBudget, deleteEnergyBudgetBatch
} from '@/api/budget/energyBudget'
import { listModelNode } from '@/api/basicsetting/modelNode'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const summary = reactive({ totalBudget: 0, executed: 0, rate: '0.0' })
const selectedIds = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建用能预算')
const formRef = ref(null)
const treeYear    = ref(2026)
const modelNodes  = ref([])   // 模型节点列表，用于绑定预算到实际能耗数据

const queryParams = reactive({
  budgetType: 'YEAR',
  budgetYear: new Date().getFullYear(),
  budgetMonth: null,
  regionType: '',
  regionName: '',
  pageNum: 1,
  pageSize: 10
})

const defaultForm = () => ({
  id: null, budgetName: '', budgetType: 'YEAR',
  budgetYear: new Date().getFullYear(), budgetMonth: null,
  budgetStart: null, budgetEnd: null,
  energyType: 'ELECTRICITY', energyUnit: 'KWh',
  regionType: 'FACTORY', regionName: '',
  unitConsumption: null, unitConsumptionSource: 'MANUAL',
  planQuantity: null, planQuantityUnit: '吨', budgetEnergy: null,
  warningThreshold: 90,
  nodeId: null   // 关联模型节点，用于查询实际能耗
})
const form = reactive(defaultForm())

const calcResult = computed(() => {
  if (form.unitConsumption && form.planQuantity)
    return (Number(form.unitConsumption) * Number(form.planQuantity))
      .toLocaleString('zh-CN', { maximumFractionDigits: 2 })
  return null
})

const sourceTree = ref([
  { id: 'indicator', label: '节能指标', children: [
    { id: 'i1', label: '全厂综合单耗', value: 18.5 },
    { id: 'i2', label: '1号间单耗指标', value: 24.8 },
    { id: 'i3', label: '一车间单耗指标', value: 4.22 },
  ]},
  { id: 'performance', label: '能源绩效', children: [
    { id: 'p1', label: '全厂电耗绩效', value: 23.34 },
    { id: 'p2', label: '一工段能效绩效', value: 396.26 },
  ]},
  { id: 'history', label: '历史实绩', children: [
    { id: 'h1', label: '2025年全厂实绩', value: 22.1 },
    { id: 'h2', label: '2024年全厂实绩', value: 21.8 },
  ]},
  { id: 'manual', label: '手动输入', value: null },
])

const rules = {
  budgetName:   [{ required: true, message: '请输入预算名称', trigger: 'blur' }],
  budgetType:   [{ required: true, message: '请选择预算类型', trigger: 'change' }],
  budgetYear:   [{ required: true, message: '请输入年份', trigger: 'blur' }],
  energyType:   [{ required: true, message: '请选择能源类型', trigger: 'change' }],
  regionType:   [{ required: true, message: '请选择地区类型', trigger: 'change' }],
  regionName:   [{ required: true, message: '请输入对应地区', trigger: 'blur' }],
  planQuantity: [{ required: true, message: '请输入计划产量', trigger: 'blur' }],
}

const energyTypeLabel = v =>
  ({ ELECTRICITY:'电', GAS:'天然气', STEAM:'蒸汽', COAL:'煤', OIL:'燃油' }[v] || v)

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

function formatBudgetTime(row) {
  if (row.budgetType === 'YEAR')  return `${row.budgetYear}年`
  if (row.budgetType === 'MONTH') return `${row.budgetYear}年${row.budgetMonth}月`
  return `${row.budgetStart} 至 ${row.budgetEnd}`
}

function formatNum(v) {
  return v != null
    ? Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
    : '—'
}

function onBudgetTypeChange() {
  form.budgetMonth = null
  form.budgetStart = null
  form.budgetEnd   = null
}

function onTreeNodeClick(node) {
  if (node.id === 'manual') {
    form.unitConsumption       = null
    form.unitConsumptionSource = 'MANUAL'
  } else if (node.value != null) {
    form.unitConsumption       = node.value
    form.unitConsumptionSource = node.id
    calcBudgetEnergy()
  }
}

function calcBudgetEnergy() {
  if (form.unitConsumption && form.planQuantity)
    form.budgetEnergy =
      +(Number(form.unitConsumption) * Number(form.planQuantity)).toFixed(4)
}

async function loadData() {
  loading.value = true
  try {
    const res = await getEnergyBudgetPage({
      budgetType:  queryParams.budgetType  || undefined,
      budgetYear:  queryParams.budgetYear  || undefined,
      budgetMonth: queryParams.budgetMonth || undefined,
      regionType:  queryParams.regionType  || undefined,
      regionName:  queryParams.regionName  || undefined,
      pageNum:  queryParams.pageNum,
      pageSize: queryParams.pageSize,
    })
    const rows = res.rows || []
    tableData.value = rows
    total.value     = res.total || 0
    // 汇总统计：优先用后端返回的 summary，否则从当前页数据计算
    if (res.summary) {
      summary.totalBudget = res.summary.totalBudget || 0
      summary.executed    = res.summary.executed    || 0
    } else {
      summary.totalBudget = rows.reduce((s, r) => s + (Number(r.budgetEnergy)  || 0), 0)
      summary.executed    = rows.reduce((s, r) => s + (Number(r.actualEnergy)  || 0), 0)
    }
    summary.rate = summary.totalBudget > 0
      ? (summary.executed / summary.totalBudget * 100).toFixed(1)
      : '0.0'
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(queryParams, {
    budgetType: 'YEAR', budgetYear: new Date().getFullYear(),
    budgetMonth: null, regionType: '', regionName: '', pageNum: 1
  })
  loadData()
}
function handleSelectionChange(rows) { selectedIds.value = rows.map(r => r.id) }

function handleAdd() {
  Object.assign(form, defaultForm())
  dialogTitle.value   = '新建用能预算'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value   = '修改用能预算'
  dialogVisible.value = true
}

function handleAnalysis() {
  router.push({ path: '/budget/budgetanalysis', query: { dimension: 'energy' } })
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除预算「${row.budgetName}」？`, '提示', { type: 'warning' })
  await deleteEnergyBudget(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(
    `确认删除已选 ${selectedIds.value.length} 条预算？`, '提示', { type: 'warning' })
  await deleteEnergyBudgetBatch(selectedIds.value)
  ElMessage.success('批量删除成功')
  loadData()
}

async function handleSubmit() {
  await formRef.value.validate()
  if (form.id) {
    await editEnergyBudget({ ...form })
    ElMessage.success('修改成功')
  } else {
    await addEnergyBudget({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

function resetForm() { formRef.value?.resetFields() }

async function loadModelNodes() {
  try {
    const res = await listModelNode({})
    modelNodes.value = (res.rows || res.data || []).map(n => ({
      value: n.nodeId,
      label: n.name
    }))
  } catch { /* 节点列表加载失败不阻断主流程 */ }
}

onMounted(() => { loadData(); loadModelNodes() })
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
.hstat-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--ems-accent-bright);
  line-height: 1;
}
.hstat-unit { font-size: 10px; color: var(--ems-text-secondary); }
.hdivider { width: 1px; height: 30px; background: var(--ems-border-dim); }

.td-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--ems-accent-bright);
}
.td-unit {
  font-size: 11px;
  color: var(--ems-text-muted);
}
.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--ems-text-primary);
}
.status-dot::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-active::before  { background: var(--ems-accent-green);  box-shadow: 0 0 6px var(--ems-accent-green); }
.status-warning::before { background: var(--ems-accent-yellow); box-shadow: 0 0 6px var(--ems-accent-yellow); }
.status-over::before    { background: var(--ems-accent-red);    box-shadow: 0 0 6px var(--ems-accent-red); }

.action-btn {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  cursor: pointer;
  border: 1px solid;
  transition: all .15s;
  margin: 0 2px;
}
.action-view { background: rgba(0,232,150,.08); border-color: rgba(0,232,150,.25); color: #44ddaa; }
.action-view:hover { background: rgba(0,232,150,.18); }
.action-edit { background: rgba(0,140,255,.1); border-color: rgba(0,140,255,.3); color: #66aaff; }
.action-edit:hover { background: rgba(0,140,255,.22); border-color: rgba(0,140,255,.55); }
.action-del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-del:hover  { background: rgba(255,77,106,.18); }
</style>
