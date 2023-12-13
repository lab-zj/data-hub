<template>
  <div class="file-tree">
    <div class="file-tree-search">
      <el-input v-model="searchKeyWord" size="small" placeholder="输入文件关键字查询" oninput="value=value.replace(/[, ]/g,'')" @keyup.enter="onSearch">
        <template #suffix>
          <el-icon @click="onSearch"><Search /></el-icon>
        </template>
      </el-input>
    </div>
    <div class="file-tree-container four-scroll-y">
      <el-tree
        :props="defaultProps"
        :load="loadNode"
        :data="treeData"
        node-key="path"
        :current-node-key="currentNode && currentNode.path"
        :highlight-current="true"
        lazy
        ref="refFileTree"
        icon="ArrowRightBold"
        class="file-tree"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <template v-if="data.isGetMore !== true">
            <icon-font :type="getFileIcon(data.name, data.directory)" class="file-icon" />
            <icon-font v-if="isUsed(data.path)" type="icon-yishiyong" class="used-icon" :style="getLeftPosition(node.level)" />
            <span class="ellipsis tree-label" :style="getMaxWidth(node.level)" :title="data.name" draggable="true" @dragstart="onStartDrag(data)">
              {{ data.name }}
            </span>
            <el-dropdown v-if="props.hasOperate" class="file-tree-node-operate" @command="(command: any) => handleCommand(command, data)">
              <icon-font type="icon-daohang-gengduocaozuobeifen" class="more" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in getMenuList(data)" :key="item.key" :command="item.key">
                    {{ item.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <span v-else class="btn-get-more">加载更多 <icon-font type="icon-a-jiantoubeifen2" /></span>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getFileIconFont, getFileType } from '@/util/util'
import type Node from 'element-plus/es/components/tree/src/model/node'
import useFolderPreview from '@/components/hooks/useFolderPreview.ts'
import { debounce } from 'lodash'
import { NODE_TYPES } from '@/constants/dataGraph'

interface Tree {
  name: string
  path: string
  directory: boolean
  leaf?: boolean
  isGetMore?: boolean
  isSearch?: boolean
}

const props = defineProps({
  // 是否展示节点操作
  hasOperate: {
    type: Boolean,
    default: true
  },
  operates: {
    type: Array,
    default: () => []
  },
  extraParams: {
    type: Array,
    default: () => {}
  }
})

const emit = defineEmits(['close', 'startDrag', 'preview', 'operate', 'confirm'])
const { queryListWithPage, querySearchWithPage } = useFolderPreview()

const refFileTree = ref(null)

const defaultProps = {
  label: 'name',
  // children: 'zones',
  isLeaf: 'leaf'
}

let currentNode: any = ref(null)

async function getFileData(parentPath = '', curPage = 1) {
  const { content, totalElements, totalPages } = await queryListWithPage(parentPath, curPage)
  const list = content
  list.forEach((l: any) => {
    l.leaf = !l.directory
  })

  const result = {
    totalElements,
    totalPages,
    content: list
  }

  return parseGetMore(result, { page: curPage, path: parentPath })
}

/**
 * 懒加载节点
 * @param node 懒加载节点
 * @param resolve
 */
const loadNode = async (node: Node, resolve: (data: Tree[]) => void) => {
  console.log('tree node', node)
  if (node.level === 0) {
    // 初始时的根节点
    const _treeData = await getFileData('', 1) //   parseGetMore(getFileData(), {page: 1, path: '/'})
    const treeData = filterTreeData(_treeData.content)
    return resolve(treeData)
  }

  setTimeout(async () => {
    const _treeData = await getFileData(node.data.path, 1) // parseGetMore(getFileData(node.data.path), {page: 1, path: node.data.path})
    const treeData = filterTreeData(_treeData.content)
    resolve(treeData)
  }, 500)
}

/**
 * 判断是否超出分页，是：添加加载更多按钮
 * @param fetchResult 当前获取的分页结果
 * @param pageConfig 当前获取的分页配置
 * @param searchConfig 搜索配置
 */
function parseGetMore(fetchResult: { totalElements: number; totalPages: number; content: Tree[] }, pageConfig: { page: number; path: string }, searchConfig?: { isSearch: true }) {
  if (pageConfig.page < fetchResult.totalPages) {
    // add get more node
    fetchResult.content.push({
      name: '加载更多',
      path: `getMore-${pageConfig.path}-${pageConfig.page}`, // 自定义path，带上当前path路径和page参数
      isGetMore: true,
      directory: false,
      leaf: true,
      isSearch: searchConfig?.isSearch
    })
  }

  return fetchResult
}

/**
 * 获取文件对应的图标
 */
function getFileIcon(fileName: string, isDir: boolean = false) {
  let suffix = ''
  if (isDir) {
    suffix = '/'
  } else {
    suffix = fileName.slice(fileName.lastIndexOf('.') + 1)
  }

  return getFileIconFont(suffix)
}

function onStartDrag(fileData: Tree) {
  console.log('filetree-onStartDrag:', fileData)
  emit('startDrag', {
    name: fileData.name,
    type: NODE_TYPES.DATA,
    dataset: {
      name: fileData.name,
      path: fileData.path,
      directory: fileData.directory,
      type: getFileType(fileData.directory, fileData.name)
    }
  })
}

/**
 * 节点点击事件
 * @param nodeData 节点的data数据
 * @param node 节点node
 */
function handleNodeClick(nodeData: Node['data'], node: Node) {
  console.log('node click', nodeData, node)
  if (nodeData.isGetMore === true) {
    // 点击的加载更多按钮
    const [, path, page] = nodeData.path.split('-')
    loadMoreNodes({
      path,
      page: Number(page) + 1,
      isSearch: nodeData.isSearch
    })
    refFileTree.value.remove(node) // 移除当前的加载更多按钮
  } else {
    // preview file or folder
    emit('preview', nodeData)
    emit('confirm', nodeData)
    currentNode.value = nodeData
  }
}

/**
 * 分页加载更多数据
 * @param param0 {path: string, page: number} path:要加载的路径，page:加载的页数
 */
async function loadMoreNodes({ path, page, isSearch }: { path: string; page: number; isSearch: boolean }) {
  const { content } = isSearch ? await getFileDataBySearch(page) : await getFileData(path, page)
  const treeData = filterTreeData(content)
  if (refFileTree.value && treeData.length > 0) {
    treeData.forEach((data) => {
      refFileTree.value.append(data, path)
    })
  }
}

/** 搜索相关 */
const searchKeyWord: any = ref('')
const treeData: any = ref([])

const onSearch = debounce(async () => {
  searchFile()
}, 300)

async function searchFile() {
  if (searchKeyWord.value.trim()) {
    const { content } = await getFileDataBySearch(1)
    treeData.value = filterTreeData(content)
  } else {
    const { content } = await getFileData('', 1)
    treeData.value = filterTreeData(content)
  }
}

async function getFileDataBySearch(curPage: number) {
  const { totalElements, totalPages, content } = await querySearchWithPage(searchKeyWord.value, curPage)
  const list = content
  list.forEach((l: any) => {
    l.leaf = !l.directory
  })

  const result = {
    totalElements,
    totalPages,
    content: list
  }

  return parseGetMore(result, { page: curPage, path: '' }, { isSearch: true })
}

function getMaxWidth(level: number) {
  return props.hasOperate
    ? {
        maxWidth: `${120 - (level - 1) * 18}px`
      }
    : {}
}

function handleCommand(command: string, data: any) {
  console.log('command', command, data)
  emit('operate', {
    command,
    data
  })
}

function getMenuList(_data: any) {
  return props.operates.filter((operate: any) => operate.permission.includes(_data.path) || operate.permission.includes('all'))
}

function isUsed(path: string) {
  return props.extraParams?.usedData?.includes(path)
}

function isUsedIncludesDirectory(path: string) {
  return (
    props.extraParams?.usedData?.includes(path) ||
    (path.includes('/') &&
      // 使用了某个文件夹下面的某个文件，该文件夹也应该展示出来
      props.extraParams?.usedData?.some((item: string) => item.includes(path))) ||
    // 点击被使用的文件夹，该文件夹下面的所有文件也应该展示
    props.extraParams?.usedData?.some((item: string) => path.startsWith(item))
  )
}

function getLeftPosition(level: number) {
  return {
    left: `${24 + (level - 1) * 18}px`
  }
}
/**
 * 数据过滤器
 * 如果勾选了只展示已使用的文件，则需要进行过滤
 * @param list
 */
function filterTreeData(list: any) {
  if (props.extraParams?.usedOnly) {
    return list?.filter((item: any) => isUsedIncludesDirectory(item.path))
  }
  return list
}

watch(
  () => props.extraParams,
  async () => {
    searchFile()
  },
  { deep: true }
)
</script>

<style lang="less" scoped>
@import '@/styles/scroll-bar';

.file-tree {
  height: 100%;
}
.file-tree-search {
  display: flex;
  justify-content: center;
  margin: 8px auto;
  :deep(.el-input) {
    width: 100%;
  }
  :deep(.el-input__wrapper) {
    border-radius: 16px;
  }
}
.file-tree-container {
  height: calc(100% - 44px);
  overflow: auto;
  padding-bottom: 8px;
  .file-tree {
    width: fit-content;
    min-width: 100%;
  }

  .file-icon {
    margin-right: 4px;
    margin-top: 4px;
    font-size: 18px;
  }
  :deep(.el-tree-node__content) {
    height: 30px;
    width: 100%;
    position: relative;
    &:hover {
      .more {
        display: flex;
      }
    }
  }

  .more {
    display: none;
    color: #6771fc;
  }

  .file-tree-node-operate {
    position: absolute;
    right: 12px;
  }

  :deep(:focus-visible) {
    outline: none;
  }

  .btn-get-more {
    background-color: #eee;
    padding: 0 8px;
    cursor: pointer;

    .iconfont {
      transform: rotate(90deg);
    }
  }

  .used-icon {
    position: absolute;
    bottom: 0;
    left: 24px;
    font-size: 18px;
  }
  .ellipsis {
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
  }
}
:deep(.el-icon) {
  cursor: pointer;
}
</style>
