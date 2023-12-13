<template>
  <div id="projects" class="page-list-card">
    <el-checkbox :disabled="dataItems.length === 0" class="check-all" v-model="localIsCheckAll" :indeterminate="indeterminate" @change="onCheckAllChange">全选</el-checkbox>
    <!-- 所有项目 卡片  -->
    <div v-if="fileList.length > 0" class="projects projects-in-card">
      <template v-for="(data, index) in fileList" :key="data.path">
        <div class="data-item" :class="{ active: dropdownMenuVisibleIndex === index, checked: checkedKeys.includes(data.path), edit: isCurFileCreateOrRename(data.path) }">
          <div class="top-btns">
            <div class="check-box" @click="checkBoxChange(data)" :class="{ checked: checkedKeys.includes(data.path) }">
              <el-icon><Check /></el-icon>
            </div>
            <el-dropdown @visible-change="cardDropMenuVisibleChange(index, $event)">
              <i class="iconfont icon-daohang-gengduocaozuobeifen more-menu" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="downloadFile(data)">下载</el-dropdown-item>
                  <el-dropdown-item disabled>导入到项目</el-dropdown-item>
                  <!-- <el-dropdown-item @click="editFolderFunc('edit', index)">重命名</el-dropdown-item> -->
                  <el-dropdown-item @click="onRename(data)">重命名</el-dropdown-item>
                  <el-dropdown-item @click="moveFiles(data)">移动</el-dropdown-item>
                  <el-dropdown-item @click="openDeleteConfirm(data)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="check-item" align="center">
            <div class="icon">
              <!-- <i v-if="data.directory" class="iconfont icon-a-wenjianjiashouqibeifen2 folder-color" /> -->
              <icon-font :type="getFileTypeOrIcon(data.directory ? 'directory' : data.name, true)" />
            </div>
            <div v-if="isCurFileCreateOrRename(data.path)" class="card-input-div">
              <el-input v-model="fileNameString" :id="`nameInputCard${index}`" oninput="value=value.replace(/[, ]/g,'')" onkeyup="value=value.replace(/[, ]/g,'')" size="small">
              </el-input>
              <div class="input-btns">
                <el-button @click.prevent="createOrRenameCancel(data)" size="small">取消</el-button>
                <el-button @click.prevent="createOrRenameConfirm(data)" size="small" type="primary" :loading="createOrRenameLoading">确认</el-button>
              </div>
            </div>
            <router-link v-show="!isCurFileCreateOrRename(data.path)" :to="getRouterLink(data)">
              <div class="normal-div">
                <el-tooltip effect="dark" :content="data.name" placement="bottom-start" :show-after="500" :show-arrow="false">
                  <span>{{ data.name }}</span>
                </el-tooltip>
                <el-tooltip effect="dark" :content="formatDate(data.updateTimestamp)" placement="bottom-start" :show-after="500" :show-arrow="false">
                  <span class="time-span">{{ formatDate(data.updateTimestamp) }}</span>
                </el-tooltip>
              </div>
            </router-link>
          </div>
        </div>
      </template>
    </div>
    <div v-else class="div-empty">
      <img src="@/assets/empty.png" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
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
const route = useRoute()

const openDeleteConfirm: any = inject('openDeleteConfirm')
const onMoveFiles: any = inject('onMoveFiles')
const setPreview: any = inject('setPreview')

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

const dropdownMenuVisibleIndex = ref(-1) // dropdown显示时所在的index

// 卡片-更多按钮显示
const cardDropMenuVisibleChange = (value: any, index: any) => {
  dropdownMenuVisibleIndex.value = value ? index : -1
}

const localIsCheckAll = ref(props.isCheckAll) // used for checkbox v-model

watch(
  () => props.isCheckAll,
  () => {
    localIsCheckAll.value = props.isCheckAll
  }
)

// 全选checkbox的中间态
const indeterminate = computed(() => {
  return props.isCheckAll === false && props.checkedKeys.length > 0
})

// 全选按钮变化
const onCheckAllChange = (val: boolean) => {
  emits('onCheckAllChange', val)
}

function checkBoxChange(data: IDataItem) {
  const checked = props.checkedKeys.includes(data.path)
  let copyed = [...props.checkedKeys]
  if (checked) {
    copyed = copyed.filter((key) => key !== data.path)
  } else {
    copyed.push(data.path)
  }
  emits('onCheckItemChange', copyed)
  setPreview(props.dataItems.filter((item) => copyed.includes(item.path)))
}

// 下载
const downloadFile = async (fileData: IDataItem) => {
  download(fileData.path)
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

/**
 * handle when cancel create folder or rename
 * @param fileData target
 */
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
  const inputEl: HTMLInputElement | null = document.querySelector('.page-list-card .card-input-div input')
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
  const inputEl: HTMLInputElement | null = document.querySelector('.page-list-card .card-input-div input')
  if (inputEl) {
    inputEl.focus()
  }
}

function moveFiles(fileData: IDataItem) {
  createOrRenameCancel({} as any) // reset create or rename
  onMoveFiles(fileData)
}

// 拼接路由地址
const getRouterLink = (fileData: IDataItem) => {
  if (isCurFileCreateOrRename(fileData.path)) {
    // 卡片在编辑态时不可跳转
    return ''
  }
  return getFileJumpUrl(fileData)
}

defineExpose({
  initCreateFolder
})
</script>

<style lang="less" scoped>
@import '@/constants';
.projects-in-card {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(@DATA_CARD_WIDTH, 1fr));
  column-gap: 32px;
  row-gap: 32px;
  overflow: auto;

  .data-item {
    padding: 8px;
    color: #5d637e;
    border-radius: 4px;
    width: calc(@DATA_CARD_WIDTH - 16px);
    height: calc(@DATA_CARD_HEIGHT - 16px);
    box-sizing: content-box;

    .top-btns {
      display: flex;
      justify-content: space-between;
      width: 100%;
      visibility: hidden;

      .check-box {
        width: 14px;
        height: 14px;
        background: #ffffff;
        border-radius: 2px;
        border: 1px solid #e9e9e9;
        cursor: pointer;

        i {
          color: #fff;
          font-size: 14px;
        }

        &:hover {
          border: 1px solid #6973ff;
        }

        &.checked {
          background: #6973ff;
        }
      }
      .check-btn {
        height: 14px;
      }

      .more-menu {
        cursor: pointer;
        outline: none;
        &:hover {
          color: #6973ff;
        }
      }

      .confirm-btns {
        background: #fff;
        padding: 3px;
        border-radius: 6px;
        width: 43px;
        display: flex;
        justify-content: space-between;
        visibility: hidden;

        .icon {
          cursor: pointer;
          font-size: 17px;
          color: #6973ff;
        }
      }
    }

    span {
      color: #373b52;
      width: calc(@DATA_CARD_WIDTH - 16px);
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      word-break: break-all;
      -webkit-line-clamp: 1;
      line-clamp: 1;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      line-height: 20px;
      letter-spacing: 1px;
    }

    .check-item {
      // padding: 32px 16px 16px 8px;
      a {
        color: #5d637e;
      }

      .icon {
        line-height: 44px;

        i {
          font-size: 44px;
        }
      }

      .normal-div {
        .time-span {
          margin-top: 5px;
          color: #5d637e;
          line-height: 22px;
          width: calc(@DATA_CARD_WIDTH - 16px);
          text-align: center;
          overflow: hidden;
          text-overflow: ellipsis;
          word-break: break-all;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          display: -webkit-box;
          -webkit-box-orient: vertical;
          line-height: 17px;
          letter-spacing: 1px;
        }
      }
      .card-input-div {
        // display: flex;
        // align-items: center;

        .el-input {
          // margin-right: 12px;
          width: 100%;
        }
        .input-btns {
          margin-top: 12px;
          display: flex;
          justify-content: space-between;

          .el-button {
            padding: 8px 5px;
          }
        }
      }
    }

    &:hover {
      background-color: rgba(103, 113, 252, 0.1);

      .top-btns {
        visibility: visible;
      }
    }

    &.active {
      background-color: rgba(103, 113, 252, 0.1);

      .top-btns {
        visibility: visible;
      }
    }
    &.checked {
      background-color: rgba(103, 113, 252, 0.1);

      .top-btns {
        .check-btn {
          visibility: visible;
        }

        .check-box {
          visibility: visible;
        }
      }
    }
    &.edit {
      background-color: rgba(103, 113, 252, 0.1);

      .top-btns {
        visibility: hidden !important;
      }
    }
  }
}
.div-empty {
  margin-top: 36px;
  width: 100%;
  text-align: center;
}
</style>
