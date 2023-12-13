<template>
  <i class="iconfont">
    <svg v-bind="svgBaseProps" :class="classList" aria-hidden="true">
      <use :xlink:href="iconName" :fill="color" />
    </svg>
  </i>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps({
  className: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    required: true
  },
  color: {
    type: String,
    default: ''
  }
})
const svgBaseProps = {
  width: '1em',
  height: '1em',
  fill: 'currentColor',
  'aria-hidden': 'true',
  focusable: 'false'
}
const classList = computed(() => {
  return ['icon', props.className || '']
})
const iconName = computed(() => {
  return `#${props.type}`
})
</script>

<style scoped>
.iconfont {
  /** 通用的样式，视后续需要再添加 */
  display: inline-block;
  text-align: center;
  vertical-align: -0.125em;
}
</style>
