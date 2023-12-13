import { watch } from 'vue'
import { useDataStore } from '@/stores/index'

/**
 * intercept when user wants to close or refresh
 */
export default function useUploadInterCept() {
  const dataStore = useDataStore()

  const beforeunloadEvent = (event: any) => {
    // 浏览器这里不支持弹框信息自定义，只能使用浏览器默认的弹框
    event.returnValue = "string"
    return 'string'
  }

  watch(()=>dataStore.uploadingCount, () => {
    if(dataStore.uploadingCount === 0) {
      removeEventListener('beforeunload',beforeunloadEvent)
    }else{
      addEventListener('beforeunload', beforeunloadEvent)
    }
  })

  return {

  }
}
