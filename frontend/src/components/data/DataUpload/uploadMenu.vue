<template>
  <el-dropdown class="upload-menu" trigger="hover">
    <el-button type="primary"><i class="iconfont icon-shujuguanli-daorushuju icon" />上传</el-button>
    <template #dropdown>
      <el-dropdown-menu class="upload-dropdown-menu">
        <el-dropdown-item command="file">
          <Upload
            ref="fileDom"
            :config="{
              type: 'file',
              title: '文件'
            }"
            @update="update"
          />
        </el-dropdown-item>
        <el-dropdown-item command="directory">
          <Upload
            ref="directoryDom"
            :config="{
              type: 'directory',
              title: '文件夹'
            }"
            @update="update"
          />
        </el-dropdown-item>
        <!-- <el-dropdown-item>
          <el-button type="" disabled link>连接服务器</el-button>
        </el-dropdown-item> -->
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import Upload from './upload.vue'
import { throttle } from 'lodash'
import { useRoute } from 'vue-router'

const fileDom = ref(null)
const directoryDom = ref(null)
const route = useRoute()

const emit = defineEmits(['update'])

const update = throttle(() => {
  // 搜索结果页，上传完成不刷新列表
  const { search } = route.query
  if (!search) {
    emit('update')
  }
}, 1000)
</script>
<style lang="less" scoped>
.upload-menu {
  margin-right: 16px;
  .icon-shujuguanli-daorushuju {
    margin-right: 10px;
  }
  .upload-file,
  .upload-directory {
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
.upload-dropdown-menu {
  :deep(.el-dropdown-menu__item) {
    padding: 0 !important;
  }
}
</style>
