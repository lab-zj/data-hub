import { defineStore } from 'pinia'
import { fetchProjectApi } from '@/api/project'

export const useProjectInfo = defineStore('projectInfo', {
  state: () => ({
    projectName: ''
  }),
  actions: {
    async fetchProjectInfo(projectId: string) {
      // todo fetch api
      const response = await fetchProjectApi(projectId)
      const { code, data } = response.data
      if (code === 200) {
        this.projectName = data.name
      }
    }
  }
})
