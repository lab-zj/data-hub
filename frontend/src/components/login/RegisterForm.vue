<template>
  <div>
    <div class="login-form">
      <el-form ref="ruleFormRef" :model="form" :rules="rules">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" class="input"> </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" class="input"> </el-input>
        </el-form-item>
        <el-form-item prop="repeatPassword">
          <el-input v-model="form.repeatPassword" show-password type="password" placeholder="请确认密码" class="input"> </el-input>
        </el-form-item>
        <div class="gotoLogin">
          已有账号？
          <router-link to="/login">去登录</router-link>
        </div>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit(ruleFormRef)" class="register-button" :disabled="!isAgreement">注册</el-button>
        </el-form-item>
      </el-form>
    </div>
    <ThreeParty />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import useUser from '@/components/hooks/useUser'
import ThreeParty from './ThreeParty.vue'

// 通用
const { userRegister } = useUser()

const form = reactive({
  password: '',
  username: '',
  repeatPassword: ''
})
const isAgreement = ref(true) // 同意 隐私协议， 默认为 false
const passwordRuleList: any = ref([
  {
    rule: false,
    text: '8-16个字符'
  },
  {
    rule: false,
    text: '包含数字'
  },
  {
    rule: false,
    text: '包含小写字母'
  },
  {
    rule: false,
    text: '数字、小写字母、大写字母、特殊字符至少包含3种'
  }
])
const ruleFormRef = ref<FormInstance>()

// 重复输入密码rule
const checkRepeatPassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('请确认密码'))
  }
  if (value !== form.password) {
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
  password: [
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
  repeatPassword: [
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

const handleSubmit = async (formEl: FormInstance | undefined) => {
  if (!formEl) {
    return
  }
  await formEl.validate(async (valid: any, fields: any) => {
    if (valid) {
      userRegister({
        username: form.username,
        password: form.password
      })
    } else {
      console.log('error submit!', fields)
    }
  })
  // event.preventDefault()
}

// password 和 repeat password 校验
const validateToRepeatPassword = (rule: any, value: any, callback: any) => {
  // 测试密码长度是否为8-16位
  if (/[\S\s]{8,16}/.test(value)) {
    passwordRuleList.value[0].rule = true
  } else {
    passwordRuleList.value[0].rule = false
  }
  // 测试密码是否包含数字
  if (/[\d.]/.test(value)) {
    passwordRuleList.value[1].rule = true
  } else {
    passwordRuleList.value[1].rule = false
  }
  // 测试密码是否包含小写数字
  if (/[.a-z]/.test(value)) {
    passwordRuleList.value[2].rule = true
  } else {
    passwordRuleList.value[2].rule = false
  }
  // 测试密码是否符合全部规则至少包含数字、大写字母、小写字母、特殊字符中的三种类型
  if (/^(?![A-Za-z]+$)(?![\dA-Z]+$)(?![\WA-Z_]+$)(?![\da-z]+$)(?![\W_a-z]+$)(?![\W\d_]+$)[\W\w]{3,}$/.test(value)) {
    passwordRuleList.value[3].rule = true
  } else {
    passwordRuleList.value[3].rule = false
  }
  // 测试密码是否符合全部规则
  if (!/^(?![A-Za-z]+$)(?![\dA-Z]+$)(?![\WA-Z_]+$)(?![\da-z]+$)(?![\W_a-z]+$)(?![\W\d_]+$)[\W\w]{8,16}$/.test(value)) {
    callback('请输入8-16位字符，至少包含数字、大写字母、小写字母、特殊字符中的三种类型')
  }
  if (value && form.getFieldValue('repeatPassword')) {
    form.validateFields(['repeatPassword'], { force: true })
  }
  callback()
}
</script>

<style lang="less" scoped>
.login-form {
  display: flex;
  justify-content: center;
}

.input {
  width: 280px;
}

// /deep/ .ant-select-selection--single {
//   height: 25px;
// }

// //去除选择器border
// /deep/ .ant-select-selection {
//   border: 0;
// }

// // 去除选择器foucus蓝框
// /deep/ .ant-select-focused .ant-select-selection,
// .ant-select-selection:focus,
// .ant-select-selection:active {
//   box-shadow: 0 0 0 0;
// }

// /deep/ .ant-input-affix-wrapper .ant-input-prefix :not(.anticon),
// .ant-input-affix-wrapper .ant-input-suffix :not(.anticon) {
//   line-height: 2;
// }

// // 输入框
// /deep/ .input .ant-input {
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

// // 手机号输入
// /deep/ .telephone-input .ant-input {
//   &:not(:first-child) {
//     padding-left: 96px;
//   }
// }

// /deep/ .ant-form-explain {
//   margin-left: calc(50% - 140px);
//   text-align: left;
//   width: 280px;
// }

// // 输入框的前缀图标
// /deep/ .input .ant-input-prefix {
//   color: #5d637e;
//   font-size: 18px;
//   left: 0;
// }

.success-password-tips {
  align-items: center;
  color: #52c41a;
  display: flex;
  line-height: 22px;
}

.error-password-tips {
  align-items: center;
  color: #7e817d;
  display: flex;
  line-height: 22px;
}

// 行间距
.ant-form-item {
  margin-bottom: 15px;

  &:last-child {
    margin-bottom: 12px;
  }
}

.register-button {
  background: #6973ff;
  border-bottom: 12px;
  border-radius: 17px;
  box-shadow: 0 2px 6px 0 rgba(101, 129, 247, 0.4);
  font-size: 16px;
  height: 34px;
  margin-bottom: 0;
  margin-top: 16px;
  width: 280px;

  &.ant-btn-primary[disabled] {
    background: #ddd;
    box-shadow: none;
  }
}

// /deep/ .agreement-form-item {
//   .ant-form-item-control {
//     margin: 0 auto;
//     text-align: left;
//     width: 280px;
//   }
// }

.link {
  color: #6973ff;
  cursor: pointer;
}

// /deep/ .ant-input-group-addon {
//   background: #fff;
//   border: 0;

//   .get-code {
//     cursor: pointer;
//   }

//   /deep/ .count {
//     color: #5d637e;
//     font-size: 14px;
//   }
// }
.gotoLogin {
  display: flex;
  align-items: center;
  justify-content: right;
  font-size: 14px;
  a {
    color: #646cff;
  }
}
</style>
