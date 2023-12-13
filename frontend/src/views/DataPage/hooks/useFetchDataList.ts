import { queryCategoryList, searchFile } from '@/api/data'
import { ref, watch } from 'vue'

interface IQueryParam {
  page: number
  size: number
  path: string
  keyword?: string
}

export function useFetchDataList(options?: { page?: number; size?: number }) {
  const fileListLoading = ref(false) // 接口loading

  const fileList = ref<any[]>([]) // 文件list
  const hasMoreData = ref(true) // 是否有更多数据可加载

  const pageIndex = ref(options?.page || 1) // 查询页码
  const pageSize = ref(options?.size || 30) // 查询每页个数
  let totalPages = -1
  const defaultParams: IQueryParam = {
    // 默认参数
    page: pageIndex.value,
    size: pageSize.value,
    path: ''
  }
  let lastQueryParams: IQueryParam = defaultParams // 记录每次查询接口的参数，用于页码变化时直接查询

  // watch(pageIndex, () => {
  //   getFileList({ ...lastQueryParams, page: pageIndex.value })
  // })
  // 获取列表
  function getFileListByPath(queryParams: IQueryParam) {
    return queryCategoryList({
      data: {
        page: queryParams.page,
        size: queryParams.size,
        path: queryParams.path ? `${queryParams.path}/` : ''
        // path: encodeURIComponent(queryParams.path ? `${queryParams.path}/`: '')
      }
    })
  }
  // 搜索接口
  async function searchFileListByPath(queryParams: IQueryParam) {
    return searchFile({
      data: {
        page: queryParams.page,
        size: queryParams.size,
        path: '',
        keyword: queryParams.keyword
      }
    })
  }

  /**
   * 获取/搜索 文件列表
   * @param queryParams 查询参数
   * @returns response
   */
  async function getFileList(queryParams: IQueryParam | { [key: string]: any }) {
    fileListLoading.value = true
    let request = getFileListByPath
    if (queryParams.keyword) {
      request = searchFileListByPath
    }
    let response = null
    try {
      lastQueryParams = { ...defaultParams, ...queryParams }
      if (totalPages > 0 && lastQueryParams.page > totalPages) {
        return
      }
      response = await request(lastQueryParams)
      if (response.data.code === 200) {
        if (lastQueryParams.page === 1) {
          fileList.value = response.data.data.content
        } else {
          fileList.value = fileList.value.concat(response.data.data.content)
        }
        totalPages = response.data.data.totalPages

        hasMoreData.value = lastQueryParams.page < totalPages
      }
    } finally {
      fileListLoading.value = false
    }

    return response
  }

  /**
   * handle when page or size change
   * @param page page index
   * @param size page size
   */
  async function onParamsPageChange(page: number, size?: number) {
    if (!hasMoreData.value) {
      return
    }
    pageIndex.value = page
    await getFileList({ ...lastQueryParams, page: pageIndex.value, size: size || pageSize.value })
  }

  return {
    fileListLoading,
    fileList,
    getFileList,
    paramsPageIndex: pageIndex,
    hasMoreData,
    onParamsPageChange
  }
}
