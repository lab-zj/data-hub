import { reactive, ref, Ref } from 'vue'
import { queryCategoryList, searchFile } from '@/api/data'
import { getRecordType } from '@/util/util'
import { PAGE_SIZE } from '@/constants/data'

export default function useFolderPreview() {
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
   * 查询文件列表
   * 1、从列表点击文件名或者查看详情进入详情页，此时，上一个、下一个就是当前父文件夹的文件列表
   * 2、从搜索列表页点击文件名或者查看详情进入详情页，此时，上一个、下一个就是当前搜索结果页的文件列表
   * @param path
   * @param keyword
   * @param callback
   */
  const queryFolder = (path: string, keyword: string, callback?: Function) => {
    setQueryParams(path, keyword)
    if (keyword) {
      querySearch(keyword, callback)
    } else {
      queryList(path, callback)
    }
  }

  /**
   * 查询文件夹下的内容
   */
  const queryList = async (path: string, callback?: Function) => {
    setQueryParams(path, '')
    const response = await queryCategoryList({
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
   * 从搜索页跳转过来之后，需要查询搜索相关的列表，进行上一个、下一个操作
   * @param path
   * @param callback
   */
  const querySearch = async (keyword: string, callback?: Function) => {
    setQueryParams('', keyword)
    const response = await searchFile({
      data: {
        page: folder_page_number.value,
        size: PAGE_SIZE,
        path: '',
        keyword
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
   * 查询文件列表
   * @param path
   * @param keyword
   * @param callback
   */
  const queryFolderWithPage = (path: string, keyword: string, page: number) => {
    if (keyword) {
      return querySearchWithPage(keyword, page)
    } else {
      return queryListWithPage(path, page)
    }
  }

  /**
   *
   * @param path
   * @param callback
   */
  const querySearchWithPage = async (keyword: string, page: number) => {
    const response = await searchFile({
      data: {
        page,
        size: PAGE_SIZE,
        path: '',
        keyword
      }
    })
    if (response.data.code === 200) {
      const content = response.data.data.content
      content?.forEach((item: any) => {
        item.type = getRecordType(item)
      })
      return {
        totalElements: response.data.data.totalElements,
        totalPages: response.data.data.totalPages,
        content
      }
    } else {
      return {
        totalElements: 0,
        totalPages: 0,
        content: []
      }
    }
  }

  /**
   * 查询文件夹下的内容
   */
  const queryListWithPage = async (path: string, page: number) => {
    const response = await queryCategoryList({
      data: {
        page,
        size: PAGE_SIZE,
        path: path
      }
    })
    if (response.data.code === 200) {
      const content = response.data.data.content
      content?.forEach((item: any) => {
        item.type = getRecordType(item)
      })
      return {
        totalElements: response.data.data.totalElements,
        totalPages: response.data.data.totalPages,
        content
      }
    } else {
      return {
        totalElements: 0,
        totalPages: 0,
        content: []
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
   * 更新某个文件信息
   */
  const updateFolderContentItem = (index: number, item: any) => {
    const file = folderContent.value?.list?.[index]
    folderContent.value.list.splice(index, 1, {
      ...file,
      ...item
    })
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
    updateFolderContentItem,
    queryFolder,
    queryList,
    queryListWithPage,
    querySearch,
    querySearchWithPage,
    queryFolderWithPage,
    onScroll
  }
}
