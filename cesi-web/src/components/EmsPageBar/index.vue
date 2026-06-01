<template>
  <div class="ems-page-bar">
    <span class="epb-total">共 {{ total }} 条</span>
    <select class="epb-select" :value="modelPageSize" @change="onSizeChange">
      <option v-for="s in pageSizes" :key="s" :value="s">{{ s }}条/页</option>
    </select>
    <div class="epb-btns">
      <button class="epb-btn" :disabled="modelPage <= 1" @click="go(modelPage - 1)">‹</button>
      <template v-for="p in visiblePages" :key="p">
        <span v-if="p === '...'" class="epb-ellipsis">…</span>
        <span v-else class="epb-num" :class="{ active: p === modelPage }" @click="go(p)">{{ p }}</span>
      </template>
      <button class="epb-btn" :disabled="modelPage >= pageCount" @click="go(modelPage + 1)">›</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  total:     { type: Number, default: 0 },
  page:      { type: Number, default: 1 },
  pageSize:  { type: Number, default: 10 },
  pageSizes: { type: Array,  default: () => [10, 20, 50, 100] },
})
const emit = defineEmits(['update:page', 'update:pageSize', 'change'])

const modelPage     = computed(() => props.page)
const modelPageSize = computed(() => props.pageSize)
const pageCount     = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const visiblePages = computed(() => {
  const n = pageCount.value
  const cur = modelPage.value
  if (n <= 7) return Array.from({ length: n }, (_, i) => i + 1)
  const pages = []
  if (cur <= 4) {
    pages.push(1, 2, 3, 4, 5, '...', n)
  } else if (cur >= n - 3) {
    pages.push(1, '...', n-4, n-3, n-2, n-1, n)
  } else {
    pages.push(1, '...', cur-1, cur, cur+1, '...', n)
  }
  return pages
})

function go(p) {
  if (p < 1 || p > pageCount.value || p === modelPage.value) return
  emit('update:page', p)
  emit('change', { page: p, pageSize: props.pageSize })
}
function onSizeChange(e) {
  const s = Number(e.target.value)
  emit('update:pageSize', s)
  emit('update:page', 1)
  emit('change', { page: 1, pageSize: s })
}
</script>

<style scoped>
.ems-page-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  padding: 8px 16px;
  border-top: 1px solid rgba(0, 140, 220, .35);
  margin-top: 4px;
}
.epb-total {
  font-size: 12px;
  color: var(--ems-text-secondary);
  margin-right: 2px;
}
.epb-select {
  background: rgba(255, 255, 255, .06);
  border: 1px solid rgba(0, 140, 220, .35);
  color: var(--ems-text-primary);
  border-radius: 3px;
  padding: 2px 4px;
  font-size: 12px;
  cursor: pointer;
  outline: none;
}
.epb-btns { display: flex; align-items: center; gap: 3px; }
.epb-btn {
  background: rgba(255, 255, 255, .06);
  border: 1px solid rgba(0, 140, 220, .35);
  color: var(--ems-text-primary);
  border-radius: 3px;
  width: 26px; height: 26px;
  font-size: 15px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.epb-btn:disabled { opacity: .3; cursor: not-allowed; }
.epb-btn:not(:disabled):hover { background: rgba(0, 140, 255, .2); color: var(--ems-accent-bright); border-color: rgba(0, 140, 255, .5); }
.epb-num {
  background: rgba(255, 255, 255, .06);
  border: 1px solid rgba(0, 140, 220, .35);
  color: var(--ems-text-primary);
  border-radius: 3px;
  min-width: 26px; height: 26px;
  padding: 0 4px;
  font-size: 12px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.epb-num.active { background: rgba(0, 140, 255, .3); border-color: rgba(0, 140, 255, .7); color: var(--ems-accent-bright); font-weight: 600; }
.epb-num:not(.active):hover { background: rgba(255, 255, 255, .12); color: var(--ems-accent-bright); }
.epb-ellipsis { color: var(--ems-text-muted); font-size: 12px; padding: 0 2px; }
</style>
