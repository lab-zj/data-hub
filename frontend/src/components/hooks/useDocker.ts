import { reactive, ref, Ref } from 'vue'
import { getBaseImageList } from '@/api/algorithm'
import { PAGE_SIZE } from '@/constants/data'

export default function useDocker() {
  const page_number: Ref<number> = ref(1)
  const content: any = reactive({
    list: [],
    totalElements: 0,
    totalPages: 0
  })

  /**
   * 查询docker image list
   */
  const queryList = async (callback?: Function) => {
    const response = await getBaseImageList({
      data: {
        pageNumber: page_number.value,
        pageSize: PAGE_SIZE,
        shared: true
      }
    })
    if (response.data.code === 200) {
      content.list = (content.list || []).concat(response.data.data.content)
      content.totalElements = response.data.data.totalElements
      content.totalPages = response.data.data.totalPages
      if (callback) {
        callback()
      }
    }
  }

  /**
   * 滚动加载
   */
  function onScroll() {
    if (content.totalElements > content.list.length) {
      page_number.value += 1
      queryList()
    }
  }

  return {
    content,
    queryList,
    onScroll
  }
}
