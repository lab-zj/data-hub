<template>
  <ProjectHeader />
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProjectHeader from '@/components/home/ProjectHeader.vue'
import { useProjectInfo } from '@/stores/projectInfoStore'

const route = useRoute()

const projectInfoStore = useProjectInfo()

onMounted(() => {
  projectInfoStore.fetchProjectInfo(route.params.id as string) // 获取项目基本信息
})
</script>
