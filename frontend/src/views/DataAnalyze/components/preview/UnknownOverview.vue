<template>
  <slot name="overviewHeader"></slot>
  <div class="unknown-overview">
    <UnknownFileTemplate />
  </div>
</template>
<script setup lang="ts">
import UnknownFileTemplate from '@/components/common/UnknownFileTemplate.vue'
</script>
<style lang="less" scoped>
.unknown-overview {
  // max-height: 424px;
  height: 100%;
  background-color: #fafafc;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  .iconfont {
    font-size: 93px;
  }
  .tips {
    color: #c8c8c8;
    font-size: 12px;
    margin: 0;
  }
}
</style>
