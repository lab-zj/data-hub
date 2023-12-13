import { defineStore } from 'pinia'

export const useDetailStore = defineStore({
  id: 'detail',
  state: () => ({
    fromPath: '' // 从哪里进入详情页
  }),
  getters: {
    
  },
  actions: {
    setFromPath(path: string) {
      this.fromPath = path
    }
  }
})

