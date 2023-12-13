<template>
  <CsvFileTemplate :path="props.data.path" />
</template>
<script setup lang="ts">
import CsvFileTemplate from '@/components/common/CsvFileTemplate.vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})
</script>
<style lang="less" scoped></style>
