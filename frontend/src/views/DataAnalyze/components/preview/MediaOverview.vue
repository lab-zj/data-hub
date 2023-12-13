<template>
  <slot name="overviewHeader"></slot>
  <slot name="previewHeader"></slot>
  <div class="media-overview">
    <div v-if="['png', 'image'].includes(type)" class="image">
      <ImgFileTemplate :path="props.data.path" />
    </div>
    <div v-else-if="['mp4', 'video'].includes(type)" class="video">
      <VideoFileTemplate :path="props.data.path" :pictureInPictureToggle="false" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import VideoFileTemplate from '@/components/common/VideoFileTemplate.vue'
import ImgFileTemplate from '@/components/common/ImgFileTemplate.vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const type = computed(() => {
  return props.data.type
})
</script>
<style lang="less" scoped>
.media-overview {
  width: 100%;
  height: 100%;
  background: #fafafc;
  display: flex;
  align-items: center;
  justify-content: center;
  .video {
    width: 100%;
    height: 50%;
  }
}
</style>
