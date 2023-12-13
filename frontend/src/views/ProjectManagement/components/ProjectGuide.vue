<template>
  <div class="guide">
    <span class="title">新用户引导</span>
    <div class="list">
      <div class="block">
        <strong class="label">平台产品手册</strong>
        <span class="entrance">快速了解平台功能<icon-font type="icon-qianwang-xiayibu" class="entrance-icon" /></span>
      </div>
      <div class="block">
        <strong class="label">导入数据</strong>
        <span class="entrance" @click="goData">开始你的数据分析之旅<icon-font type="icon-qianwang-xiayibu" class="entrance-icon" /></span>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goData() {
  router.push('/management/data')
}
</script>
<style lang="less" scoped>
.guide {
  margin-top: 80px;
  border-top: 1px solid #e9e9e9;
  padding-top: 24px;

  .title {
    font-size: 16px;
    color: #9b9da8;
  }

  .block {
    width: 250px;
    height: 165px;
    background: #fff;
    box-shadow: 0px 0px 2px 0px rgba(0, 0, 0, 0.2);
    border-radius: 4px;
    background-image: url('../../../assets/project-background.png');
    background-repeat: no-repeat;
    background-size: cover;
  }

  .list {
    display: flex;
  }

  .block {
    margin-top: 24px;
    position: relative;
  }

  .block + .block {
    margin-left: 26px;
  }

  .label {
    font-size: 24px;
    margin: 24px;
    display: inline-flex;
  }

  .entrance {
    display: flex;
    color: #6973ff;
    position: absolute;
    bottom: 12px;
    right: 12px;
    align-items: center;
    cursor: pointer;

    .entrance-icon {
      display: flex;
      align-items: center;
      margin-left: 9px;
    }
  }
}
</style>
