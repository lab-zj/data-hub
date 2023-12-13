<template>
  <div class="side-panel" :class="{ collapsed: isPanelCollapse }">
    <div v-show="!isPanelCollapse" class="side-panel-title">节点库</div>
    <div class="menu-item" :class="dataGraphStore.graph_running ? 'gray-display' : ''" v-for="menu in menuList" :key="menu.type" @click="handleSubMenu(menu)">
      <!-- @mousedown="() => onStartDrag(menu)" -->
      <icon-font class="menu-icon" :type="menu.icon" /><span v-show="!isPanelCollapse" class="menu-title">{{ menu.name }}</span>
      <!-- <icon-font v-if="menu.type === NODE_TYPES.SQL" type="icon-tugoujian-tianjiajiedian" class="addnode-iconfont" @click.stop="emit('emptyNode', menu.type)" /> -->
    </div>
    <!-- <div class="menu-item" data-type="dataset" @mousedown="onStartDrag"><icon-font class="menu-icon" type="icon-shujuku" />数据节点</div>
    <div class="menu-item" data-type="sql" @mousedown="onStartDrag">SQL节点</div>
    <div class="menu-item" data-type="algo" @mousedown="onStartDrag">算子节点</div> -->
    <div class="btn-collapse" @click="handleCollapse"><icon-font class="menu-icon" type="icon-daohang-cebianshouqi" /> <span v-show="!isPanelCollapse">收起菜单</span></div>
    <component
      v-if="isShowSubPanel"
      :style="bindSubPanelStyle()"
      :is="dynamicSubPanelComponent"
      class="sub-panel"
      @close="handleSubPanelClose"
      @startDrag="onStartDrag"
      @preview="onPreview"
      @locate="onLocate"
      @onUpload="onUpload"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import DatasetSubPanel from './DatasetSubPanel.vue'
import RelationSubPanel from './RelationSubPanel.vue'
import AlgoSubPanel from '../common/AlgoSubPanel.vue'
import { NODE_TYPES } from '@/constants/dataGraph'
import { useDataGraph } from '@/stores/dataGraphStore'

const emit = defineEmits(['startDrag', 'preview', 'collapse', 'emptyNode', 'locate'])

const dataGraphStore = useDataGraph()

const isPanelCollapse = ref(false) // 是否折叠状态
const isShowSubPanel = ref(false)
const activeMenuKey = ref('')

const menuList = [
  {
    name: '数据节点',
    type: NODE_TYPES.DATA,
    icon: 'icon-shujuku'
  },
  {
    name: '关系操作',
    type: NODE_TYPES.SQL,
    icon: 'icon-a-bianzu33'
  },
  {
    name: '算子节点',
    type: NODE_TYPES.ALGO,
    icon: 'icon-a-xingzhuangjiehe2'
  }
]

/**
 * 动态组件：子菜单
 */
const dynamicSubPanelComponent = computed(() => {
  if (activeMenuKey.value === NODE_TYPES.DATA) {
    return DatasetSubPanel
  } else if (activeMenuKey.value === NODE_TYPES.ALGO) {
    return AlgoSubPanel
  } else {
    return RelationSubPanel
  }
})

function onStartDrag(data: { name: string; type: string }) {
  console.log(data)
  if (data) {
    emit('startDrag', data)
  }
}

function onPreview(_data: any) {
  emit('preview', _data)
}

function onLocate(_data: any) {
  emit('locate', _data)
}

/**
 * 左侧菜单选择
 * @param menu
 */
function handleSubMenu(menu: { name: string; type: string }) {
  activeMenuKey.value = menu.type
  isShowSubPanel.value = true
}

/**
 * 折叠左侧菜单
 */
function handleCollapse() {
  isPanelCollapse.value = !isPanelCollapse.value
  emit('collapse', isPanelCollapse.value)
}

/**
 * init subpanel position
 */
function bindSubPanelStyle() {
  if (isShowSubPanel.value === false) {
    return {}
  }
  return {
    top: '108px',
    left: isPanelCollapse.value ? '45px' : '121px'
  }
}

function handleSubPanelClose() {
  isShowSubPanel.value = false
  activeMenuKey.value = ''
}

function onUpload() {
  dataGraphStore.init()
}

defineExpose({
  isPanelCollapse,
  isShowSubPanel
})

watch(
  () => dataGraphStore.graph_running,
  () => {
    if (dataGraphStore.graph_running) {
      isShowSubPanel.value = false
    }
  }
)
</script>

<style lang="less" scoped>
.side-panel {
  width: 120px;
  height: 100%;
  background-color: #f9f9fc;
  font-size: 14px;
  color: #5d637e;
  position: relative;
  transition: all 0.2s ease-in-out;

  .side-panel-title {
    height: 44px;
    line-height: 44px;
    padding-left: 12px;
  }
}
.menu-item {
  height: 36px;
  line-height: 36px;
  padding-left: 20px;
  // border-radius: 4px;
  // text-align: center;
  color: #373b52;
  cursor: pointer;
  white-space: nowrap;
  display: flex;
  align-items: center;

  &:hover {
    background-color: rgba(103, 113, 252, 0.1);
    .addnode-iconfont {
      display: flex !important;
    }
  }
}

.menu-icon {
  vertical-align: middle !important;
  margin-right: 6px;
  font-size: 20px;
  display: flex !important;
}

.gray-display {
  opacity: 0.3;
}

.addnode-iconfont {
  color: #6973ff;
  position: absolute;
  right: -8px;
  z-index: 9;
  display: none !important;
}

.btn-collapse {
  height: 44px;
  line-height: 44px;
  text-align: center;
  border-top: 1px solid #eee;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.side-panel.collapsed {
  width: 64px;
  // overflow: hidden;
  .btn-collapse {
    .menu-icon {
      display: inline-block;
      transform: rotate(180deg);
    }
  }
}

.sub-panel {
  position: fixed;
  left: 180px;
  top: 100px;
  z-index: 9;
  background-color: #f9f9fc;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
}
</style>
