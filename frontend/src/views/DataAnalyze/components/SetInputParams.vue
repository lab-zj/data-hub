<template>
  <div>
    <el-dialog class="set-input-params-dialog" :model-value="visible" width="382px" align-center @close="handleClose" destroy-on-close :lock-scroll="false">
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">入参设置—{{ name }}</span>
        </div>
      </template>
      <div class="content">
        <AlgoInputs :id="props.data.id" :input-list="inputList" @change="onChange" />
      </div>
      <template #footer>
        <span class="footer">
          <div>
            <el-button @click="handleClose">取消</el-button>
            <el-button type="primary" @click="onConfirm" :loading="loading">确定</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import { cloneDeep } from 'lodash'
import AlgoInputs from './common/AlgoInputs.vue'
import { convertParams } from '../util/util'

const props = defineProps({
  visible: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  data: {
    type: Object,
    default: () => {}
  }
})

const emit = defineEmits(['close', 'confirm'])

const name: any = ref('')
const inputList: any = ref(null)

watch(
  () => props.data,
  () => {
    if (props.data) {
      name.value = props.data?.name
      inputList.value = convertParams(props.data?.dataset?.inputParamTemplate) || []
    }
  },
  { deep: true }
)

function onChange(_data: any) {
  inputList.value = cloneDeep(_data)
}

function handleClose() {
  emit('close')
}

function onConfirm() {
  emit('confirm', {
    cellId: props.data.id,
    inputList: inputList.value
  })
}
</script>
<style lang="less" scoped>
:deep(.el-dialog__header) {
  padding: 10px 20px !important;
  border-bottom: 1px solid #f2f2f2;
}
:deep(.el-dialog__headerbtn) {
  top: 0;
}
</style>
