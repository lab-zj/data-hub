# vscode 国际化配置
> 辅助开发时管理国际化文案
- vscode 中安装i18n Ally 插件
- 在项目的frontend目录下添加.vscode 文件夹，文件夹内新建 settings.json 并添加如下配置：
```json
{
  "i18n-ally.localesPaths": "src/i18n", // 相对于项目根目录的语言环境目录路径
  "i18n-ally.sourceLanguage": "zh", // 根据此语言文件翻译其他语言文件的变量和内容
  "i18n-ally.displayLanguage": "zh", // 显示语言
  "i18n-ally.keystyle": "nested", // 翻译后变量格式 nested：嵌套式  flat:扁平式
  "i18n-ally.preferredDelimiter": "_",
  "i18n-ally.sortKeys": true,
  "i18n-ally.extract.keygenStrategy": "random", // 生成密钥路径的策略。可以slug，random或empty
  "i18n-ally.extract.keygenStyle": "camelCase", // 翻译字段命名样式采用驼峰
  "i18n-ally.enabledParsers": ["json", "js", "ts"],
  "i18n-ally.keysInUse": ["view.progress_submenu.translated_keys", "view.progress_submenu.missing_keys", "view.progress_submenu.empty_keys"],
  "i18n-ally.theme.annotationMissingBorder": "#d37070",
  "i18n-ally.theme.annotationMissing": "#d37070"
}
```

# Vue 3 + TypeScript + Vite

This template should help get you started developing with Vue 3 and TypeScript in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

## Recommended IDE Setup

- [VS Code](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin).

## Type Support For `.vue` Imports in TS

TypeScript cannot handle type information for `.vue` imports by default, so we replace the `tsc` CLI with `vue-tsc` for type checking. In editors, we need [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin) to make the TypeScript language service aware of `.vue` types.

If the standalone TypeScript plugin doesn't feel fast enough to you, Volar has also implemented a [Take Over Mode](https://github.com/johnsoncodehk/volar/discussions/471#discussioncomment-1361669) that is more performant. You can enable it by the following steps:

1. Disable the built-in TypeScript Extension
   1. Run `Extensions: Show Built-in Extensions` from VSCode's command palette
   2. Find `TypeScript and JavaScript Language Features`, right click and select `Disable (Workspace)`
2. Reload the VSCode window by running `Developer: Reload Window` from the command palette.
