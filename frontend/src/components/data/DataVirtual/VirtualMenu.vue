<template>
  <el-dropdown class="virtual-menu" trigger="hover">
    <el-button type=""><icon-font type="icon-shujuku" class="iconfont icon" />新建文件</el-button>
    <template #dropdown>
      <el-dropdown-menu class="virtual-dropdown-menu">
        <el-dropdown-item command="database" @click="onCreate('database')"> 数据库 </el-dropdown-item>
        <el-dropdown-item command="s3VirtualFile" @click="onCreate('s3VirtualFile')"> S3 </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>
<script setup lang="ts">
const emit = defineEmits(['create'])
function onCreate(type: string) {
  emit('create', type)
}
</script>
<style lang="less" scoped>
.virtual-menu {
  margin-left: 16px;

  .icon {
    margin-right: 10px;
  }
}

.virtual-dropdown-menu {
  :deep(.el-dropdown-menu__item) {
    width: 100%;
    padding: 6px 12px;
  }
}
</style>
