import { ref } from 'vue'
import { virtualFilePreviewApi } from '@/api/preview'

export default function useVirtualPreview() {
  const virtualPreviewContent: any = ref({})

  /**
   * 虚拟文件预览
   * @param path
   * @param callback
   */
  const virtualPreview = async (path: string, callback?: Function) => {
    const response = await virtualFilePreviewApi({
      data: {
        path
      }
    })
    const { code, data, message } = response.data
    if (code === 200) {
      virtualPreviewContent.value = data
    } else {
      console.error(message)
      virtualPreviewContent.value = ''
    }
  }

  return {
    virtualPreviewContent,
    virtualPreview
  }
}
