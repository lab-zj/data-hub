/**
 * request base axios
 * @author zhangfanfan
 */

import axios, { type InternalAxiosRequestConfig } from 'axios'
import type { AxiosResponse } from 'axios'
import { baseURL } from '@/api/axios-config'
import { ElMessage } from 'element-plus'

const publicPath = process.env.BASE_URL || ''
const disableMessageErrorCode: Set<number> = new Set([416, 417]) // 不需要提示信息的errcode

const instance = axios.create({
  baseURL,
  method: 'post',
  headers: {
    post: {
      'Content-Type': 'application/json;charset=utf-8'
    },
    'Access-Control-Allow-Origin': '*'
  }
})

// Add a request interceptor
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Do something before request is sent
    // return config
    const { method, data } = config
    let getConfig = {}
    if (method?.toUpperCase() === 'GET') {
      getConfig = {
        params: {
          ...data,
        },
      }
    }
    return {
      ...config,
      ...getConfig,
    }
  },
  (error: any) => {
    // Do something with request error
    ElMessage.error('请求超时:', error.toString())
    return Promise.reject(error)
  }
)

// Add a response interceptor
instance.interceptors.response.use(
  (response: AxiosResponse) => {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    if (disableMessageErrorCode.has(response.data.code)) {
      // 不需要提示信息的
      return response
    }
    switch (response.data.code) {
      case 200:
        return response
      // case 20012: // 未登陆
      //   //  store original url
      //   window.localStorage.setItem('originalUrl', window.location.pathname)

      //   window.location.href = `${publicPath}${publicPath.endsWith('/') ? '' : '/'}login`
      //   return response
      case 401: // 无权限访问
        ElMessage.info('无权限访问')
        setTimeout(() => {
          window.location.href = `${publicPath}${publicPath.endsWith('/') ? '' : '/'}management/projects`
        }, 1000)
        return response
      case undefined: // 下载
        return response
      case 500: // 服务器异常
        ElMessage.error('服务器异常')
        return response
      case 403: // forbidden
        // window.location.href = `${publicPath}${publicPath.endsWith('/') ? '' : '/'}login`
        return response
      default:
        return response
    }
  },
  (error) => {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    // console.log('error:', error.response.data.code)
    // console.log(error.response)
    // ElMessage.error(error.response.data.message)

    // 未登录
    // if (error.response.data.code === 301) {
    //   window.location.href = `${publicPath}${
    //     publicPath.endsWith('/') ? '' : '/'
    //   }login`
    // }
    return Promise.reject(error)
  }
)

export default instance
