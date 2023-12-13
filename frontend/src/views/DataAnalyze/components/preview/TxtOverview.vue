<template>
  <slot name="overviewHeader"></slot>
  <slot name="previewHeader"></slot>
  <div class="txt-overview four-scroll-y">
    <TxtFileTemplate :path="props.data.path" />
  </div>
</template>
<script setup lang="ts">
import TxtFileTemplate from '@/components/common/TxtFileTemplate.vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})
</script>
<style lang="less" scoped>
@import '@/styles/scroll-bar';
.txt-overview {
  height: 100%;
  background: #fafafc;
  display: flex;
  justify-content: center;
  padding: 12px;
  .txt_empty {
    display: flex;
    justify-content: center;
    color: #909399;
    font-size: 14px;
  }
}
</style>
