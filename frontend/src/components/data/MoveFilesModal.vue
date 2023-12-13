<template>
  <div>
    <el-dialog class="move-dialog" :model-value="visible" width="30%" height="300" align-center @close="operateFunc('close')">
      <template #header="{ titleId, titleClass }">
        <div class="my-header">
          <span :id="titleId" :class="titleClass">{{ title }}</span>
        </div>
      </template>
      <div v-loading="moveDialogLoading" style="height: 100%">
        <el-breadcrumb style="margin: 20px 20px 16px 20px">
          <el-breadcrumb-item>
            <el-button @click="queryListInDialog('')" link>全部数据</el-button>
          </el-breadcrumb-item>
          <el-breadcrumb-item v-for="(item, index) in moveDialogRoutes.split('/')" :key="`${item}-${index}`">
            <!-- <el-button style="display: inline-block" @click="queryListInDialog(getBreadcrumbLink(item, index,moveDialogRoutes).replace('/management/data/path=',''))" link> -->
            <a
              class="text-ellipsis"
              @click="queryListInDialog(getBreadcrumbLink(item, index, moveDialogRoutes).replace('/management/data/path=', ''))"
              style="max-width: 100px; line-height: 19.33px"
              >{{ item }}</a
            >
            <!-- </el-button> -->
          </el-breadcrumb-item>
        </el-breadcrumb>
        <div class="folder-list">
          <div v-show="folderList.length > 0" class="folder" v-for="(item, index) in folderList" :key="`item-${item.name}`">
            <div v-show="item.editType" class="input-div">
              <i class="iconfont icon-a-wenjianjiashouqibeifen2 folder-icon" />
              <el-input
                v-model="folerName"
                :id="`modalInput${index}`"
                size="small"
                oninput="value=value.replace(/[, ]/g,'')"
                onkeyup="value=value.replace(/[, ]/g,'')"
                placeholder="新建文件夹"
              ></el-input>
              <el-button @click="confirmEditFolder(item, item.editType, index)" size="small" type="primary">确认</el-button>
              <el-divider direction="vertical" />
              <el-button @click="confirmEditFolder(item, 'cancel', index)" size="small">取消</el-button>
            </div>
            <div v-show="!item.editType" @click="queryListInDialog(moveDialogRoutes ? `${moveDialogRoutes}/${item.name}` : `${item.name}`)">
              <el-tooltip effect="dark" :content="item.name" placement="bottom-start" :show-after="500" :show-arrow="false">
                <span class="item-name text-ellipsis"><i class="iconfont icon-a-wenjianjiashouqibeifen2 folder-icon" /> {{ item.name }}</span>
              </el-tooltip>
            </div>
          </div>
          <div v-show="folderList.length === 0" class="empty">
            <img src="@/assets/empty.png" />
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="editFolderFunc('create', 0)" link>新建文件夹</el-button>
          <div>
            <el-button @click="operateFunc('close')">取消</el-button>
            <el-button type="primary" @click="operateFunc('move')" :loading="loading">{{ confirmText }}</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { watch, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { queryCategoryList, rename, createFolder } from '@/api/data'
import { throttle } from 'lodash'

const props = defineProps({
  data: {
    type: Object,
    default(): Object {
      return {}
    }
  },
  visible: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  moveIndex: {
    type: Array,
    default(): Object {
      return []
    }
  },
  title: { type: String, default: '移动到' },
  confirmText: { type: String, default: '移动到此处' }
})

const emit = defineEmits<{
  (e: 'operate', type: string, data?: any): void
}>()

// 移动弹窗相关
// const moveDialogVisible: any = ref(false)
const folderList: any = ref([])
const moveDialogLoading: any = ref(false)
const moveDialogRoutes: any = ref(null)
// const moveLoading: any = ref(false)
let page: number = 1
let size: number = 100
let totalElements: number = 0
const folerName: any = ref('')

watch(
  () => props.visible,
  (value: any) => {
    if (value) {
      queryListInDialog('')
      nextTick(() => {
        tableScroll.value = document.querySelector('.folder-list')
        tableScroll.value?.addEventListener(
          'scroll',
          // onScroll(),
          throttle(() => onScroll(), 500)
        )
      })
    } else {
      // selectedRowKeys.value = []
      folderList.value = []
    }
  },
  { deep: true }
)

// 在弹窗中加载文件夹目录
const queryListInDialog = async (folderPath: any) => {
  moveDialogRoutes.value = folderPath // ? `${moveDialogRoutes.value}${folderPath}/`:folderPath
  moveDialogLoading.value = true
  try {
    const response = await queryCategoryList({
      data: {
        page,
        size,
        path: moveDialogRoutes.value ? `${moveDialogRoutes.value}/` : ''
      }
    }) // `${path}/${folderPath}`)
    moveDialogLoading.value = false
    if (response.data.code === 200) {
      const result = response.data.data.content
      folderList.value = result.filter((folder: any) => {
        return folder.directory
      })
      if (props.moveIndex?.length > 0) {
        // 剔除所勾选要移动的目录
        for (let folder of props.moveIndex) {
          folderList.value = folderList.value.filter((item: any) => {
            return item.path !== folder
          })
        }
      }
    }
  } catch (error: any) {
    moveDialogLoading.value = false
    console.error(error)
  }
}

// 获取面包屑标题
const getBreadcrumbLink = (item: any, index: any, routes: any) => {
  if (index === 0) {
    return `/management/data/path=${item}`
  }
  const urls = routes.split('/')
  let path = `/management/data/path=${urls[0]}`
  for (let i = 1; i <= index; i++) {
    path += `/${urls[i]}`
  }
  return path
}

// 事件处理
const operateFunc = (type: any) => {
  switch (type) {
    case 'move': {
      emit('operate', type, moveDialogRoutes.value)
      break
    }
    case 'close': {
      emit('operate', type)
      break
    }
    // case 'move': break
    default:
      break
  }
}

// 创建、重命名文件夹
const editFolderFunc = (type: any, index?: any) => {
  // 移动文件夹窗口在创建
  folderList.value.unshift({
    name: '',
    directory: true,
    editType: type
  })
  // 输入框聚焦
  setTimeout(() => {
    const inputWrapper = document.querySelector(`#modalInput${index}`)
    nextTick(() => {
      inputWrapper.focus()
    })
  }, 300)
}

// 确定创建（取消）文件夹
const confirmEditFolder = async (record: any, type: any, index?: any) => {
  // cancel,create
  const itemIndex: any = folderList.value[index]
  if (type === 'cancel' || (record.name && folerName.value === record.name)) {
    // 取消 或者 重命名无修改
    if (record.editType === 'create') {
      // 移动文件夹弹窗在创建
      folderList.value.shift()
    } else {
      itemIndex.editType = ''
    }
    folerName.value = ''
    return
  }
  if (!folerName.value) {
    ElMessage.info('文件夹名字不能为空！')
    return
  }
  if (
    folerName.value.includes('*') ||
    folerName.value.includes('<') ||
    folerName.value.includes('>') ||
    folerName.value.includes('|') ||
    folerName.value.includes('%') ||
    folerName.value.includes('/')
  ) {
    ElMessage.error('文件名不能包含以下字符：<,>,|,*,?')
    return
  }
  const basePath = `${moveDialogRoutes.value.length > 0 ? `${decodeURIComponent(moveDialogRoutes.value)}/` : ''}`
  const response =
    type === 'create'
      ? await createFolder({ data: `${basePath}${folerName.value.trim()}/` })
      : await rename({
          data: {
            sourcePath: `${moveDialogRoutes.value}${itemIndex.name}${itemIndex.directory ? '/' : ''}`,
            targetPath: `${basePath}${folerName.value}${itemIndex.directory ? '/' : ''}`
          }
        })
  folerName.value = ''
  if (response.data.code === 200) {
    ElMessage.success(`${type === 'create' ? '创建' : '修改'}成功！`)
  } else {
    ElMessage.error(response.data.message)
  }
  queryListInDialog(moveDialogRoutes.value)
}

// 滚动加载
const onScroll = () => {
  console.log('table scroll')
  if (tableScroll.value.scrollHeight - tableScroll.value.scrollTop - 1 <= tableScroll.value.clientHeight) {
    if (size <= totalElements) {
      size += 30
      queryListInDialog(moveDialogRoutes.value)
    }
  }
}

const tableScroll: any = ref(null)

onUnmounted(() => {
  tableScroll.value?.removeEventListener(
    'scroll',
    // onScroll()
    throttle(() => onScroll(), 500)
  )
})
</script>

<style lang="less" scoped>
:deep(.el-dialog__header) {
  padding: 10px 20px !important;
  border-bottom: 1px solid #f2f2f2;
  .el-dialog__title {
    font-size: 14px;
  }
  .el-dialog__headerbtn {
    top: 0;
  }
}

:deep(.el-dialog__body) {
  padding: 0;
  height: 30vh;
}
:deep(.el-dialog__footer) {
  border-top: 1px solid #f2f2f2 !important;
  padding: 16px 20px;
}

.folder-list {
  overflow-y: auto;
  // height: 200px;
  height: calc(100% - 40px);
  .folder {
    line-height: 30px;
    height: 30px;
    padding-left: 20px;
    cursor: pointer;
    .folder-icon {
      margin-right: 8px;
      color: #ffc436;
    }
    &:hover {
      background-color: rgba(103, 113, 252, 0.1);
    }
  }
  &::-webkit-scrollbar {
    height: 6px;
    width: 6px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(144, 147, 153, 0.5);
    border-radius: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
    border-radius: 5px;
  }
}

.empty {
  text-align: center;
  padding-top: 32px;
  img {
    width: 208px;
    height: 110px;
  }
}

.item-name {
  // display: block;
  color: #373b52;
  width: calc(100% - 25px);
  height: 23px;
}

.text-ellipsis {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
}

.input-div {
  display: flex;
  align-items: center;

  .el-input {
    margin-right: 12px;
    width: 160px;
  }
}

:deep(.el-breadcrumb__item) {
  float: none;
}
</style>
