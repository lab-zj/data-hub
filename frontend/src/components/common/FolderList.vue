<!-- 文件列表组件：不能点击进入子层级 -->
<template>
  <div class="folder-list-common">
    <el-scrollbar v-if="folderContent.list.length > 0">
      <ul class="list" v-infinite-scroll="onScroll">
        <li
          class="row"
          v-for="(item, index) in folderContent.list"
          :key="`${item.path}-${index}`"
          :style="
            needSelected
              ? {
                  backgroundColor: selectedFile && item.path === selectedFile.path ? 'rgb(103 113 252 / 20%)' : '#fff'
                }
              : {}
          "
          @click="handleFile(item)"
        >
          <icon-font :type="getFileIconFont(getFileType(item.directory, item.name))" class="type-iconfont" />
          <span class="name ellipsis" :title="item.name">{{ item.name }}</span>
        </li>
      </ul>
    </el-scrollbar>
    <div v-show="folderContent.list.length === 0" class="empty">
      <img src="@/assets/empty.png" />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import { getFileIconFont, getFileType } from '@/util/util'
import useFolderPreview from '@/components/hooks/useFolderPreview'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  needSelected: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['onSelect'])

const { folderContent, queryList, resetFolderContent, onScroll } = useFolderPreview()

const selectedFile: any = ref(null)

watch(
  () => props.data,
  () => {
    if (props?.data) {
      resetFolderContent()
      queryList(props.data.path)
    }
  },
  { immediate: true, deep: true }
)

function handleFile(file: any) {
  if (!props.needSelected) return
  console.log('handleFile:', file)
  selectedFile.value = file
  emit('onSelect', file)
}
</script>
<style lang="less" scoped>
.folder-list-common {
  height: 100%;
  .list {
    color: #373b52;
    list-style: none;
    padding: 0 !important;
    .row {
      margin: 8px 0;
      display: flex;
      align-items: center;
      img {
        width: 16px;
        height: 16px;
      }
      .name {
        margin-left: 8px;
        color: #373b52;
      }
      .type-iconfont {
        display: flex;
        align-items: center;
      }
    }
  }
  .empty {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
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
