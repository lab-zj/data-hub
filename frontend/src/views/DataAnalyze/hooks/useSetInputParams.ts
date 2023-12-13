import { ref } from 'vue'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'

/**
 * 算子节点设置入参
 * @returns
 */
export default function useSetInputParams() {
  const { setInputParamsOnAlgoNode } = useGraphInteraction()

  const visible: any = ref(false)
  const loading: any = ref(false)

  function setVisible(_visible: boolean) {
    visible.value = _visible
  }

  function onConfirm({ cellId, inputList }: { cellId: string | number; inputList: any }) {
    console.log('onSetInputParamsConfirm:', cellId, inputList)
    loading.value = true
    setInputParamsOnAlgoNode({
      cellId,
      inputList
    })
    loading.value = false
    visible.value = false
  }

  return {
    visible,
    loading,
    setVisible,
    onConfirm
  }
}
