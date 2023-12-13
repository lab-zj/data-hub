<template>
  <div>
    <el-dialog
      class="first-dinglogin-dialog"
      :model-value="visible"
      :modal="true"
      width="382px"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">该钉钉号为首次登录账户</span>
        </div>
      </template>
      <span> 若错过该流程，请在账号设置页完成用户名与密码的设置。 </span>
      <template #footer>
        <span class="footer">
          <div>
            <el-button @click="bind">绑定已有账号</el-button>
            <el-button type="primary" @click="create">创建新账号</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import useUser from '@/components/hooks/useUser'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const props = defineProps({
  visible: { type: Boolean, default: false },
  data: {
    type: Array,
    default: () => []
  }
})

const router = useRouter()
const emit = defineEmits(['close', 'bind'])
const { userInitialize } = useUser()

async function create() {
  const result = await userInitialize()
  if (result === 'success') {
    handleClose()
    ElMessageBox.alert('点击确定前往用户设置页面修改用户名和密码', '创建新账号成功', {
      confirmButtonText: '确定',
      type: 'success'
    }).then(() => {
      router.push('/user-center/config')
    })
  }
}

function bind() {
  emit('bind')
}

function handleClose() {
  emit('close')
}
</script>
<style lang="less" scoped>
:deep(.el-dialog__body) {
  padding: 12px 20px;
}
:deep(:focus) {
  outline: none;
}

:deep(:focus-visible) {
  outline: none;
}
</style>
