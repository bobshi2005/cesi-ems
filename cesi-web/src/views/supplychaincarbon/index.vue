<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="queryParams" class="query-form">
      <el-form-item label="供应商名称">
        <el-input
          v-model="queryParams.supplierName"
          placeholder="请输入供应商名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="物料分类">
        <el-select
          v-model="queryParams.materialCategory"
          placeholder="请选择物料分类"
          clearable
          style="width: 160px"
        >
          <el-option v-for="item in materialCategoryOptions" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
      <el-form-item style="float: right">
        <el-button
          type="primary"
          icon="Plus"
          v-hasPermi="['supplyChainCarbon:supplier:add']"
          @click="handleAdd"
        >新增</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="供应商名称称" prop="supplierName" align="center" min-width="130" />
      <el-table-column label="物料分类" prop="materialCategory" align="center" width="100" />
      <el-table-column label="供应物料" prop="supplyMaterial" align="center" min-width="100" />
      <el-table-column label="规格型号" prop="specModel" align="center" min-width="100" />
      <el-table-column label="系统边界" prop="systemBoundary" align="center" width="140">
        <template #default="{ row }">
          <el-tag :type="boundaryTagType(row.systemBoundary)" size="small">
            {{ row.systemBoundary }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="碳足迹" prop="carbonFootprint" align="center" width="100">
        <template #default="{ row }">
          {{ row.carbonFootprint }} {{ row.carbonFootprintUnit }}
        </template>
      </el-table-column>
      <el-table-column label="联系人" prop="contactPerson" align="center" width="90" />
      <el-table-column label="联系方式" prop="contactWay" align="center" min-width="120" />
      <el-table-column label="操作" align="center" width="130" fixed="right">
        <template #default="{ row }">
          <el-button
            link type="primary" size="small"
            v-hasPermi="['supplyChainCarbon:supplier:edit']"
            @click="handleEdit(row)"
          >编辑</el-button>
          <el-button
            link type="danger" size="small"
            v-hasPermi="['supplyChainCarbon:supplier:delete']"
            @click="handleDelete(row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <EmsPageBar
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      @change="loadData"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="680px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="供应商名称" prop="supplierName">
              <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="系统边界" prop="systemBoundary">
              <el-select v-model="form.systemBoundary" placeholder="请选择边界类型" style="width: 100%">
                <el-option label="从摇篮到大门" value="从摇篮到大门" />
                <el-option label="从摇篮到坟墓" value="从摇篮到坟墓" />
                <el-option label="从摇篮到大门到坟墓" value="从摇篮到大门到坟墓" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料分类" prop="materialCategory">
              <el-select v-model="form.materialCategory" placeholder="请选择物料分类" style="width: 100%">
                <el-option v-for="item in materialCategoryOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料名称" prop="supplyMaterial">
              <el-input v-model="form.supplyMaterial" placeholder="请输入供应物料名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格型号">
              <el-input v-model="form.specModel" placeholder="请输入规格型号" />
            </el-form-item>
          </el-col>
          <el-col :span="12" />
          <el-col :span="12">
            <el-form-item label="物料总量" prop="materialTotal">
              <el-input v-model="form.materialTotal" placeholder="请输入物料总量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物料单位" prop="materialUnit">
              <el-input v-model="form.materialUnit" placeholder="请输入物料单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="碳足迹总量" prop="carbonFootprint">
              <el-input v-model="form.carbonFootprint" placeholder="请输入碳足迹总量" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="碳足迹单位" prop="carbonFootprintUnit">
              <el-input v-model="form.carbonFootprintUnit" placeholder="请输入碳足迹单位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系方式">
              <el-input v-model="form.contactWay" placeholder="请输入联系方式" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商地址">
              <el-input v-model="form.supplierAddress" type="textarea" :rows="3" placeholder="请输入供应商地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注信息">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="SupplyChainCarbon">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSupplyChainCarbonPage,
  addSupplyChainCarbon,
  editSupplyChainCarbon,
  deleteSupplyChainCarbon
} from '@/api/supplychaincarbon/supplyChainCarbon'

const materialCategoryOptions = ['物料类', '原材料', '辅助材料', '包装材料', '零部件']

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  supplierName: '',
  materialCategory: '',
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const dialogTitle = ref('添加供应商')
const formRef = ref(null)

const defaultForm = () => ({
  id: null,
  supplierName: '',
  systemBoundary: '',
  materialCategory: '',
  supplyMaterial: '',
  specModel: '',
  materialTotal: '',
  materialUnit: '',
  carbonFootprint: '',
  carbonFootprintUnit: '',
  contactPerson: '',
  contactWay: '',
  supplierAddress: '',
  remark: ''
})

const form = reactive(defaultForm())

const rules = {
  supplierName:       [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  systemBoundary:     [{ required: true, message: '请选择系统边界',   trigger: 'change' }],
  materialCategory:   [{ required: true, message: '请选择物料分类',   trigger: 'change' }],
  supplyMaterial:     [{ required: true, message: '请输入物料名称',   trigger: 'blur' }],
  materialTotal:      [{ required: true, message: '请输入物料总量',   trigger: 'blur' }],
  materialUnit:       [{ required: true, message: '请输入物料单位',   trigger: 'blur' }],
  carbonFootprint:    [{ required: true, message: '请输入碳足迹总量', trigger: 'blur' }],
  carbonFootprintUnit:[{ required: true, message: '请输入碳足迹单位', trigger: 'blur' }],
}

function boundaryTagType(boundary) {
  if (boundary === '从摇篮到大门') return 'success'
  if (boundary === '从摇篮到坟墓') return 'primary'
  return 'info'
}

async function loadData() {
  loading.value = true
  try {
    const { rows, total: t } = await getSupplyChainCarbonPage({
      supplierName: queryParams.supplierName || undefined,
      materialCategory: queryParams.materialCategory || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    tableData.value = rows || []
    total.value = t || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.supplierName = ''
  queryParams.materialCategory = ''
  queryParams.pageNum = 1
  loadData()
}

function handleAdd() {
  Object.assign(form, defaultForm())
  dialogTitle.value = '添加供应商'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, { ...row })
  dialogTitle.value = '编辑供应商'
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除供应商「${row.supplierName}」？`, '提示', {
    type: 'warning'
  })
  await deleteSupplyChainCarbon(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleSubmit() {
  await formRef.value.validate()
  const payload = {
    ...form,
    materialTotal: Number(form.materialTotal) || 0,
    carbonFootprint: Number(form.carbonFootprint) || 0
  }
  if (form.id) {
    await editSupplyChainCarbon(payload)
    ElMessage.success('修改成功')
  } else {
    await addSupplyChainCarbon(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadData()
}

function resetForm() {
  formRef.value?.resetFields()
}

onMounted(loadData)
</script>
