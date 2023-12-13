<script setup lang="ts">
import HomeHeader from '../components/home/HomeHeader.vue'
import HomeNavBar from '@/components/home/HomeNavBar.vue'

</script>

<template>
  <main class="studio">
    <HomeHeader />
    <HomeNavBar />
    <router-view />
  </main>
</template>

<style lang="less" scoped>
@import '@/constants';

.studio {
  // background-color: #fff;
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
  width: 100vw;
  background-color: #f3f3f7;

  .main-content {
    display: flex;
    height: calc(100vh - @HEADER_HEIGHT);
  }

  // .navbar {
  //   flex: 0 0;
  // }

  .studio-view {
    flex: 1;
    overflow: hidden;
    // flex: 1 1;
  }

  .preview-view {
    bottom: 0;
    left: 0;
    position: absolute;
    right: 0;
    top: 0;
    z-index: 1;
  }
}
</style>
