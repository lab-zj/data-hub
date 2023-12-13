<template>
  <el-alert class="graph-running-alert" :title="getTime()" type="warning" show-icon :style="styleOptions" />
</template>
<script setup lang="ts">
import { reactive, watch } from 'vue'
import { useDataGraph } from '@/stores/dataGraphStore'
import moment from 'moment'

const props = defineProps({
  isChange: {
    type: Boolean,
    default: false
  }
})

const dataGraphStore = useDataGraph()

const styleOptions = reactive({
  width: '',
  left: ''
})

watch(
  () => props.isChange,
  (val: boolean) => {
    styleOptions.width = val ? 'calc(100% - 64px)' : 'calc(100% - 120px)'
    styleOptions.left = val ? '64px' : '120px'
  },
  {
    immediate: true
  }
)
/**
 * 时间戳差值转xx天xx小时xx分xx秒
 */
function getTime() {
  if (!dataGraphStore?.current_status) {
    return '-'
  }
  const gmtCreate = moment(dataGraphStore?.current_status?.gmtCreate)
  const now = moment()
  const duration = moment.duration(now.diff(gmtCreate, 'ms')) //做差
  const days = duration.get('days')
  const hours = duration.get('hours')
  const mins = duration.get('minutes')
  const ss = duration.get('seconds')
  return `项目运行中，已执行${days}天${hours}小时${mins}分钟${ss}秒`
}
</script>
<style lang="less" scoped>
.graph-running-alert {
  position: absolute;
  top: 44px;
}
</style>
