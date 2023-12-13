<template>
  <el-button type="primary" @click="uploadShow = true">算法上传</el-button>
  <el-button @click="callShow = true">算法调用</el-button>

  <el-dialog v-model="uploadShow" title="算法上传" width="631px" :close-on-click-modal="false" :destroy-on-close="true">
    <UploadAlgorithm @onCancel="uploadShow = false" />
  </el-dialog>

  <el-dialog v-model="callShow" title="算法调用" width="631px" :close-on-click-modal="false" :destroy-on-close="true">
    <CallAlgorithm />
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import UploadAlgorithm from '../components/algorithm/UploadAlgorithm.vue'
import CallAlgorithm from '../components/algorithm/CallAlgorithm.vue'
import YamlEditor from '@/components/algorithm/YamlEditor.vue'
import LoadingMask from '@/components/algorithm/LoadingMask.vue'

const uploadShow = ref(false)
const callShow = ref(false)
</script>

<style scoped>
:deep(.el-dialog__footer) {
  padding: 0;
}
</style>
