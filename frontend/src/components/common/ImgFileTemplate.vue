<template>
  <el-image :src="imgPreviewContent" fit="contain" :preview-src-list="[imgPreviewContent]" />
</template>
<script setup lang="ts">
import { watch } from 'vue'
import useImgPreview from '@/components/hooks/useImgPreview'

const props = defineProps({
  path: { type: String, default: '' }
})

const { imgPreviewContent, imagePreview } = useImgPreview()

watch(
  () => props.path,
  () => {
    if (props.path) {
      imagePreview(props.path)
    }
  },
  {
    immediate: true
  }
)
</script>
<style lang="less" scoped></style>
