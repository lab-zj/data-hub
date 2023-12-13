import axios from '@/api/http'
/* eslint-disable */

export const dingUserLoginApi = (options: any) =>
  axios.request({
    ...options,
    url: `/user/ding-talk-user/login`,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })

export const userLoginApi = (options: any) =>
  axios.request({
    ...options,
    url: `/user/login`,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  })

export const userLogoutApi = () =>
  axios.request({
    url: `/user/logout`
  })

export const userRegisterApi = (options?: any) =>
  axios.request({
    ...options,
    url: '/database-user/register'
  })

export const userVerifyApi = (options?: any) =>
  axios.request({
    ...options,
    headers: {
      'Content-Type': 'text/plain'
    },
    url: '/database-user/verify'
  })

export const userUpdateApi = (options?: any) =>
  axios.request({
    ...options,
    url: '/database-user/update'
  })

export const userInitializeApi = (options?: any) =>
  axios.request({
    ...options,
    url: '/database-user/initialize'
  })

export const userBindApi = (options?: any) =>
  axios.request({
    ...options,
    url: '/database-user/bind'
  })

/*** ding talk */
export const dingUserVerifyApi = (options?: any) =>
  axios.request({
    ...options,
    url: '/ding-talk-user/verify'
  })

export const dingUserBindApi = (options?: any) =>
  axios.request({
    ...options,
    url: '/ding-talk-user/bind'
  })

export const dingUserUnBindApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'DELETE',
    url: '/ding-talk-user/bind'
  })

export const dingUserUrlAuthApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/ding-talk-user/url/auth'
  })

export const whoAmIApi = (options?: any) =>
  axios.request({
    ...options,
    method: 'GET',
    url: '/user/who-am-i'
  })
