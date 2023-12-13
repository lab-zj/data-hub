<!-- 文件列表组件：可点击进入子层级 -->
<template>
  <div class="folder-list-with-bread">
    <div class="header">
      <el-breadcrumb ref="breadcrumbRef">
        <el-breadcrumb-item v-for="(item, index) in currentRoutes.split('/')" :key="`${item}-${index}`">
          <span v-if="index < initPathLength - 1" class="item">{{ item }}</span>
          <a v-else class="item text-ellipsis" @click="handleBreadCrumb(index)">{{ item }}</a>
        </el-breadcrumb-item>
      </el-breadcrumb>
      <div class="count" v-if="countVisible">
        {{ `${folderContent.list.length}份文件` }}
      </div>
    </div>
    <div class="folder-list" :style="{ height: getListHeight() }">
      <el-scrollbar v-if="folderContent.list.length > 0">
        <ul class="list" v-infinite-scroll="onScroll">
          <li
            class="row"
            v-for="(item, index) in folderContent.list"
            :key="`${item.path}-${index}`"
            :style="{
              backgroundColor: selectedFile && item.path === selectedFile.path ? 'rgb(103 113 252 / 20%)' : '#fff'
            }"
            @click="handleFile(item)"
          >
            <icon-font :type="getFileIconFont(getFileType(item.directory, item.name))" />
            <span class="name ellipsis" :title="item.name">{{ item.name }}</span>
          </li>
        </ul>
      </el-scrollbar>
      <div v-show="folderContent.list.length === 0" class="empty">
        <img src="@/assets/empty.png" />
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import useFolderPreview from '@/components/hooks/useFolderPreview'
import { getFileIconFont, getFileType } from '@/util/util'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  countVisible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['onSelected'])

const { folderContent, queryList, resetFolderContent, onScroll } = useFolderPreview()

const currentRoutes: any = ref('')
const breadcrumbRef: any = ref(null)
const selectedFile: any = ref(null)
const currentPath: any = ref(props.data.path)
const initPathLength: any = computed(() => {
  console.log('props.data.path:', props.data.path, props.data.path.split('/'))
  return props.data.path.split('/')?.length - 1
})
function getListHeight() {
  const height = breadcrumbRef?.value?.$el?.clientHeight || 0
  return height ? `calc(100% - ${height}px - 24px)` : '100%'
}

watch(
  () => props.data,
  (newValue, oldValue) => {
    if (props?.data && newValue?.path !== oldValue?.path) {
      fetchFolderList(props.data.path)
    }
  },
  { immediate: true, deep: true }
)

function fetchFolderList(path: string) {
  resetFolderContent()
  const _list = path.split('/')
  currentRoutes.value = _list.slice(0, _list.length - 1).join('/')
  queryList(path)
}

function handleFile(file: any) {
  if (file.directory) {
    fetchFolderList(file.path)
    currentPath.value = file.path
    emit('onSelected', {})
  } else {
    selectedFile.value = file
    emit('onSelected', file)
  }
}

function handleBreadCrumb(index: number) {
  const _list = currentRoutes.value.split('/').slice(0, index + 1)
  const path = `${_list.join('/')}/`
  currentPath.value = path
  fetchFolderList(path)
}
</script>
<style lang="less" scoped>
.folder-list-with-bread {
  height: 100%;

  .header {
    display: flex;
    justify-content: space-between;
    padding: 12px;
    .item {
      max-width: 100px;
      line-height: 19.33px;
    }
  }

  :deep(.el-breadcrumb) {
    flex: 1;
  }

  .count {
    opacity: 0.5;
    font-size: 12px;
    color: #222432;
  }
  .folder-list {
    padding-bottom: 2px;
  }
  .list {
    list-style: none;
    padding: 0;
    margin: 0;
    .row {
      padding: 0 12px;
      margin: 3px auto;
      height: 26px;
      display: flex;
      align-items: center;
      cursor: pointer;
      .iconfont {
        margin-right: 5px;
        display: flex;
      }
      &:hover {
        background-color: rgb(103 113 252 / 10%) !important;
      }
      .name {
        font-size: 14px;
      }
    }
  }
  .empty {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    img {
      width: 150px;
    }
  }
  .ellipsis {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }
}
</style>
