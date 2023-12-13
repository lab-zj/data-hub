import { computed } from 'vue'
import { useDataStore } from '@/stores/dataStore'

export default function useListMode() {
  const dataStore = useDataStore()
  const isCard = computed(() => dataStore.mode === 'card')

  function onSwitch() {
    if (!dataStore.mode) {
      resetMode()
    }
    dataStore.mode = dataStore.mode === 'table' ? 'card' : 'table'
  }

  function resetMode() {
    dataStore.mode = 'table'
  }

  return {
    isCard,
    onSwitch,
    resetMode
  }
}
