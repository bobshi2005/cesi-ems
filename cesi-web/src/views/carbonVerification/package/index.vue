<template>
  <div class="app-container">
    <!-- 提示栏 -->
    <div class="notice-bar">
      <svg class="notice-icon" viewBox="0 0 16 16" width="14" height="14" fill="none" stroke="currentColor" stroke-width="1.8">
        <circle cx="8" cy="8" r="6.5"/><line x1="8" y1="5" x2="8" y2="8.5"/><circle cx="8" cy="11" r=".5" fill="currentColor"/>
      </svg>
      <span><strong>核查材料打包：</strong>系统将自动收集活动数据表、排放因子来源文件、原始凭证及排放报告，打包为符合核查机构要求的材料包。</span>
    </div>

    <el-row :gutter="16">
      <!-- 左侧配置面板 -->
      <el-col :span="7">
        <div class="config-panel">
          <div class="panel-head" style="margin-bottom:14px">打包配置</div>
          <el-form :model="config" label-width="90px" size="small">
            <el-form-item label="核查年度">
              <el-date-picker v-model="config.packageYear" type="year" value-format="YYYY" style="width:100%" />
            </el-form-item>
            <el-form-item label="核查机构">
              <el-select v-model="config.verifyOrg" style="width:100%">
                <el-option label="中国质量认证中心" value="中国质量认证中心" />
                <el-option label="北京中创碳投" value="北京中创碳投" />
                <el-option label="广州赛宝认证" value="广州赛宝认证" />
                <el-option label="其他机构" value="其他机构" />
              </el-select>
            </el-form-item>
          </el-form>
          <div class="divider-line"></div>
          <div class="form-label-text">选择包含材料：</div>
          <div class="include-list">
            <label v-for="item in includeItems" :key="item.key" class="include-item">
              <el-checkbox v-model="config[item.key]" true-label="1" false-label="0" />
              <span class="include-label">{{ item.label }}</span>
              <span class="stage-badge" :class="'badge-' + item.status(completeness)">
                {{ itemStatusLabel(item.status(completeness)) }}
              </span>
            </label>
          </div>
          <div class="divider-line"></div>
          <el-button type="success" style="width:100%"
            v-hasPermi="['carbonVerification:package:generate']"
            :loading="packing" @click="handleGenerate">
            生成材料包
          </el-button>
        </div>
      </el-col>

      <!-- 右侧内容 -->
      <el-col :span="17">
        <!-- 材料完整度评估 -->
        <div class="section-card" style="margin-bottom:16px">
          <div class="card-head">
            <span>材料完整度评估</span>
            <span v-if="completeness" class="stage-badge"
              :class="completeness.overallRate >= 80 ? 'badge-complete' : completeness.overallRate > 0 ? 'badge-partial' : 'badge-missing'">
              整体完整度 {{ completeness.overallRate }}%
            </span>
            <span v-else class="stage-badge badge-pending">加载中</span>
          </div>
          <!-- 进度行：原始凭证 -->
          <div class="stage-row">
            <div class="stage-idx">1</div>
            <div class="stage-name">原始凭证</div>
            <div class="stage-bar-wrap">
              <div class="stage-bar" :style="{ width: voucherPct + '%' }"></div>
            </div>
            <div class="stage-value">{{ completeness ? completeness.voucherUploadedMonths : '—' }}</div>
            <div class="stage-pct">/ 12 月</div>
            <span class="stage-badge"
              :class="voucherPct >= 100 ? 'badge-complete' : voucherPct > 0 ? 'badge-partial' : 'badge-missing'">
              {{ voucherPct >= 100 ? '完整' : voucherPct > 0 ? '部分' : '缺失' }}
            </span>
          </div>
          <!-- 进度行：活动数据 -->
          <div class="stage-row">
            <div class="stage-idx">2</div>
            <div class="stage-name">活动数据</div>
            <div class="stage-bar-wrap">
              <div class="stage-bar stage-bar--green" :style="{ width: activityPct + '%' }"></div>
            </div>
            <div class="stage-value">{{ completeness ? completeness.activityDataCount : '—' }}</div>
            <div class="stage-pct">/ {{ completeness ? completeness.activityDataTotal : '—' }} 条</div>
            <span class="stage-badge"
              :class="activityPct >= 100 ? 'badge-complete' : activityPct > 0 ? 'badge-partial' : 'badge-missing'">
              {{ activityPct >= 100 ? '完整' : activityPct > 0 ? '部分' : '缺失' }}
            </span>
          </div>
          <!-- 进度行：排放因子 -->
          <div class="stage-row" style="border-bottom:none">
            <div class="stage-idx">3</div>
            <div class="stage-name">排放因子</div>
            <div class="stage-bar-wrap">
              <div class="stage-bar stage-bar--green" :style="{ width: factorPct + '%' }"></div>
            </div>
            <div class="stage-value">{{ completeness ? (completeness.factorReady ? '已就绪' : '待配置') : '—' }}</div>
            <div class="stage-pct"></div>
            <span class="stage-badge" :class="factorPct === 100 ? 'badge-complete' : 'badge-missing'">
              {{ factorPct === 100 ? '就绪' : '待配置' }}
            </span>
          </div>
          <div v-if="completeness && completeness.missingDesc" class="card-warn">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.6" style="width:13px;height:13px;flex-shrink:0">
              <path d="M8 2L14.5 13H1.5Z"/><line x1="8" y1="6.5" x2="8" y2="9.5"/><circle cx="8" cy="11.5" r=".5" fill="currentColor"/>
            </svg>
            {{ completeness.missingDesc }}
          </div>
        </div>

        <!-- 历史打包记录 -->
        <div class="section-card">
          <div class="panel-toolbar">
            <span class="panel-head" style="margin:0">历史打包记录</span>
            <span class="toolbar-sub">共 {{ total }} 条</span>
          </div>
          <el-table :data="tableData" border stripe v-loading="loading" size="small">
            <el-table-column label="包名称" prop="packageName" min-width="180">
              <template #default="{ row }">
                <span class="td-value">{{ row.packageName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="年度" prop="packageYear" align="center" width="70" />
            <el-table-column label="核查机构" prop="verifyOrg" align="center" min-width="120" />
            <el-table-column label="打包时间" prop="createTime" align="center" width="140" />
            <el-table-column label="文件数" prop="fileCount" align="center" width="70" />
            <el-table-column label="大小" align="center" width="90">
              <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="90">
              <template #default="{ row }">
                <span class="stage-badge"
                  :class="row.status === '1' ? 'badge-complete' : row.status === '2' ? 'badge-missing' : 'badge-pending'">
                  {{ row.status === '1' ? '完成' : row.status === '2' ? '失败' : '生成中' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="170" fixed="right">
              <template #default="{ row }">
                <span class="action-btn action-view"
                  :class="{ 'action-disabled': row.status !== '1' }"
                  @click="row.status === '1' && handleViewDetail(row)">查看</span>
                <span class="action-btn action-dl"
                  v-hasPermi="['carbonVerification:package:download']"
                  :class="{ 'action-disabled': row.status !== '1' }"
                  @click="row.status === '1' && handleDownload(row)">下载</span>
                <span class="action-btn action-del"
                  v-hasPermi="['carbonVerification:package:remove']"
                  @click="handleDelete(row)">删除</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <EmsPageBar
          :total="total"
          v-model:page="query.pageNum"
          v-model:page-size="query.pageSize"
          @change="loadList"
        />
      </el-col>
    </el-row>

    <!-- 打包进度弹窗 -->
    <el-dialog title="生成核查材料包" v-model="packProgressVisible" width="500px" :close-on-click-modal="false">
      <div class="timeline">
        <div v-for="(step, idx) in packSteps" :key="idx" class="tl-item">
          <div class="tl-dot" :class="{ done: packStep > idx, active: packStep === idx && !packResult && !packError }"></div>
          <div class="tl-title" :class="{ 'tl-title--pending': packStep <= idx && !packResult }">{{ step.title }}</div>
          <div class="tl-sub">
            {{ packStep > idx || packResult ? step.doneText : packStep === idx ? '处理中...' : '等待' }}
          </div>
        </div>
      </div>
      <div v-if="packResult" class="pack-result pack-result--success">
        <div class="pack-result-icon">✓</div>
        <div class="pack-result-text">打包完成，共 {{ packResult.fileCount }} 个文件</div>
        <div style="display:flex;gap:10px;margin-top:14px">
          <el-button type="primary" size="small" @click="downloadAfterPack">立即下载</el-button>
          <el-button size="small" @click="packProgressVisible = false">关闭</el-button>
        </div>
      </div>
      <div v-else-if="packError" class="pack-result pack-result--error">{{ packError }}</div>
      <div v-if="completeness && completeness.missingDesc && !packResult && !packError" class="notice-bar" style="margin-top:14px">
        <strong>注意：</strong>{{ completeness.missingDesc }}，建议核查前补充完整。
      </div>
      <template #footer>
        <el-button v-if="!packResult" @click="packProgressVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 材料包详情弹窗 -->
    <el-dialog title="材料包详情" v-model="detailVisible" width="560px">
      <div v-if="detailRow" class="detail-grid">
        <div class="detail-item full">
          <span class="detail-label">包名称</span>
          <span>{{ detailRow.packageName }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">核查年度</span>
          <span>{{ detailRow.packageYear }} 年</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">核查机构</span>
          <span>{{ detailRow.verifyOrg }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">文件数量</span>
          <span>{{ detailRow.fileCount }} 个</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">包大小</span>
          <span>{{ formatSize(detailRow.fileSize) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">完整度</span>
          <span>{{ detailRow.completenessRate }}%</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态</span>
          <span class="stage-badge" :class="detailRow.status === '1' ? 'badge-complete' : 'badge-missing'">
            {{ detailRow.status === '1' ? '完成' : '失败' }}
          </span>
        </div>
        <div class="detail-item full">
          <span class="detail-label">创建时间</span>
          <span>{{ detailRow.createTime }}</span>
        </div>
        <div v-if="detailRow.missingDesc" class="detail-item full">
          <span class="detail-label">缺失材料</span>
          <span style="color:var(--ems-accent-orange)">{{ detailRow.missingDesc }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success"
          v-hasPermi="['carbonVerification:package:download']"
          @click="handleDownload(detailRow); detailVisible = false">下载 ZIP</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CarbonVerificationPackage">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPackagePage, getPackageCompleteness, generatePackage, downloadPackage, deletePackage } from '@/api/carbonVerification/package'

const loading = ref(false)
const packing = ref(false)
const tableData = ref([])
const total = ref(0)
const completeness = ref(null)
const packProgressVisible = ref(false)
const packStep = ref(0)
const packResult = ref(null)
const packError = ref('')
const detailVisible = ref(false)
const detailRow = ref(null)

const config = reactive({
  packageYear: String(new Date().getFullYear()),
  verifyOrg: '中国质量认证中心',
  includeReport: '1', includeActivity: '1', includeVoucher: '1', includeFactor: '1',
  includeInternal: '0', includeAccount: '0'
})
const query = reactive({ pageNum: 1, pageSize: 10 })

const packSteps = [
  { title: '收集排放报告',         doneText: '2025年度温室气体排放报告 — 完成' },
  { title: '导出活动数据汇总表',   doneText: '活动数据汇总表已导出 — 完成' },
  { title: '整理原始凭证（按月归档）', doneText: '凭证分月归档完成' },
  { title: '生成排放因子说明文件', doneText: '排放因子来源说明.pdf — 完成' },
  { title: '打包 ZIP 压缩包',      doneText: '压缩包生成完毕' },
]

const includeItems = [
  { key: 'includeReport',   label: '排放报告（Excel）',
    status: c => c?.reportReady ? 'complete' : 'missing' },
  { key: 'includeActivity', label: '活动数据汇总表（Excel）',
    status: c => (c?.activityDataCount ?? 0) > 0 ? 'complete' : 'missing' },
  { key: 'includeVoucher',  label: '原始凭证（按月整理）',
    status: c => !c ? 'missing' : c.voucherUploadedMonths >= 12 ? 'complete' : c.voucherUploadedMonths > 0 ? 'partial' : 'missing' },
  { key: 'includeFactor',   label: '排放因子来源说明',
    status: c => c?.factorReady ? 'complete' : 'missing' },
  { key: 'includeInternal', label: '内部核查记录',   status: () => 'missing' },
  { key: 'includeAccount',  label: '生产台账摘要',   status: () => 'missing' },
]

function itemStatusLabel(s) {
  return { complete: '已就绪', partial: '部分', missing: '未上传' }[s] || '—'
}

const voucherPct = computed(() =>
  completeness.value ? Math.min(completeness.value.voucherUploadedMonths / 12 * 100, 100) : 0)
const activityPct = computed(() =>
  completeness.value?.activityDataTotal > 0
    ? Math.min(completeness.value.activityDataCount / completeness.value.activityDataTotal * 100, 100) : 0)
const factorPct = computed(() => completeness.value?.factorReady ? 100 : 0)

function formatSize(bytes) {
  if (!bytes) return '—'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

async function loadCompleteness() {
  try {
    const res = await getPackageCompleteness(Number(config.packageYear), '全厂')
    completeness.value = res.data
  } catch { /* 静默失败 */ }
}

async function loadList() {
  loading.value = true
  try {
    const res = await getPackagePage({ packageYear: Number(config.packageYear),
      pageNum: query.pageNum, pageSize: query.pageSize })
    tableData.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

async function handleGenerate() {
  packStep.value = 0
  packResult.value = null
  packError.value = ''
  packProgressVisible.value = true
  packing.value = true

  const stepInterval = setInterval(() => {
    if (packStep.value < 4) packStep.value++
  }, 600)

  try {
    const payload = {
      packageName: config.packageYear + '年核查材料包',
      packageYear: Number(config.packageYear),
      verifyOrg: config.verifyOrg,
      includeReport: config.includeReport,
      includeActivity: config.includeActivity,
      includeVoucher: config.includeVoucher,
      includeFactor: config.includeFactor
    }
    const res = await generatePackage(payload)
    clearInterval(stepInterval)
    packStep.value = 5
    packResult.value = res.data
    loadList()
  } catch (e) {
    clearInterval(stepInterval)
    packError.value = '打包失败：' + e.message
  } finally { packing.value = false }
}

async function downloadAfterPack() {
  if (!packResult.value) return
  try {
    const res = await downloadPackage(packResult.value.id)
    const url = URL.createObjectURL(new Blob([res], { type: 'application/zip' }))
    const a = document.createElement('a'); a.href = url
    a.download = packResult.value.packageYear + '年核查材料包.zip'; a.click()
    URL.revokeObjectURL(url)
    packProgressVisible.value = false
  } catch { ElMessage.error('下载失败') }
}

function handleViewDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

async function handleDownload(row) {
  if (!row) return
  try {
    const res = await downloadPackage(row.id)
    const url = URL.createObjectURL(new Blob([res], { type: 'application/zip' }))
    const a = document.createElement('a'); a.href = url
    a.download = row.packageYear + '年核查材料包.zip'; a.click()
    URL.revokeObjectURL(url)
  } catch { ElMessage.error('下载失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除材料包"${row.packageName}"？删除后磁盘文件同步移除。`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    await deletePackage(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

watch(() => config.packageYear, () => { loadCompleteness(); loadList() })
onMounted(() => { loadCompleteness(); loadList() })
</script>

<style scoped>
/* ─── NOTICE BAR ─── */
.notice-bar {
  display: flex; align-items: flex-start; gap: 8px;
  background: rgba(0,140,255,.06); border: 1px solid rgba(0,140,255,.18);
  border-radius: 6px; padding: 10px 14px; margin-bottom: 16px;
  color: var(--ems-text-secondary); font-size: 12px; line-height: 1.6;
}
.notice-bar strong { color: var(--ems-accent-bright); }
.notice-icon { color: var(--ems-accent); margin-top: 2px; flex-shrink: 0; width: 14px; height: 14px; }

/* ─── PANEL HEAD ─── */
.panel-head {
  font-size: 13px; font-weight: 600; color: var(--ems-text-secondary);
  border-left: 3px solid var(--ems-accent); padding-left: 10px;
  letter-spacing: .5px;
}

/* ─── CONFIG PANEL ─── */
.config-panel {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px; padding: 16px;
}
.config-panel :deep(.el-form-item__label) { color: var(--ems-text-muted); }

/* ─── DIVIDER / FORM LABEL ─── */
.divider-line { height: 1px; background: var(--ems-border-dim); margin: 12px 0; }
.form-label-text { color: var(--ems-text-muted); font-size: 12px; margin-bottom: 8px; }

/* ─── INCLUDE ITEMS ─── */
.include-list { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.include-item {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 10px;
  background: var(--ems-bg-main);
  border: 1px solid var(--ems-border-dim);
  border-radius: 5px; cursor: pointer;
  transition: border-color .15s;
}
.include-item:hover { border-color: rgba(0,140,255,.3); }
.include-label { flex: 1; color: var(--ems-text-secondary); font-size: 12px; }

/* ─── STAGE BADGE (PCF lifecycle style) ─── */
.stage-badge {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 2px 9px; border-radius: 10px;
  font-size: 10.5px; white-space: nowrap; border: 1px solid;
}
.badge-complete { background: rgba(0,180,100,.1);  color: var(--ems-accent-green);  border-color: rgba(0,180,100,.25); }
.badge-partial  { background: rgba(255,146,64,.1); color: var(--ems-accent-orange); border-color: rgba(255,146,64,.25); }
.badge-missing  { background: rgba(255,77,106,.08); color: var(--ems-accent-red);   border-color: rgba(255,77,106,.2); }
.badge-pending  { background: rgba(255,255,255,.04); color: var(--ems-text-muted);  border-color: var(--ems-border-dim); }

/* ─── SECTION CARD (PCF lc-stage-list style) ─── */
.section-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px; overflow: hidden;
}
.card-head {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  padding: 9px 16px; font-size: 11px; font-weight: 600; letter-spacing: .8px;
  color: var(--ems-text-muted); text-transform: uppercase;
  border-bottom: 1px solid var(--ems-border-dim);
  background: rgba(0,0,0,.06);
}

/* ─── STAGE ROWS (PCF lifecycle progress rows) ─── */
.stage-row {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 16px; border-bottom: 1px solid var(--ems-border-dim);
  transition: background .12s;
}
.stage-row:hover { background: rgba(0,140,255,.03); }
.stage-idx {
  width: 22px; height: 22px; border-radius: 50%;
  background: rgba(0,140,255,.1); border: 1px solid rgba(0,140,255,.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; color: var(--ems-accent-bright); font-weight: 600; flex-shrink: 0;
}
.stage-name { min-width: 72px; font-size: 13px; color: var(--ems-text-primary); }
.stage-bar-wrap {
  flex: 2; height: 6px;
  background: rgba(255,255,255,.05);
  border-radius: 3px; overflow: hidden;
}
.stage-bar {
  height: 100%; border-radius: 3px;
  background: linear-gradient(90deg, var(--ems-accent), var(--ems-accent-bright));
  transition: width .8s;
}
.stage-bar--green {
  background: linear-gradient(90deg, var(--ems-accent-green), rgba(0,232,150,.7));
}
.stage-value { min-width: 60px; text-align: right; font-weight: 600; color: var(--ems-accent-bright); font-size: 14px; }
.stage-pct   { min-width: 44px; font-size: 11px; color: var(--ems-text-muted); }
.card-warn {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px;
  font-size: 11.5px; color: var(--ems-accent-orange);
  border-top: 1px solid var(--ems-border-dim);
  background: rgba(255,146,64,.04);
}

/* ─── PANEL TOOLBAR (PCF data-panel toolbar) ─── */
.panel-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 16px; border-bottom: 1px solid var(--ems-border-dim);
}
.toolbar-sub { font-size: 11px; color: var(--ems-text-muted); }

/* ─── TABLE VALUES ─── */
.td-value { font-size: 13px; font-weight: 600; color: var(--ems-text-primary); }

/* ─── ACTION BUTTONS ─── */
.action-btn {
  display: inline-flex; align-items: center;
  padding: 2px 8px; border-radius: 3px; font-size: 11px;
  cursor: pointer; border: 1px solid; transition: all .15s; margin: 0 2px;
}
.action-view { background: rgba(0,232,150,.08); border-color: rgba(0,232,150,.25); color: #44ddaa; }
.action-view:hover { background: rgba(0,232,150,.18); }
.action-dl   { background: rgba(0,140,255,.1);   border-color: rgba(0,140,255,.3);  color: #66aaff; }
.action-dl:hover   { background: rgba(0,140,255,.22); border-color: rgba(0,140,255,.55); }
.action-del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-del:hover  { background: rgba(255,77,106,.18); }
.action-disabled { opacity: .35; cursor: not-allowed; pointer-events: none; }

/* ─── TIMELINE ─── */
.timeline { padding-left: 16px; padding-top: 4px; }
.tl-item { position: relative; padding: 0 0 16px 24px; border-left: 2px solid var(--ems-border-dim); }
.tl-item:last-child { border-left-color: transparent; }
.tl-dot {
  position: absolute; left: -7px; top: 3px;
  width: 12px; height: 12px; border-radius: 50%;
  background: var(--ems-bg-main); border: 2px solid var(--ems-border-dim);
}
.tl-dot.done   { background: var(--ems-accent-green); border-color: var(--ems-accent-green); box-shadow: 0 0 6px var(--ems-accent-green); }
.tl-dot.active { background: var(--ems-accent); border-color: var(--ems-accent-bright); box-shadow: 0 0 6px var(--ems-accent); }
.tl-title              { color: var(--ems-text-primary); font-size: 13px; }
.tl-title--pending     { color: var(--ems-text-muted); }
.tl-sub                { color: var(--ems-text-muted); font-size: 11px; margin-top: 3px; }

/* ─── PACK RESULT ─── */
.pack-result {
  display: flex; flex-direction: column; align-items: center;
  padding: 16px; margin-top: 12px; border-radius: 6px;
}
.pack-result--success {
  background: rgba(0,180,100,.08); border: 1px solid rgba(0,180,100,.25);
}
.pack-result--error {
  background: rgba(255,77,106,.06); border: 1px solid rgba(255,77,106,.2);
  color: var(--ems-accent-red); font-size: 13px; align-items: flex-start; padding: 12px 14px;
}
.pack-result-icon { font-size: 36px; color: var(--ems-accent-green); }
.pack-result-text { color: var(--ems-accent-green); font-size: 14px; margin-top: 8px; }

/* ─── DETAIL GRID ─── */
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.detail-item {
  display: flex; flex-direction: column; gap: 4px;
  background: var(--ems-bg-main);
  border: 1px solid var(--ems-border-dim);
  border-radius: 5px; padding: 10px;
}
.detail-item.full { grid-column: 1 / -1; }
.detail-label { color: var(--ems-text-muted); font-size: 11px; }
.detail-item > span:not(.detail-label):not(.stage-badge) { color: var(--ems-text-secondary); font-size: 13px; }
</style>
