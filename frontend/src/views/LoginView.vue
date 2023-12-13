<template>
  <div class="container">
    <div class="background" />
    <div class="content">
      <img src="@/assets/login.png" />
      <div class="form-container">
        <div class="content-box">
          <p v-if="$route.path === '/login'" class="title">
            登录见微可视分析平台
          </p>
          <p v-else-if="$route.path === '/register'" class="title">
            注册见微可视分析平台
          </p>
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="less" scoped>

.container {
  align-items: center;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: center;
  min-height: 100vh;
  width: 100%;
}

.background {
  background-image: linear-gradient(134deg, #f5f5ff 0%, #e7e8ff 100%);
  opacity: 0.3;
}

.logo {
  height: 50px;
  left: 32px;
  position: absolute;
  top: 32px;
}

.content {
  align-items: center;
  border-radius: 4px;
  box-shadow: 0 0 8px 0 rgba(5, 26, 179, 0.15),
    0 4px 24px 0 rgba(73, 101, 166, 0.2);
  display: flex;
  flex-direction: row;
  justify-content: center;

  img {
    height: 636px;
    width: 744px;
  }

  .form-container {
    align-content: center;
    align-items: center;
    background: #fff;
    display: flex;
    flex-flow: row nowrap;
    height: 636px;
    overflow-y: auto;
    text-align: center;
    width: 514px;

    &::-webkit-scrollbar {
      height: 10px;
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: rgb(239, 239, 239);
      border-radius: 2px;
    }

    &::-webkit-scrollbar-thumb {
      background: #bfbfbf;
      border-radius: 6px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: #333;
    }

    &::-webkit-scrollbar-corner {
      background: #179a16;
    }

    .content-box {
      width: 514px;
    }
  }
}

.title {
  color: #6973ff;
  font-size: 24px;
  font-weight: 600;
  margin: 40px;
  // margin-bottom: 41px;
  // margin-top: 168px;
}
</style>
