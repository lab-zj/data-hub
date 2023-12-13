<template>
  <div class="algo-node">
    <div class="graph-node" :class="[nodeStatusClass]">
      <span v-show="nodeStatusClass === 'running'" class="running-progress"></span>
      <icon-font type="icon-duofenwenjian" class="node-icon" />
      <div class="node-label" v-if="!renameInputVisible" :title="nodeName">{{ nodeName }}</div>
      <el-input
        v-model="nodeName"
        v-show="renameInputVisible"
        ref="renameInput"
        size="small"
        class="input-rename"
        placeholder="Please input"
        @keydown="handleInputKeydown"
        @blur="handelInputBlur"
      />
    </div>
    <!-- 算子输出列表 -->
    <div class="algo-output" v-show="outputList && outputList.length > 0">
      <div
        v-for="list in outputList"
        :key="list.name"
        class="algo-output-list-item ellipsis"
        :class="
          dataGraphStore.targetNodeList && dataGraphStore.targetNodeList[0] && dataGraphStore.targetNodeList[0].id === nodeId && dataGraphStore.current_output === list.name
            ? 'output-background'
            : ''
        "
        :title="list.name"
        @click="viewOutput(list.name)"
      >
        {{ list.name }}
      </div>
      <el-input
        v-model="currentOutputRename"
        v-show="outputRenameInputVisible"
        ref="outputRenameInput"
        size="small"
        class="input-rename"
        placeholder="Please input"
        :style="{
          top: currentOutputRenameIndex === 0 ? 0 : `${currentOutputRenameIndex * (26 + 4)}px`
        }"
        @keydown="handleOutputRenameInputKeydown"
        @blur="handelOutputRenameInputBlur"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, inject, watch } from 'vue'
import IconFont from '@/components/iconfont/IconFont.vue'
import { ElInput } from 'element-plus'
import { useDataGraph } from '@/stores/dataGraphStore'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'
import { checkName, getNodeStatus } from '@/util/util'
import { convertParams } from '../../util/util'

const NODE_WIDTH = 68 // 节点宽度
const NODE_TITLE_HEIGHT = 68 // 节点name区高度
const NODE_OUTPUT_LIST_ITEM_HEIGHT = 26 // 节点输出列表单项高度

const dataGraphStore = useDataGraph()
const { rename, outputUpdate } = useGraphInteraction()

let nodeName = ref('')
let nodeStatusClass = ref('') // 节点状态
const initNodeName = ref('')
const nodeId = ref('')
const getNode = inject('getNode')

const outputList = ref(null)
let outputRenameInputVisible = ref(false)
const outputRenameInput = ref(null)
const currentOutputRename = ref('')
const currentOutputRenameIndex = ref(0)

const renameInputVisible = ref(false)
const renameInput = ref(null)

onMounted(() => {
  initNodeInfo()
  initNodePort()
  initNodeEvents()
})

function initNodeInfo() {
  const node = getNode()
  const nodeData = node.getData()
  setInfo(nodeData)
}

function setInfo(_data: any) {
  const { id, name, status, dataset } = _data
  nodeName.value = name || 'unknown'
  initNodeName.value = name || 'unknown'
  nodeId.value = id || ''
  nodeStatusClass.value = getNodeStatus(status)
  const _outputList = convertParams(dataset.outputParamTemplate) || undefined
  outputList.value = (
    _outputList || [
      {
        name: '默认输出'
      }
    ]
  ).map((output: any) => {
    return {
      ...output,
      isEdit: false
    }
  })
}

/**
 * 初始化节点port
 */
function initNodePort() {
  const node = getNode()
  const listMarginBottom = 4
  const listOffsetX = 16
  const offsetY = NODE_TITLE_HEIGHT + 4 + NODE_OUTPUT_LIST_ITEM_HEIGHT / 2
  const outputPorts: any[] = []
  node.removePorts() // demo里是先全部删除再添加，业务里不可以，会把port相应的连线也直接删除
  // 输出list每个添加port
  outputList.value.forEach((list, index) => {
    outputPorts.push({
      // 输出名称都是唯一的，可以作为唯一标识
      id: list.name,
      group: 'nodePort',
      args: {
        x: NODE_WIDTH + listOffsetX,
        // y: 0,
        y: offsetY + index * (NODE_OUTPUT_LIST_ITEM_HEIGHT + listMarginBottom)
      }
    })
  })

  node.addPorts(outputPorts)

  node.addPort({
    id: 'input_' + Math.random().toString(36).slice(-8), // portid应该需要带上一些信息
    group: 'nodePort',
    args: {
      x: 0,
      y: NODE_TITLE_HEIGHT / 2
    },
    attrs: {
      circle: {
        opacity: 0
      }
    }
  })
}

function initNodeEvents() {
  console.log('initNodeEvents')
  const node = getNode()
  node.on('change:data', ({ current, previous }) => {
    console.log('change:data', current)
    initNodeInfo()
    // 只有节点输出改变，才需要重新initNodePort
    // 重命名这些操作不需要重新initNodePort
    // 直接重新initNodePort，会先移除所有的port，再重新增加port，导致画布上的连线都消失（因为连线id依赖port的id）
    if (current.dataset.outputParamTemplate !== previous.dataset.outputParamTemplate) {
      initNodePort()
    }
  })
}

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

function onDbClick(index: number) {
  outputRenameInputVisible.value = true
  outputRenameInput.value?.focus()
  currentOutputRename.value = outputList.value[index].name
  currentOutputRenameIndex.value = index
}

function handleOutputRenameInputKeydown(event: KeyboardEvent) {
  if (event.key !== 'Enter') {
    return
  }
  ;(event.target as HTMLInputElement).blur()
}

function handelOutputRenameInputBlur(event: FocusEvent) {
  outputRenameInputVisible.value = false
  outputList.value[currentOutputRenameIndex.value].name = currentOutputRename.value
  outputUpdate({
    id: nodeId.value,
    outputList: outputList.value
  })
}

/**
 * 点击查看输出
 */
function viewOutput(_output: string) {
  dataGraphStore.graphActionMode = 'viewoutput'
  dataGraphStore.setVisible('outputOverviewVisible', true)
  dataGraphStore.current_output = _output
}

watch(
  () => dataGraphStore.outputOverviewVisible,
  () => {
    if (!dataGraphStore.outputOverviewVisible) {
      dataGraphStore.current_output = ''
    }
  }
)
</script>

<style lang="less" scoped>
@import '@/views/DataAnalyze/styles/data-graph.less';

.algo-node {
  .algo-output {
    font-size: 12px;
    margin-top: 4px;
    box-sizing: border-box;
    position: relative;
    .algo-output-list-item {
      background-color: #fff;
      line-height: 26px;
      width: 100px;
      height: 26px;
      border-radius: 2px;
      border: 1px solid #eff0fe;
      margin-bottom: 4px;
      margin-left: -16px;
      padding-left: 8px;

      &:hover {
        background-color: #eff0fe;
      }
    }
    .input-rename {
      position: absolute;
      width: 100px;
      height: 26px;
      top: 0;
      left: -16px;
      background-color: #fff;
    }
  }

  .output-background {
    background-color: #eff0fe !important;
  }
  .ellipsis {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }
}
</style>
