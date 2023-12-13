<template>
  <slot name="overviewHeader"></slot>
  <div class="folder-overview">
    <FolderListWithBread :data="folderInfo" :breadcrumbVisible="false" :countVisible="true" @onSelected="onSelected" />
  </div>
  <div class="file-content">
    <component :is="dynamicSubPanelComponent" class="file-content-sub-panel" :data="selectedFile" @view="onView">
      <template #previewHeader>
        <div class="preview-header">
          <strong class="title ellipsis" :title="selectedFile && selectedFile.name">{{ selectedFile && selectedFile.name }}</strong>
          <span class="view-detail" @click="onView">查看详情</span>
        </div>
      </template>
    </component>
  </div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import FolderListWithBread from '@/components/common/FolderListWithBread.vue'
import CsvOverview from './CsvOverview.vue'
import MediaOverview from './MediaOverview.vue'
import TxtOverview from './TxtOverview.vue'
import UnknownOverview from './UnknownOverview.vue'
import VirtualOverview from './VirtualOverview.vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['view', 'detailClose'])

const selectedFile: any = ref(null)

const folderInfo = computed(() => {
  return {
    path: props.data.path,
    name: props.data.name,
    directory: props.data.directory
  }
})

/**
 * 动态组件：子菜单
 */
const dynamicSubPanelComponent = computed(() => {
  const { type } = selectedFile.value || {}
  if (['csv'].includes(type)) {
    return CsvOverview
  } else if (['image', 'video'].includes(type)) {
    return MediaOverview
  } else if (['txt'].includes(type)) {
    return TxtOverview
  } else if (['virtual'].includes(type)) {
    return VirtualOverview
  } else if (['unknown'].includes(type)) {
    return UnknownOverview
  }
  return ''
})

function onSelected(file: any) {
  console.log('onselected:', file, file.type)
  selectedFile.value = {
    ...file
  }
  emit('detailClose')
}

function onView() {
  emit('view', selectedFile.value)
}
</script>
<style lang="less" scoped>
.folder-overview {
  height: 311px;
  border-bottom: 1px solid #e9e9e9;
}
.file-content {
  flex: 1;
  padding: 12px;
  overflow: auto;

  :deep(.media-overview) {
    height: calc(100% - 32px);
  }
  :deep(.content) {
    padding: 0;
  }
  :deep(.txt-overview) {
    height: calc(100% - 32px);
  }
}
.ellipsis {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
