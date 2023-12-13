<template>
  <slot name="overviewHeader"></slot>
  <div class="content">
    <div class="preview-header">
      <span class="title">请输入SQL语句</span>
      <span class="view-detail" @click="onView">查看详情</span>
    </div>
    <span class="sql-tips">提示：可使用的快捷键ctrl+f</span>
    <div class="sql-editor-container">
      <div class="fullscreen" @click="toggle(true)">
        <el-tooltip effect="dark" content="展开" placement="top">
          <icon-font type="icon-a-quanping2" class="fullscreen-icon" />
        </el-tooltip>
      </div>
      <MonacoEditor v-if="!isLarge" :contentValue="contentValue" :metions="metions" @update="onUpdate" />
    </div>
  </div>
  <div class="footer">
    <el-button disabled>恢复上一版本</el-button>
    <el-button type="primary" @click="onSave">保存</el-button>
  </div>
  <div>
    <el-drawer v-model="isLarge" direction="rtl" :with-header="false" modal-class="large-editor" @close="closeDrawer">
      <div class="fullscreen indrawer" @click="toggle(false)">
        <el-tooltip effect="dark" content="折叠" placement="top">
          <icon-font type="icon-quanping" class="fullscreen-icon" />
        </el-tooltip>
      </div>
      <MonacoEditor v-if="isLarge" :contentValue="contentValue" :metions="metions" @update="onUpdate" />
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from 'vue'
import MonacoEditor from '../monaco/MonacoEditor.vue'
import useGraphDataFlow from '../../hooks/useGraphDataFlow'

const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

const { fetchSqlTable } = useGraphDataFlow()
const emit = defineEmits(['save', 'view'])

const contentValue = ref('')
const sql = ref('')
let metions: any = ref([])

watch(
  () => props.data,
  async () => {
    contentValue.value = props.data.sql
    sql.value = props.data.sql
    metions.value = await fetchSqlTable()
  },
  { immediate: true, deep: true }
)

function onUpdate(value: string) {
  sql.value = value
}

function onSave() {
  emit('save', sql.value)
}

function onView() {
  const path = props.data.path
  console.log('onView:', path, props.data.status)
  emit('view', {
    ...props.data,
    path
  })
}

/**
 * 编辑器放大
 */
const isLarge = ref(false)
function toggle(_isLarge: boolean) {
  isLarge.value = _isLarge
  contentValue.value = sql.value
}

function closeDrawer() {
  contentValue.value = sql.value
}
</script>
<style lang="less" scoped>
.content {
  padding: 12px;
  height: 100%;
  display: flex;
  flex-direction: column;
  .sql-tips {
    font-size: 12px;
    color: #222432;
    opacity: 0.5;
    margin-bottom: 8px;
  }
  .sql-editor-container {
    height: calc(100% - 40px);
  }
}
.footer {
  border-top: 1px solid #eeeeee;
  padding: 11px;
  display: flex;
  justify-content: center;
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

.indrawer {
  right: 20px !important;
}

.fullscreen-icon {
  color: #6771fc;
}
</style>
<style lang="less" scoped>
:deep(.el-drawer) {
  width: 50% !important;
}
</style>
