<template>
  <FirstDingLoginTips :visible="visible" @bind="bind" @close="visible = false" />
  <BindAccount :visible="bindVisible" @close="handleClose" @success-close="handleSuccessClose" />
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import FirstDingLoginTips from '@/views/UserCenter/components/FirstDingLoginTips.vue'
import BindAccount from '@/views/UserCenter/components/BindAccount.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const visible: any = ref(false)
const bindVisible: any = ref(false)

watch(
  () => userStore.user,
  () => {
    if (!userStore.user?.databaseUserInfo) {
      visible.value = true
    } else {
      visible.value = false
    }
  }
)

function bind() {
  visible.value = true
  bindVisible.value = true
}

function handleClose() {
  bindVisible.value = false
}

function handleSuccessClose() {
  visible.value = false
  bindVisible.value = false
}
</script>
<style lang="less"></style>
