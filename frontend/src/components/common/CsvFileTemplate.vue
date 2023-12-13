<!-- 数据表模板 -->
<template>
  <div class="csv-file-template">
    <el-table
      :data="csvPreviewContent.valueList && csvPreviewContent.valueList.content"
      border
      width="100%"
      style="height: 100%"
      header-row-class-name="preview-csv-header-row"
      empty-text="无内容"
    >
      <el-table-column v-for="(column, index) in csvPreviewContent.columnVOList" :key="column.name" :label="column.name" :prop="index + 1" />
    </el-table>
  </div>
</template>
<script setup lang="ts">
import { watch } from 'vue'
import useCsvPreview from '@/components/hooks/useCsvPreview'

const props = defineProps({
  path: {
    type: String,
    default: ''
  }
})

const { csvPreviewContent, csvPreview, reset } = useCsvPreview()

watch(
  () => props.path,
  () => {
    if (props.path) {
      reset()
      csvPreview(props.path)
    }
  },
  {
    immediate: true
  }
)
</script>
<style lang="less" scoped>
.csv-file-template {
  height: 100%;
}
</style>
