<template>
  <div class="data-detail">
    <el-container>
      <el-container>
        <el-header class="header main-header">
          <div class="left">
            <div class="back-area" @click="back">
              <i class="iconfont icon-jiantou back"></i>
              <el-button type="" link class="back">返回</el-button>
            </div>
            <el-divider direction="vertical" class="divider" />
            <strong class="title">{{ meta.name ? meta.name : file_name }}</strong>
          </div>
          <div class="right" v-if="!fromGraph">
            <el-button type="" link @click="pre" :disabled="!canPre" :class="!canPre ? 'pre-disabled' : ''">
              <i class="iconfont icon-jiantou" :class="!canPre ? 'pre-disabled' : ''"></i>上一个
            </el-button>
            <el-button type="" link @click="next" :disabled="!canNext" :class="!canNext ? 'next-disabled' : ''">
              下一个<i class="iconfont icon-jiantou next" :class="!canNext ? 'next-disabled' : ''"></i>
            </el-button>
          </div>
        </el-header>
        <el-main class="content">
          <div v-if="type === 'csv'" class="csv-detail">
            <CsvFileTemplate :path="current_routes" />
          </div>
          <div v-else-if="type === 'image'">
            <ImgFileTemplate :path="current_routes" />
          </div>
          <div v-else-if="type === 'video'" class="video-detail">
            <VideoFileTemplate :path="current_routes" />
          </div>
          <div v-else-if="type === 'txt'" class="txt-detail">
            <TxtFileTemplate :path="current_routes" />
          </div>
          <div v-else-if="type === 'virtual'" class="virtual-detail">
            <VirtualFileTemplate :path="current_routes" />
          </div>
          <div v-else class="unknown-detail">
            <UnknownFileTemplate />
          </div>
        </el-main>
      </el-container>
      <el-aside width="311px">
        <el-header class="header aside-header">
          <el-button type="" link @click="fileDownload(current_routes)"><icon-font type="icon-xiazaibendi" />下载</el-button>
          <el-button v-if="!fromGraph" type="" link @click="moveFiles(meta.path)"><icon-font type="icon-tufenxi-huidaohuaban" />移动</el-button>
          <el-button v-if="!fromGraph" type="" link @click="doDelete"><icon-font type="icon-shanchu" />删除</el-button>
        </el-header>
        <el-main>
          <div class="base-info">
            <p class="title-edit">
              <strong class="title" v-if="!isEdit"> {{ `${fileName}.${meta.type}` }} </strong>
              <icon-font type="icon-bianji" v-if="!fromGraph && !isEdit && meta.name" @click="edit" class="edit-iconfont" />
              <el-input
                v-model="fileName"
                v-show="isEdit"
                id="detail-title-input"
                size="small"
                class="title-input"
                oninput="value=value.replace(/[, ]/g,'')"
                @keyup.enter="checkFileName"
              />
              <el-button v-if="isEdit" size="small" color="#6973FF" @click.prevent="checkFileName" :loading="renameLoading">确认</el-button>
              <el-button v-if="isEdit" size="small" @click="cancel">取消</el-button>
            </p>
            <p class="label">文件大小：{{ meta && sizeConversion(meta.size) }}</p>
            <p class="label">创建时间：{{ meta && meta.createTime }}</p>
            <p class="label">最新修改时间：{{ meta && meta.updateTime }}</p>
            <p class="label">文件格式：{{ meta && meta.fileType === 'NOT_RECOGNITION' ? '无法识别的格式' : meta.type }}</p>
          </div>
        </el-main>
      </el-aside>
    </el-container>
    <MoveFilesModal :visible="moveDialogVisible" @operate="operateFunc" :loading="moveLoading" :move-index="moveIndex" />
  </div>
</template>
<script setup lang="ts">
import { onMounted, computed, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import MoveFilesModal from '@/components/data/MoveFilesModal.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import bus from '@/EventBus/eventbus.js'
import useMeta from '@/components/hooks/useMeta.ts'
import useFolderPreview from '@/components/hooks/useFolderPreview.ts'
import { fileBatchExist } from '@/api/data'
import VideoFileTemplate from '@/components/common/VideoFileTemplate.vue'
import { useDetailStore } from '@/stores/detail'
import VirtualFileTemplate from '@/components/common/VirtualFileTemplate.vue'
import UnknownFileTemplate from '@/components/common/UnknownFileTemplate.vue'
import CsvFileTemplate from '@/components/common/CsvFileTemplate.vue'
import ImgFileTemplate from '@/components/common/ImgFileTemplate.vue'
import TxtFileTemplate from '@/components/common/TxtFileTemplate.vue'
import { sizeConversion, getFileName, compatible, checkName, getRecordType } from '@/util/util'
import useFile from '@/components/hooks/useFile'

const route = useRoute()
const router = useRouter()
const detailStore = useDetailStore()
const fileName: any = ref('')
// const fileName: any = ref('csv文件')
const isEdit: any = ref(false)
// const tableWidth = document.body.clientWidth - 311 - 50
// const tableHeight = document.body.clientHeight - 64 - 60 - 40 - 50
const type: any = ref('')

// 移动弹窗相关
const moveDialogVisible: any = ref(false)
let moveIndex: any = null

const { meta, getMeta, reset } = useMeta()
const { folder_page_number, folderContent, queryFolder, updateFolderContentItem } = useFolderPreview()

const { fileDownload, fileDelete, fileBatchExistCheck, fileMove, moveLoading, fileRename, renameLoading } = useFile()

const current_routes: any = computed(() => {
  const path = route?.path.indexOf('path') > -1 ? route.path.split('=')[1] : ''
  return decodeURIComponent(path)
})

/** 是否从画布跳转进入的详情页 */
const fromGraph: any = computed(() => {
  return route?.query?.from === 'graph'
})

const routes_Parent: any = computed(() => {
  const list = current_routes.value.split('/')
  const pathList = list.slice(0, list.length - 1)
  return pathList.join('%2F')
})

const file_name: any = computed(() => {
  return current_routes.value?.split('/').pop()
})

const keyword: any = computed(() => {
  return route.query.search
})

const searchURI: any = computed(() => {
  return `${keyword.value ? `?search=${keyword.value}` : ''}`
})

const current_index: any = ref(-1)
const canPre: any = ref(false)
const canNext: any = ref(false)

const current_routes_watch = watch(current_routes, (val: any) => {
  if (val && val.includes('.')) {
    reset()
    type.value = getRecordType({
      directory: false,
      name: current_routes.value
    })
    fileName.value = ''
    getMeta(val, () => {
      fileName.value = getFileName(meta.value.name)
    })
    // 文件过多的时候，需要动态查询文件列表(用于上一个、下一个)
    if (folderContent.value.total > folderContent.value.list.length) {
      folder_page_number.value += 1
      queryFolder(decodeURIComponent(routes_Parent.value), keyword.value, () => {
        setIndex()
      })
    } else {
      setIndex()
    }
  }
})

/**
 * 路由跳转注销掉watch，不然切换路由watch方法还是会执行
 */
onBeforeRouteLeave(() => {
  current_routes_watch()
})

onMounted(() => {
  bus.emit('displayNavBar', false)
  type.value = getRecordType({
    directory: false,
    name: current_routes.value
  })
  fileName.value = ''
  getMeta(current_routes.value, () => {
    fileName.value = getFileName(meta.value.name)
  })

  queryFolder(decodeURIComponent(routes_Parent.value), keyword.value, () => {
    setIndex()
  })
})

const setIndex = () => {
  const path = compatible(current_routes.value)
  const index = folderContent.value.list?.findIndex((item: any) => item.path === path)
  current_index.value = index
  // 这里的计算需要考虑前后是不是文件夹的情况
  canPre.value = current_index.value > 0 && CanPreOrNot()
  canNext.value = current_index.value < folderContent.value.list?.length - 1 && canNextOrNot()
}

const CanPreOrNot = () => {
  let i = current_index.value
  const list = folderContent.value.list
  while (i > 0) {
    if (!list[i - 1]?.directory) {
      return true
    } else {
      i--
    }
  }
  return false
}

const canNextOrNot = () => {
  let i = current_index.value
  const list = folderContent.value.list
  while (i < list.length - 1) {
    if (!list[i + 1]?.directory) {
      return true
    } else {
      i++
    }
  }
  return false
}

const findPreIndex = (initIndex: number) => {
  let i = initIndex
  while (i >= 0) {
    const folder = folderContent.value.list?.[i]
    // 文件夹不需要预览，直接跳过
    if (folder.directory) {
      i--
    } else {
      return i
    }
  }
  return -1
}
const findNextIndex = (initIndex: number) => {
  let i = initIndex
  while (i <= folderContent.value.list.length - 1) {
    const folder = folderContent.value.list?.[i]
    // 文件夹不需要预览，直接跳过
    if (folder.directory) {
      i++
    } else {
      return i
    }
  }
  return -1
}

const pre = () => {
  isEdit.value = false
  const i = findPreIndex(current_index.value - 1)
  if (i === -1) {
    canPre.value = false
  } else {
    const folder = folderContent.value.list[i]
    const path = encodeURIComponent(folder.path)
    router.replace(`/management/detail/path=${path}${searchURI.value}`)
    current_index.value = i
  }
}

const next = () => {
  isEdit.value = false
  const i = findNextIndex(current_index.value + 1)
  console.log('next-i:', i)
  if (i === -1) {
    canNext.value = false
  } else {
    const folder = folderContent.value.list[i]
    const path = encodeURIComponent(folder.path)
    router.replace(`/management/detail/path=${path}${searchURI.value}`)
    current_index.value = i
  }
}

const checkFileName = () => {
  const _rename = `${fileName.value}.${meta.value.type}`
  if (!checkName(fileName.value, getFileName(meta.value.name))) {
    return
  }

  console.log('meta:', meta)

  const pathList = meta.value.path.split('/')
  pathList.splice(pathList.length - 1, 1, _rename)
  console.log('pathList', pathList.join('/'))
  const path = pathList.join('/')
  checkExistBeforeRename(_rename, path)
}

// 重命名时，批量检测是否有重名
const checkExistBeforeRename = async (_rename: string, movePath: any) => {
  const response = await fileBatchExist({ data: { pathList: [movePath].join(',') } })
  const { data, code } = response.data
  if (code === 200) {
    if (data[movePath]) {
      ElMessageBox.confirm('请换个名字重试', `已存在${_rename}文件`, {
        confirmButtonText: '知道了',
        showCancelButton: false,
        type: 'warning',
        autofocus: false
      }).then(() => {
        // isEdit.value = false
        // fileName.value = getFileName(meta.value.name)
      })
    } else {
      confirm()
    }
  }
}

/**
 * 修改文件名
 */
const confirm = async () => {
  renameLoading.value = true
  const list = meta.value.path.split('/')
  const targetPath = `${list.slice(0, list.length - 1).join('/')}/${fileName.value}.${meta.value.type.toLowerCase()}`

  fileRename(meta.value.path, targetPath, () => {
    isEdit.value = false
    renameLoading.value = false
    getMeta(targetPath, () => {
      fileName.value = getFileName(meta.value.name)
      const path = encodeURIComponent(meta.value.path)
      router.replace(`/management/detail/path=${path}`)
    })
    // 更新文件列表中的信息
    updateFolderContentItem(current_index.value, {
      path: targetPath,
      name: `${fileName.value}.${meta.value.type.toLowerCase()}`
    })
  })
}

const cancel = () => {
  fileName.value = getFileName(meta.value.name)
  isEdit.value = false
}

const doDelete = async () => {
  ElMessageBox.confirm('确定要删除所选中的文件吗？', '删除文件？', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    autofocus: false
  }).then(async () => {
    const path = decodeURIComponent(current_routes.value)
    fileDelete([path], () => {
      back()
    })
  })
}

// 弹窗事件响应统一处理
const operateFunc = (type: any, data?: any) => {
  switch (type) {
    case 'move':
      {
        checkExistBeforeMove(data)
      }
      break
    case 'close':
      {
        moveDialogVisible.value = false
      }
      break
    default:
      break
  }
}

// （批量）移动文件(夹)
const moveFiles = async (path: any | null) => {
  // 取消所有编辑态
  cancel()
  moveIndex = [path]
  moveDialogVisible.value = true
}

// 移动文件时，批量检测是否有重名
const checkExistBeforeMove = async (targetPath: any) => {
  const movePath = targetPath ? `${targetPath}/${meta.value.name}` : meta.value.name

  if (movePath === meta.value.path) {
    ElMessage.warning('移动前后的目标文件夹一样，请更换目标文件夹！')
    return
  }

  const response = await fileBatchExist({ data: { pathList: [movePath].join(',') } })
  const { data, code } = response.data
  if (code === 200) {
    if (data[movePath]) {
      ElMessageBox.confirm('存在同名文件，是否覆盖？', '是否覆盖', {
        confirmButtonText: '覆盖',
        cancelButtonText: '取消'
      }).then(() => {
        confirmMove(targetPath)
      })
    } else {
      confirmMove(targetPath)
    }
  }
}

// 确认移动
const confirmMove = async (movePath: any) => {
  fileMove(moveIndex, movePath, () => {
    moveDialogVisible.value = false
    // 移动成功，直接退出详情页，进入移动成功的文件夹页面
    const path = encodeURIComponent(movePath) ? `/management/data/path=${encodeURIComponent(movePath)}` : '/management/data'
    router.replace(path)
    bus.emit('displayNavBar', true)
  })
}

// const tableColumns: any = ref([])
// const tableData: any = ref([])

// /**
//  * 表格数据整形
//  */
// const shapeTable = () => {
//   const { columnVOList, valueList } = csvPreviewContent.value
//   tableColumns.value = columnVOList?.map((column: any) => {
//     const { name, type } = column
//     return {
//       key: name,
//       title: `${name}(${type})`,
//       dataKey: name,
//       width: `${100 / columnVOList.length}%`
//     }
//   })

//   const list: any = []
//   valueList?.content?.forEach((value: any) => {
//     const yy: any = {}
//     value.forEach((item: any, index: number) => {
//       const key = columnVOList[index]?.name
//       yy[key] = item
//     })
//     list.push(yy)
//   })
//   tableData.value = list
// }

const back = () => {
  // 直接返回列表页(刷新详情页之后，fromPath丢失，直接返回列表页)
  const path = detailStore.fromPath || '/management/data'
  router.replace(path)
  bus.emit('displayNavBar', true)
}

const edit = () => {
  isEdit.value = !isEdit.value
  const edit_element = document.querySelector('#detail-title-input') as HTMLElement
  nextTick(() => {
    edit_element.focus()
  })
}
</script>
<style lang="less" scoped>
.data-detail {
  background-color: #fff;
  height: calc(100% - 64px);
  .el-container {
    height: 100%;
  }
  .main-header {
    border-bottom: 1px solid #e9e9e9;
  }
  .el-aside {
    border-left: 1px solid #e9e9e9;
  }
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .back-area {
      display: flex;
      align-items: center;
      justify-content: center;
      &:hover {
        cursor: pointer;
      }
    }
    .back {
      color: #6973ff;
      font-size: 16px;
    }
    .divider {
      color: #999999;
    }
    .title {
      color: #5d637e;
    }
    .left,
    .right {
      display: flex;
      align-items: center;
    }
    .left {
      font-size: 16px;
    }
    .right {
      font-size: 14px;
      .pre-disabled,
      .next-disabled {
        color: #e9e9e9;
      }
    }
    .icon-jiantou,
    .el-button {
      color: #6973ff;
    }
    .next {
      transform: rotate(180deg);
    }
  }
  .content {
    color: #373b52;
    display: flex;
    justify-content: center;
  }
  .iconfont {
    margin-right: 9px;
  }
  .aside-header {
    justify-content: end;
  }
  .base-info {
    color: #5d637e;
    font-size: 14px;
  }
  .title-edit {
    display: flex;
    align-items: center;

    .title {
      margin-right: 9px;
      word-wrap: break-word;
      word-break: break-all;
    }
    .iconfont {
      display: flex;
      align-items: center;
      justify-content: center;
      &:hover {
        cursor: pointer;
      }
    }
    .title-input {
      margin-right: 12px;
    }
  }
  .csv-detail {
    color: #373b52;
    width: 100%;
    height: 100%;
  }

  .video-detail {
    width: 100%;
    height: 100%;
  }

  .txt_empty {
    color: #909399;
    font-size: 14px;
  }

  .virtual-detail {
    font-size: 14px;
  }
  .unknown-detail {
    width: 100%;
    height: 100%;
    color: #222432;
    font-size: 12px;
    display: flex;
    justify-content: center;
    flex-direction: column;
    align-items: center;
  }
  :deep(.el-table-v2__header-cell) {
    background-color: #fafafa;
  }
  :deep(.el-table-v2__header-cell-text) {
    color: #373b52;
  }
  :deep(.preview-csv-header-row > th) {
    background-color: #fafafa;
  }
  :deep(.el-image) {
    width: 100%;
    height: 100%;
  }
  :deep(.el-image__error) {
    height: auto;
  }
}
</style>
