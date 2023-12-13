<template>
  <slot name="overviewHeader"></slot>
  <slot name="previewHeader"></slot>
  <div class="virtual-overview four-scroll-y">
    <span class="view-detail" @click="onView">查看详情</span>
    <VirtualFileTemplate :path="props.data.path" />
  </div>
</template>
<script setup lang="ts">
import VirtualFileTemplate from '@/components/common/VirtualFileTemplate.vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['view'])
function onView() {
  emit('view', props.data)
}
</script>
<style lang="less" scoped>
@import '@/styles/scroll-bar';
.virtual-overview {
  // height: 100%;
  background: #fff;
  padding: 12px;
  font-size: 14px;
  .virtual-tables {
    border-top: 1px solid #e9e9e9;
    .label {
      display: inline-block;
      margin-top: 12px;
    }
    .select-area {
      margin-top: 12px;
    }
    :deep(.el-select .el-input .el-select__caret.el-icon) {
      transform: rotateZ(90deg);
    }
    :deep(.el-select .el-input .el-select__caret.is-reverse) {
      transform: rotateZ(-180deg);
    }
  }
  .view-detail {
    color: #6973ff;
    cursor: pointer;
    position: absolute;
    right: 12px;
  }
}
</style>
