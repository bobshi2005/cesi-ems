<template>
  <router-view />
</template>

<script setup>
import Cookies from "js-cookie"
import useSettingsStore from "@/store/modules/settings"
import { handleThemeStyle } from "@/utils/theme"
import { systemName } from "@/api/system/name"
onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
    const savedTheme = JSON.parse(localStorage.getItem('layout-setting') || '{}')
    const initTheme = savedTheme.sideTheme === 'theme-light' ? 'light' : 'dark'
    document.documentElement.setAttribute('data-theme', initTheme)
  })
  systemName().then((res) => {
    if (res.code == 200) {
      Cookies.set("SystemInfo", JSON.stringify(res.data))
      // Cookies.remove('SystemInfo')
    }
  })
})
</script>
