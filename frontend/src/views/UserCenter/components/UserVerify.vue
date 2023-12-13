<template>
  <div>
    <el-dialog class="user-verify-dialog" :model-value="visible" :modal="true" width="510px" align-center @close="handleClose" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">身份验证</span>
        </div>
      </template>
      <div class="content">
        <span v-if="!isDingBind" class="label">请输入密码验证</span>
        <span v-else class="label">请输入旧密码，或使用第三方扫码验证</span>
        <el-form ref="formRef" :model="formData" :rules="formRules" @submit.prevent>
          <el-form-item label="" prop="password" style="flex: 1; margin-right: 5px" @click.stop>
            <el-input v-model="formData.password" placeholder="请输入密码" @keyup.enter="confirm" />
          </el-form-item>
        </el-form>
        <div v-if="isDingBind" class="three-party">
          <span>第三方验证</span>
          <DingEntrance :redirect="redirect" />
        </div>
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
import { reactive, computed } from 'vue'
import DingEntrance from '@/views/UserCenter/components/DingEntrance.vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  visible: { type: Boolean, default: false },
  operate: { type: String, default: '' }
})

const emit = defineEmits(['close', 'confirm'])
const userStore = useUserStore()

const isDingBind = computed(() => {
  return userStore?.user?.dingTalkUserInfo?.openId
})

const redirect = computed(() => {
  const _operate = !['username', 'password'].includes(props.operate) ? 'ding' : props.operate
  return `/user-center/config?operate=${_operate}`
})

const formData: any = reactive({
  password: ''
})
const formRules: any = [{}]

function confirm() {
  emit('confirm', formData.password)
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
.user-verify-dialog {
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
    .three-party {
      display: flex;
      flex-direction: column;
      margin-top: 24px;
      span {
        font-size: 14px;
        margin-bottom: 11px;
        color: #5d637e;
      }
    }
  }
}
</style>
