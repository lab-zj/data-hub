<template>
  <div>
    <el-dialog class="location-dialog" :model-value="visible" :modal="false" width="382px" align-center @close="handleClose" destroy-on-close>
      <template #header="{ titleId, titleClass }">
        <div class="header">
          <span :id="titleId" :class="titleClass">第 {{ currentNumber + 1 }} 个 / 共 {{ data && data.length }} 个</span>
        </div>
      </template>
      <template #footer>
        <span class="footer">
          <div>
            <el-button :disabled="currentNumber === 0" @click="pre">上一个</el-button>
            <el-button :disabled="currentNumber + 1 === data.length" @click="next">下一个</el-button>
          </div>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  current: {
    type: Object,
    default: () => {}
  },
  data: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['close', 'locate'])

const currentNumber: any = computed(() => {
  console.log(
    'props.data:',
    props.data,
    props.current,
    props.data.findIndex((node: any) => node.id === props.current.id)
  )
  return props.data.findIndex((node: any) => node.id === props.current.id)
})

function pre() {
  console.log('pre:', currentNumber.value)
  if (currentNumber.value - 1 < 0) {
    return
  }
  emit('locate', props.data[currentNumber.value - 1])
}

function next() {
  if (currentNumber.value + 1 > props.data?.length) {
    return
  }
  emit('locate', props.data[currentNumber.value + 1])
}

function handleClose() {
  emit('close')
}
</script>
<style lang="less" scoped>
:deep(.el-dialog) {
  right: 24px;
  position: absolute;
  top: 129px;
}
:deep(.el-dialog__body) {
  display: none;
}
:deep(:focus) {
  outline: none;
}
:deep(:focus-visible) {
  outline: none;
}
</style>
