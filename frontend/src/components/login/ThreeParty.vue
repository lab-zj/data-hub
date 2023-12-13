<template>
  <div class="three-party">
    <div class="label">第三方账号登录</div>
    <DingEntrance redirect="/management/projects?from=login" />
  </div>
</template>
<script setup lang="ts">
import DingEntrance from '@/views/UserCenter/components/DingEntrance.vue'
</script>
<style lang="less" scoped>
.three-party {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  margin-top: 23px;

  .label {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    margin-bottom: 10px;
    font-size: 12px;
    color: #e9e9e9;

    &::before,
    &::after {
      content: '';
      width: 15%;
      height: 1px;
      border-top: 1px solid #e8e8e8;
      margin: 0 10px;
    }
  }
}
</style>
