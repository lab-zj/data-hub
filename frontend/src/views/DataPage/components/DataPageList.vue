<template>
  <div v-loading="fileListLoading">
    <div class="data-page-list four-scroll-y" :style="{ height: `${tableHeight}px` }">
      <!-- table mode -->
      <DataPageTableMode
        v-show="displayMode === 'table'"
        ref="pageTableRef"
        :table-height="tableHeight"
        :data-items="fileList"
        :is-check-all="isCheckAll"
        :checked-keys="checkedDataKeys"
        @on-check-all-change="onDataListCheckAllChange"
        @on-check-item-change="onDataItemCheckChange"
        @on-refresh-list="onRefreshList"
      />
      <!-- card mode -->
      <DataPageCardMode
        v-show="displayMode === 'card'"
        ref="pageCardRef"
        :table-height="tableHeight"
        :data-items="fileList"
        :is-check-all="isCheckAll"
        :checked-keys="checkedDataKeys"
        @on-check-all-change="onDataListCheckAllChange"
        @on-check-item-change="onDataItemCheckChange"
        @on-refresh-list="onRefreshList"
      />
    </div>
    <div class="batch-btns" :class="{ active: checkedDataKeys.length > 1 }">
      <span class="btn" @click="() => downloadFile(checkedDataKeys)"> <icon-font type="icon-xiazaibendi" /> 下载 </span>
      <span class="btn" @click="() => moveFiles()"> <icon-font type="icon-tufenxi-huidaohuaban" /> 移动 </span>
      <span class="btn" @click="() => openDeleteConfirm(checkedDataKeys)"><icon-font type="icon-shanchu" /> 删除 </span>
    </div>

    <MoveFilesModal :visible="moveDialogVisible" @operate="operateFunc" :loading="moveLoading" :move-index="moveIndex" />
    <MessageDialog :visible="confirmDialogVisible" :confirm-object="confirmObject" @operate="operateFunc" />
  </div>
</template>

<script setup lang="ts">
import { Ref, onMounted, ref, provide, h, computed, watch, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import DataPageCardMode from './DataPageCardMode.vue'
import DataPageTableMode from './DataPageTableMode.vue'
import MoveFilesModal from '@/components/data/MoveFilesModal.vue'
import MessageDialog from '@/components/data/MessageDialog.vue'
import { IDataItem, downloadFile } from '../util'

import { batchDelete, fileBatchExist, batchMove } from '@/api/data'

import { useFetchDataList } from '../hooks/useFetchDataList'
import { throttle } from 'lodash'

const route = useRoute()
const router = useRouter()
const basePath = computed(() => {
  return route.params.path || ''
})

const searchValue = computed(() => {
  return route.query.search || ''
})

interface Props {
  displayMode: 'table' | 'card'
}
const props = withDefaults(defineProps<Props>(), {
  displayMode: 'table'
})

watch([basePath, searchValue], ([, newSearchValue], [, oldSearchValue]) => {
  if (newSearchValue !== oldSearchValue) {
    // 搜索触发情况下如果不清空，会触发key 重复、列表重复问题
    fileList.value = []
    resetState()
  }
  if (route.path.startsWith('/management/data')) {
    onRefreshList()
  }
})

const setPreview: any = inject('setPreview')

const isCheckAll = ref(false) // is all checked
const checkedDataKeys: Ref<string[]> = ref([]) // checked data path

const { fileListLoading, fileList, getFileList, paramsPageIndex, hasMoreData, onParamsPageChange } = useFetchDataList()

/**
 * handle when check all btn change
 * @param checked checked status
 */
function onDataListCheckAllChange(checked: boolean) {
  isCheckAll.value = checked
  checkedDataKeys.value = checked ? fileList.value.map((item) => item.path) : []
}

/**
 * handle when item check status change
 * @param checkedList checked path
 */
function onDataItemCheckChange(checkedList: string[]) {
  checkedDataKeys.value = checkedList
  isCheckAll.value = checkedDataKeys.value.length === fileList.value.length
}

const pageTableRef: any = ref()
const pageCardRef: any = ref()

// 新建文件夹，输入框，监听新建文件夹按钮事件
function onCreateFolder() {
  const instance = props.displayMode === 'table' ? pageTableRef : pageCardRef

  if (instance) {
    instance.value.initCreateFolder()
  }
}

/**
 * refresh list
 */
function onRefreshList() {
  paramsPageIndex.value = 1 // reset params page
  resetState()
  getFileList({
    path: basePath.value,
    keyword: searchValue.value
  })
}

function resetState() {
  // fileList.value = []
  isCheckAll.value = false
  checkedDataKeys.value = []
}

// 移动文件、二次确认弹窗事件响应统一处理
const operateFunc = (type: any, data?: any) => {
  // console.log(type,data)
  switch (type) {
    case 'move': // 移动文件（夹
      {
        confirmMove(data)
      }
      break
    case 'delete': // 删除文件（夹
      {
        confirmDialogVisible.value = false
        deleteFileFunc(data)
      }
      break
    case 'coverAll': // 移动时覆盖文件夹
      {
        confirmDialogVisible.value = false
        moveApi(data)
      }
      break
    case 'close': // 关闭二次弹窗
      {
        moveDialogVisible.value = false
        confirmDialogVisible.value = false
        moveLoading.value = false
      }
      break
    default:
      break
  }
}

// 删除弹窗
const confirmDialogVisible: any = ref(false)
const confirmObject: any = ref({ title: '提示', message: '确认删除？', data: {} })

// 删除二次确认弹窗
const openDeleteConfirm = (data: IDataItem | string[]) => {
  if (!Array.isArray(data) && data.path) {
    // 删除单个
    confirmObject.value = {
      title: `删除“${data.name}”文件`,
      message: `确定删除“${data.name}”吗？`,
      data: [data.path],
      operate: 'delete',
      confirmBtnText: '删除'
    }
  } else {
    confirmObject.value = {
      title: '删除文件',
      message: '确定删除所选中的吗？',
      confirmBtnText: '删除',
      operate: 'delete',
      data: data
    }
  }
  confirmDialogVisible.value = true
}

// 删除
const deleteFileFunc = async (val: string[]) => {
  const response = await batchDelete({
    data: val
  })

  if (response.data.code === 200) {
    ElMessage.success('删除成功！')
  }
  onRefreshList()
  setPreview([])
}

// 移动弹窗相关
const moveDialogVisible: any = ref(false)
const moveLoading: any = ref(false)
let moveIndex: any = null

// （批量）移动文件(夹)
const moveFiles = async (fileData?: IDataItem | null) => {
  moveIndex = fileData ? [fileData.path] : checkedDataKeys.value
  moveDialogVisible.value = true
}

// 确认移动
const confirmMove = async (movePath: any) => {
  moveLoading.value = true

  // 重名检测
  await checkExist(movePath)
  setPreview([])
}

// 批量检测是否有重名
const checkExist = async (movePath: any) => {
  const targetList = moveIndex.map((element: any) => {
    const array = element.split('/')
    if (element.substr(-1) === '/') {
      return movePath ? `${movePath}/${array[array.length - 2]}/` : `${array[array.length - 2]}/`
    }
    return movePath ? `${movePath}/${array[array.length - 1]}` : `${array[array.length - 1]}`
  })
  const response = await fileBatchExist({ data: { pathList: targetList.join(',') } })
  const { data, code } = response.data
  if (code === 200) {
    // 二次确认
    if (Object.keys(data).some((key) => data[key] === true)) {
      confirmObject.value = {
        title: '是否覆盖',
        message: '存在同名文件/文件夹，是否全部覆盖？',
        confirmBtnText: '覆盖',
        operate: 'coverAll',
        data: movePath
      }
      confirmDialogVisible.value = true
    } else {
      await moveApi(movePath)
    }
  }
}

// move请求接口
const moveApi = async (movePath: any) => {
  const response: any = await batchMove({
    data: {
      sourcePathList: moveIndex,
      targetPath: `${movePath}/`,
      override: true
    }
  })
  moveLoading.value = false
  moveDialogVisible.value = false

  if (response.data.code === 200) {
    ElMessage.success({
      message: h('p', null, [
        h('span', null, '移动成功！'),
        h(
          'a',
          {
            style: 'color: #6973ff',
            onClick: () => {
              router.push(movePath ? `/management/data/path=${encodeURIComponent(movePath)}` : '/management/data')
            }
          },
          '立即查看'
        )
      ])
    })
    // 更新列表
    onRefreshList()
  } else {
    ElMessage.error(response.data.message)
  }
}

const tableHeight: any = ref(document.body.clientHeight - 64 - 44 - 24 - 32 - 32 - 52) // 内容区高度
function setHeight() {
  tableHeight.value = document.body.clientHeight - 64 - 44 - 24 - 32 - 32 - 52
}

let tableScrollEl: any = null // table scroll element
let cardScrollEl: any = null // card scroll element

/**
 * init scroll events
 */
function initScroll() {
  tableScrollEl = document.querySelector('.data-page-list .el-scrollbar__wrap') // 表头固定，拿的是内容区
  cardScrollEl = document.querySelector('.data-page-list')

  tableScrollEl.addEventListener(
    'scroll',
    throttle(() => onScroll(tableScrollEl), 300)
  )
  cardScrollEl.addEventListener(
    'scroll',
    throttle(() => onScroll(cardScrollEl), 300)
  )

  checkCardContent()
}

// handle scoll
function onScroll(target: HTMLElement) {
  if (target.scrollHeight - target.scrollTop - target.clientHeight > 100 || fileListLoading.value || !hasMoreData.value) {
    return
  }
  // get next page data
  onParamsPageChange(paramsPageIndex.value + 1)
}

watch(
  () => props.displayMode,
  () => {
    if (props.displayMode === 'card') {
      checkCardContent()
    }
  },
  { flush: 'post' }
)

/**
 * card模式高度不够时需要加载多次
 */
async function checkCardContent() {
  if (props.displayMode !== 'card') {
    return
  }
  const cardContentEl = document.querySelector('.page-list-card')
  if (hasMoreData.value && cardContentEl) {
    // const scrollTop = cardScrollEl.scrollTop
    const wrapperHeight = cardScrollEl.clientHeight
    const contentHeight = cardContentEl?.clientHeight

    if (wrapperHeight >= contentHeight) {
      await onParamsPageChange(paramsPageIndex.value + 1)
      checkCardContent()
    }
  }
}

defineExpose({
  onCreateFolder,
  onRefreshList
})

onMounted(() => {
  onRefreshList()
  setHeight()
  window.onresize = () => {
    setHeight()
  }
  initScroll()
})

provide('openDeleteConfirm', openDeleteConfirm)
provide('onMoveFiles', moveFiles)
</script>

<style lang="less" scoped>
@import '@/styles/scroll-bar';
.data-page-list {
  overflow: auto;
}
.batch-btns {
  z-index: 100;
  visibility: hidden;
  position: fixed;
  left: 50%;
  transform: translateX(-50%);
  bottom: 40px;
  width: 248px;
  height: 44px;
  background: #ffffff;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
  border-radius: 4px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(82px, 1fr));
  column-gap: 1px;

  .btn {
    border-right: 1px solid #f2f2f2;
    vertical-align: bottom;
    line-height: 32px;
    margin: 6px 0;
    color: #6973ff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    :deep(.iconfont) {
      margin-right: 2px;
      position: relative;
      top: 2px;
    }
  }

  &.active {
    visibility: visible;
  }
}
</style>
