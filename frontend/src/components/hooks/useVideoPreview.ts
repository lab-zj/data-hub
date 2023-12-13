import { ref, Ref } from 'vue'

export default function useVideoPreview() {
  const videoPreviewContent: Ref<string> = ref('')

  /**
   * 视频预览
   */
  const videoPreview = (path: string) => {
    videoPreviewContent.value = `/api/filesystem/preview/video?path=${path}`
  }

  return {
    videoPreviewContent,
    videoPreview
  }
}
