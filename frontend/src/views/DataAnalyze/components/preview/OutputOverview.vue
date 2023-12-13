<template>
  <div class="output-overview" v-if="visible">
    <div class="header">
      <strong>输出{{ dataGraphStore.current_output }}</strong>
      <icon-font type="icon-guanbibeifen" class="close-iconfont" @click="onClose" />
    </div>
    <div class="content" v-html="getContent()"></div>
  </div>
</template>
<script setup lang="ts">
import { useDataGraph } from '@/stores/dataGraphStore'

defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const dataGraphStore = useDataGraph()

function getContent() {
  if (!dataGraphStore.current_output) {
    return
  }
  let result: any = null
  if (dataGraphStore.targetNodeList?.[0]?.data?.status?.data) {
    const list = JSON.parse(dataGraphStore.targetNodeList?.[0]?.data?.status?.data)
    result = list.find((item: any) => item[dataGraphStore.current_output])
  }
  return result?.[dataGraphStore.current_output] || '暂无内容'
}

function onClose() {
  emit('close')
}
</script>
<style lang="less" scoped>
.output-overview {
  width: 404px;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
  height: 100%;
  position: absolute;
  right: 0;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  z-index: 9;
  overflow: hidden;
  .header {
    height: 44px;
    display: flex;
    align-items: center;
    border-bottom: 1px solid #e9e9e9;
    justify-content: space-between;
    padding: 12px;
    .close-iconfont {
      cursor: pointer;
    }
  }
  .content {
    padding: 12px;
  }
}
</style>
