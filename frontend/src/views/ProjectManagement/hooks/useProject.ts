import { reactive, ref } from 'vue'
import { PAGE_SIZE } from '@/constants/data'
import { fetchListApi, modifyProjectApi, deleteProjectApi } from '@/api/project'
import { ElMessage } from 'element-plus'

/**
 * 项目列表：搜索、查询、滚动加载
 * @returns
 */
export default function useProject() {
  const list: any = ref(null)
  const totalElements: any = ref(0)
  const totalPages: any = ref(0)
  const loading: any = ref(false)
  const queryParams: any = reactive({
    page_number: 1
  })

  /**
   * 初始化
   */
  function initQuery() {
    queryParams.page_number = 1
    list.value = null
    totalElements.value = 0
    totalPages.value = 0
  }

  /**
   * 查询列表
   * @param type
   */
  async function getList() {
    loading.value = true
    const response = await fetchListApi({
      data: {
        pageNumber: queryParams.page_number,
        pageSize: PAGE_SIZE
      }
    })
    const { code, data } = response.data
    if (code === 200) {
      totalElements.value = data?.totalElements
      totalPages.value = data?.totalPages
      list.value = (list.value || []).concat(data?.content)
    }
    loading.value = false
  }

  /**
   * 滚动加载
   */
  function onScroll() {
    if (queryParams.page_number < totalPages.value) {
      queryParams.page_number += 1
      getList()
    }
  }

  /**
   * 新增项目、修改项目、置顶、取消置顶
   * @param params
   */
  async function modifyProject(params: any, msg: string, callback?: Function) {
    const response = await modifyProjectApi({
      data: {
        ...params
      }
    })
    const { code, message } = response.data
    if (code === 200) {
      ElMessage.success(msg)
      if (callback) {
        callback()
      }
      // 刷新列表
      initQuery()
      getList()
    } else {
      ElMessage.error(message)
    }
  }

  /**
   * 删除
   * @param id
   */
  async function deleteProject(id: number) {
    const response = await deleteProjectApi({
      params: {
        projectIdList: [id]
      }
    })
    const { code, message } = response.data
    if (code === 200) {
      ElMessage.success('删除成功！')
      // 删除成功，刷新数据
      initQuery()
      getList()
    } else {
      ElMessage.error(message)
    }
  }

  return {
    list,
    loading,
    initQuery,
    getList,
    deleteProject,
    onScroll,
    modifyProject
  }
}
