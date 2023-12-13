<template>
  <div class="algo-inputs">
    <template v-if="_inputList && _inputList.length > 0">
      <div class="input-item" v-for="(input, index) in _inputList" :key="`input-${index}`">
        <span>{{ input.name }}：</span>
        <el-select placeholder="请选择" v-model="input.value" @change="onChange">
          <el-option v-for="(option, index) in inputSelectOptions" :key="index" :label="option.label" :value="option.value" />
        </el-select>
      </div>
    </template>
    <div class="empty" v-else>该算子没有设置入参</div>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import { cloneDeep } from 'lodash'
import { useDataGraph } from '@/stores/dataGraphStore'

const props = defineProps({
  id: {
    type: [Number, String],
    default: ''
  },
  inputList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['change'])

const dataGraphStore = useDataGraph()

const _inputList: any = ref(null)
const inputSelectOptions: any = ref([])

watch(
  () => [props.id, dataGraphStore.rawData],
  () => {
    _inputList.value = cloneDeep(props?.inputList) || []
    packageInputSelectOptions()
  },
  { immediate: true, deep: true }
)

/**
 * 组装下拉列表
 * 1、获取所有的前置节点、相关的port
 * 2、只有节点，直接使用节点id，有port的情况下，使用节点id#port名称
 */
function packageInputSelectOptions() {
  // 初始化inputList中对应的value
  _inputList.value.forEach((input: any) => {
    input.value = null
  })
  inputSelectOptions.value = []
  // 查找所有以目标节点为终点的连线
  const targets = dataGraphStore.rawData?.edges?.filter((edge: any) => {
    return edge.target.cell === props.id
  })
  // 查找连线中的所有的前置节点信息、port信息
  targets.forEach((edge: any) => {
    // 查找前置节点信息
    const _sourceNode = dataGraphStore.rawData?.nodes?.find((node: any) => node.id === edge.source.cell)
    let _port: any = null
    if (edge.source.port) {
      _port = _sourceNode?.dataset?.outputList?.find((output: any) => output.name === edge.source.port)
    }
    // 组装下拉列表List
    inputSelectOptions.value.push({
      _base: {
        ..._sourceNode,
        port: _port
      },
      label: _port ? `${_sourceNode.name}-${_port.name}` : _sourceNode.name,
      // value: _port ? `${_sourceNode.id}##${_port.name}` : _sourceNode.id
      value: edge.id
    })
    // 组装下拉框值
    _inputList.value.forEach((input: any) => {
      if (input.name === edge.name) {
        // input.value = _port ? `${_sourceNode.id}##${_port.name}` : _sourceNode.id
        input.value = edge.id
      }
    })
  })
}

function onChange() {
  console.log('onChange:', _inputList.value)
  emit('change', _inputList.value)
}
</script>
<style lang="less" scoped>
.algo-inputs {
  .input-item {
    margin-bottom: 16px;
  }

  :deep(.el-input) {
    width: 240px;
    height: 28px;
  }
  :deep(.el-input-number) {
    width: 216px;
    height: 28px;
  }
  :deep(.el-select .el-input .el-select__caret) {
    transform: rotate(90deg);
  }
  :deep(.el-select .el-input .el-select__caret.is-reverse) {
    transform: rotate(0deg);
  }

  .empty {
    display: flex;
    justify-content: center;
    align-items: center;
    color: #b6bdd2;
  }
}
</style>
