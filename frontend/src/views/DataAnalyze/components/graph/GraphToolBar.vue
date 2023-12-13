<template>
  <div class="graph-tool-bar">
    <div class="operate-action">
      <span class="action" @click="onExecute">
        <icon-font type="icon-yunhang" />
        执行
      </span>
      <el-tooltip content="暂停" placement="top">
        <icon-font type="icon-zantingyunhang" @click="stopGraphJob" />
      </el-tooltip>
    </div>
    <el-divider direction="vertical" class="divider" />
    <div class="operate-doRedo">
      <el-tooltip content="终止" placement="top">
        <icon-font type="icon-zhongzhicanshu" @click="cancelGraphJob" />
      </el-tooltip>
      <el-tooltip content="清空" placement="top">
        <icon-font type="icon-qingkongbeifen" :class="dataGraphStore.graph_running ? 'gray-display' : ''" @click="clean" />
      </el-tooltip>
      <!-- <el-tooltip content="撤销" placement="top">
        <icon-font type="icon-chexiao" />
      </el-tooltip>
      <el-tooltip content="撤销" placement="top">
        <icon-font type="icon-chexiaobeifen" />
      </el-tooltip> -->
    </div>
    <template v-if="isSelected && !dataGraphStore.graph_running">
      <el-divider direction="vertical" class="divider" />
      <div class="operate-others">
        <el-tooltip content="复制" placement="top">
          <icon-font type="icon-fuzhihuaban" @click="onCopyNode" />
        </el-tooltip>
        <el-tooltip content="删除" placement="top">
          <icon-font type="icon-shanchu" @click="onDelete" />
        </el-tooltip>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useDataGraph } from '@/stores/dataGraphStore'
import useGraphDataFlow from '@/views/DataAnalyze/hooks/useGraphDataFlow'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'
import { ElMessageBox } from 'element-plus'

const dataGraphStore = useDataGraph()
const { stopGraphJob, cancelGraphJob, onExecute, cleanGraph } = useGraphDataFlow()
const { deleteFromGraph, onCopyNode, clearGraph } = useGraphInteraction()

const isSelected: any = computed(() => dataGraphStore.targetNodeList?.length > 0)

function onDelete() {
  deleteFromGraph({
    list: dataGraphStore.targetNodeList,
    type: 'node'
  })
}

async function clean() {
  if (dataGraphStore.graph_running) {
    return
  }
  ElMessageBox.confirm('确定清空当前画布吗？', '清空画布？', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    autofocus: false
  }).then(async () => {
    const result = await cleanGraph([dataGraphStore.graphUuid])
    if (result) {
      clearGraph()
      dataGraphStore.reset()
    }
  })
}
</script>
<style lang="less" scoped>
.graph-tool-bar {
  width: 100%;
  height: 44px;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  align-items: center;
  padding-left: 6px;
  .operate-action,
  .operate-doRedo,
  .operate-others {
    display: flex;
    align-items: center;
  }
  .action {
    padding-right: 14px;
    font-size: 14px;
    display: flex;
    align-items: center;
    cursor: pointer;
    &:hover {
      background: #eaeafd;
      color: #6973ff;
    }
  }

  .gray-display {
    opacity: 0.3;
    cursor: not-allowed !important;
  }
  .iconfont {
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    &:hover {
      background: #eaeafd;
      color: #6973ff;
    }
  }
  .iconfont + .iconfont {
    margin-left: 8px;
  }
}
</style>
