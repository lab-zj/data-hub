import axios from '@/api/http'

/**
 * 当前目录基本信息
 * @param path
 * @returns
 */
export const getMetaApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/basic/meta'
  })

/**
 * 预览文本信息
 * @param path
 * @returns
 */
export const txtPreviewApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/preview/text'
  })

/**
 * 预览图片信息
 * @param path
 * @returns
 */
export const imagePreviewApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/preview/image',
    responseType: 'blob'
  })

/**
 * 预览csv数据表格信息
 * @param path
 * @returns
 */
export const csvPreviewApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/preview/csv'
  })

/**
 * 预览虚拟文件信息(虚拟文件自身信息比如数据库连接信息等)
 * @param options
 * @returns
 */
export const virtualFilePreviewApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/virtual/file/'
  })

/**
 * 预览存储在远程机器上的文件内容
 * 1、远程s3存储的某个文件
 * 2、mysql/postgresql数据库中的某个文件
 */
export const virtualRemoteFilePreviewApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/virtual/file/preview'
  })
