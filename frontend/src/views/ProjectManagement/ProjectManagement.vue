<template>
  <div class="project-panel">
    <el-container class="container">
      <ProjectList />
    </el-container>
  </div>
  <NoDatabaseUser />
</template>
<script setup lang="ts">
import { onMounted } from 'vue'
import ProjectList from './components/ProjectList.vue'
import useUser from '@/components/hooks/useUser'
import NoDatabaseUser from '@/views/UserCenter/components/NoDatabaseUser.vue'

const { dingLoginOnRedirectPage } = useUser()

onMounted(() => {
  dingLoginOnRedirectPage()
})
</script>
<style lang="less" scoped>
.project-panel {
  margin: 12px;
  background-color: #f3f3f7;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
  line-height: 20px;
  height: calc(100% - 64px - 44px - 24px);
  overflow: hidden;
  .container {
    height: 100%;
    width: 100%;
  }
}
</style>
