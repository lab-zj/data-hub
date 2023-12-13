<!-- 算法文件列表组件：点击进入子层级 -->
<template>
  <div class="algo-folder-list-common">
    <el-scrollbar v-if="folderContent.list.length > 0">
      <ul class="list" v-infinite-scroll="onScroll">
        <li
          class="row"
          v-for="(item, index) in folderContent.list"
          :key="`${item.path}-${index}`"
          :style="{
            backgroundColor: selectedFile && item.path === selectedFile.path ? 'rgb(103 113 252 / 20%)' : '#fff',
            cursor: item.directory ? 'pointer' : 'default',
            color: item.directory ? '#373b52' : '#e9e9e9'
          }"
          @click="handleFile(item)"
        >
          <icon-font :type="getFileIconFont(getFileType(item.directory, item.name))" class="type-iconfont" :class="item.directory ? '' : 'can-not-select'" />
          <span class="name ellipsis" :title="item.name">{{ item.name }}</span>
          <icon-font v-if="item.fileType === 'ALGORITHM'" type="icon-suanfa" class="algo-iconfont" />
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
import useAlgorithmFolder from '@/components/hooks/useAlgorithmFolder.ts'
import { getFileIconFont, getFileType } from '@/util/util'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['select', 'loading'])

const { folderContent, queryList, resetFolderContent, onScroll } = useAlgorithmFolder()

const selectedFile: any = ref(null)

watch(
  () => props.data,
  (newVal: any, oldVal: any) => {
    if (props?.data && newVal?.path !== oldVal?.path) {
      // console.log('props.data', props.data)
      emit('loading', true)
      resetFolderContent()
      queryList(props.data.path, () => {
        emit('loading', false)
      })
    }
  },
  { immediate: true, deep: true }
)

function handleFile(file: any) {
  if (file.directory) {
    selectedFile.value = file
    emit('select', file)
  }
}
</script>
<style lang="less" scoped>
.algo-folder-list-common {
  height: 100%;
  .list {
    color: #373b52;
    list-style: none;
    padding: 0 !important;
    position: relative;
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
      }
      .type-iconfont {
        display: flex;
        align-items: center;
      }

      .algo-iconfont {
        display: flex;
        justify-content: center;
        align-items: center;
        position: absolute;
        right: 20px;
      }
      .can-not-select {
        filter: grayscale(1);
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
