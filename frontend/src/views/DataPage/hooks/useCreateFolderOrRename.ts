/**
 * create new folder or rename
 */
import { Ref, computed, ref } from 'vue'
import { IDataItem } from '../util'
import { fileBatchExist, rename as renameApi, createFolder as createFolderApi } from '@/api/data'
import { isError } from 'lodash'
import { ElMessage, ElMessageBox } from 'element-plus'

export function useCreateFolderOrRename() {
  const loading = ref(false) // loading status in create/rename
  const fileNameString = ref('') // new name in create/rename

  const renameFilePath = ref('') // renaming file path

  /**
   * check is file exist when create/rename
   * @param file orgin file
   * @param newName
   * @returns
   */
  async function checkFileExists(file: IDataItem, newName?: string) {
    const filePath = parseFilePath(file)
    const basePath = filePath.includes('/') ? filePath.slice(0, filePath.lastIndexOf('/') + 1) : ''
    let fileName = file.name
    if (newName) {
      fileName = file.directory ? `${newName}/` : `${newName}.${file.name.split('.')[1]}`
    }

    const newPath = `${basePath}${fileName}`
    const response = await fileBatchExist({ data: { pathList: newPath } })
    const { data, code, message } = response.data
    let result = null
    if (code === 200) {
      result = {
        fileExist: !!data[newPath]
      }
    } else {
      result = new Error(message || '接口查询失败')
    }
    return result
  }

  function parseFilePath(file: IDataItem) {
    let path = file.path
    if (path.includes(NEW_FORDER_PATH)) {
      // 新建文件夹情况的path处理
      path = path.replace(NEW_FORDER_PATH, '')
      return path !== '' ? path + '/' : ''
    }
    if (file.directory) {
      // 文件夹末尾默认带 "/"
      return path.slice(0, -1)
    }
    return path
  }

  /**
   * rename request
   * @param file
   * @param newName
   * @param override
   * @returns
   */
  async function rename(file: IDataItem, newName: string, override: boolean = true) {
    const filePath = parseFilePath(file)
    const basePath = filePath.includes('/') ? filePath.slice(0, filePath.lastIndexOf('/') + 1) : ''
    const newPath = `${basePath}${file.directory ? `${newName}/` : `${newName}.${file.name.split('.')[1]}`}`
    loading.value = true

    try {
      const response = await renameApi({
        data: {
          sourcePath: file.path,
          targetPath: newPath,
          override
        }
      })
      return response.data
    } finally {
      loading.value = false
      onRenameFileName({} as any, 'cancel')
    }
  }

  /**
   * handle rename file
   * @param fileData
   */
  function onRenameFileName(fileData: IDataItem, type?: 'cancel') {
    if (type === 'cancel') {
      renameFilePath.value = ''
      fileNameString.value = ''
    } else {
      renameFilePath.value = fileData.path
      // 普通文件需要去掉后缀进行重命名
      fileNameString.value = fileData.directory ? fileData.name : fileData.name.slice(0, fileData.name.lastIndexOf('.'))

      onCreateFolder('', 'cancel')
    }
  }

  /**
   * 检测新建文件夹名/重命名的合法性
   * @param fileData
   * @returns [validStatus, validErrMessage]
   */
  function validateFileName(fileData: IDataItem) {
    if (!isCreateFolderStatus.value && renameFilePath.value !== fileData.path) {
      return [false, '']
    }
    if (!fileNameString.value) {
      return [false, '名字不能为空！']
    }
    if (['*', '<', '>', '|', '%', '/'].some((key) => fileNameString.value.includes(key))) {
      return [false, '名字不能包含以下字符：<,>,|,*,?']
    }
    const beforeName = fileData.directory ? fileData.name : fileData.name.slice(0, fileData.name.lastIndexOf('.'))
    if (!isCreateFolderStatus.value && fileNameString.value === beforeName) {
      return [false, '修改前后文件名一样，请重新输入！']
    }
    return [true]
  }

  // create folder function start
  const newFolderName = ref('') // create folder new name
  const newFolderItem: Ref<IDataItem[]> = ref([]) // init new item when create folder
  const NEW_FORDER_PATH = 'new_init_folder' // mark of new folder path

  const isCreateFolderStatus = computed(() => {
    // 当前是否存在新建文件夹状态
    return newFolderItem.value.length > 0
  })

  /**
   * handle/cancel create folder
   * @param basePath target path when create
   * @param type
   */
  function onCreateFolder(basePath?: string, type?: 'cancel') {
    if (type === 'cancel') {
      newFolderItem.value = []
      newFolderName.value = ''
    } else {
      newFolderItem.value = [
        {
          name: '',
          path: `${NEW_FORDER_PATH}${basePath || ''}`,
          directory: true,
          size: 0,
          creatTimestamp: +new Date(),
          updateTimestamp: +new Date()
        }
      ]
      onRenameFileName({} as any, 'cancel')
    }
  }

  /**
   * create folder request
   * @param file
   * @param newName
   * @returns
   */
  async function createFolder(file: IDataItem, newName: string) {
    let basePath = file.path.replace(NEW_FORDER_PATH, '')
    if (basePath !== '') {
      basePath += '/'
    }

    loading.value = true

    try {
      const response = await createFolderApi({
        data: `${basePath}${newName}`
      })
      return response.data
    } finally {
      loading.value = false
      onCreateFolder('', 'cancel')
    }
  }

  /**
   * if filePath in create or rename status
   * @param filePath target file path
   * @returns
   */
  const isCurFileCreateOrRename = function (filePath: string) {
    return renameFilePath.value === filePath || filePath.startsWith(NEW_FORDER_PATH)
  }

  /**
   * combine create & rename request
   * @param file
   * @param newName
   * @param override
   * @returns
   */
  function createOrRenameRequest(file: IDataItem, newName: string, override: boolean = true) {
    if (isCreateFolderStatus.value) {
      return createFolder(file, newName)
    } else {
      return rename(file, newName, override)
    }
  }

  /**
   * export function: confirm create/rename
   * @param file
   * @param onSuccess
   * @returns
   */
  async function confirmCreateOrRename(file: IDataItem, onSuccess?: Function) {
    const [vStatus, vMessage] = validateFileName(file)
    if (vStatus === false) {
      vMessage && ElMessage.error(vMessage as string)
      return
    }

    const checkResult = await checkFileExists(file, fileNameString.value)
    if (checkResult && !isError(checkResult)) {
      const { fileExist } = checkResult
      if (fileExist) {
        // 存在重名情况
        const name = file.directory ? fileNameString.value : `${fileNameString.value}${file.name.slice(file.name.lastIndexOf('.'))}`
        ElMessageBox.confirm('请换个名字重试', `已存在${name}文件${file.directory ? '夹' : ''}`, {
          confirmButtonText: '知道了',
          showCancelButton: false,
          type: 'warning',
          autofocus: false
        })
      } else {
        const isNewCreate = isCreateFolderStatus.value
        const result = await createOrRenameRequest(file, fileNameString.value)
        if (result.code === 200) {
          ElMessage.success(isNewCreate ? '创建成功' : '修改成功')
        } else {
          ElMessage.error(result.message || (isNewCreate ? '创建失败' : '修改失败'))
        }
        if (onSuccess) {
          onSuccess()
        }
      }
    } else {
      ElMessage.error(checkResult as Error)
    }
  }

  return {
    loading,
    renameFilePath,
    fileNameString,
    checkFileExists,
    onRenameFileName,
    rename,
    validateFileName,

    newFolderName,
    newFolderItem,
    onCreateFolder,
    isCurFileCreateOrRename,

    confirmCreateOrRename
  }
}
