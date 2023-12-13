<!-- dingding登录或者验证的入口 -->
<template>
  <img src="@/assets/dingding.png" class="dingding" @click="onClick" />
</template>
<script setup lang="ts">
import useUser from '@/components/hooks/useUser'

const props = defineProps({
  redirect: { type: String, default: '/management/projects' }
})

const { dingVerifyFlow } = useUser()

function onClick() {
  dingVerifyFlow(props.redirect)
}
</script>
<style lang="less" scoped>
.dingding {
  cursor: pointer;
  width: 35px;
}
</style>
