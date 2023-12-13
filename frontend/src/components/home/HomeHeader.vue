<template>
  <a-layout-header class="header-container">
    <div class="logo-container">
      <img class="logo-text" alt="logo" src="../../assets/logo-text.png" />
    </div>
    <info-header></info-header>
  </a-layout-header>
</template>

<script setup lang="ts">
import InfoHeader from '@/components/home/InfoHeader.vue'
</script>

<style lang="less" scoped>
@import '@/constants';

.header-container {
  align-items: center;
  background-color: @THEME_COLOR;
  display: flex;
  flex-direction: row;
  height: @HEADER_HEIGHT;
  justify-content: space-between;
  padding: 0 10px;
}

.logo {
  height: 32px;
  margin-left: 10px;
}

.logo-text {
  height: 28px;
  margin-left: 10px;
}
</style>
