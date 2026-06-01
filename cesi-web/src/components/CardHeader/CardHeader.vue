<template>
  <div class="header">
    <slot></slot>
    <div class="btn-list" v-if="props.showBtn">
      <div
        class="btn-list-item"
        :class="{ active: timeType == dict.value }"
        v-for="dict in props.period"
        :key="dict.value"
        @click="handleClick(dict.value)"
      >
        {{ dict.label }}
      </div>
    </div>
  </div>
</template>

<script setup>
const emit = defineEmits()
const props = defineProps(["showBtn", "period", "active"])
const data = reactive({
  timeType: "DAY",
})
const { timeType } = toRefs(data)
handleClick(timeType.value)

function handleClick(value) {
  timeType.value = value
  emit("handleClick", timeType.value, props.active)
}
</script>

<style lang="scss" scoped>
.header {
  height: 26px;
  font-weight: bold;
  font-size: 18px;
  color: var(--ems-text-primary);
  line-height: 29px;
  text-align: left;
  display: flex;
  justify-content: space-between;

  .btn-list {
    display: flex;

    .btn-list-item {
      width: 46px;
      height: 32px;
      line-height: 32px;
      background: rgba(0,170,255,.08);
      border: 1px solid var(--ems-border-dim);
      text-align: center;
      font-weight: 500;
      font-size: 14px;
      color: var(--ems-text-secondary);
      cursor: pointer;
      transition: all .2s;
      &:hover { background: rgba(0,170,255,.15); color: var(--ems-accent-bright); }
    }

    :first-child { border-radius: 4px 0 0 4px; }
    :last-child  { border-radius: 0 4px 4px 0; }

    .active {
      background: var(--ems-accent);
      border-color: var(--ems-accent);
      color: #fff;
    }
  }
}
</style>
