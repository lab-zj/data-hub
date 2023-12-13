import { defineStore } from 'pinia'

export const useUserStore = defineStore({
  id: 'user',
  state: () => ({
    user: null // 当前用户
  }),
  getters: {
    // 获取当前登录用户的用户名
    username: (state: any): any => {
      return state.user?.databaseUserInfo?.username
    },
    // 用户头像
    avatarUrl: (state: any): any => {
      return state.user?.universalUser?.avatarUrl || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    },
    // 当前用户是不是有效用户
    validUser: (state: any): any => {
      return state.user?.databaseUserInfo?.username || state.user?.dingTalkUserInfo?.openId || state.user?.universalUser?.id
    }
  },
  actions: {
    setUser(_user: any) {
      this.user = { ..._user }
    }
  }
})
