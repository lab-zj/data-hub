<template>
  <div class="projects projects-in-list" style="display: flex; align-items: center">
    <el-table
      class="projects-table"
      ref="multipleTableRef"
      :height="tableHeight"
      :data="fileList"
      :pagination="false"
      @selection-change="onSelectChange"
      @row-click="rowClick"
      @row-dblclick="rowDbClick"
      rowKey="path"
    >
      <template #empty>
        <img src="@/assets/empty.png" />
      </template>
      <el-table-column :show-header="false" ref="check-box" type="selection" width="30" />
      <el-table-column label="文件名" min-width="50%">
        <template #default="scope">
          <div class="table-col" :class="{ active: dropMenuVisibleIndex === scope.$index }">
            <span class="name-col">
              <icon-font v-if="scope.row.directory" type="icon-a-wenjianjiashouqibeifen2" class="iconfont name-icon"></icon-font>
              <icon-font v-else :type="getFileTypeOrIcon(scope.row.name, true)" class="iconfont name-icon"></icon-font>
              <div v-if="isCurFileCreateOrRename(scope.row.path)" class="input-div">
                <el-input
                  v-model="fileNameString"
                  :id="`nameInput${scope.$index}`"
                  oninput="value=value.replace(/[, ]/g,'')"
                  onkeyup="value=value.replace(/[, ]/g,'')"
                  @dblclick.stop
                ></el-input>
                <el-button @click.stop="createOrRenameConfirm(scope.row)" size="small" type="primary" :loading="createOrRenameLoading">确认</el-button>
                <el-divider direction="vertical" />
                <el-button @click.stop="createOrRenameCancel(scope.row)" size="small">取消</el-button>
              </div>
              <el-tooltip effect="dark" :content="scope.row.name" placement="bottom-start" :show-after="500" :show-arrow="false">
                <a class="item-name" v-show="!isCurFileCreateOrRename(scope.row.path)" @click.stop="goPath(getFileJumpUrl(scope.row))">{{ scope.row.name }}</a>
                <!-- <router-link class="item-name" v-show="!scope.row.editType" :to="getRouterLink(scope.row)"><a>{{ scope.row.name }}</a></router-link> -->
              </el-tooltip>
            </span>
            <div @click.stop class="col-btns" :class="{ hide: isCurFileCreateOrRename(scope.row.path) }">
              <el-tooltip class="box-item" effect="dark" content="下载" placement="top" :show-after="500">
                <span @click.stop="downloadFile(scope.row)" class="btn-icon">
                  <icon-font type="icon-xiazaibendi" class="iconfont download" />
                </span>
              </el-tooltip>

              <el-tooltip class="box-item" effect="dark" content="导入到项目" placement="top" :show-after="500">
                <span class="btn-icon">
                  <icon-font type="icon-daorudaorenwu" />
                </span>
              </el-tooltip>

              <el-tooltip class="box-item" effect="dark" content="删除" placement="top" :show-after="500">
                <span @click.stop="openDeleteConfirm(scope.row)" class="btn-icon">
                  <icon-font type="icon-shanchu" />
                </span>
              </el-tooltip>

              <el-dropdown :show-timeout="500" @visible-change="dropMenuVisibleChange(scope.$index, $event)">
                <span class="btn-icon">
                  <icon-font type="icon-daohang-gengduocaozuobeifen" />
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="onRename(scope.row)">重命名</el-dropdown-item>
                    <el-dropdown-item @click="moveFiles(scope.row)">移动</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column v-if="route.query.search" label="所在位置" min-width="25%">
        <template #default="scope">
          <a
            @click.stop="
              goPath(`/management/data${scope.row.parentDirectory ? `/path=${encodeURIComponent(scope.row.parentDirectory.slice(0, scope.row.parentDirectory.length - 1))}` : ''}`)
            "
          >
            <u class="text-ellipsis">{{ scope.row.parentDirectory ? formatPath(scope.row.parentDirectory) : '全部数据' }}</u></a
          >
        </template>
      </el-table-column>
      <el-table-column prop="updateTimestamp" label="最新更新" min-width="25%" :formatter="(row: IDataItem) => formatDate(row.updateTimestamp)"></el-table-column>
      <el-table-column prop="size" label="大小" min-width="20%" :formatter="formatSize"></el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, inject, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { sizeConversion } from '@/util/util'
import { IDataItem, downloadFile as download, formatDate, getFileJumpUrl, getFileTypeOrIcon } from '../util'
import { useCreateFolderOrRename } from '../hooks/useCreateFolderOrRename'

interface Props {
  dataItems: Array<IDataItem>
  tableHeight: number
  isCheckAll: boolean
  checkedKeys: string[]
}
const props = withDefaults(defineProps<Props>(), {
  dataItems: () => [],
  tableHeight: 400,
  isCheckAll: false,
  checkedKeys: () => []
})

const emits = defineEmits(['onCheckAllChange', 'onCheckItemChange', 'onRefreshList'])

const openDeleteConfirm: any = inject('openDeleteConfirm')
const onMoveFiles: any = inject('onMoveFiles')
const setPreview: any = inject('setPreview')

const router = useRouter()
const route = useRoute()
const multipleTableRef: any = ref()
const fileList = computed(() => {
  return newFolderItem.value.concat(props.dataItems)
})
const basePath = computed(() => {
  return route.params.path || ''
})

const searchValue = computed(() => {
  return route.query.search || ''
})

watch([basePath, searchValue], () => {
  createOrRenameCancel({} as any) // reset create or rename
})

// 行单击点击事件
const rowClick = (record: IDataItem) => {
  if (isCurFileCreateOrRename(record.path)) {
    // 创建新文件夹取消时
    return
  }
  multipleTableRef.value!.toggleRowSelection(record, undefined)
}

// 行双击事件
const rowDbClick = (record: IDataItem) => {
  createOrRenameCancel({} as any) // reset create or rename
  router.push(getFileJumpUrl(record))
  setPreview([])
}

// dropmenu未关闭前行或缩略图保持hover状态
const dropMenuVisibleIndex: any = ref(-1)
const dropMenuVisibleChange = (index: any, visible: boolean) => {
  if (visible) {
    dropMenuVisibleIndex.value = index
  } else {
    dropMenuVisibleIndex.value = -1
  }
}

// 多选发生变化
const onSelectChange = (items: IDataItem[]) => {
  emits(
    'onCheckItemChange',
    items.map((item: any) => item.path)
  )

  setPreview(items)
}

const {
  loading: createOrRenameLoading,
  fileNameString,
  onRenameFileName,
  newFolderItem,
  isCurFileCreateOrRename,
  onCreateFolder,
  confirmCreateOrRename
} = useCreateFolderOrRename()

function createOrRenameCancel(fileData: IDataItem) {
  onRenameFileName(fileData, 'cancel')
  onCreateFolder('', 'cancel')
}

/**
 * confirm create folder or rename
 */
async function createOrRenameConfirm(fileData: IDataItem) {
  confirmCreateOrRename(fileData, () => {
    emits('onRefreshList')
  })
}

/**
 * handle click rename btn
 * @param fileData
 */
async function onRename(fileData: IDataItem) {
  onRenameFileName(fileData)
  await nextTick()
  const inputEl: HTMLInputElement | null = document.querySelector('.projects-table .input-div input')
  if (inputEl) {
    inputEl.focus()
  }
}

/**
 * handle when click new create btn
 */
async function initCreateFolder() {
  onCreateFolder(basePath.value as string)

  await nextTick()
  const inputEl: HTMLInputElement | null = document.querySelector('.projects-table .input-div input')
  if (inputEl) {
    inputEl.focus()
  }
}

// 下载
const downloadFile = async (fileData: IDataItem) => {
  download(fileData.path)
}

function moveFiles(fileData: IDataItem) {
  createOrRenameCancel({} as any) // reset create or rename
  onMoveFiles(fileData)
}

// 跳转到指定路径
const goPath = (path: string) => {
  // searchInput.value = ''
  router.push(path)
}

// 格式化文件大小
const formatSize = (record: IDataItem) => {
  return record.directory ? '-' : sizeConversion(record.size)
}

// 格式化所在位置
const formatPath = (parentDirectory: any) => {
  return parentDirectory ? parentDirectory.slice(0, parentDirectory.length - 1) : '全部数据'
}

// 勾选的row
const checkedRows = computed(() => {
  return props.dataItems.filter((dataItem) => props.checkedKeys.includes(dataItem.path))
})

watch(
  () => props.checkedKeys,
  (newValue, oldvalue) => {
    // 同步table和card的选中状态
    const currentTableSelectionRowLength = multipleTableRef!.value.getSelectionRows().length
    // !== currentTableSelectionRowLength 证明不是从table内部触发
    if (newValue.length !== oldvalue.length && newValue.length !== currentTableSelectionRowLength) {
      toggleTableSelection()
    }
  }
)

function toggleTableSelection() {
  if (props.isCheckAll) {
    // 全选状态下
    multipleTableRef!.value.toggleAllSelection()
    return
  }
  if (checkedRows.value.length > 0) {
    multipleTableRef!.value.clearSelection()
    checkedRows.value.forEach((row) => {
      multipleTableRef!.value.toggleRowSelection(row, true)
    })
    return
  }
  multipleTableRef!.value.clearSelection()
}

onMounted(() => {
  toggleTableSelection()
})

defineExpose({
  initCreateFolder
})
</script>

<style lang="less" scoped>
.projects-table {
  width: 100%;

  :deep(.el-table__row) {
    height: 45px;
    .cell {
      .el-checkbox {
        display: none;
      }
      .is-checked {
        display: inline-block !important;
      }
    }
  }

  :deep(.el-table__row:hover) {
    .cell {
      .el-checkbox {
        display: inline-block !important;
      }
    }
  }

  :deep(.el-table__inner-wrapper::before) {
    height: 0;
  }

  :deep(.el-table__cell) {
    height: 44px;
  }

  .table-col {
    display: flex;
    justify-content: space-between;

    .input-div {
      display: flex;
      align-items: center;

      .el-input {
        margin-right: 12px;
        height: 24px;
      }
    }

    .col-btns {
      display: flex;
      align-items: center;
      visibility: hidden;

      .btn-icon {
        outline: none;
        font-size: 17px;
        color: #5d637e;
        margin-right: 16px;
        cursor: pointer;

        &:hover {
          color: #6771fc;
        }
      }
    }

    &.active,
    &:hover {
      .col-btns {
        visibility: visible;
      }

      .col-btns.hide {
        visibility: hidden;
      }
    }
  }

  .name-col {
    display: flex;
    align-items: center;
    max-width: calc(100% - 160px);
    // max-width:200px;

    .item-name {
      display: block;
      color: #373b52;
      // width: calc(100% - 25px);
      // width: 200px;
      height: 23px;
      text-overflow: ellipsis;
      white-space: nowrap;
      overflow: hidden;
    }

    .name-icon {
      margin-right: 8px;
      font-size: 20px;
      display: flex;
      align-items: center;
    }
  }
}
</style>
