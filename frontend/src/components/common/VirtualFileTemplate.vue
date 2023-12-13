<!-- 虚拟文件渲染模板 -->
<template>
  <template v-if="virtualPreviewContent.type && (virtualPreviewContent.type === 'postgresql' || virtualPreviewContent.type === 'mysql')">
    <p>数据库类型：{{ virtualPreviewContent.type }}</p>
    <p>数据库名：{{ virtualPreviewContent.databaseName }}</p>
    <p v-if="virtualPreviewContent.type === 'postgresql'">schemaName：{{ virtualPreviewContent.schemaName }}</p>
    <p>主机地址：{{ virtualPreviewContent.host }}</p>
    <p>端口号：{{ virtualPreviewContent.port }}</p>
    <p>登录用户名：{{ virtualPreviewContent.username }}</p>
    <p>
      密码：{{ isShow ? virtualPreviewContent.password : '********' }} <el-icon class="eye-icon" @click="onView"><View v-if="!isShow" /><Hide v-else /></el-icon>
    </p>
    <p>连接测试：<el-button class="connection-test" round :loading="connecting" @click="onConnect">连接测试</el-button></p>
  </template>
  <template v-if="virtualPreviewContent.type && virtualPreviewContent.type === 's3'">
    <p>网络地址：{{ virtualPreviewContent.namedStreamResources.endpoint }}</p>
    <p>密钥：{{ virtualPreviewContent.namedStreamResources.accessKey }}</p>
    <p>私钥：{{ virtualPreviewContent.namedStreamResources.accessSecret }}</p>
    <p>bucket：{{ virtualPreviewContent.bucket || '-' }}</p>
    <p>objectKey：{{ virtualPreviewContent.objectKey || '-' }}</p>
  </template>
</template>
<script setup lang="ts">
import { watch, ref } from 'vue'
import useVirtualPreview from '@/components/hooks/useVirtualPreview'
import useVirtual from '@/components/data/hooks/useVirtual'

const props = defineProps({
  path: { type: String, default: '' }
})

const { virtualPreviewContent, virtualPreview } = useVirtualPreview()
const { connecting, connectTestFile } = useVirtual()

watch(
  () => props.path,
  () => {
    if (props.path) {
      virtualPreview(props.path)
    }
  },
  {
    immediate: true
  }
)

function onConnect() {
  connectTestFile({
    ...virtualPreviewContent.value
  })
}

const isShow: any = ref(false)
function onView() {
  isShow.value = !isShow.value
}
</script>
<style lang="less" scoped>
.eye-icon {
  cursor: pointer;
  &:hover {
    color: #6973ff;
  }
}
</style>
