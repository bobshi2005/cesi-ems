<template>
  <div class="page">
    <div class="gs-container">

      <!-- 标题栏 -->
      <div class="gs-filter-bar">
        <span class="gs-filter-label">网关状态监测</span>
      </div>

      <!-- 内容区 -->
      <div class="gs-content">
        <div class="gs-card">
          <div class="gs-grid">
            <div class="gs-table" v-for="(item, index) in dataList" :key="index">
              <div class="gs-row">
                <div class="gs-name">{{ item.name }}</div>
                <div class="gs-cols" :class="{ 'gs-cols-less': item.list.length < 16 }">
                  <div
                    class="gs-col"
                    v-for="(i, inde) in item.list"
                    :key="inde"
                    :class="{ 'gs-col-last': inde === item.list.length - 1 }"
                  >
                    <div class="gs-col-num" v-if="i.title_num">{{ i.title_num }}</div>
                    <div class="gs-col-dots" v-if="i.dot && i.dot.length > 0">
                      <span class="gs-dot" v-for="(j, ind) in i.dot" :key="ind"></span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
let dataList = ref([
  {
    name: "网关",
    list: [
      { id: 1,  title_num: "数据库", dot: [] },
      { id: 2,  title_num: "200331", dot: [] },
      { id: 3,  title_num: "200331", dot: [] },
      { id: 4,  title_num: "200331", dot: [] },
      { id: 5,  title_num: "200331", dot: [] },
      { id: 6,  title_num: "200331", dot: [] },
      { id: 7,  title_num: "200331", dot: [] },
      { id: 8,  title_num: "200331", dot: [] },
      { id: 9,  title_num: "200331", dot: [] },
      { id: 10, title_num: "200331", dot: [] },
      { id: 11, title_num: "200331", dot: [] },
      { id: 12, title_num: "200331", dot: [] },
      { id: 13, title_num: "200331", dot: [] },
      { id: 14, title_num: "200331", dot: [] },
      { id: 15, title_num: "200461", dot: [] },
      { id: 16, title_num: "200475", dot: [] },
    ],
  },
  {
    name: "计算器具",
    list: [
      { id: 1, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }] },
      { id: 2, title_num: "", dot: [{ id: "001" }, { id: "002" }] },
      { id: 3, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }, { id: "004" }] },
    ],
  },
  {
    name: "网关",
    list: [
      { id: 1,  title_num: "200505" },
      { id: 2,  title_num: "200331" },
      { id: 3,  title_num: "200331" },
      { id: 4,  title_num: "200331" },
      { id: 5,  title_num: "200331" },
      { id: 6,  title_num: "200331" },
      { id: 7,  title_num: "200331" },
      { id: 8,  title_num: "200331" },
      { id: 9,  title_num: "200331" },
      { id: 10, title_num: "200331" },
      { id: 11, title_num: "200331" },
      { id: 12, title_num: "200331" },
      { id: 13, title_num: "200331" },
      { id: 14, title_num: "200331" },
      { id: 15, title_num: "200461" },
      { id: 16, title_num: "200604" },
    ],
  },
  {
    name: "计算器具",
    list: [
      { id: 1, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }] },
      { id: 2, title_num: "", dot: [{ id: "001" }, { id: "002" }] },
      { id: 3, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }, { id: "004" }] },
      { id: 4, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }, { id: "004" }, { id: "005" }] },
      { id: 5, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }, { id: "004" }, { id: "005" }, { id: "006" }] },
    ],
  },
  {
    name: "网关",
    list: [
      { id: 1, title_num: "200605" },
      { id: 2, title_num: "200331" },
      { id: 3, title_num: "200331" },
      { id: 4, title_num: "38081703" },
    ],
  },
  {
    name: "计算器具",
    list: [
      { id: 1, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }] },
      { id: 2, title_num: "", dot: [{ id: "001" }, { id: "002" }] },
      { id: 3, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }, { id: "004" }] },
      { id: 4, title_num: "", dot: [{ id: "001" }, { id: "002" }, { id: "003" }, { id: "004" }, { id: "005" }] },
    ],
  },
])
</script>

<style scoped>
/* ── 容器 ──────────────────────────────────────────────────────────────── */
.gs-container {
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 148px);
}

/* ── 标题栏 ────────────────────────────────────────────────────────────── */
.gs-filter-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  border-bottom: 1px solid var(--ems-border-dim);
  flex-shrink: 0;
}
.gs-filter-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ems-text-primary);
  letter-spacing: .5px;
}

/* ── 内容区 ────────────────────────────────────────────────────────────── */
.gs-content {
  flex: 1;
  padding: 14px 16px;
  overflow-y: auto;
}

/* ── 卡片 ──────────────────────────────────────────────────────────────── */
.gs-card {
  background: var(--ems-bg-card);
  border: 1px solid var(--ems-border-dim);
  border-radius: 8px;
  overflow: hidden;
}

/* ── 网格表格 ──────────────────────────────────────────────────────────── */
.gs-grid {
  /* mobile-friendly scrolling when content overflows */
  overflow-x: auto;
}

.gs-table:last-child .gs-row {
  border-bottom: none;
}

.gs-row {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid var(--ems-border-dim);
}

.gs-name {
  width: 80px;
  min-width: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--ems-text-secondary);
  padding: 12px 8px;
  border-right: 1px solid var(--ems-border-dim);
}

.gs-cols {
  flex: 1;
  display: flex;
}

.gs-col {
  flex: 1;
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid var(--ems-border-dim);
  padding: 6px 4px;
}
.gs-col-last {
  border-right: none;
}

.gs-col-num {
  font-size: 12px;
  color: var(--ems-text-primary);
}

/* ── 状态点 ────────────────────────────────────────────────────────────── */
.gs-col-dots {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 6px;
}
.gs-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  background: var(--ems-accent-bright);
}
</style>
