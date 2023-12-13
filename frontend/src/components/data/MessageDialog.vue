<!-- 二次确认弹窗 -->
<template>
  <div>
    <el-dialog :model-value="visible" width="384" align-center @close="operateFunc('close')">
      <div>
        <div class="dialog-title">
          <el-icon color="#EEAF38" size="24"><WarningFilled /></el-icon>
          <strong>{{ confirmObject.title }}</strong>
        </div>
        <div class="message">{{ confirmObject.message }}</div>
      </div>
      <template #footer>
        <!-- <span class="dialog-footer"> -->
        <div>
          <!-- <el-button @click="operateFunc(confirmObject.cancelBtnText==='新增'?'add':'close')">{{confirmObject.cancelBtnText?confirmObject.cancelBtnText:'取消'}}</el-button> -->
          <el-button @click="operateFunc('close')">{{ confirmObject.cancelBtnText ? confirmObject.cancelBtnText : '取消' }}</el-button>
          <el-button type="primary" @click="operateFunc(confirmObject.operate)">{{ confirmObject.confirmBtnText }}</el-button>
        </div>
        <!-- </span> -->
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '提示' },
  confirmObject: {
    type: Object,
    default(): Object {
      return {
        title: '提示',
        message: '确认删除？',
        data: {}
      }
    }
  }
})

const emit = defineEmits<{
  (e: 'operate', data?: any): void
}>()

const operateFunc = (type: any) => {
  emit('operate', type, props.confirmObject.data)
}
</script>

<style lang="less" scoped>
:deep(.el-dialog__header) {
  padding: 0;
  // .el-dialog__headerbtn {
  //   top: 0;
  //   display: none;
  // }
}
:deep(.el-dialog__body) {
  padding: 32px;
  padding-bottom: 24px;
}
:deep(.el-dialog__footer) {
  border-top: 0 !important;
  padding: 24px 32px;
  padding-top: 0;
}

.dialog-title {
  display: flex;
  align-items: center;
  margin-bottom: 12px;

  strong {
    font-size: 16px;
    color: #111111;
    word-break: break-all;
  }
  .el-icon {
    margin-right: 24px;
  }
}

.message {
  word-break: break-all;
  margin-left: 48px;
  color: #5d637e;
}

.text-ellipsis {
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}
</style>
