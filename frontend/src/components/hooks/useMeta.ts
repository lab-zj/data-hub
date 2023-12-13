import { ref, Ref } from 'vue'
import moment from 'moment'
import { getMetaApi } from '@/api/preview'
import { getVirtualFileMetaApi } from '@/api/virtual'
import { compatible } from '@/util/util'

interface IMETA {
  name: string
  path: string
  size: number
  fileType: string // 后端划分的文件分类
  creatTimestamp: any
  updateTimestamp: any
  createTime?: any
  updateTime?: any
  type?: string // 文件后缀
}

export default function useMeta() {
  const meta: Ref<IMETA> = ref({
    name: '',
    path: '',
    size: 0,
    fileType: '-',
    creatTimestamp: null,
    updateTimestamp: null,
    createTime: '-',
    updateTime: '-',
    type: ''
  })

  /**
   * 清空预览内容
   */
  const reset = () => {
    meta.value = {
      name: '',
      path: '',
      size: 0,
      fileType: '-',
      creatTimestamp: null,
      updateTimestamp: null,
      createTime: '-',
      updateTime: '-',
      type: ''
    }
  }

  /**
   * 获取目录基本信息
   * @param path
   */
  const getMeta = async (path: string, callback?: Function) => {
    if (!path) {
      return
    }
    const requestApi = path.includes('.s3:') ? getVirtualFileMetaApi : getMetaApi
    const requestPath = path.includes('.s3:') ? path.split(':')[0] : path
    const response = await requestApi({
      data: {
        path: compatible(requestPath)
      }
    })
    const { code, data, message } = response.data
    if (code === 200) {
      meta.value = data
      meta.value.createTime = data?.creatTimestamp ? moment(data?.creatTimestamp).format('YYYY-MM-DD HH:mm:ss') : '-'
      meta.value.updateTime = data?.updateTimestamp ? moment(data?.updateTimestamp).format('YYYY-MM-DD HH:mm:ss') : '-'
      meta.value.type = data.name.split('.').slice(-1).toString()

      if (callback) {
        callback()
      }
    } else {
      console.error(message)
      meta.value = {
        name: '',
        path: '',
        size: 0,
        fileType: '-',
        creatTimestamp: null,
        updateTimestamp: null,
        createTime: '-',
        updateTime: '-',
        type: ''
      }
    }
  }

  return {
    meta,
    getMeta,
    reset
  }
}
