<template>
  <div class="data-preview-panel" v-if="props.visible">
    <div class="header">
      <strong>文件预览</strong>
      <span class="text-btn" @click="onClose"><i class="iconfont icon-a-jiantoubeifen2 icon" /> 收起</span>
    </div>
    <div class="preview-content">
      <DataPreview :multiFile="[]" :record="props.record" from="graph" />
    </div>
  </div>
</template>
<script setup lang="ts">
import DataPreview from '@/components/data/DataPreview.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  record: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['close'])

function onClose() {
  emit('close')
}
</script>
<style lang="less" scoped>
.data-preview-panel {
  border-left: 1px solid #e9e9e9;
  padding: 16px;
  width: 400px;
  font-size: 14px;
  overflow-y: auto;
  .header {
    color: #373b52;
    display: flex;
    justify-content: space-between;
    align-items: center;

    strong {
      font-size: 16px;
    }

    .text-btn {
      color: #5d637e;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      &:hover {
        color: #6973ff;
      }
    }
  }
  .preview-content {
    margin-top: 21px;
    height: calc(100% - 16px - 20px - 21px);
    font-size: 14px;
  }
  :deep(.preview-csv-header-row > th) {
    background-color: #fafafa !important;
  }
  :deep(.el-table-v2__header-cell) {
    background-color: #fafafa !important;
  }
  :deep(.el-table-v2__header-cell-text) {
    color: #373b52 !important;
  }
  :deep(.preview-csv-header-row > th) {
    background-color: #fafafa !important;
  }
  :deep(.el-image__error) {
    height: auto;
  }
}
</style>
