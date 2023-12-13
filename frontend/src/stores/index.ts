import { defineStore } from 'pinia'

export const useDataStore = defineStore({
  id: 'data',
  state: () => ({
    fileList: [] as any[], // 具体上传的文件列表（注意：上传文件夹，也是逐一上传文件夹里面的内容，这里是包括文件夹下的所有的具体文件）
    uploadList: [] as any[], // 上传记录（注意：上传文件夹，这里算就一条记录）
    fileStack: [] as any[], // 文件队列（所有通过文件选择对话框选择的文件，都需要放到该队列中），用于控制文件上传，每次最多发起3个文件上传
    uploadings: [] as any[], // 正在上传的文件列表，最多3个
    cancelControls: [] as any[] // 上传请求对应的取消请求的函数（取消http请求）
  }),
  getters: {
    // 所有的任务个数
    allUploadCount: (state: any): any => {
      return state.uploadList.length
    },
    // 上传中、排队中的任务个数
    uploadingCount: (state: any): any => {
      return state.uploadList.filter((file: any) => file.status !== 'success' && file.status !== 'fail')?.length
    }
  },
  actions: {
    addFile(file: any) {
      this.fileList.push(file)
    },
    removeFile(fileUid: any) {
      const i = this.fileList.findIndex((item: any) => item.uid === fileUid)
      if (i > -1) this.fileList.splice(i, 1)
    },
    updateFileStatus(fileUid: any, filePath: string, status: string) {
      this.fileList.forEach((item: any) => {
        if (item.uid === fileUid && item.path === filePath) {
          item.status = status
        }
      })
    },
    addUpload(upload: any) {
      const i = this.uploadList.findIndex((item: any) => item.uid === upload.uid)
      if (i > -1) {
        const _List = this.uploadList[i].list
        this.uploadList.splice(i, 1, {
          ...upload,
          list: (_List || []).concat(upload.list)
        })
      } else {
        this.uploadList.push(upload)
      }
    },
    removeUpload(uploadPath: any) {
      const i = this.uploadList.findIndex((item: any) => item.path === uploadPath)
      if (i > -1) this.uploadList.splice(i, 1)
    },
    // 根据fileList中的上传状态，去更新uploadList中的状态
    // 上传文件夹的情况，一条上传任务可能对应多个文件（实际上传是一个文件一个文件上传）
    updateUploadFromfile() {
      const _uploadList = this.uploadList.map((upload: any) => {
        if (upload.type === 'folder') {
          const isReady = upload.list.some((item: any) => {
            const file = this.fileList.find((file: any) => upload.uid === file.uid && file.path === item)
            return file.status === 'ready'
          })
          const allPending = upload.list.every((item: any) => {
            const file = this.fileList.find((file: any) => upload.uid === file.uid && file.path === item)
            return file.status === 'pending'
          })
          const allSuccess = upload.list.every((item: any) => {
            const file = this.fileList.find((file: any) => upload.uid === file.uid && file.path === item)
            return file.status === 'success'
          })
          // 一期先不区分部分失败（文件夹上传可能存在部分失败的情况） 和 全部失败
          const allFail = upload.list.every((item: any) => {
            const file = this.fileList.find((file: any) => upload.uid === file.uid && file.path === item)
            return file.status === 'fail'
          })
          if (isReady) {
            // 上传中
            upload.status = 'ready'
          } else if (allPending) {
            // 排队中
            upload.status = 'pending'
          } else if (allSuccess) {
            // 上传成功
            upload.status = 'success'
          } else if (allFail) {
            upload.status = 'fail'
          }
        } else {
          const file = this.fileList.find((file: any) => upload.uid === file.uid && file.path === upload.path)
          upload.status = file.status
        }
        return upload
      })
      this.uploadList = _uploadList
    },
    addFileToStack(file: any) {
      this.fileStack.push(file)
    },
    removeFileFromStack(fileUid: string) {
      const i = this.fileStack.findIndex((item: any) => item.uid === fileUid)
      if (i > -1) this.fileStack.splice(i, 1)
    },
    addUploading(fileUid: string) {
      this.uploadings.push(fileUid)
    },
    removeUploading(fileUid: string) {
      const i = this.uploadings.findIndex((item: any) => item === fileUid)
      if (i > -1) this.uploadings.splice(i, 1)
    },
    addCancel(fileUid: string, cancel: any) {
      this.cancelControls.push({
        uid: fileUid,
        cancel
      })
    },
    removeCancel(fileUid: string) {
      const i = this.cancelControls.findIndex((item: any) => item === fileUid)
      if (i > -1) this.cancelControls.splice(i, 1)
    }
  }
})
