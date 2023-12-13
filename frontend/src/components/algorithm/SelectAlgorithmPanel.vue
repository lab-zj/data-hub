<template>
  <div class="select-algorithm-panel">
    <el-button class="back-pre" type="primary" @click="backPre" link :disabled="!currentRoutes" :loading="loading">返回上一级</el-button>
    <icon-font type="icon-guanbibeifen" class="close-iconfont" @click="onClose" />
    <div class="folder-list">
      <AlgorithmFolderList :data="{ path: initPath }" @select="onSelect" @loading="onLoading" />
    </div>
    <el-button class="add-dataset" type="primary" @click="handleAddDataet" link>新增数据源</el-button>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import AlgorithmFolderList from '@/components/algorithm/AlgorithmFolderList.vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const emit = defineEmits(['select', 'close'])

const currentRoutes: any = ref('')
const initPath: any = ref('')
const loading: any = ref(false)

function handleAddDataet() {
  const routeUrl = router.resolve({
    path: '/management/data'
  })
  window.open(routeUrl.href, '_blank')
}

function onSelect(file: any) {
  console.log('file:', file.path)
  if (file.fileType !== 'ALGORITHM') {
    currentRoutes.value = file.path
    initPath.value = file.path
  } else {
    emit('select', file)
    emit('close')
  }
}

function backPre() {
  const _list = currentRoutes.value.split('/')
  const _path = _list.slice(0, _list.length - 2).join('/')
  currentRoutes.value = _path
  initPath.value = _path
}

function onLoading(_loading: boolean) {
  loading.value = _loading
}

function onClose() {
  emit('close')
}
</script>
<style lang="less" scoped>
.select-algorithm-panel {
  position: absolute;
  width: 100%;
  height: 300px;
  background-color: #fff;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
  z-index: 9;

  .back-pre {
    padding: 20px 0 0 20px;
  }
  .close-iconfont {
    cursor: pointer;
    position: absolute;
    right: 20px;
    top: 13px;
    &:hover {
      color: #646cff;
    }
  }
  .folder-list {
    height: 220px;
  }
  .list {
    color: #373b52;
    list-style: none;
    padding: 0 !important;
    height: 240px;
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

  .add-dataset {
    position: relative;
    left: 20px;
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

  :deep(.row) {
    padding-left: 20px;
  }
}
</style>
