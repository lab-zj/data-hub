<template>
  <main class="studio">
    <HomeHeader />
    <UserNavBar />
    <NoDatabaseUser />
    <router-view />
  </main>
</template>

<script setup lang="ts">
import HomeHeader from '@/components/home/HomeHeader.vue'
import UserNavBar from './components/UserNavBar.vue'
import NoDatabaseUser from '@/views/UserCenter/components/NoDatabaseUser.vue'
</script>
<style lang="less" scoped>
.studio {
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: relative;
  width: 100vw;
  background-color: #f3f3f7;
}
</style>
