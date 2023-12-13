<template>
  <div class="user-config-container">
    <el-container>
      <el-aside width="200px" class="aside">
        <div class="active-line" :class="active === 'account' ? 'account-pos' : 'system-pos'"></div>
        <ul class="tabs" @click="(event) => onTab(event)">
          <li class="item active">账号管理</li>
          <li class="item" style="cursor: not-allowed; color: #e9e9e9">系统设置</li>
        </ul>
      </el-aside>
      <el-main class="main account_main" v-if="active === 'account'">
        <div class="title">
          <span>账号管理</span>
        </div>
        <div class="profile">
          <img :src="avatarUrl" />
        </div>
        <div class="info">
          <div class="label">用户名</div>
          <div class="row">
            <span>{{ username }}</span>
            <span class="operate-btn" @click="modifyUserName">更改</span>
          </div>
        </div>
        <div class="info">
          <div class="label">登录密码</div>
          <div class="row">
            <span>{{ username ? '***********' : '' }}</span>
            <span class="operate-btn" @click="modifyPassword">更改</span>
          </div>
        </div>
        <div class="info">
          <div class="label">第三方绑定</div>
          <div class="row">
            <img src="@/assets/dingding.png" :class="isDingBind ? 'dingding' : 'dingding gray-filter'" />
            <span class="operate-btn" @click="handleBind">{{ isDingBind ? '解绑' : '绑定' }}</span>
          </div>
        </div>
      </el-main>
      <el-main class="main system_main" v-else>
        <div class="title">
          <span>系统管理</span>
        </div>
        <div class="info">
          <div class="label">界面语言</div>
          <div class="row">
            <span>选择见微可视分析平台界面语言：</span>
            <el-select v-model="langConfig" size="small">
              <el-option value="zh_CN" label="中文" />
              <el-option value="en_US" label="英文" />
            </el-select>
          </div>
        </div>
        <div class="info">
          <div class="label">消息设置</div>
          <div class="row">
            <el-radio-group v-model="msgConfig">
              <el-radio label="接受全部消息通知" />
              <el-radio label="只接收@我的消息通知" />
              <el-radio label="不接受消息通知" />
            </el-radio-group>
          </div>
        </div>
        <div class="info">
          <div class="label">二次确认设置</div>
          <div class="row">
            <span>数据视图中删除节点要二次确认</span>
            <el-switch v-model="delConfig"></el-switch>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
  <UserVerify v-if="visible" :visible="visible" :operate="operate" @close="visible = false" @confirm="confirm" />
  <UserModify v-if="modifyVisible" :visible="modifyVisible" :title="title" :operate="operate" @close="modifyVisible = false" @confirm="modifyConfirm" />
</template>
<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import UserVerify from './UserVerify.vue'
import UserModify from './UserModify.vue'
import useUser from '@/components/hooks/useUser'
import { ElMessage } from 'element-plus'
import router from '@/router'

const route = useRoute()
const userStore = useUserStore()
const {
  whoAmI,
  dingBindOnRedirectPage,
  dingUnBindOnRedirectPage,
  unbindDingdingWithPasswordVerifyFlow,
  bindDingdingWithPasswordVerifyFlow,
  useVerifyWithPasswordCommonFlow,
  modifyUsernameFlow,
  modifyPasswordFlow,
  dingVerifyOnRedirectPage
} = useUser()

const active: any = ref('account') // 当前选中的tab
const visible: any = ref(false)
const operate: any = ref('bindOrUnbind') // ding、username、password
const modifyVisible: any = ref(false)
const title: any = ref('')

const langConfig: any = ref('zh_CN')
const msgConfig: any = ref('接受全部消息通知')
const delConfig: any = ref(false)

const username = computed(() => {
  return userStore?.username || ''
})

const isDingBind = computed(() => {
  return userStore?.user?.dingTalkUserInfo?.openId
})

const avatarUrl = computed(() => {
  return userStore.avatarUrl
})

function onTab(event: any) {
  // active.value = event.target.innerText === '账号管理' ? 'account' : 'system'
  active.value = 'account'
}

function handleBind() {
  operate.value = 'bindOrUnbind'
  visible.value = true
}

function modifyUserName() {
  operate.value = 'username'
  visible.value = true
}

function modifyPassword() {
  operate.value = 'password'
  visible.value = true
}

/**
 * 使用平台密码验证，在进行绑定、解绑、修改用户名/密码的操作
 * @param password
 */
function confirm(password: string) {
  if (!password) {
    ElMessage.error('请输入密码进行验证！')
    return
  }
  switch (operate.value) {
    // 绑定或者解绑钉钉用户
    case 'bindOrUnbind':
      if (isDingBind.value) {
        unbindDingdingWithPasswordVerifyFlow(password, () => {
          // 关掉身份验证窗口
          visible.value = false
        })
      } else {
        bindDingdingWithPasswordVerifyFlow(password)
      }
      break
    // 修改用户名
    case 'username':
      useVerifyWithPasswordCommonFlow(password, () => {
        // 关掉身份验证窗口
        visible.value = false
        modifyVisible.value = true
        title.value = '用户名更改'
      })
      break
    // 修改密码
    case 'password':
      useVerifyWithPasswordCommonFlow(password, () => {
        // 关掉身份验证窗口
        visible.value = false
        modifyVisible.value = true
        title.value = '修改密码'
      })
      break
    default:
  }
}

async function modifyConfirm({ username, passwordf, passwords }: { username: string; passwordf: string; passwords: string }) {
  if (operate.value === 'username') {
    modifyUsernameFlow(username, async () => {
      // 用户名修改成功，关闭弹框
      modifyVisible.value = false
      // 修改成功之后，去掉url中的authCode和state，避免发起重复绑定请求
      router.replace(`/user-center/config?timestamp=${new Date().getTime()}`)
    })
  } else if (operate.value === 'password') {
    if (!passwordf || !passwords) {
      ElMessage.error('请输入密码进行验证！')
      return
    } else if (passwordf !== passwords) {
      ElMessage.error('两次输入的密码不一致！请重新输入')
      return
    } else {
      modifyPasswordFlow(passwordf, () => {
        // 密码修改成功，关闭弹框
        modifyVisible.value = false
        // 修改成功之后，去掉url中的authCode和state，避免发起重复绑定请求
        router.replace(`/user-center/config?timestamp=${new Date().getTime()}`)
      })
    }
  }
}

onMounted(async () => {
  await whoAmI()
  doInit()
})

/**
 * 用户使用dingding验证，重定向到该页面，继续验证、解绑、修改等操作
 * 1、发起dingding扫码获取authCode、state，然后重定向到该页面
 * 2、在该页面根据operate的类型，继续操作
 */
function doInit() {
  const _operate = route.query?.operate
  if (!_operate) {
    return
  }
  if (_operate === 'bindOrUnbind') {
    // 直接使用平台用户密码验证之后，进行dingding绑定或者解绑
    if (isDingBind.value) {
      dingUnBindOnRedirectPage()
    } else {
      dingBindOnRedirectPage()
    }
  } else if (_operate === 'ding') {
    // 发起dingding验证,进行解绑或者绑定操作
    dingVerifyOnRedirectPage(() => {
      if (isDingBind.value) {
        dingUnBindOnRedirectPage()
      } else {
        dingBindOnRedirectPage()
      }
    })
  } else if (['username', 'password'].includes(_operate)) {
    // 发起dingding验证
    dingVerifyOnRedirectPage(() => {
      operate.value = _operate
      modifyVisible.value = true
      title.value = _operate === 'username' ? '用户名更改' : _operate === 'password' ? '修改密码' : ''
    })
  }
}
</script>

<style lang="less" scoped>
.user-config-container {
  background-color: #f3f3f7;
  height: calc(100% - 54px - 64px);
  padding: 12px;
  .aside {
    background-color: #fff;
    margin-right: 12px;
    position: relative;
    height: 100px;
    overflow: hidden;
    .active-line {
      width: 4px;
      height: 50px;
      background-color: #6973ff;
      position: absolute;
      top: 0;
    }
    .account-pos {
      top: 0;
    }
    .system-pos {
      top: 50px;
    }
    .tabs {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
      .item {
        height: 50px;
        display: flex;
        width: 100%;
        justify-content: center;
        align-items: center;
        font-size: 16px;
        color: #222432;
        cursor: pointer;
      }
      .item:first-child {
        border-bottom: 1px solid #e9e9e9;
      }
      .active {
        color: #373b52;
      }
    }
  }
  .main {
    background-color: #fff;
    padding: 0;
    .title {
      height: 50px;
      display: flex;
      align-items: center;
      border-bottom: 1px solid #e9e9e9;
      padding-left: 24px;
      span {
        color: #5d637e;
        font-size: 16px;
      }
    }
    .profile {
      padding: 24px 32px 4px;
      img {
        width: 88px;
        height: 88px;
        border-radius: 50%;
      }
    }
    .info {
      padding: 20px 41px;
      border-bottom: 1px solid #f3f3f7;
      .label {
        font-weight: 600;
        font-size: 14px;
        color: #373b52;
        margin-bottom: 12px;
      }
      .row {
        font-weight: 400;
        font-size: 14px;
        color: #5d637e;
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 50%;
      }
      .dingding {
        width: 35px;
      }
      .gray-filter {
        filter: grayscale(100%);
      }
      .operate-btn {
        cursor: pointer;
        color: #6973ff;
      }
    }
  }
  .account_main {
    .row {
      width: 50%;
    }
  }
  .system_main {
    .row {
      width: 100% !important;
    }
  }
}
</style>
