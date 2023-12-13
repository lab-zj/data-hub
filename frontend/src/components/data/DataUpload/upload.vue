<template>
  <div class="upload-btn">
    <div class="label">{{ props.config.title }}</div>
    <input type="file" id="filepicker" name="fileList" multiple :webkitdirectory="props.config.type === 'directory'" title="" @change="onChange" />
  </div>
</template>
<script setup lang="ts">
import { computed, watch, h, ref } from 'vue'
import { useDataStore } from '@/stores/index'
import { useRoute } from 'vue-router'
import { uploadFile, isExistBatchApi, directoryRenameApi } from '@/api/upload'
import { nanoid } from 'nanoid'
import { ElCheckbox, ElMessageBox } from 'element-plus'
import bus from '@/EventBus/eventbus.js'

const props = defineProps({
  config: {
    type: Object,
    default(): Object {
      return {}
    }
  }
})

const currentUploadUid: any = ref('')
const dataStore = useDataStore()
const route = useRoute()
let routes: any = computed(() => {
  const path = route?.path.indexOf('path') > -1 ? route.path.split('=')[1] : ''
  return path
})

const emit = defineEmits<{
  (e: 'update'): void
}>()

const onChange = async (event: any) => {
  console.log('upload:', event, event.target.files, typeof event.target.files)
  const { files } = event.target
  let webkitdirectory: boolean = false

  // 上传之前批量检测：是否存在同名文件。
  // 1、服务器已有文件是否有同名文件
  // 2、浏览器本地是否有同名文件（检测前端正在上传、排队中的上传任务，是否有同名文件）
  const currentPath = `${decodeURIComponent(routes.value)}/`
  const pathList: string[] = []
  let folderPath: string = ''
  for (let file of files) {
    webkitdirectory = !!file.webkitRelativePath
    const path = file.webkitRelativePath ? `${currentPath}${file.webkitRelativePath}` : `${currentPath}${file.name}`
    pathList.push(path)
  }
  let existRemote: boolean = false
  if (webkitdirectory) {
    folderPath = files[0].webkitRelativePath.split('/')[0]
    // 上传文件夹，检测文件夹即可
    const _path = pathList[0].split('/')
    const directoryPath = _path.slice(0, _path.length - 1).join('/')
    existRemote = await isExistBatchCheck([`${directoryPath}/`])
  } else {
    // 上传文件，检测所有文件
    existRemote = await isExistBatchCheck(pathList)
  }

  const existLocal = dataStore.fileList?.some((file: any) => !['success', 'fail'].includes(file.status) && pathList.includes(file.path))

  // 有同名的情况
  if (existRemote || existLocal) {
    const overwrite = ref<boolean>(false)
    ElMessageBox.confirm('', '已存在同名文件，是否上传并覆盖？', {
      confirmButtonText: '继续上传',
      showCancelButton: false,
      autofocus: false,
      // Should pass a function if VNode contains dynamic props
      message: () =>
        h('div', null, [
          h(ElCheckbox, {
            label: '覆盖',
            modelValue: overwrite.value,
            'onUpdate:modelValue': (val: boolean) => {
              overwrite.value = val
            }
          })
        ])
    })
      .then(async () => {
        // 文件夹同名、且选择不覆盖的情况下，需要去后端获取最新名字
        const folderPathRename = webkitdirectory && !overwrite.value ? await directoryRename(currentPath, folderPath) : ''
        // 点击继续上传
        queueUpForUpload(files, 'pending', overwrite.value, folderPathRename)
        // 先后选择同样的文件，无法触发onchange事件，这里需要清空
        event.target.value = null
      })
      .catch(() => {
        event.target.value = null
      })
  } else {
    // 没有同名的情况，直接上传
    queueUpForUpload(files, 'pending', true)
    event.target.value = null
  }
}

/**
 * 排队
 * @param files
 * @param status
 * @param overwrite
 * @param folderRename
 */
const queueUpForUpload = (files: any, status: string, overwrite: boolean, folderRename?: string) => {
  // 如果是文件夹，会生成一个统一的uid
  let uid: string = files[0].webkitRelativePath ? nanoid(10) : ''
  // 完成检测：进入队列进行排队
  for (let file of files) {
    const _uid = uid || nanoid(10)
    beforeUpload(file, status, overwrite, _uid, folderRename)
  }
  balance()
}

/**
 * 文件管理：
 * 1、上传任务：勾选多个文件属于多个上传任务，上传一个文件夹属于一个上传任务，每个上传任务对应一个uid
 * 2、上传文件列表：这里将文件夹中所有文件平铺
 * 3、上传文件队列：设置最大并发为3，一次只能发起3个上传请求
 * @param file
 * @param status
 * @param overwrite
 * @param uid
 * @param folderRename: 文件夹上传的时候，如果已有同名的文件夹，这里需要将获取到的folderRename传递给后端
 */
const beforeUpload = (file: any, status: string, overwrite: boolean, uid: string, folderRename?: string) => {
  const currentPath = `${decodeURIComponent(routes.value)}/`
  const { name, size, webkitRelativePath } = file
  const type = name.split('.')?.slice(-1)?.[0]
  const filePath = webkitRelativePath ? `${currentPath}${webkitRelativePath}` : `${currentPath}${name}`

  // 1、管理上传任务
  if (webkitRelativePath) {
    // 上传文件夹
    const relativePath = webkitRelativePath?.split('/')?.[0]
    const path = `${currentPath}${relativePath}`
    dataStore.addUpload({
      name: relativePath,
      path, // 文件夹路径
      type: 'folder',
      size: undefined,
      status,
      list: [filePath], // 该文件夹下包含的每个文件的具体路径（包括文件name）
      uid
    })
  } else {
    // 上传文件
    dataStore.addUpload({
      name: name,
      path: `${currentPath}${name}`,
      type,
      size,
      status,
      list: [],
      uid
    })
  }
  // 2、所有上传的文件列表
  dataStore.addFile({
    name,
    path: filePath,
    type,
    size,
    webkitRelativePath,
    status,
    uid
  })

  // 3、添加到上传文件排队队列中
  dataStore.addFileToStack({
    file,
    overwrite,
    uid,
    path: filePath,
    folderRename: folderRename || ''
  })
  // 打开上传任务浮窗
  bus.emit('displayUploadBar', true)
}

/**
 * 上传文件请求
 * @param upload
 */
const upload = async (upload: any) => {
  const currentPath = `${decodeURIComponent(routes.value)}/`
  const formData = new FormData()
  formData.append('file', upload.file)
  formData.append('currentPath', currentPath)
  formData.append('overwrite', upload.overwrite)
  formData.append('rootDirectoryToReplace', upload.folderRename)

  dataStore.updateFileStatus(upload.uid, upload.path, 'ready')
  dataStore.addUploading(upload.uid)

  currentUploadUid.value = upload.uid

  // 控制可以取消http请求
  const controller = new AbortController()
  dataStore.addCancel(upload.uid, controller)

  uploadFile({
    data: formData,
    signal: controller.signal
  })
    .then((response: any) => {
      const { code, message } = response.data
      if (code === 200) {
        updateUpload(upload.uid, upload.path, 'success')
        // 上传完成，刷新放到watch里进行（每个上传任务里面的所有文件都上传完成再进行刷新）
      } else {
        updateUpload(upload.uid, upload.path, 'fail')
        console.error(message)
      }
    })
    .catch((error: any) => {
      console.error(error)
      updateUpload(upload.uid, upload.path, 'fail')
    })
}

/**
 * 每次最多发起3个上传文件请求
 */
const balance = () => {
  const remaining = 3 - dataStore.uploadings.length
  if (remaining > 0 && dataStore.fileStack?.length > 0) {
    const ready = dataStore.fileStack.slice(0, remaining)
    ready.forEach((file: any) => {
      upload(file)
    })
    dataStore.fileStack.splice(0, remaining)
  }
}

// 监听正在上传队列，如果少于3个，继续发起请求，如果等于3个，等待
watch(
  () => dataStore.uploadings,
  () => {
    balance()
  },
  { deep: true }
)

// 上传完成，刷新文件列表
watch(
  () => dataStore.uploadList,
  (val: any) => {
    const _upload = val.find((item: any) => item.uid === currentUploadUid.value)
    if (['success', 'fail'].includes(_upload?.status)) {
      emit('update')
    }
  },
  { deep: true }
)

/**
 * 文件同名批量检测
 * @param pathList
 */
const isExistBatchCheck = async (pathList: string[]) => {
  const response = await isExistBatchApi({
    data: {
      pathList: pathList.join(',')
    }
  })
  const { code, data, message } = response.data
  if (code === 200) {
    // { path: true/false } 存在：true，不存在：false
    return Object.keys(data).some((key) => data[key] === true)
  } else {
    console.error(message)
  }
}

/**
 *
 * @param currentPath
 * @param name
 */
const directoryRename = async (currentPath: string, name: string) => {
  const path = `${currentPath}${name}`
  const response = await directoryRenameApi({
    data: {
      path
    }
  })
  const { code, data } = response.data
  if (code === 200) {
    return data
  }
}

/**
 * 更新文件相关状态
 * @param uid
 * @param path
 * @param status
 */
const updateUpload = (uid: string, path: string, status: string) => {
  dataStore.updateFileStatus(uid, path, status)
  dataStore.removeUploading(uid)
  dataStore.removeCancel(uid)
}
</script>
<style lang="less" scoped>
.upload-btn {
  position: relative;
  width: 100%;
  padding: 6px 12px;
  #filepicker {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    outline: medium none;
    opacity: 0;
  }
}
</style>
<style lang="less">
button:focus,
button:focus-visible {
  outline: none !important;
}
</style>
