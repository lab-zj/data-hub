<template>
  <div class="header-container">
    <span class="back" @click="back">
      <icon-font class="icon-back" type="icon-jiantou" />
      返回
    </span>
    <span class="project-name ellipsis" :title="projectInfoStore.projectName">{{ projectInfoStore.projectName }}</span>

    <InfoHeader />
  </div>
</template>

<script setup lang="ts">
import InfoHeader from '@/components/home/InfoHeader.vue'
import { useProjectInfo } from '@/stores/projectInfoStore'
import { useRouter } from 'vue-router'

const projectInfoStore = useProjectInfo()
const router = useRouter()

function back() {
  router.push('/management/projects')
}
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

.back {
  color: #fff;
  font-size: 16px;
  padding: 4px;
  cursor: pointer;
  margin-left: 12px;
}

.project-name {
  color: #fff;
  font-size: 20px;
  max-width: 360px;
}
.ellipsis {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
