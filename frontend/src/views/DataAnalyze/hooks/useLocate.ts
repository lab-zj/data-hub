import { ref } from 'vue'
import { useDataGraph } from '@/stores/dataGraphStore'
import { NODE_TYPES } from '@/constants/dataGraph'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'

/**
 * 定位
 * @returns
 */
export default function useLocate() {
  const dataGraphStore = useDataGraph()
  const { locateNode } = useGraphInteraction()

  const visible: any = ref(false)
  const currentLocationData: any = ref(null)
  const locationUsedDatasets: any = ref(null)

  function setVisible(_visible: boolean) {
    visible.value = _visible
  }

  /**
   * 从左边栏发起定位
   * @param _data
   */
  function onLocate(_data: any) {
    dataGraphStore.init()
    const { type, dataset, id } = _data

    let target: any = null
    if (id) {
      // 画布上一个下一个发起定位，此时直接知道节点id
      target = dataGraphStore.rawData?.nodes?.find((node: any) => node.id === id)
    } else {
      // 从左侧面板发起
      // 1、数据文件此时只知道文件path
      // 2、算法文件此时只知道算法id
      target =
        type === NODE_TYPES.DATA
          ? dataGraphStore.rawData?.nodes?.find((node: any) => node.type === NODE_TYPES.DATA && node.dataset.path === dataset.path)
          : type === NODE_TYPES.ALGO
          ? dataGraphStore.rawData?.nodes?.find((node: any) => node.type === NODE_TYPES.ALGO && +node.dataset.algorithm === +dataset.algorithm)
          : null
    }
    currentLocationData.value = target
    // 定位画布上同一个文件path 或者 算法id的文件
    locationUsedDatasets.value =
      type === NODE_TYPES.DATA
        ? dataGraphStore.used_datasets.filter((node: any) => node.dataset.path === target.dataset.path)
        : type === NODE_TYPES.ALGO
        ? dataGraphStore.used_algos.filter((node: any) => +node.dataset.algorithm === +target.dataset.algorithm)
        : []

    if (target) {
      setVisible(true)
      locateNode({
        type,
        id: target.id
      })
    }
  }

  function handleLocationClose() {
    setVisible(false)
    dataGraphStore.graphActionMode = ''
  }

  return {
    visible,
    setVisible,
    locationUsedDatasets,
    currentLocationData,
    onLocate,
    handleLocationClose
  }
}
