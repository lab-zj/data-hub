import {
  whoAmIApi,
  userVerifyApi,
  userUpdateApi,
  userRegisterApi,
  userInitializeApi,
  userBindApi,
  userLoginApi,
  userLogoutApi,
  dingUserBindApi,
  dingUserUnBindApi,
  dingUserUrlAuthApi,
  dingUserLoginApi,
  dingUserVerifyApi
} from '@/api/user'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import useListMode from '@/views/DataPage/hooks/useListMode'

export default function useUser() {
  const route = useRoute()
  const router = useRouter()
  const userStore = useUserStore()

  async function whoAmI() {
    const response = await whoAmIApi()
    const { code, data } = response.data
    if (code === 200) {
      userStore.setUser(data)
    }
  }

  async function userVerify(password: string) {
    const response = await userVerifyApi({
      data: password
    })
    const { code } = response.data
    if (code === 200) {
      return 'success'
    }
    return 'failed'
  }

  async function userUpdate({ username, password }: { username?: string; password?: string }) {
    const response = await userUpdateApi({
      data: {
        username,
        password
      }
    })
    const { code } = response.data
    if (code === 200) {
      return 'success'
    }
    return 'failed'
  }

  async function userRegister({ username, password }: { username: string; password: string }) {
    const response = await userRegisterApi({
      data: {
        username,
        password
      }
    })
    const { code } = response.data
    if (code === 200) {
      ElMessage.success('注册成功！')
      router.push('/')
    } else {
      ElMessage.error(response.data.message || '注册失败，请稍候再试')
    }
  }

  async function userInitialize() {
    const response = await userInitializeApi({
      data: {}
    })
    const { code } = response.data
    if (code === 200) {
      return 'success'
    }
    return 'failed'
  }

  async function userBind({ username, password }: { username: string; password: string }) {
    const response = await userBindApi({
      data: {
        username,
        password
      }
    })
    const { code } = response.data
    if (code === 200) {
      return 'success'
    }
    return 'failed'
  }

  async function userLogin({ username, password, rememberMe }: { username: string; password: string; rememberMe: boolean }) {
    try {
      const response = await userLoginApi({
        data: {
          username,
          password,
          'remember-me': rememberMe
        }
      })
      if (response.status >= 200 && response.status < 300) {
        ElMessage.success('用户登录成功！')
        router.push('/management/projects')
      } else {
        ElMessage.error(response.data.message)
      }
    } catch (error: any) {
      // const errorMessage = error.response?.data?.error || '登录失败，请稍候再试'
      const errorMessage = '用户名或者密码错误，请重新登录！'
      ElMessage.error(errorMessage)
    }
  }

  async function userLogout() {
    const response = await userLogoutApi()
    if (response.data === 'logout success') {
      router.push('/login')
      // 列表视图模式切换
      const { resetMode } = useListMode()
      resetMode()
      ElMessage.success({
        message: '您已退出登录',
        duration: 500
      })
    }
  }

  async function dingUserUrlAuth() {
    const response = await dingUserUrlAuthApi()
    const { code, data } = response.data
    if (code === 200) {
      return data
    }
    return null
  }

  async function dingUserLogin({ authCode, state }: { authCode: string; state: string }) {
    const response = await dingUserLoginApi({
      data: {
        authCode,
        state
      }
    })
    return response.data
  }

  async function dingUserBind({ authCode, state }: { authCode: string; state: string }) {
    const response = await dingUserBindApi({
      data: {
        authCode,
        state
      }
    })
    const { code, message } = response.data
    if (code === 200) {
      return 'success'
    } else {
      ElMessage.error(message)
      return 'failed'
    }
  }

  async function dingUserUnBind() {
    const response = await dingUserUnBindApi()
    const { code } = response.data
    if (code === 200) {
      ElMessage.success('验证成功，完成解绑！')
      return 'success'
    }
    ElMessage.error('解绑失败！')
    return 'failed'
  }

  async function dingUserVerify({ authCode, state }: { authCode: string; state: string }) {
    const response = await dingUserVerifyApi({
      data: {
        authCode,
        state
      }
    })
    const { code } = response.data
    if (code === 200) {
      return 'success'
    }
    return 'failed'
  }

  /**
   * dingding操作流程
   * 点击dingding图标，发起扫码，扫码成功之后重定向到某个页面
   * 这一步操作主要是为了获取authCode和state，之后还需要在重定向页面上发起具体的登录、验证、解绑、修改等操作
   */
  async function dingVerifyFlow(_uri: string) {
    const url = await dingUserUrlAuth()
    const { protocol, host } = window.location
    if (url) {
      const redirect_uri = encodeURIComponent(`${protocol}//${host}${_uri}`)
      window.location.href = `${url}&redirect_uri=${redirect_uri}`
      // 接下来需要在重定向的页面上，发起dingding登录请求或者dingding绑定请求
    }
  }

  /**
   * 在重定向页面上，发起dingding登录
   */
  async function dingLoginOnRedirectPage() {
    const authCode: any = route.query?.authCode
    const state: any = route.query?.state
    if (authCode) {
      const response = await dingUserLogin({
        authCode,
        state
      })
      if (!response) {
        whoAmI()
      }
    } else {
      whoAmI()
    }
  }

  /**
   * 在重定向页面上，发起verify，验证通过之后，继续其他操作
   */
  async function dingVerifyOnRedirectPage(callback?: Function) {
    const authCode: any = route.query?.authCode
    const state: any = route.query?.state
    if (authCode) {
      const response = await dingUserVerify({
        authCode,
        state
      })
      if (response === 'success' && callback) {
        callback()
      }
    }
  }
  /**
   * dingding登录成功之后，绑定已有用户
   * @param param0
   */
  async function dingBindExistAccountFlow({ username, password }: { username: string; password: string }) {
    const result = await userBind({ username, password })
    // 绑定成功
    if (result === 'success') {
      // 切换登录用户
      await userLogout()
      await userLogin({
        username,
        password,
        rememberMe: false
      })
      await whoAmI()
      return 'success'
    } else {
      ElMessage.error('绑定失败！')
      return 'failed'
    }
  }

  /**
   * 解绑钉钉
   * 1、使用用户密码验证身份
   * 2、身份验证成功进行解绑
   * @param password
   */
  async function unbindDingdingWithPasswordVerifyFlow(password: string, callback?: Function) {
    const result = await userVerify(password)
    if (result === 'success') {
      await dingUserUnBind()
      await whoAmI()
      if (callback) {
        callback()
      }
    } else {
      ElMessage.error('密码错误！')
    }
  }

  /**
   * 平台用户登录成功之后，主动绑定钉钉
   * 1、使用用户密码验证身份
   * 2、身份验证成功之后，发起dingding扫码绑定
   * @param password
   * @param callback
   */
  async function bindDingdingWithPasswordVerifyFlow(password: string, callback?: Function) {
    const result = await userVerify(password)
    if (result === 'success') {
      dingVerifyFlow('/user-center/config?operate=bindOrUnbind')
      if (callback) {
        callback()
      }
    } else {
      ElMessage.error('密码错误！')
    }
  }

  /**
   * 在重定向页面上，发起dingding绑定
   */
  async function dingBindOnRedirectPage() {
    const authCode: any = route.query?.authCode
    const state: any = route.query?.state
    if (authCode) {
      const result = await dingUserBind({
        authCode,
        state
      })
      if (result === 'success') {
        ElMessage.success('验证成功，绑定成功！')
        await whoAmI()
        // 绑定成功之后，去掉url中的authCode和state，避免发起重复绑定请求
        router.replace(`/user-center/config?timestamp=${new Date().getTime()}`)
      } else {
        ElMessage.error('绑定失败！')
      }
    }
  }

  /**
   * 在重定向页面上，发起dingding解绑
   */
  async function dingUnBindOnRedirectPage() {
    const result = await dingUserUnBind()
    if (result === 'success') {
      await whoAmI()
      // 解绑成功之后，去掉url中的authCode和state，避免发起重复解绑请求
      router.replace(`/user-center/config?timestamp=${new Date().getTime()}`)
    } else {
      ElMessage.error('解绑失败！')
    }
  }

  /**
   * 使用平台用户验证身份流程
   * @param password
   * @param callback
   */
  async function useVerifyWithPasswordCommonFlow(password: string, callback?: Function) {
    const result = await userVerify(password)
    if (result === 'success') {
      if (callback) {
        callback()
      }
    } else {
      ElMessage.error('密码错误！')
    }
  }

  /**
   * 修改用户名
   * @param password
   * @param callback
   */
  async function modifyUsernameFlow(username: string, callback?: Function) {
    const result = await userUpdate({
      username
    })
    if (result === 'success') {
      ElMessage.success('用户名修改成功！')
      await whoAmI()
      if (callback) {
        callback()
      }
    } else {
      ElMessage.error('用户名修改失败！')
    }
  }

  /**
   * 修改密码
   * @param password
   * @param callback
   */
  async function modifyPasswordFlow(password: string, callback?: Function) {
    const result = await userUpdate({
      password
    })
    if (result === 'success') {
      ElMessage.success('密码修改成功！')
      if (callback) {
        callback()
      }
    } else {
      ElMessage.error('密码修改失败！')
    }
  }

  return {
    whoAmI,
    userVerify,
    userUpdate,
    userRegister,
    userBind,
    userInitialize,
    userLogin,
    userLogout,
    dingUserUrlAuth,
    dingUserLogin,
    dingUserBind,
    dingUserUnBind,
    dingUserVerify,
    dingVerifyFlow,
    dingLoginOnRedirectPage,
    dingVerifyOnRedirectPage,
    dingBindExistAccountFlow,
    unbindDingdingWithPasswordVerifyFlow,
    bindDingdingWithPasswordVerifyFlow,
    dingBindOnRedirectPage,
    dingUnBindOnRedirectPage,
    useVerifyWithPasswordCommonFlow,
    modifyUsernameFlow,
    modifyPasswordFlow
  }
}
