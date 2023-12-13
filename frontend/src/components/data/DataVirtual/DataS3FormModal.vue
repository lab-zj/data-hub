<template>
  <div>
    <el-dialog class="s3FormContainer" :model-value="visible" width="520px" align-center @close="onCancel" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">新建S3文件</span>
        </div>
      </template>
      <div class="content">
        <el-form ref="s3FormRef" :model="formData" :rules="formRules" label-width="120px">
          <el-form-item prop="fileName" label="文件名">
            <el-input v-model="formData.fileName" placeholder="请输入文件名" />
          </el-form-item>
          <el-form-item prop="endpoint" label="网络地址">
            <el-input v-model="formData.endpoint" placeholder="请输入网络地址" />
          </el-form-item>
          <el-form-item prop="accessKey" label="密钥">
            <el-input v-model="formData.accessKey" placeholder="请输入密钥" />
          </el-form-item>
          <el-form-item prop="accessSecret" label="私钥">
            <el-input v-model="formData.accessSecret" placeholder="请输入私钥" />
          </el-form-item>
          <el-form-item prop="bucket" label="bucket">
            <el-input v-model="formData.bucket" placeholder="请输入bucket" />
          </el-form-item>
          <el-form-item prop="objectKey" label="objectKey">
            <el-input v-model="formData.objectKey" placeholder="请输入objectKey" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="onCancel">取消</el-button>
        <el-button type="primary" :loading="loading" :disabled="disabled" @click="onSubmit">新建</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
import { reactive, ref, computed } from 'vue'
import type { FormInstance } from 'element-plus'
import { useRoute } from 'vue-router'

defineProps({
  visible: { type: Boolean, default: false },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['create', 'close'])

const route = useRoute()
const routes: any = computed(() => {
  const path = route?.path.indexOf('path') > -1 ? route.path.split('=')[1] : ''
  return path
})
const currentPath: any = computed(() => {
  return `${decodeURIComponent(routes.value)}/`
})

const s3FormRef = ref<FormInstance>()

const formData = reactive({
  fileName: '', // 文件名
  endpoint: '', // 网络地址
  accessKey: '', // 密钥
  accessSecret: '', // 私钥
  bucket: '',
  objectKey: ''
})

const formRules = {
  fileName: [{ required: true, message: '请输入文件名', trigger: 'blur' }],
  endpoint: [{ required: true, message: '请输入网络地址', trigger: 'blur' }],
  accessKey: [{ required: true, message: '请输入密钥', trigger: 'blur' }],
  accessSecret: [{ required: true, message: '请输入私钥', trigger: 'blur' }],
  bucket: [{ required: true, message: '请输入bucket', trigger: 'blur' }],
  objectKey: [{ required: true, message: '请输入objectKey', trigger: 'blur' }]
}

const disabled: any = computed(() => {
  return !formData.fileName || !formData.endpoint || !formData.accessKey || !formData.accessSecret || !formData.bucket || !formData.objectKey
})

function onSubmit() {
  const { fileName, endpoint, accessKey, accessSecret, bucket, objectKey } = formData
  const suffix = '.s3'
  emit('create', {
    path: `${currentPath.value}${fileName}${suffix}`,
    virtualFile: {
      type: 's3',
      name: fileName,
      namedStreamResources: {
        endpoint,
        accessKey,
        accessSecret
      },
      bucket,
      objectKey
    }
  })
}

function onCancel() {
  emit('close')
  reset()
}

function reset() {
  formData.fileName = ''
  formData.endpoint = ''
  formData.accessKey = ''
  formData.accessSecret = ''
  formData.bucket = ''
  formData.objectKey = ''
}
</script>
<style lang="less" scoped>
.s3FormContainer {
}
</style>
