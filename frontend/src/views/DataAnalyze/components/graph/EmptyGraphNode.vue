<template>
  <div class="graph-node empty-graph-node">
    <icon-font type="icon-shujubeifen" class="dataset-iconfont" />
    <span class="title">空数据节点</span>
    <div class="choose-btn" @click="onChoose">选择数据</div>
  </div>
</template>

<script setup lang="ts">
import IconFont from '@/components/iconfont/IconFont.vue'
import bus from '@/EventBus/eventbus.js'

function onChoose() {
  bus.emit('change-dataset', 'empty-dataset')
}
</script>

<style lang="less" scoped>
@import '@/views/DataAnalyze/styles/data-graph.less';
.empty-graph-node {
  border-radius: 4px;
  display: flex;
  align-items: center;
  flex-direction: column;
  .dataset-iconfont {
    font-size: 32px;
  }
  .title {
    font-size: 12px;
    line-height: 17px;
    position: absolute;
    bottom: 4px;
  }
  .choose-btn {
    width: 70px;
    height: 24px;
    background: #6973ff;
    border-radius: 2px;
    color: #fff;
    position: absolute;
    bottom: -30px;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
