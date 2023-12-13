import { ref, Ref } from 'vue'
import { txtPreviewApi } from '@/api/preview'

export default function useTxtPreview() {
  const txtPreviewContent: Ref<string> = ref('')

  /**
   * 文本预览
   */
  const txtPreview = async (path: string) => {
    const response = await txtPreviewApi({
      data: {
        path
      }
    })
    const { code, data, message } = response.data
    if (code === 200) {
      txtPreviewContent.value = data
    } else {
      console.error(message)
      txtPreviewContent.value = ''
    }
  }

  return {
    txtPreviewContent,
    txtPreview
  }
}
