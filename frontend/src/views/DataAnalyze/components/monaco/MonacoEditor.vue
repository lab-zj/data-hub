<template>
  <div class="monacoeditor-content-box">
    <div ref="codeEditBox" class="code-edit-box"></div>
  </div>
</template>
<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as monaco from 'monaco-editor'
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

const props = defineProps({
  contentValue: {
    type: String,
    default: ''
  },
  metions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update'])

const codeEditBox: any = ref(null)
const text: any = ref(props.contentValue)
const provider: any = ref(null)
let editor: any = null

onMounted(() => {
  initEditor()
})

watch(
  () => props.contentValue,
  () => {
    text.value = props.contentValue
    editor.setValue(text.value)
  }
)
/**
 * 自动提示
 */
function registerCompletion() {
  provider.value = monaco.languages.registerCompletionItemProvider('sql', {
    provideCompletionItems: function (model, position) {
      // 获取当前行数、当前列数
      const { lineNumber, column } = position
      const suggestions: any = []
      // 设置提示内容
      for (let i = 0; i < props.metions?.length; i++) {
        suggestions.push({
          label: props.metions[i].showName, // 显示的提示内容
          kind: monaco.languages.CompletionItemKind['Field'], // 用来显示提示内容后的不同的图标
          insertText: props.metions[i].value // 选择后粘贴到编辑器中的文字
        })
      }
      return {
        suggestions: suggestions,
        dispose: () => {
          editor.executeEdits('', [
            {
              // new monaco.Range(lineNumber, column - 1, lineNumber, column) 改成
              // new monaco.Range(lineNumber, column, lineNumber, column)
              // 这里不是用的特殊字符调出suggest，不需要回退一列把特殊字符给去掉
              range: new monaco.Range(lineNumber, column, lineNumber, column),
              text: null
            }
          ])
        }
      }
    },
    triggerCharacters: ['']
  })
}

function initEditor() {
  // @ts-ignore
  self.MonacoEnvironment = {
    getWorker(_: any, label: string) {
      if (label === 'json') {
        return new jsonWorker()
      }
      if (label === 'css' || label === 'scss' || label === 'less') {
        return new cssWorker()
      }
      if (label === 'html' || label === 'handlebars' || label === 'razor') {
        return new htmlWorker()
      }
      if (label === 'typescript' || label === 'javascript') {
        return new tsWorker()
      }
      return new editorWorker()
    }
  }
  registerCompletion()
  editor = monaco.editor.create(codeEditBox.value, {
    value: text.value,
    theme: 'CodeSampleTheme', //官方自带三种主题vs, hc-black, or vs-dark
    language: 'sql',
    automaticLayout: true, // 自动布局
    glyphMargin: true, // 字形边缘
    folding: true, // 代码可折叠
    foldingStrategy: 'indentation', // 代码可分小段折叠
    renderLineHighlight: 'line', // 行亮
    overviewRulerBorder: true, // 不要滚动条的边框
    minimap: {
      enabled: true
    }, // 预览小地图
    scrollBeyondLastLine: false, // 滚动完最后一行后再滚动一屏幕
    colorDecorators: true, // 颜色装饰器
    disableLayerHinting: true, // 等宽优化
    // codeLens: false, // 代码镜头
    acceptSuggestionOnCommitCharacter: true, // 接受关于提交字符的建议
    acceptSuggestionOnEnter: 'on',
    quickSuggestions: false, // 默认的提示关掉
    wordWrap: 'on',
    lineNumbers: 'on' // 控制行号展示，设置为off，左边还是会有空间
  })

  editor.onDidChangeModelContent(() => {
    text.value = editor.getValue()
    emit('update', text.value)
  })
  //添加按键监听,弹出suggest提示
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyF, function () {
    if (!props.metions || props.metions?.length === 0) {
      console.log('no suggest')
      return
    }
    editor.trigger('', 'editor.action.triggerSuggest', () => {})
  })
}

onBeforeUnmount(() => {
  provider.value.dispose()
  editor.dispose()
})
</script>
<style lang="less" scoped>
.monacoeditor-content-box {
  height: 100%;
  border: 1px solid #d4d4d4;
  .code-edit-box {
    height: 100%;
  }
  // :deep(.suggest-widget) {
  //   left: -6px !important;
  //   width: 200px !important;
  // }
}
</style>
