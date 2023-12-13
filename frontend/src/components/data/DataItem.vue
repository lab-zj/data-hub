<template>
  <div class="data-item" :class="{ active: dropdownMenuVisible, checked:checked,edit: data.editType}">
    <div class="top-btns">
      <el-checkbox v-model="checked" class="check-btn"></el-checkbox>
      <el-dropdown @visible-change="dropMenuVisibleChange">
        <i class="iconfont icon-daohang-gengduocaozuobeifen more-menu" />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="operateFunc('download')">下载</el-dropdown-item>
            <el-dropdown-item disabled>导入到项目</el-dropdown-item>
            <el-dropdown-item @click="operateFunc('edit')">重命名</el-dropdown-item>
            <el-dropdown-item>移动</el-dropdown-item>
            <el-dropdown-item  @click="operateFunc('delete')">删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <div class="check-item" align="center">
      <router-link :to="getRouterLink(props.data)">
        <div class="icon">
          <i v-if="data.directory" class="iconfont icon-a-wenjianjiashouqibeifen2" />
          <!-- <i v-else :class="getFileTypeOrIcon(props.data.name,true)" class="iconfont" /> -->
          <icon-font v-else :type="getFileTypeOrIcon(props.data.name,true)" />
        </div>

        <div v-show="data.editType" class="input-div">
            <el-input
              v-model:value="name"
              ref="nameInput2"
              oninput="value=value.replace(/[, ]/g,'')"
              onkeyup="value=value.replace(/[, ]/g,'')"
              placeholder="请输入文件夹名"
              size="small"
            >
            </el-input>
            <div class="input-btns">
              <el-button @click="operateFunc('cancel')" size="small">取消</el-button>
              <el-button @click="operateFunc(data.editType)" size="small" type="primary">确认</el-button>
            </div>
        </div>
        <div v-show="!data.editType" class="normal-div">
          <span>{{ data.name }}</span>
          <span class="time-span">time:{{ data.name }}</span>
        </div>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { getCurrentInstance, nextTick, ref, watch, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  data: {
    type: Object,
    default(): Object {
      return {}
    }
  },
  index: { type: Number, default: 0 },
  checkList: { type: Array, default(): Object {
      return []
    } },
})
let name: any = ref<string>('')
let checked: any = ref(false)
const dropdownMenuVisible: any = ref(false)

const emit = defineEmits<{
  (e: 'operate', type: string, data: any, index: any): void
  (e: 'check',  checked: any): void
}>()

// watch(
//   () => props.data.editType,
//   (newValue: any) => {
//     if (newValue) {
//       // 输入框聚焦
//       const inputWrapper = instance?.refs.nameInput2 as HTMLInputElement
//       nextTick(() => {
//         inputWrapper?.focus()
//         // console.log(2)
//       })
//     }
//   }
// )

watch(() => props.checkList,(newValue: any) => {
  console.log('checklist change',newValue,newValue.includes(props.data.name))
  checked.value = newValue.includes(props.data.name)
//   stopWatchChecked() // 停止监听checked
//   // 重启监听checked
//   stopWatchChecked = watch(checked,()=>{
//     let checkList = props.checkList
//     const index = checkList.findIndex((item: any)=>item === props.data.name)
//     if (index < 0){
//       checkList.push(props.data.name)
//     }else{
//       checkList.splice(index, 1)
//     }
//     emit('check',checkList)
//   })
},{deep: true})

// let stopWatchChecked = watch(checked,()=>{
//   let checkList = props.checkList
//   const index = checkList.findIndex((item: any)=>item === props.data.name)
//   console.log('watch check',index)
//   if (index < 0){
//     checkList.push(props.data.name)
//   }else{
//     checkList.splice(index, 1)
//   }
//   emit('check',checkList)
// },{deep: true})

const route = useRoute()
const getRouterLink = (record: any) => {
  if (!record.directory || record.editType) { // 点击非文件夹定位到当前文件夹本级
    return route?.path
  }
  return `${route.path.indexOf('path') > -1 && record.directory ? `${route.path}%2F` : `${route.path}/path=`}${
    record.name}`
}

const dropMenuVisibleChange = (value: any) => {
  dropdownMenuVisible.value = value
}

// 根据文件名返回文件类型
const getFileTypeOrIcon = (name: any,isIcon: boolean) =>{
  let type: any = ''
  if (name.split('.').length > 1)
  {
    type = name.split('.')[1]
  }
  if (isIcon){
    switch (type) {
      case 'txt': return 'icon-wendang'
      case 'png': return 'icon-tupian'
      case 'mp4': return 'icon-shipin'
      case 'csv': return 'icon-shuju'
      default: return 'icon-feichangguiwenjian'
    }
  }
  return ''
}

const operateFunc = (type: any) => {
  console.log('data-item', type)
  if (type === 'edit' || type === 'create') {
    if (type === 'edit') {
      name.value = props.data.name
    }
    const data = {
      editType: type,
      directory: props.data.directory,
      name: name.value
    }
    emit('operate', type, data, props.index)
  } else {
    emit('operate', type, props.data, props.index)
  }
}
</script>

<style lang="less" scoped>
@import '@/constants';

.data-item {
  padding: 8px;
  color: #5d637e;
  border-radius: 4px;
  width: calc(@DATA_CARD_WIDTH - 16px);
  height: calc(@DATA_CARD_HEIGHT - 16px);

  .top-btns {
    display: flex;
    justify-content: space-between;
    width: 100%;
    visibility: hidden;

    .check-btn {
      height: 14px;
    }

    .more-menu {
      cursor: pointer;
      outline: none;
      &:hover {
        color: #6973ff;
      }
    }

    .confirm-btns {
      background: #fff;
      padding: 3px;
      border-radius: 6px;
      width: 43px;
      display: flex;
      justify-content: space-between;
      visibility: hidden;

      .icon {
        cursor: pointer;
        font-size: 17px;
        color: #6973ff;
      }
    }
  }

  span {
    color: #373B52;
    width: calc(@DATA_CARD_WIDTH - 16px);
    text-align: center;
    overflow: hidden;
    text-overflow: ellipsis;
    word-break: break-all;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    line-height: 20px;
    letter-spacing: 1px;
  }

  .check-item {
    // padding: 32px 16px 16px 8px;
    a {
      color: #5d637e;
    }

    .icon {
      line-height: 44px;

      i {
        font-size: 44px;
      }
    }

    .normal-div {
      .time-span {
        color: #5D637E;
        line-height: 22px;
        width: calc(@DATA_CARD_WIDTH - 16px);
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        line-height: 17px;
        letter-spacing: 1px;
      }
    }
    .input-div {
      // display: flex;
      // align-items: center;

      .el-input{
        margin-right: 12px;
        width: 100%;
        
      }
      .input-btns {
        margin-top: 12px;
        display: flex;
        justify-content: space-between;

        .el-button {
          padding: 8px 5px;
        }
      }
    }
  }

  &:hover {
    background-color: rgba(103, 113, 252, 0.1);

    .top-btns {
      visibility: visible !important;
    }
  }
  
  &.active {
    background-color: rgba(103, 113, 252, 0.1);

    .top-btns {
      visibility: visible !important;
    }
  }
  &.checked {
    background-color: rgba(103, 113, 252, 0.1);

    .top-btns {
      .check-btn {
        visibility: visible;
      }
    }
  }
  &.edit {
    background-color: rgba(103, 113, 252, 0.1);
  }
}
</style>
