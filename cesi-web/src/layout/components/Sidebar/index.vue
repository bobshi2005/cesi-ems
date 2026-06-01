<template>
  <div
    :class="{ 'has-logo': showLogo }"
    :style="{ backgroundColor: 'var(--ems-bg-sidebar)' }"
    class="sidebar-container-wrapper"
  >
    <el-scrollbar :class="sideTheme" wrap-class="scrollbar-wrapper" view-class="scrollbar-view">
      <!-- 始终显示菜单项，不再根据路径判断 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="'transparent'"
        :text-color="'var(--ems-text-primary)'"
        :unique-opened="true"
        :active-text-color="theme"
        :collapse-transition="false"
        mode="vertical"
        class="custom-menu"
      >
        <!-- 当前是首页看板子路由时，渲染专用路由 -->
        <template v-if="isIndexSubRoute">
          <sidebar-item
            v-for="(route, index) in indexPageRouters"
            :key="route.path + index"
            :item="route"
            :base-path="route.path"
          />
        </template>
        <template v-else>
          <sidebar-item
            v-for="(route, index) in sidebarRouters"
            :key="route.path + index"
            :item="route"
            :base-path="route.path"
          />
        </template>
      </el-menu>
    </el-scrollbar>
    
    <!-- 底部用户区域 -->
    <div class="sidebar-footer" :class="{ 'collapsed': isCollapse, 'theme-light': sideTheme === 'theme-light' }">
      <div class="user-avatar-container">
        <img :src="userStore.avatar" class="user-avatar" />
      </div>
      
      <!-- 展开状态下显示完整内容 -->
      <div class="user-info" v-if="!isCollapse">
        <div class="username">{{ userStore.name || 'admin' }}</div>
        
        <div class="action-buttons">
          <div class="action-button" :class="{'theme-light': sideTheme === 'theme-light'}" @click="toUserProfile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </div>
          
          <div class="action-button" :class="{'theme-light': sideTheme === 'theme-light'}" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </div>
        </div>
      </div>
      
      <!-- 折叠状态下只显示图标按钮 -->
      <div class="collapsed-actions" v-if="isCollapse">
        <div class="action-icon" :class="{'theme-light': sideTheme === 'theme-light'}" @click="toUserProfile" title="个人中心">
          <el-icon><User /></el-icon>
        </div>
        
        <div class="action-icon" :class="{'theme-light': sideTheme === 'theme-light'}" @click="handleLogout" title="退出登录">
          <el-icon><SwitchButton /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import SidebarItem from "./SidebarItem"
import variables from "@/assets/styles/variables.module.scss"
import useAppStore from "@/store/modules/app"
import useSettingsStore from "@/store/modules/settings"
import usePermissionStore from "@/store/modules/permission"
import useUserStore from "@/store/modules/user"
import { User, Brush, SwitchButton } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const settingsStore = useSettingsStore()
const permissionStore = usePermissionStore()
const userStore = useUserStore()

const sidebarRouters = computed(() => permissionStore.sidebarRouters)

// 判断当前是否为首页子路由(/index/index)
const isIndexSubRoute = computed(() => {
  return route.path === '/index/index'
})

// 判断当前是否为主首页路由(/index或/)
const isMainIndexRoute = computed(() => {
  return route.path === '/index' || route.path === '/'
})

// 首页专用路由，首页看板相关菜单
const indexPageRouters = computed(() => {
  // 查找name为Index的路由
  const indexRoute = sidebarRouters.value.find(route => route.name === 'Index')
  return indexRoute ? [indexRoute] : []
})

const showLogo = computed(() => settingsStore.sidebarLogo)
const sideTheme = computed(() => settingsStore.sideTheme)
const theme = computed(() => settingsStore.theme)
const isCollapse = computed(() => !appStore.sidebar.opened)

const activeMenu = computed(() => {
  const { meta, path } = route
  // if set path, the sidebar will highlight the path you set
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

function toUserProfile() {
  router.push('/user/profile')
}

function toggleTheme() {
  if (settingsStore.sideTheme === 'theme-dark') {
    settingsStore.sideTheme = 'theme-light'
    document.documentElement.setAttribute('data-theme', 'light')
  } else {
    settingsStore.sideTheme = 'theme-dark'
    document.documentElement.setAttribute('data-theme', 'dark')
  }
}

function handleLogout() {
  ElMessageBox.confirm("确定注销并退出系统吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      userStore.logOut().then(() => {
        location.href = "/index"
      })
    })
    .catch(() => {})
}
</script>
<style lang="scss" scoped>
.sidebar-container-wrapper {
  position: relative;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

:deep(.scrollbar-wrapper) {
  height: calc(100% - 220px) !important;
  overflow-x: hidden !important;
}

:deep(.scrollbar-view) {
  height: 100%;
  padding-bottom: 20px;
}

:deep(.el-scrollbar__bar.is-vertical) {
  right: 0;
  width: 6px;
}

:deep(.el-scrollbar__thumb) {
  background-color: rgba(144, 147, 153, 0.3);
  &:hover {
    background-color: rgba(144, 147, 153, 0.5);
  }
}

.custom-menu {
  width: 100%;
  padding: 6px 0;
  height: auto !important;
  transition: all 0.3s ease;

  .el-menu-item {
    height: 38px !important;
    line-height: 38px !important;
    border-radius: 4px;
    margin: 4px 10px;
    width: calc(100% - 20px);
    transition: all 0.2s ease;
    color: var(--ems-text-secondary);

    &.is-active {
      background-color: rgba(0,170,255,.15) !important;
      color: var(--ems-accent-bright) !important;
      font-weight: 600;
      position: relative;
      box-shadow: none;
      &::before {
        content: '';
        position: absolute;
        left: 0; top: 0; bottom: 0;
        width: 2px;
        background: var(--ems-accent);
        border-radius: 0 2px 2px 0;
      }
    }

    &:hover { background-color: rgba(0,170,255,.06) !important; }
  }

  .el-sub-menu {
    .el-sub-menu__title {
      height: 38px !important;
      line-height: 38px !important;
      border-radius: 4px;
      margin: 4px 10px;
      width: calc(100% - 20px);
      transition: all 0.2s ease;
      color: var(--ems-text-secondary);
      &:hover { background-color: rgba(0,170,255,.06) !important; color: var(--ems-text-primary) !important; }
    }

    &.is-active > .el-sub-menu__title {
      color: var(--ems-accent-bright) !important;
      font-weight: 600;
    }

    .el-menu-item {
      padding-left: 45px !important;
      min-width: auto !important;
      &.is-active { padding-left: 45px !important; }
    }

    .el-menu {
      .el-menu-item, .el-sub-menu__title {
        height: 38px !important;
        line-height: 38px !important;
      }
      .el-sub-menu {
        .el-menu-item {
          padding-left: 65px !important;
          &.is-active { padding-left: 65px !important; }
        }
        .el-menu {
          .el-menu-item {
            padding-left: 85px !important;
            &.is-active { padding-left: 85px !important; }
          }
        }
      }
    }
  }
}

// 首页空白菜单区域样式
.home-empty-menu {
  height: auto;
  min-height: 100px;
}

// 底部用户区域样式
.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  border-top: 1px solid var(--ems-border-dim);
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: var(--ems-bg-sidebar);

  &.collapsed {
    padding: 10px;
    .user-avatar-container { margin-bottom: 10px; }
  }

  .user-avatar-container {
    margin-bottom: 10px;
    border: 2px dashed var(--ems-border-mid);
    border-radius: 4px;
    width: 54px;
    height: 54px;
    display: flex;
    align-items: center;
    justify-content: center;
    .user-avatar { width: 38px; height: 38px; border-radius: 4px; }
  }

  .user-info {
    width: 100%;
    text-align: center;
    .username {
      color: var(--ems-text-primary);
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 16px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .action-buttons {
      .action-button {
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(0,170,255,.08);
        border: 1px solid var(--ems-border-dim);
        color: var(--ems-text-primary);
        padding: 10px;
        margin-bottom: 10px;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.2s;
        &:hover { background: rgba(0,170,255,.18); border-color: var(--ems-border-mid); }
        .el-icon { margin-right: 8px; font-size: 16px; color: var(--ems-text-secondary); }
        span { font-size: 14px; }
      }
    }
  }

  .collapsed-actions {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    .action-icon {
      width: 40px;
      height: 40px;
      margin-bottom: 8px;
      background: rgba(0,170,255,.08);
      border: 1px solid var(--ems-border-dim);
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      &:hover { background: rgba(0,170,255,.18); border-color: var(--ems-border-mid); }
      .el-icon { font-size: 20px; color: var(--ems-text-secondary); }
    }
  }
}


// Add global style to override Element Plus defaults
:global(.el-menu--vertical .el-menu-item),
:global(.el-menu--vertical .el-sub-menu__title) {
  height: 38px !important;
  line-height: 38px !important;
}

// 折叠菜单样式
:deep(.custom-menu.el-menu--collapse) {
  width: 54px !important;

  .el-menu-item, .el-sub-menu__title {
    width: 36px !important;
    min-width: 36px !important;
    margin: 4px 9px !important;
    padding: 0 !important;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 4px;
    transition: all 0.2s ease;

    &.is-active {
      background-color: rgba(0,170,255,.15) !important;
      color: var(--ems-accent-bright) !important;
      box-shadow: 0 0 8px var(--ems-accent-glow);
    }

    &:hover { background-color: rgba(0,170,255,.06) !important; }

    .el-icon, .svg-icon {
      margin: 0 !important;
      font-size: 18px !important;
      svg { width: 1.2em; height: 1.2em; }
    }

    .el-sub-menu__icon-arrow { display: none; }
  }

  .el-tooltip__trigger:focus:not(.focusing) { outline: none; }
}
</style>
