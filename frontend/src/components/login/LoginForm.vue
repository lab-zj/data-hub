<template>
  <div>
    <div class="login-form">
      <el-form ref="ruleFormRef" :model="form" :rules="rules">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="账号"
            class="input"
            @focusin="inputFocus('name', 'in')"
            @focusout="inputFocus('name', 'out')"
            @mouseover="inputHover('name', 'in')"
            @mouseout="inputHover('name', 'out')"
          >
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            class="input"
            show-password
            @focusin="inputFocus('password', 'in')"
            @focusout="inputFocus('password', 'out')"
            @mouseover="inputHover('password', 'in')"
            @mouseout="inputHover('password', 'out')"
          >
          </el-input>
        </el-form-item>
        <!-- <div class="forget-password">
          <a style="margin-right: 220px;" @click="clickForgetPassword"
            >忘记密码</a
          >
          <forget-password
            :forget-password-modal-visible="forgetPasswordModalVisible"
            @close-forget-password-modal="closeForgetPasswordModal"
          ></forget-password>
        </div> -->
        <div class="optional">
          <el-form-item prop="rememberMe">
            <el-checkbox v-model="form.rememberMe" label="下次自动登录" />
          </el-form-item>
          <div class="gotoRegister">
            还没账号？
            <router-link to="/register">去注册</router-link>
          </div>
        </div>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit(ruleFormRef)" class="login-button">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
    <ThreeParty />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import useUser from '@/components/hooks/useUser'
import ThreeParty from './ThreeParty.vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'
// import UserStore from '@/store/modules/user'
// import DataStore from '@/store/modules/data'
// import UserGuideStore from '@/store/modules/user-guide'
// import ForgetPassword from '@/components/Login/ForgetPassword.vue'

const userStore = useUserStore()

const { userLogin, whoAmI } = useUser()
const form: any = reactive({
  username: '',
  password: '',
  rememberMe: false
})
const rules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: 'blur'
    }
  ]
})
const ruleFormRef = ref<FormInstance>()

// const forgetPasswordModalVisible: any = ref(false)
const focus: any = ref('')
const hover: any = ref('')

// const clickForgetPassword = () => {
//       forgetPasswordModalVisible.value = true
//     }
// const closeForgetPasswordModal = () => {
//       forgetPasswordModalVisible.value = false
//     }

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) {
    return
  }
  await formEl.validate(async (valid: any, fields: any) => {
    if (valid) {
      userLogin({
        username: form.username,
        password: form.password,
        rememberMe: form.rememberMe
      })
    } else {
      console.log('error submit!', fields)
    }
  })
  // event.preventDefault()
}

const inputFocus = (item: any, type: any) => {
  switch (type) {
    case 'in':
      focus.value = item
      break
    case 'out':
      focus.value = ''
      break
    default:
  }
}

const inputHover = (item: any, type: any) => {
  switch (type) {
    case 'in':
      hover.value = item
      break
    case 'out':
      hover.value = ''
      break
    default:
  }
}

onMounted(async () => {
  // rememberMe功能
  await whoAmI()
  if (userStore?.validUser) {
    router.push('/management/projects')
  }
})
</script>

<style lang="less" scoped>
.login-form {
  display: flex;
  justify-content: center;
}

.input {
  width: 280px;
}

:deep(.el-input__inner) {
  background-color: #fff !important;
}

// 输入框
// .input .el-input {
//   border: 0;
//   border-bottom: 1px solid #e8e8e8;
//   border-bottom-left-radius: 0;
//   border-bottom-right-radius: 0;
//   font-size: 16px;

//   &:not(:first-child) {
//     padding-left: 25px;
//   }

//   &:focus {
//     border-bottom: 1px solid #6973ff;
//     box-shadow: none;
//   }
// }

// 输入框的前缀图标

// 行间距
.ant-form-item {
  margin-bottom: 15px;
}

// 对话框标题
// 记住密码栏
.forget-password {
  margin-bottom: 28px;
  margin-top: 24px;
}

.login-button {
  background: #6973ff;
  border-bottom: 12px;
  border-radius: 17px;
  box-shadow: 0 2px 6px 0 rgba(101, 129, 247, 0.4);
  font-size: 16px;
  height: 34px;
  width: 280px;
}

// 按钮上面的间距
.ant-form-item:last-child {
  margin-bottom: 12px;
}

.optional {
  display: flex;
  font-size: 14px;
  align-items: center;
  justify-content: space-between;
  .gotoRegister {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 18px;
    a {
      color: #646cff;
    }
  }
}
</style>
