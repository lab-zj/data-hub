import axios from '@/api/http'
import * as qs from 'qs'

/**
 * save graph info
 * @param
 * @returns
 */
export const saveGraphApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/graph'
  })

/**
 * stop graph job
 * @param
 * @returns
 */
export const stopGraphJobApi = (graphUuid?: any) =>
  axios.request({
    method: 'POST',
    url: `/graph/${graphUuid}/stop`
  })

/**
 * cancel graph job
 * @param
 * @returns
 */
export const cancelGraphJobApi = (graphUuid?: any) =>
  axios.request({
    method: 'POST',
    url: `/graph/${graphUuid}/cancel`
  })

/**
 * execute graph job asynchronously
 * @param
 * @returns
 */
export const executeGraphApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/graph/execute'
  })

/**
 * get my graph list
 * @param
 * @returns
 */
export const getGraphListApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/graph/all'
  })

/**
 * get graph info
 * @param
 * @returns
 */
export const getGraphApi = (graphUuid?: any) =>
  axios.request({
    method: 'GET',
    url: `/graph/${graphUuid}`
  })

/**
 * get graph job status
 * @param
 * @returns
 */
export const getGraphJobStatusApi = (graphUuid?: any) =>
  axios.request({
    method: 'GET',
    url: `/graph/${graphUuid}/status`
  })

/**
 * batch get info of task node details
 * @param projectId
 * @returns
 */
export const getGraphTaskDetailsBatchApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/graph/task/details/batch',
    // 序列化数组格式
    paramsSerializer: function (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  })

/**
 * update or add a task node
 * @param
 * @returns
 */
export const upsertTaskNodeApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/graph/task'
  })

/**
 * batch get info of task nodes
 * @param
 * @returns
 */
export const getTaskNodeBatchApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/graph/task/batch'
  })

/**
 * batch delete task nodes
 * @param
 * @returns
 */
export const deleteTaskBatchApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'DELETE',
    url: '/graph/task/batch',
    // 序列化数组格式
    paramsSerializer: function (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  })

/**
 * stop tasks by specifying task id list
 * @param
 * @returns
 */
export const stopTaskBatchApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/graph/task/build/batch/stop'
  })

/**
 * get task build info
 * @param
 * @returns
 */
export const settTaskBuildBatchInitApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/graph/task/runtime/batch/init',
    // 序列化数组格式
    paramsSerializer: function (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  })

/**
 * batch delete graph
 * @param options
 * @returns
 */
export const cleanGraphApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'DELETE',
    url: '/graph/batch',
    // 序列化数组格式
    paramsSerializer: function (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  })

/**
 * fetch sql table options
 * @param options
 * @returns
 */
export const graphTablesApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/graph/tables'
  })

/**
 * get graph list of project
 * @param options
 * @returns
 */
export const getGraphListOfProjectApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/graph/project/all'
  })
