import axios from '@/api/http'

// 上传
export const uploadFile = (options?: any) =>
  axios.request({
    ...options,
    url: '/filesystem/basic/file/upload',
    headers: {
      'Content-Type': 'multipart/form-data;charset=utf-8;'
    }
  })

// 文件同名批量校验
export const isExistBatchApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: `/filesystem/basic/exists/batch`
  })

// 文件夹同名、且不覆盖的情况下，需要获取文件夹名称比如_1
export const directoryRenameApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/filesystem/basic/directory/name/suggest'
  })
export default {}
