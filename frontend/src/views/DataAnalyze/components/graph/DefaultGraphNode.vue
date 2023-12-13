<template>
  <div class="graph-node" :class="[nodeStatusClass]">
    <!-- <icon-font v-if="isSelected" :type="['init','start'].includes(nodeStatusClass) ? 'icon-zantingyunhang' : 'icon-yunhang'" class="action-icon" @click="onClick" /> -->
    <span v-show="nodeStatusClass === 'running'" class="running-progress"></span>
    <icon-font :type="iconType" class="node-icon" />
    <div v-if="!renameInputVisible" class="node-label" :title="nodeName">{{ nodeName }}</div>
    <el-input
      v-model="nodeName"
      v-show="renameInputVisible"
      ref="renameInput"
      size="small"
      class="input-rename"
      placeholder="请输入"
      @keydown="handleInputKeydown"
      @blur="handelInputBlur"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, inject, watch, shallowRef } from 'vue'
import IconFont from '@/components/iconfont/IconFont.vue'
import { ElInput } from 'element-plus'
import { useDataGraph } from '@/stores/dataGraphStore'
import { getFileIconFont, getNodeStatus, checkName } from '@/util/util'
import { NODE_TYPES } from '@/constants/dataGraph'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'

const NODE_WIDTH = 68 // 节点宽度
const NODE_HEIGHT = 68

let nodeName = ref('')
let initNodeName = ref('')
let nodeType = ref('') // 节点类型
const getNode = inject('getNode')
const getGraph = inject('getGraph')
let nodeStatusClass = ref('') // 节点状态
let renameInputVisible = ref(false)
const renameInput = ref(null)
const iconType = ref('')
let nodeId = ref('')
const isSelected = ref(false)

const dataGraphStore = useDataGraph()
const { rename } = useGraphInteraction()

onMounted(() => {
  console.log('defaultgraphnode  mounted')
  initNodeInfo()
  initNodePort()
})

watch(
  () => dataGraphStore.graphActionMode,
  (newValue, oldValue) => {
    const node = getNode()
    if (newValue === 'rename' && dataGraphStore.targetNodeList[0].id === node.id) {
      renameInputVisible.value = true
      renameInput.value?.focus()
    }
  }
)

watch(
  () => dataGraphStore.targetNodeList,
  () => {
    const node = getNode()
    const graph = getGraph()
    isSelected.value = graph.isSelected(node)
  },
  { immediate: true, deep: true }
)

function initNodeInfo() {
  const node = getNode()
  const nodeData = node.getData()
  setInfo(nodeData)
  node.on('change:data', ({ current }: { current: any }) => {
    setInfo(current)
  })
}

function setInfo(_data: any) {
  const { id, type, name, status, dataset } = _data
  nodeId.value = id || ''
  nodeType.value = type || ''
  nodeName.value = name || 'unknown'
  initNodeName.value = name || 'unknown'
  nodeStatusClass.value = getNodeStatus(status)
  iconType.value = getFileIcon({
    type,
    dataset
  })
}

/**
 * 初始化节点port，只有输出port
 */
function initNodePort() {
  const node = getNode()

  // 输出port
  node.addPort({
    id: 'output+' + Math.random().toString(36).slice(-8), // portid应该需要带上一些信息
    group: 'nodePort',
    args: {
      x: NODE_WIDTH,
      y: NODE_HEIGHT / 2
    }
  })
  // 所有节点的前面都可以接入其他节点（数据节点前面也可以接入其他节点）
  node.addPort({
    id: 'input_' + Math.random().toString(36).slice(-8), // portid应该需要带上一些信息
    group: 'nodePort',
    args: {
      x: 0,
      y: NODE_HEIGHT / 2
    },
    attrs: {
      circle: {
        opacity: 0
      }
    }
  })
}

function handleInputKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter') {
    return
  }
  ;(event.target as HTMLInputElement).blur()
}

function handelInputBlur(event: FocusEvent) {
  console.log('onblur', nodeName.value)
  if (!checkName(nodeName.value, initNodeName.value)) {
    renameInputVisible.value = false
    nodeName.value = initNodeName.value
    return
  }
  renameInputVisible.value = false
  rename({
    id: nodeId.value,
    _name: nodeName.value
  })
}

/**
 * 获取文件类型对应图标
 */
function getFileIcon({ type, dataset }: { type: string; dataset: any }) {
  let suffix = ''
  // sql类型
  if (type === NODE_TYPES.SQL) {
    return 'icon-sql2'
  }
  // dataset类型
  const { directory: isDir, name } = dataset
  if (isDir) {
    suffix = '/'
  } else {
    suffix = name.slice(name.lastIndexOf('.') + 1)
  }

  return getFileIconFont(suffix)
}
</script>

<style lang="less" scoped>
@import '@/views/DataAnalyze/styles/data-graph.less';
</style>
