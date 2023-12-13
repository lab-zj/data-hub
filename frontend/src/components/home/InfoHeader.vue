<template>
  <div class="icons">
    <el-dropdown
      :get-popup-container="(triggerNode: any) => triggerNode.parentNode"
      @visibleChange="
        (visible: any) => {
          dropdownIconType = visible ? 'up' : 'down'
        }
      "
    >
      <div class="user-info-container">
        <el-avatar :src="avatarUrl" />
        <span class="name">{{ username }}</span>
        <el-icon class="arrow"><ArrowUp v-if="dropdownIconType === 'up'" /><ArrowDown v-else /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <!-- <el-dropdown-item disabled>用户名</el-dropdown-item> -->
          <el-dropdown-item class="logout-item" @click="setUser"> <icon-font type="icon-zhanghaoshezhi" class="config-iconfont" /><span>设置</span> </el-dropdown-item>
          <el-dropdown-item class="logout-item" @click="userLogout"> <icon-font type="icon-tuichu" class="exit-iconfont" /><span>退出</span> </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

import { ref } from 'vue'
import useUser from '@/components/hooks/useUser'

const router = useRouter()
const userStore = useUserStore()
const { userLogout, whoAmI } = useUser()
const dropdownIconType: any = ref('down')
const username = computed(() => {
  return userStore.username
})
const avatarUrl = computed(() => {
  return userStore.avatarUrl
})

function setUser() {
  router.push('/user-center/config')
}

onMounted(() => {
  if (!username.value) {
    whoAmI()
  }
})
</script>

<style lang="less" scoped>
@import '@/constants';

.icons {
  display: inline-flex;
}

.user-info-container {
  align-items: center;
  cursor: pointer;
  display: flex;
  height: @HEADER_HEIGHT;
  padding: 0 15px;
  outline: none;

  .avatar {
    border-radius: 50%;
    font-size: 24px;
    margin-right: 8px;
    overflow: hidden;
    padding: 0;
  }
  .name {
    color: #fff;
    margin-left: 8px;
  }
  .arrow {
    margin-left: 8px;
    color: #fff;
  }
}
.config-iconfont,
.exit-iconfont {
  display: flex !important;
  justify-content: center;
  align-items: center;
}
</style>
