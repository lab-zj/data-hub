<template>
  <div class="project-list-page" v-loading="loading">
    <span class="title">创建{{ list && list.length }}个项目</span>
    <div class="list four-scroll-y" v-infinite-scroll="onScroll" :infinite-scroll-distance="1">
      <div class="block create-block" @click="doAdd">
        <icon-font type="icon-jiahao1" class="add-icon" />
        <span class="label">新建项目</span>
      </div>
      <template v-if="list && list.length > 0">
        <div class="block item-block" v-for="(item, index) in list" :key="index" @click="goGraph(item.id)">
          <span class="name ellipsis" :title="item.name">{{ item.name }}</span>
          <span class="time">创建时间：{{ moment(item.createTimestamp).format('YYYY-MM-DD HH:mm') || '-' }}</span>
          <div class="tools">
            <span class="tool" @click.stop="doEdit(item)">
              <el-tooltip content="编辑" placement="top">
                <icon-font type="icon-bianji" class="tool-icon" />
              </el-tooltip>
            </span>
            <span class="tool" @click.stop="doDelete(item.id)">
              <el-tooltip content="删除" placement="top">
                <icon-font type="icon-shanchu" class="tool-icon" />
              </el-tooltip>
            </span>
            <span v-if="item.pinTimestamp" class="tool" @click.stop="cancelPin(item)" style="opacity: 1">
              <el-tooltip content="取消置顶" placement="top">
                <icon-font type="icon-zhiding" class="tool-icon" />
              </el-tooltip>
            </span>
            <span v-else class="tool" @click.stop="pin(item)">
              <el-tooltip content="置顶" placement="top">
                <icon-font type="icon-zhiding" class="tool-icon" />
              </el-tooltip>
            </span>
          </div>
        </div>
      </template>
      <ProjectGuide v-if="list && list.length === 0" />
    </div>
  </div>
  <ModifyProject :visible="visible" :title="title" :project="currentProject" @close="handleClose" @confirm="onConfirm" />
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import ProjectGuide from './ProjectGuide.vue'
import ModifyProject from './ModifyProject.vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import useProject from '../hooks/useProject'
import moment from 'moment'

const router = useRouter()
const { list, loading, getList, onScroll, modifyProject, deleteProject } = useProject()

const visible: any = ref(false)
const title: any = ref('')
const currentProject: any = ref(null)

function handleClose() {
  visible.value = false
}

function goGraph(id: number) {
  router.push(`/project/${id}/data-analyze`)
}

function doAdd() {
  title.value = '新建项目'
  visible.value = true
  currentProject.value = null
}

function doDelete(id: number) {
  ElMessageBox.confirm('确定要删除所选中的项目吗？', '删除项目？', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
    autofocus: false
  }).then(async () => {
    deleteProject(id)
  })
}

function doEdit(item: any) {
  title.value = '修改项目'
  visible.value = true
  currentProject.value = item
}

function onConfirm(params: any) {
  modifyProject(params, `${title.value}成功`, handleClose)
}

function pin(project: any) {
  modifyProject(
    {
      ...project,
      pinTimestamp: new Date().getTime()
    },
    '置顶成功'
  )
}

function cancelPin(project: any) {
  modifyProject(
    {
      ...project,
      pinTimestamp: 0
    },
    '取消置顶成功'
  )
}

onMounted(() => {
  getList()
})
</script>
<style lang="less" scoped>
@import '@/styles/scroll-bar';
.project-list-page {
  height: 100%;
  width: 100%;
  margin: 12px;
  padding: 25px 42px;
  position: relative;
  .title {
    font-size: 16px;
    color: #9b9da8;
  }
  .block {
    width: 250px;
    height: 165px;
    background: #fff;
    border-radius: 4px;
    border: 1px solid #e3e3e7;
  }
  .list {
    overflow: auto;
    padding-bottom: 10px;
    height: calc(100% - 12px - 25px);
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    grid-auto-rows: 165px;
    grid-auto-flow: row dense;
    column-gap: 26px;
    grid-row-gap: 24px;
    .block {
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      margin-top: 24px;
      position: relative;
    }
    .create-block {
      cursor: pointer;
      &:hover {
        box-shadow: 0 0 17px -7px rgba(105, 115, 255, 0.7);
        background-color: #f3f4ff;
      }
    }
    .item-block {
      background-image: url('../../../assets/project-background.png');
      background-repeat: no-repeat;
      background-size: cover;
      cursor: pointer;
      &:hover {
        background-color: #f3f4ff;
        box-shadow: 0 0 17px -7px rgba(105, 115, 255, 0.7);
        .tool {
          opacity: 0.6;
        }
      }
    }
    .add-icon {
      font-size: 34px;
      color: #9b9da8;
      margin-bottom: 12px;
      cursor: pointer;
    }
    .label {
      font-size: 16px;
      color: #5d637e;
    }
    .name {
      width: 226px;
      color: #5d637e;
      font-size: 16px;
      font-weight: 500;
      text-align: center;
      position: absolute;
      top: 57px;
    }
    .time {
      position: absolute;
      bottom: 12px;
      color: #5d637e;
      font-size: 14px;
      font-weight: 400;
    }
    .tools {
      position: absolute;
      top: 8px;
      right: 12px;
      display: flex;
      .tool {
        width: 24px;
        height: 24px;
        background-color: #6973ff;
        opacity: 0;
        border-radius: 24px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-right: 6px;
        .tool-icon {
          font-size: 15px;
          display: flex;
          align-items: center;
          color: #fff;
        }
        &:hover {
          opacity: 1;
        }
      }
    }
  }

  .guide {
    position: absolute;
    top: 50%;
  }
  .ellipsis {
    text-overflow: ellipsis;
    // white-space: nowrap;
    overflow: hidden;
    -webkit-line-clamp: 2;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    word-break: break-all;
  }
}
</style>
