<template>
  <div class="node-overview-panel" v-if="overviewVisible" :style="nodeType === NODE_TYPES.SQL ? 'width:500px;' : 'width:364px;'">
    <!-- 点击节点出节点信息面板 -->
    <component
      :is="dynamicSubPanelComponent"
      class="node-overview-sub-panel"
      :data="detail"
      :type="nodeType"
      @close="onClose"
      @view="onView"
      @detailClose="onDetailClose"
      @save="onSave"
    >
      <template #overviewHeader>
        <OverviewHeader
          v-if="detail"
          :data="detail"
          :type="nodeType"
          :can-change="nodeType !== NODE_TYPES.SQL && nodeType !== NODE_TYPES.ALGO"
          :can-run="nodeType === NODE_TYPES.SQL || nodeType === NODE_TYPES.ALGO"
          @close="onClose"
        />
      </template>
    </component>
  </div>
  <!-- 查看详情的详情面板 -->
  <div class="data-detail-panel" v-if="detailVisible" :style="autoWidthStyle" v-dragWidth="{ dragSide: 'left', minWidth: 390, maxWidth: dragMaxWidth }">
    <div class="resize-line"></div>
    <DetailHeader :data="detailData" @close="onDetailClose" />
    <component :is="dynamicDetailComponent" class="data-detail-sub-panel" :data="detailData"> </component>
  </div>
</template>
<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import OverviewHeader from './OverviewHeader.vue'
import CsvOverview from './CsvOverview.vue'
import SqlOverview from './SqlOverview.vue'
import AlgoOverview from './AlgoOverview.vue'
import FolderOverview from './FolderOverview.vue'
import MediaOverview from './MediaOverview.vue'
import TxtOverview from './TxtOverview.vue'
import UnknownOverview from './UnknownOverview.vue'
import DetailHeader from './DetailHeader.vue'
import CsvDetail from './CsvDetail.vue'
import VirtualOverview from './VirtualOverview.vue'
import NoResult from './NoResult.vue'
import { getFileType, getNodeStatus } from '@/util/util'
import { NODE_TYPES } from '@/constants/dataGraph'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  data: {
    type: Object,
    default: () => {}
  }
})

const router = useRouter()
const emit = defineEmits(['close', 'save'])

const overviewVisible = ref(props.visible)
const detail: any = ref(null)
const nodeType: any = ref('')
const fileType: any = ref('')
const dragMaxWidth: any = document.body.clientWidth - 400

/**
 * 动态组件：子菜单
 */
const dynamicSubPanelComponent = computed(() => {
  if (nodeType.value === NODE_TYPES.ALGO) {
    return AlgoOverview
  } else if (nodeType.value === NODE_TYPES.SQL) {
    return SqlOverview
  } else if (nodeType.value === NODE_TYPES.DATA) {
    if (fileType.value === 'csv') {
      return CsvOverview
    } else if (fileType.value === '/') {
      return FolderOverview
    } else if (['png', 'mp4', 'image', 'video'].includes(fileType.value)) {
      return MediaOverview
    } else if (['txt'].includes(fileType.value)) {
      return TxtOverview
    } else if (['s3', 'my', 'ps'].includes(fileType.value)) {
      return VirtualOverview
    }
  }
  return UnknownOverview
})

watch(
  () => props.visible,
  (val: boolean) => {
    overviewVisible.value = props.visible
    if (!val) {
      detailVisible.value = false
    }
  }
)

watch(
  () => props.data,
  () => {
    nodeType.value = props.data?.type
    if (props.data?.type === NODE_TYPES.DATA) {
      fileType.value = getFileType(props.data.dataset.directory, props.data.dataset.name)
      detail.value = {
        ...props.data.dataset,
        id: props.data.id,
        name: props.data.name, // 节点名
        fileName: props.data.dataset.name // 文件名
      }
    } else if (props.data?.type === NODE_TYPES.SQL) {
      detail.value = {
        ...props.data,
        sql: props.data?.dataset?.sql || '',
        path: getOutputPath(props.data.status)
      }
    } else {
      detail.value = props.data
    }
  },
  {
    deep: true
  }
)

function onClose() {
  emit('close')
  onDetailClose()
}

function onSave(sql: string) {
  emit('save', {
    ...props.data,
    dataset: {
      taskId: detail.value.taskId,
      taskUuid: detail.value.id,
      sql
    }
  })
}

const detailVisible: any = ref(false)
const detailData: any = ref(null)

/**
 * 动态组件：子菜单
 */
const dynamicDetailComponent = computed(() => {
  const { type, status } = detailData.value
  if (type === 'sql') {
    const _status = getNodeStatus(status)
    if (!_status || _status === 'running' || _status === 'failed') {
      return NoResult
    } else {
      return CsvDetail
    }
  }
  if (['csv'].includes(type)) {
    return CsvDetail
  } else if (['png', 'mp4', 'image', 'video'].includes(type)) {
    return MediaOverview
  } else if (type === 'txt') {
    return TxtOverview
  }
  // else if (type === NODE_TYPES.SQL) {
  //   return SqlOverview
  // } else if (type === NODE_TYPES.ALGO) {
  //   return AlgoOverview
  // } else if (type === '/') {
  //   return FolderOverview
  // } else if (['png','mp4'].includes(type.value)) {
  //   return MediaOverview
  // } else if (['txt'].includes(type.value)) {
  //   return TxtOverview
  // }
  return ''
})

const autoWidthStyle = computed(() => {
  const { type } = detailData.value
  return ['png', 'mp4', 'image', 'video'].includes(type)
    ? {
        width: '390px'
      }
    : {
        width: '540px'
      }
})

function onView(data: any) {
  // detailVisible.value = true
  // detailData.value = data
  // 数据节点本来就有对应的文件path（来自云盘）
  // sql节点运行成功，有对应的文件path（直接存到云盘那里）
  // sql节点未运行成功，面板提示
  if (data.path) {
    const { path } = data
    gotoDetail(path)
  } else {
    detailVisible.value = true
    detailData.value = data
  }
}

const gotoDetail = (_path: string) => {
  const path = encodeURIComponent(_path)
  const routeUrl = router.resolve({
    path: `/management/detail/path=${path}`,
    query: {
      from: 'graph'
    }
  })
  // window.open(routeUrl.href, '_self')
  router.push(routeUrl)
}

function onDetailClose() {
  detailVisible.value = false
}

/**
 * 从status中获取sql节点运行结果的path
 * @param _status
 */
function getOutputPath(_status: any) {
  if (!_status) {
    return ''
  }

  const { data, cloudData } = _status
  // sql节点后面接虚拟数据节点（.ps、.s3、.my），执行结果存放在cloudData中，且用冒号分开（后端设计这种类型参数，方便之后去组装预览远程虚拟文件）
  // sql节点后面接普通数据节点，执行结果存放在data中
  if (data) {
    const parseData = JSON.parse(data)
    const keys = Object.keys(parseData[0])
    return parseData[0][keys[0]]
  } else if (cloudData) {
    const parseData = JSON.parse(cloudData)
    const keys = Object.keys(parseData[0])
    const result = parseData[0][keys[0]]?.split(':')
    // 暂时后端只提供了远程s3上的csv文件预览，这里临时兼容一下
    return result[0].includes('.s3') ? parseData[0][keys[0]] : result[0]
  }
}
</script>
<style lang="less" scoped>
.node-overview-panel {
  width: 364px;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
  height: 100%;
  position: absolute;
  right: 0;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  z-index: 9;
  overflow: hidden;

  :deep(.preview-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    .title,
    .view-detail {
      font-size: 14px;
      line-height: 20px;
      letter-spacing: 1px;
    }
    .title {
      flex: 1;
    }
    .view-detail {
      color: #6973ff;
      cursor: pointer;
    }
  }
}
.data-detail-panel {
  width: 540px;
  position: absolute;
  right: 364px;
  background: #fff;
  height: 100%;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
  z-index: 9;

  :deep(.media-overview) {
    padding: 12px;
    height: calc(100% - 44px);
  }
  :deep(.txt-overview) {
    padding: 12px;
    height: calc(100% - 44px);
  }
  .data-detail-sub-panel {
    padding: 6px 12px 12px;
    height: calc(100% - 44px);
    :deep(.video) {
      height: calc(100% - 44px);
    }
    :deep(.el-table-v2__header-cell) {
      background-color: #fafafa;
    }
    :deep(.el-table-v2__header-cell-text) {
      color: #373b52;
    }
    :deep(.preview-csv-header-row > th) {
      background-color: #fafafa;
    }
    :deep(.el-image__error) {
      height: auto;
    }
  }
  .resize-line {
    width: 2px;
    height: calc(100% - 44px);
    position: absolute;
    left: 0;
  }
}
</style>
