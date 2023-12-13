<template>
  <el-scrollbar v-if="_list.length > 0" height="calc(100% - 32px - 11px)">
    <ul class="algo-list" v-infinite-scroll="onScroll">
      <li v-for="algo in _list" :key="algo.name" class="row" draggable="true" @dragstart="onStartDrag(algo)" @click="onClick(algo)">
        <div class="left">
          <icon-font :type="algo && getIconInfo(algo.type, algo.algType)" class="algo-icon-font" />
          <span>{{ algo.name }}</span>
        </div>
        <el-dropdown v-if="hasOperate" @command="(command: any) => handleCommand(command, algo.id)">
          <icon-font type="icon-daohang-gengduocaozuobeifen" class="more" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="item in getMenuList(algo)" :key="item.key" :command="item.key">
                {{ item.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </li>
    </ul>
  </el-scrollbar>
  <div v-show="_list.length === 0" class="empty">
    <img src="@/assets/empty.png" />
  </div>
</template>
<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { nodeIconWithBackGround } from '@/views/DataAnalyze/util/node-icon-util'
import { NODE_TYPES } from '@/constants/dataGraph'
import yaml from 'js-yaml'

const props = defineProps({
  list: {
    type: Array,
    default: () => []
  },
  hasOperate: {
    type: Boolean,
    default: true
  },
  operates: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['startDrag', 'select', 'scroll', 'operate'])

const _list: any = computed(() => {
  return props.list
})

onMounted(() => {})

function getIconInfo(type: number, algType: number) {
  return nodeIconWithBackGround(type, algType).icon
}

function onStartDrag(algo: any) {
  console.log(algo)
  const params = yaml.load(algo.algorithm.paramInfoTemplateYaml)
  emit('startDrag', {
    type: NODE_TYPES.ALGO,
    name: algo.name,
    dataset: {
      directory: false,
      algorithm: algo.id,
      inputParamTemplate: params?.input ? JSON.stringify(params?.input) : undefined,
      outputParamTemplate: params?.output ? JSON.stringify(params?.output) : undefined
    }
  })
}

function onClick(algo: any) {
  emit('select', algo)
}

function onScroll() {
  emit('scroll')
}

function handleCommand(command: string, data: any) {
  emit('operate', {
    command,
    data
  })
}

function getMenuList(_data: any) {
  return props.operates.filter((operate: any) => operate.permission.includes(+_data.id) || operate.permission.includes('all'))
}
</script>
<style lang="less" scoped>
.algo-list {
  list-style: none;
  padding: 0 !important;
  margin: 11px 0 0 0;
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
  .algo-icon-font {
    margin-right: 5px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  :deep(:focus-visible) {
    outline: none;
  }
}
.empty {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  img {
    width: 150px;
  }
}
</style>
