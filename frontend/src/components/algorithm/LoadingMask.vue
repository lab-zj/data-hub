<template>
  <!--加载界面-->
  <div v-if="props.isLoading" class="loading" v-loading="props.isLoading" element-loading-background="rgba(0, 0, 0, 0.05)">
    <div class="loadingText">
      {{ props.loadingText }}
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps({
  isLoading: { type: Boolean, default: false },
  loadingText: { type: String, default: '' }
})
</script>
<style scoped>
.loading {
  background-color: rgba(0, 0, 0, 0.05);
  bottom: 0;
  left: 0;
  position: fixed;
  right: 0;
  text-align: center;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.loadingText {
  position: absolute;
  top: 54%;
  color: #626ce0;
}
</style>
