import { ref, Ref } from 'vue'
import { cloneDeep } from 'lodash'
import { csvPreviewApi, virtualRemoteFilePreviewApi } from '@/api/preview'
import { PAGE_SIZE } from '@/constants/data'

export default function useCsvPreview() {
  const csvPreviewContent: any = ref({})
  const page_number: Ref<number> = ref(1)

  function reset() {
    page_number.value = 1
    csvPreviewContent.value = {}
  }

  /**
   * csv数据表格文件预览
   * 1、本地云盘的csv文件预览
   * 2、远程s3上面的csv文件预览
   */
  const csvPreview = async (path: string, callback?: Function) => {
    const response = path.includes('.s3')
      ? await virtualRemoteFilePreviewApi({
          data: {
            path
          }
        })
      : await csvPreviewApi({
          data: {
            path,
            page: page_number.value,
            size: PAGE_SIZE
          }
        })
    const { code, data, message } = response.data
    if (code === 200 && data) {
      const { columnVOList, valueList } = cloneDeep(data)
      valueList.content = valueList.content.map((item: any, index: number) => {
        item.splice(0, 0, index)
        return item
      })
      // 初始没有数据
      if (!csvPreviewContent.value.columnVOList) {
        csvPreviewContent.value = {
          columnVOList,
          valueList
        }
      } else {
        // 更新content数据
        csvPreviewContent.value.valueList.content = csvPreviewContent.value.valueList.content.concat(valueList.content)
      }
      // scroll事件触发继续查询
      if (csvPreviewContent.value?.valueList?.content?.length < valueList.totalElements) {
        page_number.value += 1
      }
      if (callback) {
        callback()
      }
    } else {
      console.error(message || 'no data')
      csvPreviewContent.value = {}
      page_number.value = 1
    }
  }

  return {
    page_number,
    csvPreviewContent,
    csvPreview,
    reset
  }
}
