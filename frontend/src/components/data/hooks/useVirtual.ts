import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createVirtualFileApi, connectTestFileApi } from '@/api/virtual'

/**
 * 虚拟文件：数据库文件、S3
 * @returns
 */
export default function useVirtual(callback?: Function) {
  /** 数据库文件 */
  const dataBaseFormVisible: any = ref(false)
  const virtualLoading: any = ref(false)

  /** 连接测试loading */
  const connecting: any = ref(false)

  /** S3文件 */
  const s3FormVisible: any = ref(false)

  function setDataBaseFormVisible(_visible: boolean) {
    dataBaseFormVisible.value = _visible
  }
  function setS3FormVisible(_visible: boolean) {
    s3FormVisible.value = _visible
  }

  /**
   * 新建虚拟文件
   */
  async function createVirtualFile(params: any) {
    virtualLoading.value = true
    try {
      const response = await createVirtualFileApi({
        data: params
      })
      const { code, message } = response.data
      if (code === 200) {
        ElMessage.success('新建成功！')
        setDataBaseFormVisible(false)
        setS3FormVisible(false)
        // 刷新当前列表页
        if (callback) {
          callback()
        }
      } else {
        ElMessage.error(message)
      }
    } catch (error: any) {
      console.error(error)
    } finally {
      virtualLoading.value = false
    }
  }

  /**
   * 连接测试
   * @param params
   */
  async function connectTestFile(params: any) {
    connecting.value = true
    const { schemaName, ...rest } = params
    const response = await connectTestFileApi({
      data: params.type === 'MYSQL' ? rest : params
    })
    const { code, message } = response.data
    if (code === 200) {
      ElMessage.success('连接成功！')
    } else {
      ElMessage.error(message)
    }
    connecting.value = false
  }

  return {
    dataBaseFormVisible,
    virtualLoading,
    s3FormVisible,
    connecting,
    setDataBaseFormVisible,
    setS3FormVisible,
    createVirtualFile,
    connectTestFile
  }
}
