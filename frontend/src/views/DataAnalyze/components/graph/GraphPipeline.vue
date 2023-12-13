<template>
  <div class="graph-content">
    <GraphToolBar />
    <div id="graphContainer"></div>
    <div v-if="dataGraphStore.rawData.nodes.length === 0" class="graph-placeholder">
      <div class="rect">
        <icon-font type="icon-jiahao1" class="add-icon" />
      </div>
      <p class="tips">请从左侧栏拖入节点</p>
    </div>
    <GraphContextmenu :type="contextmenuType" :contextmenu-visible="contextmenuVisible" :contextmenu-style="contextmenuStyle" :graph="graph" />
  </div>
</template>
<script setup lang="ts">
import { Graph, Node, Edge } from '@antv/x6'
import { Dnd } from '@antv/x6-plugin-dnd'
import { Selection } from '@antv/x6-plugin-selection'
import { Clipboard } from '@antv/x6-plugin-clipboard'
import { register } from '@antv/x6-vue-shape'
import { onMounted, ref, watch, onBeforeUnmount } from 'vue'
import { useDataGraph } from '@/stores/dataGraphStore'
import { PORT_CONFIG, NODE_TYPES, EDGE_LINE_STYLES } from '@/constants/dataGraph'
import GraphContextmenu from './GraphContextmenu.vue'
import EmptyGraphNode from './EmptyGraphNode.vue'
import GraphToolBar from './GraphToolBar.vue'
import DefaultGraphNode from './DefaultGraphNode.vue'
import AlgoGraphNode from './AlgoGraphNode.vue'
import bus from '@/EventBus/eventbus.js'
import { throttle } from 'lodash'
import useGraphDataFlow from '@/views/DataAnalyze/hooks/useGraphDataFlow'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'

let graph: any = null
let dnd: any = null

const dataGraphStore = useDataGraph()
const contextmenuVisible = ref(true)
const contextmenuType = ref('node')
const contextmenuStyle = ref({
  top: '-9999px',
  left: '-9999px'
})
const translateX = ref(0)
const translateY = ref(0)
// 框选n个节点，一起移动
const selectionHasMovedCount = ref(0)

const { getGraphListOfProject, getGraph, getGraphJobStatus, getGraphTaskDetailsBatch, upsertTaskNode, saveGraph, setStatusesInterval, clearStatusesInterval } = useGraphDataFlow()
const { nodeAddedEvent } = useGraphInteraction()
const emit = defineEmits(['close-side'])

watch(
  () => dataGraphStore.status_list,
  () => {
    // 每次更新完状态都需要更新画布信息
    dataGraphStore.packageRawData()
    // renderData()
    rerenderData()
  },
  {
    deep: true
  }
)

watch(
  () => dataGraphStore.detail_list,
  () => {
    // 更新画布信息
    dataGraphStore.packageRawData()
  },
  {
    deep: true
  }
)

// watch(
//   () => dataGraphStore.projectId,
//   () => {
//     init()
//   },
//   {
//     immediate: true
//   }
// )

watch(
  () => dataGraphStore.graph_running,
  () => {
    if (dataGraphStore.graph_running) {
      setStatusesInterval()
    } else {
      clearStatusesInterval()
    }
  }
)

watch(
  () => dataGraphStore.targetNodeList,
  () => {
    const nodes = graph.getNodes()?.filter((node: any) => !dataGraphStore.targetNodeList.some((_node: any) => +_node.id === +node.id))
    hiddenPorts(nodes)
  }
)

onMounted(async () => {
  initGraph()
  registerCustomGraphNode()
  initGraphEvents()
  initOtherEvents()
  init()
})

async function init() {
  await getBaseInfo()
  renderData()
  resetStore()
}

function initGraph() {
  if (document.getElementById('graphContainer')) {
    graph = new Graph({
      // @ts-ignore
      container: document.getElementById('graphContainer'),
      width: document.body.clientWidth - 120,
      height: document.body.clientHeight - 64 - 44,
      // autoResize: true, // 监听容器大小改变，并自动更新画布大小
      panning: true, // 鼠标拖拽平移
      // mousewheel: true, // 鼠标滚轮缩放 是否开启得确认下画布最终交互，拖拽和框选
      background: {
        color: '#fff'
      },
      connecting: {
        router: {
          name: 'manhattan',
          args: {
            startDirections: ['right'],
            endDirections: ['left']
            // padding: 30
          }
        },
        allowBlank: false, // 是否允许连接到画布空白位置的点
        allowLoop: false,
        allowEdge: false,
        allowMulti: 'withPort', // 起始和终止节点之间可以创建多条边，但必须要要链接在不同的连接桩上
        createEdge() {
          return this.createEdge({
            shape: 'edge',
            attrs: {
              line: EDGE_LINE_STYLES.LINE
            }
          })
        },
        validateConnection: validateConnection
      },
      highlighting: {
        // 当连接桩可以被链接时，在连接桩外围渲染一个 1px 宽的蓝色矩形框
        magnetAdsorbed: {
          name: 'stroke',
          args: {
            // padding: 4,
            attrs: {
              'stroke-width': 1,
              stroke: '#6973FF'
            }
          }
        }
      }
    })

    graph.use(
      new Selection({
        className: 'ccccc',
        enabled: true,
        multiple: true,
        // rubberband: true, // 框选节点，开启后框选和拖动画布会冲突
        movable: true,
        showNodeSelectionBox: true
      })
    )
    graph.use(
      new Clipboard({
        enabled: true
      })
    )

    dnd = new Dnd({
      target: graph,
      getDragNode: (node) => node.clone({ keepId: true }),
      getDropNode: (node) => node.clone({ keepId: true }),
      validateNode: validateNode
    })

    dataGraphStore.graph = graph
  }
}

function initGraphEvents() {
  graph.on('edge:connected', ({ isNew, edge, currentCell, currentPort }: { isNew: boolean; edge: Edge; currentCell: Node; currentPort: string }) => {
    dataGraphStore.init()
    if (isNew) {
      console.log('currentCell', currentCell, 'currentPort', currentPort, edge)
      if (!currentPort) {
        // 边连线连接到的是节点，非port
        const ports: any[] = currentCell.getPorts()
        const inputPort = ports.find((port) => port.id.includes('input'))
        edge.setTarget({ cell: currentCell.id, port: inputPort.id })
      }
    }
    const { source, target, labels } = edge
    const edgeId = `${source.cell}${source.port ? '##' : ''}${source.port || ''}##${target.cell}${target.port ? '##' : ''}${target.port || ''}`
    graph.updateCellId(edge, edgeId)
    console.log('graph:', graph.getNodes(), graph.getEdges())
    // 调用saveGraph接口，保存画布信息（这里需要构造edge的id）sourceCellId##portId##targetCellId##portId##随机数
    dataGraphStore.addEdge({
      id: edgeId,
      name: labels.length > 0 ? labels[0].attrs.label.text : '',
      source,
      target
    })
    saveGraph()
  })

  graph.on('node:mouseenter', ({ node }: { node: Node }) => {
    const ports = node.getPorts()
    ports.forEach((port) => {
      if (!port.id?.includes('input')) {
        // hover时显示port
        node.setPortProp(port.id, 'attrs/circle', {
          visibility: 'visible'
        })
      }
    })
  })

  graph.on('node:mouseleave', ({ node }: { node: Node }) => {
    const ports = node.getPorts()
    ports.forEach((port) => {
      // hover 移出时隐藏port
      node.setPortProp(port.id, 'attrs/circle', {
        visibility: 'hidden'
      })
    })
  })

  // 右键所触发的监听事件顺序是：contextmenu、selected
  graph.on('node:contextmenu', onContextMenu)

  graph.on('edge:contextmenu', ({ x, y, edge }: { x: number; y: number; edge: any }) => {
    contextmenuType.value = 'edge'
    const newPosition = graph.graphToLocal(x, y)
    contextmenuStyle.value = {
      top: `${newPosition.y + 5 + translateY.value * 2}px`,
      left: `${newPosition.x + translateX.value * 2}px`
    }
    contextmenuVisible.value = true
    dataGraphStore.setTargetNodeList([edge])
  })

  // // 通过dnd插件拖拽到画布上之后，要通过node:added事件来获取position
  // graph.on('node:added', async ({ node }: { node: any }) => {
  //   // const { operateType } = node.getProp() || {}
  //   // if (operateType === 'drag') {
  //   //   nodeAddedEvent(node)
  //   //   await saveGraph()
  //   // }
  // })

  // graph.on('node:selected', ({ node }: { node: Node }) => {
  //   console.log('selected:', node)
  //   // if (dataGraphStore.graphActionMode === 'rename') {
  //   //   return
  //   // }
  // })

  // 点击节点所触发的监听事件顺序是：selected、click
  // 将详情打开逻辑写在这里，就不会影响定位、菜单功能
  graph.on('node:click', ({ node }: { node: Node }) => {
    if (node.shape === 'empty-node') {
      return
    }
    // click单选或者ctrl+click之后，工具栏需要出复制、删除按钮
    setSelected(node)
  })

  graph.on('node:moved', (parameter: any) => {
    const { cell, x, y } = parameter
    const data = cell.getData()
    const isMultiSelection = graph.getSelectedCells()?.length > 1
    // 框选的情况下，整个框移动
    if (isMultiSelection) {
      selectionHasMovedCount.value += 1
    }
    console.log('moved:', isMultiSelection, data.position, x, y, cell.position())
    // 更新rawData，并保存画布saveGraph
    dataGraphStore.updateNode(cell.id, {
      ...data,
      position: {
        x: cell.position().x,
        y: cell.position().y
      }
    })
    // 框选的情况下，框中的每个节点都会触发node:moved方法，每次都saveGraph的话，无法保证异步请求顺序，有些节点移动后的位置会被覆盖，出现异常
    if (!isMultiSelection || selectionHasMovedCount.value === graph.getSelectedCells()?.length) {
      saveGraph()
      selectionHasMovedCount.value = 0
    }
  })

  graph.on('node:port:mouseenter', () => {
    // 如果一个节点被选中，此时，去连该节点，无反应，在这里，提前清空选区
    graph.cleanSelection()
  })

  graph.on('translate', ({ tx, ty }) => {
    translateX.value = tx
    translateY.value = ty
  })

  graph.on('blank:click', reset)

  document.addEventListener('click', onDocumentClick)

  resizeListener()
}

function registerCustomGraphNode() {
  const nodeList = [
    {
      shape: 'default-node',
      component: DefaultGraphNode,
      config: {
        width: 68,
        height: 68,
        ports: PORT_CONFIG
      }
    },
    {
      shape: 'algo-node',
      component: AlgoGraphNode,
      config: {
        width: 68,
        height: 68,
        ports: PORT_CONFIG
      }
    },
    {
      shape: 'empty-node',
      component: EmptyGraphNode,
      config: {
        width: 68,
        height: 68,
        ports: PORT_CONFIG
      }
    }
  ]

  nodeList.forEach((list) => {
    register({
      shape: list.shape,
      component: list.component,
      ...list.config
    })
  })
}

/**
 * 校验能否连接
 * [后续支持]1、算子节点的输出可以连到一个数据节点上，算子节点执行完之后，输出内容会覆盖该数据集节点（此处数据节点是一个正常的拖过来有内容的数据节点）
 * 2、sql节点的输入只能是table表格，本期是csv格式
 * @param parameter
 */
function validateConnection(parameter: any) {
  console.log('validateConnection:', parameter)
  const { sourceCell, targetCell } = parameter
  const targetNodeType = targetCell.getData().type
  const sourceCellType = sourceCell.getData().type
  // if (targetNodeType === NODE_TYPES.DATA) {
  //   // 本期设置：不能连接到dataset类型的数据集节点
  //   return false
  // }

  // if (targetNodeType === NODE_TYPES.SQL && (sourceCellType !== NODE_TYPES.DATA || sourceCell.getData()?.dataset?.type !== 'csv')) {
  //   // sql节点的输入只能是csv类型的数据集
  //   return false
  // }

  return true
}

/**
 * 设置选中节点
 * @param node
 */
function setSelected(node: any) {
  if (isMultiSelect(node)) {
    dataGraphStore.setTargetNodeList(graph.getSelectedCells())
  } else {
    graph.resetSelection(node)
    dataGraphStore.setTargetNodeList([node])
  }
}

/**
 * 判断当前是否是多选
 */
function isMultiSelect(node: Node) {
  if (graph.isSelectionEmpty()) {
    // 选区为空，即交互的只有当前右键的节点
    return false
  }
  if (graph.isSelected(node)) {
    // 选区非空，且node节点在选区中，视为当前交互的是多个节点
    return true
  }
  return false // 选区非空，且node节点未在选区中，视为切换交互焦点
}

/**
 * 右键菜单
 * @param param0
 */
function onContextMenu({ x, y, node }: { x: number; y: number; node: Node }) {
  console.log(arguments, node.data)
  console.log(graph.graphToLocal(x, y))
  contextmenuType.value = node.data.type === 'empty-dataset' ? 'empty-dataset' : 'node'
  const newPosition = graph.graphToLocal(x, y)
  contextmenuStyle.value = {
    top: `${newPosition.y + 5 + translateY.value * 2}px`,
    left: `${newPosition.x + translateX.value * 2}px`
  }
  contextmenuVisible.value = true

  setSelected(node)
  dataGraphStore.graphActionMode = 'contextmenu'
}

function onDocumentClick() {
  contextmenuVisible.value = false
  contextmenuStyle.value = {
    top: '-9999px',
    left: '-9999px'
  }
}

async function onStartDrag(_node: any) {
  console.log('onStartDrag:', _node)
  const node = graph.createNode({
    shape: _node.type === NODE_TYPES.ALGO ? 'algo-node' : 'default-node',
    height: 68,
    width: 68,
    data: {
      ..._node,
      status: ''
    }
  })
  dnd.start(node, event)
}

/**
 * dnd插件校验函数：调用validateNode 方法来验证节点是否可以被添加到目标画布中
 * 该验证方法支持异步验证，例如发送接口到远端验证或者将新节点插入到数据库
 * @param _node
 */
async function validateNode(_node: any) {
  const data = _node.getData()
  const { taskUuid, taskId } = await upsertTaskNode(data)
  console.log('validateNode:', _node)
  _node.id = taskUuid
  _node.setData({
    ...data,
    status: '',
    id: taskUuid,
    taskId
  })
  nodeAddedEvent(_node)
  await saveGraph()
  return taskUuid
}

/**
 * 重置store中数据
 */
function resetStore() {
  dataGraphStore.cleanTargetNodeList()
}

/**
 * 数据渲染
 */
function renderData() {
  const _data = processData(dataGraphStore.rawData)
  graph.unselect(dataGraphStore.targetNodeList)
  graph.fromJSON(_data)
  graph.resetSelection(dataGraphStore.targetNodeList)
}

/**
 * 重新渲染
 */
function rerenderData() {
  dataGraphStore.status_list?.forEach((_status: any) => {
    const { taskUuid } = _status
    const _cell = graph.getCellById(taskUuid)
    if (_cell) {
      _cell.setData({
        status: _status
      })
    }
  })
}

/**
 * 数据处理：转为图渲染数据
 * @param param0
 */
function processData({ nodes, edges }: { nodes: any; edges: any }) {
  const _nodes = nodes.map((node: any) => {
    const { id, type, position } = node
    return {
      id,
      shape: type === NODE_TYPES.ALGO ? 'algo-node' : 'default-node',
      position,
      data: {
        ...node
      }
    }
  })
  const _edges = edges.map((edge: any) => {
    return {
      ...edge,
      label: edge.name,
      attrs: {
        line: EDGE_LINE_STYLES.LINE,
        text: EDGE_LINE_STYLES.TEXT
      }
      // connector: { name: 'rounded' }
    }
  })
  return {
    nodes: _nodes,
    edges: _edges
  }
}

/**
 * 获取并组装画布数据
 */
async function getBaseInfo() {
  await getGraphListOfProject()
  await getGraph()
  await getGraphTaskDetailsBatch()
  await getGraphJobStatus()
  dataGraphStore.packageRawData()
}

function reset() {
  bus.emit('reset-on-data-analyze')
  resetOnGraph()
  dataGraphStore.graphActionMode = ''
  emit('close-side')
  hiddenPorts(graph.getNodes())
}

function resetOnGraph() {
  graph.cleanSelection()
  dataGraphStore.cleanTargetNodeList()
}
// 隐藏port
function hiddenPorts(nodes: any) {
  nodes.forEach((node: any) => {
    const ports = node.getPorts()
    ports.forEach((port) => {
      // hover 移出时隐藏port
      node.setPortProp(port.id, 'attrs/circle', {
        visibility: 'hidden'
      })
    })
  })
}

const isPanelCollapse: any = ref(false)

function setIsPanelCollapse(isCollapse: boolean) {
  isPanelCollapse.value = isCollapse
}

function resizeGraph() {
  const sidePanelWidth = !isPanelCollapse.value ? 120 : 64
  const width = document.body.clientWidth - sidePanelWidth
  const height = document.body.clientHeight - 64 - 44
  if (dataGraphStore.dataPreviewVisible) {
    graph.resize(width - 400, height)
  } else {
    graph.resize(width, height)
  }
}

function resizeListener() {
  document.addEventListener(
    'resize',
    throttle(() => {
      resizeGraph()
    }, 300)
  )
}

function initOtherEvents() {
  // 从左侧面板拖动节点到画布上
  bus.on('on-start-drag', onStartDrag)
  bus.on('reset-on-graph', resetOnGraph)
  bus.on('set-is-panel-collapse', setIsPanelCollapse)
  bus.on('resize-graph', resizeGraph)
}

/**
 * 卸载bus事件
 */
function destoryOtherEvents() {
  // 从左侧面板拖动节点到画布上
  bus.off('on-start-drag')
  bus.off('reset-on-graph')
  bus.off('set-is-panel-collapse')
  bus.off('resize-graph')
}

onBeforeUnmount(() => {
  clearStatusesInterval()
  destoryOtherEvents()
})
</script>
<style lang="less" scoped>
.graph-placeholder {
  position: absolute;
  top: 84px;
  left: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  .rect {
    width: 68px;
    height: 68px;
    border: 1px dashed #c3cad9;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .add-icon {
    color: #909198;
    font-size: 28px;
  }
  .tips {
    opacity: 0.5;
    font-family: PingFangSC-Regular;
    font-size: 12px;
    color: #222432;
    letter-spacing: 0;
    text-align: center;
    font-weight: 400;
    margin: 4px;
  }
}
</style>
