import moment from 'moment'

export interface IDataItem {
  path: string
  name: string
  directory: boolean
  size: number
  creatTimestamp: number
  updateTimestamp: number
}

/**
 * 下载文件
 * @param val path string or path list
 */
export function downloadFile(val: string | string[]) {
  if (typeof val === 'string') {
    window.open(`/api/filesystem/basic/download/batch?pathList=${encodeURIComponent(val)}`)
  } else {
    const array = val.map((element: any) => {
      return encodeURIComponent(element)
    })
    window.open(`/api/filesystem/basic/download/batch?pathList=${array.join(',')}`)
  }
}

// 格式化时间戳
export const formatDate = (timestamp: number, format: string = 'YYYY-MM-DD HH:mm:ss') => {
  return timestamp === 0 ? '-' : moment(timestamp).format(format)
}

export function getFileJumpUrl(fileData: IDataItem, extraData?: { searchInput: string }) {
  if (!fileData.directory) {
    // 点击非文件夹跳转到详情页
    const path = encodeURIComponent(fileData.path)
    const { searchInput } = extraData || {}
    return `/management/detail/path=${path}${searchInput ? `?search=${searchInput}` : ''}`
  }
  // 点击文件夹进入到文件夹内
  const pathArray: any = fileData.path.split('/')
  pathArray.pop()
  return `/management/data/path=${encodeURIComponent(pathArray.join('/'))}`
}

// 根据文件名返回文件类型
export function getFileTypeOrIcon(name: string, isIcon: boolean) {
  let type: any = name
  if (name.split('.').length > 1) {
    type = name.split('.').pop()
  }
  if (isIcon) {
    switch (type) {
      case 'txt':
        return 'icon-wendang'
      case 'png':
        return 'icon-tupian'
      case 'mp4':
        return 'icon-shipin'
      case 'csv':
        return 'icon-shuju'
      case 'ps':
      case 'my':
      case 's3':
        return 'icon-xuniwenjian'
      case 'directory':
        return 'icon-a-wenjianjiashouqibeifen2'
      default:
        return 'icon-feichangguiwenjian'
    }
  }
  return ''
}
