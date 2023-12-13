<template>
  <div>
    <el-dialog class="change-dataset-dialog" :model-value="visible" width="40%" align-center @close="handleClose" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">更换数据</span>
        </div>
      </template>
      <div class="content">
        <div class="switch">
          <span class="label">仅查看已使用文件</span>
          <el-switch v-model="usedOnly" size="small" />
        </div>
        <div class="tree">
          <FileTree
            :has-operate="false"
            :extraParams="{
              usedData: used_datasets,
              usedOnly
            }"
            @confirm="onConfirm"
          />
        </div>
        <div class="tips_area">
          <el-checkbox :checked="true" label="数据源名称替换节点名称" size="small" @change="doReplace" />
          <!-- 重置同一pipeline上的所有节点状态，还没有接口，暂时注释掉 -->
          <!-- <span class="tips"> 提示：更换数据后，同一pipeline上的节点将全部重置为未运行状态 </span> -->
        </div>
      </div>
      <template #footer>
        <span class="footer">
          <el-button type="primary" @click="handleAddDataet" link>新增数据源</el-button>
          <div>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="onChange" :loading="loading">确定更换</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import FileTree from './common/FileTree.vue'
import { useRouter } from 'vue-router'
import { useDataGraph } from '@/stores/dataGraphStore'

defineProps({
  visible: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['close', 'confirm'])

const router = useRouter()
const dataGraphStore = useDataGraph()
const usedOnly: any = ref(false)
const replaced: any = ref(true) // 数据源名称替换节点名称
let currentTreeNode: any = ref(null)

const used_datasets = computed(() => {
  return dataGraphStore.used_datasets?.map((node: any) => node.dataset?.path)
})

function handleAddDataet() {
  const routeUrl = router.resolve({
    path: '/management/data'
  })
  window.open(routeUrl.href, '_blank')
}

function onConfirm(nodeData: any) {
  currentTreeNode.value = nodeData
}

function onChange() {
  emit('confirm', {
    _nodeData: currentTreeNode.value,
    replaced: replaced.value
  })
}

function handleClose() {
  emit('close')
  replaced.value = true
}

function doReplace(_checked: any) {
  replaced.value = _checked
}
</script>
<style lang="less" scoped>
:deep(.el-dialog__header) {
  padding: 10px 20px !important;
  border-bottom: 1px solid #f2f2f2;
}

:deep(.el-dialog__title) {
  font-size: 14px;
}
:deep(.el-dialog__headerbtn) {
  top: 0;
}

:deep(.el-dialog__body) {
  padding: 0;
  height: 442px;
}
:deep(.el-dialog__footer) {
  border-top: 1px solid #f2f2f2 !important;
  padding: 16px 20px;
}
:deep(.el-input__wrapper) {
  border-radius: 16px;
  height: 28px;
}

.change-dataset-dialog {
  .content {
    margin: 24px;
    overflow: hidden;
  }
  .search {
    margin-bottom: 8px;
  }
  .switch {
    font-size: 12px;
    color: #5d637e;
    position: absolute;
    top: 120px;
    .label {
      margin-right: 8px;
    }
  }

  .tree {
    margin-bottom: 8px;
    height: 400px;
  }
  .tips_area {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .tips {
    font-size: 12px;
    color: #95969b;
  }
  .footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
:deep(.file-tree) {
  .file-tree-search {
    margin-bottom: 50px;
  }
  .file-tree-container {
    height: calc(100% - 84px);
  }
}
</style>
