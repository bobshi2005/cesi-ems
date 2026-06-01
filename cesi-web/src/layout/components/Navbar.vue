<template>
  <div class="navbar">
    <div class="navbar-left">
      <hamburger
        id="hamburger-container"
        :is-active="appStore.sidebar.opened"
        class="hamburger-container"
        @toggleClick="toggleSideBar"
      />
      <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!settingsStore.topNav" />
      <top-nav id="topmenu-container" class="topmenu-container" v-if="settingsStore.topNav" />
    </div>

    <div class="right-menu">
      <!-- 报警按钮 -->
      <div class="right-menu-item hover-effect nav-btn-item">
        <el-tooltip content="报警" effect="dark" placement="bottom">
          <div class="nav-btn" @click="handleAlarm">
            <img src="@/assets/images/alarm.png" alt="报警" />
            <span>报警</span>
          </div>
        </el-tooltip>
      </div>
      
      <!-- 大模型按钮 -->
      <div class="right-menu-item hover-effect nav-btn-item">
        <el-tooltip content="大模型" effect="dark" placement="bottom">
          <div class="nav-btn" @click="handleRobot">
            <img src="@/assets/images/robot.png" alt="大模型" />
            <span>大模型</span>
          </div>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup>
import Breadcrumb from "@/components/Breadcrumb"
import TopNav from "@/components/TopNav"
import Hamburger from "@/components/Hamburger"
import useAppStore from "@/store/modules/app"
import useSettingsStore from "@/store/modules/settings"
import { useRouter } from "vue-router"

const appStore = useAppStore()
const settingsStore = useSettingsStore()
const router = useRouter()

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleAlarm() {
  // 跳转到报警管理页面
  router.push('/realtime/alarmmanage/measuremen?modelCode=BJGL')
}

function handleRobot() {
  // 跳转到智能助手页面
  window.open('https://deepseek.com', '_blank')
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 70px;
  background: var(--ems-bg-header);
  border-bottom: 1px solid var(--ems-border-dim);
  display: flex;
  justify-content: space-between;
  align-items: center;

  .navbar-left {
    display: flex;
    align-items: center;
  }

  .hamburger-container {
    margin-right: 6px;
    cursor: pointer;
    transition: background .3s;
    -webkit-tap-highlight-color: transparent;
    color: var(--ems-text-secondary);
    &:hover { background: rgba(0,170,255,.06); }
  }

  .breadcrumb-container { float: left; }
  .topmenu-container { position: absolute; left: 50px; }
  .errLog-container { display: inline-block; vertical-align: top; }

  .right-menu {
    display: flex;
    &:focus { outline: none; }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: var(--ems-text-secondary);
      vertical-align: text-bottom;
      &.hover-effect {
        cursor: pointer;
        transition: background .3s;
        &:hover { background: rgba(0,170,255,.06); }
      }
    }

    .nav-btn-item {
      display: flex;
      align-items: center;
      margin-right: 20px;
      height: 70px;

      .nav-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: var(--ems-text-primary);
        background-color: rgba(0,170,255,.08);
        border: 1px solid var(--ems-border-dim);
        border-radius: 4px;
        padding: 7px 14px;
        transition: all .2s;
        &:hover {
          background-color: rgba(0,170,255,.18);
          border-color: var(--ems-border-mid);
        }
        img { width: 20px; height: 20px; margin-right: 6px; }
        span { font-size: 14px; font-weight: 500; }
      }
    }
  }
}
</style>
