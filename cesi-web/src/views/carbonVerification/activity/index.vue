<template>
  <div class="app-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <span class="filter-label">组织单元</span>
      <el-select v-model="query.orgUnit" style="width:120px">
        <el-option label="全厂" value="全厂" />
      </el-select>

      <div class="filter-divider"></div>

      <span class="filter-label">核查年度</span>
      <el-date-picker v-model="query.year" type="year" value-format="YYYY"
        style="width:120px" placeholder="选择年份" />

      <el-button class="btn-search" :loading="loading" @click="handleSearch">
        <svg class="btn-icon" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="7" cy="7" r="5"/><line x1="11" y1="11" x2="14" y2="14"/>
        </svg>
        查询
      </el-button>
      <el-button class="btn-reset" @click="handleReset">重置</el-button>

      <div style="margin-left:auto;display:flex;gap:6px;align-items:center">
        <el-button class="btn-action-green"
          @click="$router.push({ path: '/carbonVerification/report' })">
          生成排放报告
        </el-button>
        <el-button class="btn-action"
          v-hasPermi="['carbonVerification:activity:add']"
          @click="handleAdd">新增</el-button>
        <el-upload :show-file-list="false" :before-upload="handleImport" accept=".xlsx,.xls"
          v-hasPermi="['carbonVerification:activity:import']" style="display:inline-block">
          <el-button class="btn-action">导入</el-button>
        </el-upload>
        <el-button class="btn-export"
          v-hasPermi="['carbonVerification:activity:export']"
          @click="handleExport">
          <svg class="btn-icon" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12L8 7L13 12M8 7V2"/><rect x="2" y="13" width="12" height="1.5" rx=".75"/>
          </svg>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-strip" v-if="summary">
      <div class="scard blue">
        <div class="scard-label">年度总排放量</div>
        <div class="scard-value">{{ summary.totalEmission }}</div>
        <div class="scard-unit">tCO₂e</div>
        <div class="scard-sub">{{ summary.sourceCount }} 个排放源</div>
      </div>
      <div class="scard green">
        <div class="scard-label">排放源数量</div>
        <div class="scard-value">{{ summary.sourceCount }}</div>
        <div class="scard-unit">个</div>
        <div class="scard-sub">直接 {{ summary.directCount ?? 0 }} | 间接 {{ summary.indirectCount ?? 0 }}</div>
      </div>
      <div class="scard orange">
        <div class="scard-label">凭证上传进度</div>
        <div class="scard-value">
          {{ summary.voucherUploadedMonths }}<span class="scard-unit-inline">/{{ expectedMonths }}月</span>
        </div>
        <div class="scard-unit"></div>
        <div class="scard-sub">{{ Math.round(summary.voucherUploadedMonths / expectedMonths * 100) }}% 已完成</div>
      </div>
      <div class="scard" :class="summary.dataCompleteness >= 90 ? 'green' : 'red'">
        <div class="scard-label">数据完整性</div>
        <div class="scard-value">{{ summary.dataCompleteness }}</div>
        <div class="scard-unit">%</div>
        <div class="scard-sub" v-if="summary.missingWarning">{{ summary.missingWarning }}</div>
        <div class="scard-sub" v-else>数据完整度良好</div>
      </div>
    </div>

    <!-- 预警提示 -->
    <div v-if="summary && summary.missingWarning" class="notice-bar">
      <span class="notice-icon">⚠</span>
      <strong>待处理：</strong>{{ summary.missingWarning }}
    </div>

    <!-- 排放源活动数据明细 -->
    <div class="chart-card" style="margin-bottom:14px">
      <div class="chart-card-head">
        <span>排放源活动数据明细</span>
        <span class="chart-head-sub">共 {{ sourceList.length }} 条</span>
      </div>
      <div style="overflow-x:auto">
        <el-table :data="sourceList" border stripe v-loading="loading" size="small">
          <el-table-column label="序号" type="index" width="55" align="center" />
          <el-table-column label="排放源/活动" prop="emissionSource" align="center" min-width="100" />
          <el-table-column label="排放类型" prop="emissionType" align="center" width="100">
            <template #default="{ row }">
              <span class="type-badge" :class="row.emissionType === 'DIRECT' ? 'badge-blue' : 'badge-green'">
                {{ row.emissionType === 'DIRECT' ? '直接排放' : '间接排放' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="数据周期" prop="dataPeriod" align="center" width="90" />
          <el-table-column label="活动数据方式" prop="dataMethod" align="center" width="110" />
          <el-table-column label="单位" prop="unit" align="center" width="70" />
          <el-table-column label="年度活动量" prop="annualActivityValue" align="center" width="110" />
          <el-table-column label="年度排放量(tCO₂e)" prop="annualEmissionAmount" align="center" width="150">
            <template #default="{ row }">
              <span class="td-value">{{ row.annualEmissionAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="凭证状态" align="center" width="100">
            <template #default="{ row }">
              <span class="status-dot" :class="voucherDotClass(row.voucherStatus)">
                {{ voucherTagLabel(row.voucherStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="220" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showDetail(row)">活动数据</el-button>
              <el-button link type="warning" size="small" @click="showFactor(row)">实测因子</el-button>
              <el-button link type="success" size="small"
                v-hasPermi="['carbonVerification:voucher:upload']"
                @click="handleUploadVoucher(row)">上传凭证</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 月度凭证热力矩阵 -->
    <div v-if="summary && summary.monthlyProgress" class="chart-card">
      <div class="chart-card-head">
        <span>月度凭证上传进度</span>
        <div class="chart-legend">
          <span class="legend-item uploaded">已上传</span>
          <span class="legend-item missing">缺失（点击补充）</span>
          <span class="legend-item pending">未到期</span>
        </div>
      </div>
      <div style="padding:14px;overflow-x:auto">
        <table style="border-collapse:collapse;font-size:11px;min-width:600px">
          <thead>
            <tr>
              <th style="padding:4px 10px;text-align:left;color:var(--ems-text-muted);font-weight:normal;min-width:90px">排放源\月份</th>
              <th v-for="m in 12" :key="m"
                style="padding:4px 2px;text-align:center;color:var(--ems-text-muted);font-weight:normal;width:38px">
                {{ m }}月
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(progress, src) in summary.monthlyProgress" :key="src">
              <td style="padding:3px 10px;color:var(--ems-text-secondary)">{{ src }}</td>
              <td v-for="(status, idx) in progress" :key="idx" style="padding:3px 2px;text-align:center">
                <span :style="cellStyle(status)"
                  style="display:inline-block;width:26px;height:20px;border-radius:2px;line-height:20px;font-size:10px;cursor:pointer"
                  @click="status === 2 && handleUploadVoucherBySource(src, idx+1)">
                  {{ status === 1 ? '✓' : status === 2 ? '✗' : '—' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ─── 新增/编辑弹窗 ─── -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px" destroy-on-close
      @close="onEditDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="核查年度" prop="year">
            <el-date-picker v-model="form.year" type="year" value-format="YYYY" style="width:100%" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="月份" prop="month">
            <el-select v-model="form.month" style="width:100%">
              <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排放源" prop="emissionSource">
            <el-input v-model="form.emissionSource" placeholder="如：天然气" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排放类型" prop="emissionType">
            <el-select v-model="form.emissionType" style="width:100%">
              <el-option label="直接排放" value="DIRECT" />
              <el-option label="间接排放" value="INDIRECT" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="活动量" prop="activityValue">
            <el-input v-model="form.activityValue" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="计量单位" prop="unit">
            <el-input v-model="form.unit" placeholder="Nm³ / KWh / GJ" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排放因子" prop="emissionFactor">
            <el-input v-model="form.emissionFactor" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="因子来源">
            <el-input v-model="form.factorSource" placeholder="国家标准/实测" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="数据获取方式">
            <el-select v-model="form.dataMethod" style="width:100%">
              <el-option label="手工录入" value="手工录入" />
              <el-option label="Excel导入" value="Excel导入" />
              <el-option label="系统数据" value="系统数据" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="组织单元">
            <el-input v-model="form.orgUnit" />
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" />
          </el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- ─── 活动数据月度明细弹窗（完整版，含凭证状态列+汇总+按钮） ─── -->
    <el-dialog :title="detailTitle + ' — 活动数据明细（' + query.year + '年度）'"
      v-model="detailVisible" width="1000px">
      <el-table :data="detailData" border size="small"
        :row-class-name="({ row }) => row.voucherMissing ? 'row-warn' : ''">
        <el-table-column label="月份" align="center" width="70">
          <template #default="{ row }">{{ row.month }}月</template>
        </el-table-column>
        <el-table-column label="活动数据" prop="activityValue" align="center" width="110" />
        <el-table-column label="单位" prop="unit" align="center" width="70" />
        <el-table-column label="数据来源" prop="dataMethod" align="center" width="100" />
        <el-table-column label="排放因子" prop="emissionFactor" align="center" width="100" />
        <el-table-column label="因子来源" prop="factorSource" align="center" />
        <el-table-column label="月排放量(tCO₂e)" prop="emissionAmount" align="center" width="130" />
        <el-table-column label="凭证状态" align="center" width="90">
          <template #default="{ row }">
            <el-tag :type="row.voucherMissing ? 'danger' : 'success'" size="small">
              {{ row.voucherMissing ? '凭证缺失' : '已上传' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small"
              v-hasPermi="['carbonVerification:activity:edit']"
              @click="handleEditFromDetail(row)">编辑</el-button>
            <el-button link type="danger" size="small"
              v-hasPermi="['carbonVerification:activity:remove']"
              @click="handleDeleteFromDetail(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- 汇总行 -->
      <div style="display:flex;gap:24px;padding:10px 14px;background:#f5f7fa;border:1px solid #e4e7ed;border-radius:4px;margin-top:10px;font-size:12px;">
        <span>年度合计：<strong style="color:#409EFF">{{ detailAnnualValue }} {{ detailUnit }}</strong></span>
        <span>年度排放：<strong style="color:#409EFF">{{ detailAnnualEmission }} tCO₂e</strong></span>
        <span>凭证完整度：<strong :style="{color: detailVoucherMonths < 12 ? '#e6a23c' : '#67c23a'}">
          {{ detailVoucherMonths }}/{{ expectedMonths }}月
        </strong></span>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="info" @click="handleUploadVoucher({ emissionSource: detailTitle })">补充凭证</el-button>
        <el-button type="primary"
          v-hasPermi="['carbonVerification:activity:export']"
          @click="handleExportDetail">导出明细</el-button>
      </template>
    </el-dialog>

    <!-- ─── 实测因子弹窗 ─── -->
    <el-dialog :title="factorRow?.emissionSource + ' — 实测排放因子'" v-model="factorVisible" width="560px" destroy-on-close>
      <!-- 当前使用因子 -->
      <div class="factor-current">
        <div style="color:#606266;font-size:12px;margin-bottom:8px;font-weight:600">当前使用因子</div>
        <el-descriptions :column="2" size="small" border>
          <el-descriptions-item label="因子值">
            <strong style="color:#409EFF">{{ factorRow?.emissionFactor }} tCO₂e/{{ factorRow?.unit }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="因子类型">缺省因子</el-descriptions-item>
          <el-descriptions-item label="来源">{{ factorRow?.factorSource || '国家发改委指南（2015）' }}</el-descriptions-item>
          <el-descriptions-item label="有效期">{{ query.year }}年</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-alert title="若企业有实测热值数据，可上传检测报告并填写实测因子值，实测值优先级高于缺省值。"
        type="warning" show-icon :closable="false" style="margin:12px 0" />
      <el-form ref="factorFormRef" :model="factorForm" label-width="110px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="实测热值(MJ/单位)">
              <el-input v-model="factorForm.measuredValue" placeholder="如：38.5" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实测因子">
              <el-input v-model="factorForm.measuredFactor" placeholder="tCO₂e/单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实测周期">
              <el-select v-model="factorForm.measuredPeriod" style="width:100%">
                <el-option :label="query.year + 'Q1'" :value="query.year + 'Q1'" />
                <el-option :label="query.year + 'Q2'" :value="query.year + 'Q2'" />
                <el-option :label="query.year + '全年'" :value="query.year + '全年'" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检测机构">
              <el-input v-model="factorForm.laboratory" placeholder="检测机构名称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="上传检测报告">
              <el-upload :auto-upload="false" :limit="1" accept=".pdf"
                :on-change="f => factorForm.reportFile = f.raw">
                <el-button size="small">选择文件（PDF）</el-button>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="factorVisible = false">取消</el-button>
        <el-button type="primary" :loading="factorSaving" @click="handleSaveFactor">保存实测因子</el-button>
      </template>
    </el-dialog>

    <!-- ─── 行级上传凭证弹窗 ─── -->
    <el-dialog title="上传凭证" v-model="rowUploadVisible" width="560px" destroy-on-close>
      <el-form ref="rowUploadFormRef" :model="rowUploadForm" :rules="rowUploadRules" label-width="90px">
        <el-form-item label="排放源">
          <el-input :value="rowUploadForm.emissionSource" disabled />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="凭证名称" prop="voucherName">
              <el-input v-model="rowUploadForm.voucherName" placeholder="请输入凭证名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="凭证类型" prop="voucherType">
              <el-select v-model="rowUploadForm.voucherType" style="width:100%">
                <el-option label="计量凭证/抄表单" value="METER" />
                <el-option label="检测检验报告" value="DETECTION" />
                <el-option label="购进凭证/发票" value="PURCHASE_INVOICE" />
                <el-option label="销售凭证/发票" value="SALE_INVOICE" />
                <el-option label="其他凭证" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="凭证月份" prop="month">
              <el-select v-model="rowUploadForm.month" style="width:100%">
                <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="上传文件" prop="file">
          <el-upload :auto-upload="false" :limit="1" drag accept=".pdf,.jpg,.jpeg,.png"
            :on-change="f => rowUploadForm.file = f.raw">
            <el-icon style="font-size:24px"><upload-filled /></el-icon>
            <div style="font-size:12px;margin-top:4px">PDF / JPG / PNG，≤ 20MB</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rowUploadVisible = false">取消</el-button>
        <el-button type="primary" :loading="rowUploading" @click="submitRowUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CarbonVerificationActivity">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import {
  getActivitySourceList, getActivitySummary, getActivityDetail,
  addActivity, editActivity, deleteActivity, exportActivity, updateActivityFactor
} from '@/api/carbonVerification/activity'
import { uploadVoucher, previewVoucherUrl } from '@/api/carbonVerification/voucher'

const loading = ref(false)
const sourceList = ref([])
const summary = ref(null)

const query = reactive({
  orgUnit: '全厂',
  year: String(new Date().getFullYear())
})

const expectedMonths = computed(() => {
  const y = Number(query.year)
  const now = new Date()
  return y < now.getFullYear() ? 12 : now.getMonth() + 1
})

// ── 新增/编辑弹窗 ──
const dialogVisible = ref(false)
const dialogTitle = ref('新增活动数据')
const formRef = ref(null)
const defaultForm = () => ({
  id: null, year: String(new Date().getFullYear()), month: null,
  orgUnit: '全厂', emissionSource: '', emissionType: 'DIRECT',
  dataMethod: '手工录入', unit: '', activityValue: '', emissionFactor: '',
  factorSource: '国家缺省值', remark: ''
})
const form = reactive(defaultForm())
const rules = {
  year:           [{ required: true, message: '请选择年度', trigger: 'change' }],
  month:          [{ required: true, message: '请选择月份', trigger: 'change' }],
  emissionSource: [{ required: true, message: '请输入排放源', trigger: 'blur' }],
  emissionType:   [{ required: true, message: '请选择排放类型', trigger: 'change' }],
  activityValue:  [{ required: true, message: '请输入活动量', trigger: 'blur' }],
  unit:           [{ required: true, message: '请输入单位', trigger: 'blur' }],
  emissionFactor: [{ required: true, message: '请输入排放因子', trigger: 'blur' }],
}

// ── 月度明细弹窗 ──
const detailVisible = ref(false)
const detailTitle = ref('')
const detailSource = ref('')   // 当前打开的排放源，用于编辑/删除后刷新
const detailData = ref([])
const detailUnit = ref('')
const editFromDetail = ref(false)  // 标记是否从明细弹窗触发的编辑
const detailAnnualValue = computed(() =>
  detailData.value.reduce((s, r) => s + (Number(r.activityValue) || 0), 0).toFixed(2))
const detailAnnualEmission = computed(() =>
  detailData.value.reduce((s, r) => s + (Number(r.emissionAmount) || 0), 0).toFixed(4))
const detailVoucherMonths = computed(() =>
  detailData.value.filter(r => !r.voucherMissing).length)

// ── 实测因子弹窗 ──
const factorVisible = ref(false)
const factorSaving = ref(false)
const factorRow = ref(null)
const factorFormRef = ref(null)
const factorForm = reactive({ measuredValue: '', measuredFactor: '', measuredPeriod: '', laboratory: '', reportFile: null })

// ── 行级上传凭证弹窗 ──
const rowUploadVisible = ref(false)
const rowUploading = ref(false)
const rowUploadFormRef = ref(null)
const rowUploadForm = reactive({ emissionSource: '', voucherName: '', voucherType: 'METER', month: null, file: null })
const rowUploadRules = {
  voucherName: [{ required: true, message: '请输入凭证名称', trigger: 'blur' }],
  voucherType: [{ required: true, message: '请选择凭证类型', trigger: 'change' }],
  month:       [{ required: true, message: '请选择月份',     trigger: 'change' }],
}

function voucherDotClass(s) {
  if (s === 'COMPLETE') return 'status-complete'
  if (s === 'PARTIAL')  return 'status-partial'
  return 'status-missing'
}
function voucherTagType(s) {
  if (s === 'COMPLETE') return 'success'
  if (s === 'PARTIAL')  return 'warning'
  return 'danger'
}
function voucherTagLabel(s) {
  if (s === 'COMPLETE') return '已完整'
  if (s === 'PARTIAL')  return '部分上传'
  return '未上传'
}
function cellStyle(status) {
  if (status === 1) return 'background:rgba(46,125,50,.85);color:#fff'
  if (status === 2) return 'background:rgba(180,30,30,.6);color:#ffa0a0'
  return 'background:rgba(255,255,255,.04);color:var(--ems-text-muted)'
}

async function loadData() {
  loading.value = true
  try {
    const year = Number(query.year)
    const [srcRes, sumRes] = await Promise.all([
      getActivitySourceList(year, query.orgUnit),
      getActivitySummary(year, query.orgUnit)
    ])
    sourceList.value = srcRes.data || []
    summary.value = sumRes.data
  } finally { loading.value = false }
}

function handleSearch() { loadData() }
function handleReset() { query.orgUnit = '全厂'; loadData() }

function handleAdd() {
  editFromDetail.value = false   // 确保新增时不触发明细回跳
  Object.assign(form, defaultForm()); form.year = query.year
  dialogTitle.value = '新增活动数据'; dialogVisible.value = true
}
async function handleSubmit() {
  await formRef.value.validate()
  const payload = { ...form, year: Number(form.year),
    activityValue: Number(form.activityValue) || undefined,
    emissionFactor: Number(form.emissionFactor) || undefined }
  if (form.id) { await editActivity(payload); ElMessage.success('修改成功') }
  else { await addActivity(payload); ElMessage.success('新增成功') }
  const fromDetail = editFromDetail.value
  editFromDetail.value = false   // 先重置，再关闭弹窗，防止 @close 钩子误触发
  dialogVisible.value = false
  await loadData()               // 必须 await，确保 summary 更新后 refreshDetail 拿到最新凭证状态
  if (fromDetail) {
    await refreshDetail()
    detailVisible.value = true
  }
}
function onEditDialogClose() {
  // 用户取消编辑（点取消或 X）时：若来自明细弹窗，重新打开明细
  if (editFromDetail.value) {
    editFromDetail.value = false
    detailVisible.value = true
  }
}

async function showDetail(row) {
  detailTitle.value = row.emissionSource
  detailSource.value = row.emissionSource
  detailUnit.value = row.unit || ''
  // 注意：API 签名为 (year, emissionSource, orgUnit)
  const res = await getActivityDetail(Number(query.year), row.emissionSource, query.orgUnit)
  const items = res.data || []
  const uploaded = summary.value?.monthlyProgress?.[row.emissionSource] || []
  detailData.value = items.map(a => ({
    ...a,
    voucherMissing: a.month <= expectedMonths.value && uploaded[a.month - 1] !== 1
  }))
  detailVisible.value = true
}

async function refreshDetail() {
  const res = await getActivityDetail(Number(query.year), detailSource.value, query.orgUnit)
  const items = res.data || []
  const uploaded = summary.value?.monthlyProgress?.[detailSource.value] || []
  detailData.value = items.map(a => ({
    ...a,
    voucherMissing: a.month <= expectedMonths.value && uploaded[a.month - 1] !== 1
  }))
}

function handleEditFromDetail(row) {
  // 将明细行数据填入新增/编辑表单并打开弹窗
  Object.assign(form, {
    id: row.id,
    year: String(row.year),
    month: row.month,
    orgUnit: row.orgUnit || query.orgUnit,
    emissionSource: row.emissionSource,
    emissionType: row.emissionType,
    dataMethod: row.dataMethod,
    unit: row.unit,
    activityValue: row.activityValue != null ? String(row.activityValue) : '',
    emissionFactor: row.emissionFactor != null ? String(row.emissionFactor) : '',
    factorSource: row.factorSource || '',
    remark: row.remark || ''
  })
  dialogTitle.value = '编辑活动数据'
  editFromDetail.value = true
  detailVisible.value = false
  dialogVisible.value = true
}

async function handleDeleteFromDetail(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除 ${row.emissionSource} ${row.month}月 的活动数据？此操作不可恢复。`,
      '删除确认', { type: 'warning' }
    )
    await deleteActivity(row.id)
    ElMessage.success('删除成功')
    // 从明细列表中移除该行，同时刷新主表
    detailData.value = detailData.value.filter(r => r.id !== row.id)
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') ElMessage.error('删除失败：' + (e?.message || e))
  }
}

async function handleExportDetail() {
  try {
    const res = await exportActivity(Number(query.year), query.orgUnit)
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = query.year + '年活动数据明细.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch (e) { ElMessage.error('导出失败') }
}

function showFactor(row) {
  factorRow.value = row
  Object.assign(factorForm, { measuredValue: '', measuredFactor: row.emissionFactor || '',
    measuredPeriod: query.year + '全年', laboratory: '', reportFile: null })
  factorVisible.value = true
}

async function handleSaveFactor() {
  if (!factorForm.measuredFactor) { ElMessage.warning('请填写实测因子值'); return }
  factorSaving.value = true
  try {
    // 1. 持久化因子值：批量更新该排放源全年各月的 emission_factor 并重算排放量
    await updateActivityFactor({
      year: Number(query.year),
      orgUnit: query.orgUnit,
      emissionSource: factorRow.value.emissionSource,
      emissionFactor: Number(factorForm.measuredFactor),
      factorSource: factorForm.laboratory
        ? factorForm.laboratory + '实测（' + factorForm.measuredPeriod + '）'
        : '实测值'
    })
    // 2. 若有检测报告文件，同步上传为 DETECTION 类型凭证
    if (factorForm.reportFile) {
      const fd = new FormData()
      fd.append('file', factorForm.reportFile)
      fd.append('year', Number(query.year))
      fd.append('month', 12)
      fd.append('orgUnit', query.orgUnit)
      fd.append('emissionSource', factorRow.value.emissionSource)
      fd.append('voucherType', 'DETECTION')
      fd.append('voucherName', (factorForm.laboratory || '实测') + '检测报告_' + factorForm.measuredPeriod)
      await uploadVoucher(fd)
    }
    ElMessage.success('实测因子已保存，排放量已重新计算')
    factorVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('保存失败：' + e.message) }
  finally { factorSaving.value = false }
}

function handleUploadVoucher(row) {
  Object.assign(rowUploadForm, {
    emissionSource: row.emissionSource, voucherName: '', voucherType: 'METER', month: null, file: null
  })
  rowUploadVisible.value = true
}
function handleUploadVoucherBySource(src, month) {
  Object.assign(rowUploadForm, { emissionSource: src, voucherName: '', voucherType: 'METER', month, file: null })
  rowUploadVisible.value = true
}
async function submitRowUpload() {
  await rowUploadFormRef.value.validate()
  if (!rowUploadForm.file) { ElMessage.warning('请选择要上传的文件'); return }
  rowUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', rowUploadForm.file)
    fd.append('year', Number(query.year))
    fd.append('month', rowUploadForm.month)
    fd.append('orgUnit', query.orgUnit)
    fd.append('emissionSource', rowUploadForm.emissionSource)
    fd.append('voucherType', rowUploadForm.voucherType)
    fd.append('voucherName', rowUploadForm.voucherName)
    await uploadVoucher(fd)
    ElMessage.success('上传成功')
    rowUploadVisible.value = false
    loadData()
  } catch (e) { ElMessage.error('上传失败：' + e.message) }
  finally { rowUploading.value = false }
}

async function handleImport(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('year', Number(query.year))
  formData.append('orgUnit', query.orgUnit)
  try {
    const res = await fetch('/dev-api/carbonVerification/activity/importData', {
      method: 'POST', body: formData,
      headers: { Authorization: `Bearer ${localStorage.getItem('token') || ''}` }
    })
    const json = await res.json()
    ElMessage.success(json.msg || '导入成功')
    loadData()
  } catch (e) { ElMessage.error('导入失败') }
  return false
}

async function handleExport() {
  try {
    const res = await exportActivity(Number(query.year), query.orgUnit)
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a'); a.href = url
    a.download = query.year + '年活动数据.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch (e) { ElMessage.error('导出失败') }
}

onMounted(loadData)
</script>

<style scoped>
/* ─── FILTER BAR ─── */
.filter-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 0; border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px; flex-wrap: wrap;
}
.filter-label { font-size: 12px; color: var(--ems-text-secondary); white-space: nowrap; }
.filter-divider { width: 1px; height: 20px; background: var(--ems-border-dim); margin: 0 2px; }
.btn-search {
  background: linear-gradient(135deg, #0055bb, #0099ee) !important;
  border-color: transparent !important;
  color: #fff !important;
}
.btn-search:hover { background: linear-gradient(135deg, #0066cc, #00aaff) !important; }
.btn-icon { width: 14px; height: 14px; margin-right: 4px; vertical-align: middle; }
.btn-reset {
  background: rgba(255,255,255,.04) !important;
  border-color: var(--ems-border-mid) !important;
  color: var(--ems-text-secondary) !important;
}
.btn-reset:hover { background: rgba(255,255,255,.08) !important; color: var(--ems-text-primary) !important; }
.btn-action {
  background: rgba(255,255,255,.04) !important;
  border-color: var(--ems-border-mid) !important;
  color: var(--ems-text-secondary) !important;
}
.btn-action:hover { background: rgba(255,255,255,.08) !important; color: var(--ems-text-primary) !important; }
.btn-action-green {
  background: rgba(0,180,100,.08) !important;
  border-color: rgba(0,180,100,.3) !important;
  color: var(--ems-accent-green) !important;
}
.btn-action-green:hover { background: rgba(0,180,100,.14) !important; }
.btn-export {
  background: rgba(255,255,255,.04) !important;
  border-color: var(--ems-border-mid) !important;
  color: var(--ems-text-secondary) !important;
}
.btn-export:hover { background: rgba(255,255,255,.08) !important; color: var(--ems-text-primary) !important; }

/* ─── SUMMARY STRIP ─── */
.summary-strip {
  display: flex; gap: 12px; padding: 12px 0;
  border-bottom: 1px solid var(--ems-border-dim);
  margin-bottom: 14px; flex-wrap: wrap;
}
.scard {
  flex: 1; min-width: 150px;
  background: var(--ems-bg-card); border: 1px solid var(--ems-border-dim);
  border-radius: 8px; padding: 12px 16px;
  position: relative; overflow: hidden;
  display: flex; flex-direction: column; gap: 3px;
}
.scard::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 2px;
}
.scard.blue::before   { background: linear-gradient(90deg, transparent, var(--ems-accent), transparent); }
.scard.green::before  { background: linear-gradient(90deg, transparent, var(--ems-accent-green), transparent); }
.scard.orange::before { background: linear-gradient(90deg, transparent, var(--ems-accent-orange), transparent); }
.scard.red::before    { background: linear-gradient(90deg, transparent, var(--ems-accent-red), transparent); }
.scard-label { font-size: 11px; color: var(--ems-text-muted); letter-spacing: .5px; }
.scard-value { font-size: 22px; font-weight: 700; line-height: 1.1; color: var(--ems-text-primary); }
.scard.blue   .scard-value { color: var(--ems-accent-bright); }
.scard.green  .scard-value { color: var(--ems-accent-green); }
.scard.orange .scard-value { color: var(--ems-accent-orange); }
.scard.red    .scard-value { color: var(--ems-accent-red); }
.scard-unit { font-size: 11px; color: var(--ems-text-secondary); }
.scard-unit-inline { font-size: 13px; font-weight: 400; color: var(--ems-text-secondary); margin-left: 2px; }
.scard-sub  { font-size: 11px; color: var(--ems-text-muted); margin-top: 1px; }

/* ─── NOTICE BAR ─── */
.notice-bar {
  display: flex; align-items: center; gap: 8px;
  background: rgba(255,146,64,.06); border: 1px solid rgba(255,146,64,.2);
  border-radius: 6px; padding: 8px 14px; margin-bottom: 14px;
  font-size: 12px; color: var(--ems-text-secondary);
}
.notice-bar strong { color: var(--ems-accent-orange); }
.notice-icon { color: var(--ems-accent-orange); font-size: 14px; }

/* ─── CHART CARD ─── */
.chart-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px; overflow: hidden;
}
.chart-card-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 16px; font-size: 11.5px; font-weight: 600;
  color: var(--ems-text-secondary); border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.chart-head-sub { font-size: 11px; color: var(--ems-text-muted); font-weight: 400; }
.chart-legend { display: flex; gap: 14px; font-size: 11px; color: var(--ems-text-muted); font-weight: 400; }
.legend-item { display: flex; align-items: center; gap: 5px; }
.legend-item::before { content: ''; display: inline-block; width: 12px; height: 12px; border-radius: 2px; }
.legend-item.uploaded::before { background: rgba(46,125,50,.85); }
.legend-item.missing::before  { background: rgba(180,30,30,.6); }
.legend-item.pending::before  { background: rgba(255,255,255,.06); border: 1px solid var(--ems-border-dim); }

/* ─── TABLE ─── */
.td-value { font-size: 13px; font-weight: 600; color: var(--ems-accent-bright); }

/* ─── TYPE BADGE ─── */
.type-badge {
  display: inline-block; padding: 1px 8px; border-radius: 10px; font-size: 11px;
}
.badge-blue  { background: rgba(0,140,255,.12); color: var(--ems-accent-bright); border: 1px solid rgba(0,140,255,.25); }
.badge-green { background: rgba(0,180,100,.1);  color: var(--ems-accent-green);  border: 1px solid rgba(0,180,100,.25); }

/* ─── STATUS DOT ─── */
.status-dot {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 11.5px; color: var(--ems-text-primary);
}
.status-dot::before { content: ''; width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.status-complete::before { background: var(--ems-accent-green);  box-shadow: 0 0 5px var(--ems-accent-green); }
.status-partial::before  { background: var(--ems-accent-yellow); box-shadow: 0 0 5px var(--ems-accent-yellow); }
.status-missing::before  { background: var(--ems-accent-red);    box-shadow: 0 0 5px var(--ems-accent-red); }

/* ─── DIALOG INTERNALS ─── */
.factor-current {
  background: var(--ems-bg-card); border: 1px solid var(--ems-border-dim);
  border-radius: 4px; padding: 14px; margin-bottom: 12px;
}
:deep(.row-warn) td { background: rgba(230, 162, 60, 0.08) !important; }
</style>
