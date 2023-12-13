import axios from '@/api/http'
import * as qs from 'qs'

// fetch project list
export const fetchListApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/project/all'
  })

// modify project
export const modifyProjectApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/project'
  })

// delete project
export const deleteProjectApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'DELETE',
    url: '/project/batch',
    // 序列化数组格式
    paramsSerializer: function (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  })

// fetch project info
export const fetchProjectApi = (projectId?: any) =>
  axios.request({
    method: 'GET',
    url: `/project/${projectId}`
  })
