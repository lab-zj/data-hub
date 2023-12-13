import { ref, Ref } from 'vue'
import { imagePreviewApi } from '@/api/preview'

export default function useImgPreview() {
  const imgPreviewContent: Ref<string> = ref('')

  /**
   * 图片预览
   */
  const imagePreview = async (path: string) => {
    const response = await imagePreviewApi({
      data: {
        path
      }
    })
    const myBlob = new window.Blob([response.data], { type: 'image/jpeg' })
    imgPreviewContent.value = window.URL.createObjectURL(myBlob)
  }

  return {
    imgPreviewContent,
    imagePreview
  }
}
