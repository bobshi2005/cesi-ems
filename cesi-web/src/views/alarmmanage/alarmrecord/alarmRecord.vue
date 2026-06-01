<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="ar-filter-bar">
          <el-radio-group v-model="form.eierarchyFlag" size="small" class="ar-radio-group">
            <el-radio-button label="B">本级</el-radio-button>
            <el-radio-button label="ALL">包含下级</el-radio-button>
          </el-radio-group>

          <div class="ar-filter-item">
            <span class="ar-filter-label">时间</span>
            <el-date-picker
              v-model="form.dataTime"
              type="datetimerange"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择时间范围"
              size="small"
              style="width: 320px"
              unlink-panels
              time-format="HH:mm:ss"
            />
          </div>

          <div class="ar-filter-item">
            <span class="ar-filter-label">报警类别</span>
            <el-select v-model="form.indexType" placeholder="请选择报警类别" size="small" style="width: 160px">
              <el-option v-for="dict in alarm_record_category" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </div>

          <div class="ar-filter-item">
            <span class="ar-filter-label">指标名称</span>
            <el-input v-model="form.indexName" placeholder="请输入指标名称" size="small" style="width: 160px" />
          </div>

          <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </div>

        <!-- 内容区 -->
        <div class="ar-content" v-loading="loading">

          <!-- 空状态 -->
          <div v-if="!currentNode" class="ar-empty">
            <el-icon><InfoFilled /></el-icon>
            请在左侧选择节点
          </div>

          <!-- 表格 -->
          <template v-else>
            <div class="ar-card">
              <div class="ar-card-head">{{ currentNode.label }} · 报警记录</div>
              <div class="ar-table-wrap">
                <el-table :data="tableData" height="calc(100vh - 380px)" stripe>
                  <el-table-column type="index" label="序号" width="60" />
                  <el-table-column label="用能单元" prop="modelName" align="center" show-overflow-tooltip />
                  <el-table-column label="指标名称" prop="indexName" align="center" show-overflow-tooltip />
                  <el-table-column
                    label="报警类别" prop="indexType" align="center" show-overflow-tooltip
                    :formatter="(row) => proxy.selectDictLabel(alarm_record_category, row.indexType)"
                  />
                  <el-table-column
                    label="能源类型" prop="energyId" align="center" show-overflow-tooltip
                    :formatter="(row) => formatterLabel(energyTypeList, row.energyId)"
                  />
                  <el-table-column label="预设值" prop="limitingValue" align="center" show-overflow-tooltip />
                  <el-table-column label="报警值" prop="alarmValue" align="center" show-overflow-tooltip />
                  <el-table-column label="报警时间" prop="alarmBeginTime" align="center" show-overflow-tooltip width="170" />
                </el-table>
              </div>
            </div>

            <EmsPageBar
              :total="total"
              v-model:page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              @change="getList()"
            />
          </template>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="alarmRecord">
import { listEnergyTypeList } from "@/api/modelConfiguration/energyType"
import { historicalAlarm } from "@/api/alarmManage/alarmManage"
import { InfoFilled } from "@element-plus/icons-vue"

const { proxy } = getCurrentInstance()
const { alarm_record_category } = proxy.useDict("alarm_record_category")

const loading       = ref(false)
const currentNode   = ref(null)
const tableData     = ref([])
const total         = ref(0)
const energyTypeList = ref([])

const form = ref({
  eierarchyFlag: "B",
  dataTime: [
    proxy.dayjs(new Date()).format("YYYY-MM-DD 00:00:00"),
    proxy.dayjs(new Date()).format("YYYY-MM-DD 23:59:59"),
  ],
  nodeId:      "",
  indexType:   "",
  indexName:   "",
})

const queryParams = ref({
  pageNum:  1,
  pageSize: 10,
})

function getEnergyTypeList() {
  listEnergyTypeList().then((res) => {
    energyTypeList.value = res.data || []
    form.value.indexType = alarm_record_category.value[0]?.value
    getList()
  })
}

function handleNodeClick(data) {
  currentNode.value = data
  getEnergyTypeList()
}

function formatterLabel(list, value) {
  const dict = list.find((item) => item.enersno == value)
  return dict ? dict.enername : ""
}

function getList() {
  if (!currentNode.value) return
  form.value.nodeId = currentNode.value.id
  loading.value = true
  historicalAlarm({
    ...form.value,
    ...queryParams.value,
    beginTime: form.value.dataTime[0],
    endTime:   form.value.dataTime[1],
  }).then((response) => {
    loading.value = false
    if (response.code === 200) {
      tableData.value = response.rows
      total.value     = response.total
    }
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  form.value = {
    eierarchyFlag: "B",
    dataTime: [
      proxy.dayjs(new Date()).format("YYYY-MM-DD 00:00:00"),
      proxy.dayjs(new Date()).format("YYYY-MM-DD 23:59:59"),
    ],
    nodeId:    "",
    indexType: alarm_record_category.value[0]?.value,
    indexName: "",
  }
  queryParams.value = { pageNum: 1, pageSize: 10 }
  getList()
}
</script>

<style scoped>
/* ── 左右 flex 布局 ────────────────────────────────────────────────────── */
.page-container {
  display: flex;
  min-height: calc(100vh - 148px);
}
.page-container-left {
  flex-shrink: 0;
  width: 220px;
  min-height: calc(100vh - 148px);
  border-right: 1px solid var(--ems-border-dim);
  overflow-y: auto;
}
.page-container-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 筛选栏 ────────────────────────────────────────────────────────────── */
.ar-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-wrap: wrap;
  flex-shrink: 0;
}
.ar-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.ar-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ar-radio-group {
  :deep(.el-radio-button__inner) {
    padding: 0 12px;
    height: 30px;
    line-height: 30px;
    font-size: 13px;
    background: rgba(0,170,255,.08);
    border: 1px solid var(--ems-border-dim);
    color: var(--ems-text-secondary);
    box-shadow: none !important;
  }
  :deep(.el-radio-button:first-child .el-radio-button__inner) { border-radius: 4px 0 0 4px; }
  :deep(.el-radio-button:last-child .el-radio-button__inner)  { border-radius: 0 4px 4px 0; }
  :deep(.el-radio-button--active .el-radio-button__inner) {
    background: var(--ems-accent);
    border-color: var(--ems-accent);
    color: #fff;
  }
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.ar-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.ar-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.ar-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 卡片 ──────────────────────────────────────────────────────────────── */
.ar-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.ar-card-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
}
.ar-table-wrap {
  padding: 8px;
}
</style>
