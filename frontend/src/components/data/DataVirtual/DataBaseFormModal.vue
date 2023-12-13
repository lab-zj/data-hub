<template>
  <div>
    <el-dialog class="dataBaseFormContainer" :model-value="visible" width="520px" align-center @close="onCancel" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">新建数据库文件</span>
        </div>
      </template>
      <div class="content">
        <el-form ref="databaseFormRef" :model="formData" :rules="formRules" label-width="120px">
          <el-form-item prop="fileName" label="服务器文件名">
            <el-input v-model="formData.fileName" placeholder="请输入服务器文件名" />
          </el-form-item>
          <el-form-item prop="type" label="数据库类型">
            <el-radio-group v-model="formData.type">
              <el-radio border label="MySQL" />
              <el-radio border label="postgresql" />
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="databaseName" label="数据库名">
            <el-input v-model="formData.databaseName" placeholder="请输入数据库名" />
          </el-form-item>
          <el-form-item v-if="formData.type === 'postgresql'" prop="schemaName" label="schemaName">
            <el-input v-model="formData.schemaName" placeholder="请输入schemaName" />
          </el-form-item>
          <el-form-item prop="host" label="主机地址">
            <el-input v-model="formData.host" placeholder="请输入主机地址" />
          </el-form-item>
          <el-form-item prop="port" label="端口号">
            <el-input v-model="formData.port" placeholder="请输入端口号" />
          </el-form-item>
          <el-form-item prop="username" label="登录用户名">
            <el-input v-model="formData.username" placeholder="请输入登录用户名" />
          </el-form-item>
          <el-form-item prop="password" label="登录密码" class="password">
            <el-input v-model="formData.password" placeholder="请输入登录密码" />
            <el-button class="connection-test" round :loading="connecting" :disabled="connectDisabled" @click="onConnect">连接测试</el-button>
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
  loading: { type: Boolean, default: false },
  connecting: { type: Boolean, default: false }
})

const emit = defineEmits(['create', 'close', 'connect'])

const route = useRoute()
const routes: any = computed(() => {
  const path = route?.path.indexOf('path') > -1 ? route.path.split('=')[1] : ''
  return path
})
const currentPath: any = computed(() => {
  return `${decodeURIComponent(routes.value)}/`
})

const databaseFormRef = ref<FormInstance>()

const formData = reactive({
  fileName: '', // 服务器文件名
  type: 'MySQL',
  databaseName: '', // 数据库名
  schemaName: '',
  host: '',
  port: '',
  username: '',
  password: ''
})

const formRules = {
  fileName: [{ required: true, message: '请输入服务器文件名', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  schemaName: [{ required: true, message: '请输入schemaName', trigger: 'blur' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }]
}

const disabled: any = computed(() => {
  return (
    !formData.fileName ||
    !formData.databaseName ||
    !formData.host ||
    !formData.port ||
    !formData.username ||
    !formData.password ||
    (formData.type === 'postgresql' && !formData.schemaName)
  )
})

const connectDisabled: any = computed(() => {
  return !formData.databaseName || !formData.host || !formData.port || !formData.username || !formData.password || (formData.type === 'postgresql' && !formData.schemaName)
})

/**
 * 连接测试
 */
function onConnect() {
  const { type, host, port, username, password, databaseName, schemaName } = formData
  emit('connect', {
    host,
    port,
    username,
    password,
    databaseName,
    schemaName,
    type: type.toLowerCase()
  })
}
/**
 * 新建虚拟文件
 */
function onSubmit() {
  const { type, host, port, username, password, databaseName, schemaName, fileName } = formData
  const suffix = type.toLowerCase() === 'mysql' ? '.my' : '.ps'
  emit('create', {
    path: `${currentPath.value}${fileName}${suffix}`,
    virtualFile: {
      type: type.toLowerCase(),
      host,
      port,
      username,
      password,
      databaseName,
      schemaName: schemaName || undefined
    }
  })
}

function onCancel() {
  emit('close')
  reset()
}

function reset() {
  formData.fileName = ''
  formData.databaseName = ''
  formData.type = 'MySQL'
  formData.schemaName = ''
  formData.host = ''
  formData.port = ''
  formData.username = ''
  formData.password = ''
}
</script>
<style lang="less" scoped>
.dataBaseFormContainer {
  .password {
    :deep(.el-input) {
      width: 50%;
    }
    .connection-test {
      margin-left: 10px;
    }
  }
}
</style>
