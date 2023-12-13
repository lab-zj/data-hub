<template>
  <div class="detail-header-template">
    <div class="title ellipsis" :title="name">{{ name }}</div>
    <div class="tools">
      <!-- <el-icon v-if="!showSearchBox" class="search-icon" @click="showSearchBox = true"><Search /></el-icon>
      <el-input
        v-if="showSearchBox"
        v-model="searchKeyWord"
        size="small"
        placeholder="请输入"
        suffix-icon="Search"
        oninput="value=value.replace(/[, ]/g,'')"
        @keyup.enter="handleSearch"
      />
      <icon-font type="icon-xiazaibendi" @click="onDownload" /> -->
      <div class="wrap" @click="onClose"><icon-font type="icon-a-jiantoubeifen2" /><span>收起</span></div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, computed } from 'vue'
import { debounce } from 'lodash'
import { compatible } from '@/util/util'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['close'])

const searchKeyWord: any = ref('')
const showSearchBox: any = ref(false)

const name = computed(() => {
  return props.data?.name || '-'
})

function onDownload() {
  if (!props.data?.path) {
    return
  }
  const path = compatible(props.data?.path)
  if (path) {
    window.open(`/api/filesystem/basic/download/batch?pathList=${encodeURIComponent(path)}`)
  }
}

function onClose() {
  emit('close')
}

const handleSearch = debounce(() => {
  search()
}, 300)

function search() {}
</script>
<style lang="less" scoped>
.detail-header-template {
  display: flex;
  height: 44px;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding: 12px 12px 6px;
  .title {
    flex: 1;
  }
  .tools {
    display: flex;
    flex-direction: row;
    align-items: center;

    .iconfont,
    .wrap {
      cursor: pointer;
    }
    .wrap {
      display: flex;
      flex-direction: row;
      align-items: center;
      color: #6973ff;
      margin-left: 16px;
      .iconfont {
        margin-right: 5px;
        display: flex;
      }
    }
    .search-icon {
      cursor: pointer;
      width: 24px;
      height: 24px;
      margin-right: 8px;

      &:hover {
        background-color: #eaeafd;
      }
    }
    :deep(.el-input) {
      width: auto;
    }
  }
}

:deep(.el-input) {
  margin-right: 9px;
}

:deep(.el-input__wrapper) {
  border-radius: 16px;
}

.ellipsis {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
