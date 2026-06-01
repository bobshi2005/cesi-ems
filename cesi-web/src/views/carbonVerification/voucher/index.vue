<template>
  <div class="app-container">
    <!-- 筛选栏 -->
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="年度">
        <el-date-picker v-model="query.year" type="year" value-format="YYYY" style="width:110px" />
      </el-form-item>
      <el-form-item label="月份">
        <el-select v-model="query.month" clearable placeholder="全部" style="width:90px">
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="凭证类型">
        <el-select v-model="query.voucherType" clearable placeholder="全部类型" style="width:150px">
          <el-option v-for="t in voucherTypes" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="排放源">
        <el-input v-model="query.emissionSource" clearable placeholder="排放源" style="width:100px" />
      </el-form-item>
      <el-form-item label="凭证名称">
        <el-input v-model="query.voucherName" clearable placeholder="凭证名称" style="width:130px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">查询</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
      <el-form-item style="float:right">
        <el-button type="primary" icon="Upload"
          v-hasPermi="['carbonVerification:voucher:upload']"
          @click="uploadDialogVisible = true">上传凭证</el-button>
        <el-button icon="Download"
          v-hasPermi="['carbonVerification:voucher:download']"
          :disabled="!selectedIds.length"
          @click="handleBatchDownload">批量下载</el-button>
        <el-button icon="Document"
          v-hasPermi="['carbonVerification:voucher:export']"
          @click="handleExport">导出清单</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading" size="small"
      @selection-change="rows => selectedIds = rows.map(r => r.id)">
      <el-table-column type="selection" width="45" align="center" />
      <el-table-column label="序号" type="index" width="55" align="center" />
      <el-table-column label="凭证月份" align="center" width="100">
        <template #default="{ row }">{{ row.year }}年{{ row.month }}月</template>
      </el-table-column>
      <el-table-column label="排放源" prop="emissionSource" align="center" width="100" />
      <el-table-column label="凭证类型" prop="voucherType" align="center" width="150">
        <template #default="{ row }">
          <span class="type-badge" :class="voucherTypeBadgeClass(row.voucherType)">
            {{ voucherTypeLabel(row.voucherType) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="凭证名称" prop="voucherName" min-width="160">
        <template #default="{ row }">
          <span class="td-value">{{ row.voucherName }}</span>
        </template>
      </el-table-column>
      <el-table-column label="格式" prop="fileFormat" align="center" width="70" />
      <el-table-column label="上传时间" prop="uploadTime" align="center" width="140" />
      <el-table-column label="上传人" prop="uploadBy" align="center" width="90" />
      <el-table-column label="操作" align="center" width="190" fixed="right">
        <template #default="{ row }">
          <span class="action-btn action-view"
            v-hasPermi="['carbonVerification:voucher:list']"
            @click="handlePreview(row)">查看</span>
          <span class="action-btn action-dl"
            v-hasPermi="['carbonVerification:voucher:download']"
            @click="handleDownload(row)">下载</span>
          <span class="action-btn action-del"
            v-hasPermi="['carbonVerification:voucher:remove']"
            @click="handleDelete(row)">删除</span>
        </template>
      </el-table-column>
    </el-table>

    <EmsPageBar
      v-model:page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      @change="loadData"
    />

    <!-- 上传凭证弹窗 -->
    <el-dialog title="上传凭证" v-model="uploadDialogVisible" width="640px" destroy-on-close>
      <el-form ref="uploadFormRef" :model="uploadForm" :rules="uploadRules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="凭证名称" prop="voucherName">
              <el-input v-model="uploadForm.voucherName" placeholder="请输入凭证名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="凭证类型" prop="voucherType">
              <el-select v-model="uploadForm.voucherType" placeholder="请选择" style="width:100%">
                <el-option v-for="t in voucherTypes" :key="t.value" :label="t.label" :value="t.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排放源" prop="emissionSource">
              <el-input v-model="uploadForm.emissionSource" placeholder="如：天然气"
                @blur="loadExistingVouchers" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="凭证月份" prop="month">
              <el-select v-model="uploadForm.month" placeholder="选择月份" style="width:100%"
                @change="loadExistingVouchers">
                <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="uploadForm.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="上传文件" prop="file">
              <el-upload ref="uploadRef" :auto-upload="false" :limit="1" drag
                accept=".pdf,.jpg,.jpeg,.png"
                :on-change="file => uploadForm.file = file.raw"
                :on-exceed="() => ElMessage.warning('每次只能上传1个文件')">
                <el-icon style="font-size:28px;margin-bottom:8px"><upload-filled /></el-icon>
                <div>拖拽文件到此处，或 <em>点击上传</em></div>
                <template #tip>
                  <div style="color:#909399;font-size:12px;margin-top:4px">
                    支持 PDF、JPG、PNG 格式，单文件 ≤ 20MB
                  </div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
          <!-- 已有凭证列表 -->
          <el-col :span="24" v-if="existingVouchers.length > 0">
            <el-divider content-position="left" style="font-size:12px">
              该排放源该月已有凭证（{{ existingVouchers.length }} 条）
            </el-divider>
            <el-table :data="existingVouchers" size="small" border>
              <el-table-column label="凭证名称" prop="voucherName" />
              <el-table-column label="类型" prop="voucherType" align="center" width="130">
                <template #default="{ row }">{{ voucherTypeLabel(row.voucherType) }}</template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="100">
                <template #default="{ row }">
                  <el-button link type="danger" size="small" @click="deleteExisting(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUploadSubmit">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CarbonVerificationVoucher">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getVoucherPage, uploadVoucher, downloadVoucher, batchDownloadVoucher, deleteVoucher, exportVoucherList, getVoucherBySourceMonth, previewVoucherUrl } from '@/api/carbonVerification/voucher'

const voucherTypes = [
  { value: 'METER',           label: '计量凭证/抄表单' },
  { value: 'DETECTION',       label: '检测检验报告' },
  { value: 'SALE_INVOICE',    label: '销售凭证/发票' },
  { value: 'PURCHASE_INVOICE',label: '购进凭证/发票' },
  { value: 'OTHER',           label: '其他凭证' }
]
const voucherTypeLabel = v => voucherTypes.find(t => t.value === v)?.label || v
function voucherTypeBadgeClass(v) {
  return {
    METER:            'badge-blue',
    DETECTION:        'badge-purple',
    SALE_INVOICE:     'badge-green',
    PURCHASE_INVOICE: 'badge-orange',
    OTHER:            'badge-gray',
  }[v] || 'badge-gray'
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedIds = ref([])

const query = reactive({
  year: String(new Date().getFullYear()), month: null,
  orgUnit: '全厂', emissionSource: '', voucherType: '', voucherName: '',
  pageNum: 1, pageSize: 20
})

const uploadDialogVisible = ref(false)
const uploading = ref(false)
const uploadFormRef = ref(null)
const uploadRef = ref(null)
const existingVouchers = ref([])
const uploadForm = reactive({
  voucherName: '', voucherType: '', emissionSource: '',
  month: null, remark: '', file: null
})
const uploadRules = {
  voucherName:    [{ required: true, message: '请输入凭证名称', trigger: 'blur' }],
  voucherType:    [{ required: true, message: '请选择凭证类型', trigger: 'change' }],
  emissionSource: [{ required: true, message: '请输入排放源',   trigger: 'blur' }],
  month:          [{ required: true, message: '请选择月份',     trigger: 'change' }],
}

async function loadData() {
  loading.value = true
  try {
    const res = await getVoucherPage({
      year: Number(query.year),
      month: query.month || undefined,
      orgUnit: query.orgUnit,
      emissionSource: query.emissionSource || undefined,
      voucherType: query.voucherType || undefined,
      voucherName: query.voucherName || undefined,
      pageNum: query.pageNum, pageSize: query.pageSize
    })
    tableData.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

function handleSearch() { query.pageNum = 1; loadData() }
function handleReset() {
  Object.assign(query, { month: null, emissionSource: '', voucherType: '', voucherName: '', pageNum: 1 })
  loadData()
}

async function loadExistingVouchers() {
  if (!uploadForm.emissionSource || !uploadForm.month) { existingVouchers.value = []; return }
  const res = await getVoucherBySourceMonth(Number(query.year), uploadForm.month, uploadForm.emissionSource, query.orgUnit)
  existingVouchers.value = res.rows || []
}
async function deleteExisting(row) {
  await deleteVoucher(row.id)
  ElMessage.success('删除成功')
  loadExistingVouchers()
  loadData()
}

async function handleUploadSubmit() {
  await uploadFormRef.value.validate()
  if (!uploadForm.file) { ElMessage.warning('请选择要上传的文件'); return }
  uploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', uploadForm.file)
    fd.append('year', Number(query.year))
    fd.append('month', uploadForm.month)
    fd.append('orgUnit', query.orgUnit)
    fd.append('emissionSource', uploadForm.emissionSource)
    fd.append('voucherType', uploadForm.voucherType)
    fd.append('voucherName', uploadForm.voucherName)
    if (uploadForm.remark) fd.append('remark', uploadForm.remark)
    await uploadVoucher(fd)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    existingVouchers.value = []
    loadData()
  } catch (e) { ElMessage.error('上传失败：' + e.message) }
  finally { uploading.value = false }
}

function handlePreview(row) {
  const url = previewVoucherUrl(row.id)
  window.open(url, '_blank')
}

async function handleDownload(row) {
  try {
    const res = await downloadVoucher(row.id)
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a')
    a.href = url; a.download = row.voucherName + '.' + row.fileFormat.toLowerCase(); a.click()
    URL.revokeObjectURL(url)
  } catch (e) { ElMessage.error('下载失败') }
}

async function handleBatchDownload() {
  try {
    const res = await batchDownloadVoucher(selectedIds.value)
    const url = URL.createObjectURL(new Blob([res], { type: 'application/zip' }))
    const a = document.createElement('a'); a.href = url; a.download = '凭证批量下载.zip'; a.click()
    URL.revokeObjectURL(url)
  } catch (e) { ElMessage.error('批量下载失败') }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除凭证「${row.voucherName}」？`, '提示', { type: 'warning' })
  await deleteVoucher(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleExport() {
  try {
    const res = await exportVoucherList(Number(query.year), query.orgUnit)
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a'); a.href = url; a.download = query.year + '年凭证清单.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch (e) { ElMessage.error('导出失败') }
}

onMounted(loadData)
</script>

<style scoped>
/* ─── TABLE VALUES ─── */
.td-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--ems-text-primary);
}

/* ─── TYPE BADGE ─── */
.type-badge {
  display: inline-block;
  padding: 1px 9px;
  border-radius: 10px;
  font-size: 11px;
  border: 1px solid;
  white-space: nowrap;
}
.badge-blue   { background: rgba(0,140,255,.1);   color: var(--ems-accent-bright);  border-color: rgba(0,140,255,.25); }
.badge-purple { background: rgba(140,80,240,.1);  color: #bb88ff;                   border-color: rgba(140,80,240,.25); }
.badge-green  { background: rgba(0,180,100,.1);   color: var(--ems-accent-green);   border-color: rgba(0,180,100,.25); }
.badge-orange { background: rgba(255,146,64,.1);  color: var(--ems-accent-orange);  border-color: rgba(255,146,64,.25); }
.badge-gray   { background: rgba(255,255,255,.06); color: var(--ems-text-muted);    border-color: var(--ems-border-dim); }

/* ─── ACTION BUTTONS ─── */
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
.action-dl   { background: rgba(0,140,255,.1);  border-color: rgba(0,140,255,.3);  color: #66aaff; }
.action-dl:hover   { background: rgba(0,140,255,.22); border-color: rgba(0,140,255,.55); }
.action-del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-del:hover  { background: rgba(255,77,106,.18); }
</style>
