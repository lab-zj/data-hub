<template>
  <div class="header-container">
    <span class="back" @click="back">
      <icon-font class="icon-back" type="icon-jiantou" />
      返回
    </span>
    <el-divider direction="vertical" />
    <span class="title">设置</span>

    <InfoHeader />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function back() {
  router.push('/management/projects')
}
</script>

<style lang="less" scoped>
.header-container {
  align-items: center;
  background-color: #fff;
  display: flex;
  flex-direction: row;
  height: 54px;
  padding: 0 10px;
}

.back {
  color: #6973ff;
  font-size: 16px;
  padding: 4px;
  cursor: pointer;
  margin-left: 12px;
}

.title {
  color: #373b52;
}
</style>
