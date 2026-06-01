<template>
  <div class="pcf-config">
    <!-- 配置页头部 -->
    <div class="config-header">
      <el-button :icon="ArrowLeft" @click="$router.back()">返回核算</el-button>
      <span class="config-title">碳足迹 <em>配置管理</em></span>
      <el-button type="primary" @click="handleSaveAll">保存配置</el-button>
    </div>

    <!-- 配置子导航 -->
    <el-tabs v-model="configTab" class="config-tabs">
      <el-tab-pane label="产品分类管理" name="category" />
      <el-tab-pane label="排放因子库"   name="factor"   />
      <el-tab-pane label="核算参数"     name="params"   />
      <el-tab-pane label="绿证参数"     name="greenParam" />
      <el-tab-pane label="报告模板"     name="reportTpl" />
    </el-tabs>

    <div class="config-body">

      <!-- ══ 产品分类管理 ══ -->
      <div v-show="configTab === 'category'" class="cfg-split">
        <!-- 左：分类树 -->
        <div class="split-left">
          <div class="split-header">
            <span class="split-title">产品分类</span>
            <el-button type="primary" size="small" @click="openCategoryDialog()">+ 新增分类</el-button>
          </div>
          <div class="cat-scroll">
            <template v-for="cat in categoryTree" :key="cat.id">
              <div class="cat-tree-item" :class="{ active: selectedCat?.id === cat.id }"
                   @click="selectCategory(cat)">
                <span class="cat-arrow" @click.stop="cat.expanded = !cat.expanded">
                  {{ cat.children?.length ? (cat.expanded ? '▼' : '▷') : '' }}
                </span>
                <svg class="cat-icon" width="13" height="13" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.8">
                  <rect x="1" y="3" width="12" height="9" rx="1"/><path d="M4 3V2h6v1"/>
                </svg>
                <span class="cat-name">{{ cat.categoryName }}</span>
                <span class="cat-count">{{ cat.productCount ?? 0 }}款</span>
                <div class="cat-actions">
                  <button class="cat-act-btn" @click.stop="openCategoryDialog(cat)">编辑</button>
                  <button class="cat-act-btn del" @click.stop="handleRemoveCategory(cat)">删除</button>
                </div>
              </div>
              <template v-if="cat.expanded && cat.children?.length">
                <div v-for="sub in cat.children" :key="sub.id"
                     class="cat-tree-item sub" :class="{ active: selectedCat?.id === sub.id }"
                     @click="selectCategory(sub)">
                  <span class="cat-arrow">▷</span>
                  <svg class="cat-icon" width="13" height="13" viewBox="0 0 14 14" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M7 1l2 4h4l-3 3 1 4-4-2-4 2 1-4L1 5h4z"/>
                  </svg>
                  <span class="cat-name">{{ sub.categoryName }}</span>
                  <span class="cat-count">{{ sub.productCount ?? 0 }}款</span>
                  <div class="cat-actions">
                    <button class="cat-act-btn" @click.stop="openCategoryDialog(sub)">编辑</button>
                    <button class="cat-act-btn del" @click.stop="handleRemoveCategory(sub)">删除</button>
                  </div>
                </div>
              </template>
            </template>
          </div>
        </div>
        <!-- 右：产品列表 -->
        <div class="split-right">
          <div class="split-header" style="padding:8px 14px;">
            <span class="split-title">产品列表{{ selectedCatPath ? ' — ' + selectedCatPath : '' }}</span>
            <div style="display:flex;gap:8px">
              <el-button type="primary" size="small" @click="openProductDialog()">+ 新增产品</el-button>
              <el-button size="small">导入</el-button>
              <el-button size="small">下载模板</el-button>
            </div>
          </div>
          <div style="overflow-x:auto">
          <el-table :data="productPagedData" size="small" v-loading="productLoading" style="width:100%"
                    :row-style="{ cursor: 'pointer' }" @row-click="handleProductRowClick">
            <el-table-column label="序号" width="50" align="center">
              <template #default="{ $index }">
                {{ (productPageNum - 1) * productPageSize + $index + 1 }}
              </template>
            </el-table-column>
            <el-table-column prop="productName"        label="产品名称"     min-width="120" show-overflow-tooltip />
            <el-table-column prop="specModel"          label="规格型号"     min-width="100" show-overflow-tooltip />
            <el-table-column prop="unit"               label="单位"         width="50"  align="center" />
            <el-table-column prop="lifecycleBoundary"  label="生命周期边界" min-width="110">
              <template #default="{ row }">
                <span :class="['lc-badge', lcBadgeClass(row.lifecycleBoundary)]">{{ row.lifecycleBoundary }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="accountingStandard" label="核算标准"     min-width="115" show-overflow-tooltip />
            <el-table-column prop="functionalUnit"     label="功能单位"     min-width="100" show-overflow-tooltip />
            <el-table-column prop="defaultYear"        label="默认年份"     width="70"  align="center" />
            <el-table-column prop="status" label="状态" width="70" align="center">
              <template #default="{ row }">
                <span :class="row.status === '0' ? 'status-active' : 'status-inactive'">
                  {{ row.status === '0' ? '启用' : '停用' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="110" align="center" fixed="right">
              <template #default="{ row }">
                <div style="display:flex;gap:4px;justify-content:center;white-space:nowrap">
                  <button class="action-btn edit" @click.stop="openProductDialog(row)">编辑</button>
                  <button class="action-btn del"  @click.stop="handleRemoveProduct(row)">删除</button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          </div>
          <EmsPageBar
            :total="productList.length"
            v-model:page="productPageNum"
            v-model:page-size="productPageSize"
          />
          <div class="config-tip">提示：点击产品行可进入该产品的碳足迹核算界面</div>
        </div>
      </div>

      <!-- ══ 排放因子库 ══ -->
      <div v-show="configTab === 'factor'" class="factor-panel">
        <div class="factor-filter">
          <button v-for="f in factorTabs" :key="f.value"
            class="factor-filter-btn" :class="{ active: factorCategory === f.value }"
            @click="factorCategory = f.value; loadFactors()">
            {{ f.label }}
          </button>
        </div>
        <div class="panel-toolbar">
          <span class="source-hint">数据来源：国家温室气体排放因子数据库（2024版）、ecoinvent 3.9 等</span>
          <div class="btn-group">
            <el-button type="primary" size="small" v-hasPermi="['pcf:factor:add']"
                       @click="openFactorDialog()">+ 新增因子</el-button>
            <el-button size="small">导入因子库</el-button>
            <el-button size="small">下载模板</el-button>
            <el-button size="small">导出</el-button>
          </div>
        </div>
        <div style="overflow-x:auto">
        <el-table :data="factorPagedData" size="small" v-loading="factorLoading" style="width:100%">
          <el-table-column label="序号" width="55" align="center">
            <template #default="{ $index }">
              {{ (factorPageNum - 1) * factorPageSize + $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="factorName"     label="名称"         min-width="140" />
          <el-table-column prop="specType"       label="规格/类型"   min-width="130" />
          <el-table-column prop="emissionFactor" label="排放因数"    min-width="110" align="right" />
          <el-table-column prop="factorUnit"     label="单位"         min-width="130" />
          <el-table-column prop="dataSource" label="数据来源" min-width="140">
            <template #default="{ row }">
              <span :class="['factor-badge', dataSourceBadgeClass(row.dataSource)]">
                {{ row.dataSource }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="referenceYear"  label="参考年份"    width="80"  align="center" />
          <el-table-column prop="applicableScope" label="适用范围"   min-width="100" />
          <el-table-column prop="isBuiltin" label="是否内置" width="80" align="center">
            <template #default="{ row }">
              <span :class="['factor-badge', row.isBuiltin === '1' ? 'badge-green' : 'badge-orange']">
                {{ row.isBuiltin === '1' ? '内置' : '自定义' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center" fixed="right">
            <template #default="{ row }">
              <div style="display:flex;gap:4px;justify-content:center;white-space:nowrap">
                <button class="action-btn edit" @click="openFactorDialog(row)">编辑</button>
                <button v-if="row.isBuiltin !== '1'" class="action-btn del"
                        v-hasPermi="['pcf:factor:remove']" @click="handleRemoveFactor(row)">删除</button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        </div>
        <EmsPageBar
          :total="factorList.length"
          v-model:page="factorPageNum"
          v-model:page-size="factorPageSize"
        />
      </div>

      <!-- ══ 核算参数 ══ -->
      <div v-show="configTab === 'params'" class="params-panel">
        <el-scrollbar>
          <div class="params-form">

            <!-- 生命周期边界配置 -->
            <div class="param-card">
              <div class="param-card-header">生命周期边界配置</div>
              <div class="param-grid">
                <div class="param-item">
                  <div class="param-label">默认生命周期边界</div>
                  <el-select v-model="params.lifecycle_boundary" class="param-select">
                    <el-option label="从摇篮到坟墓（Cradle-to-Grave）" value="从摇篮到坟墓（Cradle-to-Grave）" />
                    <el-option label="从摇篮到大门（Cradle-to-Gate）" value="从摇篮到大门（Cradle-to-Gate）" />
                    <el-option label="从摇篮到用户（Cradle-to-Customer）" value="从摇篮到用户（Cradle-to-Customer）" />
                    <el-option label="从大门到大门（Gate-to-Gate）" value="从大门到大门（Gate-to-Gate）" />
                  </el-select>
                  <div class="param-hint">影响核算范围，不同边界对应不同的数据采集要求</div>
                </div>
                <div class="param-item">
                  <div class="param-label">全球变暖潜势(GWP)时间跨度</div>
                  <el-select v-model="params.gwp_timespan" class="param-select">
                    <el-option label="GWP100（100年）—— ISO 14067推荐" value="GWP100（100年）—— ISO 14067推荐" />
                    <el-option label="GWP20（20年）" value="GWP20（20年）" />
                    <el-option label="GWP500（500年）" value="GWP500（500年）" />
                  </el-select>
                </div>
                <div class="param-item">
                  <div class="param-label">截止规则阈值（Cutoff Rule）</div>
                  <el-input v-model="params.cutoff_threshold" class="param-input-short" placeholder="例：1%" />
                  <div class="param-hint">单项排放源贡献低于该阈值时可忽略，但总忽略量不超过5%</div>
                </div>
                <div class="param-item">
                  <div class="param-label">数据质量最低要求</div>
                  <el-select v-model="params.data_quality_level" class="param-select">
                    <el-option label="Level 2 — 行业平均数据" value="Level 2 — 行业平均数据" />
                    <el-option label="Level 1 — 实测/供应商数据（最高）" value="Level 1 — 实测/供应商数据（最高）" />
                    <el-option label="Level 3 — 数据库通用数据" value="Level 3 — 数据库通用数据" />
                  </el-select>
                </div>
              </div>
            </div>

            <!-- 核算标准与参考规范 -->
            <div class="param-card">
              <div class="param-card-header">核算标准与参考规范</div>
              <div class="param-grid">
                <div class="param-item param-item--full">
                  <div class="param-label">适用核算标准（可多选）</div>
                  <div class="sf-checkbox-group">
                    <label v-for="std in accountingStds" :key="std.value" class="sf-checkbox">
                      <input type="checkbox"
                        :checked="accountingStdArr.includes(std.value)"
                        @change="toggleStd(std.value)"
                        style="accent-color:var(--ems-accent)">
                      {{ std.label }}
                    </label>
                  </div>
                </div>
                <div class="param-item">
                  <div class="param-label">GWP参考来源</div>
                  <el-select v-model="params.gwp_reference" class="param-select">
                    <el-option label="IPCC AR6（第六次评估报告）" value="IPCC AR6（第六次评估报告）" />
                    <el-option label="IPCC AR5（第五次评估报告）" value="IPCC AR5（第五次评估报告）" />
                    <el-option label="IPCC AR4（第四次评估报告）" value="IPCC AR4（第四次评估报告）" />
                  </el-select>
                </div>
                <div class="param-item">
                  <div class="param-label">排放因子数据库优先级</div>
                  <el-select v-model="params.factor_priority" class="param-select">
                    <el-option label="国家因子库 → ecoinvent → IPCC → 供应商实测" value="国家因子库 → ecoinvent → IPCC → 供应商实测" />
                    <el-option label="供应商实测 → 国家因子库 → ecoinvent → IPCC" value="供应商实测 → 国家因子库 → ecoinvent → IPCC" />
                    <el-option label="自定义排序" value="自定义排序" />
                  </el-select>
                </div>
                <div class="param-item">
                  <div class="param-label">报告编号前缀</div>
                  <el-input v-model="params.report_number_prefix" class="param-input-short" placeholder="例：PCF" />
                </div>
                <div class="param-item">
                  <div class="param-label">声明有效期（年）</div>
                  <el-input-number v-model.number="params.report_validity_years" :min="1" :max="10" style="width:130px" />
                </div>
              </div>
            </div>

            <!-- 不确定性与敏感性分析 -->
            <div class="param-card">
              <div class="param-card-header">不确定性与敏感性分析</div>
              <div class="param-grid">
                <div class="param-item">
                  <div class="param-label">是否启用不确定性分析</div>
                  <el-radio-group v-model="params.uncertainty_enabled">
                    <el-radio label="1">启用</el-radio>
                    <el-radio label="0">不启用</el-radio>
                  </el-radio-group>
                </div>
                <div class="param-item">
                  <div class="param-label">不确定性分析方法</div>
                  <el-select v-model="params.uncertainty_method" class="param-select">
                    <el-option label="Monte Carlo 蒙特卡洛模拟" value="Monte Carlo 蒙特卡洛模拟" />
                    <el-option label="区间分析法" value="区间分析法" />
                    <el-option label="专家判断法" value="专家判断法" />
                  </el-select>
                </div>
                <div class="param-item">
                  <div class="param-label">置信区间</div>
                  <el-select v-model="params.confidence_interval" class="param-select">
                    <el-option label="90%" value="90%" />
                    <el-option label="95%" value="95%" />
                    <el-option label="99%" value="99%" />
                  </el-select>
                </div>
                <div class="param-item">
                  <div class="param-label">敏感性分析变化幅度</div>
                  <el-input v-model="params.sensitivity_range" class="param-input-short" placeholder="例：±10%" />
                </div>
              </div>
            </div>

          </div>
        </el-scrollbar>
        <div class="save-bar">
          <el-button @click="loadParams">重置默认</el-button>
          <el-button type="primary" @click="handleSaveParams">保存核算参数</el-button>
        </div>
      </div>

      <!-- ══ 绿证参数 ══ -->
      <div v-show="configTab === 'greenParam'" class="green-panel">
        <div class="panel-toolbar">
          <span class="split-title">绿证减排系数配置</span>
          <el-button type="primary" size="small" @click="openGreenFactorDialog()">+ 新增系数</el-button>
        </div>
        <div style="overflow-x:auto">
        <el-table :data="greenPagedData" size="small" v-loading="greenFactorLoading" style="width:100%">
          <el-table-column label="序号" width="55" align="center">
            <template #default="{ $index }">
              {{ (greenPageNum - 1) * greenPageSize + $index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="certType"          label="绿证类型"   min-width="110" />
          <el-table-column prop="powerType"         label="发电类型"   width="80" align="center" />
          <el-table-column prop="emissionFactor"    label="减排系数"   min-width="100" align="right" />
          <el-table-column prop="factorUnit"        label="单位"       min-width="120" />
          <el-table-column prop="referenceStandard" label="参考标准"   min-width="120" />
          <el-table-column prop="applicableRegion"  label="适用区域"   min-width="100" />
          <el-table-column prop="referenceYear"     label="参考年份"   width="80" align="center" />
          <el-table-column prop="status" label="状态" width="70" align="center">
            <template #default="{ row }">
              <span :class="row.status === '0' ? 'status-active' : 'status-inactive'">
                {{ row.status === '0' ? '启用' : '停用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="110" align="center" fixed="right">
            <template #default="{ row }">
              <div style="display:flex;gap:4px;justify-content:center;white-space:nowrap">
                <button class="action-btn edit" @click="openGreenFactorDialog(row)">编辑</button>
                <button class="action-btn del"  @click="handleRemoveGreenFactor(row)">删除</button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        </div>
        <EmsPageBar
          :total="greenFactorList.length"
          v-model:page="greenPageNum"
          v-model:page-size="greenPageSize"
        />
      </div>

      <!-- ══ 报告模板 ══ -->
      <div v-show="configTab === 'reportTpl'" class="report-tpl-panel">
        <el-scrollbar class="tpl-scroll">
          <div class="tpl-section">

            <!-- 企业基本信息 -->
            <div class="cfg-group">
              <div class="cfg-group-title">企业基本信息（报告封面显示）</div>
              <div class="cfg-group-body">
                <div class="settings-form">
                  <div class="sf-item">
                    <label class="sf-label">企业全称</label>
                    <el-input v-model="rptTpl.companyName" placeholder="请输入企业全称" class="sf-el-input" />
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">统一社会信用代码</label>
                    <el-input v-model="rptTpl.creditCode" placeholder="18位统一社会信用代码" class="sf-el-input" />
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">企业地址</label>
                    <el-input v-model="rptTpl.address" placeholder="请输入企业注册地址" class="sf-el-input" />
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">所属行业</label>
                    <el-select v-model="rptTpl.industry" class="sf-el-select" style="width:100%">
                      <el-option label="电气机械和器材制造业" value="电气机械和器材制造业" />
                      <el-option label="通用设备制造业"       value="通用设备制造业" />
                      <el-option label="专用设备制造业"       value="专用设备制造业" />
                      <el-option label="其他"                 value="其他" />
                    </el-select>
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">报告联系人</label>
                    <el-input v-model="rptTpl.contactName" placeholder="负责人姓名" class="sf-el-input" />
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">联系电话 / 邮箱</label>
                    <el-input v-model="rptTpl.contactInfo" placeholder="电话或邮箱" class="sf-el-input" />
                  </div>
                </div>
              </div>
            </div>

            <!-- 报告格式配置 -->
            <div class="cfg-group">
              <div class="cfg-group-title">报告格式配置</div>
              <div class="cfg-group-body">
                <div class="settings-form">
                  <div class="sf-item">
                    <label class="sf-label">报告编号前缀</label>
                    <el-input v-model="rptTpl.reportPrefix" placeholder="例：PCF" class="sf-el-input" style="width:160px" />
                    <div class="sf-hint">报告编号格式：{前缀}-{年份}-{产品编号}-{序号}，例：PCF-2025-S11-001</div>
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">默认导出格式</label>
                    <div class="sf-radio-group">
                      <div v-for="fmt in ['PDF','Word','Excel']" :key="fmt"
                           class="sf-radio" :class="{ checked: rptTpl.exportFormat === fmt }"
                           @click="rptTpl.exportFormat = fmt">{{ fmt }}</div>
                    </div>
                  </div>
                  <div class="sf-item sf-item--full">
                    <label class="sf-label">报告包含章节</label>
                    <div class="sf-checkbox-group">
                      <label v-for="s in reportSections" :key="s.key" class="sf-checkbox">
                        <input type="checkbox" v-model="rptTpl.sections[s.key]"
                               style="accent-color:var(--ems-accent)">
                        {{ s.label }}
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 碳标识认证信息 -->
            <div class="cfg-group">
              <div class="cfg-group-title">碳标识认证信息</div>
              <div class="cfg-group-body">
                <div class="settings-form">
                  <div class="sf-item sf-item--full">
                    <label class="sf-label">目标认证类型</label>
                    <div class="sf-checkbox-group">
                      <label v-for="c in certTypes" :key="c.key" class="sf-checkbox">
                        <input type="checkbox" v-model="rptTpl.certTypes[c.key]"
                               style="accent-color:var(--ems-accent)">
                        {{ c.label }}
                      </label>
                    </div>
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">第三方核查机构</label>
                    <el-input v-model="rptTpl.verifyOrg" placeholder="例：中国质量认证中心（CQC）" class="sf-el-input" />
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">核查机构联系方式</label>
                    <el-input v-model="rptTpl.verifyContact" placeholder="可选" class="sf-el-input" />
                  </div>
                  <div class="sf-item">
                    <label class="sf-label">声明有效期（年）</label>
                    <el-input-number v-model="rptTpl.validityYears" :min="1" :max="10" style="width:130px" />
                    <div class="sf-hint">碳足迹声明有效期，超期需重新核算</div>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </el-scrollbar>
        <div class="save-bar">
          <el-button @click="resetReportTpl">重置默认</el-button>
          <el-button type="primary" @click="saveReportTpl">保存报告模板</el-button>
        </div>
      </div>
    </div>

    <!-- ══ 弹窗：分类 ══ -->
    <el-dialog v-model="catDialogVisible" :title="catForm.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="catForm" label-width="100px" size="small">
        <el-form-item label="分类名称" required>
          <el-input v-model="catForm.categoryName" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-tree-select
            v-model="catForm.parentId"
            :data="parentCatOptions"
            :props="{ label: 'categoryName', children: 'children' }"
            node-key="id"
            default-expand-all
            check-strictly
            style="width:100%"
          />
        </el-form-item>
        <el-form-item label="分类编码">
          <el-input v-model="catForm.categoryCode" placeholder="可选" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="catForm.orderNum" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="catForm.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveCategory">保存</el-button>
      </template>
    </el-dialog>

    <!-- ══ 弹窗：产品 ══ -->
    <el-dialog v-model="productDialogVisible" :title="productForm.id ? '编辑产品' : '新增产品'" width="620px">
      <el-form :model="productForm" label-width="130px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="产品名称" required>
              <el-input v-model="productForm.productName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input v-model="productForm.specModel" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类">
              <el-select v-model="productForm.categoryId" style="width:100%">
                <el-option v-for="c in flatCategoryList" :key="c.id"
                           :label="c.categoryName" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计量单位">
              <el-select v-model="productForm.unit" style="width:100%">
                <el-option v-for="u in ['台','件','套','个','kg','t']" :key="u" :label="u" :value="u" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生命周期边界">
              <el-select v-model="productForm.lifecycleBoundary" style="width:100%">
                <el-option label="从摇篮到坟墓（Cradle-to-Grave）"     value="从摇篮到坟墓" />
                <el-option label="从摇篮到大门（Cradle-to-Gate）"       value="从摇篮到大门" />
                <el-option label="从摇篮到用户（Cradle-to-Customer）"   value="从摇篮到用户" />
                <el-option label="从大门到大门（Gate-to-Gate）"         value="从大门到大门" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="核算标准">
              <el-select v-model="productForm.accountingStandard" style="width:100%">
                <el-option label="ISO 14067:2018" value="ISO 14067:2018" />
                <el-option label="PAS 2050:2011"  value="PAS 2050:2011" />
                <el-option label="GB/T 24067-2024" value="GB/T 24067-2024" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认核算年份">
              <el-input-number v-model="productForm.defaultYear" :min="2020" :max="2099" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="productForm.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="功能单位描述">
              <el-input v-model="productForm.functionalUnit" placeholder="例：1台S11-10000KVA变压器" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参考流">
              <el-input v-model="productForm.referenceFlow" placeholder="例：1台" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="productDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveProduct">保存</el-button>
      </template>
    </el-dialog>

    <!-- ══ 弹窗：排放因子 ══ -->
    <el-dialog v-model="factorDialogVisible" :title="factorForm.id ? '编辑排放因子' : '新增排放因子'" width="580px">
      <el-form :model="factorForm" label-width="140px" size="small">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="因子名称" required>
              <el-input v-model="factorForm.factorName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格/类型">
              <el-input v-model="factorForm.specType" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="因子类别">
              <el-select v-model="factorForm.factorCategory" style="width:100%">
                <el-option label="原材料因子" value="raw" />
                <el-option label="能源因子"   value="energy" />
                <el-option label="运输因子"   value="transport" />
                <el-option label="废弃处理因子" value="waste" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排放因数">
              <el-input-number v-model="factorForm.emissionFactor" :precision="6" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="因数单位">
              <el-input v-model="factorForm.factorUnit" placeholder="例：kgCO₂e/t" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据来源">
              <el-select v-model="factorForm.dataSource" style="width:100%">
                <el-option label="国家因子库(2024)" value="国家因子库(2024)" />
                <el-option label="ecoinvent 3.9"    value="ecoinvent 3.9" />
                <el-option label="IPCC AR6"         value="IPCC AR6" />
                <el-option label="供应商实测值"      value="供应商实测值" />
                <el-option label="其他"             value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="参考年份">
              <el-input-number v-model="factorForm.referenceYear" :min="2000" :max="2099" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用范围">
              <el-input v-model="factorForm.applicableScope" placeholder="例：中国大陆" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否回收抵消">
              <el-radio-group v-model="factorForm.isRecycled">
                <el-radio label="0">否（正排放）</el-radio>
                <el-radio label="1">是（负值减排）</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="factorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveFactor">保存</el-button>
      </template>
    </el-dialog>

    <!-- ══ 弹窗：绿证系数 ══ -->
    <el-dialog v-model="greenFactorDialogVisible" :title="greenFactorForm.id ? '编辑绿证减排系数' : '新增绿证减排系数'" width="540px">
      <el-form :model="greenFactorForm" label-width="160px" size="small">
        <el-form-item label="绿证类型">
          <el-select v-model="greenFactorForm.certType" style="width:100%">
            <el-option label="I-REC"        value="I-REC" />
            <el-option label="绿电证书GEC"  value="绿电证书GEC" />
            <el-option label="GO(欧盟)"     value="GO(欧盟)" />
            <el-option label="RECs(北美)"   value="RECs(北美)" />
          </el-select>
        </el-form-item>
        <el-form-item label="发电类型">
          <el-select v-model="greenFactorForm.powerType" style="width:100%">
            <el-option v-for="t in ['光伏','风电','水电','生物质','地热','核能']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="减排系数(tCO₂e/MWh)">
          <el-input-number v-model="greenFactorForm.emissionFactor" :precision="6" style="width:100%" />
        </el-form-item>
        <el-form-item label="参考标准">
          <el-input v-model="greenFactorForm.referenceStandard" />
        </el-form-item>
        <el-form-item label="适用电网/区域">
          <el-select v-model="greenFactorForm.applicableRegion" style="width:100%">
            <el-option v-for="r in ['华东电网','华北电网','华中电网','南方电网','西北电网','东北电网','全国','欧盟','全球']"
                       :key="r" :label="r" :value="r" />
          </el-select>
        </el-form-item>
        <el-form-item label="参考年份">
          <el-input-number v-model="greenFactorForm.referenceYear" :min="2000" :max="2099" style="width:100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="greenFactorForm.status">
            <el-radio label="0">启用</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="greenFactorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveGreenFactor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PcfConfig">
import { ref, reactive, computed, onMounted } from 'vue'
const router = useRouter()
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  getCategoryTree, saveCategory, removeCategory,
  listProductsByCategory, saveProduct, removeProduct,
  listEmissionFactors, saveEmissionFactor, removeEmissionFactor,
  getAccountingParams, saveAccountingParams,
  listGreenCertFactors, saveGreenCertFactor, removeGreenCertFactor
} from '@/api/productCarbonFootprint/index'

const configTab = ref('category')
const saving    = ref(false)

/* ─── 分类 ─── */
const categoryTree       = ref([])
const flatCategoryList   = ref([])
const selectedCat        = ref(null)
const selectedCatPath    = ref('')
const productList        = ref([])
const productLoading     = ref(false)
const productPageNum     = ref(1)
const productPageSize    = ref(10)
const productPagedData   = computed(() => {
  const start = (productPageNum.value - 1) * productPageSize.value
  return productList.value.slice(start, start + productPageSize.value)
})
const catDialogVisible   = ref(false)
const catForm            = ref({ parentId: 0, orderNum: 0, status: '0' })
const productDialogVisible = ref(false)
const productForm        = ref({ status: '0', unit: '台', lifecycleBoundary: '从摇篮到坟墓', accountingStandard: 'ISO 14067:2018' })

async function loadCategoryTree() {
  const { data } = await getCategoryTree()
  const tree = (data || []).map(n => ({ ...n, expanded: true }))
  categoryTree.value = tree
  flatCategoryList.value = flattenTree(tree)
  // 自动选中第一个有产品的分类（或第一个叶节点）
  if (tree.length && !selectedCat.value) {
    const firstLeaf = findFirstLeaf(tree)
    if (firstLeaf) await selectCategory(firstLeaf)
  }
}

function findFirstLeaf(nodes) {
  for (const n of nodes) {
    if (n.children?.length) {
      const found = findFirstLeaf(n.children)
      if (found) return found
    } else {
      return n
    }
  }
  return nodes[0] || null
}

function flattenTree(nodes, list = []) {
  nodes.forEach(n => {
    list.push(n)
    if (n.children) flattenTree(n.children, list)
  })
  return list
}

/* 父级分类树选项：顶级节点 + 全量分类树（排除当前正在编辑的分类，防止循环引用） */
const parentCatOptions = computed(() => [
  { id: 0, categoryName: '（顶级分类）', children: [] },
  ...excludeFromTree(categoryTree.value, catForm.value.id)
])

function excludeFromTree(nodes, excludeId) {
  return nodes
    .filter(n => n.id !== excludeId)
    .map(n => ({
      ...n,
      children: n.children?.length ? excludeFromTree(n.children, excludeId) : undefined
    }))
}

function dataSourceBadgeClass(val) {
  if (!val) return 'badge-gray'
  if (val.includes('国家') || val.includes('China') || val.includes('2024') || val.includes('2023'))
    return 'badge-blue'
  if (val.includes('ecoinvent') || val.includes('IPCC') || val.includes('ISO'))
    return 'badge-purple'
  if (val.includes('供应商') || val.includes('实测'))
    return 'badge-orange'
  if (val.includes('其他') || val.includes('Other'))
    return 'badge-gray'
  return 'badge-blue'
}

function lcBadgeClass(val) {
  if (!val) return ''
  if (val.includes('坟墓')) return 'lc-blue'
  if (val.includes('大门') && val.includes('摇篮')) return 'lc-yellow'
  if (val.includes('用户')) return 'lc-orange'
  return 'lc-gray'
}

async function handleRemoveCategory(cat) {
  await ElMessageBox.confirm(`确认删除分类"${cat.categoryName}"？`, '提示', { type: 'warning' })
  await removeCategory(cat.id)
  ElMessage.success('删除成功')
  if (selectedCat.value?.id === cat.id) selectedCat.value = null
  await loadCategoryTree()
}

async function selectCategory(cat) {
  selectedCat.value = cat
  // 构建面包屑路径：有父级则显示 "父级 / 当前"
  const parent = flatCategoryList.value.find(c => c.id === cat.parentId && cat.parentId !== 0)
  selectedCatPath.value = parent ? `${parent.categoryName} / ${cat.categoryName}` : cat.categoryName
  productLoading.value = true
  try {
    const { data } = await listProductsByCategory(cat.id)
    productList.value = data || []
    productPageNum.value = 1
  } finally {
    productLoading.value = false
  }
}

function openCategoryDialog(row) {
  catForm.value = row ? { ...row } : { parentId: 0, orderNum: 0, status: '0' }
  catDialogVisible.value = true
}

async function handleSaveCategory() {
  saving.value = true
  try {
    await saveCategory(catForm.value)
    ElMessage.success('保存成功')
    catDialogVisible.value = false
    await loadCategoryTree()
  } finally {
    saving.value = false
  }
}

function openProductDialog(row) {
  productForm.value = row
    ? { ...row }
    : { categoryId: selectedCat.value?.id, status: '0', unit: '台',
        lifecycleBoundary: '从摇篮到坟墓', accountingStandard: 'ISO 14067:2018',
        defaultYear: new Date().getFullYear() }
  productDialogVisible.value = true
}

async function handleSaveProduct() {
  saving.value = true
  try {
    await saveProduct(productForm.value)
    ElMessage.success('保存成功')
    productDialogVisible.value = false
    if (selectedCat.value) await selectCategory(selectedCat.value)
  } finally {
    saving.value = false
  }
}

async function handleRemoveProduct(row) {
  await ElMessageBox.confirm('确认删除该产品？', '提示', { type: 'warning' })
  await removeProduct(row.id)
  ElMessage.success('删除成功')
  if (selectedCat.value) await selectCategory(selectedCat.value)
}

function handleProductRowClick(row) {
  router.push({ path: '/productCarbonFootprint/pcfAccounting', query: { productId: row.id } })
}

/* ─── 排放因子 ─── */
const factorList          = ref([])
const factorLoading       = ref(false)
const factorCategory      = ref('raw')
const factorPageNum       = ref(1)
const factorPageSize      = ref(10)
const factorPagedData     = computed(() => {
  const start = (factorPageNum.value - 1) * factorPageSize.value
  return factorList.value.slice(start, start + factorPageSize.value)
})
const factorTabs = [
  { label: '原材料因子', value: 'raw' },
  { label: '能源因子',   value: 'energy' },
  { label: '运输因子',   value: 'transport' },
  { label: '废弃处理因子', value: 'waste' },
]
const factorDialogVisible = ref(false)
const factorForm          = ref({ isRecycled: '0', status: '0' })

async function loadFactors() {
  factorLoading.value = true
  try {
    const { data } = await listEmissionFactors(factorCategory.value)
    factorList.value = data || []
    factorPageNum.value = 1
  } finally {
    factorLoading.value = false
  }
}

function openFactorDialog(row) {
  factorForm.value = row ? { ...row } : { factorCategory: factorCategory.value, isRecycled: '0', status: '0' }
  factorDialogVisible.value = true
}

async function handleSaveFactor() {
  saving.value = true
  try {
    await saveEmissionFactor(factorForm.value)
    ElMessage.success('保存成功')
    factorDialogVisible.value = false
    await loadFactors()
  } finally {
    saving.value = false
  }
}

async function handleRemoveFactor(row) {
  await ElMessageBox.confirm('确认删除该排放因子？内置因子不可删除', '提示', { type: 'warning' })
  await removeEmissionFactor(row.id)
  ElMessage.success('删除成功')
  await loadFactors()
}

/* ─── 核算参数 ─── */
const params = reactive({
  lifecycle_boundary: '',
  gwp_timespan: '',
  cutoff_threshold: '',
  data_quality_level: '',
  gwp_reference: '',
  factor_priority: '',
  report_number_prefix: 'PCF',
  report_validity_years: 3,
  uncertainty_enabled: '1',
  uncertainty_method: '',
  confidence_interval: '',
  sensitivity_range: '',
})
const accountingStdArr = ref([])
const accountingStds = [
  { value: 'ISO 14067:2018',   label: 'ISO 14067:2018 — 产品碳足迹量化要求和指南（推荐）' },
  { value: 'PAS 2050:2011',   label: 'PAS 2050:2011 — 商品和服务生命周期温室气体排放评估规范' },
  { value: 'GHG Protocol',    label: 'GHG Protocol Product Standard — 温室气体核算体系产品标准' },
  { value: 'GB/T 24067-2024', label: 'GB/T 24067-2024 — 温室气体 产品碳足迹（国标）' },
  { value: '行业专项标准',     label: '行业专项标准（如 T/CECEA 团体标准）' },
]
function toggleStd(val) {
  const i = accountingStdArr.value.indexOf(val)
  if (i === -1) accountingStdArr.value.push(val)
  else accountingStdArr.value.splice(i, 1)
}

async function loadParams() {
  const { data } = await getAccountingParams()
  const map = {}
  ;(Array.isArray(data) ? data : Object.entries(data || {}).map(([k, v]) => ({ paramKey: k, paramValue: v })))
    .forEach(item => { map[item.paramKey] = item.paramValue })
  Object.assign(params, map)
  accountingStdArr.value = map.accounting_standard
    ? map.accounting_standard.split(',').filter(Boolean)
    : []
}

async function handleSaveParams() {
  const payload = [
    ...Object.entries(params).map(([paramKey, paramValue]) => ({
      paramKey, paramValue: String(paramValue ?? '')
    })),
    { paramKey: 'accounting_standard', paramValue: accountingStdArr.value.join(',') }
  ]
  await saveAccountingParams(payload)
  ElMessage.success('核算参数保存成功')
}

/* ─── 绿证系数 ─── */
const greenFactorList          = ref([])
const greenFactorLoading       = ref(false)
const greenPageNum             = ref(1)
const greenPageSize            = ref(10)
const greenPagedData           = computed(() => {
  const start = (greenPageNum.value - 1) * greenPageSize.value
  return greenFactorList.value.slice(start, start + greenPageSize.value)
})
const greenFactorDialogVisible = ref(false)
const greenFactorForm          = ref({ status: '0' })

async function loadGreenFactors() {
  greenFactorLoading.value = true
  try {
    const { data } = await listGreenCertFactors()
    greenFactorList.value = data || []
    greenPageNum.value = 1
  } finally {
    greenFactorLoading.value = false
  }
}

function openGreenFactorDialog(row) {
  greenFactorForm.value = row ? { ...row } : { status: '0' }
  greenFactorDialogVisible.value = true
}

async function handleSaveGreenFactor() {
  saving.value = true
  try {
    await saveGreenCertFactor(greenFactorForm.value)
    ElMessage.success('保存成功')
    greenFactorDialogVisible.value = false
    await loadGreenFactors()
  } finally {
    saving.value = false
  }
}

async function handleRemoveGreenFactor(row) {
  await ElMessageBox.confirm('确认删除该绿证系数？', '提示', { type: 'warning' })
  await removeGreenCertFactor(row.id)
  ElMessage.success('删除成功')
  await loadGreenFactors()
}

async function handleSaveAll() {
  if (configTab.value === 'params')     await handleSaveParams()
  else if (configTab.value === 'reportTpl') saveReportTpl()
  else ElMessage.success('配置已保存')
}

/* ─── 报告模板 ─── */
const reportSections = [
  { key: 'lifecycle',    label: '生命周期各阶段汇总' },
  { key: 'details',     label: '各阶段数据明细' },
  { key: 'greenCert',   label: '绿电绿证抵消' },
  { key: 'declaration', label: '核算声明' },
  { key: 'uncertainty', label: '不确定性分析结果' },
  { key: 'dataQuality', label: '数据质量评估' },
  { key: 'reduction',   label: '减排建议' },
]
const certTypes = [
  { key: 'carbonLabelCN',  label: '中国碳标签（Carbon Label China）' },
  { key: 'carbonTrust',    label: 'Carbon Trust 碳信任认证（英国）' },
  { key: 'epd',            label: 'EPD 环境产品声明' },
  { key: 'thirdParty',     label: '第三方核查报告（自愿）' },
]
const defaultRptTpl = () => ({
  companyName:   '',
  creditCode:    '',
  address:       '',
  industry:      '电气机械和器材制造业',
  contactName:   '',
  contactInfo:   '',
  reportPrefix:  'PCF',
  exportFormat:  'PDF',
  validityYears: 3,
  sections: { lifecycle: true, details: true, greenCert: true, declaration: true,
              uncertainty: false, dataQuality: false, reduction: false },
  certTypes: { carbonLabelCN: true, carbonTrust: false, epd: false, thirdParty: false },
  verifyOrg:     '',
  verifyContact: '',
})
const rptTpl = reactive(defaultRptTpl())

function resetReportTpl() {
  Object.assign(rptTpl, defaultRptTpl())
  ElMessage.success('已重置为默认值')
}
function saveReportTpl() {
  ElMessage.success('报告模板配置已保存')
}

/* ─── 初始化 ─── */
onMounted(async () => {
  await loadCategoryTree()
  await loadFactors()
  await loadParams()
  await loadGreenFactors()
})
</script>

<style scoped>
/* ── 深色主题色板（对齐全局 ems token） ── */
.pcf-config {
  display: flex; flex-direction: column; height: 100%;
  background: var(--ems-bg-main);
  --bd:    var(--ems-border-dim);
  --bg0:   var(--ems-bg-main);
  --bg1:   var(--ems-bg-card);
  --bg2:   var(--ems-bg-header);
  --txt:   var(--ems-text-primary);
  --txt2:  var(--ems-text-secondary);
  --muted: var(--ems-text-muted);
  --cyan:  var(--ems-accent);
  --green: var(--ems-accent-green);
  --red:   var(--ems-accent-red);
}

/* ── 页头 ── */
.config-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 20px; min-height: 56px;
  background: var(--bg2); border-bottom: 1px solid var(--ems-border-mid);
  flex-shrink: 0; position: relative;
}
.config-header::after {
  content: ''; position: absolute; left: 0; bottom: 0; right: 0; height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0,200,255,.3), transparent);
}
.config-title { font-size: 17px; font-weight: 600; color: var(--txt); letter-spacing: .8px; }
.config-title em { color: var(--ems-accent-bright); font-style: normal; }

/* ── 配置 Tabs ── */
.config-tabs { background: var(--bg2); padding: 0 20px; flex-shrink: 0; }
:deep(.el-tabs__header) { margin-bottom: 0; border-bottom-color: var(--bd) !important; }
:deep(.el-tabs__item) { color: var(--txt2) !important; }
:deep(.el-tabs__item.is-active) { color: var(--cyan) !important; }
:deep(.el-tabs__active-bar) { background-color: var(--cyan) !important; }
:deep(.el-tabs__nav-wrap::after) { background-color: var(--bd) !important; }
.config-body { flex: 1; overflow: hidden; display: flex; flex-direction: column; }

/* ── 分类管理面板 ── */
.cfg-split { display: flex; flex: 1; overflow: hidden; }
.split-left {
  width: 270px; min-width: 270px;
  background: var(--bg1); border-right: 1px solid var(--bd);
  display: flex; flex-direction: column; overflow: hidden;
}
.split-right {
  flex: 1; display: flex; flex-direction: column; overflow: hidden; background: var(--bg0);
}
.split-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 14px; border-bottom: 1px solid var(--bd);
  background: var(--bg2); flex-shrink: 0;
}
.split-title { font-size: 12px; font-weight: 600; color: var(--txt2); letter-spacing: .3px; text-transform: uppercase; }

/* ── 分类树 ── */
.cat-scroll { flex: 1; overflow-y: auto; }
.cat-scroll::-webkit-scrollbar { width: 4px; }
.cat-scroll::-webkit-scrollbar-thumb { background: rgba(79,195,247,.2); border-radius: 2px; }
.cat-tree-item {
  display: flex; align-items: center; gap: 5px;
  padding: 7px 10px 7px 12px;
  border-bottom: 1px solid rgba(30,58,95,.6);
  cursor: pointer; font-size: 12.5px; color: var(--txt2);
  transition: background 0.15s; position: relative;
}
.cat-tree-item.sub { padding-left: 26px; }
.cat-tree-item:hover { background: rgba(79,195,247,.06); color: var(--txt); }
.cat-tree-item:hover .cat-actions { opacity: 1; }
.cat-tree-item.active {
  background: rgba(79,195,247,.12); color: var(--cyan); font-weight: 600;
}
.cat-tree-item.active::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0;
  width: 2px; background: var(--cyan);
}
.cat-arrow { width: 14px; flex-shrink: 0; color: var(--muted); font-size: 10px; text-align: center; line-height: 1; user-select: none; }
.cat-icon { flex-shrink: 0; color: var(--muted); }
.cat-tree-item.active .cat-icon { color: var(--cyan); }
.cat-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cat-count {
  font-size: 11px; color: var(--muted); background: rgba(255,255,255,.05);
  padding: 1px 6px; border-radius: 10px; flex-shrink: 0; border: 1px solid rgba(30,58,95,.8);
}
.cat-tree-item.active .cat-count { background: rgba(79,195,247,.15); color: var(--cyan); border-color: rgba(79,195,247,.3); }
.cat-actions { display: flex; gap: 4px; opacity: 0; transition: opacity 0.15s; flex-shrink: 0; }
.cat-act-btn {
  font-size: 11px; color: var(--muted); background: none; border: none;
  cursor: pointer; padding: 2px 4px; border-radius: 2px; transition: color .15s, background .15s;
}
.cat-act-btn:hover { color: var(--cyan); background: rgba(79,195,247,.1); }
.cat-act-btn.del:hover { color: var(--red); background: rgba(239,154,154,.1); }

/* ── El-Table 深色覆盖 ── */
:deep(.el-table), :deep(.el-table tr),
:deep(.el-table__body-wrapper), :deep(.el-table__inner-wrapper) {
  background-color: transparent !important;
}
:deep(.el-table th.el-table__cell) {
  background-color: rgba(0,0,0,.4) !important;
  color: var(--txt2) !important;
  border-bottom: 1px solid var(--bd) !important;
  border-right: 1px solid rgba(30,58,95,.5) !important;
  font-size: 11px; font-weight: 500; letter-spacing: .3px;
}
:deep(.el-table td.el-table__cell) {
  color: var(--txt) !important;
  border-bottom: 1px solid rgba(30,58,95,.5) !important;
  border-right: 1px solid rgba(30,58,95,.3) !important;
  font-size: 12.5px;
}
:deep(.el-table__body tr:hover > td.el-table__cell) {
  background-color: rgba(79,195,247,.06) !important;
}
:deep(.el-table__empty-block) { background-color: transparent; }
:deep(.el-table__empty-text) { color: var(--muted); }
:deep(.el-table__inner-wrapper::before) { background-color: transparent; }

/* ── 状态 badge（胶囊形式，对齐原型） ── */
.status-active {
  display: inline-flex; align-items: center;
  padding: 2px 8px; border-radius: 8px; font-size: 11px; font-weight: 500;
  background: rgba(0,232,150,.1); border: 1px solid rgba(0,232,150,.25);
  color: var(--ems-accent-green);
}
.status-inactive {
  display: inline-flex; align-items: center;
  padding: 2px 8px; border-radius: 8px; font-size: 11px; font-weight: 500;
  background: rgba(255,77,106,.08); border: 1px solid rgba(255,77,106,.2);
  color: var(--ems-accent-red);
}

/* ── 操作按钮（带背景/边框，对齐原型） ── */
.action-btn {
  display: inline-flex; align-items: center;
  padding: 3px 10px; border-radius: 3px; font-size: 11px;
  cursor: pointer; border: 1px solid; transition: all .15s;
  margin: 0 2px; background: none;
}
.action-btn.edit { background: rgba(0,140,255,.1); border-color: rgba(0,140,255,.3); color: #66aaff; }
.action-btn.edit:hover { background: rgba(0,140,255,.22); border-color: rgba(0,140,255,.55); }
.action-btn.del  { background: rgba(255,77,106,.08); border-color: rgba(255,77,106,.25); color: #ff7a90; }
.action-btn.del:hover  { background: rgba(255,77,106,.18); }

/* ── 生命周期边界 badge ── */
.lc-badge    { display: inline-block; padding: 2px 7px; border-radius: 3px; font-size: 11px; white-space: nowrap; }
.factor-badge { display: inline-block; padding: 2px 8px; border-radius: 10px; font-size: 10.5px; font-weight: 500; white-space: nowrap; }
.badge-blue   { background: rgba(0,170,255,.12);  color: #66ccff; border: 1px solid rgba(0,170,255,.2); }
.badge-purple { background: rgba(160,100,255,.12); color: #cc88ff; border: 1px solid rgba(160,100,255,.2); }
.badge-orange { background: rgba(255,146,64,.12);  color: #ffaa66; border: 1px solid rgba(255,146,64,.2); }
.badge-green  { background: rgba(0,232,150,.12);   color: #00e896; border: 1px solid rgba(0,232,150,.2); }
.badge-red    { background: rgba(255,77,106,.12);  color: #ff8a9a; border: 1px solid rgba(255,77,106,.2); }
.badge-yellow { background: rgba(255,201,64,.12);  color: #ffcc66; border: 1px solid rgba(255,201,64,.2); }
.badge-gray   { background: rgba(255,255,255,.06); color: var(--txt2); border: 1px solid rgba(255,255,255,.1); }
.lc-blue   { background: rgba(79,195,247,.15); color: var(--cyan); }
.lc-yellow { background: rgba(230,162,60,.15);  color: #ffcc80; }
.lc-orange { background: rgba(255,146,64,.15);  color: #ffab76; }
.lc-gray   { background: rgba(255,255,255,.05); color: var(--txt2); }


/* ── 提示文字 ── */
.config-tip { padding: 8px 14px; font-size: 11px; color: var(--muted); border-top: 1px solid var(--bd); }

/* ── 排放因子面板 ── */
.factor-panel { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.factor-filter {
  display: flex; padding: 0 20px;
  background: rgba(0,0,0,.08); border-bottom: 1px solid var(--bd); flex-shrink: 0;
}
.factor-filter-btn {
  padding: 9px 14px; font-size: 12px; color: var(--txt2);
  cursor: pointer; border: none; background: none;
  border-bottom: 2px solid transparent;
  transition: all .2s; white-space: nowrap;
}
.factor-filter-btn:hover { color: var(--txt); }
.factor-filter-btn.active { color: var(--ems-accent-bright); border-bottom-color: var(--ems-accent); }
.panel-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 16px; border-bottom: 1px solid rgba(30,58,95,.6); flex-shrink: 0;
}
.source-hint { font-size: 12px; color: var(--muted); }
.btn-group { display: flex; gap: 8px; }

/* ── 核算参数面板 ── */
.params-panel { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.params-form { padding: 16px; display: flex; flex-direction: column; gap: 16px; }
.hint-text { font-size: 11px; color: var(--muted); margin-left: 8px; }
.save-bar {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 10px 20px; border-top: 1px solid var(--bd); flex-shrink: 0;
}

/* ── 绿证参数面板 ── */
.green-panel { display: flex; flex-direction: column; flex: 1; overflow: hidden; }

/* ── 报告模板面板 ── */
.report-tpl-panel { display: flex; flex-direction: column; flex: 1; overflow: hidden; }
.tpl-scroll { flex: 1; }
.tpl-section { padding: 16px; display: flex; flex-direction: column; gap: 16px; }

/* ── cfg-group（对齐原型 cfg-group） ── */
.cfg-group {
  background: var(--bg1);
  border: 1px solid var(--bd);
  border-radius: 6px;
  overflow: hidden;
}
.cfg-group-title {
  padding: 10px 16px;
  font-size: 12px; font-weight: 600; color: var(--txt2);
  border-bottom: 1px solid var(--bd);
  letter-spacing: .8px; text-transform: uppercase;
  display: flex; align-items: center; justify-content: space-between;
}
.cfg-group-body { padding: 16px; }

/* ── settings-form（2列网格，对齐原型） ── */
.settings-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.sf-item { display: flex; flex-direction: column; gap: 6px; }
.sf-item--full { grid-column: 1 / -1; }
.sf-label { font-size: 11.5px; color: var(--txt2); }
.sf-hint  { font-size: 11px; color: var(--muted); line-height: 1.4; margin-top: 2px; }

/* sf-input / sf-select —— el-input/el-select 深色适配通过 .sf-el-input 局部覆盖 */
:deep(.sf-el-input .el-input__wrapper) {
  background: rgba(0,0,0,.2) !important;
  border: 1px solid var(--bd) !important;
  box-shadow: none !important;
}
:deep(.sf-el-input .el-input__wrapper.is-focus) {
  border-color: var(--ems-accent) !important;
  box-shadow: 0 0 0 2px var(--ems-accent-glow) !important;
}
:deep(.sf-el-input .el-input__inner) { color: var(--txt) !important; }
:deep(.sf-el-select .el-input__wrapper) {
  background: rgba(0,0,0,.2) !important;
  border: 1px solid var(--bd) !important;
  box-shadow: none !important;
}
:deep(.sf-el-select .el-input__inner) { color: var(--txt) !important; }

/* ── 自定义 Radio 胶囊（对齐原型 sf-radio） ── */
.sf-radio-group { display: flex; flex-wrap: wrap; gap: 8px; padding-top: 4px; }
.sf-radio {
  display: flex; align-items: center;
  padding: 5px 14px;
  border: 1px solid var(--bd);
  border-radius: 12px;
  cursor: pointer;
  font-size: 12px;
  color: var(--txt2);
  transition: all .15s;
  user-select: none;
}
.sf-radio:hover { border-color: var(--ems-accent-bright); color: var(--txt); }
.sf-radio.checked {
  background: rgba(0,140,255,.12);
  border-color: rgba(0,140,255,.4);
  color: var(--ems-accent-bright);
}

/* ── 复选框组 ── */
.sf-checkbox-group { display: flex; flex-direction: column; gap: 8px; padding-top: 4px; }
.sf-checkbox {
  display: flex; align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: var(--txt);
  cursor: pointer;
}
.sf-checkbox input[type=checkbox] {
  width: 14px; height: 14px;
  accent-color: var(--ems-accent);
  cursor: pointer;
  flex-shrink: 0;
}

/* ── 核算参数面板 ── */
.param-card {
  background: var(--bg1);
  border: 1px solid var(--bd);
  border-radius: 6px;
  overflow: hidden;
}
.param-card-header {
  padding: 10px 16px;
  font-size: 13px; font-weight: 600; color: var(--cyan);
  background: rgba(0,0,0,.25);
  border-bottom: 1px solid var(--bd);
  letter-spacing: .5px;
}
.param-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
  padding: 16px;
}
.param-item { display: flex; flex-direction: column; gap: 6px; }
.param-item--full { grid-column: 1 / -1; }
.param-label { font-size: 12px; color: var(--txt2); font-weight: 500; }
.param-hint { font-size: 11px; color: var(--muted); line-height: 1.4; }
.param-select { width: 100%; }
.param-input-short { width: 160px; }


/* 核算参数：深色 el-select / el-input 覆盖 */
:deep(.param-select .el-input__wrapper),
:deep(.param-input-short .el-input__wrapper) {
  background: rgba(13,31,60,.8) !important;
  border: 1px solid var(--bd) !important;
  box-shadow: none !important;
}
:deep(.param-select .el-input__inner),
:deep(.param-input-short .el-input__inner) { color: var(--txt) !important; }
:deep(.el-input-number .el-input__wrapper) {
  background: rgba(13,31,60,.8) !important;
  border: 1px solid var(--bd) !important;
  box-shadow: none !important;
}
:deep(.el-input-number .el-input__inner) { color: var(--txt) !important; }
:deep(.params-panel .el-radio__label) { color: var(--txt2) !important; font-size: 12.5px; }
:deep(.params-panel .el-radio.is-checked .el-radio__label) { color: var(--cyan) !important; }
:deep(.params-panel .el-radio__inner) {
  background: rgba(13,31,60,.8) !important;
  border-color: var(--bd) !important;
}
:deep(.params-panel .el-radio.is-checked .el-radio__inner) {
  background: #1565c0 !important;
  border-color: #1976d2 !important;
}

/* ── 深色主题 El-Button 统一风格 ── */
/* 次要按钮（无 type / default）：深色半透明底 + 蓝边框 */
:deep(.config-header .el-button:not(.el-button--primary)),
:deep(.split-header  .el-button:not(.el-button--primary)),
:deep(.panel-toolbar .el-button:not(.el-button--primary)),
:deep(.save-bar      .el-button:not(.el-button--primary)) {
  background: rgba(13,31,60,.8) !important;
  border: 1px solid var(--bd) !important;
  color: var(--txt2) !important;
}
:deep(.config-header .el-button:not(.el-button--primary):hover),
:deep(.split-header  .el-button:not(.el-button--primary):hover),
:deep(.panel-toolbar .el-button:not(.el-button--primary):hover),
:deep(.save-bar      .el-button:not(.el-button--primary):hover) {
  background: rgba(79,195,247,.12) !important;
  border-color: rgba(79,195,247,.5) !important;
  color: var(--cyan) !important;
}

/* 主要按钮（primary）：保持品牌蓝，统一圆角与阴影 */
:deep(.el-button--primary) {
  background: #1565c0 !important;
  border-color: #1976d2 !important;
  color: #e3f2fd !important;
  box-shadow: 0 1px 4px rgba(21,101,192,.5) !important;
}
:deep(.el-button--primary:hover) {
  background: #1976d2 !important;
  border-color: #42a5f5 !important;
  color: #fff !important;
}

/* 弹窗内按钮深色适配 */
:deep(.el-dialog) {
  background: #0d1f3c !important;
  border: 1px solid var(--bd) !important;
  box-shadow: 0 8px 32px rgba(0,0,0,.6) !important;
}
:deep(.el-dialog__header) {
  background: #0a1628 !important;
  border-bottom: 1px solid var(--bd) !important;
}
:deep(.el-dialog__title) { color: var(--txt) !important; }
:deep(.el-dialog__footer) {
  border-top: 1px solid var(--bd) !important;
  background: #0a1628 !important;
}
:deep(.el-dialog .el-button:not(.el-button--primary)) {
  background: rgba(13,31,60,.8) !important;
  border: 1px solid var(--bd) !important;
  color: var(--txt2) !important;
}
:deep(.el-dialog .el-button:not(.el-button--primary):hover) {
  background: rgba(79,195,247,.12) !important;
  border-color: rgba(79,195,247,.5) !important;
  color: var(--cyan) !important;
}

</style>
