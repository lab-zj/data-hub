import { reactive, ref } from 'vue'
import { PAGE_SIZE } from '@/constants/data'
import { getAllRelatedAlgorithmApi, deleteAlgorithmApi, getBusinessAlgorithmById, uploadAlgorithm } from '@/api/algorithm'
import { ElMessage } from 'element-plus'

/**
 * 算法列表：搜索、查询、滚动加载
 * @returns
 */
export default function useAlgorithm() {
  const algoList: any = ref([])
  const totalElements: any = ref(0)
  const totalPages: any = ref(0)
  const queryParams: any = reactive({
    type: 'custom',
    keyWord: '',
    page_number: 1
  })
  const loading: any = ref(false)

  /**
   * 初始化
   */
  function initQuery(type: string, keyWord: string) {
    setQueryParams(type, keyWord)
    algoList.value = []
    totalElements.value = 0
    totalPages.value = 0
  }

  /**
   * 设置查询参数
   * @param type
   * @param keyWord
   */
  function setQueryParams(type = 'custom', keyWord = '') {
    queryParams.type = type
    queryParams.keyWord = keyWord
    queryParams.page_number = 1
  }

  /**
   * 查询算法列表
   * @param type
   */
  async function getAllRelatedAlgorithm() {
    const response = await getAllRelatedAlgorithmApi({
      data: {
        nameKeyWord: queryParams.keyWord,
        version: '1.0',
        type: queryParams.type,
        pageNumber: queryParams.page_number,
        pageSize: PAGE_SIZE * 4
      }
    })
    const { code, data } = response.data
    if (code === 200) {
      totalElements.value = data?.totalElements
      totalPages.value = data?.totalPages
      algoList.value = (algoList.value || []).concat(data?.content)
    }
  }

  /**
   * 滚动加载
   */
  function onScroll() {
    // if (totalElements.value > algoList.value.length) {
    //   queryParams.page_number += 1
    //   getAllRelatedAlgorithm()
    // }
    if (queryParams.page_number < totalPages.value) {
      queryParams.page_number += 1
      getAllRelatedAlgorithm()
    }
  }

  /**
   * 删除算法
   * @param id
   */
  async function deleteAlgorithm(id: number) {
    const response = await deleteAlgorithmApi(id)
    const { code, message } = response.data
    if (code === 200) {
      ElMessage.success('删除成功！')
      // 删除成功，刷新数据
      initQuery(queryParams.type, queryParams.keyWord)
      getAllRelatedAlgorithm()
    } else {
      ElMessage.error(message)
    }
  }

  /**
   * 根据算法id获取算法详情
   * @param id
   */
  async function getBusinessAlgorithm(id: string | number) {
    if (!id) {
      console.error('algorithm id is null')
      return
    }

    const response = await getBusinessAlgorithmById({
      data: {
        id
      }
    })
    const { code, data } = response.data
    if (code === 200) {
      return data
    }
    return
  }

  /**
   * 上传新增算法或者修改算法
   */
  async function modifyAlgorithm(params: any, tip: string) {
    loading.value = true
    const response = await uploadAlgorithm({
      data: params
    })
    const { code, message } = response.data
    loading.value = false
    if (code === 200) {
      ElMessage.success(tip)
    } else {
      ElMessage.error(message)
    }
  }

  return {
    algoList,
    initQuery,
    getAllRelatedAlgorithm,
    onScroll,
    deleteAlgorithm,
    getBusinessAlgorithm,
    modifyAlgorithm,
    loading
  }
}
