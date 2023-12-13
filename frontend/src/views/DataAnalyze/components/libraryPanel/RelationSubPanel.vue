<template>
  <div class="sql-sub-panel">
    <div class="panel-title" draggable="true" @dragstart="console.log('sdfsfsdf')">关系节点<icon-font type="icon-a-jiantoubeifen2" class="btn-close" @click="emit('close')" /></div>
    <div class="panel-content">
      <ul class="sql-list">
        <li class="row" draggable="true" @dragstart="onStartDrag()">
          <div class="left">
            <icon-font type="icon-sql2" class="sql-icon-font" />
            <span>自定义sql</span>
          </div>
          <!-- <el-dropdown>
              <icon-font type="icon-daohang-gengduocaozuobeifen" class="more" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>定位</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown> -->
        </li>
      </ul>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted } from 'vue'
import { NODE_TYPES } from '@/constants/dataGraph'

const emit = defineEmits(['close', 'startDrag', 'select'])

onMounted(() => {})

function onStartDrag() {
  emit('startDrag', {
    name: '空sql节点',
    type: NODE_TYPES.SQL,
    dataset: {
      sql: ''
    }
  })
}
</script>

<style lang="less" scoped>
.sql-sub-panel {
  width: 208px;
  height: 480px;

  .panel-title {
    height: 36px;
    line-height: 36px;
    background-color: #ededf4;
    padding: 0 12px;
    display: flex;
    justify-content: space-between;
  }

  .btn-close {
    transform: rotate(180deg);
    padding: 0 4px;
    margin-bottom: 3px;
    cursor: pointer;
  }

  .sql-list {
    list-style: none;
    padding: 0 !important;
    margin: 0;
    height: calc(100% - 32px - 11px);
    overflow: auto;
    .row {
      height: 30px;
      padding: 0 12px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 6px;
      cursor: pointer;

      &:hover {
        background-color: #eff0fe;
        .more {
          display: flex;
          align-items: center;
        }
      }
      .left {
        display: flex;
      }
      .more {
        display: none;
        color: #6771fc;
      }
    }
    .sql-icon-font {
      margin-right: 5px;
      display: flex;
      justify-content: center;
      align-items: center;
    }
    :deep(:focus-visible) {
      outline: none;
    }
  }
}
</style>
