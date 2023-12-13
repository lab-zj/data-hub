import { ElMessage } from 'element-plus'

/**
 * 根据文件后缀获取iconfont type
 * @param suffix file suffix
 * @returns iconfont type
 */
export function getFileIconFont(suffix: string) {
  switch (suffix) {
    case 'txt':
      // case 'pdf':
      return 'icon-wendang'
    case 'png':
    case 'jpg':
    case 'jpeg':
      return 'icon-tupian'
    case 'mp4':
      return 'icon-shipin'
    case 'csv':
      return 'icon-shuju'
    case '/':
      return 'icon-a-wenjianjiashouqibeifen2' // 文件夹
    case 'ps':
    case 'my':
    case 's3':
      return 'icon-xuniwenjian' // 虚拟文件
    default:
      return 'icon-feichangguiwenjian'
  }
}
/**
 * 获取文件类型
 */
export function getFileType(directory: boolean, name: string) {
  if (directory) {
    return '/'
  }
  const name_list = name.split('.')
  const type = name_list.slice(-1)[0]
  return ['png', 'txt', 'mp4', 'csv', 's3', 'my', 'ps'].includes(type) ? type : 'unknown'
}

/**
 * 文件大小转换
 */
export const sizeConversion = (size: number) => {
  if (typeof size !== 'number') {
    return
  }
  if (size < 1024) {
    return `${size}B`
  } else if (size < 1024 * 1024) {
    return `${Math.ceil(size / 1024)}K`
  } else if (size < 1024 * 1024 * 1024) {
    return `${Math.ceil(size / (1024 * 1024))}M`
  } else if (size < 1024 * 1024 * 1024 * 1204) {
    return `${Math.ceil(size / (1024 * 1024 * 1024))}G`
  } else {
    return `${Math.ceil(size / (1024 * 1024 * 1024 * 1024))}T`
  }
}

/**
 * 从路径中获取文件名
 * @param path
 */
export const getFileNameFromPath = (path: string) => {
  return path.split('/').slice(-1).join('')
}

/**
 * 获取文件名称（不包括后缀）比如1.txt
 * @param name
 * @returns
 */
export const getFileName = (name: string) => {
  const list = name.split('.')
  return list.slice(0, list.length - 1).join('.')
}

/**
 * 百分号无法进行decode
 */
export const compatible = (path: string) => {
  return path.includes('%') ? path : decodeURIComponent(path)
}

/**
 * 获取当前记录的类型
 * @param record
 */
export const getRecordType = (record: any) => {
  const type = record.directory ? 'folder' : record.name && record.name.split('.').pop()
  switch (type) {
    case 'folder':
    case 'txt':
    case 'csv':
      return type
    case 'mp4':
      return 'video'
    case 'png':
      return 'image'
    case 's3':
    case 'my':
    case 'ps':
      return 'virtual'
    default:
      return 'unknown'
  }
}

/**
 * 重命名/修改名称检查
 * @param newName
 * @param oldName
 * @returns
 */
export function checkName(newName: string, oldName: string) {
  if (newName === oldName) {
    ElMessage.error('修改前后名称一样，请重新输入！')
    return false
  }
  if (!newName) {
    ElMessage.error('名称不能为空，请重新输入！')
    return false
  }
  if (newName.includes('*') || newName.includes('<') || newName.includes('>') || newName.includes('|') || newName.includes('%') || newName.includes('/')) {
    ElMessage.error('名称不能包含以下字符：<,>,|,*,?,')
    return false
  }
  return true
}

/**
 * 复制到剪切板
 */
export const copyToClipboard = (value: string) => {
  const input = document.createElement('input')
  document.body.append(input)
  input.setAttribute('value', value)
  input.setAttribute('readonly', 'readonly')
  input.select()
  document.execCommand('copy')
  ElMessage.success('复制成功')
  input.remove()
}

/**
 * 后端状态、结果：
 *  status: init、start、finished、canceled、stopped
 *  result: ''、success、failed
 * 结合两者获取节点展示状态：''，running、success、failed
 * @param parameter
 * @returns
 */
export function getNodeStatus(parameter: any) {
  // 节点暂未执行
  if (!parameter) {
    return ''
  }
  const { status, result } = parameter

  if (['start'].includes(status)) {
    return 'running'
  } else if (result) {
    return result
  }
  return ''
}

// string 转 base64
export const stringToBase64 = (inputString: any) => {
  // const encode = encodeURI(inputString) // 先转化为URI
  const base64 = btoa(inputString) // 编码成base64
  return base64
}

// base64 转 string
export const base64ToString = (base64: any) => {
  const decode = atob(base64) // 解码成uri字符串
  // const outputString = decodeURI(decode)   // 解码成原字符串
  return decode
}
