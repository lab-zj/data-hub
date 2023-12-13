<template>
  <el-tabs v-if="isShow" v-model="currentMenu" class="nav" @tab-click="toPage">
    <el-tab-pane v-for="target in navigationTargets" :key="target.path" :label="target.name" :name="target.path" class="menu-item ant-menu-item"> </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { watch, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import bus from '@/EventBus/eventbus.js'
const navigationTargets = [
  {
    name: '项目管理',
    path: '/management/projects'
  },
  {
    name: '数据管理',
    path: '/management/data'
  }
]
const route = useRoute()
const router = useRouter()
let currentMenu: any = ref('')

watch(
  () => route.path,
  () => {
    currentMenu.value = route.path.startsWith('/management/project') ? '/management/projects' : '/management/data'
  },
  {
    immediate: true
  }
)

const toPage = (panel: any) => {
  currentMenu.value = panel.props.name
  router.push(currentMenu.value)
}

const isShow: any = ref(true)

onMounted(() => {
  bus.on('displayNavBar', (param: any) => {
    isShow.value = param
  })
})
</script>

<style lang="less" scoped>
@import '@/constants';
.el-tabs {
  color: #5d637e;
  font-weight: bold;
  line-height: 43px;
  --el-tabs-header-height: 44px;
}
.nav {
  height: 44px;
  background-color: #fff;
  padding: 0 @PAGE_HORIZONTAL_PADDING 0 45px;

  :deep(.el-tabs__nav-wrap::after) {
    position: static;
  }
}

.menu-item > a {
  color: @TEXT_COLOR;
  display: inline-block;
  font-size: @TEXT_FONT_SIZE;
  text-align: center;
}
</style>
