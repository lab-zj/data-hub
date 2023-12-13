<template>
  <div v-html="txtPreviewContent"></div>
  <div v-if="!txtPreviewContent" class="txt_empty">无内容</div>
</template>
<script setup lang="ts">
import { watch } from 'vue'
import useTxtPreview from '@/components/hooks/useTxtPreview'

const { txtPreviewContent, txtPreview } = useTxtPreview()

const props = defineProps({
  path: { type: String, default: '' }
})

watch(
  () => props.path,
  () => {
    if (props.path) {
      txtPreview(props.path)
    }
  },
  {
    immediate: true
  }
)
</script>
