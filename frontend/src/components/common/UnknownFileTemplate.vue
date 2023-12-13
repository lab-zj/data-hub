<!-- 非常规文件模板 -->
<template>
  <div class="unknow-file-template">
    <icon-font type="icon-feichangguiwenjian" class="unknown-iconfont" />
    <p>非常规文件，无法识别</p>
  </div>
</template>
<script setup lang="ts"></script>
<style lang="less" scoped>
.unknow-file-template {
  height: 180px;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  .unknown-iconfont {
    display: flex;
    font-size: 93px;
    margin-bottom: 10px;
  }
  p {
    margin: 0 !important;
    color: #909399;
    font-size: 12px;
  }
}
</style>
