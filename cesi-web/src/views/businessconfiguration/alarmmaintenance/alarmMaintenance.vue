<template>
  <div class="page">
    <div class="alt-container">
      <!-- 筛选栏 -->
      <div class="alt-filter-bar">
        <div class="alt-filter-item">
          <span class="alt-filter-label">限值类型名称</span>
          <el-input v-model="queryParams.limitName" placeholder="请输入限值类型名称" size="small" style="width: 200px" />
        </div>
        <el-button size="small" type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button size="small" icon="Refresh" @click="resetQuery">重置</el-button>
        <el-button size="small" type="primary" icon="Plus" @click="handleAdd" class="alt-btn-add">新增</el-button>
      </div>

      <!-- 内容区 -->
      <div class="alt-content">
        <div class="alt-card">
          <div class="alt-table-wrap">
            <el-table :data="tableData" v-loading="loading" stripe>
              <el-table-column prop="limitName" label="限值类型名称" show-overflow-tooltip align="center" />
              <el-table-column prop="limitCode" label="限值类型编号" show-overflow-tooltip align="center" />
              <el-table-column
                prop="alarmType" label="报警限制类型" show-overflow-tooltip align="center"
                :formatter="(row) => proxy.selectDictLabel(alarm_type, row.alarmType)"
              />
              <el-table-column prop="colorNumber" label="色号" show-overflow-tooltip align="center">
                <template #default="scope">
                  <div
                    style="width: 20px; height: 20px; border-radius: 5px; margin: 0 auto"
                    :style="{ background: scope.row.colorNumber }"
                  />
                </template>
              </el-table-column>
              <el-table-column
                prop="comparatorOperator" label="比较运算符" show-overflow-tooltip align="center"
                :formatter="(row) => proxy.selectDictLabel(operatorList, row.comparatorOperator)"
              />
              <el-table-column label="操作" width="240" align="center">
                <template #default="scope">
                  <el-button link type="primary" icon="Edit" @click="handleAdd(scope.row)">修改</el-button>
                  <el-button link type="primary" icon="Delete" @click="handleDel(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <EmsPageBar
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          @change="getList"
        />
      </div>
    </div>

    <edit-modal ref="EditModalRef" @getList="getList" :operatorList="operatorList" :alarmTypeList="alarm_type" />
  </div>
</template>

<script setup>
import EditModal from "./components/EditModal.vue"
import { alarmList, alarmDel } from "@/api/businessConfiguration/businessConfiguration"

const { proxy }        = getCurrentInstance()
const { alarm_type }   = proxy.useDict("alarm_type")

const operatorList = ref([
  { label: "大于", value: ">" },
  { label: "大于等于", value: ">=" },
  { label: "小于", value: "<" },
  { label: "小于等于", value: "<=" },
  { label: "等于", value: "=" },
])

const loading     = ref(false)
const total       = ref(0)
const tableData   = ref([])
const queryParams = ref({
  limitName: "",
  pageNum:   1,
  pageSize:  10,
})

const EditModalRef = ref()

function getList() {
  loading.value = true
  alarmList(queryParams.value).then((res) => {
    tableData.value = res.rows
    total.value     = res.total
    loading.value   = false
  }).catch(() => { loading.value = false })
}
getList()

function handleAdd(row) {
  EditModalRef.value?.handleOpen(row)
}

function handleDel(row) {
  proxy.$modal.confirm("是否确认删除数据项?").then(() => {
    return alarmDel(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.value = { limitName: "", pageNum: 1, pageSize: 10 }
  getList()
}
</script>

<style scoped>
/* ── 容器 ──────────────────────────────────────────────────────────────── */
.alt-container {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 148px);
}

/* ── 筛选栏 ────────────────────────────────────────────────────────────── */
.alt-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.alt-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.alt-filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.alt-btn-add {
  margin-left: auto;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.alt-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ── 卡片 ──────────────────────────────────────────────────────────────── */
.alt-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}
.alt-table-wrap {
  padding: 8px;
}
</style>
