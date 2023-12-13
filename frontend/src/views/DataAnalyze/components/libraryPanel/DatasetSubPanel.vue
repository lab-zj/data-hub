<template>
  <div class="dataset-sub-panel">
    <div class="panel-title" draggable="true" @dragstart="console.log('sdfsfsdf')">数据节点<icon-font type="icon-a-jiantoubeifen2" class="btn-close" @click="emit('close')" /></div>
    <div class="content four-scroll-y">
      <FileTree
        :operates="operates"
        :extraParams="{
          usedData: used_datasets,
          usedOnly
        }"
        @start-drag="onStartDrag"
        @preview="onPreview"
        @operate="onOperate"
      />
    </div>
    <div class="panel-footer">仅查看已使用文件 <el-switch v-model="usedOnly" size="small" /></div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, watch } from 'vue'
import FileTree from '../common/FileTree.vue'
import { useRouter } from 'vue-router'
import { useDataGraph } from '@/stores/dataGraphStore'
import { NODE_TYPES } from '@/constants/dataGraph'

interface Tree {
  name: string
  path: string
  directory: boolean
  leaf?: boolean
  isGetMore?: boolean
}

const emit = defineEmits(['close', 'startDrag', 'preview', 'locate'])

const router = useRouter()
const dataGraphStore = useDataGraph()

const usedOnly = ref(false)
const operates: any = reactive([
  {
    name: '定位',
    key: 'locate'
  },
  {
    name: '管理数据',
    key: 'manage',
    permission: ['all']
  }
])

const used_datasets = computed(() => {
  return dataGraphStore.used_datasets?.map((node: any) => node.dataset?.path)
})

watch(
  () => dataGraphStore.rawData,
  () => {
    operates[0].permission = used_datasets.value
  },
  { immediate: true, deep: true }
)

function onStartDrag(fileData: Tree) {
  console.log(fileData)
  emit('startDrag', fileData)
}

function onPreview(_data: any) {
  emit('preview', _data)
}

function onOperate({ command, data }: { command: string; data: any }) {
  switch (command) {
    case 'locate':
      console.log('定位', data)
      emit('locate', {
        dataset: data,
        type: NODE_TYPES.DATA
      })
      break
    case 'manage':
      gotoManage(data)
      break
    default:
  }
}

function gotoManage(data: any) {
  if (!data) {
    return
  }
  const { directory, path } = data
  const _path = getPath(directory, path)
  const routeUrl = router.resolve({
    path: _path
  })
  window.open(routeUrl.href, '_blank')
}

function getPath(directory: boolean, path: string) {
  let _path = ''
  if (directory) {
    const len = path.length
    _path = path.slice(0, len - 1)
  } else {
    const list = path.split('/')
    _path = list.slice(0, list.length - 1).join('/')
  }
  return _path ? `/management/data/path=${encodeURIComponent(_path)}` : '/management/data'
}
</script>

<style lang="less" scoped>
@import '@/styles/scroll-bar';
.dataset-sub-panel {
  width: 208px;
  height: 580px;
  font-size: 14px;
  position: relative;

  .panel-title {
    height: 36px;
    line-height: 36px;
    background-color: #ededf4;
    padding: 0 12px;
    display: flex;
    justify-content: space-between;
  }

  .btn-close {
    transform: rotate(180deg);
    padding: 0 4px;
    margin-bottom: 3px;
    cursor: pointer;
  }

  .panel-footer {
    position: absolute;
    width: 100%;
    left: 0;
    bottom: 0;
    font-size: 12px;
    background-color: #f9f9fc;
    height: 41px;
    line-height: 41px;
    padding: 0 12px;
    color: #5d637e;
    border-top: 1px solid #eee;
  }

  :deep(.file-tree) {
    background-color: #f9f9fc;
  }
  :deep(.tree-label) {
    // max-width: 130px;
    display: inline-block;
  }

  .content {
    height: calc(100% - 77px);
    overflow: hidden;
  }

  :deep(.el-tree-node__content) {
    height: 30px;
  }
  :deep(.file-tree-search) {
    width: 184px;
  }
}
</style>
