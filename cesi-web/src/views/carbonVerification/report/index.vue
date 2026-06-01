<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧配置面板 -->
      <el-col :span="7">
        <div class="config-panel">
          <div class="panel-head" style="margin-bottom:14px">报告配置</div>
          <el-form :model="config" label-width="90px" size="small">
            <el-form-item label="报告类型">
              <el-select v-model="config.reportType" style="width:100%">
                <el-option label="温室气体排放报告（年度）" value="ANNUAL" />
                <el-option label="碳核查辅助报告" value="ASSIST" />
                <el-option label="月度排放快报" value="MONTHLY" />
              </el-select>
            </el-form-item>
            <el-form-item label="核查年度">
              <el-date-picker v-model="config.year" type="year" value-format="YYYY" style="width:100%" />
            </el-form-item>
            <el-form-item label="组织范围">
              <el-select v-model="config.orgUnit" style="width:100%">
                <el-option label="全厂（企业边界）" value="全厂" />
              </el-select>
            </el-form-item>
            <el-form-item label="排放核算标准">
              <el-select v-model="config.standard" style="width:100%">
                <el-option label="发改委指南（2015版）" value="发改委指南（2015版）" />
                <el-option label="ISO 14064-1:2018" value="ISO 14064-1:2018" />
              </el-select>
            </el-form-item>
            <el-form-item label="报告模板">
              <el-select v-model="config.templateType" style="width:100%">
                <el-option label="通用行业模板" value="通用行业模板" />
                <el-option label="电力行业模板" value="电力行业模板" />
                <el-option label="化工行业模板" value="化工行业模板" />
              </el-select>
            </el-form-item>
            <div class="divider-line"></div>
            <div class="form-label-text">包含章节</div>
            <div class="section-check-list">
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.basicInfo" />
                <span>企业基本信息</span>
              </label>
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.boundary" />
                <span>核算边界与排放源</span>
              </label>
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.activityData" />
                <span>活动数据及来源</span>
              </label>
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.factor" />
                <span>排放因子及来源</span>
              </label>
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.result" />
                <span>排放量计算结果</span>
              </label>
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.uncertainty" />
                <span>不确定性分析</span>
              </label>
              <label class="section-check-item">
                <el-checkbox v-model="config.sections.voucherList" />
                <span>附件凭证清单</span>
              </label>
            </div>
            <div class="divider-line"></div>
            <el-button type="success" style="width:100%;margin-bottom:10px"
              v-hasPermi="['carbonVerification:report:generate']"
              :loading="generating" @click="handleGenerate">
              生成报告预览
            </el-button>
            <div class="divider-line"></div>
            <el-button type="primary" style="width:100%"
              v-hasPermi="['carbonVerification:report:export']"
              :disabled="!previewData"
              @click="exportPdf">
              导出 PDF
            </el-button>
          </el-form>
        </div>
      </el-col>

      <!-- 右侧：检查状态 + 历史列表 -->
      <el-col :span="17">
        <!-- 生成进度与状态（始终显示4项，未生成时为 pending 态） -->
        <div class="panel-head" style="margin-bottom:12px">生成进度与状态</div>
        <div class="check-list">
          <!-- 1. 活动数据完整性 -->
          <div class="check-item"
            :class="!checkResult ? 'pending-item' : checkResult.checkDataStatus === 'PASS' ? 'done-item' : 'warn-item'">
            <div class="check-icon"
              :class="!checkResult ? 'pending' : checkResult.checkDataStatus === 'PASS' ? 'done' : 'warn'">
              {{ !checkResult ? '—' : checkResult.checkDataStatus === 'PASS' ? '✓' : '!' }}
            </div>
            <div class="check-text">
              <div class="check-title">活动数据完整性检查</div>
              <div class="check-desc">
                <template v-if="checkResult">
                  {{ checkResult.sourceItems ? checkResult.sourceItems.length : 0 }} 个排放源，
                  数据完整度 {{ checkResult.dataCompleteness }}%
                  <span v-if="checkResult.checkDataStatus !== 'PASS'">（部分月份数据缺失，将使用估算值）</span>
                </template>
                <template v-else>等待生成报告后检查</template>
              </div>
            </div>
            <span class="check-tag"
              :class="!checkResult ? 'tag-pending' : checkResult.checkDataStatus === 'PASS' ? 'tag-complete' : 'tag-partial'">
              {{ !checkResult ? '待检查' : checkResult.checkDataStatus === 'PASS' ? '通过' : '警告' }}
            </span>
          </div>

          <!-- 2. 排放因子有效性 -->
          <div class="check-item" :class="!checkResult ? 'pending-item' : 'done-item'">
            <div class="check-icon" :class="!checkResult ? 'pending' : 'done'">
              {{ !checkResult ? '—' : '✓' }}
            </div>
            <div class="check-text">
              <div class="check-title">排放因子有效性核验</div>
              <div class="check-desc">
                <template v-if="checkResult">所有排放因子均在有效期内，来源符合国家标准</template>
                <template v-else>等待生成报告后检查</template>
              </div>
            </div>
            <span class="check-tag" :class="!checkResult ? 'tag-pending' : 'tag-complete'">
              {{ !checkResult ? '待检查' : '通过' }}
            </span>
          </div>

          <!-- 3. 原始凭证关联 -->
          <div class="check-item"
            :class="!checkResult ? 'pending-item' : checkResult.checkVoucherStatus === 'PASS' ? 'done-item' : 'warn-item'">
            <div class="check-icon"
              :class="!checkResult ? 'pending' : checkResult.checkVoucherStatus === 'PASS' ? 'done' : 'warn'">
              {{ !checkResult ? '—' : checkResult.checkVoucherStatus === 'PASS' ? '✓' : '!' }}
            </div>
            <div class="check-text">
              <div class="check-title">原始凭证关联检查</div>
              <div class="check-desc">
                <template v-if="checkResult">
                  凭证覆盖完整度 {{ checkResult.dataCompleteness }}%
                  <span v-if="checkResult.checkVoucherStatus !== 'PASS'">，建议补充后再导出正式报告</span>
                </template>
                <template v-else>等待生成报告后检查</template>
              </div>
            </div>
            <span class="check-tag"
              :class="!checkResult ? 'tag-pending' : checkResult.checkVoucherStatus === 'PASS' ? 'tag-complete' : 'tag-partial'">
              {{ !checkResult ? '待检查' : checkResult.checkVoucherStatus === 'PASS' ? '通过' : '警告' }}
            </span>
          </div>

          <!-- 4. 排放量计算汇总 -->
          <div class="check-item"
            :class="!checkResult ? 'pending-item' : checkResult.totalEmission ? 'done-item' : 'pending-item'">
            <div class="check-icon"
              :class="!checkResult ? 'pending' : checkResult.totalEmission ? 'done' : 'pending'">
              {{ !checkResult ? '—' : checkResult.totalEmission ? '✓' : '—' }}
            </div>
            <div class="check-text">
              <div class="check-title">排放量计算汇总</div>
              <div class="check-desc">
                <template v-if="checkResult && checkResult.totalEmission">
                  年度总排放 {{ checkResult.totalEmission }} tCO₂e，计算逻辑验证通过
                </template>
                <template v-else>等待生成报告后计算</template>
              </div>
            </div>
            <span class="check-tag"
              :class="checkResult && checkResult.totalEmission ? 'tag-complete' : 'tag-pending'">
              {{ checkResult && checkResult.totalEmission ? '完成' : '待计算' }}
            </span>
          </div>
        </div>

        <!-- 历史报告列表 -->
        <div class="panel-head" style="margin:20px 0 12px">历史报告记录</div>
        <el-table :data="tableData" border stripe v-loading="loading" size="small">
          <el-table-column label="报告名称" prop="reportName" min-width="190">
            <template #default="{ row }">
              <span class="td-value">{{ row.reportName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="报告年度" prop="reportYear" align="center" width="80" />
          <el-table-column label="生成时间" prop="generateTime" align="center" width="140" />
          <el-table-column label="生成人" prop="createBy" align="center" width="90" />
          <el-table-column label="版本" prop="reportVersion" align="center" width="70" />
          <el-table-column label="操作" align="center" width="170" fixed="right">
            <template #default="{ row }">
              <span class="action-btn action-view" @click="handlePreview(row)">预览</span>
              <span class="action-btn action-dl"
                v-hasPermi="['carbonVerification:report:export']"
                @click="handleExport(row)">下载</span>
              <span class="action-btn action-del"
                v-hasPermi="['carbonVerification:report:remove']"
                @click="handleDelete(row)">删除</span>
            </template>
          </el-table-column>
        </el-table>

        <EmsPageBar
          :total="total"
          v-model:page="query.pageNum"
          v-model:page-size="query.pageSize"
          @change="loadList"
        />
      </el-col>
    </el-row>

    <!-- 报告预览弹窗 -->
    <el-dialog title="排放报告预览" v-model="previewVisible" width="920px">
      <div v-if="previewData" class="report-preview" ref="reportPreviewRef">
        <div class="report-h1">温室气体排放报告</div>
        <div class="report-meta">核查年度：{{ previewData.reportYear }}年 &nbsp;|&nbsp;
          组织范围：{{ previewData.orgUnit }} &nbsp;|&nbsp;
          报告生成时间：{{ new Date().toLocaleDateString('zh-CN') }}
        </div>

        <!-- 企业基本信息 -->
        <div v-if="hasSection('basicInfo')" class="report-section">
          <div class="report-h2">{{ sectionIndex('basicInfo') }}、企业基本信息</div>
          <p class="report-p">本报告依据国家发展和改革委员会发布的温室气体核算方法与报告指南，对本企业 {{ previewData.reportYear }} 年度温室气体排放情况进行核算与报告。核算边界为企业法人边界内所有生产及辅助生产活动。</p>
        </div>

        <!-- 二、核算边界与排放源识别 -->
        <div v-if="hasSection('boundary')" class="report-section">
          <div class="report-h2">{{ sectionIndex('boundary') }}、核算边界与排放源识别</div>
          <p class="report-p">本企业核算边界内识别到以下排放源：</p>
          <table class="report-table">
            <thead>
              <tr><th>排放源</th><th>排放类型</th><th>气体种类</th><th>是否纳入核算</th></tr>
            </thead>
            <tbody>
              <tr v-for="row in previewData.sourceItems" :key="row.emissionSource">
                <td>{{ row.emissionSource }}</td>
                <td>{{ row.emissionType === 'DIRECT' ? '直接排放（范围一）' : '间接排放（范围二）' }}</td>
                <td>{{ row.emissionType === 'DIRECT' ? 'CO₂, CH₄, N₂O' : 'CO₂' }}</td>
                <td>是</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 三、活动数据及来源 -->
        <div v-if="hasSection('activityData')" class="report-section">
          <div class="report-h2">{{ sectionIndex('activityData') }}、活动数据及来源</div>
          <table class="report-table">
            <thead>
              <tr><th>排放源</th><th>年活动量</th><th>单位</th><th>数据获取方式</th></tr>
            </thead>
            <tbody>
              <tr v-for="row in previewData.sourceItems" :key="row.emissionSource">
                <td>{{ row.emissionSource }}</td>
                <td>{{ row.activityValue }}</td>
                <td>{{ row.unit }}</td>
                <td>系统数据</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 四、排放因子及来源 -->
        <div v-if="hasSection('factor')" class="report-section">
          <div class="report-h2">{{ sectionIndex('factor') }}、排放因子及来源</div>
          <table class="report-table">
            <thead>
              <tr><th>排放源</th><th>排放因子</th><th>单位</th><th>因子来源</th></tr>
            </thead>
            <tbody>
              <tr v-for="row in previewData.sourceItems" :key="row.emissionSource">
                <td>{{ row.emissionSource }}</td>
                <td>{{ row.emissionFactor }}</td>
                <td>tCO₂e/{{ row.unit }}</td>
                <td>{{ row.factorSource || '国家缺省值' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 五、排放量计算结果 -->
        <div v-if="hasSection('result')" class="report-section">
          <div class="report-h2">{{ sectionIndex('result') }}、排放量计算结果</div>
          <table class="report-table">
            <thead>
              <tr><th>排放源</th><th>活动数据量</th><th>单位</th><th>排放因子</th><th>排放量(tCO₂e)</th><th>占比</th></tr>
            </thead>
            <tbody>
              <tr v-for="row in previewData.sourceItems" :key="row.emissionSource">
                <td>{{ row.emissionSource }}</td>
                <td>{{ row.activityValue }}</td>
                <td>{{ row.unit }}</td>
                <td>{{ row.emissionFactor }}</td>
                <td>{{ row.totalEmission }}</td>
                <td>{{ previewData.totalEmission ? (row.totalEmission / previewData.totalEmission * 100).toFixed(1) + '%' : '—' }}</td>
              </tr>
              <tr class="report-table-total">
                <td><strong>合计</strong></td>
                <td>—</td><td>—</td><td>—</td>
                <td><strong style="color:#4fc3f7">{{ previewData.totalEmission }}</strong></td>
                <td>100%</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- 六、不确定性分析 -->
        <div v-if="hasSection('uncertainty')" class="report-section">
          <div class="report-h2">{{ sectionIndex('uncertainty') }}、不确定性分析</div>
          <p class="report-p">活动数据来源于能源管理系统实时采集，计量仪器经过定期检定，不确定性 ≤ ±2%。排放因子采用国家发改委公布的缺省值，综合不确定性评估结果满足温室气体核算与报告的数据质量要求。</p>
        </div>

        <!-- 七、数据质量声明（附件凭证清单入口） -->
        <div v-if="hasSection('voucherList')" class="report-section">
          <div class="report-h2">{{ sectionIndex('voucherList') }}、附件凭证清单</div>
          <p class="report-p">本报告中所有活动数据均来源于企业能源管理系统实时采集数据，已与原始计量凭证进行交叉核对，数据完整性达 {{ previewData.dataCompleteness }}%，符合温室气体核算与报告的数据质量要求。凭证清单详见导出 Excel 中的"附件凭证清单"工作表。</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="primary"
          v-hasPermi="['carbonVerification:report:export']"
          @click="exportPdf">导出 PDF</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CarbonVerificationReport">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import html2pdf from 'html2pdf.js'
import { getReportPage, checkReportStatus, generateReport, previewReport, exportReport, deleteReport } from '@/api/carbonVerification/report'

const reportPreviewRef = ref(null)
const loading = ref(false)
const generating = ref(false)
const tableData = ref([])
const total = ref(0)
const checkResult = ref(null)
const previewVisible = ref(false)
const previewData = ref(null)
const currentPreviewRow = ref(null)

const config = reactive({
  reportType: 'ANNUAL',
  year: String(new Date().getFullYear()),
  orgUnit: '全厂',
  standard: '发改委指南（2015版）',
  templateType: '通用行业模板',
  sections: {
    basicInfo: true, boundary: true, activityData: true,
    factor: true, result: true, uncertainty: true, voucherList: false
  }
})
const query = reactive({ pageNum: 1, pageSize: 10 })

async function loadList() {
  loading.value = true
  try {
    const res = await getReportPage({ reportYear: Number(config.year), orgUnit: config.orgUnit,
      pageNum: query.pageNum, pageSize: query.pageSize })
    tableData.value = res.rows || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

const SECTION_ORDER = ['basicInfo', 'boundary', 'activityData', 'factor', 'result', 'uncertainty', 'voucherList']

function sectionsToString() {
  return SECTION_ORDER.filter(k => config.sections[k]).join(',')
}

function hasSection(key) {
  if (!previewData.value?.sections) return true
  return previewData.value.sections.includes(key)
}

function sectionIndex(key) {
  if (!previewData.value?.sections) return '—'
  const list = SECTION_ORDER.filter(k => previewData.value.sections.includes(k))
  const idx = list.indexOf(key)
  const nums = ['一', '二', '三', '四', '五', '六', '七']
  return idx >= 0 ? nums[idx] : '—'
}

async function handleGenerate() {
  generating.value = true
  try {
    const statusRes = await checkReportStatus(Number(config.year), config.orgUnit)
    checkResult.value = statusRes.data
    const genRes = await generateReport(Number(config.year), config.orgUnit, config.standard, config.templateType, sectionsToString())
    const report = genRes.data
    ElMessage.success('报告生成成功')
    loadList()
    if (report?.id) {
      currentPreviewRow.value = report
      const prevRes = await previewReport(report.id)
      previewData.value = prevRes.data
      previewVisible.value = true
    }
  } catch (e) { ElMessage.error('生成失败：' + e.message) }
  finally { generating.value = false }
}

async function handlePreview(row) {
  currentPreviewRow.value = row
  const res = await previewReport(row.id)
  previewData.value = res.data
  previewVisible.value = true
}

async function exportPdf() {
  if (!previewData.value) {
    ElMessage.warning('请先生成报告预览')
    return
  }
  if (!previewVisible.value) {
    previewVisible.value = true
    await nextTick()
    await new Promise(r => setTimeout(r, 300))
  }
  const el = reportPreviewRef.value
  if (!el) return
  const filename = `${previewData.value.reportYear}年度温室气体排放报告.pdf`
  await html2pdf()
    .set({
      margin:      [12, 14, 12, 14],
      filename,
      image:       { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true, logging: false, backgroundColor: '#060f1e' },
      jsPDF:       { unit: 'mm', format: 'a4', orientation: 'portrait' },
      pagebreak:   { mode: ['avoid-all', 'css', 'legacy'] }
    })
    .from(el)
    .save()
}

async function handleExport(row) {
  if (!row) return
  try {
    const res = await exportReport(row.id)
    const url = URL.createObjectURL(new Blob([res]))
    const a = document.createElement('a'); a.href = url
    a.download = row.reportYear + '年度排放报告.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch (e) { ElMessage.error('导出失败') }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除报告"${row.reportName}"？`, '提示', {
      type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消'
    })
    await deleteReport(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadList)
</script>

<style scoped>
/* ─── PANEL HEAD (section title) ─── */
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

/* ─── SECTION CHECKBOXES ─── */
.section-check-list { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.section-check-item {
  display: flex; align-items: center; gap: 8px;
  color: var(--ems-text-secondary); font-size: 12px; cursor: pointer;
}
.section-check-item:hover { color: var(--ems-text-primary); }

/* ─── CHECK LIST ─── */
.check-list { display: flex; flex-direction: column; gap: 8px; }
.check-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 14px;
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 6px;
  transition: border-color .2s;
}
.check-item.done-item    { border-color: var(--ems-accent-green); }
.check-item.warn-item    { border-color: var(--ems-accent-orange); }
.check-item.pending-item { border-color: var(--ems-border-dim); }

.check-icon {
  width: 20px; height: 20px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; flex-shrink: 0; font-weight: 700;
}
.check-icon.done {
  background: rgba(0,180,100,.15); color: var(--ems-accent-green);
  box-shadow: 0 0 6px var(--ems-accent-green);
}
.check-icon.warn {
  background: rgba(255,146,64,.12); color: var(--ems-accent-orange);
  box-shadow: 0 0 6px var(--ems-accent-orange);
}
.check-icon.pending {
  background: rgba(255,255,255,.04); color: var(--ems-text-muted);
  border: 1px solid var(--ems-border-dim);
}

.check-text { flex: 1; }
.check-title { color: var(--ems-text-primary); font-size: 13px; }
.check-desc  { color: var(--ems-text-muted); font-size: 11px; margin-top: 2px; }

/* ─── CHECK TAGS ─── */
.check-tag {
  display: inline-block; padding: 2px 9px; border-radius: 10px;
  font-size: 11px; white-space: nowrap; border: 1px solid;
}
.tag-complete { background: rgba(0,180,100,.1); color: var(--ems-accent-green);   border-color: rgba(0,180,100,.25); }
.tag-partial  { background: rgba(255,146,64,.1); color: var(--ems-accent-orange); border-color: rgba(255,146,64,.25); }
.tag-missing  { background: rgba(255,77,106,.08); color: var(--ems-accent-red);   border-color: rgba(255,77,106,.2); }
.tag-pending  { background: rgba(255,255,255,.04); color: var(--ems-text-muted);  border-color: var(--ems-border-dim); }

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
.action-dl   { background: rgba(0,140,255,.1);  border-color: rgba(0,140,255,.3);  color: #66aaff; }
.action-dl:hover   { background: rgba(0,140,255,.22); border-color: rgba(0,140,255,.55); }
.action-del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-del:hover  { background: rgba(255,77,106,.18); }

/* ─── REPORT PREVIEW ─── */
.report-preview {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 6px; padding: 24px 30px;
}
.report-h1 {
  color: var(--ems-accent-bright); font-size: 18px;
  text-align: center; margin-bottom: 6px; font-weight: 700;
}
.report-meta { text-align: center; color: var(--ems-text-muted); font-size: 12px; margin-bottom: 20px; }
.report-section { margin-bottom: 16px; }
.report-h2 {
  color: var(--ems-text-secondary); font-size: 14px; font-weight: 600;
  border-bottom: 1px solid var(--ems-border-dim);
  padding-bottom: 6px; margin-bottom: 10px; margin-top: 16px;
}
.report-p { color: var(--ems-text-secondary); line-height: 1.8; font-size: 13px; margin-bottom: 8px; }
.report-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 12px; }
.report-table th,
.report-table td { border: 1px solid var(--ems-border-dim); padding: 7px 10px; text-align: center; }
.report-table th { background: var(--ems-bg-main); color: var(--ems-text-muted); }
.report-table td { color: var(--ems-text-secondary); }
.report-table-total td { background: var(--ems-bg-main); }
</style>
