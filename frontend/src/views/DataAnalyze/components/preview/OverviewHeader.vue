<template>
  <div class="overview-header">
    <div class="name">
      <icon-font v-if="type === NODE_TYPES.SQL" type="icon-sql2" class="type-iconfont" />
      <icon-font v-else-if="type === NODE_TYPES.ALGO" type="icon-juleiK-Means" class="type-iconfont" />
      <icon-font v-else :type="getFileIconFont(getFileType(data.directory, data.fileName))" class="type-iconfont" />
      <span v-if="!isEdit" class="ellipsis title" :title="name">{{ name }}</span>
      <el-tooltip content="编辑" placement="top">
        <icon-font v-if="!isEdit" type="icon-bianji" class="bianji-iconfont" @click="edit" />
      </el-tooltip>
      <el-tooltip content="复制节点名称" placement="top">
        <icon-font v-if="!isEdit" type="icon-fuzhiziduan" class="copy-iconfont" @click="copyToClipboard(name)" />
      </el-tooltip>
      <el-input
        v-model="name"
        v-show="isEdit"
        id="detail-title-input"
        size="small"
        class="title-input"
        oninput="value=value.replace(/[, ]/g,'')"
        :input-style="{
          width: '100px'
        }"
        @keyup.enter="modifyName"
      />
      <el-button v-if="isEdit" type="primary" size="small" class="confirm-btn" @click="modifyName">确认</el-button>
      <el-button v-if="isEdit" size="small" class="cancel-btn" @click="onCancel">取消</el-button>
    </div>
    <div class="operate">
      <el-tooltip content="运行" placement="top" v-if="props.canRun">
        <icon-font type="icon-yunhang" class="run-iconfont" @click="onExecute" />
      </el-tooltip>
      <el-dropdown>
        <icon-font type="icon-daohang-gengduocaozuobeifen" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-if="data && data.path" @click="onDownload">下载</el-dropdown-item>
            <el-dropdown-item @click="onCopyNode">复制</el-dropdown-item>
            <!-- 服务端在跑完sql后，已经直接上传结果 -->
            <!-- <el-dropdown-item @click="onSave">保存至数据管理</el-dropdown-item> -->
            <!-- 更换数据：仅针对数据起始节点出现此操作入口 -->
            <el-dropdown-item v-if="props.canChange" @click="onChange">更换数据</el-dropdown-item>
            <el-dropdown-item @click="onDelete">删除节点</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <icon-font type="icon-guanbibeifen" class="close-iconfont" @click="onClose" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { getFileIconFont, getFileType, compatible, checkName, copyToClipboard } from '@/util/util'
import bus from '@/EventBus/eventbus.js'
import { NODE_TYPES } from '@/constants/dataGraph'
import useGraphDataFlow from '@/views/DataAnalyze/hooks/useGraphDataFlow'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'
import { ElMessage } from 'element-plus'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  type: {
    type: String,
    default: ''
  },
  canChange: {
    type: Boolean,
    default: false
  },
  canRun: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const { onExecute } = useGraphDataFlow()
const { rename, deleteFromGraph, onCopyNode } = useGraphInteraction()

const name = ref('')
const isEdit = ref(false)

watch(
  () => props.data,
  () => {
    if (props.data) {
      name.value = props.data?.name
    }
  },
  { immediate: true, deep: true }
)

function edit() {
  isEdit.value = !isEdit.value
  const edit_element = document.querySelector('#detail-title-input') as HTMLElement
  nextTick(() => {
    edit_element.focus()
  })
}

function modifyName() {
  if (!checkName(name.value, props.data.name)) {
    return
  }
  rename({
    id: props.data.id,
    _name: name.value
  })
  isEdit.value = false
}

function onDownload() {
  if (!props.data?.path) {
    ElMessage.warning('暂无数据下载！')
    return
  }
  // 后端待补充远程文件的下载接口
  if (props.data?.path.includes(':')) {
    ElMessage.warning('远程文件的下载待补充！')
    return
  }
  const path = compatible(props.data?.path)
  if (path) {
    window.open(`/api/filesystem/basic/download/batch?pathList=${encodeURIComponent(path)}`)
  }
}

function onDelete() {
  deleteFromGraph({
    list: [props.data],
    type: 'node'
  })
}

function onCancel() {
  isEdit.value = false
  name.value = props.data.name
}

function onClose() {
  emit('close')
}

// 保存至数据管理相关
function onSave() {
  bus.emit('save-data-to-data-management')
}

// 更换数据相关
function onChange() {
  bus.emit('change-dataset', props.data.id)
}
</script>
<style lang="less" scoped>
.overview-header {
  height: 44px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e9e9e9;
  justify-content: space-between;
  padding: 12px;
  .iconfont {
    cursor: pointer;
    display: flex;
    &:hover {
      color: #6973ff;
    }
  }
  .type-iconfont {
    margin-right: 4px;
  }
  .name,
  .operate {
    display: flex;
    align-items: center;
  }
  .name {
    .confirm-btn {
      margin-left: 16px;
      padding: 5px;
    }
    .cancel-btn {
      margin-left: 4px;
      padding: 5px;
    }
  }

  .title {
    max-width: 160px;
    font-size: 14px;
  }
  .bianji-iconfont,
  .copy-iconfont {
    margin-left: 4px;
  }
  .close-iconfont {
    margin-left: 8px;
  }
  .run-iconfont {
    margin-right: 8px;
  }

  :deep(.el-dropdown) {
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    &:hover {
      background: #eaeafd;
      color: #6973ff;
    }
  }

  :deep(:focus-visible) {
    outline: none;
  }

  .ellipsis {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }
}
</style>
