<template>
  <div>
    <el-dialog class="bind-account-dialog" :model-value="visible" :modal="true" width="408px" align-center @close="handleClose" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">绑定已有账号</span>
        </div>
      </template>
      <div class="content">
        <span class="label">请输入您想要绑定的平台用户名与密码</span>
        <el-form ref="formRef" :model="formData" :rules="formRules">
          <el-form-item label="" prop="username" style="flex: 1; margin-right: 5px" @click.stop>
            <el-input v-model="formData.username" placeholder="请输入用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="" prop="password" style="flex: 1; margin-right: 5px">
            <el-input v-model="formData.password" placeholder="请输入密码" :maxLength="33" :prefix-icon="Lock" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="footer">
          <div>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="bind">绑定</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import useUser from '@/components/hooks/useUser'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  data: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'success-close'])
const { dingBindExistAccountFlow } = useUser()

const formRef: any = ref(null)
const formData: any = reactive({
  username: '',
  password: ''
})
const formRules: any = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    }
  ]
}

async function bind() {
  formRef.value.validate(async (valid: any, fields: any) => {
    if (valid) {
      try {
        const result = await dingBindExistAccountFlow({
          username: formData.username,
          password: formData.password
        })
        if (result === 'success') {
          handleSuccessClose()
        }
      } catch (error: any) {
        ElMessage.error('用户名或者密码输入错误！')
      }
    }
  })
}

function handleClose() {
  formData.username = ''
  formData.password = ''
  emit('close')
}

function handleSuccessClose() {
  emit('success-close')
}
</script>
<style lang="less" scoped>
:deep(.el-dialog__header) {
  margin-right: 0;
  border-bottom: 1px solid #e9e9e9;
}
:deep(.el-dialog__body) {
  padding: 12px 20px;
}
:deep(.el-dialog__footer) {
  border-top: 1px solid #e9e9e9;
}
:deep(:focus) {
  outline: none;
}

:deep(:focus-visible) {
  outline: none;
}
.bind-account-dialog {
  .content {
    .label {
      display: flex;
      margin-bottom: 16px;
    }
  }
}
</style>
