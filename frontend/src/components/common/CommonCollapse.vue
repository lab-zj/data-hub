<!-- 横向折叠组件 -->
<template>
  <div class="common-collapse" :class="props.class">
    <div class="header" :style="isShow ? { marginBottom: '12px' } : {}">
      <strong> {{ title }} </strong>
      <div class="operate-hot" @click="isShow = !isShow">
        <icon-font
          type="icon-a-jiantoubeifen2"
          :style="{
            transform: isShow ? 'rotate(90deg)' : 'rotate(180deg)'
          }"
        />
      </div>
    </div>
    <div class="content" v-if="isShow">
      <slot name="content"></slot>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  class: {
    type: String,
    default: ''
  }
})

const isShow: any = ref(false)
</script>
<style lang="less" scoped>
.common-collapse {
  padding: 16px 0;
  font-size: 14px;
  border-bottom: 1px solid #e9e9e9;
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .operate-hot {
      cursor: pointer;
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: end;
      &:hover {
        .iconfont {
          color: #6973ff;
        }
      }
    }
  }
}
</style>
