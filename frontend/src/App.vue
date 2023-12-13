<script setup lang="ts">
import { RouterView } from 'vue-router'
import UploadManage from '@/components/data/DataUpload/uploadManage.vue'
import useUploadInterCept from '@/components/data/hooks/useUploadInterCept';

useUploadInterCept()

</script>

<template>
  <RouterView />
  <UploadManage />
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
