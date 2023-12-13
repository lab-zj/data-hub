import { ref } from 'vue'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'

/**
 * 更换数据源
 * @returns
 */
export default function useChangeDataset() {
  const { setDataIntoEmptyDatasetNode, changeDataIntoExistDatasetNode } = useGraphInteraction()

  const visible: any = ref(false)
  const loading: any = ref(false)
  const from: any = ref('')

  function setVisible(_visible: boolean) {
    visible.value = _visible
  }

  function setFrom(_from: string) {
    from.value = _from
  }

  function onConfirm({ _nodeData, replaced }: { _nodeData: any; replaced: any }) {
    loading.value = true
    // 空数据节点选择更换数据
    if (from.value === 'empty-dataset') {
      setDataIntoEmptyDatasetNode(_nodeData)
    } else {
      // 已有数据节点更换数据
      changeDataIntoExistDatasetNode(_nodeData, replaced)
    }
    loading.value = false
    visible.value = false
  }

  return {
    visible,
    loading,
    from,
    setVisible,
    setFrom,
    onConfirm
  }
}
