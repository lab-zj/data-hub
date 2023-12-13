<template>
  <div>
    <el-dialog class="new-project-dialog" :model-value="visible" :modal="true" width="408px" align-center @close="handleClose" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">{{ title }}</span>
        </div>
      </template>
      <div class="content">
        <el-form ref="formRef" :model="formData" :rules="formRules" @submit.prevent>
          <el-form-item label="项目名称" prop="name" style="flex: 1; margin-right: 5px" @click.stop>
            <el-input v-model="formData.name" placeholder="请输入项目名称" @keyup.enter="confirm" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="footer">
          <div>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="confirm">确认</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, watch } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '新建项目' },
  project: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['close', 'confirm'])

const formRef: any = ref(null)
const formData: any = reactive({
  name: ''
})
const formRules: any = reactive<any>({
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
})

async function confirm() {
  formRef.value.validate(async (valid: any, fields: any) => {
    if (valid) {
      emit('confirm', {
        ...props.project,
        name: formData.name,
        id: props.project?.id
      })
    }
  })
}

function handleClose() {
  formData.name = ''
  emit('close')
}

watch(
  () => props.visible,
  () => {
    formData.name = props.project?.name
  }
)
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
