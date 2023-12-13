import { reactive, ref, Ref } from 'vue'
import { queryAlgorithmCategoryList } from '@/api/data'
import { getRecordType } from '@/util/util'
import { PAGE_SIZE } from '@/constants/data'

export default function useAlgorithmFolder() {
  const folder_page_number: Ref<number> = ref(1)
  const folderContent: any = ref({
    list: [],
    total: 0
  })

  const queryParams: any = reactive({
    path: '',
    keyword: ''
  })

  function setQueryParams(path: string, keyword: string) {
    queryParams.path = path
    queryParams.keyword = keyword
  }

  /**
   * 查询算法文件夹
   */
  const queryList = async (path: string, callback?: Function) => {
    setQueryParams(path, '')
    const response = await queryAlgorithmCategoryList({
      data: {
        page: folder_page_number.value,
        size: PAGE_SIZE,
        path: path
      }
    })
    if (response.data.code === 200) {
      folderContent.value.list = (folderContent.value.list || []).concat(response.data.data.content)
      folderContent.value.list?.forEach((item: any) => {
        item.type = getRecordType(item)
      })
      folderContent.value.total = response.data.data.totalElements
      folderContent.value.totalPages = response.data.data.totalPages
      if (callback) {
        callback()
      }
    }
  }

  /**
   * 清空当前文件所在文件夹的内容（主要用于上一个、下一个）
   */
  const resetFolderContent = () => {
    folderContent.value = {
      list: [],
      total: 0
    }
    folder_page_number.value = 1
  }

  /**
   * 滚动加载
   */
  function onScroll() {
    if (folderContent.value.total > folderContent.value.list.length) {
      folder_page_number.value += 1
      queryList(queryParams.path)
    }
  }

  return {
    folder_page_number,
    folderContent,
    resetFolderContent,
    queryList,
    onScroll
  }
}
