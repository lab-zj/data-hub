<template>
  <div class="CallAlgorithmView">
    <el-form ref="formRef" :model="formData" :rules="formRules">
      <el-form-item ref="selectedAlgorithm" label="算法" prop="selectedAlgorithm">
        <div style="display: flex; width: 100%">
          <el-autocomplete
            v-model="formData.selectedAlgorithm"
            placeholder="请选择算法或输入算法服务地址"
            :options="algoNameOptions"
            :fetch-suggestions="fetchSuggestions"
            @focus="fetchAvailableAlgorithm"
            @select="onAlgorithmChange"
            clearable
            style="width: 100%; margin-right: 10px"
          >
            <template #default="{ item }">
              <div>{{ item.label }}</div>
            </template>
          </el-autocomplete>
          <el-button type="primary" :disabled="!callEnable" @click="handleAlgorithmCall"> 调用</el-button>
        </div>
      </el-form-item>
    </el-form>

    <!--yaml 配置窗口-->
    <div style="margin: 10px 0">参数配置：</div>

    <YamlEditor :yaml-content="formData.yamlConfigContent" :template="yamlTemplate" @onContentChange="handleYamlContentChange" />

    <div style="margin: 10px 0">输出结果：</div>
    <el-textarea v-model="algorithmResult" :rows="7" disabled />
  </div>

  <!--加载蒙层-->
  <LoadingMask :is-loading="isLoading" :loading-text="loadingText" />
</template>

<script setup lang="ts">
import { callAlgorithm } from '@/api/algorithm'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { stringToBase64 } from '@/util/util'
import YamlEditor from './YamlEditor.vue'
import LoadingMask from '@/components/algorithm/LoadingMask.vue'
import { sliceComment } from '@/util/yaml-adapter'
import yaml from 'js-yaml'
import { ElMessage } from 'element-plus'
import useAlgorithm from '@/views/DataAnalyze/hooks/useAlgorithm'

const { algoList, initQuery, getAllRelatedAlgorithm, onScroll } = useAlgorithm()

// 表单数据
const formData = reactive({
  selectedAlgorithm: '',
  yamlConfigContent: ''
})

const formRules = reactive({
  selectedAlgorithm: [
    {
      required: true,
      message: '请选择算法或输入算法服务地址',
      trigger: 'blur'
    }
  ]
})

const algoNameOptions = ref([]) // 算法名选项
const yamlTemplate = ref('') // yaml模板
const algorithmResult = ref('') // 函数调用结果
const isLoading = ref(false) // 是否正在加载中
const loadingText = ref('算法运行中') // loading文本
const callEnable = computed(() => {
  const { selectedAlgorithm } = formData
  return selectedAlgorithm !== ''
})

const formRef = ref(null)

// 算法切换时，获取默认模板
const onAlgorithmChange = async (value: any) => {
  const content = JSON.parse(value.value)
  const { outerServerAddress, algorithm } = content
  const { configurationYaml } = algorithm

  formData.selectedAlgorithm = outerServerAddress

  if (configurationYaml) {
    const template =
      '# k均值聚类算法（k-means clustering algorithm）是一种迭代求解的聚类分析算法，其步骤是，预将数据分为K组，则随机选取K个对象作为初始的聚类中心，然后计算每个对象与各个种子聚类中心之间的距离，把每个对象分配给距离它最近的聚类中心。聚类中心以及分配给它们的对象就代表一个聚类。每分配一个样本，聚类的聚类中心会根据聚类中现有的对象被重新计算。这个过程将不断重复直到满足某个终止条件。终止条件可以是没有（或最小数目）对象被重新分配给不同的聚类，没有（或最小数目）聚类中心再发生变化，误差平方和局部最小。\n' +
      '---\n' +
      '#{"chineseName": "算法类型","name": "type"}\n' +
      'type: KMeans\n' +
      '#{"chineseName": "特征列","name": "cols","type": "checkboxGroup","message": "只支持数值型数据","props": {"options": []}}\n' +
      'cols: []\n' +
      'k: 2\n' +
      '#{"chineseName": "最大迭代数","name": "max_iter","type": "inputNumber","message": "输入值为大于1的正整数","props": {"min": 2,"precision": 0},"rules": [{"trigger": ["change","blur"],"message": "不允许为空!请输入大于1的正整数","required": true}]}\n' +
      'max_iter: 300'
    yamlTemplate.value = content.algorithm.configurationYaml
    // yamlTemplate.value = template

    formData.yamlConfigContent = sliceComment(content.algorithm.configurationYaml)
    // formData.yamlConfigContent = sliceComment(template)
  } else {
    formData.yamlConfigContent = ''
  }
}

// 点击调用
const handleAlgorithmCall = async () => {
  const { selectedAlgorithm, yamlConfigContent } = formData
  const outerServerAddress = selectedAlgorithm

  if (outerServerAddress) {
    // http://nebula-dev.lab.zjvis.net:32080/algorithms/algo-0220-65aa6bb5e5734c24a3ad38c27a85ebc7/algorithms/invoke
    // http://nebula-dev.lab.zjvis.net/algorithms/algo-0220-a78bcb8997ea4a79b965420c935abd08/algorithms/invoke
    // 替换掉域名和端口
    // const reg = /http(s)?:\/\/.*:\d+\//
    // const url = serverAddressOuter.replace(reg, '')

    const base64Yaml = stringToBase64(yamlConfigContent)
    const index = outerServerAddress.indexOf('algorithms')
    const url = outerServerAddress.slice(index)
    console.log('url', url, outerServerAddress)

    const passData = {
      url: `http://${outerServerAddress}/${url}/invoke`,
      data: {
        base64_yaml: base64Yaml
      }
    }

    isLoading.value = true
    try {
      const response = await callAlgorithm(passData)
      if (response.data.success) {
        algorithmResult.value = response.data.result
        isLoading.value = false
      }
    } catch {
      ElMessage.error('算法调用失败')
      isLoading.value = false
    }
  } else {
    ElMessage.error('调用地址不存在')
  }
}

watch(
  () => algoList.value,
  () => {
    algoNameOptions.value = algoList.value.map((algo: any) => {
      const { name } = algo
      return {
        label: name,
        value: JSON.stringify(algo)
      }
    })
  }
)

// 获取可用算法
const fetchAvailableAlgorithm = () => {
  initQuery('custom', '')
  getAllRelatedAlgorithm()
  // const response = await getAllRelatedAlgorithmApi()
  // if (response.data.code === 200) {
  //   algoNameOptions.value = response.data.data.content.map((item) => {
  //     const { name } = item
  //     return {
  //       label: name,
  //       value: JSON.stringify(item)
  //     }
  //   })
  // }
}

// 筛选提示项
const fetchSuggestions = (queryString, callback) => {
  const results = queryString
    ? algoNameOptions.value.filter((item) => {
        return item.label.toLowerCase().indexOf(queryString.toLowerCase()) === 0
      })
    : algoNameOptions.value
  callback(results)
}

const handleYamlContentChange = (newValue: string) => {
  formData.yamlConfigContent = newValue
}

onMounted(() => {})
</script>
s
<style scoped>
.CallAlgorithmView {
}

/*.ant-form-item*/
</style>
