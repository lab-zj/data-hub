<template>
  <div class="UploadAlgorithmView">
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="125px" label-position="right">
      <el-form-item ref="dirPath" label="选择算法包：" prop="dirPath" style="flex: 1; margin-right: 5px" @click.stop>
        <div class="uploadFormItem">
          <el-input v-model="formData.dirPath" placeholder="请选择算法" @click="onFocus" />
          <SelectAlgorithmPanel v-show="isSelectAlgorithmVisible" @select="onSelect" @close="isSelectAlgorithmVisible = false" />
        </div>
      </el-form-item>
      <el-form-item ref="algorithmName" label="自定义算法名：" prop="algorithmName" name="algorithmName" style="flex: 1; margin-right: 5px">
        <el-input v-model="formData.algorithmName" placeholder="请输入算法名称" :maxLength="33" />
      </el-form-item>
    </el-form>

    <el-button type="primary" link @click="advanceConfigShow = !advanceConfigShow">
      高级配置
      <icon-font type="icon-a-jiantoubeifen2" :class="advanceConfigShow ? 'arrow arrowDown' : 'arrow arrowLeft'" />
    </el-button>

    <!--高级配置-->
    <div v-if="advanceConfigShow" class="advancedConfig">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item ref="baseImage" label="选择环境：" prop="baseImage">
          <el-select v-model="formData.baseImage" value-key="id" :teleported="false" placeholder="请选择" style="width: 100%" v-loadmore="onDockerScroll">
            <el-option
              v-for="item in dockerContent.list || []"
              :title="`${item.registry}/${item.repository}:${item.tag}`"
              :key="JSON.stringify(item)"
              :value="item"
              :label="`${item.registry}/${item.repository}:${item.tag}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item ref="applicationType" label="服务类型：" prop="applicationType">
          <el-select v-model="formData.applicationType" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in applicationTypeOptions" :key="item.name" :value="item.name">
              {{ item.name }}
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <el-button link @click="paramConfigShow = !paramConfigShow" style="margin-bottom: 8px">
        参数配置
        <icon-font type="icon-a-jiantoubeifen2" :class="paramConfigShow ? 'arrow arrowDown' : 'arrow arrowLeft'" />
      </el-button>

      <YamlEditor v-if="paramConfigShow" :yaml-content="valuesYaml" @onContentChange="handleYamlContentChange" />
    </div>

    <!--上传按钮-->
    <div class="uploadButton">
      <el-button @click="emit('onCancel')">取消</el-button>
      <el-button type="primary" :disabled="!uploadEnable" @click="handleSubmit">立即上传</el-button>
    </div>
  </div>

  <!--成功对话框-->
  <el-dialog v-model="dialogVisible" centered width="400px">
    <template #header>
      <div class="success-header">
        <icon-font type="icon-check-circle-fill" class="success-iconfont" />
        您上传的算法已经部署完毕
      </div>
    </template>
    <div>
      算法ID：
      <span>{{ algorithmId }}</span>
      <icon-font type="icon-fuzhiziduan" class="copy-iconfont" @click="copyToClipboard(algorithmId)" />
    </div>
    <div>
      算法名称：
      <span>{{ algorithmNameByUpload }}</span>
      <icon-font type="icon-fuzhiziduan" class="copy-iconfont" @click="copyToClipboard(algorithmNameByUpload)" />
    </div>
    <div>
      调用地址：
      <span>{{ outerServerAddress }}</span>
      <icon-font type="icon-fuzhiziduan" class="copy-iconfont" @click="copyToClipboard(outerServerAddress)" />
    </div>

    <template #footer>
      <el-button type="primary" @click="iKnow"> 知道了</el-button>
    </template>
  </el-dialog>

  <!--加载蒙层-->
  <LoadingMask :is-loading="isLoading" :loading-text="loadingText" />
</template>

<script setup lang="ts">
import { algorithmNameExist, releaseAlgorithm, deployAlgorithm, getAlgorithmState, uploadAlgorithm, getApplicationTypeList, getBusinessAlgorithmById } from '@/api/algorithm'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import LoadingMask from '@/components/algorithm/LoadingMask.vue'
import YamlEditor from '@/components/algorithm/YamlEditor.vue'
import { ElMessage } from 'element-plus'
import SelectAlgorithmPanel from './SelectAlgorithmPanel.vue'
import useTxtPreview from '@/components/hooks/useTxtPreview'
import { copyToClipboard } from '@/util/util'
import useDocker from '@/components/hooks/useDocker'
import yaml from 'js-yaml'

const emit = defineEmits(['onCancel', 'refresh'])

// 表单信息
const formData = reactive({
  dirPath: '',
  configurationYaml: '',
  algorithmName: '',
  baseImage: '',
  applicationType: ''
})

// 表单规则
const formRules = {
  baseImage: [{ required: true, message: '请选择镜像', trigger: 'blur' }],
  dirPath: [
    { required: true, message: '请选择算法包', trigger: 'blur' },
    { required: true, message: '请选择算法包', trigger: 'change' }
  ],
  algorithmName: [
    {
      required: true,
      message: '算法名长度不得超过32',
      min: 0,
      max: 32,
      trigger: 'change'
    },
    {
      required: true,
      message: '请输入算法名',
      trigger: 'blur'
    },
    { validator: checkAlgorithmName, trigger: 'change' },
    { validator: checkAlgorithmName, trigger: 'blur' }
  ],
  applicationType: [{ required: true, message: '请选择服务类型', trigger: 'blur' }]
}

const applicationTypeOptions = ref([]) // 服务类型选项
const dialogVisible = ref(false) // 对话框是否显示
const isLoading = ref(false) // 是否正在加载中
const loadingText = ref('loading text') // loading文本
const algorithmId = ref('') // 算法id
const algorithmNameByUpload = ref('') // 算法名称
const outerServerAddress = ref('') // 调用地址
const valuesYaml = ref('') // 配置文件上传
const algoNameReg = ref(/^[A-z][A-Za-z0-9]*$/)
// 是否可上传
const uploadEnable = computed(() => {
  const { dirPath, baseImage, applicationType, algorithmName } = formData
  return dirPath !== '' && baseImage !== '' && applicationType !== '' && algorithmName !== '' && algoNameReg.value.test(algorithmName)
})

const formRef = ref(null) // 表单实例

// 显示高级设置
const advanceConfigShow = ref(false)

// 显示参数配置
const paramConfigShow = ref(false)

function checkAlgorithmName(rule: any, value: any, callback: any) {
  const isMatch = algoNameReg.value.test(value)
  if (isMatch) {
    callback()
  } else {
    return callback(new Error('只能输入字母、数字，且必须以字母开头'))
  }
}

/** 选择算法文件夹 */
const isSelectAlgorithmVisible: any = ref(false)
const selectAlgorithmFile: any = ref(null) // 选择的算法文件夹

const { txtPreviewContent, txtPreview } = useTxtPreview()

function onFocus() {
  isSelectAlgorithmVisible.value = !isSelectAlgorithmVisible.value
}

function onSelect(file: any) {
  console.log('file:', file)
  selectAlgorithmFile.value = file
  formData.dirPath = file.path.slice(0, file.path.length - 1)
  // 查询yaml配置信息
  txtPreview(`${file.path}values.yaml`)
}

watch(
  () => txtPreviewContent,
  () => {
    valuesYaml.value = txtPreviewContent.value
  },
  {
    immediate: true,
    deep: true
  }
)

/**
 * 捕获yaml文件解析错误
 * 1、格式不对
 * 2、yaml文件缺失内容
 * @param _yamlStr
 */
function catchYamlException(_yamlStr: string) {
  try {
    yaml.load(_yamlStr)
  } catch (error: any) {
    console.error('YamlException:', error)
    ElMessage.error('yaml文件解析出错，请检查参数配置')
    return true
  }
  return false
}

// 点击组件以外区域关闭算法文件选择面板
function clickToCloseSelectAlgorithmPanel() {
  document.addEventListener(
    'click',
    () => {
      isSelectAlgorithmVisible.value = false
    },
    false
  )
}

// 轮询信息
const roundQueryInfo = (type: 'release' | 'deploy') => {
  return new Promise((resolve, reject) => {
    const interval = setInterval(async () => {
      const infoResponse = await getAlgorithmState({
        type,
        algorithmId: algorithmId.value
      })
      if (infoResponse.data.code === 200) {
        const status = infoResponse.data.data.status
        if (status === 'SUCCEED' || status === 'FAILED') {
          clearInterval(interval)
          resolve(infoResponse.data.data.status)
        }
      } else {
        reject()
      }
    }, 10000)
  })
}

// 点击上传
const handleSubmit = async () => {
  // 判断算法名是否存在
  const response = await algorithmNameExist({
    data: {
      name: formData.algorithmName,
      version: 'v1.0'
    }
  })

  if (response.data.code === 200) {
    if (!response.data.data) {
      const isYamlException = catchYamlException(valuesYaml.value)
      if (isYamlException) {
        return
      }

      const { algorithmName, baseImage, dirPath } = formData

      isLoading.value = true
      loadingText.value = 'Uploading'

      const { values, configuraion, params } = yaml.load(valuesYaml.value)
      const { baseImageId, ...rest } = baseImage as any

      // 算法上传
      const uploadResponse = await uploadAlgorithm({
        data: {
          algorithm: {
            valuesYaml: yaml.dump(values),
            configurationYaml: yaml.dump(configuraion),
            paramInfoTemplateYaml: yaml.dump(params),
            registry: 'docker-registry-ops-dev.lab.zjvis.net:32443', // 固定的
            baseImage: {
              ...rest,
              id: baseImageId
            },
            dirPath
          },
          name: algorithmName,
          version: 'v1.0',
          type: 'custom'
        }
      })

      // 如果上传成功了
      if (uploadResponse.data.code === 200) {
        algorithmId.value = uploadResponse.data.data.id
        algorithmNameByUpload.value = uploadResponse.data.data.name
        await handleAlgorithmRelease()
      } else {
        isLoading.value = false
        ElMessage.error(uploadResponse.data.message)
      }
    } else {
      ElMessage.error('算法名已存在，请更改后重新进行上传操作')
    }
  } else {
    ElMessage.error(response.data.message)
    isLoading.value = false
  }
}

// 算法打包
const handleAlgorithmRelease = async () => {
  loadingText.value = 'Building'

  // 算法打包
  const releaseResponse = await releaseAlgorithm({
    algorithmId: algorithmId.value
  })

  if (releaseResponse.data.code === 200) {
    const status = await roundQueryInfo('release')
    if (status === 'SUCCEED') {
      await handleAlgorithmDeploy()
    } else {
      ElMessage.error('算法打包失败')
      isLoading.value = false
    }
  } else {
    isLoading.value = false
    ElMessage.error(releaseResponse.data.message)
  }
}

// 算法部署
const handleAlgorithmDeploy = async () => {
  loadingText.value = 'Deploying'

  // 算法部署
  const deployResponse = await deployAlgorithm({
    algorithmId: algorithmId.value
  })

  if (deployResponse.data.code === 200) {
    const status = await roundQueryInfo('deploy')
    if (status === 'SUCCEED') {
      ElMessage.success('算法部署成功')

      // 获取部署信息
      const response = await getBusinessAlgorithmById({
        data: {
          id: algorithmId.value
        }
      })

      if (response.data.code === 200) {
        outerServerAddress.value = response.data.data.outerServerAddress
        dialogVisible.value = true
      } else {
        ElMessage.error(deployResponse.data.message)
      }
    } else {
      ElMessage.error('算法部署失败')
    }
  } else {
    ElMessage.error(deployResponse.data.message)
  }

  isLoading.value = false
}

// 修改yaml配置内容
const handleYamlContentChange = (newValue: string) => {
  valuesYaml.value = newValue
}

const { content: dockerContent, queryList: queryDockerList, onScroll: onDockerScroll } = useDocker()

onMounted(() => {
  // 获取所有base image
  queryDockerList()

  // 获取所有服务类型
  getApplicationTypeList().then((res: any) => {
    if (res.data.code === 200) {
      const keys = Object.keys(res.data.data)
      // @ts-ignore
      applicationTypeOptions.value = keys.map((key) => {
        return {
          name: key,
          value: res.data.data[key]
        }
      })
    }
  })

  clickToCloseSelectAlgorithmPanel()
})

/**
 * 点击我知道了，关闭两个对话框
 */
function iKnow() {
  dialogVisible.value = false
  emit('onCancel')
  emit('refresh')
}
</script>

<style lang="less" scoped>
.UploadAlgorithmView {
  background-color: #ffffff;
}

.uploadFormItem {
  width: 100%;

  .acceptType {
    margin-top: -5px;
    color: #adaeb3;
    font-size: 13px;
  }

  .uploadBottom {
    display: flex;
    align-items: center;
  }

  .uploadStatus {
    flex: 1;
    margin-left: 45px;
  }
}

.uploadButton {
  text-align: right;
  margin-top: 10px;
}

.advancedConfig {
  margin-top: 10px;
  background: #f3f3f7;
  padding: 10px;
}

:deep(.el-upload) {
  display: block;
}

:deep(.el-upload-list) {
  margin: 0;
}

:deep(.el-upload-list) {
  position: absolute;
  left: 120px;
  width: calc(100% - 150px);
  top: 0;
}
.arrow {
  margin-left: 7px;
}
.arrowLeft {
  transform: rotate(-180deg);
}
.arrowDown {
  transform: rotate(90deg);
}
.iconfont {
  cursor: pointer;
  display: flex;
  &:hover {
    color: #6973ff;
  }
}
.copy-iconfont {
  cursor: pointer;
}
.success-header {
  font-size: 16px;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
}
.success-iconfont {
  font-size: 20px;
  color: #5cb464;
  display: flex;
  align-items: center;
  margin-right: 10px;
}
:deep(.el-select-dropdown__wrap) {
  height: 200px;
  overflow: auto;
}
</style>
