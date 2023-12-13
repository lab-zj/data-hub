<template>
  <slot name="overviewHeader"></slot>
  <div class="algo-overview four-scroll-y">
    <div class="algo-select-panel">
      <span>算子：</span>
      <div class="select-self" :style="{ borderColor: isAlgoPanelShow ? '#6771FF' : '#E9E9E9' }" @click="isAlgoPanelShow = !isAlgoPanelShow">
        <span v-if="selectedAlgo">{{ selectedAlgo.name }}</span>
        <span v-else class="placeholder">请选择</span>
        <el-icon v-if="!isAlgoPanelShow"><ArrowLeft /></el-icon>
        <el-icon v-else><ArrowDown /></el-icon>
      </div>
      <!-- <div class="icon-tools">
        <el-tooltip content="重置" placement="top">
          <icon-font type="icon-zhongzhicanshu" @click="reset" />
        </el-tooltip>
        <el-tooltip content="帮助文档" placement="top">
          <icon-font type="icon-bangzhuwendang" />
        </el-tooltip>
      </div> -->
    </div>
    <CommonCollapse title="算子输入" class="algo-inputs">
      <template #content>
        <AlgoInputs :id="props.data.id" :input-list="inputList" @change="onChange" />
      </template>
    </CommonCollapse>
    <CommonCollapse title="算子输出" class="algo-outputs">
      <template #content>
        <!-- <span class="tips">提示：该算子支持多个输出，请配置要显示的输出源</span> -->
        <div class="row">
          <ul class="list">
            <li v-for="(value, key) in algooutput" :key="key">
              {{ `${key}：${value}` }}
            </li>
          </ul>
        </div>
      </template>
    </CommonCollapse>
    <CommonCollapse title="参数配置" class="algo-config">
      <template #content>
        <div class="fullscreen" @click="toggle(true)">
          <el-tooltip effect="dark" content="展开" placement="top">
            <icon-font type="icon-a-quanping2" class="fullscreen-icon" />
          </el-tooltip>
        </div>
        <YamlEditor :yaml-content="valuesYaml" @onContentChange="handleYamlContentChange" />
        <div class="btns">
          <el-button type="primary" :loading="loading" @click="save">保存</el-button>
        </div>
      </template>
    </CommonCollapse>
    <CommonCollapse title="失败原因" class="algo-reason algo-failed" v-if="data && data.status && data.status.result === 'failed'">
      <template #content>
        {{ data.status.message || '-' }}
      </template>
    </CommonCollapse>
    <AlgoSubPanel v-if="isAlgoPanelShow" :has-header="false" :has-footer="false" :has-operate="false" @select="onSelect" />
  </div>
  <div>
    <el-drawer v-model="isLarge" direction="rtl" :with-header="false" modal-class="large-editor" @close="closeDrawer">
      <div class="fullscreen indrawer" @click="toggle(false)">
        <el-tooltip effect="dark" content="折叠" placement="top">
          <icon-font type="icon-quanping" class="fullscreen-icon" />
        </el-tooltip>
      </div>
      <YamlEditor v-if="isLarge" :yaml-content="valuesYaml" @onContentChange="handleYamlContentChange" />
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import AlgoSubPanel from '../common/AlgoSubPanel.vue'
import CommonCollapse from '@/components/common/CommonCollapse.vue'
import AlgoInputs from '../common/AlgoInputs.vue'
import { convertParams } from '../../util/util'
import useAlgorithm from '../../hooks/useAlgorithm'
import useGraphInteraction from '@/views/DataAnalyze/hooks/useGraphInteraction'
import useSetInputParams from '@/views/DataAnalyze/hooks/useSetInputParams'
import yaml from 'js-yaml'
import YamlEditor from '@/components/algorithm/YamlEditor.vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  },
  rawData: {
    type: Object,
    default: () => {}
  }
})

const { getBusinessAlgorithm, modifyAlgorithm, loading } = useAlgorithm()
const { changeAlgoIntoExistAlgoNode } = useGraphInteraction()

const isAlgoPanelShow: any = ref(false)
let selectedAlgo: any = ref(null)
const algooutput: any = ref(null)
const inputList: any = ref(null)
const valuesYaml = ref('') // yaml配置文件

watch(
  () => props.data,
  async () => {
    console.log('props.data', props.data)
    inputList.value = convertParams(props.data.dataset.inputParamTemplate) || []
    console.log('inputList.value:', inputList.value)
    algooutput.value = parseOutputParam(props.data.dataset.outputParamTemplate)
    selectedAlgo.value = await getBusinessAlgorithm(props.data.dataset.algorithm)
    valuesYaml.value = selectedAlgo.value?.algorithm?.configurationYaml || ''
  },
  { immediate: true, deep: true }
)

function onSelect(algo: any) {
  console.log('onSelect:', algo)
  if (+algo.id === +props.data.dataset.algorithm) {
    ElMessage.warning('同一个算法，请重新选择！')
    return
  }
  isAlgoPanelShow.value = false
  selectedAlgo.value = algo
  // 更改节点内部的算子
  const { input, output } = algo.algorithm?.paramInfoTemplateYaml ? yaml.load(algo.algorithm.paramInfoTemplateYaml) : {}
  const { taskId, taskUuid } = props.data.dataset
  const inputParamTemplate = input ? JSON.stringify(input) : ''
  const outputParamTemplate = output ? JSON.stringify(output) : ''
  inputList.value = convertParams(inputParamTemplate) || []
  algooutput.value = parseOutputParam(outputParamTemplate)
  changeAlgoIntoExistAlgoNode({
    algorithm: algo.id,
    directory: false,
    inputParamTemplate,
    outputParamTemplate,
    taskId,
    taskUuid
  })
}
/**
 * 解析输出
 * @param _template
 */
function parseOutputParam(_template: string) {
  if (!_template) {
    return undefined
  }
  const _parse = JSON.parse(_template)
  return _parse
}

// 修改yaml配置内容
const handleYamlContentChange = (newValue: string) => {
  valuesYaml.value = newValue
}

/** 设置入参相关  */
const { onConfirm: onSetInputParamsConfirm } = useSetInputParams()

function onChange(_inputList: any) {
  onSetInputParamsConfirm({
    cellId: props.data.id,
    inputList: _inputList
  })
}

function save() {
  const params = {
    ...selectedAlgo.value,
    algorithm: {
      ...selectedAlgo.value.algorithm,
      configurationYaml: valuesYaml.value
    }
  }
  modifyAlgorithm(params, '保存成功')
}

/**
 * 编辑器放大
 */
const isLarge = ref(false)
function toggle(_isLarge: boolean) {
  isLarge.value = _isLarge
}

function closeDrawer() {}

function reset() {}
</script>
<style lang="less" scoped>
@import '@/styles/scroll-bar';
.algo-overview {
  padding: 0 12px;
  height: calc(100% - 44px);
  overflow: auto;
  .algo-select-panel {
    font-size: 14px;
    display: flex;
    flex-direction: row;
    align-items: center;
    height: 60px;
    border-bottom: 1px solid #e9e9e9;
    .select-self {
      width: 168px;
      height: 28px;
      background: #ffffff;
      border-radius: 2px;
      border: 1px solid #e9e9e9;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px;
      cursor: pointer;
      margin-left: 5px;
      .placeholder {
        color: #dcdfe6;
      }
    }
    .icon-tools {
      margin-left: 4px;
      .iconfont {
        margin-left: 9px;
        cursor: pointer;
      }
    }
  }

  .algo-inputs {
  }
  .algo-outputs {
    .list {
      list-style: none;
      margin: 0;
      padding: 0;
    }
    .tips {
      font-size: 12px;
      color: #222432;
      opacity: 0.5;
    }
  }

  .algo-config {
    .btns {
      display: flex;
      justify-content: center;
      margin-top: 10px;
    }
    .save-btn {
      margin-top: 20px;
    }
    :deep(.content) {
      height: 400px;
      padding-bottom: 40px;
    }
  }

  .algo-failed {
    :deep(.header) {
      strong {
        color: #f56c6c;
      }
    }
    :deep(.content) {
      color: #f56c6c;
    }
  }

  :deep(.algo-sub-panel) {
    width: 216px;
    height: 364px;
    background: #ffffff;
    box-shadow: 0px 2px 8px 0px rgba(0, 0, 0, 0.18);
    position: absolute;
    top: 89px;
    left: 60px;
    z-index: 9;
    .panel-content {
      height: 100%;
    }
  }
  .iconfont {
    &:hover {
      color: #6973ff;
    }
  }
}
.fullscreen {
  width: 26px;
  height: 26px;
  position: absolute;
  right: 13px;
  z-index: 9;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  &:hover {
    background: #eaeafd;
  }
}
:deep(.YamlEditor) {
  height: 100% !important;
}
</style>
<style lang="less" scoped>
:deep(.el-drawer) {
  width: 50% !important;
}
</style>
