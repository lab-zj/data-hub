import axios from '@/api/http'

/**
 * 新建虚拟文件
 * @param options
 * @returns
 */
export const createVirtualFileApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/filesystem/virtual/file/'
  })

/**
 * 连接测试
 * @param options
 * @returns
 */
export const connectTestFileApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'POST',
    url: '/filesystem/virtual/file/connection/test'
  })

/**
 * 虚拟文件meta信息（目前只支持s3）
 * @param options
 * @returns
 */
export const getVirtualFileMetaApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: `/filesystem/virtual/file/meta`
  })

/**
 * 虚拟文件下载（目前只支持s3）
 * @param options
 * @returns
 */
export const virtualDownLoadApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    headers: {
      'Content-Type': 'application/octet-stream'
    },
    url: '/filesystem/virtual/file/download'
  })
