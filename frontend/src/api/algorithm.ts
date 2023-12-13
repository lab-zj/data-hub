import axios from '@/api/http'

// 上传
export const uploadAlgorithm = (options?: any) =>
  axios.request({
    ...options,
    url: '/algorithm'
  })

// 打包
export const releaseAlgorithm = (options: any) =>
  axios.request({
    method: 'POST',
    url: `/algorithm/${options.algorithmId}/release`
  })

// 部署
export const deployAlgorithm = (options?: any) =>
  axios.request({
    method: 'POST',
    url: `/algorithm/${options.algorithmId}/deploy`
  })

// 通过algorithmId查询businessAlgorithm
export const getBusinessAlgorithmById = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/algorithm/algorithmid'
  })

// 获取当前用户下的所有算法服务列表
export const getMyAlgorithm = () => axios.get('/algorithm/v1/all/deployed')

// 查询base image 列表
export const getBaseImageList = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/docker/registry/all'
  })

// 查看服务类型列表
export const getApplicationTypeList = () => axios.get('/algorithm/application/type/all')

// 调用算法
export const callAlgorithm = (options?: any) =>
  axios.request({
    ...options
  })

// 查看算法状态
export const getAlgorithmState = (options?: any) => axios.get(`/algorithm/${options.algorithmId}/${options.type}`)

// 自定义算法名是否存在
export const algorithmNameExist = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/algorithm/info/exist'
  })

// 查看用户账号下已部署的算法
export const getAllRelatedAlgorithmApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/algorithm/all/search'
  })

/**
 * delete algorithm
 * @param
 * @returns
 */
export const deleteAlgorithmApi = (businessAlgorithmId: any) =>
  axios.request({
    method: 'DELETE',
    url: `/algorithm/${businessAlgorithmId}`
  })

export default {}
