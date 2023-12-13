import axios from '@/api/http'

// 展示当前目录文件list
export const queryCategoryList = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/basic/directory/list/meta'
  })

// 搜索
export const searchFile = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/basic/search/meta'
  })

// 文件（夹）批量是否重名
export const fileBatchExist = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/basic/exists/batch'
  })

// 重名后获取新的名字
export const forceRename = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: `/filesystem/basic/directory/name/suggest`
  })

// 批量删除
export const batchDelete = (options?: any) =>
  axios.request({
    ...options,
    method: 'DELETE',
    url: `/filesystem/basic/delete/batch`
  })

// 创建
export const createFolder = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    headers: {
      'Content-Type': 'text/plain'
    },
    url: '/filesystem/basic/directory/create'
  })

// 移动单个文件夹或者重命名
export const rename = (options?: any) =>
  axios.request({
    ...options,
    url: '/filesystem/basic/move'
  })

// 批量移动文件夹
export const batchMove = (options?: any) =>
  axios.request({
    ...options,
    url: '/filesystem/basic/move/batch'
  })

// 批量下载
export const batchDownload = (options?: any) =>
  axios.request({
    ...options,
    url: '/filesystem/batch/download'
  })

// 创建http连接文件、数据库连接文件
export const createVirtualFile = (options?: any) =>
  axios.request({
    ...options,
    url: '/filesystem/file/virtualFile'
  })

// 展示算法目录文件list
export const queryAlgorithmCategoryList = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/basic/directory/list/meta/algorithm'
  })

export default {}
