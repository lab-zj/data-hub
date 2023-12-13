<template>
  <div>
    <div class="no-result">
      <div v-if="status === 'running'" class="content">
        <img v-if="status === 'running'" src="@/assets/running.svg" class="image" />
        <span class="tips">节点运行中</span>
        <span class="tips">请等待</span>
      </div>
      <div v-else-if="status === 'failed'" class="content">
        <img src="@/assets/failed.svg" class="image" />
        <span class="tips">节点运行失败</span>
        <span class="tips">请重新配置</span>
        <div class="error_info" v-html="JSON.stringify(data.status.message)"></div>
      </div>
      <div v-else class="content">
        <img src="@/assets/empty.png" class="image" />
        <span class="tips">节点未运行,或者未完成运行</span>
        <span class="tips">暂无结果</span>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { watch, ref } from 'vue'
import { getNodeStatus } from '@/util/util'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const status = ref('')

watch(
  () => props.data,
  () => {
    status.value = getNodeStatus(props.data.status)
  },
  { immediate: true, deep: true }
)
</script>
<style lang="less" scoped>
.no-result {
  background-color: #fafafc;
  width: 100%;
  height: 100%;
  .content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding-top: 5%;
  }
  .image {
    width: 208px;
  }
  .tips {
    font-size: 12px;
    color: #222432;
  }

  .error_info {
    overflow: auto;
    width: 100%;
    height: 400px;
    font-size: 12px;
    margin-top: 20px;
    word-wrap: break-word;
  }
}
</style>
