<template>
  <div class="graph-test">
    <GraphSidePanel ref="graphSidePanelRef" @start-drag="onStartDrag" @preview="onPreview" @collapse="onCollapse" @empty-node="addEmptyNode" @locate="onLocate" />
    <GraphPipeline @close-side="closeSidePanel" />
    <NodeOverviewPanel :visible="dataGraphStore.nodeOverviewVisible" :data="nodeData" @close="dataGraphStore.setVisible('nodeOverviewVisible', false)" @save="onSqlSave" />
    <DataPreviewPanel :visible="dataGraphStore.dataPreviewVisible" :record="currentRecord" @close="dataGraphStore.setVisible('dataPreviewVisible', false)" />
    <ChangeDataset :visible="changeDatasetVisible" @close="setChangeDatasetVisible(false)" @confirm="onChangeDatasetConfirm" />
    <SetInputParams
      :visible="setInputParamsVisible"
      :data="nodeData"
      :loading="setInputParamsLoading"
      @close="changeInputParamsVisible(false)"
      @confirm="onSetInputParamsConfirm"
    />
    <LocationModal :visible="locationVisible" :current="currentLocationData" :data="locationUsedDatasets" @locate="onLocate" @close="handleLocationClose" />
    <MoveFilesModal :visible="saveDialogVisible" :loading="saveLoading" :move-index="saveIndex" title="保存至数据管理" confirmText="保存到此处" @operate="operateFunc" />
    <GraphRunningAlert v-if="dataGraphStore.graph_running" :isChange="isPanelCollapse" />
    <OutputOverview :visible="dataGraphStore.outputOverviewVisible" @close="reset" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch, onBeforeUnmount, computed } from 'vue'
import bus from '@/EventBus/eventbus.js'
import { useRoute, onBeforeRouteLeave } from 'vue-router'
import { useDataGraph } from '@/stores/dataGraphStore'
import GraphSidePanel from './components/libraryPanel/GraphSidePanel.vue'
import NodeOverviewPanel from './components/preview/NodeOverviewPanel.vue'
import DataPreviewPanel from './components/preview/DataPreviewPanel.vue'
import ChangeDataset from './components/ChangeDataset.vue'
import SetInputParams from './components/SetInputParams.vue'
import LocationModal from './components/LocationModal.vue'
import GraphPipeline from './components/graph/GraphPipeline.vue'
import GraphRunningAlert from './components/graph/GraphRunningAlert.vue'
import MoveFilesModal from '@/components/data/MoveFilesModal.vue'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'
import useChangeDataset from '@/views/DataAnalyze/hooks/useChangeDataset'
import useSetInputParams from '@/views/DataAnalyze/hooks/useSetInputParams'
import useLocate from '@/views/DataAnalyze/hooks/useLocate'
import { ElLoading } from 'element-plus'
import { useDetailStore } from '@/stores/detail'
import OutputOverview from './components/preview/OutputOverview.vue'

let runningMask: any = null

const dataGraphStore = useDataGraph()
const route = useRoute()
const detailStore = useDetailStore()
const { addEmptyNode, onSqlSave } = useGraphInteraction()
const graphSidePanelRef = ref(null)
const isPanelCollapse = computed(() => graphSidePanelRef?.value?.isPanelCollapse || false)

watch(
  () => dataGraphStore.targetNodeList,
  (val: any) => {
    if (val?.length > 1 || val?.length === 0) {
      dataGraphStore.setVisible('nodeOverviewVisible', false)
    } else if (val[0].shape !== 'edge') {
      const _data = val[0].getData()
      nodeData.value = dataGraphStore.getDetail(_data.id)
      if (!['contextmenu', 'rename', 'locate', 'viewoutput'].includes(dataGraphStore.graphActionMode)) {
        dataGraphStore.setVisible('nodeOverviewVisible', true)
      }
      dataGraphStore.setVisible('dataPreviewVisible', false)
      dataGraphStore.setVisible('outputOverviewVisible', false)
    }
  }
)

watch(
  () => dataGraphStore.dataPreviewVisible,
  () => {
    bus.emit('resize-graph')
  },
  {
    immediate: true
  }
)

/**
 * 画布运行时，增加遮罩，禁止修改画布操作
 */
watch(
  () => dataGraphStore.graph_running,
  () => {
    handleMask()
  }
)

/**
 * 画布运行中遮罩处理
 */
function handleMask() {
  if (dataGraphStore.graph_running) {
    runningMask = ElLoading.service({
      target: '.graph-test',
      lock: true,
      text: '',
      fullscreen: false,
      background: 'rgba(0, 0, 0, 0)',
      customClass: 'runningMask'
    })
  } else {
    runningMask?.close()
  }
}

onMounted(() => {
  dataGraphStore.reset()
  initProjectInfo()
  initOtherEvents()
  handleMask()
})

function initProjectInfo() {
  dataGraphStore.setProjectId(route.params.id)
}

/** 左侧菜单面板相关  */
function onStartDrag(_node: any) {
  bus.emit('on-start-drag', _node)
}

function onCollapse(visible: boolean) {
  bus.emit('set-is-panel-collapse', visible)
  bus.emit('resize-graph')
}

// 关掉左侧面板
function closeSidePanel() {
  graphSidePanelRef.value.isShowSubPanel = false
}

/** 预览面板相关  */
const nodeData: any = ref(null)
const currentRecord: any = reactive({
  name: '',
  directory: false,
  path: '',
  creatTimestamp: '',
  updateTimestamp: '',
  size: 0
})

function onPreview(_data: any) {
  console.log('onPreview:', _data)
  dataGraphStore.setVisible('nodeOverviewVisible', false)
  dataGraphStore.setVisible('outputOverviewVisible', false)

  currentRecord.name = _data.name
  currentRecord.directory = _data.directory
  currentRecord.path = _data.path
  currentRecord.creatTimestamp = _data.creatTimestamp
  currentRecord.updateTimestamp = _data.updateTimestamp
  currentRecord.size = _data.size

  dataGraphStore.setVisible('dataPreviewVisible', true)
}

/** 更换数据相关  */
const { visible: changeDatasetVisible, setVisible: setChangeDatasetVisible, setFrom, onConfirm: onChangeDatasetConfirm } = useChangeDataset()

/** 设置入参相关  */
const { visible: setInputParamsVisible, loading: setInputParamsLoading, setVisible: changeInputParamsVisible, onConfirm: onSetInputParamsConfirm } = useSetInputParams()

/** 定位相关  */
const { visible: locationVisible, locationUsedDatasets, currentLocationData, onLocate, handleLocationClose } = useLocate()

/** 保存至数据管理相关 */
const saveDialogVisible: any = ref(false)
const saveLoading: any = ref(false)
let saveIndex: any = null

function operateFunc(type: String, data?: any) {
  switch (type) {
    case 'close':
      saveDialogVisible.value = false
      break
    case 'move':
      saveData(data)
      break
    default:
  }
}

function saveData(data: any) {
  console.log('saveData:', data)
}

/**
 * 重置
 */
function reset() {
  dataGraphStore.init()
  nodeData.value = null
  dataGraphStore.graphActionMode = ''
}
/**
 * bus事件注册
 */
function initOtherEvents() {
  // 设置入参
  bus.on('set-input-params-for-algo-graph-node', () => {
    changeInputParamsVisible(true)
  })
  // 保存至数据管理
  bus.on('save-data-to-data-management', () => {
    saveDialogVisible.value = true
  })
  // 更换数据
  bus.on('change-dataset', (from: string) => {
    console.log('change-dataset:', from)
    setChangeDatasetVisible(true)
    setFrom(from)
  })
  bus.on('reset-on-data-analyze', reset)
}

/**
 * 卸载bus事件
 */
function destoryOtherEvents() {
  // 设置入参
  bus.off('set-input-params-for-algo-graph-node')
  // 保存至数据管理
  bus.off('save-data-to-data-management')
  // 更换数据
  bus.off('change-dataset')
  bus.off('reset-on-data-analyze')
}

onBeforeUnmount(() => {
  destoryOtherEvents()
})

onBeforeRouteLeave((to: any, from: any) => {
  const { path } = from
  detailStore.setFromPath(path)
})
</script>

<style lang="less" scoped>
@import '@/constants';

.graph-test {
  background-color: #fff;
  display: flex;
  height: calc(100vh - @HEADER_HEIGHT);
  overflow-y: hidden;
  position: relative;

  .graph-content {
    flex: 1;
    position: relative;
  }

  :deep(.x6-widget-selection-box) {
    border-radius: 4px;
    border: 1px solid #6973ff;
    box-shadow: 0px 0px 4px 0px rgba(88, 98, 230, 0.2);
  }

  // hover of edge
  :deep(.x6-edge:hover path:nth-child(2)) {
    stroke: #6973ff;
  }
}
</style>
<style lang="less">
.runningMask {
  top: 45px !important;
  width: auto !important;
  height: auto !important;
  .el-loading-spinner {
    display: none;
  }
}
// .el-loading-spinner .path {
//   stroke: #f9f9fc !important;
// }
// .el-loading-spinner .el-loading-text {
//   color: #f9f9fc !important;
// }
</style>
