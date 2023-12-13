<template>
  <!-- 选中多文件 -->
  <div v-if="isMulti" class="multi">
    <div class="file">
      <icon-font type="icon-duofenwenjian" class="multi-iconfont" />
    </div>
    <div class="tips">{{ `选中${multiFile.length}个文件` }}</div>
  </div>
  <template v-else>
    <div v-if="type === 'csv'" class="csv">
      <CsvFileTemplate :path="record.path" />
    </div>
    <div v-else-if="type === 'image'" class="image">
      <ImgFileTemplate :path="record.path" />
    </div>
    <div v-else-if="type === 'video'" class="video">
      <VideoFileTemplate :path="record.path" />
    </div>
    <div v-else-if="type === 'txt'" class="txt four-scroll-y">
      <TxtFileTemplate :path="record.path" />
    </div>
    <div v-else-if="type === 'folder'" class="folder">
      <i class="iconfont icon-a-wenjianjiashouqibeifen2 icon" />
    </div>
    <div v-else-if="type === 'virtual'" class="virtual txt four-scroll-y">
      <VirtualFileTemplate :path="record.path" />
    </div>
    <div v-else class="unknown">
      <UnknownFileTemplate />
    </div>
    <div v-if="type === 'folder'" class="base-info base-info-folder">
      <span class="title ellipsis" :title="record.name">
        <strong>{{ record.name }}</strong>
      </span>
      <!-- <p class="label">创建时间：{{ meta && meta.createTime }}</p>
      <p class="label">最新修改时间：{{ meta && meta.updateTime }}</p> -->
      <div class="folder-list_preview">
        <strong class="title"> 文件夹内容 </strong>
        <FolderList :data="record" :need-selected="false" />
      </div>
    </div>
    <div v-else class="base-info">
      <p class="view" v-if="type !== 'unknown'" @click="gotoDetail">查看详情<i class="iconfont icon-a-jiantoubeifen2 icon" /></p>
      <span class="title ellipsis" :title="record.name">
        <strong>{{ record.name }}</strong>
      </span>
      <p class="label">文件大小：{{ meta && sizeConversion(meta.size) }}</p>
      <p class="label">创建时间：{{ meta && meta.createTime }}</p>
      <p class="label">最新修改时间：{{ meta && meta.updateTime }}</p>
      <p class="label">文件格式：{{ meta && meta.fileType === 'NOT_RECOGNITION' ? '无法识别的格式' : meta.type }}</p>
    </div>
  </template>
</template>
<script setup lang="ts">
import { ref, watch, computed, Ref } from 'vue'
import { useRouter } from 'vue-router'
import useMeta from '@/components/hooks/useMeta.ts'
import VideoFileTemplate from '@/components/common/VideoFileTemplate.vue'
import VirtualFileTemplate from '@/components/common/VirtualFileTemplate.vue'
import UnknownFileTemplate from '@/components/common/UnknownFileTemplate.vue'
import CsvFileTemplate from '@/components/common/CsvFileTemplate.vue'
import ImgFileTemplate from '@/components/common/ImgFileTemplate.vue'
import TxtFileTemplate from '@/components/common/TxtFileTemplate.vue'
import FolderList from '@/components/common/FolderList.vue'
import { sizeConversion, getRecordType } from '@/util/util'

const props = defineProps({
  multiFile: { type: Array, default: () => [] },
  record: { type: Object, default: () => {} },
  from: { type: String, default: '' }
})

const type: Ref<string> = ref('')
const isMulti = computed(() => props.multiFile.length > 1)
const router = useRouter()

const { meta, getMeta, reset } = useMeta()

watch(
  () => props.record,
  async (val: any) => {
    reset()
    type.value = getRecordType(props.record)
    // 文件夹没有meta信息
    if (!val.directory) {
      getMeta(val.path)
    }
  },
  { deep: true, immediate: true }
)

/**
 * 查看详情页
 */
const gotoDetail = () => {
  const path = encodeURIComponent(props.record.path)
  if (props.from) {
    router.push(`/management/detail/path=${path}?from=${props.from}`)
  } else {
    router.push(`/management/detail/path=${path}`)
  }
}
</script>
<style lang="less" scoped>
@import '@/styles/scroll-bar';
.multi {
  .file {
    height: 180px;
    background: #fafafc;
    display: flex;
    justify-content: center;
    align-items: center;
    .multi-iconfont {
      font-size: 120px;
    }
  }
  .tips {
    color: #373b52;
    font-size: 600;
    margin-top: 16px;
    text-align: center;
  }
}
.image {
  height: 510px;
  background: #fafafc;
  .el-image {
    width: 100%;
    height: 100%;
  }
  img {
    max-width: 100%;
    max-height: 100%;
  }
}

.video {
  width: 367px;
  max-height: 510px;
  height: 180px;
}
.txt {
  max-height: 510px;
  background: #fafafc;
  overflow: auto;
  color: #5d637e;
  padding: 8px;
  .txt_empty {
    display: flex;
    justify-content: center;
    color: #909399;
    font-size: 14px;
  }
}

.csv {
  height: 520px;
  :deep(.el-table) {
    background-color: #fafafc;
  }
}
.folder {
  height: 180px;
  background: #fafafc;
  display: flex;
  justify-content: center;
  align-items: center;
  .icon-a-wenjianjiashouqibeifen2 {
    font-size: 120px;
  }
}
.unknown {
  background: #fafafc;
}
.base-info {
  padding-top: 16px;
  p {
    margin: 8px 0 !important;
  }
  .view {
    color: #6973ff;
    margin-bottom: 16px !important;
    &:hover {
      cursor: pointer;
    }
  }
  .title {
    font-size: 14px;
    font-weight: 600;
    color: #373b52;
    display: inline-block;
  }
  .label {
    color: #5d637e;
  }
  .folder-list_preview {
    margin-top: 16px;
    height: calc(100% - 36px - 12px);
  }
}
.base-info-folder {
  height: calc(100% - 180px);
  .empty {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 50%;
    img {
      width: 208px;
    }
  }
}
.icon-a-wenjianjiashouqibeifen2 {
  color: #ffc436;
}

.ellipsis {
  max-width: 340px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
:deep(.preview-csv-header-row > th) {
  background-color: #fafafa !important;
}
:deep(.el-table-v2__header-cell) {
  background-color: #fafafa !important;
}
:deep(.el-table-v2__header-cell-text) {
  color: #373b52 !important;
}
:deep(.preview-csv-header-row > th) {
  background-color: #fafafa !important;
}
:deep(.el-image__error) {
  height: auto;
}
</style>
