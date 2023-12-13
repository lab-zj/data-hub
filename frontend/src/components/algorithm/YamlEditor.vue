<template>
  <div class="YamlContainer">
    <div ref="yamlEditorRef" id="YamlEditor" class="YamlEditor" />
  </div>
</template>

<script setup lang="ts">
import * as monaco from 'monaco-editor'
import { setDiagnosticsOptions } from 'monaco-yaml'
import { parseToSchema } from '@/util/yaml-adapter'
import { ref, onMounted, onUnmounted, watch } from 'vue'

import YamlWorker from './yaml.worker.js?worker'

// @ts-ignore
window.MonacoEnvironment = {
  getWorker(moduleId, label) {
    switch (label) {
      // Handle other cases
      case 'yaml':
        return new YamlWorker()
      default:
        throw new Error(`Unknown label ${label}`)
    }
  }
}

const emit = defineEmits(['onContentChange'])
const props = defineProps({
  yamlContent: {
    type: String,
    default: ''
  },
  template: {
    type: String,
    default: ''
  }
})

// 编辑器实例
let yamlEditor: any = {}
const yamlEditorRef: any = ref(null)

watch(
  () => props.yamlContent,
  (value) => {
    if (value !== yamlEditor.getValue()) {
      yamlEditor.setValue(value)
    }
  }
)

watch(
  () => props.template,
  (value) => {
    setSchema()
  }
)

const setSchema = () => {
  const defaultSchema = parseToSchema(props.template)

  // console.log(defaultSchema)

  const schemas = {
    // 可以不管好像，算是一个default值，如果没提供schema，则这边下
    uri: 'https://github.com/remcohaszing/monaco-yaml/blob/HEAD/examples/demo/src/schema.json',
    // @ts-expect-error TypeScript can’t narrow down the type of JSON imports
    schema: defaultSchema,
    fileMatch: ['monaco-yaml.yaml']
  }

  setDiagnosticsOptions({
    enableSchemaRequest: true,
    hover: true,
    completion: true,
    validate: true,
    format: true,
    schemas: [schemas]
  })
}

onMounted(() => {
  if (yamlEditorRef.value) {
    setSchema()

    // The uri is used for the schema file match.
    // const modelUri = monaco.Uri.parse('monaco-yaml.yaml')

    // 创建编辑器
    yamlEditor = monaco.editor.create(yamlEditorRef.value, {
      value: props.yamlContent,
      automaticLayout: true,
      // Monaco-yaml features should just work if the editor language is set to 'yaml'.
      language: 'yaml',
      // model: monaco.editor.createModel(props.yamlContent, 'yaml', modelUri),
      quickSuggestions: { other: true, strings: true }
    })

    // 编辑器内容发生改变时触发
    yamlEditor.onDidChangeModelContent(() => {
      emit('onContentChange', yamlEditor.getValue())
    })
  }
})

onUnmounted(() => {
  const model = yamlEditor.getModel()
  yamlEditor.dispose()
  model.dispose()
})
</script>

<style scoped>
.YamlContainer {
  height: 100%;
  border: 1px solid #d4d4d4;
}

.YamlEditor {
  height: 250px;
}
</style>
