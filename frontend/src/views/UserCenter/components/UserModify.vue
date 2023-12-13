<template>
  <div>
    <el-dialog class="user-modify-dialog" :model-value="visible" :modal="true" width="510px" align-center @close="handleClose" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">{{ title }}</span>
        </div>
      </template>
      <div class="content">
        <el-form ref="formRef" :model="formData" :rules="rules" labelPosition="top" @submit.prevent>
          <el-form-item v-if="operate === 'username'" label="用户名" prop="username" style="flex: 1; margin-right: 5px" @click.stop>
            <el-input v-model="formData.username" placeholder="请输入用户名" @keyup.enter="confirm" />
          </el-form-item>
          <el-form-item v-if="operate === 'password'" label="请输入新登录密码" prop="passwordf" style="flex: 1; margin-right: 5px" @click.stop>
            <el-input v-model="formData.passwordf" placeholder="请输入新登录密码" />
          </el-form-item>
          <el-form-item v-if="operate === 'password'" label="请确认新登录密码" prop="passwords" style="flex: 1; margin-right: 5px" @click.stop>
            <el-input v-model="formData.passwords" placeholder="请输入新登录密码" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="footer">
          <div>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="confirm">确定</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'

defineProps({
  visible: { type: Boolean, default: false },
  title: {
    type: String,
    default: ''
  },
  operate: { type: String, default: '' }
})

const emit = defineEmits(['close', 'confirm'])

const formRef: any = ref(null)
const formData: any = reactive({
  username: '',
  passwordf: '',
  passwords: ''
})
// 重复输入密码rule
const checkRepeatPassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('请确认密码'))
  }
  if (value !== formData.passwordf) {
    callback('密码输入不一致！')
  }
  callback()
}

const validateUsername = (rule: any, value: any, callback: any) => {
  const allNumber = /^[0-9]+.?[0-9]+$/.test(value)
  if (value.length < 5) {
    return callback(new Error('用户名不可少于5个字符'))
  } else if (allNumber) {
    return callback(new Error('用户名禁止为纯数字'))
  } else if (value.includes('/')) {
    return callback(new Error('用户名不能包含特殊字符/'))
  } else {
    callback()
  }
}

const validatePassword = (rule: any, value: any, callback: any) => {
  const isMatch = /^(?=.*[a-zA-Z])(?=.*\d).+$/.test(value)
  if (value.length < 8) {
    return callback(new Error('密码不可少于8位'))
  } else if (!isMatch) {
    return callback(new Error('密码必须包含字母和数字'))
  } else {
    callback()
  }
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  passwordf: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    },
    {
      validator: validatePassword,
      trigger: 'blur'
    }
  ],
  passwords: [
    {
      required: true,
      message: '请确认密码',
      trigger: 'blur'
    },
    {
      validator: checkRepeatPassword,
      trigger: 'blur'
    }
  ]
})

function confirm() {
  formRef.value.validate((valid: any) => {
    if (valid) {
      emit('confirm', {
        username: formData.username,
        passwordf: formData.passwordf,
        passwords: formData.passwords
      })
    }
  })
}

function handleClose() {
  emit('close')
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
.user-modify-dialog {
  .dingding {
    width: 35px;
    cursor: pointer;
  }
  .header {
    span {
      font-size: 16px;
      color: #373b52;
    }
  }
  .content {
    .label {
      font-size: 14px;
      color: #5d637e;
      margin-bottom: 16px;
      display: flex;
    }
  }
}
</style>
