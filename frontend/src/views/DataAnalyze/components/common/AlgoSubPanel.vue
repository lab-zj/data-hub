<template>
  <div class="algo-sub-panel">
    <div class="panel-header" v-if="props.hasHeader">
      <icon-font type="icon-a-jiantoubeifen2" class="btn-close" @click="emit('close')" />
    </div>
    <div class="panel-content">
      <el-tabs type="border-card">
        <el-input v-model="searchKey" placeholder="搜索内容" class="algo-input" @keyup.enter="onSearch" />
        <el-tab-pane label="内置">
          <AlgoList
            :list="systemAlgoList"
            :operates="operates"
            @start-drag="onStartDrag"
            :has-operate="hasOperate"
            @select="onSelect"
            @scroll="onSystemScroll"
            @operate="onOperate"
          />
        </el-tab-pane>
        <el-tab-pane label="自定义算法">
          <AlgoList :list="algoList" :operates="operates" @start-drag="onStartDrag" :has-operate="hasOperate" @select="onSelect" @scroll="onScroll" @operate="onOperate" />
        </el-tab-pane>
      </el-tabs>
    </div>
    <div class="panel-footer" v-if="hasFooter">
      <el-button type="primary" class="upload-btn" round @click="onUpload"><icon-font type="icon-shujuguanli-daorushuju" class="upload-icon-font" />上传算法</el-button>
    </div>
    <div>
      <el-dialog v-model="uploadVisible" title="算法上传" width="631px" :close-on-click-modal="false" :destroy-on-close="true" :lock-scroll="false">
        <UploadAlgorithm @onCancel="uploadVisible = false" @refresh="onSearch" />
      </el-dialog>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, reactive, watch, computed } from 'vue'
import AlgoList from './AlgoList.vue'
import UploadAlgorithm from '@/components/algorithm/UploadAlgorithm.vue'
import useAlgorithm from '../../hooks/useAlgorithm'
import { NODE_TYPES } from '@/constants/dataGraph'
import { useDataGraph } from '@/stores/dataGraphStore'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  hasHeader: {
    type: Boolean,
    default: true
  },
  hasFooter: {
    type: Boolean,
    default: true
  },
  hasOperate: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['close', 'startDrag', 'select', 'onUpload', 'locate'])
const dataGraphStore = useDataGraph()
const { algoList, initQuery, getAllRelatedAlgorithm, onScroll, deleteAlgorithm } = useAlgorithm()
const { algoList: systemAlgoList, initQuery: initSystemQuery, getAllRelatedAlgorithm: getSystemAlgoList, onScroll: onSystemScroll } = useAlgorithm()

const searchKey: any = ref('')
const uploadVisible: any = ref(false)

const operates: any = reactive([
  {
    name: '定位',
    key: 'locate'
  },
  {
    name: '删除',
    key: 'del',
    permission: ['all']
  }
])

const used_algos = computed(() => {
  return dataGraphStore.used_algos?.map((node: any) => +node.dataset?.algorithm)
})

watch(
  () => dataGraphStore.rawData,
  () => {
    operates[0].permission = used_algos.value
  },
  { immediate: true, deep: true }
)

onMounted(() => {
  // 查询内置算法
  initSystemQuery('built-in', '')
  getSystemAlgoList()
  // 查询自定义算法
  initQuery('custom', '')
  getAllRelatedAlgorithm()
})

function onSearch() {
  // 查询内置算法
  initSystemQuery('built-in', searchKey.value)
  getSystemAlgoList()
  // 查询自定义算法
  initQuery('custom', searchKey.value)
  getAllRelatedAlgorithm()
}

function onStartDrag(algo: any) {
  emit('startDrag', algo)
}

function onSelect(algo: any) {
  emit('select', algo)
}

function onUpload() {
  uploadVisible.value = true
  emit('onUpload')
}

function onOperate({ command, data }: { command: string; data: any }) {
  switch (command) {
    case 'locate':
      emit('locate', {
        dataset: {
          algorithm: data
        },
        type: NODE_TYPES.ALGO
      })
      break
    case 'del':
      ElMessageBox.confirm('确定要删除所选中的算法吗？', '删除算法？', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
        autofocus: false
      }).then(async () => {
        deleteAlgorithm(data)
      })
      break
    default:
  }
}
</script>

<style lang="less" scoped>
.algo-sub-panel {
  width: 240px;
  height: 480px;
  // height: 160px;

  .panel-header {
    position: absolute;
    right: 16px;
    height: 36px;
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9;
    .btn-close {
      transform: rotate(180deg);
      cursor: pointer;
      display: flex;
      align-items: center;
    }
  }

  .panel-content {
    height: calc(100% - 48px);
    :deep(.el-tabs) {
      height: 100%;
      border: 0;
    }
    :deep(.el-tabs__nav, .el-tabs--border-card > .el-tabs__header .el-tabs__item) {
      height: 36px;
    }
    :deep(.el-tabs--border-card > .el-tabs__header) {
      background-color: #ededf4;
      height: 36px;
    }
    :deep(.el-tabs--border-card > .el-tabs__header .el-tabs__item.is-active) {
      color: #373b52;
    }
    :deep(.el-tabs--border-card > .el-tabs__content) {
      padding: 0;
      height: calc(100% - 39px);
    }
    :deep(.el-tab-pane) {
      height: 100%;
    }

    .algo-input {
      font-size: 12px;
      padding: 8px 12px 0;
      :deep(.el-input__wrapper) {
        border-radius: 16px;
        height: 24px;
        width: 216px;
      }
    }
  }

  .panel-footer {
    height: 48px;
    display: flex;
    justify-content: center;
    align-items: center;
    .upload-btn {
      width: 129px;
      height: 25px;
      font-size: 12px;
    }
    .upload-icon-font {
      display: flex;
      align-items: center;
      margin-right: 3px;
    }
  }
}
</style>
