import { ref } from 'vue'
import { fileBatchExist, batchDelete, rename, batchMove } from '@/api/data'
import { virtualDownLoadApi } from '@/api/virtual'
import { ElMessage } from 'element-plus'
import { compatible, getFileNameFromPath } from '@/util/util'

/**
 * 文件常见操作
 * @returns
 */
export default function useFile() {
  const renameLoading: any = ref(false)
  const moveLoading: any = ref(false)

  /**
   * 文件同名批量检测
   * @param pathList
   */
  const fileBatchExistCheck = async (pathList: string[]) => {
    const response = await fileBatchExist({
      data: {
        pathList: pathList.join(',')
      }
    })
    const { code, data, message } = response.data
    if (code === 200) {
      // {path: true/false} 存在：true，不存在：false
      return data
    } else {
      console.error(message)
    }
  }

  /**
   * 批量删除
   */
  const fileDelete = async (sourcePathList: any, callback?: Function) => {
    const response = await batchDelete({
      data: sourcePathList
    })

    const { code } = response.data
    if (code === 200) {
      ElMessage.success('删除成功！')
    } else {
      console.error('删除失败！')
    }
    if (callback) {
      callback()
    }
  }

  /**
   * 文件下载
   * 单文件下载: path
   * 多文件下载: [path1,path2]
   */
  const fileDownload = (params: any) => {
    // 单文件下载
    if (typeof params === 'string') {
      // s3文件远程下载
      if (params.includes('.s3:')) {
        virtualFileDownload(params)
      } else {
        const path = compatible(params)
        window.open(`/api/filesystem/basic/download/batch?pathList=${encodeURIComponent(path)}`)
      }
    } else {
      // 多文件下载
      const _pathList = params.map((item: any) => {
        return encodeURIComponent(item)
      })
      window.open(`/api/filesystem/basic/download/batch?pathList=${_pathList.join(',')}`)
    }
  }

  /**
   * 虚拟文件下载（目前只支持s3）
   * @param params
   */
  const virtualFileDownload = async (params: any) => {
    const [path, filePath] = params.split(':')
    const response = await virtualDownLoadApi({
      data: {
        path
      }
    })
    if (response.data) {
      const blob = new Blob([response.data], {
        type: 'application/octet-stream;charset=utf-8'
      })
      const downloadName = getFileNameFromPath(filePath)
      const element = document.createElement('a')
      element.setAttribute('href', URL.createObjectURL(blob))
      element.setAttribute('download', downloadName)
      element.style.display = 'none'
      element.click()
    }
  }

  /**
   * 修改文件名
   */
  const fileRename = async (sourcePath: string, targetPath: string, callback?: Function) => {
    renameLoading.value = true
    const response = await rename({
      data: {
        sourcePath,
        targetPath,
        override: true
      }
    })
    const { code, message } = response.data
    if (code === 200) {
      if (callback) {
        callback()
      }
    } else {
      console.error(message)
    }
  }

  /**
   * 移动文件
   * @param movePath
   */
  const fileMove = async (sourcePathList: any, movePath: any, callback?: Function) => {
    moveLoading.value = true

    const response: any = await batchMove({
      data: {
        sourcePathList,
        targetPath: `${movePath}/`,
        override: true
      }
    })
    moveLoading.value = false

    if (response.data.code === 200) {
      ElMessage.success('移动成功！')
      if (callback) {
        callback()
      }
    } else {
      ElMessage.error(response.data.message)
    }
  }

  return {
    fileBatchExistCheck,
    fileDelete,
    fileDownload,
    fileRename,
    renameLoading,
    fileMove,
    moveLoading
  }
}
