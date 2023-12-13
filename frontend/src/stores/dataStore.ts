import { defineStore } from 'pinia'

type IDataListMode = 'table' | 'card'

export const useDataStore = defineStore({
  id: 'data',
  state: () => ({
    mode: 'table' as IDataListMode
  }),
  getters: {
    getMode: (state: any): any => {
      return state.mode
    }
  },
  actions: {},
  persist: true
})
