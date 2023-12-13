<script setup lang="ts">
import { ref } from 'vue'
import {useI18n} from 'vue-i18n'
const { t } = useI18n()

defineProps<{ msg: string }>()

console.log('国际化信息 ===>', t('modal.cancelText'))

const count = ref(0)
</script>

<template>
  <h1>{{ msg }}</h1>

  <el-button type="primary">主题色按钮</el-button>

  <div>
    <h2>国际化信息：{{ $t("modal.acceptText") }}</h2>
  </div>
  <div class="card">
    <button type="button" @click="count++">count is {{ count }}</button>
    <p>
      Edit
      <code>components/HelloWorld.vue</code> to test HMR
    </p>
  </div>

  <p>
    Check out
    <a href="https://vuejs.org/guide/quick-start.html#local" target="_blank"
      >create-vue</a
    >, the official Vue + Vite starter
  </p>
  <p>
    Install
    <a href="https://github.com/vuejs/language-tools" target="_blank">Volar</a>
    in your IDE for a better DX
  </p>
  <p class="read-the-docs">Click on the Vite and Vue logos to learn more</p>
</template>

<style scoped>
.read-the-docs {
  color: #888;
}
</style>
