<template>
  <div class="page">
    <div class="page-container">
      <div class="page-container-left">
        <LeftTree ref="leftTreeRef" @handleNodeClick="handleNodeClick" ParentModelCode="ElectricityModel" autoSelectFirstLeaf />
      </div>
      <div class="page-container-right">

        <!-- 筛选栏 -->
        <div class="pt-filter-bar">
          <span class="pt-filter-label" v-if="currentNode">{{ currentNode.label }}</span>
          <span class="pt-filter-label" v-else>请在左侧选择节点</span>
          <div class="pt-filter-actions">
            <el-button size="small" icon="Setting" @click="reset" :disabled="!currentNode">重新选择地图</el-button>
            <el-button size="small" type="primary" icon="CircleCheck" @click="handleSaveSetting" :disabled="!currentNode">保存配置</el-button>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="pt-content" v-loading="svgLoading">

          <!-- 未选择节点 -->
          <div v-if="!currentNode" class="pt-empty">
            <el-icon><InfoFilled /></el-icon>
            请在左侧选择节点
          </div>

          <!-- 上传区 -->
          <div v-else-if="filePath === '空节点'" class="pt-upload-card">
            <el-upload class="pt-upload" drag ref="upload" :limit="1"
              :headers="uploadData.headers" :action="uploadData.url" :with-credentials="true"
              :on-success="handleFileSuccess" :show-file-list="false">
              <el-icon class="pt-upload-icon"><UploadFilled /></el-icon>
              <div class="pt-upload-text">将 SVG 文件拖到此处，或<em>点击上传</em></div>
            </el-upload>
          </div>

          <!-- SVG 展示区 -->
          <div v-else class="pt-svg-card">
            <div class="pt-svg-head">{{ currentNode.label }} · 电力组态图</div>
            <div class="pt-svg-body" id="svgHtml" v-html="svgHtml" />
          </div>

        </div>

      </div>
    </div>
  </div>
</template>

<script setup name="powerTopologymap">
import { getConfigure, updateEquipmentfile, saveSetting } from "@/api/realTimeMonitor/realTimeMonitor"
import { getToken } from "@/utils/auth"
import { InfoFilled, UploadFilled } from "@element-plus/icons-vue"

const { proxy }     = getCurrentInstance()

const svgLoading        = ref(false)
const currentNode       = ref(null)
const filePath          = ref(null)
const svgHtml           = ref("")

const uploadData = {
  headers: { Authorization: "Bearer " + getToken() },
  url: import.meta.env.VITE_APP_BASE_API + "/equipmentFile/upload",
}

function handleNodeClick(data) {
  currentNode.value = data
  svgHtml.value = ""
  filePath.value = null
  getConfigureList(data.id)
}

function getConfigureList(id) {
  svgLoading.value = true
  getConfigure(id).then((response) => {
    svgLoading.value = false
    filePath.value = "空节点"
    svgHtml.value = ""
    if (response.code === 200 && response.data) {
      filePath.value = import.meta.env.VITE_APP_BASE_API + response.data.filePath
      if (response.data.filePath) {
        getSvg()
      }
    }
  }).catch(() => { svgLoading.value = false })
}

function handleFileSuccess(response, file, fileList) {
  if (response.code === 200) {
    updateEquipmentfile({
      nodeId: currentNode.value?.id,
      filePath: response.msg,
      svgType: "COLLECT",
    }).then((result) => {
      if (result.code === 200) {
        filePath.value = import.meta.env.VITE_APP_BASE_API + response.msg
        getSvg()
      } else {
        proxy.$modal.msgError(result.msg)
      }
    })
  } else {
    proxy.$modal.msgError(response.msg)
  }
}

function getSvg() {
  const xhr = new XMLHttpRequest()
  xhr.open("GET", filePath.value, true)
  xhr.send()
  xhr.addEventListener("load", () => {
    svgHtml.value = xhr.responseText
  })
}

function reset() {
  filePath.value = "空节点"
  svgHtml.value = ""
}

function handleSaveSetting() {
  if (!currentNode.value) return
  const values = []
  const parser = new DOMParser()
  const doc = parser.parseFromString(svgHtml.value, "image/svg+xml")
  const textEls = doc.getElementsByTagName("text")
  for (let i = 0; i < textEls.length; i++) {
    if (textEls[i].getAttribute("id") != undefined) {
      values.push({
        param: textEls[i].textContent,
        tag: "",
        tagType: "COLLECT",
      })
    }
  }
  saveSetting(currentNode.value.id, values).then((response) => {
    if (response.code === 200) {
      proxy.$modal.msgSuccess(response.msg)
    } else {
      proxy.$modal.msgError(response.msg)
    }
  })
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
.pt-filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.pt-filter-label {
  font-size: 12px;
  color: var(--ems-text-secondary);
  white-space: nowrap;
}
.pt-filter-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.pt-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 空状态 ────────────────────────────────────────────────────────────── */
.pt-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--ems-text-muted);
  font-size: 13px;
  margin-top: 80px;
}
.pt-empty .el-icon { font-size: 36px; opacity: .5; }

/* ── 上传卡片 ──────────────────────────────────────────────────────────── */
.pt-upload-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  padding: 40px;
  display: flex;
  justify-content: center;
  min-height: 300px;
}
.pt-upload {
  width: 100%;
  max-width: 500px;
}
.pt-upload-icon {
  font-size: 48px;
  color: var(--ems-text-muted);
  margin-bottom: 12px;
}
.pt-upload-text {
  color: var(--ems-text-secondary);
  font-size: 13px;
}
.pt-upload-text em {
  color: var(--ems-accent);
  font-style: normal;
}

/* ── SVG 卡片 ──────────────────────────────────────────────────────────── */
.pt-svg-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
  height: calc(100vh - 210px);
  display: flex;
  flex-direction: column;
}
.pt-svg-head {
  padding: 9px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  border-bottom: 1px solid var(--ems-border-dim);
  letter-spacing: .5px;
  flex-shrink: 0;
}
.pt-svg-body {
  flex: 1;
  overflow: auto;
  padding: 12px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.pt-svg-body :deep(svg) {
  max-width: 100%;
  height: auto;
}
</style>
