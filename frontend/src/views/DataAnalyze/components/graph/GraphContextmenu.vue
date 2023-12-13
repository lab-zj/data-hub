<template>
  <div class="contextmenu" v-show="contextmenuVisible" :style="contextmenuStyle">
    <div class="menu-list" v-for="list in contextmenuList" :key="list.key" @click="() => onContextMenuClick(list.key)">{{ list.name }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { cloneDeep } from 'lodash'
import { useDataGraph } from '@/stores/dataGraphStore'
import bus from '@/EventBus/eventbus.js'
import { NODE_TYPES } from '@/constants/dataGraph'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'
import { copyToClipboard } from '@/util/util'

interface Props {
  contextmenuVisible: boolean
  contextmenuStyle: any
  // contextmenuTargetNode: any | any[]
  graph: any
  type: string
}

const props = defineProps<Props>()
const dataGraphStore = useDataGraph()
const { deleteFromGraph, onCopyNode, outputUpdate } = useGraphInteraction()

const baseContextmenuList = [
  {
    key: 'copy',
    name: '复制',
    permission: ['multi', NODE_TYPES.ALGO, NODE_TYPES.DATA, NODE_TYPES.SQL]
  },
  {
    key: 'rename',
    name: '重命名',
    permission: [NODE_TYPES.ALGO, NODE_TYPES.DATA, NODE_TYPES.SQL]
  },
  // {
  //   key: 'addOutput',
  //   name: '添加输出',
  //   permission: [NODE_TYPES.ALGO]
  // },
  {
    key: 'setInputParams',
    name: '入参设置',
    permission: [NODE_TYPES.ALGO]
  },
  {
    key: 'delete',
    name: '删除',
    permission: ['multi', NODE_TYPES.ALGO, NODE_TYPES.DATA, NODE_TYPES.SQL]
  },
  {
    key: 'copyID',
    name: '复制ID',
    permission: [NODE_TYPES.ALGO, NODE_TYPES.DATA, NODE_TYPES.SQL]
  }
  // {
  //   key: 'save',
  //   name: '保存',
  //   permission: ['multi', NODE_TYPES.ALGO, NODE_TYPES.DATA, NODE_TYPES.SQL]
  // },
]

const _type: any = computed(() => {
  if (dataGraphStore.targetNodeList.length > 1) {
    return 'multi'
  } else {
    return dataGraphStore.targetNodeList[0]?.data?.type
  }
})
// 边和空数据节点有删除菜单
const contextmenuList: any = computed(() => {
  return props.type === 'node'
    ? baseContextmenuList.filter((menu: any) => menu.permission.includes(_type.value))
    : [
        {
          key: 'delete',
          name: '删除'
        }
      ]
})

function onContextMenuClick(menuKey: string) {
  switch (menuKey) {
    case 'copy':
      onCopyNode()
      break
    case 'addOutput':
      onAddOutput()
      break
    case 'rename':
      onRenameNode()
      break
    case 'setInputParams':
      onSetInputParams()
      break
    case 'delete':
      onDelete()
      break
    case 'save':
      onSave()
      break
    case 'copyID':
      onCopyID()
      break
    default:
  }
}

/**
 * 增加输出
 */
function onAddOutput() {
  if (dataGraphStore.targetNodeList.length === 0) {
    return
  }
  // const node = Array.isArray(props.contextmenuTargetNode) ? props.contextmenuTargetNode[0] : props.contextmenuTargetNode
  const node = dataGraphStore.targetNodeList[0]
  const nodeData = node.getData()
  const algoOutputList = cloneDeep(nodeData.dataset.outputList || [])
  algoOutputList.push({ name: `output${Math.random().toString(36).slice(-8)}` }) // !!!!!! 结合业务替换
  outputUpdate({
    id: nodeData.id,
    outputList: algoOutputList
  })
}

/**
 * 重命名
 */
function onRenameNode() {
  dataGraphStore.graphActionMode = 'rename'
  dataGraphStore.setVisible('nodeOverviewVisible', false)
}

/**
 * 入参数值
 */
function onSetInputParams() {
  bus.emit('set-input-params-for-algo-graph-node')
}
/**
 * 删除：
 *  node 节点
 *  edge 边
 *  empty-dataset 空数据节点
 */
function onDelete() {
  deleteFromGraph({
    list: dataGraphStore.targetNodeList,
    type: props.type
  })
}

function onSave() {
  bus.emit('save-data-to-data-management')
}

function onCopyID() {
  if (dataGraphStore.targetNodeList.length === 0) {
    return
  }
  const node = dataGraphStore.targetNodeList[0]
  const nodeData = node.getData()
  copyToClipboard(nodeData.id)
}
</script>

<style lang="less" scoped>
.contextmenu {
  width: 150px;
  background-color: #fff;
  position: absolute;
  padding: 4px 0;
  box-shadow: 0 3px 6px -4px #0000001f, 0 6px 16px #00000014, 0 9px 28px 8px #0000000d;
}
.menu-list {
  padding: 5px 12px;
  font-size: 12px;
  line-height: 22px;
  cursor: pointer;

  &:hover {
    background-color: #f5f5f5;
  }
}
</style>
