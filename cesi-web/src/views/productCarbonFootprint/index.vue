<template>
  <div class="pcf-page">
    <!-- 左侧产品树 -->
    <div class="pcf-sidebar">
      <div class="sidebar-header">
        <span>产品分类</span>
      </div>
      <el-scrollbar class="sidebar-scroll">
        <!-- 无数据引导 -->
        <div v-if="!categoryTree.length" class="sidebar-empty">
          <el-icon style="font-size:32px;color:#c0c4cc"><FolderOpened /></el-icon>
          <p>暂无产品分类</p>
          <p style="font-size:11px;color:#c0c4cc">请前往碳足迹配置页添加</p>
        </div>
        <!-- 分类产品树（产品已注入为分类叶节点） -->
        <el-tree
          v-else
          ref="treeRef"
          :data="categoryTree"
          :props="{ label: 'categoryName', children: 'children' }"
          node-key="id"
          highlight-current
          default-expand-all
          @node-click="onTreeNodeClick"
        >
          <template #default="{ node, data }">
            <span class="tree-node-label" :class="{ 'active-product': data._isProduct && currentProduct?.id === data.id }">
              <el-icon v-if="data._isProduct" class="node-icon-product"><Document /></el-icon>
              <el-icon v-else-if="node.expanded" class="node-icon-folder"><FolderOpened /></el-icon>
              <el-icon v-else class="node-icon-folder"><Folder /></el-icon>
              <span class="label-text">{{ data.productName || data.categoryName }}</span>
              <el-tag v-if="data.productCount != null && !data._isProduct"
                      size="small" type="info" class="count-tag">
                {{ data.productCount }}款
              </el-tag>
            </span>
          </template>
        </el-tree>
      </el-scrollbar>
      <div class="sidebar-config">
        <router-link to="/productCarbonFootprint/config" class="config-link">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.8">
            <circle cx="7" cy="7" r="2.5"/>
            <path d="M7 1v2M7 11v2M1 7h2M11 7h2" stroke-linecap="round"/>
          </svg>
          碳足迹配置
        </router-link>
      </div>
    </div>

    <!-- 右侧主内容 -->
    <div class="pcf-main" v-if="currentProduct">
      <!-- 产品信息头部 -->
      <div class="product-header">
        <div class="product-meta">
          <div class="meta-item">
            <span class="meta-label">产品名称</span>
            <span class="meta-value">{{ currentProduct.productName }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="meta-item">
            <span class="meta-label">规格型号</span>
            <span class="meta-value">{{ currentProduct.specModel }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="meta-item">
            <span class="meta-label">单位</span>
            <span class="meta-value">{{ currentProduct.unit }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="meta-item">
            <span class="meta-label">生命周期边界</span>
            <span class="meta-value">{{ currentProduct.lifecycleBoundary }}</span>
          </div>
          <el-divider direction="vertical" />
          <div class="meta-item">
            <span class="meta-label">核算年份</span>
            <el-date-picker
              v-model="currentYear"
              type="year"
              value-format="YYYY"
              style="width:100px"
              @change="onYearChange"
            />
          </div>
          <el-divider direction="vertical" />
          <div class="meta-item">
            <span class="meta-label">核算标准</span>
            <span class="meta-value highlight">{{ currentProduct.accountingStandard }}</span>
          </div>
        </div>
        <div class="carbon-total">
          <div class="carbon-orb">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <circle cx="9" cy="9" r="7.5" stroke="#00aaff" stroke-width="1.3"/>
              <path d="M5.5 9C5.5 7 7 5.5 9 5.5c1.2 0 2.2.6 2.8 1.5" stroke="#00ccff" stroke-width="1.3" stroke-linecap="round"/>
              <path d="M12.5 9c0 2-1.5 3.5-3.5 3.5-1.2 0-2.2-.6-2.8-1.5" stroke="#00ccff" stroke-width="1.3" stroke-linecap="round"/>
            </svg>
            <span class="carbon-orb-label">CO₂e</span>
          </div>
          <div class="carbon-total-info">
            <div class="total-label">碳足迹（tCO₂e/{{ currentProduct.unit }}）</div>
            <div class="total-value">{{ lifecycle.totalFootprint ?? '—' }}</div>
            <div class="net-hint" v-if="lifecycle.greenCertOffset > 0">
              绿证抵消后：<span class="green-val">{{ lifecycle.netFootprint }}</span> tCO₂e
            </div>
          </div>
        </div>
        <div class="btn-report-wrap">
          <el-button type="primary" @click="openReportDialog" v-hasPermi="['pcf:report:generate']">
            生成报告
          </el-button>
        </div>
      </div>

      <!-- Tab 导航 -->
      <el-tabs v-model="activeTab" class="pcf-tabs" @tab-change="onTabChange">
        <el-tab-pane label="生命周期" name="lifecycle" />
        <el-tab-pane name="raw">
          <template #label>原材料获取 <span v-if="tabCounts.raw" class="tab-badge">{{ tabCounts.raw }}</span></template>
        </el-tab-pane>
        <el-tab-pane name="mfg">
          <template #label>生产制造 <span v-if="tabCounts.mfg" class="tab-badge">{{ tabCounts.mfg }}</span></template>
        </el-tab-pane>
        <el-tab-pane name="logistics">
          <template #label>物流/仓储/运输 <span v-if="tabCounts.logistics" class="tab-badge">{{ tabCounts.logistics }}</span></template>
        </el-tab-pane>
        <el-tab-pane name="use">
          <template #label>产品使用 <span v-if="tabCounts.use" class="tab-badge">{{ tabCounts.use }}</span></template>
        </el-tab-pane>
        <el-tab-pane name="eol">
          <template #label>废弃回收 <span v-if="tabCounts.eol" class="tab-badge">{{ tabCounts.eol }}</span></template>
        </el-tab-pane>
        <el-tab-pane name="cert">
          <template #label>绿电绿证 <span v-if="tabCounts.cert" class="tab-badge tab-badge--green">{{ tabCounts.cert }}</span></template>
        </el-tab-pane>
      </el-tabs>

      <!-- Tab 内容 -->
      <div class="tab-content">

        <!-- ══ 生命周期总览 ══ -->
        <div v-show="activeTab === 'lifecycle'" class="lc-overview">
          <!-- panel-toolbar -->
          <div class="lc-toolbar">
            <div class="lc-toolbar-total">
              <span class="panel-total-label">合计：</span>
              <span class="total-big">{{ lifecycle.totalFootprint ?? '0.00' }}</span>
              <span class="panel-total-unit">tCO₂e/{{ currentProduct.unit }}</span>
            </div>
            <button class="lc-btn-export" @click="$emit('export')">导出数据</button>
          </div>

          <div class="lc-stage-list">
            <div class="lc-card-title">生命周期各阶段碳排放概览</div>
            <div v-for="(s, idx) in lifecycle.stages" :key="idx" class="lc-stage-row">
              <div class="stage-idx">{{ idx + 1 }}</div>
              <div class="stage-name">{{ s.stageName }}</div>
              <div class="stage-bar-wrap">
                <div class="stage-bar"
                  :style="{
                    width: s.percentage + '%',
                    background: stageBarGradient(idx)
                  }"
                />
              </div>
              <div class="stage-value" :style="{ color: stageValueColor(idx) }">{{ s.amount }}</div>
              <div class="stage-pct">{{ s.percentage }}%</div>
              <span class="stage-badge" :class="stageBadgeClass(idx)">{{ s.stageLabel || s.stageName }}</span>
            </div>
            <div class="lc-total-row">
              <span>合计（总碳足迹）</span>
              <span class="total-num">{{ lifecycle.totalFootprint }} <small>tCO₂e/{{ currentProduct.unit }}</small></span>
            </div>
          </div>
          <div v-if="lifecycle.greenCertOffset > 0" class="green-offset-bar">
            <svg width="22" height="22" viewBox="0 0 22 22" fill="none" style="flex-shrink:0">
              <path d="M11 2.5l2.8 8.5h8.5l-6.8 5 2.8 8.5-7.3-5-7.3 5 2.8-8.5L.7 11H9.2z"
                fill="rgba(0,232,150,.2)" stroke="#00e896" stroke-width="1.3"/>
            </svg>
            <div class="green-offset-info">
              <div class="green-offset-title">绿电绿证抵消</div>
              <div class="green-offset-desc">
                本产品已购绿证 <b>{{ lifecycle.greenCertMwh ?? '—' }} MWh</b>，
                折算碳减排量 <b>{{ lifecycle.greenCertOffset }} tCO₂e</b>
              </div>
            </div>
            <div class="green-offset-amount">
              <div class="green-offset-num">-{{ lifecycle.greenCertOffset }}</div>
              <div class="green-offset-unit">tCO₂e 抵消</div>
            </div>
            <button class="btn-green-cert" @click="activeTab = 'cert'">绿证详情</button>
          </div>
          <div class="net-total-bar">
            <div>
              <div class="net-total-title">净碳足迹（扣除绿证抵消后）</div>
              <div class="net-total-hint">可用于产品碳标识认证及碳标签申报</div>
            </div>
            <div class="net-total-right">
              <span class="net-num">{{ lifecycle.netFootprint }}</span>
              <span class="net-unit">tCO₂e/{{ currentProduct.unit }}</span>
              <span v-if="reductionPct !== '0.00'" class="net-badge">↓ {{ reductionPct }}%</span>
            </div>
          </div>
        </div>

        <!-- ══ 通用表格面板（原材料/生产/物流/使用/回收/绿证） ══ -->
        <template v-for="tab in dataTabs" :key="tab.name">
          <div v-show="activeTab === tab.name" class="data-panel">
            <div class="panel-toolbar">
              <div class="total-info">
                <span class="panel-total-label">合计：</span>
                <span class="total-big">{{ tabTotals[tab.name] ?? '0.00' }}</span>
                <span class="panel-total-unit">tCO₂e/{{ currentProduct.unit }}</span>
              </div>
              <div class="btn-group">
                <el-button type="primary" size="small" v-hasPermi="[tab.addPerm]"
                           @click="openAddDialog(tab)">
                  + 新增
                </el-button>
                <el-button size="small" v-hasPermi="[tab.importPerm]"
                           @click="handleImport(tab)">
                  导入Excel
                </el-button>
                <el-button size="small" @click="handleDownloadTemplate(tab)">下载模板</el-button>
              </div>
            </div>

            <div style="overflow-x:auto">
            <el-table
              :data="pagedData(tab.name)"
              size="small"
              style="width:100%"
              v-loading="tabLoading[tab.name]"
            >
              <el-table-column label="序号" width="55" align="center">
                <template #default="{ $index }">
                  {{ (tabPageNum[tab.name] - 1) * tabPageSize[tab.name] + $index + 1 }}
                </template>
              </el-table-column>
              <el-table-column
                v-for="col in tab.columns"
                :key="col.prop"
                :prop="col.prop"
                :label="col.label"
                :min-width="col.width"
                :align="col.align || 'center'"
                show-overflow-tooltip
              >
                <template v-if="col.slot" #default="{ row }">
                  <el-tag v-if="col.slot === 'transport'" size="small">{{ row.transportMode }}</el-tag>
                  <el-tag v-else-if="col.slot === 'certStatus'" :type="row.status === '1' ? 'success' : 'warning'" size="small">
                    {{ row.status === '1' ? '已核验' : '待核验' }}
                  </el-tag>
                  <span v-else>{{ row[col.prop] }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center" fixed="right">
                <template #default="{ row }">
                  <span class="action-btn action-edit" @click="openEditDialog(tab, row)">编辑</span>
                  <span class="action-btn action-del" v-hasPermi="[tab.removePerm]"
                        @click="handleRemove(tab, row)">删除</span>
                </template>
              </el-table-column>
            </el-table>
            </div>

            <div v-if="!tabData[tab.name]?.length" class="empty-hint">
              暂无数据，请点击"新增"录入
            </div>
            <EmsPageBar
              :total="tabData[tab.name]?.length ?? 0"
              v-model:page="tabPageNum[tab.name]"
              v-model:page-size="tabPageSize[tab.name]"
              @change="onPageSizeChange(tab.name)"
            />
          </div>
        </template>
      </div>
    </div>

    <!-- 未选产品时的空状态 -->
    <div class="pcf-empty" v-else>
      <el-empty description="请从左侧选择一个产品" />
    </div>

    <!-- ══ 新增/编辑弹窗 ══ -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="680px"
      destroy-on-close
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="140px" size="small">
        <el-row :gutter="16">
          <el-col :span="12" v-for="field in currentTabDef?.formFields" :key="field.prop">
            <el-form-item :label="field.label" :prop="field.prop">
              <el-select v-if="field.type === 'select'" v-model="formData[field.prop]"
                         style="width:100%" :placeholder="'请选择' + field.label">
                <el-option v-for="o in field.options" :key="o" :label="o" :value="o" />
              </el-select>
              <el-input-number v-else-if="field.type === 'number'" v-model="formData[field.prop]"
                               :precision="4" style="width:100%" />
              <el-date-picker v-else-if="field.type === 'date'" v-model="formData[field.prop]"
                              type="date" value-format="YYYY-MM-DD" style="width:100%"
                              :placeholder="'请选择' + field.label" />
              <el-input v-else v-model="formData[field.prop]" :placeholder="'请输入' + field.label" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- ══ 报告预览弹窗 ══ -->
    <el-dialog v-model="reportVisible" title="产品碳足迹报告预览" width="900px"
               v-loading="reportLoading" destroy-on-close>
      <div class="report-body" ref="reportBodyRef">

        <!-- 报告头：编号 + 品牌 -->
        <div class="rpt-head">
          <div class="rpt-meta">
            报告编号：{{ reportNo }}<br>
            报告日期：{{ reportDate }}<br>
            核算标准：{{ currentProduct?.accountingStandard }}
          </div>
        </div>

        <h2 class="rpt-title">产品碳足迹报告</h2>

        <!-- 摘要信息栏 -->
        <div class="rpt-highlight">
          <div class="rpt-hl-text">
            <b>产品名称：</b>{{ currentProduct?.productName }} &nbsp;|&nbsp;
            <b>规格型号：</b>{{ currentProduct?.specModel }} &nbsp;|&nbsp;
            <b>单位：</b>{{ currentProduct?.unit }}<br>
            <b>生命周期边界：</b>{{ currentProduct?.lifecycleBoundary }} &nbsp;|&nbsp;
            <b>核算年份：</b>{{ currentYear }}年<br>
            <b>核算标准：</b>{{ currentProduct?.accountingStandard }}
          </div>
          <div class="rpt-hl-value-wrap">
            <div class="rpt-hl-label">产品碳足迹总量</div>
            <div class="rpt-hl-value">{{ lifecycle.totalFootprint }}</div>
            <div class="rpt-hl-unit">tCO₂e/{{ currentProduct?.unit }}</div>
          </div>
        </div>

        <!-- 一、生命周期各阶段碳排放汇总 -->
        <div class="rpt-section">一、生命周期各阶段碳排放汇总</div>
        <table class="rpt-table">
          <thead><tr><th>序号</th><th>生命周期阶段</th><th>碳排放量</th><th>占比</th><th>单位</th></tr></thead>
          <tbody>
            <tr v-for="(s, i) in lifecycle.stages" :key="i">
              <td>{{ i + 1 }}</td><td>{{ s.stageName }}</td>
              <td>{{ s.amount }}</td><td>{{ s.percentage }}%</td><td>tCO₂e</td>
            </tr>
            <tr class="tr-total">
              <td colspan="2">合计</td>
              <td>{{ lifecycle.totalFootprint }}</td><td>100%</td>
              <td>tCO₂e/{{ currentProduct?.unit }}</td>
            </tr>
          </tbody>
        </table>

        <!-- 二、原材料获取阶段明细（前5项） -->
        <div class="rpt-section">二、原材料获取阶段明细（前5项）</div>
        <table class="rpt-table">
          <thead><tr><th>序号</th><th>材料名称</th><th>规格型号</th><th>数量</th><th>单位</th><th>排放因数</th><th>碳足迹</th><th>单位</th></tr></thead>
          <tbody>
            <template v-if="(tabData.raw || []).length">
              <tr v-for="(r, i) in (tabData.raw || []).slice(0, 5)" :key="i">
                <td>{{ i+1 }}</td><td>{{ r.materialName }}</td><td>{{ r.specModel }}</td>
                <td>{{ r.quantity }}</td><td>{{ r.unit }}</td>
                <td>{{ r.emissionFactor }}</td><td>{{ r.emissionAmount }}</td><td>tCO₂e</td>
              </tr>
              <tr v-if="(tabData.raw||[]).length > 5">
                <td colspan="6" style="text-align:right;color:#888;font-size:11px">…共{{ tabData.raw.length }}项，详见附件…</td>
                <td>{{ tabTotals.raw }}</td><td>tCO₂e</td>
              </tr>
            </template>
            <tr v-else><td colspan="8" class="td-empty">本阶段暂无数据录入，碳排放量计为 0.00 tCO₂e/{{ currentProduct?.unit }}</td></tr>
            <tr class="tr-total"><td colspan="6">合计</td><td>{{ tabTotals.raw ?? '0.00' }}</td><td>tCO₂e/{{ currentProduct?.unit }}</td></tr>
          </tbody>
        </table>

        <!-- 三、生产制造 -->
        <div class="rpt-section">三、生产制造</div>
        <table class="rpt-table">
          <thead><tr><th>序号</th><th>排放源/活动设施</th><th>活动数据种类</th><th>排放数量</th><th>单位</th><th>统计周期</th><th>碳迹值</th><th>单位</th></tr></thead>
          <tbody>
            <template v-if="(tabData.mfg || []).length">
              <tr v-for="(r, i) in tabData.mfg" :key="i">
                <td>{{ i+1 }}</td><td>{{ r.emissionSource }}</td><td>{{ r.activityType }}</td>
                <td>{{ r.activityValue }}</td><td>{{ r.unit }}</td>
                <td>{{ r.statPeriodStart }}~{{ r.statPeriodEnd }}</td>
                <td>{{ r.carbonValue }}</td><td>kgCO₂e/{{ currentProduct?.unit }}</td>
              </tr>
            </template>
            <tr v-else><td colspan="8" class="td-empty">本阶段暂无数据录入，碳排放量计为 0.00 tCO₂e/{{ currentProduct?.unit }}</td></tr>
            <tr class="tr-total"><td colspan="6">合计</td><td>{{ tabTotals.mfg ?? '0.00' }}</td><td>tCO₂e/{{ currentProduct?.unit }}</td></tr>
          </tbody>
        </table>

        <!-- 四、物流/仓储/运输 -->
        <div class="rpt-section">四、物流/仓储/运输</div>
        <table class="rpt-table">
          <thead><tr><th>序号</th><th>材料/货物名称</th><th>规格型号</th><th>运输距离</th><th>单位</th><th>运输方式</th><th>排放因数</th><th>碳足迹</th><th>单位</th></tr></thead>
          <tbody>
            <template v-if="(tabData.logistics || []).length">
              <tr v-for="(r, i) in tabData.logistics" :key="i">
                <td>{{ i+1 }}</td><td>{{ r.materialName }}</td><td>{{ r.specModel }}</td>
                <td>{{ r.transportDist }}</td><td>{{ r.distUnit }}</td><td>{{ r.transportMode }}</td>
                <td>{{ r.emissionFactor }}</td><td>{{ r.carbonFootprint }}</td><td>tCO₂e</td>
              </tr>
            </template>
            <tr v-else><td colspan="9" class="td-empty">本阶段暂无数据录入，碳排放量计为 0.00 tCO₂e/{{ currentProduct?.unit }}</td></tr>
            <tr class="tr-total"><td colspan="7">合计</td><td>{{ tabTotals.logistics ?? '0.00' }}</td><td>tCO₂e/{{ currentProduct?.unit }}</td></tr>
          </tbody>
        </table>

        <!-- 五、产品使用 -->
        <div class="rpt-section">五、产品使用</div>
        <table class="rpt-table">
          <thead><tr><th>序号</th><th>能耗/材料名称</th><th>规格型号</th><th>年消耗量</th><th>单位</th><th>使用年限(年)</th><th>排放因数</th><th>碳足迹</th><th>单位</th></tr></thead>
          <tbody>
            <template v-if="(tabData.use || []).length">
              <tr v-for="(r, i) in tabData.use" :key="i">
                <td>{{ i+1 }}</td><td>{{ r.materialName }}</td><td>{{ r.specModel }}</td>
                <td>{{ r.annualConsume }}</td><td>{{ r.unit }}</td><td>{{ r.usefulLife }}</td>
                <td>{{ r.emissionFactor }}</td><td>{{ r.carbonFootprint }}</td><td>tCO₂e</td>
              </tr>
            </template>
            <tr v-else><td colspan="9" class="td-empty">本阶段暂无数据录入，碳排放量计为 0.00 tCO₂e/{{ currentProduct?.unit }}</td></tr>
            <tr class="tr-total"><td colspan="7">合计</td><td>{{ tabTotals.use ?? '0.00' }}</td><td>tCO₂e/{{ currentProduct?.unit }}</td></tr>
          </tbody>
        </table>

        <!-- 六、废弃回收 -->
        <div class="rpt-section">六、废弃回收</div>
        <table class="rpt-table">
          <thead><tr><th>序号</th><th>废弃物/回收材料</th><th>处理方式</th><th>重量/数量</th><th>单位</th><th>排放因数</th><th>因数单位</th><th>碳足迹</th><th>单位</th></tr></thead>
          <tbody>
            <template v-if="(tabData.eol || []).length">
              <tr v-for="(r, i) in tabData.eol" :key="i">
                <td>{{ i+1 }}</td><td>{{ r.wasteMaterial }}</td><td>{{ r.disposalMethod }}</td>
                <td>{{ r.weight }}</td><td>{{ r.unit }}</td>
                <td>{{ r.emissionFactor }}</td><td>{{ r.factorUnit }}</td>
                <td>{{ r.carbonFootprint }}</td><td>tCO₂e</td>
              </tr>
            </template>
            <tr v-else><td colspan="9" class="td-empty">本阶段暂无数据录入，碳排放量计为 0.00 tCO₂e/{{ currentProduct?.unit }}</td></tr>
            <tr class="tr-total"><td colspan="7">合计</td><td>{{ tabTotals.eol ?? '0.00' }}</td><td>tCO₂e/{{ currentProduct?.unit }}</td></tr>
          </tbody>
        </table>

        <!-- 七、绿电绿证抵消 -->
        <div class="rpt-section">七、绿电绿证抵消</div>
        <table class="rpt-table">
          <thead><tr><th>绿证类型</th><th>采购量(MWh)</th><th>发电类型</th><th>折算减排(tCO₂e)</th><th>状态</th></tr></thead>
          <tbody>
            <template v-if="(tabData.cert || []).length">
              <tr v-for="(r, i) in tabData.cert" :key="i">
                <td>{{ r.certType }}</td><td>{{ r.purchaseVolume }}</td>
                <td>{{ r.powerType }}</td><td>{{ r.emissionReduction }}</td>
                <td>{{ r.status === '1' ? '已核验' : '待核验' }}</td>
              </tr>
              <tr class="tr-total">
                <td>合计</td>
                <td>{{ (tabData.cert||[]).reduce((s,r)=>s+(Number(r.purchaseVolume)||0),0) }}</td>
                <td>—</td><td>{{ lifecycle.greenCertOffset }}</td><td>—</td>
              </tr>
            </template>
            <tr v-else><td colspan="5" class="td-empty">本阶段暂无绿电绿证数据</td></tr>
          </tbody>
        </table>
        <div class="rpt-net">
          <span>净碳足迹（扣除绿证抵消后）</span>
          <div style="text-align:right">
            <div class="rpt-net-val">{{ lifecycle.netFootprint }} tCO₂e/{{ currentProduct?.unit }}</div>
            <div v-if="lifecycle.greenCertOffset > 0" class="rpt-net-sub">较总量降低 {{ reductionPct }}%</div>
          </div>
        </div>

        <!-- 八、核算声明 -->
        <div class="rpt-section">八、核算声明</div>
        <div class="rpt-disclaimer">
          1. 本报告依据 <b>{{ currentProduct?.accountingStandard }}</b>《温室气体——产品碳足迹——量化要求和指南》及相关标准核算。<br>
          2. 系统边界为{{ currentProduct?.lifecycleBoundary }}，涵盖原材料获取、生产制造、物流运输、产品使用及废弃回收全阶段。<br>
          3. 排放因子来源：国家温室气体排放因子数据库（2024版）、ecoinvent 3.9、IPCC AR6。<br>
          4. 绿证减排量按中国绿色电力证书（GEC）及国际可再生能源证书（I-REC）规则计算。<br>
          5. 本报告核算结果可作为产品碳标识认证及企业供应链碳信息披露的依据。
        </div>

        <div class="rpt-footer">
          报告编号：{{ reportNo }} &nbsp;|&nbsp; 生成时间：{{ reportDate }}
        </div>
      </div>
      <template #footer>
        <el-button @click="reportVisible = false">关闭</el-button>
        <el-button @click="handlePrint">打印</el-button>
        <el-button type="primary" @click="exportPdf">导出 PDF</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProductCarbonFootprint">
import { ref, reactive, computed, onMounted, onActivated, nextTick } from 'vue'
import html2pdf from 'html2pdf.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategoryTree, listProductsByCategory,
  getLifecycleOverview,
  listRawMaterial,    saveRawMaterial,    removeRawMaterial,
  listManufacturing,  saveManufacturing,  removeManufacturing,
  listLogistics,      saveLogistics,      removeLogistics,
  listProductUse,     saveProductUse,     removeProductUse,
  listEol,            saveEol,            removeEol,
  listGreenCert,      saveGreenCert,      removeGreenCert
} from '@/api/productCarbonFootprint/index'

const route = useRoute()

/* ─── 状态 ─── */
const treeRef       = ref(null)
const categoryTree  = ref([])
const currentProduct = ref(null)
const currentYear   = ref(String(new Date().getFullYear()))
const activeTab     = ref('lifecycle')

const lifecycle  = ref({ stages: [], totalFootprint: 0, netFootprint: 0, greenCertOffset: 0 })
const tabData    = reactive({})
const tabLoading = reactive({})
const tabCounts  = reactive({ raw: 0, mfg: 0, logistics: 0, use: 0, eol: 0, cert: 0 })
const tabTotals  = reactive({})

// 分页状态：每个 tab 独立维护当前页 & 每页条数
const TAB_NAMES = ['raw', 'mfg', 'logistics', 'use', 'eol', 'cert']
const tabPageNum  = reactive(Object.fromEntries(TAB_NAMES.map(n => [n, 1])))
const tabPageSize = reactive(Object.fromEntries(TAB_NAMES.map(n => [n, 10])))

// 返回当前页切片数据
function pagedData(name) {
  const all  = tabData[name] || []
  const start = (tabPageNum[name] - 1) * tabPageSize[name]
  return all.slice(start, start + tabPageSize[name])
}
// 切换每页条数时重置到第 1 页
function onPageSizeChange(name) {
  tabPageNum[name] = 1
}

/* ─── Tab 定义 ─── */
const dataTabs = [
  {
    name: 'raw', label: '原材料获取', addPerm: 'pcf:raw:add',
    importPerm: 'pcf:raw:import', removePerm: 'pcf:raw:remove',
    listFn: listRawMaterial, saveFn: saveRawMaterial, removeFn: removeRawMaterial,
    totalField: 'emissionAmount',
    columns: [
      { prop: 'materialName', label: '材料/零件名称', width: 140 },
      { prop: 'specModel',    label: '规格型号',     width: 100 },
      { prop: 'quantity',     label: '数量',         width: 80  },
      { prop: 'unit',         label: '单位',         width: 60  },
      { prop: 'emissionFactor', label: '排放因数',   width: 90  },
      { prop: 'factorUnit',   label: '因数单位',     width: 110 },
      { prop: 'emissionAmount', label: '本项碳足迹', width: 100 },
      { prop: 'emissionUnit', label: '单位',         width: 70  },
      { prop: 'dataSource',   label: '数据来源',     width: 110 },
    ],
    formFields: [
      { prop: 'materialName',   label: '材料名称',   type: 'text' },
      { prop: 'specModel',      label: '规格型号',   type: 'text' },
      { prop: 'quantity',       label: '数量',       type: 'number' },
      { prop: 'unit',           label: '单位', type: 'select',
        options: ['吨','kg','g','个','件','m','km','m²','m³','L'] },
      { prop: 'emissionFactor', label: '排放因数', type: 'number' },
      { prop: 'factorUnit',     label: '因数单位', type: 'text' },
      { prop: 'dataSource',     label: '数据来源', type: 'select',
        options: ['国家因子库(2024)','ecoinvent 3.9','IPCC AR6','供应商实测值','其他'] },
    ]
  },
  {
    name: 'mfg', label: '生产制造', addPerm: 'pcf:mfg:add',
    importPerm: 'pcf:mfg:add', removePerm: 'pcf:mfg:remove',
    listFn: listManufacturing, saveFn: saveManufacturing, removeFn: removeManufacturing,
    totalField: 'carbonValue',
    columns: [
      { prop: 'emissionSource', label: '排放源/活动设施', width: 130 },
      { prop: 'activityType',   label: '活动数据种类',   width: 110 },
      { prop: 'activityValue',  label: '排放数量',        width: 100 },
      { prop: 'unit',           label: '单位',            width: 70  },
      { prop: 'statPeriodStart', label: '统计开始',       width: 90  },
      { prop: 'statPeriodEnd',   label: '统计结束',       width: 90  },
      { prop: 'productionVolume', label: '产品产量',      width: 90  },
      { prop: 'productionUnit', label: '产量单位',        width: 80  },
      { prop: 'carbonValue',    label: '碳迹值',          width: 90  },
      { prop: 'carbonUnit',     label: '碳排放单位',      width: 110 },
    ],
    formFields: [
      { prop: 'emissionSource',  label: '排放源/活动设施', type: 'text' },
      { prop: 'activityType',    label: '活动数据种类', type: 'select',
        options: ['电力','热力','天然气','煤','柴油','汽油','蒸汽','其他'] },
      { prop: 'activityValue',   label: '排放数量', type: 'number' },
      { prop: 'unit',            label: '单位', type: 'select',
        options: ['KWh','GJ','Nm³','kg','t','L'] },
      { prop: 'statPeriodStart', label: '统计开始', type: 'date' },
      { prop: 'statPeriodEnd',   label: '统计结束', type: 'date' },
      { prop: 'productionVolume', label: '产品产量', type: 'number' },
      { prop: 'productionUnit',  label: '产量单位', type: 'select',
        options: ['台','件','套','kg','t'] },
      { prop: 'carbonValue',     label: '碳迹值(kgCO₂e/台)', type: 'number' },
    ]
  },
  {
    name: 'logistics', label: '物流运输', addPerm: 'pcf:log:add',
    importPerm: 'pcf:log:add', removePerm: 'pcf:log:remove',
    listFn: listLogistics, saveFn: saveLogistics, removeFn: removeLogistics,
    totalField: 'carbonFootprint',
    columns: [
      { prop: 'materialName',   label: '材料/货物名称', width: 120 },
      { prop: 'specModel',      label: '规格型号',      width: 90  },
      { prop: 'transportDist',  label: '运输距离',      width: 90  },
      { prop: 'distUnit',       label: '单位',          width: 60  },
      { prop: 'transportMode',  label: '运输方式',      width: 90, slot: 'transport' },
      { prop: 'emissionFactor', label: '排放因数',      width: 90  },
      { prop: 'factorUnit',     label: '因数单位',      width: 120 },
      { prop: 'carbonFootprint', label: '本项碳足迹',   width: 100 },
      { prop: 'carbonUnit',     label: '碳足迹单位',    width: 80  },
    ],
    formFields: [
      { prop: 'materialName',  label: '材料/货物名称', type: 'text' },
      { prop: 'specModel',     label: '规格型号',      type: 'text' },
      { prop: 'transportDist', label: '运输距离',      type: 'number' },
      { prop: 'distUnit',      label: '距离单位', type: 'select', options: ['km','m'] },
      { prop: 'transportMode', label: '运输方式', type: 'select',
        options: ['公路运输','铁路运输','航运/海运','空运','内河运输','管道运输'] },
      { prop: 'emissionFactor', label: '排放因数', type: 'number' },
      { prop: 'factorUnit',    label: '因数单位',  type: 'text' },
    ]
  },
  {
    name: 'use', label: '产品使用', addPerm: 'pcf:footprint:list',
    importPerm: 'pcf:footprint:list', removePerm: 'pcf:footprint:list',
    listFn: listProductUse, saveFn: saveProductUse, removeFn: removeProductUse,
    totalField: 'carbonFootprint',
    columns: [
      { prop: 'materialName',   label: '能耗/材料名称', width: 130 },
      { prop: 'specModel',      label: '规格型号',      width: 90  },
      { prop: 'usefulLife',     label: '使用年限(年)',  width: 100 },
      { prop: 'annualConsume',  label: '年消耗量',      width: 90  },
      { prop: 'unit',           label: '单位',          width: 60  },
      { prop: 'emissionFactor', label: '排放因数',      width: 90  },
      { prop: 'factorUnit',     label: '因数单位',      width: 110 },
      { prop: 'carbonFootprint', label: '本项碳足迹',   width: 100 },
      { prop: 'carbonUnit',     label: '碳足迹单位',    width: 80  },
    ],
    formFields: [
      { prop: 'materialName',  label: '能耗/材料名称', type: 'text' },
      { prop: 'specModel',     label: '规格型号',      type: 'text' },
      { prop: 'usefulLife',    label: '使用年限(年)',  type: 'number' },
      { prop: 'annualConsume', label: '年消耗量',      type: 'number' },
      { prop: 'unit',          label: '单位', type: 'select',
        options: ['KWh','GJ','L','kg','件'] },
      { prop: 'emissionFactor', label: '排放因数', type: 'number' },
      { prop: 'factorUnit',    label: '因数单位',  type: 'text' },
      { prop: 'carbonFootprint', label: '本项碳足迹', type: 'number' },
    ]
  },
  {
    name: 'eol', label: '废弃回收', addPerm: 'pcf:footprint:list',
    importPerm: 'pcf:footprint:list', removePerm: 'pcf:footprint:list',
    listFn: listEol, saveFn: saveEol, removeFn: removeEol,
    totalField: 'carbonFootprint',
    columns: [
      { prop: 'wasteMaterial',  label: '废弃物/回收材料', width: 130 },
      { prop: 'disposalMethod', label: '处理方式',         width: 90  },
      { prop: 'weight',         label: '重量/数量',        width: 90  },
      { prop: 'unit',           label: '单位',             width: 60  },
      { prop: 'emissionFactor', label: '排放因数',         width: 90  },
      { prop: 'factorUnit',     label: '因数单位',         width: 110 },
      { prop: 'carbonFootprint', label: '本项碳足迹',      width: 100 },
      { prop: 'carbonUnit',     label: '碳足迹单位',       width: 80  },
      { prop: 'isRecycled',     label: '是否回收减排',     width: 100 },
    ],
    formFields: [
      { prop: 'wasteMaterial',  label: '废弃物/回收材料', type: 'text' },
      { prop: 'disposalMethod', label: '处理方式', type: 'select',
        options: ['填埋','焚烧','回收再利用','拆解处理','危废处置'] },
      { prop: 'weight',        label: '重量/数量', type: 'number' },
      { prop: 'unit',          label: '单位', type: 'select', options: ['kg','t','件','L'] },
      { prop: 'emissionFactor', label: '排放因数', type: 'number' },
      { prop: 'factorUnit',    label: '因数单位',  type: 'text' },
      { prop: 'isRecycled',    label: '是否回收减排', type: 'select', options: ['是','否'] },
    ]
  },
  {
    name: 'cert', label: '绿电绿证', addPerm: 'pcf:cert:add',
    importPerm: 'pcf:cert:add', removePerm: 'pcf:cert:remove',
    listFn: listGreenCert, saveFn: saveGreenCert, removeFn: removeGreenCert,
    totalField: 'emissionReduction',
    columns: [
      { prop: 'certType',         label: '绿证类型',           width: 110 },
      { prop: 'certNo',           label: '证书编号',           width: 160 },
      { prop: 'powerType',        label: '发电类型',           width: 80  },
      { prop: 'purchaseVolume',   label: '采购量(MWh)',        width: 100 },
      { prop: 'convertedPower',   label: '折算电量(KWh)',      width: 110 },
      { prop: 'emissionFactor',   label: '减排系数',           width: 100 },
      { prop: 'emissionReduction', label: '折算减排(tCO₂e)',  width: 130 },
      { prop: 'validityStart',    label: '有效期开始',         width: 90  },
      { prop: 'validityEnd',      label: '有效期结束',         width: 90  },
      { prop: 'status',           label: '状态',   width: 80, slot: 'certStatus' },
    ],
    formFields: [
      { prop: 'certType',       label: '绿证类型', type: 'select',
        options: ['I-REC','绿电证书(GEC)','GO(欧盟)','RECs(北美)','其他'] },
      { prop: 'certNo',         label: '证书编号',     type: 'text' },
      { prop: 'powerType',      label: '发电类型', type: 'select',
        options: ['光伏','风电','水电','生物质','地热','核能'] },
      { prop: 'purchaseVolume', label: '采购量(MWh)',  type: 'number' },
      { prop: 'emissionFactor', label: '减排系数(tCO₂e/MWh)', type: 'number' },
      { prop: 'validityStart',  label: '有效期开始',  type: 'date' },
      { prop: 'validityEnd',    label: '有效期结束',  type: 'date' },
    ]
  }
]

/* ─── 弹窗 ─── */
const reportBodyRef = ref(null)
const dialogVisible = ref(false)
const dialogTitle   = ref('')
const formData      = ref({})
const formRef       = ref(null)
const formRules     = {}
const saving        = ref(false)
const currentTabDef = ref(null)
const reportVisible = ref(false)
const reportLoading = ref(false)

/* ─── 报告计算值 ─── */
const reportNo = computed(() => {
  if (!currentProduct.value) return ''
  const code = String(currentProduct.value.specModel || currentProduct.value.id)
    .replace(/[^a-zA-Z0-9]/g, '').toUpperCase().slice(0, 8)
  return `PCF-${currentYear.value}-${code}-001`
})
const reportDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})
const reductionPct = computed(() => {
  const total  = Number(lifecycle.value.totalFootprint)
  const offset = Number(lifecycle.value.greenCertOffset)
  if (!total || !offset) return '0.00'
  return ((offset / total) * 100).toFixed(2)
})

/* ─── 阶段颜色（对齐原型） ─── */
const STAGE_COLORS = [
  { bar: 'linear-gradient(90deg,#0066cc,#00aaff)', value: '#33ccff', badge: 'badge-blue',   label: '原材料' },
  { bar: 'linear-gradient(90deg,#cc6600,#ff9240)', value: '#ffaa66', badge: 'badge-orange', label: '生产' },
  { bar: 'linear-gradient(90deg,#cc9900,#ffcc44)', value: '#ffcc44', badge: 'badge-yellow', label: '物流' },
  { bar: 'linear-gradient(90deg,#8800cc,#cc88ff)', value: '#cc88ff', badge: 'badge-purple', label: '使用' },
  { bar: 'linear-gradient(90deg,#cc0033,#ff4d6a)', value: '#ff8a9a', badge: 'badge-red',    label: '回收' },
]
function stageBarGradient(idx) { return (STAGE_COLORS[idx] || STAGE_COLORS[0]).bar }
function stageValueColor(idx)  { return (STAGE_COLORS[idx] || STAGE_COLORS[0]).value }
function stageBadgeClass(idx)  { return (STAGE_COLORS[idx] || STAGE_COLORS[0]).badge }

/* ─── 初始化 ─── */
onMounted(loadCategoryTree)

// keep-alive 激活时：若携带了新的 productId，则在已有树中重新定位
onActivated(() => {
  const targetId = route.query.productId || null
  if (!targetId) return
  if (String(currentProduct.value?.id) === String(targetId)) return
  const flat = flattenTree(categoryTree.value)
  const target = flat.find(n => n._isProduct && String(n.id) === String(targetId))
  if (target) selectProduct(target)
  else loadCategoryTree()
})

async function loadCategoryTree() {
  const { data } = await getCategoryTree()
  const tree = data || []
  // route.query 永远是字符串，保持 string 类型，比较时统一用 String()
  const targetId = route.query.productId || null
  await injectProducts(tree, targetId)
  categoryTree.value = tree
}

function flattenTree(nodes, list = []) {
  nodes.forEach(n => { list.push(n); if (n.children) flattenTree(n.children, list) })
  return list
}

async function injectProducts(nodes, targetId = null) {
  for (const node of nodes) {
    if (!node.children || !node.children.length) {
      const { data: products } = await listProductsByCategory(node.id)
      if (products && products.length) {
        node.children = products.map(p => ({ ...p, _isProduct: true }))
        if (targetId) {
          // String() 统一类型，避免 "42" !== 42 的比较失败
          const target = node.children.find(p => String(p.id) === String(targetId))
          if (target && !currentProduct.value) selectProduct(target)
        } else if (!currentProduct.value) {
          selectProduct(products[0])
        }
      }
    } else {
      await injectProducts(node.children, targetId)
    }
  }
}

function onTreeNodeClick(data) {
  if (data._isProduct) selectProduct(data)
}

async function selectProduct(p) {
  currentProduct.value = p
  activeTab.value = 'lifecycle'
  TAB_NAMES.forEach(n => { tabPageNum[n] = 1 })   // 切换产品时重置所有分页
  nextTick(() => treeRef.value?.setCurrentKey(p.id))
  await Promise.all([loadCurrentTabData(), loadAllTabCounts()])
}

async function onYearChange() {
  await Promise.all([loadCurrentTabData(), loadAllTabCounts()])
}

async function loadAllTabCounts() {
  if (!currentProduct.value) return
  const pid  = currentProduct.value.id
  const year = Number(currentYear.value)
  await Promise.all(dataTabs.map(tab =>
    tab.listFn(pid, year).then(({ data }) => {
      const list = data || []
      tabData[tab.name] = list
      const sum = list.reduce((acc, row) => acc + (Number(row[tab.totalField]) || 0), 0)
      tabTotals[tab.name] = sum.toFixed(4)
      tabCounts[tab.name] = list.length
    }).catch(() => {})
  ))
}

async function onTabChange(tab) {
  activeTab.value = tab
  await loadCurrentTabData()
}

async function loadCurrentTabData() {
  if (!currentProduct.value) return
  const pid  = currentProduct.value.id
  const year = Number(currentYear.value)

  if (activeTab.value === 'lifecycle') {
    const { data } = await getLifecycleOverview(pid, year)
    lifecycle.value = data || {}
    return
  }

  const tabDef = dataTabs.find(t => t.name === activeTab.value)
  if (!tabDef) return

  tabLoading[activeTab.value] = true
  try {
    const { data } = await tabDef.listFn(pid, year)
    tabData[activeTab.value] = data || []
    // 计算合计
    const list = tabData[activeTab.value]
    const sum  = list.reduce((acc, row) => acc + (Number(row[tabDef.totalField]) || 0), 0)
    tabTotals[activeTab.value] = sum.toFixed(4)
    // 更新badge计数
    if (['raw','mfg','logistics','use','eol','cert'].includes(activeTab.value)) {
      tabCounts[activeTab.value] = list.length
    }
  } finally {
    tabLoading[activeTab.value] = false
  }
}

/* ─── 新增 / 编辑 ─── */
function openAddDialog(tabDef) {
  currentTabDef.value = tabDef
  formData.value = { productId: currentProduct.value.id, year: Number(currentYear.value) }
  dialogTitle.value = '新增' + tabDef.label + '数据'
  dialogVisible.value = true
}

function openEditDialog(tabDef, row) {
  currentTabDef.value = tabDef
  formData.value = { ...row }
  dialogTitle.value = '编辑' + tabDef.label + '数据'
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await currentTabDef.value.saveFn(formData.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadCurrentTabData()
    // 刷新生命周期总览数据
    const { data } = await getLifecycleOverview(currentProduct.value.id, Number(currentYear.value))
    lifecycle.value = data || {}
  } finally {
    saving.value = false
  }
}

async function handleRemove(tabDef, row) {
  await ElMessageBox.confirm('确认删除该条数据？', '提示', { type: 'warning' })
  await tabDef.removeFn(row.id)
  ElMessage.success('删除成功')
  await loadCurrentTabData()
  const { data } = await getLifecycleOverview(currentProduct.value.id, Number(currentYear.value))
  lifecycle.value = data || {}
}

function handleImport(tabDef) {
  ElMessage.info('Excel导入功能开发中')
}

function handleDownloadTemplate(tabDef) {
  ElMessage.info('模板下载功能开发中')
}

async function openReportDialog() {
  reportVisible.value = true
  reportLoading.value = true
  try {
    const pid  = currentProduct.value.id
    const year = Number(currentYear.value)
    await Promise.all([
      getLifecycleOverview(pid, year).then(({ data }) => { lifecycle.value = data || {} }),
      ...dataTabs.map(tab =>
        tab.listFn(pid, year).then(({ data }) => {
          tabData[tab.name] = data || []
          const sum = (data || []).reduce((acc, row) => acc + (Number(row[tab.totalField]) || 0), 0)
          tabTotals[tab.name] = sum.toFixed(4)
        })
      )
    ])
  } finally {
    reportLoading.value = false
  }
}

function handlePrint() {
  window.print()
}

async function exportPdf() {
  const el = reportBodyRef.value
  if (!el) return

  const filename = `产品碳足迹报告_${currentProduct.value?.productName || ''}_${currentYear.value}.pdf`

  await html2pdf()
    .set({
      margin:      [12, 14, 12, 14],
      filename,
      image:       { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true, logging: false, backgroundColor: '#ffffff' },
      jsPDF:       { unit: 'mm', format: 'a4', orientation: 'portrait' },
      pagebreak:   { mode: ['avoid-all', 'css', 'legacy'] }
    })
    .from(el)
    .save()
}
</script>

<style scoped>
/* ─── Scrollbar ─── */
:deep(.el-scrollbar__bar .el-scrollbar__thumb) { background: rgba(0,140,220,.25); border-radius: 3px; }
:deep(.el-scrollbar__bar .el-scrollbar__thumb:hover) { background: rgba(0,170,255,.4); }

/* ── CSS Variables ── */
.pcf-page {
  --bg0:   #040b1a;
  --bg1:   #080f22;
  --bg2:   #0a1528;
  --bg3:   #0b1530;
  --bd0:   rgba(0,140,220,.12);
  --bd1:   rgba(0,170,255,.20);
  --bd2:   rgba(0,200,255,.35);
  --cyan:  #00aaff;
  --cyan2: #33ccff;
  --green: #00e896;
  --txt0:  #d8e8f8;
  --txt1:  #6e90b8;
  --txt2:  #3a5878;
}

.pcf-page {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: var(--bg1);
}

/* ─── 侧边栏 ─── */
.pcf-sidebar {
  width: 220px;
  min-width: 220px;
  background: var(--bg0);
  border-right: 1px solid var(--bd0);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px 10px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--txt1);
  letter-spacing: 1px;
  text-transform: uppercase;
  border-bottom: 1px solid var(--bd0);
  flex-shrink: 0;
}
.sidebar-scroll { flex: 1; }
.sidebar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 30px 16px;
  color: var(--txt2);
  font-size: 12px;
  text-align: center;
}
.tree-node-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  width: 100%;
  color: var(--txt1);
}
.active-product { color: var(--cyan2) !important; font-weight: 600; }
.node-icon-product { color: var(--cyan); flex-shrink: 0; }
.node-icon-folder  { color: #e6a23c; flex-shrink: 0; }
.count-tag { margin-left: auto; flex-shrink: 0; }

/* el-tree dark overrides */
:deep(.el-tree) {
  background: transparent;
  color: var(--txt1);
}
:deep(.el-tree-node__content) {
  background: transparent !important;
  color: var(--txt1);
}
:deep(.el-tree-node__content:hover) {
  background: rgba(0,170,255,.06) !important;
  color: var(--txt0);
}
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: rgba(0,170,255,.12) !important;
  color: var(--cyan2);
}
:deep(.el-tree-node__expand-icon) { color: var(--txt2); }

/* ─── 主内容 ─── */
.pcf-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--bg1);
}
.pcf-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg1);
}
:deep(.pcf-empty .el-empty__description p) { color: var(--txt1); }

/* ─── 产品头部 ─── */
.product-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 20px;
  background: var(--bg2);
  border-bottom: 1px solid var(--bd1);
  flex-wrap: wrap;
  flex-shrink: 0;
  position: relative;
  min-height: 64px;
}
.product-header::after {
  content: '';
  position: absolute;
  left: 0; bottom: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, var(--cyan), transparent);
  opacity: .35;
}
.product-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  flex-wrap: wrap;
}
.meta-item { display: flex; flex-direction: column; gap: 1px; }
.meta-label { font-size: 10px; color: var(--txt2); letter-spacing: .5px; }
.meta-value { font-size: 13px; color: var(--txt0); font-weight: 500; }
.meta-value.highlight { color: var(--cyan2); }

/* el-divider dark */
:deep(.el-divider--vertical) { border-color: var(--bd0); height: 30px; }

/* Year picker dark */
:deep(.product-header .el-date-editor .el-input__wrapper) {
  background: rgba(0,0,0,.2);
  border: 1px solid var(--bd1);
  box-shadow: none;
}
:deep(.product-header .el-date-editor .el-input__inner) {
  color: var(--txt0);
  font-size: 13px;
}

.carbon-total {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 18px;
  background: linear-gradient(135deg, rgba(0,100,200,.15), rgba(0,180,255,.08));
  border: 1px solid var(--bd2);
  border-radius: 8px;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}
.carbon-total::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(0,170,255,.1) 0%, transparent 70%);
}
.carbon-orb {
  width: 46px; height: 46px;
  background: radial-gradient(circle, rgba(0,170,255,.2), rgba(0,100,200,.1));
  border: 1px solid rgba(0,170,255,.3);
  border-radius: 50%;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  flex-shrink: 0;
}
.carbon-orb-label { font-size: 8px; font-weight: 700; color: var(--cyan2); letter-spacing: .5px; }
.carbon-total-info { display: flex; flex-direction: column; align-items: flex-end; }
.total-label { font-size: 10px; color: var(--txt1); letter-spacing: .5px; }
.total-value {
  font-size: 28px; font-weight: 700;
  color: var(--cyan2); line-height: 1.1;
  text-shadow: 0 0 20px rgba(0,200,255,.4);
}
.net-hint { font-size: 11px; color: var(--txt1); text-align: right; margin-top: 2px; }
.green-val { color: var(--green); font-weight: 600; }

/* 生成报告按钮 */
:deep(.pcf-page .btn-report-wrap .el-button) {
  background: linear-gradient(135deg, #0060cc, #00aaff);
  border: none;
  color: #fff;
  box-shadow: 0 0 16px rgba(0,170,255,.2);
  font-weight: 500;
  letter-spacing: .5px;
}
:deep(.pcf-page .btn-report-wrap .el-button:hover) {
  background: linear-gradient(135deg, #0070dd, #00ccff);
  box-shadow: 0 0 28px rgba(0,170,255,.4);
  transform: translateY(-1px);
}

/* ─── Tabs ─── */
.pcf-tabs { padding: 0 20px; background: var(--bg2); flex-shrink: 0; }
:deep(.el-tabs__header) { margin-bottom: 0; }
:deep(.el-tabs__nav-wrap::after) { background-color: var(--bd0); }
:deep(.el-tabs__item) {
  color: var(--txt1);
  font-size: 13px;
  padding: 11px 16px;
}
:deep(.el-tabs__item:hover) { color: var(--txt0); }
:deep(.el-tabs__item.is-active) { color: var(--cyan2); }
:deep(.el-tabs__active-bar) { background-color: var(--cyan); }
.tab-badge {
  display: inline-block;
  font-size: 10px;
  background: rgba(0,170,255,.15);
  color: var(--cyan);
  padding: 1px 5px;
  border-radius: 8px;
  margin-left: 4px;
  font-weight: 600;
  vertical-align: middle;
}
.tab-badge--green {
  background: rgba(0,232,150,.12);
  color: var(--green);
}

/* ─── Tab 内容 ─── */
.tab-content {
  flex: 1;
  overflow: auto;
  padding: 16px 20px;
  background: var(--bg1);
}

/* 生命周期总览 */
.lc-overview { display: flex; flex-direction: column; gap: 12px; }
.lc-stage-list {
  background: var(--bg3);
  border: 1px solid var(--bd0);
  border-radius: 8px;
  overflow: hidden;
}
.lc-card-title {
  padding: 10px 16px;
  font-size: 11px; font-weight: 600;
  color: var(--txt1);
  border-bottom: 1px solid var(--bd0);
  letter-spacing: .8px; text-transform: uppercase;
}
.lc-stage-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--bd0);
}
.lc-stage-row:last-child { border-bottom: none; }
.stage-idx {
  width: 22px; height: 22px;
  background: rgba(0,140,255,.1);
  border: 1px solid rgba(0,140,255,.2);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; color: var(--cyan); font-weight: 600; flex-shrink: 0;
}
.stage-name { min-width: 120px; font-size: 13px; color: var(--txt0); }
.stage-bar-wrap { flex: 2; height: 6px; background: rgba(255,255,255,.05); border-radius: 3px; overflow: hidden; }
.stage-bar { height: 100%; background: linear-gradient(90deg, var(--cyan), var(--cyan2)); border-radius: 3px; transition: width 0.8s; }
.stage-value { min-width: 72px; text-align: right; font-weight: 600; color: var(--cyan2); font-size: 15px; }
.stage-pct { min-width: 50px; text-align: right; font-size: 11px; color: var(--txt2); }
.lc-total-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px;
  background: rgba(0,170,255,.06);
  border-top: 1px solid var(--bd1);
  font-size: 13px; font-weight: 600; color: var(--txt0);
}
.total-num { font-size: 20px; color: var(--cyan2); }
.green-offset-bar {
  background: linear-gradient(135deg, rgba(0,232,150,.05), rgba(0,100,80,.05));
  border: 1px solid rgba(0,232,150,.15);
  border-radius: 8px; padding: 10px 16px;
  display: flex; align-items: center; justify-content: space-between;
  font-size: 13px; color: var(--txt0);
}
.net-total-bar {
  background: rgba(0,170,255,.06);
  border: 1px solid var(--bd1);
  border-radius: 8px; padding: 12px 16px;
  display: flex; align-items: center; justify-content: space-between;
  font-size: 13px; font-weight: 600; color: var(--txt0);
}
.net-num { font-size: 20px; color: var(--green); }

/* 数据面板 */
.data-panel {
  background: var(--bg3);
  border-radius: 8px;
  border: 1px solid var(--bd0);
  display: flex; flex-direction: column;
}
.panel-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 16px; border-bottom: 1px solid var(--bd0);
  background: rgba(0,0,0,.08); flex-shrink: 0;
}
.total-info { display: flex; align-items: baseline; gap: 5px; font-size: 12px; color: var(--txt1); }
.panel-total-label { font-size: 12px; color: var(--txt1); }
.total-big { font-size: 22px; font-weight: 600; color: var(--cyan2); }
.panel-total-unit { font-size: 12px; color: var(--txt2); }
.btn-group { display: flex; gap: 8px; }
.empty-hint { text-align: center; padding: 40px; color: var(--txt2); font-size: 13px; }

/* el-table dark */
:deep(.el-table) {
  background: transparent;
  color: var(--txt0);
}
:deep(.el-table tr) { background: transparent; }
:deep(.el-table th.el-table__cell) {
  background: #0a1830;
  color: var(--txt1);
  font-size: 11.5px;
  font-weight: 600;
  border-bottom: 1px solid var(--bd1);
}
:deep(.el-table td.el-table__cell) {
  background: transparent;
  color: var(--txt0);
  border-bottom: 1px solid var(--bd0);
}
:deep(.el-table__inner-wrapper::before) { background-color: transparent; }
:deep(.el-table__body tr:hover > td.el-table__cell) {
  background: rgba(0,140,255,.05) !important;
}
:deep(.el-table__body tr:nth-child(odd) > td.el-table__cell) {
  background: rgba(255,255,255,.008);
}
:deep(.el-table__empty-block) { background: transparent; }
:deep(.el-table__empty-text) { color: var(--txt2); }

/* action buttons in table */
:deep(.el-button.is-link.el-button--primary) { color: #66aaff; }
:deep(.el-button.is-link.el-button--primary:hover) { color: #88ddff; }
:deep(.el-button.is-link.el-button--danger) { color: #ff7a90; }
:deep(.el-button.is-link.el-button--danger:hover) { color: #ff9aaa; }

/* toolbar buttons */
:deep(.btn-group .el-button--primary) {
  background: rgba(0,140,255,.15);
  border-color: rgba(0,140,255,.4);
  color: #66ccff;
}
:deep(.btn-group .el-button--primary:hover) {
  background: rgba(0,140,255,.28);
  border-color: rgba(0,140,255,.7);
  color: #88ddff;
}
:deep(.btn-group .el-button:not(.el-button--primary)) {
  background: rgba(255,255,255,.04);
  border-color: var(--bd1);
  color: var(--txt1);
}
:deep(.btn-group .el-button:not(.el-button--primary):hover) {
  background: rgba(255,255,255,.08);
  color: var(--txt0);
}


/* el-loading dark */
:deep(.el-loading-mask) { background: rgba(4,11,26,.7); }
:deep(.el-loading-spinner .circular) { stroke: var(--cyan); }
:deep(.el-loading-spinner .path) { stroke: var(--cyan); }

/* ─── 弹窗 dark ─── */
:deep(.el-dialog) {
  background: #0c1833;
  border: 1px solid var(--bd2);
  border-radius: 8px;
  box-shadow: 0 24px 64px rgba(0,0,0,.6);
}
:deep(.el-dialog__header) {
  border-bottom: 1px solid var(--bd0);
  padding: 15px 20px;
}
:deep(.el-dialog__title) { font-size: 15px; color: var(--txt0); font-weight: 600; }
:deep(.el-dialog__headerbtn .el-dialog__close) { color: var(--txt1); }
:deep(.el-dialog__headerbtn:hover .el-dialog__close) { color: #ff6b85; }
:deep(.el-dialog__body) { padding: 12px 20px; }
:deep(.el-dialog__footer) {
  border-top: 1px solid var(--bd0);
  padding: 13px 20px;
}
:deep(.el-dialog__footer .el-button) {
  background: rgba(255,255,255,.04);
  border-color: var(--bd1);
  color: var(--txt1);
}
:deep(.el-dialog__footer .el-button:hover) {
  background: rgba(255,255,255,.08);
  color: var(--txt0);
}
:deep(.el-dialog__footer .el-button--primary) {
  background: rgba(0,140,255,.15);
  border-color: rgba(0,140,255,.4);
  color: #66ccff;
}
:deep(.el-dialog__footer .el-button--primary:hover) {
  background: rgba(0,140,255,.28);
  border-color: rgba(0,140,255,.7);
  color: #88ddff;
}

/* form dark */
:deep(.el-form-item__label) { color: var(--txt1); font-size: 12px; }
:deep(.el-input__wrapper) {
  background: rgba(0,0,0,.2) !important;
  box-shadow: 0 0 0 1px var(--bd1) !important;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--cyan) !important;
}
:deep(.el-input__inner) { color: var(--txt0) !important; }
:deep(.el-input-number__wrapper) {
  background: rgba(0,0,0,.2);
  box-shadow: 0 0 0 1px var(--bd1);
}
:deep(.el-select__wrapper) {
  background: rgba(0,0,0,.2) !important;
  box-shadow: 0 0 0 1px var(--bd1) !important;
  color: var(--txt0) !important;
}
:deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 1px var(--cyan) !important;
}
:deep(.el-select__selected-item) { color: var(--txt0); }
:deep(.el-select__placeholder) { color: var(--txt2); }

/* ─── 报告预览（白色纸张风格） ─── */
.report-body {
  background: #fff;
  color: #1a1a1a;
  padding: 32px 36px;
  font-size: 12px;
  line-height: 1.7;
  border-radius: 4px;
}

/* 报告头 */
.rpt-head {
  display: flex; align-items: flex-start; justify-content: space-between;
  margin-bottom: 20px;
}
.rpt-meta { font-size: 11px; color: #777; line-height: 2.1; }
.rpt-brand { display: flex; align-items: center; gap: 8px; }
.rpt-brand-icon {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #0060cc, #00aaff);
  border-radius: 6px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-weight: 700; font-size: 13px;
}
.rpt-brand-name { font-size: 14px; font-weight: 700; color: #1a2f5a; }

/* 主标题 */
.rpt-title {
  text-align: center; font-size: 22px; font-weight: 700;
  color: #1a2f5a; margin-bottom: 20px; letter-spacing: 3px;
}

/* 摘要信息栏 */
.rpt-highlight {
  display: flex; justify-content: space-between; align-items: center;
  background: linear-gradient(135deg, #f0f7ff, #e8f0fe);
  border: 1px solid #b8d0f0; border-radius: 6px;
  padding: 14px 20px; margin-bottom: 18px;
  font-size: 13px; line-height: 2;
}
.rpt-hl-text { color: #333; }
.rpt-hl-value-wrap { text-align: center; flex-shrink: 0; padding-left: 20px; }
.rpt-hl-label { font-size: 11px; color: #0060cc; margin-bottom: 2px; }
.rpt-hl-value { font-size: 32px; font-weight: 700; color: #0060cc; line-height: 1.1; }
.rpt-hl-unit { font-size: 12px; color: #0060cc; }

/* 章节标题 */
.rpt-section {
  font-size: 14px; font-weight: 700; color: #1a2f5a;
  padding: 8px 0; border-bottom: 2px solid #1a2f5a;
  margin: 18px 0 10px;
}

/* 数据表格（纸张风格） */
.rpt-table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 14px; }
.rpt-table thead tr { background: #1a2f5a; }
.rpt-table th {
  padding: 7px 10px; text-align: center;
  font-size: 11.5px; font-weight: 500; color: #fff;
}
.rpt-table td {
  padding: 6px 10px; border: 1px solid #ddd;
  text-align: center; color: #333;
}
.rpt-table tbody tr:nth-child(even) td { background: #f5f7fa; }
.rpt-table .tr-total td { background: #e8f0fe; font-weight: 700; color: #1a2f5a; }
.td-empty { color: #999 !important; font-style: italic; }

/* 净碳足迹栏 */
.rpt-net {
  display: flex; justify-content: space-between; align-items: center;
  background: #f0fff8; border: 1px solid #a8e8cc; border-radius: 6px;
  padding: 12px 16px; margin: 12px 0;
  font-size: 13px; font-weight: 600; color: #333;
}
.rpt-net-val { font-size: 22px; font-weight: 700; color: #00a060; }
.rpt-net-sub { font-size: 11px; color: #00a060; margin-top: 2px; }

/* 核算声明 */
.rpt-disclaimer { font-size: 11px; color: #555; line-height: 2.2; padding: 0 2px; }

/* 报告页脚 */
.rpt-footer {
  margin-top: 28px; padding-top: 14px;
  border-top: 1px solid #ddd; font-size: 11px; color: #aaa; text-align: center;
}

/* ─── 侧边栏配置入口 ─── */
.sidebar-config {
  border-top: 1px solid var(--bd0);
  padding: 8px 0;
  flex-shrink: 0;
}
.config-link {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; cursor: pointer;
  color: var(--txt1); font-size: 12.5px;
  text-decoration: none; transition: background .15s, color .15s;
}
.config-link:hover { background: rgba(0,170,255,.06); color: var(--txt0); }

/* ─── 生命周期工具栏 ─── */
.lc-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 9px 16px; border-bottom: 1px solid var(--bd0);
  background: rgba(0,0,0,.08); flex-shrink: 0;
}
.lc-toolbar-total { display: flex; align-items: baseline; gap: 5px; }
.lc-btn-export {
  background: rgba(255,255,255,.04); border: 1px solid var(--bd1);
  border-radius: 4px; color: var(--txt1); font-size: 12px;
  padding: 4px 12px; cursor: pointer; transition: all .15s;
}
.lc-btn-export:hover { background: rgba(255,255,255,.08); color: var(--txt0); }

/* ─── 阶段 badge ─── */
.stage-badge {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 2px 8px; border-radius: 10px; font-size: 10.5px;
  font-weight: 500; white-space: nowrap; min-width: 50px; flex-shrink: 0;
}
.badge-blue   { background: rgba(0,170,255,.12);  color: #66ccff; border: 1px solid rgba(0,170,255,.2); }
.badge-orange { background: rgba(255,146,64,.12); color: #ffaa66; border: 1px solid rgba(255,146,64,.2); }
.badge-yellow { background: rgba(255,201,64,.12); color: #ffcc66; border: 1px solid rgba(255,201,64,.2); }
.badge-purple { background: rgba(160,100,255,.12);color: #cc88ff; border: 1px solid rgba(160,100,255,.2); }
.badge-red    { background: rgba(255,77,106,.12); color: #ff8a9a; border: 1px solid rgba(255,77,106,.2); }
.badge-green  { background: rgba(0,232,150,.12);  color: #00e896; border: 1px solid rgba(0,232,150,.2); }

/* ─── 绿证抵消块增强 ─── */
.green-offset-bar {
  background: linear-gradient(135deg, rgba(0,232,150,.05), rgba(0,100,80,.05));
  border: 1px solid rgba(0,232,150,.18); border-radius: 8px;
  padding: 12px 16px; display: flex; align-items: center; gap: 14px;
}
.green-offset-info { flex: 1; }
.green-offset-title { font-size: 12px; color: var(--green); font-weight: 600; margin-bottom: 3px; }
.green-offset-desc { font-size: 11px; color: var(--txt1); }
.green-offset-desc b { color: var(--green); }
.green-offset-amount { text-align: right; flex-shrink: 0; }
.green-offset-num { font-size: 22px; font-weight: 700; color: var(--green); line-height: 1.1; }
.green-offset-unit { font-size: 11px; color: var(--txt2); }
.btn-green-cert {
  background: rgba(0,232,150,.1); border: 1px solid rgba(0,232,150,.3);
  border-radius: 4px; color: var(--green); font-size: 12px;
  padding: 5px 12px; cursor: pointer; flex-shrink: 0; transition: all .15s;
}
.btn-green-cert:hover { background: rgba(0,232,150,.2); }

/* ─── 净碳足迹块增强 ─── */
.net-total-bar {
  background: rgba(0,170,255,.06); border: 1px solid var(--bd1);
  border-radius: 8px; padding: 12px 16px;
  display: flex; align-items: center; justify-content: space-between;
}
.net-total-title { font-size: 13px; font-weight: 600; color: var(--txt0); margin-bottom: 3px; }
.net-total-hint  { font-size: 11px; color: var(--txt2); }
.net-total-right { display: flex; align-items: baseline; gap: 6px; flex-shrink: 0; }
.net-num  { font-size: 24px; font-weight: 700; color: var(--green); }
.net-unit { font-size: 12px; color: var(--txt2); }
.net-badge {
  padding: 2px 8px; background: rgba(0,232,150,.12); color: var(--green);
  border: 1px solid rgba(0,232,150,.2); border-radius: 10px;
  font-size: 10.5px; font-weight: 500;
}

/* ─── 操作按钮 ─── */
.action-btn {
  display: inline-flex; align-items: center;
  padding: 3px 10px; border-radius: 3px; font-size: 11px;
  cursor: pointer; border: 1px solid; transition: all .15s; margin: 0 2px;
}
.action-edit { background: rgba(0,140,255,.1); border-color: rgba(0,140,255,.3); color: #66aaff; }
.action-edit:hover { background: rgba(0,140,255,.22); border-color: rgba(0,140,255,.55); }
.action-del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-del:hover { background: rgba(255,77,106,.18); }
</style>
