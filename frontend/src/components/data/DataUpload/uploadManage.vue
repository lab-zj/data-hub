<template>
  <div class="upload-manage-panel" v-if="showUploadManage && isShow">
    <div class="content" :style="{ height: isWraped ? 0 : '267px', padding: isWraped ? 0 : '12px 2px 12px 16px' }">
      <el-scrollbar height="100%">
        <ul class="list">
          <li v-for="item in _uploadList" :key="item.uid">
            <div class="title">
              <icon-font :type="getUploadIcon(item.type)"></icon-font>
              <span class="name ellipsis" :title="item.name">{{ item.name }}</span>
            </div>
            <div class="size">
              {{ sizeConversion(item.size) }}
            </div>
            <div class="status">
              {{ item.status === 'ready' ? '上传中' : item.status === 'pending' ? '排队中' : item.status === 'fail' ? '上传失败' : '' }}
            </div>
            <div class="close">
              <i v-if="item.status === 'success'" class="iconfont icon-check-circle-fill icon"></i>
              <!-- 前端发送http请求到后端服务器，后端服务器接受完文件流，再去写mino服务器写文件，这里存在一个时差，导致上传中的文件有可能无法取消，暂时不做 -->
              <i v-if="item.status === 'pending'" class="iconfont icon-close-circle-fillbeifen icon" @click="onDelete(item)"></i>
            </div>
          </li>
        </ul>
      </el-scrollbar>
    </div>
    <div class="bar">
      <div class="title">
        <i v-if="allComplete" class="iconfont icon-check-circle-fill icon"></i>
        <icon-font v-else type="icon-shangchuanrenwu" color="#FAAD14" />
        {{ barInfo.tip }}
      </div>
      <div class="operation">
        <div class="wrapper" @click="onWrap">
          <i class="iconfont icon-fanye-baogaobeifen icon" :style="{ transform: !isWraped ? 'rotate(180deg)' : 'none' }"></i>
        </div>
        <i class="iconfont icon-guanbibeifen icon" @click="onCloseBar"></i>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useDataStore } from '@/stores/index'
import bus from '@/EventBus/eventbus.js'
import { useRoute } from 'vue-router'
// import { ElMessageBox } from 'element-plus'
import { sizeConversion } from '@/util/util'

const isWraped: any = ref(true) // 默认折叠状态

const dataStore = useDataStore()
const _uploadList: any = ref([])
const allComplete: any = computed(() => {
  return dataStore.uploadingCount === 0
})
const route = useRoute()
const isShow: any = ref(true)
const showUploadManage: any = computed(() => {
  const routePath = decodeURIComponent(route?.path)
  // 上传浮窗只能在项目管理、数据管理两个Tab页可见
  return dataStore.allUploadCount > 0 && routePath.includes('/management/data')
})
const barInfo: any = computed(() => {
  return dataStore.uploadingCount === 0
    ? {
        done: true,
        tip: '上传任务已完成'
      }
    : {
        done: false,
        tip: `${dataStore.uploadingCount}/${dataStore.allUploadCount}上传任务进行中`
      }
})

onMounted(() => {
  bus.on('displayUploadBar', (param: any) => {
    isShow.value = param
  })
})

// 根据所有文件的status，去更新data.uploadList中的状态
watch(
  () => dataStore.fileList,
  () => {
    dataStore.updateUploadFromfile()
  },
  { deep: true }
)

// 根据dataStore.uploadList去更新上传浮窗中的任务
watch(
  () => dataStore.uploadList,
  (val: any) => {
    _uploadList.value = val
  },
  { deep: true }
)

const onWrap = () => {
  isWraped.value = !isWraped.value
}

const onCloseBar = () => {
  isShow.value = false
  // 全部上传完成，点击关闭浮窗
  // if(allComplete.value) {
  //   console.log('全部已完成')
  //   isShow.value = false
  // }else{
  //   // 暂时不做上传中任务的删除
  //   ElMessageBox.confirm('删除任务？','您还有进行中的任务，确定要删除吗？',
  //   {
  //     confirmButtonText: '确定',
  //     cancelButtonText: '取消',
  //     type: 'info',
  //   }).then(()=>{
  //     // 点击确定
  //     onWrap()
  //   }).catch(()=>{
  //     // 点击取消
  //     console.log(456)
  //     return false
  //   })
  // }
}

const onDelete = (item: any) => {
  console.log('onDelete:', item, dataStore.uploadList, dataStore.fileStack)
  // ready状态：取消http请求
  const { status, type, uid } = item
  // 1、如果是单个文件
  if (type !== 'folder') {
    // pending状态：还未发起http请求，直接删除即可
    // ready状态：已经发起http请求，需要cancel掉http请求（后端服务器只接受了一部分的文件流，后端可以try/catch去处理删掉文件）
    // 边界情况，后端服务器接收了全部的文件流，已经去mino写文件了，前端触发cancel，暂无响应
    if (status === 'ready') {
      const cancelControl = dataStore.cancelControls.find((c: any) => c.uid === uid)
      console.log('onDelete-cancelControl-cancel:', cancelControl)
      if (cancelControl?.cancel) {
        cancelControl.cancel.abort()
      }
      dataStore.removeCancel(uid)
    }
    deleteUpload(uid)
  } else {
    // 2、如果是文件夹，文件夹里n个子文件，就会发起n个http请求，上传中的任务暂时无法删除
  }
}

const deleteUpload = (fileUid: string) => {
  dataStore.removeUpload(fileUid)
  dataStore.removeFile(fileUid)
  dataStore.removeFileFromStack(fileUid)
}

const getUploadIcon = (type: string) => {
  if (['csv'].includes(type)) {
    return 'icon-shuju'
  } else if (['png'].includes(type)) {
    return 'icon-tupian'
  } else if (['mp4'].includes(type)) {
    return 'icon-shipin'
  } else if (['txt'].includes(type)) {
    return 'icon-wendang'
  } else if (type === 'folder') {
    return 'icon-a-wenjianjiashouqibeifen2'
  } else {
    return 'icon-feichangguiwenjian'
  }
}
</script>
<style lang="less" scoped>
.upload-manage-panel {
  position: absolute;
  bottom: 28px;
  right: 20px;
  background: #fff;
  width: 400px;
  box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
  border-radius: 4px;
  z-index: 9;
  .bar {
    height: 44px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 18px;
    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
    .title {
      font-weight: 600;
      color: #373b52;
      font-size: 14px;
      display: flex;
      align-items: center;
      .iconfont {
        display: flex;
        align-items: center;
        margin-right: 8px;
      }
    }
    .operation {
      display: flex;
      flex: 1;
      &:hover {
        cursor: pointer;
      }
    }
    .wrapper {
      margin-right: 8px;
      display: flex;
      justify-content: end;
      flex: 1;
    }
    .iconfont {
      // color: #5D637E;
      font-size: 16px;
    }
  }
  .content {
    height: 236px;
    padding: 12px 16px;
    .list {
      list-style: none;
      padding: 0 !important;
      margin: 0 !important;
      li {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;
        margin-right: 12px;
      }
      .title {
        color: #373b52;
        font-size: 14px;
        width: 180px;
        display: flex;
        flex-direction: row;
        align-items: center;
        img {
          width: 16px;
        }
        .iconfont {
          font-size: 16px;
        }
        .name {
          margin-left: 8px;
          display: inline-block;
        }
        .ellipsis {
          max-width: 155px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .size {
        color: #373b52;
        font-size: 14px;
        width: 80px;
      }
      .status {
        color: #222432;
        font-size: 12px;
        width: 48px;
      }
      .operate,
      .close {
        color: #6973ff;
      }
      .close {
        width: 16px;
        &:hover {
          cursor: pointer;
        }
      }
    }
  }
  .icon-check-circle-fill {
    color: #8cc380 !important;
  }
}
</style>
