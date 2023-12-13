<template>
  <div class="project-content">
    <el-container>
      <el-container style="padding: 16px">
        <el-header>
          <div>
            <UploadMenu @update="queryList" />
            <el-button v-if="!route.query.search" @click="onCreateFolder">
              <i class="iconfont icon-xinjianwenjianjia" style="margin-right: 10px; font-size: 14px" />新建文件夹
            </el-button>
            <VirtualMenu @create="onCreate" />
          </div>
          <el-input clearable id="searchInput" placeholder="搜索文件、文件夹" @clear="clearSearch" v-model="searchInput" class="input-with-select" @keyup.enter="searchClick">
            <template #append>
              <el-button :icon="Search" @click="searchClick" />
            </template>
          </el-input>
        </el-header>
        <el-main>
          <div class="content-header">
            <el-breadcrumb v-if="!route.query.search">
              <el-breadcrumb-item>
                <a @click="clearSearch">全部数据</a>
                <!-- <router-link to="/management/data"><a>全部数据</a></router-link> -->
              </el-breadcrumb-item>
              <el-breadcrumb-item v-for="(item, index) in decodeURIComponent(routes).split('/')" :key="`${item}-${index}`">
                <el-tooltip effect="dark" :content="item" placement="bottom-start" :show-after="500" :show-arrow="false">
                  <router-link :to="getBreadcrumbLink(item, index, routes)">
                    <a class="text-ellipsis" style="max-width: 100px">{{ item }}</a>
                  </router-link>
                </el-tooltip>
              </el-breadcrumb-item>
            </el-breadcrumb>
            <div v-else class="result-title">
              <el-icon @click="router.go(-1)"><ArrowLeft /></el-icon><strong>&nbsp;{{ searchValue }}</strong
              >&nbsp;的搜索结果
            </div>
            <div class="btns" @click="onSwitch">
              <i v-if="!isCard" class="iconfont icon-liebiaoxianshi icon" style="color: #6973ff" />
              <i v-else class="iconfont icon-tuxianshi icon" />
            </div>
          </div>
          <DataPageList ref="dataPageListRef" :display-mode="isCard ? 'card' : 'table'" />
        </el-main>
      </el-container>
      <el-aside v-show="!sideIsCollapse">
        <div class="header">
          <strong>文件预览</strong>
          <span class="text-btn" @click="sideIsCollapse = !sideIsCollapse"><i class="iconfont icon-a-jiantoubeifen2 icon" /> 收起</span>
        </div>
        <div class="preview-content">
          <DataPreview :multiFile="multiFile" :record="currentRecord" />
        </div>
      </el-aside>
    </el-container>

    <!-- 新建数据库文件 -->
    <DataBaseFormModal
      :visible="dataBaseFormVisible"
      :loading="virtualLoading"
      :connecting="connecting"
      @close="setDataBaseFormVisible(false)"
      @create="createVirtualFile"
      @connect="connectTestFile"
    />
    <!-- 新建S3文件 -->
    <DataS3FormModal :visible="s3FormVisible" :loading="virtualLoading" @close="setS3FormVisible(false)" @create="createVirtualFile" />
  </div>
</template>

<script setup lang="ts">
// import DataItem from '@/components/data/DataItem.vue'
import { computed, ref, onMounted, watch, provide } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import DataPreview from '@/components/data/DataPreview.vue'
import UploadMenu from '@/components/data/DataUpload/uploadMenu.vue'
import { useDetailStore } from '@/stores/detail'
import VirtualMenu from '@/components/data/DataVirtual/VirtualMenu.vue'
import DataBaseFormModal from '@/components/data/DataVirtual/DataBaseFormModal.vue'
import DataS3FormModal from '@/components/data/DataVirtual/DataS3FormModal.vue'
import useVirtual from '@/components/data/hooks/useVirtual'

import DataPageList from '@/views/DataPage/components/DataPageList.vue'
import { IDataItem } from '@/views/DataPage/util'

import useListMode from '@/views/DataPage/hooks/useListMode'

// 通用
const route = useRoute()
const router = useRouter()

// 右侧详情
const sideIsCollapse: any = ref<boolean>(true)

const searchInput: any = ref('')

let routes: any = computed(() => {
  //用于面包屑
  const path = route?.path.indexOf('path') > -1 ? route.path.split('path=')[1] : ''
  return path
})

const searchValue = computed(() => {
  return route.query.search || ''
})

watch([searchValue], () => {
  searchInput.value = searchValue.value || ''
})

// 列表模式切换
const { isCard, onSwitch } = useListMode()

/*********************
 * 功能函数 start
 *********************/

// 获取面包屑标题
const getBreadcrumbLink = (item: any, index: any, routes: any) => {
  // console.log(item,index)
  if (index === 0) {
    return `/management/data/path=${item}`
    // console.log(`/management/data/path=${item}`)
  }
  const urls = routes.split('%2F')
  let path = `/management/data/path=${urls[0]}`
  for (let i = 1; i <= index; i++) {
    path += `%2F${urls[i]}`
  }
  return path
}

// 预览
const currentRecord: any = ref({})
const multiFile: any = ref([])

const setPreview = (selectedItems: IDataItem[]) => {
  sideIsCollapse.value = !(selectedItems.length > 0)
  currentRecord.value = selectedItems?.[0] || {}
  multiFile.value = selectedItems || []
}
/********************* 功能函数 end *********************/

/*********************************
 * 图表功能区相关 start
 *********************************/

// 点击搜索
const searchClick = () => {
  router.push(`/management/data${searchInput.value ? `?search=${searchInput.value}` : ''}`)
}

// 点击清空搜索（不包括键盘清空，键盘输入清空需手动触发search
const clearSearch = () => {
  console.log('search clear')
  router.push('/management/data')
  // queryList()
}

// 获取当前文件夹目录
const queryList = async () => {
  if (dataPageListRef.value) {
    dataPageListRef.value.onRefreshList()
  }
}

const detailStore = useDetailStore()
onBeforeRouteLeave((to: any, from: any) => {
  const { fullPath } = from
  detailStore.setFromPath(fullPath)
})

/** 新建数据库文件、S3文件相关 */
const { dataBaseFormVisible, s3FormVisible, virtualLoading, connecting, setDataBaseFormVisible, setS3FormVisible, createVirtualFile, connectTestFile } = useVirtual(queryList)

function onCreate(type: string) {
  if (type === 'database') {
    setDataBaseFormVisible(true)
  } else {
    setS3FormVisible(true)
  }
}

const dataPageListRef: any = ref(null)
function onCreateFolder() {
  if (dataPageListRef.value) {
    dataPageListRef.value.onCreateFolder()
  }
}

onMounted(() => {
  if (searchValue.value) {
    searchInput.value = searchValue.value
  }
})

provide('setPreview', setPreview)
</script>

<style lang="less" scoped>
@import '@/constants';

.project-content {
  background: #fff;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
  line-height: 20px;
  height: calc(100% - 64px - 44px - 24px);
  margin: 12px;

  .el-container {
    height: 100%;
  }

  .el-aside {
    border-left: 1px solid #e9e9e9;
    padding: 16px;
    width: 400px;

    .header {
      color: #373b52;
      display: flex;
      justify-content: space-between;
      align-items: center;

      strong {
        font-size: 16px;
      }

      .text-btn {
        color: #5d637e;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        &:hover {
          color: #6973ff;
        }
      }
    }
    .preview-content {
      margin-top: 21px;
      height: calc(100% - 16px - 20px - 21px);
    }
  }

  .el-header {
    // border-bottom: 1px solid #E9E9E9;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0;
    height: 32px;

    :deep(.el-input) {
      --el-border-radius-base: 16px;
      width: 260px;
    }
  }
  .el-main {
    padding: 0;
  }

  .upload-btn {
    margin-right: 32px;

    .icon {
      font-size: 17px;
      margin-right: 10px;
    }
  }

  .content-header {
    padding: 16px 0;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .btns {
      width: 60px;
      display: flex;
      justify-content: right;
      color: #6973ff;
      padding-right: 16px;

      .icon {
        font-size: 17px;
        cursor: pointer;
        &:hover {
          color: #6973ff;
        }
        &.active {
          color: #6973ff;
        }
      }
    }

    .result-title {
      display: flex;
      align-items: center;

      i {
        line-height: 20px;
        font-size: 20px;
        cursor: pointer;

        &:hover {
          color: #6973ff;
        }
      }
    }
  }
}

.text-ellipsis {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
