<template>
  <slot name="overviewHeader"></slot>
  <div class="content">
    <div class="preview-header">
      <strong class="title">字段预览</strong>
      <span class="view-detail" @click="onView">查看详情</span>
    </div>
    <el-scrollbar v-if="columnVOList.length > 0">
      <ul class="csv-list">
        <li v-for="(column, index) in columnVOList" :key="`${index}-${column.name}`" class="row">
          <icon-font :type="getFieldIconType(column.type)" class="field_type_iconfont" /><span>{{ column.name }}</span>
        </li>
      </ul>
    </el-scrollbar>
    <div v-else class="empty">
      <img src="@/assets/empty.png" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { watch, computed } from 'vue'
import useCsvPreview from '@/components/hooks/useCsvPreview'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['view'])

const { csvPreviewContent, csvPreview, reset } = useCsvPreview()
const columnVOList = computed(() => {
  return csvPreviewContent.value?.columnVOList || []
})

watch(
  () => props.data,
  (newValue, oldValue) => {
    if (props?.data && newValue?.path !== oldValue?.path) {
      reset()
      csvPreview(props.data.path)
    }
  },
  { immediate: true, deep: true }
)

function onView() {
  emit('view', props.data)
}

function getFieldIconType(type: String) {
  const _upperCase = type.toUpperCase()
  if (_upperCase === 'INT') {
    return 'icon-bianzu'
  } else if (_upperCase === 'DATE') {
    return 'icon-shujuleixing-riqi'
  } else if (_upperCase === 'STRING') {
    return 'icon-a-bianzu4'
  }
  return 'icon-a-bianzu4'
}
</script>
<style lang="less" scoped>
.content {
  padding: 12px;
  height: 100%;
  display: flex;
  flex-direction: column;
  .csv-list {
    list-style: none;
    padding: 0;
    margin: 0;
    font-size: 14px;
    color: #373b52;
    .row:first-of-type {
      margin-bottom: 12px;
    }
    .row + .row {
      margin-bottom: 12px;
    }
    .field_type_iconfont {
      margin-right: 12px;
    }
  }
  .empty {
    display: flex;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
    img {
      width: 148px;
    }
  }
}
</style>
